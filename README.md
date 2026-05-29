# Termi Anora (AplikasiIosKu) - Android Native

Aplikasi terminal SSH dengan manajemen koneksi dan grup menggunakan Supabase sebagai backend database.

## 📋 Prasyarat Sistem
- **Android Studio** (Koala atau versi terbaru)
- **JDK 17** atau yang lebih baru
- **Kotlin 2.3.21**
- Akun **Supabase** (untuk Database & Auth)

## 🛠️ Langkah Awal (Setup)
1. **Clone Project**: Pastikan folder project sudah lengkap.
2. **Build Configuration**: Project ini menggunakan Gradle 8.13 dengan Kotlin 2.3.21.
3. **Konfigurasi Supabase**:
   - Buka file `app/src/main/java/com/example/myapplication1/network/SupabaseClient.kt`.
   - Pastikan `SUPABASE_URL` dan `SUPABASE_ANON_KEY` sudah terisi dengan benar.

## 🗄️ Konfigurasi Database (Supabase)
Jalankan script SQL yang ada di panduan utama untuk menyiapkan tabel `groups` dan `connections` serta kebijakan RLS.

## ⚡ Fitur Utama
- **Direct SSH Connection**: Menggunakan library JSch untuk koneksi SSH interaktif (Terminal).
- **Supabase Integration**: Auth (Email/Password), Postgrest (Database), dan Realtime.
- **Connection Groups**: Pengelompokan server berdasarkan kategori.
- **Modern UI**: Menggunakan Navigation Component untuk alur aplikasi.

## 🛠️ Pengembangan (Build)
Gunakan Android Studio untuk menjalankan aplikasi di Emulator atau Device fisik melalui Gradle task `:app:installDebug`.
