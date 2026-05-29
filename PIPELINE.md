# Pipeline Pengembangan Termi Anora

## ✅ Progress Saat Ini (Sudah Selesai)
- [x] **Setup Project Native**: Konfigurasi Gradle dengan Kotlin 2.3.21.
- [x] **Manajemen Dependensi**: Migrasi ke **SSHJ**, Integrasi **BouncyCastle**, Supabase, Ktor.
- [x] **Data Models**: Implementasi `ConnectionGroup` dan `SshConnection` (Sinkron dengan Supabase).
- [x] **Build Success**: Memperbaiki semua error kompilasi dan metadata Kotlin.
- [x] **Supabase Repository**: CRUD koneksi, grup, dan Auth Logic.
- [x] **SSH Engine**: Implementasi modern menggunakan **SSHJ** untuk fleksibilitas algoritma.
- [x] **CRUD Koneksi**: Fitur tambah koneksi baru via FloatingActionButton di Dashboard.
- [x] **Terminal UI Fix**: Pembersihan kode ANSI dan auto-scroll.

## 🚀 Fitur yang Sedang Berjalan (In Progress)
- [ ] **Group Management UI**: Membuat tab/kategori khusus untuk pengelompokan server di Dashboard.
- [ ] **Import/Export**: Fitur untuk mengimpor daftar koneksi dari file JSON/CSV.

## 🔜 Rencana Selanjutnya (Next Tasks)
- [ ] **Encryption**: Enkripsi password server di lokal sebelum disimpan.
- [ ] **SSH Optimization**: Penanganan resize terminal secara dinamis.

## ⚠️ Hal yang Perlu Diperhatikan
- **Cisco Connectivity**: Jika FA01-FA03 masih gagal, pastikan routing subnet `10.10.11.x` dapat dijangkau oleh emulator.
