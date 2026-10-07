package com.example.ra5generarinformes

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONArray
import kotlin.math.min


class MainActivity : AppCompatActivity() {
    private lateinit var spinnerCiudad: Spinner
    private lateinit var spinnerModalidad: Spinner
    private lateinit var botonGenerarInforme: Button
    private lateinit var botonExportarPdf: Button
    private lateinit var contenedorInforme: LinearLayout
    private lateinit var textoCiudadSeleccionada: TextView
    private lateinit var textoNumeroOfertas: TextView
    private lateinit var textoSalarioTotal: TextView
    private lateinit var textoSalarioMedio: TextView
    private lateinit var textoSalarioMaximo: TextView
    private lateinit var barraValencia: ProgressBar
    private lateinit var barraMadrid: ProgressBar
    private lateinit var barraAlicante: ProgressBar
    private lateinit var barraCastellon: ProgressBar
    private lateinit var textoCantidadValencia: TextView
    private lateinit var textoCantidadMadrid: TextView
    private lateinit var textoCantidadAlicante: TextView
    private lateinit var textoCantidadCastellon: TextView
    private lateinit var contenedorOfertas: LinearLayout

    private lateinit var textoSalario: TextView

    private lateinit var textoSalarioMinimo: TextView

    private lateinit var seekBarSalario: SeekBar

    private lateinit var textoMejorOferta: TextView

    private val ofertas = ArrayList<Oferta>()

    // Fase 12.1
    private val codigoCrearPdf = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Variable con su View
        spinnerCiudad = findViewById(R.id.spinnerCiudad)
        spinnerModalidad = findViewById(R.id.spinnerModalidad)
        botonGenerarInforme = findViewById(R.id.botonGenerarInforme)
        botonExportarPdf = findViewById(R.id.botonExportarPdf)
        contenedorInforme = findViewById(R.id.contenedorInforme)
        textoCiudadSeleccionada = findViewById(R.id.textoCiudadSeleccionada)
        textoNumeroOfertas = findViewById(R.id.textoNumeroOfertas)
        textoSalarioTotal = findViewById(R.id.textoSalarioTotal)
        textoSalarioMedio = findViewById(R.id.textoSalarioMedio)
        textoSalarioMaximo = findViewById(R.id.textoSalarioMaximo)
        barraValencia = findViewById(R.id.barraValencia)
        barraMadrid = findViewById(R.id.barraMadrid)
        barraAlicante = findViewById(R.id.barraAlicante)
        barraCastellon = findViewById(R.id.barraCastellon)
        textoCantidadValencia = findViewById(R.id.textoCantidadValencia)
        textoCantidadMadrid = findViewById(R.id.textoCantidadMadrid)
        textoCantidadAlicante = findViewById(R.id.textoCantidadAlicante)
        textoCantidadCastellon = findViewById(R.id.textoCantidadCastellon)
        contenedorOfertas = findViewById(R.id.contenedorOfertas)
        textoSalario = findViewById(R.id.textoSalario)
        seekBarSalario = findViewById(R.id.seekBarSalario)
        textoSalarioMinimo = findViewById(R.id.textoSalarioMinimo)
        textoMejorOferta = findViewById(R.id.textoMejorOferta)

        // Fase 6 y 7
        configurarSpinner()
        configurarSpinnerModalidad()
        configurarSeekBarSalario()
        ofertas.addAll(cargarOfertas())

        // Fase 8.2
        botonGenerarInforme.setOnClickListener {
            val ciudadSeleccionada = spinnerCiudad.selectedItem.toString()
            val modalidadSeleccionada = spinnerModalidad.selectedItem.toString()

            val ofertasFiltradas = filtrarOfertasPorCiudad(ciudadSeleccionada)
            val ofertasPorModalidad =
                filtrarOfertasPorModalidad(ofertasFiltradas, modalidadSeleccionada)
            val ofertasFinales =
                filtrarOfertaPorSalario(ofertasPorModalidad, obtenerSalarioMinimo())
            mostrarResultado(ciudadSeleccionada, ofertasFinales)
        }

