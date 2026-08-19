package com.posopensrc.core.scanner

import android.content.Context
import android.util.Log
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class BarcodeScanner @Inject constructor() {

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_UPC_E,
            Barcode.FORMAT_CODE_128,
            Barcode.FORMAT_CODE_39,
            Barcode.FORMAT_QR_CODE
        )
        .build()

    private val scanner = BarcodeScanning.getClient(options)

    suspend fun scanFromImage(inputImage: InputImage): List<String> {
        return suspendCancellableCoroutine { continuation ->
            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    val barcodeValues = barcodes.mapNotNull { barcode ->
                        barcode.rawValue
                    }
                    continuation.resume(barcodeValues)
                }
                .addOnFailureListener { e ->
                    Log.e("BarcodeScanner", "Scan failed", e)
                    continuation.resume(emptyList())
                }
        }
    }

    fun scanFromUri(
        context: Context,
        uri: android.net.Uri,
        onResult: (List<String>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            val image = InputImage.fromFilePath(context, uri)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    val barcodeValues = barcodes.mapNotNull { it.rawValue }
                    onResult(barcodeValues)
                }
                .addOnFailureListener { e ->
                    onError(e)
                }
        } catch (e: Exception) {
            onError(e)
        }
    }
}
