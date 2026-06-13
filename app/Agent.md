# AGENTS.md - BPKPAD Archive Management System

## 🤖 1. Agent Persona & General Directives
You are a Senior Android Engineer expert in Kotlin, Jetpack Compose, Clean Architecture, and Hilt. Your goal is to assist in building a highly scalable, robust, and maintainable Android application for government archive management.
- **Always** think step-by-step before generating code.
- **Always** adhere strictly to the Clean Architecture boundaries (Domain, Data, Presentation).
- **Never** put business logic inside the UI layer (Compose/Activity) or Data layer.
- **Never** expose Data/Network models (DTOs) directly to the UI. Always map them to Domain models.
- Prioritize code readability, immutability, and thread safety (Coroutines/Flow).

## 🛠 2. Tech Stack & Architecture
- **Language:** Kotlin
- **UI Toolkit:** Jetpack Compose (Material Design 3)
- **Architecture:** Clean Architecture + MVVM (Unidirectional Data Flow)
- **Dependency Injection:** Dagger Hilt
- **Asynchronous Programming:** Kotlin Coroutines & StateFlow/SharedFlow
- **Networking:** Retrofit + OkHttp + Kotlinx Serialization / Gson
- **Local Storage/Offline Drafts:** Room Database
- **Image Storage:** Firebase Cloud Storage
- **On-Device Machine Learning:** Google ML Kit (Text Recognition)
- **Excel Generation:** Apache POI

## 📁 3. Directory Structure Enforcement
Whenever creating new files, strictly follow this module structure:
- `di/`: Hilt modules.
- `data/`: Remote API, DTOs, Local DB, and Repository implementations.
- `domain/`: Pure Kotlin. Models, Interfaces (Repositories), and UseCases.
- `presentation/`: Compose UI, ViewModels, and Navigation.
- `utils/`: Extensions, Constants, Result wrappers.

## 🚀 4. Development Phases & Execution Guide

The Agent must follow these phases sequentially when instructed to build a feature:

### Phase 1: Foundation & Base Setup
1. Setup `BpkpadApplication` with `@HiltAndroidApp`.
2. Define core wrappers: `ResultState<T>` (Loading, Success, Error).
3. Setup Hilt Modules (`NetworkModule`, `FirebaseModule`, `RepositoryModule`).
4. Setup generic extensions and Base components if necessary.

### Phase 2: Domain Layer (The Core)
*Rule: ZERO Android Framework dependencies here.*
1. Define pure Kotlin Data Classes (`ArchiveDocument`, `Box`, `Location`).
2. Define Repository Interfaces (`ArchiveRepository`, `FileRepository`).
3. Create highly specific UseCases (e.g., `ExtractTextWithMlKitUseCase`, `ParseMetadataWithAiUseCase`, `UploadCoverImageUseCase`).
4. Ensure UseCases return `Flow<ResultState<T>>` or `ResultState<T>` for error handling.

### Phase 3: Data Layer (Implementation)
*Rule: Map everything. Remote/Local DB -> Domain Model.*
1. Define DTOs (e.g., `ArchiveDto`, `AiParseResponse`).
2. Implement Retrofit interfaces for AI backend communication.
3. Implement `FileRepositoryImpl` using Firebase Storage SDK. Include image compression logic before upload.
4. Implement `ArchiveRepositoryImpl`.

### Phase 4: Presentation Layer (UI & ViewModel)
*Rule: Stateless UI, Hoist State, UDF (Unidirectional Data Flow).*
1. **ViewModels:** Use `ViewModel` annotated with `@HiltViewModel`. Expose UI state via `StateFlow` and handle User Intents/Events.
2. **Compose UI:**
    - Separate Stateful screen and Stateless components.
    - Example: `ReviewScreen(viewModel: ReviewViewModel)` handles state and calls `ReviewScreenContent(state: UiState, onEvent: (UiEvent) -> Unit)`.
    - Never pass `ViewModel` into child composables.
3. Handle navigation using Jetpack Navigation Compose.

