-- ============================================================
-- SUPABASE SCHEMA - ARSIP KEUANGAN BPKPAD BALANGAN
-- Fresh database schema for financial archive flow:
-- SPP -> SPM -> SP2D -> SPJ
--
-- Main flow:
-- 1) Create physical box/location first in storage_locations.
-- 2) Insert documents directly into archive_documents.
-- 3) Use transaction_bundles for one disbursement flow.
-- 4) ORIGINAL documents always have copy_count = 1.
-- 5) COPY documents may have copy_count >= 1.
-- 6) SPJ cannot exist without a transaction bundle.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- 1. ENUMS
-- ============================================================

DO $$
BEGIN
  CREATE TYPE public.doc_type AS ENUM ('SPP', 'SPM', 'SP2D', 'SPJ');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
  CREATE TYPE public.doc_status AS ENUM ('AVAILABLE', 'BORROWED', 'DISPOSED', 'UNVERIFIED');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
  CREATE TYPE public.doc_condition AS ENUM ('GOOD', 'DAMAGED', 'LOST');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
  CREATE TYPE public.document_copy_type AS ENUM ('ORIGINAL', 'COPY');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

-- ============================================================
-- 2. CLASSIFICATIONS
-- ============================================================

CREATE TABLE IF NOT EXISTS public.archive_classifications (
  code text PRIMARY KEY,
  name text NOT NULL,
  parent_code text REFERENCES public.archive_classifications(code),
  level integer NOT NULL DEFAULT 1 CHECK (level > 0),
  is_active boolean NOT NULL DEFAULT true,
  created_at timestamp with time zone NOT NULL DEFAULT now()
);

INSERT INTO public.archive_classifications (code, name, parent_code, level, is_active)
VALUES
  ('900', 'Keuangan', NULL, 1, true),
  ('900.1', 'Pengelolaan Keuangan', '900', 2, true),
  ('900.1.3', 'Pelaksanaan Anggaran', '900.1', 3, true),
  ('900.1.3.1', 'Dokumen Pencairan dan Pertanggungjawaban Keuangan', '900.1.3', 4, true)
ON CONFLICT (code) DO UPDATE SET
  name = EXCLUDED.name,
  parent_code = EXCLUDED.parent_code,
  level = EXCLUDED.level,
  is_active = EXCLUDED.is_active;

-- ============================================================
-- 3. STORAGE LOCATIONS
-- One row represents one physical box/rack location.
-- Android flow: Create Box Screen -> insert into storage_locations.
-- ============================================================

CREATE TABLE IF NOT EXISTS public.storage_locations (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  room text NOT NULL,
  shelf text NOT NULL,
  box_number text NOT NULL,
  description text,
  is_active boolean NOT NULL DEFAULT true,
  created_by uuid REFERENCES auth.users(id),
  created_at timestamp with time zone NOT NULL DEFAULT now(),

  CONSTRAINT uq_storage_box UNIQUE (room, shelf, box_number)
);

-- ============================================================
-- 4. TRANSACTION BUNDLES
-- Parent table for one financial disbursement flow.
-- A bundle may contain SPP, SPM, SP2D, and SPJ.
-- Columns in archive_documents reference this table through bundle_id.
-- ============================================================

CREATE TABLE IF NOT EXISTS public.transaction_bundles (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  bundle_name text NOT NULL,
  bundle_number text,
  year integer NOT NULL CHECK (year >= 1900 AND year <= 2100),
  nominal decimal(15, 2),
  third_party text,
  description text,
  classification_code text NOT NULL DEFAULT '900.1.3.1'
    REFERENCES public.archive_classifications(code),
  metadata jsonb NOT NULL DEFAULT '{}'::jsonb,

  created_by uuid REFERENCES auth.users(id),
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now(),
  deleted_at timestamp with time zone
);

CREATE INDEX IF NOT EXISTS idx_transaction_bundles_year
  ON public.transaction_bundles(year);

CREATE INDEX IF NOT EXISTS idx_transaction_bundles_deleted_at
  ON public.transaction_bundles(deleted_at);

-- ============================================================
-- 5. ARCHIVE DOCUMENTS
-- Main table for archive records.
--
-- Business rules:
-- - document_type is financial only: SPP, SPM, SP2D, SPJ.
-- - ORIGINAL always has copy_count = 1.
-- - COPY can have copy_count >= 1.
-- - SPJ requires bundle_id.
-- - classification_code defaults to 900.1.3.1.
-- - metadata JSONB can store denormalized storage labels:
--   {
--     "room": "Gudang A",
--     "shelf": "Rak 01",
--     "box_number": "BOX-2026-001"
--   }
-- ============================================================

