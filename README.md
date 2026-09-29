# Hangman — Teori 5

Game tebak kata (Hangman) Android native (Kotlin + View System XML).
Pemain menebak nama kota satu huruf demi satu huruf sebelum nyawa habis.

Repo: https://github.com/syafiqwildanu/teori5-hangmanGame.git — branch `main`.

## Daftar kata (12 kota, satu kata, A–Z saja)

`MADRID, BARCELONA, TURIN, MANCHESTER, PARIS, LONDON, BERLIN, ROMA, TOKYO, JAKARTA, SURABAYA, MEDAN`

Disimpan di `app/src/main/res/values/strings.xml` (`<string-array name="daftar_kata">`).
Syarat kata: UPPERCASE, tanpa spasi/strip — kota berspasi (mis. NEW YORK) tidak didukung karena spasi tidak bisa ditebak.

## Fitur

1. **Tombol A–Z** — `GridLayout` (`@+id/gridHuruf`, `columnCount=7`) di lapisan pertama, di bawah input. Dibuat di `buatTombolHuruf()` (`'A'..'Z'`), klik memanggil `tebakHuruf()`. Huruf yang sudah ditebak `isEnabled = false` via `perbaruiTombolHuruf()`.
2. **Animasi gantungan** — `perbaruiGantungan()` : gambar yang baru muncul dipasang `alpha = 0f` lalu `gambar.animate().alpha(1f).setDuration(350).start()`.
3. **Petunjuk** — overlay mulai (`tvPetunjukMulai`): "Petunjuk: Kata ini berkaitan dengan nama kota".
4. **Skor** — `tvSkor` di baris judul. Menang `+10`, kalah `+0`, akumulasi antar ronde.
5. **Kesulitan** — overlay ke-4 `overlayKesulitan` (di dalam root `FrameLayout`): Mudah = 6 nyawa, Sedang = 4, Sulit = 3.
6. **Tahan rotasi** — `onSaveInstanceState()` menyimpan `kataRahasia, hurufBenar, hurufSalah, skor, maksNyawa` + status overlay; `onCreate()` me-restore lalu `gambarPapan()`.

## Alur main (ringkas)

```
overlayMulai (Mulai Bermain) → overlayKesulitan (Mudah/Sedang/Sulit)
  → mulaiPermainan() → tebak via EditText / tombol A-Z → tebakHuruf()
  → gambarPapan() → periksaAkhirPermainan() → tampilkanHasil() → Main Lagi
```

Detail langkah per fungsi ada di [ALUR.md](ALUR.md).

## Struktur penting

```
app/src/main/
  java/com/example/teori5_hangmangame/MainActivity.kt   ← seluruh logika
  res/layout/activity_main.xml                          ← FrameLayout 4 lapis
  res/values/strings.xml                                ← daftar_kata + teks UI
  res/drawable/gambar_*.xml                             ← tiang + 6 bagian tubuh
  AndroidManifest.xml                                   ← MainActivity = LAUNCHER
```

## Cara jalan & build

1. Buka folder ini di Android Studio.
2. Run di emulator / HP (minSdk 24).
3. Atau via terminal: `./gradlew assembleDebug` → APK di `app/build/outputs/apk/debug/`.

## File logika utama

- `MainActivity.kt` — state (`kataRahasia, hurufBenar, hurufSalah, maksNyawa, skor`), UI (`buatTombolHuruf, perbaruiTombolHuruf, perbaruiGantungan, gambarPapan, prosesTebakan, tebakHuruf, periksaAkhirPermainan, tampilkanHasil, pilihKesulitan`), lifecycle (`onCreate, onSaveInstanceState`).
- `activity_main.xml` — Lapis 1 papan (judul + tvSkor + tvNyawa + gambar + tvKata + tvRiwayat + input + gridHuruf), Lapis 2 `overlayHasil`, Lapis 3 `overlayMulai`, Lapis 4 `overlayKesulitan`.