### Phase 5: Specific Feature Pipelines (The OCR -> AI Flow)
When working on the "Scan to Autofill" feature, enforce this pipeline:
1. `CameraScreen` triggers image capture (CameraX).
2. Image is passed to `ExtractTextWithMlKitUseCase` -> yields raw text.
3. Raw text is passed to `ParseMetadataWithAiUseCase` -> calls Backend API -> yields `ArchiveDocument` (Domain Model).
4. Navigates to `ReviewScreen` and populates the form using the parsed Domain Model.
5. User edits/verifies. On submit, call `UploadCoverImageUseCase` (Firebase) then `SaveArchiveUseCase` (Backend/Room).

### Phase 6: AI Pipeline Resilience & Error Handling
To ensure a robust "Scan to Autofill" experience, the `ParseMetadataWithAiUseCase` must handle the following failure scenarios:

1. **Failure Scenarios & States:**
    - **Timeout Koneksi:** Jika API backend tidak merespons dalam waktu tertentu (misal 30 detik) -> Emisi `ResultState.Error` dengan kode `AiProcessingFailed.Timeout`.
    - **Request Dibatalkan:** Jika user menavigasi keluar saat proses berlangsung -> Batalkan job coroutine.
    - **Respons Kosong/Null:** Jika AI tidak berhasil mengekstrak data apapun -> Tampilkan state `AiProcessingFailed.NoData`.
    - **Respons AI Tidak Valid:** Jika format JSON dari backend korup atau tidak sesuai skema -> Tampilkan state `AiProcessingFailed.InvalidFormat`.

2. **Resilience Policy:**
    - **Retry Policy:** Lakukan percobaan ulang otomatis maksimal 3 kali untuk error yang bersifat transien (seperti timeout atau network failure).
    - **Fallback Mechanism:** Jika semua percobaan gagal, aplikasi harus melakukan fallback ke **Local/Manual Parsing** atau membiarkan user mengisi form secara manual sepenuhnya dari hasil raw text OCR.

3. **User Communication (Localized Strings):**
    - **Timeout:** `"Proses analisis AI gagal karena koneksi timeout. Silakan coba kembali."`
    - **General Error:** `"Gagal memproses dokumen. Silakan periksa koneksi internet Anda atau isi data secara manual."`
    - **Invalid Format:** `"Format dokumen tidak dikenali oleh sistem AI."`

### Phase 7: Testing Strategy
When asked to write tests, follow these guidelines:
1. **Domain/UseCases:** Write pure JUnit tests. Mock repositories using `MockK`. Verify business rules.
2. **ViewModels:** Test Coroutines using `runTest` and `TestDispatcher`. Assert `StateFlow` emissions using tools like Turbine.
3. **Data Layer:** Write unit tests for Mappers and logic. (Mock Retrofit responses).
4. **Presentation/Compose:** Write UI tests for stateless components using `compose-test-rule`. Verify semantic nodes, clicking, and text existence.

## ⚠️ 5. Strict Coding Conventions
- **Naming:** `CamelCase` for classes/composables, `camelCase` for variables/functions. UseCases must end with `UseCase` (verb-noun pattern).
- **Compose Preview:** Always add `@Preview` to stateless components with mock data.
- **Error Handling:** Never swallow exceptions. Always catch them in Repository/UseCase and emit `ResultState.Error` with meaningful messages.
- **Hardcoding:** No hardcoded strings in Compose. Use `stringResource(R.string.x)`.
- **Secrets:** API endpoints or keys must not be hardcoded. Retrieve them from `BuildConfig`.

## 🎨 6. Design System & Theming
When generating or updating UI components, strictly adhere to the following design system:

- **Typography:** Use **Poppins** as the primary font family for all text elements.
- **Color Palette:**
    - Primary Green: `#2E7D32`
    - Light Green: `#CBFFC2`
    - Light Blue/Background: `#F3FAFF`
    - Pure White: `#FFFFFF`
- **Implementation Rule:** Ensure these colors and typography are defined in `ui/theme/Color.kt` and `ui/theme/Type.kt` respectively, and applied properly through the Material 3 `ColorScheme` in `Theme.kt`. Avoid hardcoding these hex values directly in Composable functions.

## 🗄️ 7. Database & Offline-First Strategy (Room + Supabase)

