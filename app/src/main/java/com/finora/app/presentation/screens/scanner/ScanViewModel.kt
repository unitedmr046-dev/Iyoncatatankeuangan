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

    fun processImage(context: Context, uri: Uri, onResult: (Double?) -> Unit) {
        _isLoading.value = true
        try {
            val image = InputImage.fromFilePath(context, uri)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    _isLoading.value = false
                    onResult(ReceiptParser.parseAmount(visionText.text))
                }
                .addOnFailureListener {
                    _isLoading.value = false
                    onResult(null)
                }
                .addOnCompleteListener {
                    if (uri.scheme == "file") {
                        uri.path?.let { File(it).delete() }
                    }
                }
        } catch (e: Exception) {
            _isLoading.value = false
            if (uri.scheme == "file") {
                uri.path?.let { File(it).delete() }
            }
            onResult(null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        recognizer.close()
    }
}
