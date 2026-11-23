package com.finora.app.presentation.screens.scanner

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.finora.app.utils.ReceiptParser
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor() : ViewModel() {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    fun clearError() {
        _errorMessage.value = null
    }

    fun processImage(context: Context, uri: Uri, onAmountFound: (Double) -> Unit) {
        _isLoading.value = true
        _errorMessage.value = null
        try {
            val image = InputImage.fromFilePath(context, uri)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    _isLoading.value = false
                    deleteTempFile(uri)
                    val amount = ReceiptParser.parseAmount(visionText.text)
                    if (amount != null) {
                        onAmountFound(amount)
                    } else {
                        _errorMessage.value = "Nominal total tidak terbaca. Pastikan struk terlihat jelas lalu coba lagi."
                    }
                }
                .addOnFailureListener {
                    _isLoading.value = false
                    deleteTempFile(uri)
                    _errorMessage.value = "Gagal memproses gambar struk. Silakan coba lagi."
                }
        } catch (e: Exception) {
            _isLoading.value = false
            deleteTempFile(uri)
            _errorMessage.value = "Tidak dapat membuka gambar struk."
        }
    }

    private fun deleteTempFile(uri: Uri) {
        uri.path?.let { path ->
            runCatching { File(path).delete() }
        }
    }

    override fun onCleared() {
        super.onCleared()
        recognizer.close()
    }
}
