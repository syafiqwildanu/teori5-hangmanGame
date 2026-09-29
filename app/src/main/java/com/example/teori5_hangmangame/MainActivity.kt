package com.example.teori5_hangmangame

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var kataRahasia: String
    private val hurufBenar = mutableSetOf<Char>()
    private val hurufSalah = mutableSetOf<Char>()
    private var maksNyawa = 6
    private var skor = 0

    private lateinit var tvNyawa: TextView
    private lateinit var tvKata: TextView
    private lateinit var tvRiwayat: TextView
    private lateinit var tvSkor: TextView
    private lateinit var etTebak: EditText
    private lateinit var overlayHasil: LinearLayout
    private lateinit var overlayMulai: LinearLayout
    private lateinit var overlayKesulitan: LinearLayout
    private lateinit var tvHasil: TextView
    private lateinit var tvKataAkhir: TextView
    private lateinit var gridHuruf: GridLayout

    private val tombolHuruf = mutableMapOf<Char, Button>()

    private lateinit var bagianTubuh: List<ImageView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvNyawa = findViewById(R.id.tvNyawa)
        tvKata = findViewById(R.id.tvKata)
        tvRiwayat = findViewById(R.id.tvRiwayat)
        tvSkor = findViewById(R.id.tvSkor)
        etTebak = findViewById(R.id.etTebak)
        overlayHasil = findViewById(R.id.overlayHasil)
        overlayMulai = findViewById(R.id.overlayMulai)
        overlayKesulitan = findViewById(R.id.overlayKesulitan)
        tvHasil = findViewById(R.id.tvHasil)
        tvKataAkhir = findViewById(R.id.tvKataAkhir)
        gridHuruf = findViewById(R.id.gridHuruf)

        bagianTubuh = listOf(
            findViewById(R.id.imgKepala),
            findViewById(R.id.imgBadan),
            findViewById(R.id.imgTanganKiri),
            findViewById(R.id.imgTanganKanan),
            findViewById(R.id.imgKakiKiri),
            findViewById(R.id.imgKakiKanan)
        )

        buatTombolHuruf()

        findViewById<Button>(R.id.btnTebak).setOnClickListener { prosesTebakan() }
        findViewById<Button>(R.id.btnMainLagi).setOnClickListener { mulaiPermainan() }
        findViewById<Button>(R.id.btnMulai).setOnClickListener {
            overlayMulai.visibility = View.GONE
            overlayKesulitan.visibility = View.VISIBLE
        }
        findViewById<Button>(R.id.btnMudah).setOnClickListener { pilihKesulitan(6) }
        findViewById<Button>(R.id.btnSedang).setOnClickListener { pilihKesulitan(4) }
        findViewById<Button>(R.id.btnSulit).setOnClickListener { pilihKesulitan(3) }

        if (savedInstanceState != null) {
            kataRahasia = savedInstanceState.getString("kataRahasia", "")
            if (kataRahasia.isEmpty()) {
                kataRahasia = resources.getStringArray(R.array.daftar_kata).random()
            }
            hurufBenar.clear()
            hurufBenar.addAll(savedInstanceState.getString("hurufBenar", "")?.toList() ?: emptyList())
            hurufSalah.clear()
            hurufSalah.addAll(savedInstanceState.getString("hurufSalah", "")?.toList() ?: emptyList())
            skor = savedInstanceState.getInt("skor", 0)
            maksNyawa = savedInstanceState.getInt("maksNyawa", 6)
            overlayHasil.visibility =
                if (savedInstanceState.getBoolean("hasilTampil", false)) View.VISIBLE else View.GONE
            overlayMulai.visibility =
                if (savedInstanceState.getBoolean("mulaiTampil", false)) View.VISIBLE else View.GONE
            overlayKesulitan.visibility =
                if (savedInstanceState.getBoolean("sulitTampil", false)) View.VISIBLE else View.GONE
            etTebak.isEnabled = savedInstanceState.getBoolean("inputAktif", true)
            tvHasil.text = savedInstanceState.getString("teksHasil", "")
            tvKataAkhir.text = savedInstanceState.getString("teksKataAkhir", "")
            gambarPapan()
        } else {
            mulaiPermainan()
            overlayMulai.visibility = View.VISIBLE
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("kataRahasia", kataRahasia)
        outState.putString("hurufBenar", hurufBenar.joinToString(""))
        outState.putString("hurufSalah", hurufSalah.joinToString(""))
        outState.putInt("skor", skor)
        outState.putInt("maksNyawa", maksNyawa)
        outState.putBoolean("hasilTampil", overlayHasil.visibility == View.VISIBLE)
        outState.putBoolean("mulaiTampil", overlayMulai.visibility == View.VISIBLE)
        outState.putBoolean("sulitTampil", overlayKesulitan.visibility == View.VISIBLE)
        outState.putBoolean("inputAktif", etTebak.isEnabled)
        outState.putString("teksHasil", tvHasil.text.toString())
        outState.putString("teksKataAkhir", tvKataAkhir.text.toString())
    }

    private fun pilihKesulitan(nyawa: Int) {
        maksNyawa = nyawa
        overlayKesulitan.visibility = View.GONE
        mulaiPermainan()
    }

    private fun buatTombolHuruf() {
        gridHuruf.removeAllViews()
        tombolHuruf.clear()
        for (huruf in 'A'..'Z') {
            val tombol = Button(this).apply {
                text = huruf.toString()
                textSize = 12f
                isAllCaps = true
                setOnClickListener { tebakHuruf(huruf) }
            }
            val params = GridLayout.LayoutParams().apply {
                width = 0
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            }
            tombol.layoutParams = params
            tombolHuruf[huruf] = tombol
            gridHuruf.addView(tombol)
        }
    }

    private fun perbaruiTombolHuruf() {
        for ((huruf, tombol) in tombolHuruf) {
            tombol.isEnabled = !(hurufBenar.contains(huruf) || hurufSalah.contains(huruf))
        }
    }

    private fun perbaruiGantungan(kesalahan: Int) {
        bagianTubuh.forEachIndexed { urutan, gambar ->
            if (urutan < kesalahan) {
                if (gambar.visibility != View.VISIBLE) {
                    gambar.alpha = 0f
                    gambar.visibility = View.VISIBLE
                    gambar.animate().alpha(1f).setDuration(350).start()
                }
            } else {
                gambar.animate().cancel()
                gambar.visibility = View.INVISIBLE
                gambar.alpha = 1f
            }
        }
    }

    private fun mulaiPermainan() {
        val daftarKata = resources.getStringArray(R.array.daftar_kata)
        kataRahasia = daftarKata.random()

        hurufBenar.clear()
        hurufSalah.clear()

        overlayHasil.visibility = View.GONE
        etTebak.text.clear()
        etTebak.isEnabled = true

        gambarPapan()
    }

    private fun gambarPapan() {
        perbaruiGantungan(hurufSalah.size)

        tvKata.text = kataRahasia
            .map { if (hurufBenar.contains(it)) it else '_' }
            .joinToString(" ")

        val sisaNyawa = maksNyawa - hurufSalah.size
        tvNyawa.text = "Nyawa: $sisaNyawa"
        tvSkor.text = "Skor: $skor"

        tvRiwayat.text = if (hurufSalah.isEmpty()) {
            "Salah: -"
        } else {
            "Salah: ${hurufSalah.joinToString(" ")}"
        }

        perbaruiTombolHuruf()
    }

    private fun prosesTebakan() {
        val input = etTebak.text.toString().trim().uppercase()

        if (input.length != 1 || !input[0].isLetter()) {
            Toast.makeText(this, "Masukkan satu huruf", Toast.LENGTH_SHORT).show()
            etTebak.text.clear()
            return
        }

        tebakHuruf(input[0])
        etTebak.text.clear()
    }

    private fun tebakHuruf(huruf: Char) {
        if (hurufBenar.contains(huruf) || hurufSalah.contains(huruf)) {
            Toast.makeText(this, "Huruf $huruf sudah ditebak", Toast.LENGTH_SHORT).show()
            return
        }

        if (kataRahasia.contains(huruf)) {
            hurufBenar.add(huruf)
        } else {
            hurufSalah.add(huruf)
        }

        gambarPapan()

        periksaAkhirPermainan()
    }

    private fun periksaAkhirPermainan() {
        val semuaTerbuka = kataRahasia.all { hurufBenar.contains(it) }

        when {
            semuaTerbuka -> tampilkanHasil(menang = true)
            hurufSalah.size >= maksNyawa -> tampilkanHasil(menang = false)
        }
    }

    private fun tampilkanHasil(menang: Boolean) {
        if (menang) {
            skor += 10
        } else {
            skor += 0
        }
        tvSkor.text = "Skor: $skor"
        tvHasil.text = if (menang) getString(R.string.menang) else getString(R.string.kalah)
        tvHasil.setTextColor(
            if (menang) 0xFF3DDC84.toInt() else 0xFFF87171.toInt()
        )
        tvKataAkhir.text = "Kata: $kataRahasia"

        etTebak.isEnabled = false
        perbaruiTombolHuruf()

        overlayHasil.alpha = 0f
        overlayHasil.visibility = View.VISIBLE
        overlayHasil.animate()
            .alpha(1f)
            .setDuration(350)
            .start()

        overlayHasil.requestFocus()
    }
}
