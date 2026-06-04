-- Supabase Schema for BPKPAD Archive Management System

-- 1. Enums
CREATE TYPE doc_type AS ENUM ('SP2D', 'SPM', 'SPJ');
CREATE TYPE doc_status AS ENUM ('AVAILABLE', 'BORROWED', 'DISPOSED', 'UNVERIFIED');

-- 2. Physical Storage Locations
CREATE TABLE storage_locations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 3. Main Archive Table (Arsip Keuangan)
CREATE TABLE arsip_keuangan (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type doc_type NOT NULL,
    document_number TEXT UNIQUE NOT NULL,
    nominal DECIMAL(15, 2),
    third_party TEXT,
    year INTEGER NOT NULL,
    date_issued DATE,
    status doc_status DEFAULT 'UNVERIFIED',
    id_storage_location UUID REFERENCES storage_locations(id) ON DELETE SET NULL,
    metadata JSONB, -- Flexible JSON for extra data like bank_name, account_number, etc.
    created_by UUID REFERENCES auth.users(id),
    verified_by UUID REFERENCES auth.users(id),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- 4. Transaction Bundles (Relation between SP2D, SPM, SPJ)
CREATE TABLE transaction_bundles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bundle_name TEXT NOT NULL,
    sp2d_id UUID REFERENCES arsip_keuangan(id) ON DELETE SET NULL,
    spm_id UUID REFERENCES arsip_keuangan(id) ON DELETE SET NULL,
    spj_id UUID REFERENCES arsip_keuangan(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 5. Audit Logs
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_user UUID REFERENCES auth.users(id),
    id_document UUID REFERENCES arsip_keuangan(id) ON DELETE CASCADE,
    action TEXT NOT NULL, -- INSERT, UPDATE, DELETE
    previous_data JSONB,
    new_data JSONB,
    timestamp TIMESTAMPTZ DEFAULT now()
);

-- 6. Audit Trigger Function
CREATE OR REPLACE FUNCTION process_archive_audit()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'DELETE') THEN
        INSERT INTO audit_logs (id_user, id_document, action, previous_data)
        VALUES (auth.uid(), OLD.id, TG_OP, to_jsonb(OLD));
        RETURN OLD;
    ELSIF (TG_OP = 'UPDATE') THEN
        INSERT INTO audit_logs (id_user, id_document, action, previous_data, new_data)
        VALUES (auth.uid(), NEW.id, TG_OP, to_jsonb(OLD), to_jsonb(NEW));
        RETURN NEW;
    ELSE
        INSERT INTO audit_logs (id_user, id_document, action, new_data)
        VALUES (auth.uid(), NEW.id, TG_OP, to_jsonb(NEW));
        RETURN NEW;
    END IF;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 7. Apply Trigger to Archive Table
CREATE TRIGGER archive_audit_trigger
AFTER INSERT OR UPDATE OR DELETE ON arsip_keuangan
FOR EACH ROW EXECUTE FUNCTION process_archive_audit();

-- 8. Enable Row Level Security (RLS)
ALTER TABLE arsip_keuangan ENABLE ROW LEVEL SECURITY;
ALTER TABLE storage_locations ENABLE ROW LEVEL SECURITY;
ALTER TABLE transaction_bundles ENABLE ROW LEVEL SECURITY;
ALTER TABLE audit_logs ENABLE ROW LEVEL SECURITY;

-- 9. Basic RLS Policies (Allow authenticated users to read/write)
CREATE POLICY "Allow authenticated read" ON arsip_keuangan FOR SELECT TO authenticated USING (true);
CREATE POLICY "Allow authenticated insert" ON arsip_keuangan FOR INSERT TO authenticated WITH CHECK (true);
CREATE POLICY "Allow authenticated update" ON arsip_keuangan FOR UPDATE TO authenticated USING (true);