        // Fase 12.2
        botonExportarPdf.setOnClickListener {
            val ciudadSeleccionada = spinnerCiudad.selectedItem.toString()
            val modalidadSeleccionada = spinnerModalidad.selectedItem.toString()

            val ofertasFiltradas = filtrarOfertasPorCiudad(ciudadSeleccionada)
            val ofertasPorModalidad =
                filtrarOfertasPorModalidad(ofertasFiltradas, modalidadSeleccionada)
            val ofertasFinales =
                filtrarOfertaPorSalario(ofertasPorModalidad, obtenerSalarioMinimo())
            mostrarResultado(ciudadSeleccionada, ofertasFinales)

            contenedorInforme.post {
                abrirSelectorParaGuardarPdf(ciudadSeleccionada)
            }
        }
    }

    // Fase 13
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == codigoCrearPdf && resultCode == RESULT_OK) {
            val uriPdf = data?.data
            if (uriPdf != null) {
                exportarInformePdf(uriPdf)
            }
        }
    }

    // Fase 6
    private fun configurarSpinner() {
        val ciudades = arrayOf("Todas", "Valencia", "Madrid", "Alicante", "Castellón")
        val adaptador = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            ciudades
        )
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCiudad.adapter = adaptador
    }

    private fun configurarSpinnerModalidad() {
        val modalidades = arrayOf("Todas", "Presencial", "Híbrido", "Remoto")
        val adaptador = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            modalidades
        )
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerModalidad.adapter = adaptador
    }

    // Fase 7
    private fun cargarOfertas(): ArrayList<Oferta> {
        val listaOfertas = ArrayList<Oferta>()

        val textoJson = assets.open("ofertas.json").bufferedReader().use { it.readText() }
        val arrayJson = JSONArray(textoJson)

        for (i in 0 until arrayJson.length()) {
            val objetoJson = arrayJson.getJSONObject(i)

            val oferta = Oferta(
                objetoJson.getInt("id"),
                objetoJson.getString("puesto"),
                objetoJson.getString("empresa"),
                objetoJson.getString("ciudad"),
                objetoJson.getInt("salario"),
                objetoJson.getString("modalidad")
            )
            listaOfertas.add(oferta)
        }
        return listaOfertas
    }

    // Fase 8
    private fun filtrarOfertasPorCiudad(ciudad: String): List<Oferta> {
        val resultado = if (ciudad == "Todas") {
            ofertas
        } else {
            ofertas.filter {
                it.ciudad == ciudad
            }
        }
        return resultado
    }

    private fun filtrarOfertasPorModalidad(
        lista: List<Oferta>,
        modalidad: String
    ): List<Oferta> {
        val resultado = if (modalidad == "Todas") {
            lista
        } else {
            lista.filter {
                it.modalidad == modalidad
            }
        }
        return resultado
    }

    private fun filtrarOfertaPorSalario(lista: List<Oferta>, salarioMinimo: Int): List<Oferta> {
        return lista.filter { it.salario >= salarioMinimo }
    }

    // Fases 9 y 10
    private fun mostrarResultado(ciudad: String, ofertasFiltradas: List<Oferta>) {
        // Fase 9: cálculos
        val numeroOfertas = ofertasFiltradas.size
        val salarioTotal = ofertasFiltradas.sumOf { it.salario }
        val salarioMedio = if (ofertasFiltradas.size > 0) {
            ofertasFiltradas.map { it.salario }.average()
        } else {
            0.0
        }
        val salarioMaximo = ofertasFiltradas.maxOfOrNull { it.salario } ?: 0
        val salarioMinimo = ofertasFiltradas.minOfOrNull { it.salario } ?: 0
        val mejorOferta = ofertasFiltradas.maxByOrNull { it.salario }


        // Fase 10: resumen
        textoCiudadSeleccionada.text = "Ciudad seleccionada: $ciudad"
        textoNumeroOfertas.text = "Numero de ofertas: $numeroOfertas"
        textoSalarioTotal.text = "Salario total: ${formatearSalario(salarioTotal)}"
        textoSalarioMedio.text = "Salario medio: ${formatearSalario(salarioMedio.toInt())}"
        textoSalarioMaximo.text = "Salario maximo: ${formatearSalario(salarioMaximo)}"
        textoSalarioMinimo.text = "Salario mínimo: ${formatearSalario(salarioMinimo)}"

        // Fase 11: grafico
        mostrarGrafico(ofertasFiltradas)

        // Fase 10: listado
        contenedorOfertas.removeAllViews()

        ofertasFiltradas.forEachIndexed { indice, oferta ->
            val textoOferta = TextView(this)
            textoOferta.text = "${indice + 1}. ${oferta.puesto}\n" +
                    "${oferta.empresa} · ${oferta.ciudad} · ${oferta.modalidad} · ${
                        formatearSalario(
                            oferta.salario
                        )
                    }"
            textoOferta.textSize = 16f
            textoOferta.setPadding(0, 0, 0, 24)
            contenedorOfertas.addView(textoOferta)
        }

        if (mejorOferta != null) {
            val textoMejorOferta = TextView(this)
            val textoMejorOfertaPagada = TextView(this)
            textoMejorOferta.text =
                "${mejorOferta.empresa} · ${mejorOferta.ciudad} · ${mejorOferta.modalidad}· ${
                    formatearSalario(
                        mejorOferta.salario
                    )
                }"

            textoMejorOfertaPagada.textSize = 18f
            textoMejorOferta.textSize = 16f
            textoMejorOferta.setTextColor(Color.parseColor("#333333"))
            textoMejorOfertaPagada.setTextColor(Color.parseColor("#333333"))
            textoMejorOfertaPagada.setPadding(0, 0, 0, 24)
            textoMejorOferta.setPadding(0, 0, 0, 24)
            textoMejorOfertaPagada.setTypeface(null, Typeface.BOLD)
            textoMejorOfertaPagada.text = "La mejor oferta pagada del filtro es:"

            contenedorOfertas.addView(textoMejorOfertaPagada)
            contenedorOfertas.addView(textoMejorOferta)
        } else {
            val textoMejorOferta = TextView(this)
            textoMejorOferta.textSize = 16f
            textoMejorOferta.setPadding(0, 0, 0, 24)
            textoMejorOferta.text = "No hay ninguna oferta"
            contenedorOfertas.addView(textoMejorOferta)
        }
    }

    // Fase 11
    private fun mostrarGrafico(ofertasFiltradas: List<Oferta>) {
        val ofertasValencia = ofertasFiltradas.count { it.ciudad == "Valencia" }
        val ofertasMadrid = ofertasFiltradas.count { it.ciudad == "Madrid" }
        val ofertasAlicante = ofertasFiltradas.count { it.ciudad == "Alicante" }
        val ofertasCastellon = ofertasFiltradas.count { it.ciudad == "Castellón" }

        val valorMaximo =
            maxOf(ofertasValencia, ofertasMadrid, ofertasAlicante, ofertasCastellon, 1)
        barraValencia.max = valorMaximo
        barraMadrid.max = valorMaximo
        barraAlicante.max = valorMaximo
        barraCastellon.max = valorMaximo

        barraValencia.progress = ofertasValencia
        barraMadrid.progress = ofertasMadrid
        barraAlicante.progress = ofertasAlicante
        barraCastellon.progress = ofertasCastellon

        textoCantidadValencia.text = ofertasValencia.toString()
        textoCantidadMadrid.text = ofertasMadrid.toString()
        textoCantidadAlicante.text = ofertasAlicante.toString()
        textoCantidadCastellon.text = ofertasCastellon.toString()
    }

    // Fase 10.1
    private fun formatearSalario(salario: Int): String {
        return "%,d".format(salario).replace(",", ".") + " €"
    }

    // Fase 12.1
    private fun abrirSelectorParaGuardarPdf(ciudad: String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/pdf"
        intent.putExtra(Intent.EXTRA_TITLE, "informe_${ciudad}.pdf")

        startActivityForResult(intent, codigoCrearPdf)
    }

    // Fase 14
    private fun exportarInformePdf(uriPdf: Uri) {
        // Crear el documento y la primera pagina (A4)
        val documentoPdf = PdfDocument()
        val pagina = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val paginaPdf = documentoPdf.startPage(pagina)
        val canvas = paginaPdf.canvas

        // 14.1 Titulo
        val pinturaTitulo = Paint()
        pinturaTitulo.color = Color.BLACK
        pinturaTitulo.textSize = 24f
        pinturaTitulo.isFakeBoldText = true
        canvas.drawColor(Color.WHITE)
        canvas.drawText("Informe de ofertas", 32f, 48f, pinturaTitulo)

        // 14.2 Dibujar el informe que ya esta en pantalla
        val anchoDisponible = 531f
        val altoDisponible = 760f
        val anchoInforme = contenedorInforme.width
        val altoInforme = contenedorInforme.height

        if (anchoInforme > 0 && altoInforme > 0) {
            val escala = min(anchoDisponible / anchoInforme, altoDisponible / altoInforme)
            canvas.save()
            canvas.translate(32f, 72f)
            canvas.scale(escala, escala)
            contenedorInforme.draw(canvas)
            canvas.restore()
        }

        // 14.3 Terminar la pagina y escribir el archivo
        documentoPdf.finishPage(paginaPdf)

        try {
            val salida = contentResolver.openOutputStream(uriPdf)
            if (salida != null) {
                salida.use {
                    documentoPdf.writeTo(salida)
                }
                Toast.makeText(this, "PDF guardado", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "No se pudo guardar el PDF", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "No se pudo guardar el PDF", Toast.LENGTH_LONG).show()
        } finally {
            documentoPdf.close()
        }
    }

    private fun obtenerSalarioMinimo(): Int {
        return 18000 + seekBarSalario.progress * 1000
    }

    private fun configurarSeekBarSalario() {
        textoSalario.text = "Salario mínimo: ${formatearSalario(obtenerSalarioMinimo())}"

        seekBarSalario.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textoSalario.text =
                    "Salario mínimo: ${formatearSalario(obtenerSalarioMinimo())}"

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

}
