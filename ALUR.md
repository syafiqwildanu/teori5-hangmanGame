# Alur Hangman — Penjelasan Langkah per Langkah

Dokumen ini menjelaskan alur program dari awal dibuka sampai main lagi.
Acuan: `MainActivity.kt` + `activity_main.xml`.

## 1. Peta layar (4 lapis dalam 1 `FrameLayout`)

| Lapis | ID | Isi | Tampil kapan |
|---|---|---|---|
| 1 | (papan utama) | Baris judul (`judul_permainan` + `tvSkor` + `tvNyawa`), `frameGantungan` (1 tiang + 6 tubuh), `tvKata`, `tvRiwayat`, `etTebak` + `btnTebak`, `gridHuruf` A–Z | Selalu (dasar) |
| 2 | `overlayHasil` | `tvHasil`, `tvKataAkhir`, `btnMainLagi` | Hanya saat menang/kalah |
| 3 | `overlayMulai` | Judul + aturan + `tvPetunjukMulai` ("...nama kota") + `btnMulai` | Hanya saat pertama buka |
| 4 | `overlayKesulitan` | `btnMudah` (6 nyawa), `btnSedang` (4), `btnSulit` (3) | Setelah klik Mulai, sebelum ronde jalan |

## 2. Alur hidup Activity

```
Buka app
  → onCreate()
    → findViewById semua view + bagianTubuh (6 ImageView)
    → buatTombolHuruf()          // 26 tombol A-Z masuk gridHuruf
    → pasang listener: btnTebak, btnMainLagi, btnMulai, btnMudah/Sedang/Sulit
    → if ada savedInstanceState? restore : mulaiPermainan() + tampilkan overlayMulai
Putar layar
  → onSaveInstanceState() simpan state → Activity dibuat ulang → onCreate() restore
```

### Yang disimpan saat rotasi (`onSaveInstanceState`)

`kataRahasia`, `hurufBenar` (sebagai String), `hurufSalah`, `skor`, `maksNyawa`,
`hasilTampil`, `mulaiTampil`, `sulitTampil`, `inputAktif`, `teksHasil`, `teksKataAkhir`.

Saat restore: semua dikembalikan, lalu `gambarPapan()` dipanggil agar tulisan,
gambar tubuh, dan tombol A–Z kembali persis seperti sebelum diputar.

## 3. Alur satu ronde (urutan fungsi)

### 3.1 Mulai

1. `btnMulai` diklik → `overlayMulai` disembunyikan, `overlayKesulitan` ditampilkan.
2. Pilih `btnMudah / btnSedang / btnSulit` → `pilihKesulitan(nyawa)`:
   `maksNyawa = 6 / 4 / 3`, sembunyikan overlay, panggil `mulaiPermainan()`.
3. `mulaiPermainan()`:
   - `kataRahasia = daftarKata.random()` (12 kota dari `strings.xml`)
   - `hurufBenar.clear()`, `hurufSalah.clear()`
   - sembunyikan `overlayHasil`, kosongkan + aktifkan `etTebak`
   - panggil `gambarPapan()`

### 3.2 Menebak (dua jalan, satu muara)

- Via ketik: `btnTebak` → `prosesTebakan()` → validasi (harus 1 huruf) → `tebakHuruf(huruf)` → kosongkan input.
- Via tombol: klik tombol `K` di grid → langsung `tebakHuruf('K')`.

`tebakHuruf(huruf)`:

1. Jika huruf sudah ada di benar/salah → Toast "sudah ditebak", berhenti.
2. Jika `kataRahasia.contains(huruf)` → masuk `hurufBenar`, else masuk `hurufSalah`.
3. Panggil `gambarPapan()`, lalu `periksaAkhirPermainan()`.

### 3.3 Menggambar papan (`gambarPapan()` — dipanggil tiap ada perubahan)

1. `perbaruiGantungan(hurufSalah.size)` — tiap bagian tubuh `urutan < kesalahan` ditampilkan; yang **baru muncul** dianimasikan `alpha 0 → gambar.animate().alpha(1f)` 350ms; sisanya disembunyikan.
2. `tvKata` — tiap huruf rahasia: tampilkan aslinya jika sudah ditebak, else `_`. Digabung dengan spasi, mis. `J _ K _ R T _`.
3. `tvNyawa` = `"Nyawa: ${maksNyawa - salah}"`, `tvSkor` = `"Skor: $skor"`.
4. `tvRiwayat` — `"Salah: -"` atau `"Salah: X Q Z"`.
5. `perbaruiTombolHuruf()` — tiap tombol A–Z: `isEnabled = !(sudahBenar || sudahSalah)`.

### 3.4 Akhir ronde

`periksaAkhirPermainan()`:

- `semuaTerbuka = kataRahasia.all { hurufBenar.contains(it) }` → `tampilkanHasil(menang = true)`
- `hurufSalah.size >= maksNyawa` → `tampilkanHasil(menang = false)`
- Selain itu ronde lanjut (tidak terjadi apa-apa).

`tampilkanHasil(menang)`:

1. Menang → `skor += 10`; kalah → `skor += 0`. Update `tvSkor`.
2. `tvHasil` = "Kamu Menang!" (hijau) / "Kamu Kalah" (merah); `tvKataAkhir` = "Kata: ...".
3. `etTebak.isEnabled = false` + kunci semua tombol yang relevan via `perbaruiTombolHuruf()`.
4. `overlayHasil` fade-in (`alpha 0 → 1`, 350ms) lalu `requestFocus()`.
5. `btnMainLagi` → `mulaiPermainan()` (skor **tidak** direset, kata diacak ulang).

## 4. Contoh jejak main (kata JAKARTA, Mudah/6 nyawa)

1. Pilih Mudah → kata = JAKARTA, papan `_ _ _ _ _ _ _`, Nyawa 6, Skor 0.
2. Tebak `A` (benar) → `_ A _ A _ _ A`, tombol A mati.
3. Tebak `X` (salah) → kepala muncul (animasi fade), Nyawa 5, riwayat `X`, tombol X mati.
4. Tebak `J, K, R, T` → `J A K A R T A` → menang → Skor 10 → overlay "Kamu Menang! Kata: JAKARTA".
5. Main Lagi → kata baru diacak, Skor tetap 10.

## 5. Kalau mau ubah-ubah

- Tambah/kurangi kota: edit `strings.xml` → `daftar_kata` (ingat: satu kata, A–Z).
- Ubah nyawa: edit angka di `pilihKesulitan()` (`btnMudah/Sedang/Sulit` listener di `onCreate`).
- Ubah poin: edit `skor += 10` di `tampilkanHasil()`.
- Ubah petunjuk: edit `tvPetunjukMulai` di `activity_main.xml`.