CREATE TABLE IF NOT EXISTS public.archive_documents (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),

  document_type public.doc_type NOT NULL,
  document_number text,

  copy_type public.document_copy_type NOT NULL DEFAULT 'ORIGINAL',
  copy_count integer NOT NULL DEFAULT 1,

  classification_code text NOT NULL DEFAULT '900.1.3.1'
    REFERENCES public.archive_classifications(code),

  description text,
  nominal decimal(15, 2),
  third_party text,
  year integer NOT NULL CHECK (year >= 1900 AND year <= 2100),

  condition public.doc_condition NOT NULL DEFAULT 'GOOD',
  status public.doc_status NOT NULL DEFAULT 'AVAILABLE',

  metadata jsonb NOT NULL DEFAULT '{}'::jsonb,

  storage_location_id uuid NOT NULL REFERENCES public.storage_locations(id) ON DELETE RESTRICT,
  bundle_id uuid REFERENCES public.transaction_bundles(id) ON DELETE RESTRICT,

  created_by uuid REFERENCES auth.users(id),
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  updated_at timestamp with time zone NOT NULL DEFAULT now(),
  deleted_at timestamp with time zone,

  CONSTRAINT chk_copy_count_positive
    CHECK (copy_count >= 1),

  CONSTRAINT chk_original_copy_count_one
    CHECK (
      (copy_type = 'ORIGINAL'::public.document_copy_type AND copy_count = 1)
      OR
      (copy_type = 'COPY'::public.document_copy_type AND copy_count >= 1)
    ),

  CONSTRAINT chk_spj_requires_bundle
    CHECK (
      document_type <> 'SPJ'::public.doc_type
      OR bundle_id IS NOT NULL
    )
);

CREATE INDEX IF NOT EXISTS idx_archive_documents_year
  ON public.archive_documents(year);

CREATE INDEX IF NOT EXISTS idx_archive_documents_type
  ON public.archive_documents(document_type);

CREATE INDEX IF NOT EXISTS idx_archive_documents_status
  ON public.archive_documents(status);

CREATE INDEX IF NOT EXISTS idx_archive_documents_storage_location
  ON public.archive_documents(storage_location_id);

CREATE INDEX IF NOT EXISTS idx_archive_documents_bundle
  ON public.archive_documents(bundle_id);

CREATE INDEX IF NOT EXISTS idx_archive_documents_deleted_at
  ON public.archive_documents(deleted_at);

CREATE INDEX IF NOT EXISTS idx_archive_documents_metadata_gin
  ON public.archive_documents USING gin (metadata);

-- Prevent duplicate ORIGINAL document rows with the same identity.
CREATE UNIQUE INDEX IF NOT EXISTS uq_archive_original_document
  ON public.archive_documents(document_type, document_number, year)
  WHERE copy_type = 'ORIGINAL'::public.document_copy_type
    AND document_number IS NOT NULL
    AND deleted_at IS NULL;

-- Prevent duplicate COPY group rows.
-- If there are more copies, update copy_count instead of creating repeated copy rows.
CREATE UNIQUE INDEX IF NOT EXISTS uq_archive_copy_document
  ON public.archive_documents(document_type, document_number, year)
  WHERE copy_type = 'COPY'::public.document_copy_type
    AND document_number IS NOT NULL
    AND deleted_at IS NULL;

-- One active bundle should not contain two active documents of the same type and copy type.
CREATE UNIQUE INDEX IF NOT EXISTS uq_archive_bundle_document_type_copy
  ON public.archive_documents(bundle_id, document_type, copy_type)
  WHERE bundle_id IS NOT NULL
    AND deleted_at IS NULL;

-- ============================================================
-- 6. DOCUMENT PLACEMENTS
-- History of physical placement/movement.
-- This does not replace archive_documents.storage_location_id.
-- It records movement history only.
-- ============================================================

