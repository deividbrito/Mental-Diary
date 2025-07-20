package br.edu.ifsp.dmo2.mentaldiary.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import br.edu.ifsp.dmo2.mentaldiary.R
import br.edu.ifsp.dmo2.mentaldiary.model.DiaryEntry
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.*

class EntryFormActivity : AppCompatActivity() {

    // campos da tela
    private lateinit var editTextTexto: EditText
    private lateinit var buttonTranscricao: Button
    private lateinit var radioGroupHumor: RadioGroup
    private lateinit var buttonImagem: Button
    private lateinit var imagePreview: ImageView
    private lateinit var buttonSalvar: Button

    // reconhecedor de voz
    private lateinit var speechRecognizer: SpeechRecognizer

    // imagem tirada e entrada que ta sendo editada (se for o caso)
    private var imagemBitmap: Bitmap? = null
    private var entradaExistente: DiaryEntry? = null

    // firebase: banco, storage e auth
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val REQUEST_RECORD_AUDIO = 1
        private const val REQUEST_IMAGE_CAPTURE = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry_form)

        // conecta os campos da tela
        editTextTexto = findViewById(R.id.editTextTexto)
        buttonTranscricao = findViewById(R.id.buttonTranscricao)
        radioGroupHumor = findViewById(R.id.radioGroupHumor)
        buttonImagem = findViewById(R.id.buttonImagem)
        imagePreview = findViewById(R.id.imagePreview)
        buttonSalvar = findViewById(R.id.buttonSalvar)

        // pede permissoes de microfone e camera
        solicitarPermissoes()

        // verifica se veio uma entrada pra editar
        entradaExistente = intent.getParcelableExtra("entry")

        // se tiver entrada, preenche os campos com os dados dela
        entradaExistente?.let { entrada ->
            editTextTexto.setText(entrada.texto)
            when (entrada.humor) {
                "Feliz" -> radioGroupHumor.check(R.id.radioFeliz)
                "Triste" -> radioGroupHumor.check(R.id.radioTriste)
                "Neutro" -> radioGroupHumor.check(R.id.radioNeutro)
            }
            if (!entrada.imagemUrl.isNullOrEmpty()) {
                Glide.with(this).load(entrada.imagemUrl).into(imagePreview)
                imagePreview.visibility = ImageView.VISIBLE
            }
        }

        // prepara o intent pro reconhecimento de voz
        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
        }

        // configura o speechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onError(error: Int) {
                Toast.makeText(this@EntryFormActivity, "erro na transcricao", Toast.LENGTH_SHORT).show()
            }
            override fun onResults(results: Bundle?) {
                val result = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                result?.let { editTextTexto.setText(it[0]) }
            }
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        // acao do botao de transcricao
        buttonTranscricao.setOnClickListener {
            Toast.makeText(this, "fale agora...", Toast.LENGTH_SHORT).show()
            speechRecognizer.startListening(recognizerIntent)
        }

        // acao do botao pra tirar foto
        buttonImagem.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        }

        // acao do botao salvar
        buttonSalvar.setOnClickListener {
            salvarEntrada()
        }
    }

    // pede permissoes de microfone e camera
    private fun solicitarPermissoes() {
        val permissoes = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissoes.add(Manifest.permission.RECORD_AUDIO)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissoes.add(Manifest.permission.CAMERA)
        }
        if (permissoes.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissoes.toTypedArray(), 100)
        }
    }

    // salva ou atualiza a entrada no firestore
    private fun salvarEntrada() {
        val texto = editTextTexto.text.toString().trim()
        val userId = auth.currentUser?.uid
        val humorId = radioGroupHumor.checkedRadioButtonId

        // valida os campos obrigatorios
        if (texto.isEmpty() || humorId == -1 || userId == null) {
            Toast.makeText(this, "preencha todos os campos obrigatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val humor = findViewById<RadioButton>(humorId).text.toString()
        val entryData: HashMap<String, Any> = hashMapOf(
            "userId" to userId,
            "texto" to texto,
            "humor" to humor,
            "dataCriacao" to com.google.firebase.Timestamp.now(),
            "foiPorVoz" to false
        )

        // funcao auxiliar pra salvar no firestore
        fun continuarComFirestore() {
            val collection = firestore.collection("entries")
            val task = entradaExistente?.id?.let { id ->
                collection.document(id).set(entryData)
            } ?: collection.add(entryData)

            task.addOnSuccessListener {
                setResult(RESULT_OK)
                Toast.makeText(this, "entrada salva com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "erro ao salvar entrada", Toast.LENGTH_SHORT).show()
            }
        }

        // se tiver imagem, faz upload antes de salvar
        if (imagemBitmap != null) {
            val baos = ByteArrayOutputStream()
            imagemBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val imageData = baos.toByteArray()

            val imageRef = storage.reference.child("imagens/${UUID.randomUUID()}.jpg")
            imageRef.putBytes(imageData)
                .addOnSuccessListener {
                    imageRef.downloadUrl
                        .addOnSuccessListener { uri ->
                            entryData["imagemUrl"] = uri.toString()
                            continuarComFirestore()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "erro ao obter url da imagem", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "upload falhou", Toast.LENGTH_LONG).show()
                }
        } else {
            continuarComFirestore()
        }
    }

    // trata o retorno da foto tirada
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            imagemBitmap = data?.extras?.get("data") as Bitmap
            imagePreview.setImageBitmap(imagemBitmap)
            imagePreview.visibility = ImageView.VISIBLE
        }
    }

    // libera o recurso do reconhecimento de voz
    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }
}