The application uses an **Offline-First** architecture. Room Database (SQLite) is the Single Source of Truth for the UI, while Supabase (PostgreSQL) acts as the remote backend.

### 7.1. Database Responsibilities
- **Local (Room):** Stores `ArchiveEntity`. Used directly by the Presentation layer via Flows. Must support fast local queries and offline access.
- **Remote (Supabase):** Stores the actual records. Handles complex relations, ENUMs, and automated `Audit Logs` via PostgreSQL Triggers.
- **NEVER** write manual audit log insertion code in the Android App (Kotlin). Audit logs are handled strictly by Supabase Database Triggers (`ON INSERT/UPDATE/DELETE`).

### 7.2. Synchronization Pipeline
When instructed to create or update data flows, strictly follow this pattern:
1. **Fetch/Read:** UI requests data -> `ArchiveRepository` queries Room (`ArchiveDao.getArchives(query)`) -> returns `Flow<ResultState<List<ArchiveDocument>>>`.
2. **Sync (Background):** `ArchiveRepository` fetches fresh data from Supabase -> Maps Supabase DTOs to Room Entities -> Updates Room (`ArchiveDao.insertAll()`). UI updates reactively.
3. **Write/Insert:**
    - User submits new Draft/Archive.
    - App saves to Room as `status = DRAFT`.
    - App attempts to push to Supabase.
    - If network fails, keep in Room as DRAFT for later sync. If success, update Room status to `UNVERIFIED` or `AVAILABLE` based on Supabase response.

### 7.3. Type Mapping & Conversion Rules
When writing Entity and DTO classes, respect these type conversions:
- **UUIDs:** Supabase uses `UUID`. Room and Kotlin Domain must use `String`.
- **JSONB Metadata:** Supabase uses `JSONB`. In Kotlin, use a Data Class (`ArchiveMetadata`) and convert it to a JSON `String` using Room `@TypeConverter`.
- **Enums:** Supabase `doc_type` and `doc_status` Enums should be mapped to Kotlin `enum class` in the Domain layer.

## 🔄 8. June 2026 Revision Rules (Must Follow)
When modifying the Archive logic or UI, enforce these constraints derived from the latest stakeholder requirements:

### 8.1. Input & Validation Logic
- **Bypass Add Screen:** Delete the `AddArchiveScreen` navigation route[cite: 3]. Use a Floating Action Button (FAB) on the `ArchiveListScreen` to navigate directly to the `ManualAddScreen`[cite: 3].
- **Auto-Validity:** Remove manual calendar inputs for the document validity period[cite: 3]. The system must auto-calculate the validity as `Document Year + 10 Years` in the ViewModel before saving[cite: 3].
- **Document Status & Duplicates:** Introduce a `DocumentCopyStatus` (Original/Copy) selection[cite: 3]. Allow duplicate Document Numbers to be saved ONLY IF their physical status differs[cite: 3]. If a user enters an existing number with a different status, show a warning dialog before proceeding[cite: 3].

### 8.2. Local Staging Area (Bulk Insert)
- **Staging Pipeline:** Do not save single documents directly to the main database[cite: 3]. Store inputted data temporarily in a Local Staging Area (Room DB or ViewModel state)[cite: 3].
- **Box Verification:** All documents added in one session belong to a single Box[cite: 3]. Route users to the `ArchiveReviewScreen` to review the staging list, and perform a **bulk insert** to the main database only when the user applies/verifies the list[cite: 3].

### 8.3. Archive List Presentation
- **Mandatory Year Filter:** Before displaying the archive list, force the user to select a Document Year (via dialog or transition screen)[cite: 3]. Fetch and cache data from Supabase strictly based on this selected year[cite: 3].
- **Table View:** Replace the standard `LazyColumn` Card design with a **Dynamic Table Layout** using `LazyColumn` and `Row` with `Modifier.weight()`[cite: 3].
- **Pagination:** Implement AndroidX `Paging3` (`androidx.paging:paging-compose`) to load the table rows lazily[cite: 3].
- **Visual Cues (Destruction Policy):** If `Current Year > (Document Year + 10)`, highlight the specific table row in red (or display a specific icon) to visually indicate that the document's retention period has expired and it must be destroyed[cite: 3].