CREATE TABLE IF NOT EXISTS public.document_placements (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  archive_document_id uuid NOT NULL REFERENCES public.archive_documents(id) ON DELETE CASCADE,
  storage_location_id uuid NOT NULL REFERENCES public.storage_locations(id) ON DELETE RESTRICT,
  placed_at timestamp with time zone NOT NULL DEFAULT now(),
  removed_at timestamp with time zone,
  note text,
  created_by uuid REFERENCES auth.users(id)
);

CREATE INDEX IF NOT EXISTS idx_document_placements_archive_document
  ON public.document_placements(archive_document_id);

CREATE INDEX IF NOT EXISTS idx_document_placements_storage_location
  ON public.document_placements(storage_location_id);

-- ============================================================
-- 7. ACTIVITY LOGS
-- Audit trail table.
-- In development mode, actor_id may be NULL because app has no login yet.
-- ============================================================

CREATE TABLE IF NOT EXISTS public.activity_logs (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  actor_id uuid REFERENCES auth.users(id),
  action text NOT NULL,
  entity_type text NOT NULL,
  entity_id uuid,
  metadata jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_at timestamp with time zone NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_activity_logs_entity
  ON public.activity_logs(entity_type, entity_id);

CREATE INDEX IF NOT EXISTS idx_activity_logs_created_at
  ON public.activity_logs(created_at);

-- ============================================================
-- 8. TRIGGERS
-- ============================================================

-- 8A. updated_at trigger helper
CREATE OR REPLACE FUNCTION public.set_updated_at()
RETURNS trigger AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_transaction_bundles_updated_at ON public.transaction_bundles;
CREATE TRIGGER trg_transaction_bundles_updated_at
BEFORE UPDATE ON public.transaction_bundles
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

DROP TRIGGER IF EXISTS trg_archive_documents_updated_at ON public.archive_documents;
CREATE TRIGGER trg_archive_documents_updated_at
BEFORE UPDATE ON public.archive_documents
FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

-- 8B. Normalize original copy_count.
-- Even if Android sends copy_count = 2 for ORIGINAL, DB resets it to 1.
CREATE OR REPLACE FUNCTION public.normalize_archive_copy_count()
RETURNS trigger AS $$
BEGIN
  IF NEW.copy_type = 'ORIGINAL'::public.document_copy_type THEN
    NEW.copy_count := 1;
  END IF;

  IF NEW.copy_type = 'COPY'::public.document_copy_type AND NEW.copy_count < 1 THEN
    RAISE EXCEPTION 'copy_count for COPY documents must be at least 1';
  END IF;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_normalize_archive_copy_count ON public.archive_documents;
CREATE TRIGGER trg_normalize_archive_copy_count
BEFORE INSERT OR UPDATE OF copy_type, copy_count ON public.archive_documents
FOR EACH ROW EXECUTE FUNCTION public.normalize_archive_copy_count();

-- 8C. Denormalize storage label into metadata.
-- This makes Android list display faster without heavy joins.
CREATE OR REPLACE FUNCTION public.inject_storage_metadata()
RETURNS trigger AS $$
DECLARE
  loc record;
BEGIN
  SELECT room, shelf, box_number
  INTO loc
  FROM public.storage_locations
  WHERE id = NEW.storage_location_id;

  IF FOUND THEN
    NEW.metadata :=
      COALESCE(NEW.metadata, '{}'::jsonb)
      || jsonb_build_object(
        'room', loc.room,
        'shelf', loc.shelf,
        'box_number', loc.box_number
      );
  END IF;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_inject_storage_metadata ON public.archive_documents;
CREATE TRIGGER trg_inject_storage_metadata
BEFORE INSERT OR UPDATE OF storage_location_id ON public.archive_documents
FOR EACH ROW EXECUTE FUNCTION public.inject_storage_metadata();

-- 8D. Audit log trigger.
-- For development without login, actor_id can be NULL.
CREATE OR REPLACE FUNCTION public.write_activity_log()
RETURNS trigger AS $$
DECLARE
  v_entity_id uuid;
  v_metadata jsonb;
  v_actor_id uuid;
BEGIN
  IF TG_OP = 'DELETE' THEN
    v_entity_id := OLD.id;
    v_metadata := to_jsonb(OLD);
    -- Robustly handle missing created_by column
    BEGIN
      v_actor_id := OLD.created_by;
    EXCEPTION WHEN undefined_column THEN
      v_actor_id := NULL;
    END;
  ELSE
    v_entity_id := NEW.id;
    v_metadata := to_jsonb(NEW);
    -- Robustly handle missing created_by column
    BEGIN
      v_actor_id := NEW.created_by;
    EXCEPTION WHEN undefined_column THEN
      v_actor_id := NULL;
    END;
  END IF;

  INSERT INTO public.activity_logs(actor_id, action, entity_type, entity_id, metadata)
  VALUES (v_actor_id, TG_OP, TG_TABLE_NAME, v_entity_id, v_metadata);

  RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_log_storage_locations ON public.storage_locations;
CREATE TRIGGER trg_log_storage_locations
AFTER INSERT OR UPDATE OR DELETE ON public.storage_locations
FOR EACH ROW EXECUTE FUNCTION public.write_activity_log();

DROP TRIGGER IF EXISTS trg_log_transaction_bundles ON public.transaction_bundles;
CREATE TRIGGER trg_log_transaction_bundles
AFTER INSERT OR UPDATE OR DELETE ON public.transaction_bundles
FOR EACH ROW EXECUTE FUNCTION public.write_activity_log();

DROP TRIGGER IF EXISTS trg_log_archive_documents ON public.archive_documents;
CREATE TRIGGER trg_log_archive_documents
AFTER INSERT OR UPDATE OR DELETE ON public.archive_documents
FOR EACH ROW EXECUTE FUNCTION public.write_activity_log();

DROP TRIGGER IF EXISTS trg_log_document_placements ON public.document_placements;
CREATE TRIGGER trg_log_document_placements
AFTER INSERT OR UPDATE OR DELETE ON public.document_placements
FOR EACH ROW EXECUTE FUNCTION public.write_activity_log();

-- ============================================================
-- 9. DEVELOPMENT RLS POLICIES
-- WARNING:
-- These public policies are ONLY for development because the Android app
-- currently has no login feature.
-- For production, replace them with authenticated-user policies.
-- ============================================================

ALTER TABLE public.archive_classifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.storage_locations ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.transaction_bundles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.archive_documents ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.document_placements ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.activity_logs ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "dev_public_all_archive_classifications" ON public.archive_classifications;
CREATE POLICY "dev_public_all_archive_classifications"
ON public.archive_classifications FOR ALL TO public
USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "dev_public_all_storage_locations" ON public.storage_locations;
CREATE POLICY "dev_public_all_storage_locations"
ON public.storage_locations FOR ALL TO public
USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "dev_public_all_transaction_bundles" ON public.transaction_bundles;
CREATE POLICY "dev_public_all_transaction_bundles"
ON public.transaction_bundles FOR ALL TO public
USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "dev_public_all_archive_documents" ON public.archive_documents;
CREATE POLICY "dev_public_all_archive_documents"
ON public.archive_documents FOR ALL TO public
USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "dev_public_all_document_placements" ON public.document_placements;
CREATE POLICY "dev_public_all_document_placements"
ON public.document_placements FOR ALL TO public
USING (true) WITH CHECK (true);

DROP POLICY IF EXISTS "dev_public_all_activity_logs" ON public.activity_logs;
CREATE POLICY "dev_public_all_activity_logs"
ON public.activity_logs FOR ALL TO public
USING (true) WITH CHECK (true);

-- ============================================================
-- 10. OPTIONAL TEST INSERT EXAMPLE
-- Uncomment only for manual testing.
-- ============================================================

/*
INSERT INTO public.storage_locations(room, shelf, box_number, description)
VALUES ('Gudang Arsip', 'Rak A1', 'BOX-2026-001', 'Box dokumen keuangan tahun 2026')
RETURNING id;

-- Replace the UUID below with the returned storage location id.
INSERT INTO public.transaction_bundles(bundle_name, bundle_number, year, nominal, third_party)
VALUES ('Bundle Pencairan Belanja ATK 2026', 'BND-2026-001', 2026, 1500000, 'PT Contoh Rekanan')
RETURNING id;

-- Replace both UUIDs below.
INSERT INTO public.archive_documents(
  document_type,
  document_number,
  copy_type,
  copy_count,
  description,
  nominal,
  third_party,
  year,
  storage_location_id,
  bundle_id
)
VALUES
('SP2D', 'SP2D/001/2026', 'ORIGINAL', 9, 'SP2D Belanja ATK', 1500000, 'PT Contoh Rekanan', 2026, '<storage_location_id>', '<bundle_id>');

-- copy_count above will become 1 automatically because copy_type = ORIGINAL.
*/
