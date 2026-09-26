package com.example.teori5_hangmangame

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.    appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var kataRahasia: String
    private val hurufBenar = mutableSetOf<Char>()
    private val hurufSalah = mutableSetOf<Char>()
    private val maksNyawa = 6

    private lateinit var tvNyawa: TextView
    private lateinit var tvKata: TextView
    private lateinit var tvRiwayat: TextView
    private lateinit var etTebak: EditText
    private lateinit var overlayHasil: LinearLayout
    private lateinit var overlayMulai: LinearLayout
    private lateinit var tvHasil: TextView
    private lateinit var tvKataAkhir: TextView

    private lateinit var bagianTubuh: List<ImageView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvNyawa = findViewById(R.id.tvNyawa)
        tvKata = findViewById(R.id.tvKata)
        tvRiwayat = findViewById(R.id.tvRiwayat)
        etTebak = findViewById(R.id.etTebak)
        overlayHasil = findViewById(R.id.overlayHasil)
        overlayMulai = findViewById(R.id.overlayMulai)
        tvHasil = findViewById(R.id.tvHasil)
        tvKataAkhir = findViewById(R.id.tvKataAkhir)

        bagianTubuh = listOf(
            findViewById(R.id.imgKepala),
            findViewById(R.id.imgBadan),
            findViewById(R.id.imgTanganKiri),
            findViewById(R.id.imgTanganKanan),
            findViewById(R.id.imgKakiKiri),
            findViewById(R.id.imgKakiKanan)
        )

        findViewById<Button>(R.id.btnTebak).setOnClickListener { prosesTebakan() }
        findViewById<Button>(R.id.btnMainLagi).setOnClickListener { mulaiPermainan() }
        findViewById<Button>(R.id.btnMulai).setOnClickListener {
            overlayMulai.visibility = View.GONE
            mulaiPermainan()
        }

        mulaiPermainan()
        overlayMulai.visibility = View.VISIBLE
    }

    private fun perbaruiGantungan(kesalahan: Int) {
        bagianTubuh.forEachIndexed { urutan, gambar ->
            gambar.visibility = if (urutan < kesalahan) View.VISIBLE else View.INVISIBLE
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

        tvRiwayat.text = if (hurufSalah.isEmpty()) {
            "Salah: -"
        } else {
            "Salah: ${hurufSalah.joinToString(" ")}"
        }
    }

    private fun prosesTebakan() {
        val input = etTebak.text.toString().trim().uppercase()

        if (input.length != 1 || !input[0].isLetter()) {
            Toast.makeText(this, "Masukkan satu huruf", Toast.LENGTH_SHORT).show()
            etTebak.text.clear()
            return
        }

        val huruf = input[0]

        if (hurufBenar.contains(huruf) || hurufSalah.contains(huruf)) {
            Toast.makeText(this, "Huruf $huruf sudah ditebak", Toast.LENGTH_SHORT).show()
            etTebak.text.clear()
            return
        }

        if (kataRahasia.contains(huruf)) {
            hurufBenar.add(huruf)
        } else {
            hurufSalah.add(huruf)
        }

        etTebak.text.clear()
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
        tvHasil.text = if (menang) getString(R.string.menang) else getString(R.string.kalah)
        tvHasil.setTextColor(
            if (menang) 0xFF3DDC84.toInt() else 0xFFF87171.toInt()
        )
        tvKataAkhir.text = "Kata: $kataRahasia"

        etTebak.isEnabled = false

        overlayHasil.alpha = 0f
        overlayHasil.visibility = View.VISIBLE
        overlayHasil.animate()
            .alpha(1f)
            .setDuration(350)
            .start()

        overlayHasil.requestFocus()
    }
}