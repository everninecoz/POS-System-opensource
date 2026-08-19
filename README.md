# POS Open Source untuk UMKM 

Aplikasi kasir berbasis Android gratis (disarankan pakai tablet) dan open source untuk UMKM Indonesia. Berjalan 100% offline, tidak perlu koneksi internet.
untuk UMKM yang butuh POS Opensource gratis, bisa lanjut poles sendiri, edit app nya juga, pokoknya ambil aja.

app ini masih V1.0 cuma iseng di buat ya, dan bisa di pake kok.

---

## Kenapa Pilih POS Ini?

- **Gratis selamanya** - Tidak ada biaya langganan, tidak ada fitur tersembunyi
- **Offline-first** - Semua data tersimpan di HP, bisa jualan tanpa internet
- **Bahasa Indonesia** - Interface dalam Bahasa Indonesia, mudah dipahami
- **Khusus UMKM** - Didesain untuk warung, kedai, toko kecil, dan usaha sejenisnya

## Fitur Utama

- Transaksi penjualan dengan berbagai metode bayar (Tunai, QRIS, Transfer)
- Manajemen produk dan kategori
- Barcode scanner untuk scan produk
- Cetak struk via printer thermal (Bluetooth)
- Laporan penjualan harian, mingguan, bulanan
- Laporan laba rugi lengkap
- Database pelanggan dengan poin loyalitas
- Diskon dan promo (persen atau nominal)
- Manajemen shift kasir (buka/tutup shift)
- Stock opname untuk cek stok fisik vs sistem
- Export data ke CSV
- Backup dan restore database
- User management (admin & kasir)
- Mode gelap dan terang
- Bilingual (Indonesia & English)


## Teknologi

| Komponen | Teknologi |
|----------|-----------|
| Bahasa | Kotlin |
| UI | Jetpack Compose + Material3 |
| Database | Room (SQLite) |
| DI | Hilt |
| Scanner | ML Kit Barcode |
| Camera | CameraX |
| Arsitektur | MVVM + Clean Architecture |

## Kebutuhan Spesifikasi

- Android 8.0 (API 26) atau lebih baru
- Untuk printer thermal: printer Bluetooth yang kompatibel ESC/POS
- Untuk barcode scanner: kamera HP

## Instalasi

### Cara 1: Build dari Source

1. Clone repository ini

```bash
git clone https://github.com/username/pos-opensrc.git
```

2. Buka project di Android Studio

3. Tunggu Gradle Sync selesai

4. Build APK:
   - Menu `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`

5. Install APK ke HP Android

### Cara 2: Download APK

untuk apk ada di git ini, perbolehkan dulu setting unknown source install biar bisa di install manual.

## Login Default

| Field | Nilai |
|-------|-------|
| Username | `admin` |
| PIN | `123456` |
| Password | `admin123` |

> **Penting:** Segera ganti password setelah login pertama kali!

## Cara Pakai

1. Install aplikasi seperti biasa
2. Buka aplikasi, login dengan akun admin
3. **Pengaturan Toko** - Masukkan nama toko, alamat, nomor telepon
4. **Tambah Produk** - Input produk yang dijual
5. **Mulai Jualan** - Buka shift, lakukan transaksi
6. **Lihat Laporan** - Cek penjualan dan laba rugi

## Struktur Folder

```
app/src/main/java/com/posopensrc/
├── core/           # Utilities, security, session, navigation
├── data/           # Database, DAO, repository
├── domain/         # Model dan business logic
├── printer/        # Manajemen printer thermal
└── ui/             # Screen, ViewModel, komponen Compose
```

## Kontribusi

Kontribusi sangat dipersilakan! Cara berkontribusi:

1. Fork repository ini
2. Buat branch baru (`git checkout -b fitur/ nama-fitur`)
3. Commit perubahan (`git commit -m 'Tambah fitur xyz'`)
4. Push ke branch (`git push origin fitur/ nama-fitur`)
5. Buka Pull Request

## Roadmap

- [ ] Integrasi pembayaran QRIS (GoPay, OVO, Dana)
- [ ] Multi-cabang
- [ ] PPOB (bayar listrik, pulsa, dll)
- [ ] Integrasi GrabFood/GoFood
- [ ] Notifikasi stok menipis via notifikasi HP

## Known Issues

- Printer thermal hanya mendukung ESC/POS
- Barcode scanner belum mendukung semua format barcode

## Lisensi

Proyek ini menggunakan lisensi MIT. Lihat file [LICENSE](LICENSE) untuk informasi lebih lanjut.

## Kontak

- GitHub: [@everninecoz]((https://github.com/everninecoz))
- Email: everninecoz@gmail.com

---

Dibuat untuk UMKM Indonesia.
