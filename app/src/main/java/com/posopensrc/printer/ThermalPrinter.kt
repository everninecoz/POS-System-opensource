package com.posopensrc.printer

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

class ThermalPrinter(private val context: Context) {

    private val TAG = "ThermalPrinter"
    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var isConnected: Boolean = false

    // Default paper size, can be changed
    var paperSize: PaperSize = PaperSize.WIDTH_80MM
        private set

    init {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    /**
     * Set paper size for the printer
     */
    fun setPaperSize(size: PaperSize) {
        paperSize = size
        Log.d(TAG, "Paper size set to ${size.displayName}")
    }

    /**
     * Set paper size by width in mm
     */
    fun setPaperSizeByMm(widthMm: Int) {
        paperSize = PaperSize.fromWidthMm(widthMm)
        Log.d(TAG, "Paper size set to ${paperSize.displayName}")
    }

    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<BluetoothDevice> {
        return bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
    }

    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice): Boolean {
        return try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            bluetoothSocket?.connect()
            outputStream = bluetoothSocket?.outputStream
            isConnected = true
            Log.d(TAG, "Connected to ${device.name}")
            true
        } catch (e: IOException) {
            Log.e(TAG, "Connection failed: ${e.message}")
            isConnected = false
            false
        }
    }

    fun disconnect() {
        try {
            outputStream?.close()
            bluetoothSocket?.close()
            isConnected = false
            Log.d(TAG, "Disconnected")
        } catch (e: IOException) {
            Log.e(TAG, "Disconnect failed: ${e.message}")
        }
    }

    /**
     * Initialize printer with ESC/POS commands
     */
    private fun initializePrinter(): Boolean {
        return try {
            // ESC @ - Initialize printer
            val initBytes = byteArrayOf(0x1B, 0x40)
            outputStream?.write(initBytes)

            // Set character code table to UTF-8
            val charCode = byteArrayOf(0x1B, 0x74, 0x06)
            outputStream?.write(charCode)

            // Set line spacing
            val lineSpacing = byteArrayOf(0x1B, 0x33, 0x00)
            outputStream?.write(lineSpacing)

            true
        } catch (e: IOException) {
            Log.e(TAG, "Initialize failed: ${e.message}")
            false
        }
    }

    fun printText(text: String): Boolean {
        if (!isConnected || outputStream == null) {
            Log.e(TAG, "Not connected")
            return false
        }

        return try {
            // Initialize printer first
            initializePrinter()

            val bytes = text.toByteArray(Charsets.UTF_8)
            outputStream?.write(bytes)
            outputStream?.flush()
            Log.d(TAG, "Printed ${bytes.size} bytes")
            true
        } catch (e: IOException) {
            Log.e(TAG, "Print failed: ${e.message}")
            false
        }
    }

    fun printReceipt(receipt: String): Boolean {
        return try {
            printText(receipt)
            feedAndCut()
            true
        } catch (e: IOException) {
            Log.e(TAG, "Print receipt failed: ${e.message}")
            false
        }
    }

    /**
     * Feed paper and cut
     */
    private fun feedAndCut() {
        try {
            // Feed 3 lines
            val feed = byteArrayOf(0x1B, 0x64, 0x03)
            outputStream?.write(feed)

            // Cut paper (GS V 1)
            val cutBytes = byteArrayOf(0x1D, 0x56, 0x01)
            outputStream?.write(cutBytes)
        } catch (e: IOException) {
            Log.e(TAG, "Feed and cut failed: ${e.message}")
        }
    }

    /**
     * Set bold mode
     */
    fun setBold(enabled: Boolean): Boolean {
        return try {
            val bold = if (enabled) byteArrayOf(0x1B, 0x45, 0x01)
            else byteArrayOf(0x1B, 0x45, 0x00)
            outputStream?.write(bold)
            true
        } catch (e: IOException) {
            false
        }
    }

    /**
     * Set text size (double width/height)
     */
    fun setTextSize(widthMultiplier: Int = 1, heightMultiplier: Int = 1): Boolean {
        return try {
            // GS ! n where n is combination of width and height
            // Width: bits 4-6, Height: bits 0-2
            val width = when {
                widthMultiplier >= 8 -> 7
                widthMultiplier >= 4 -> 6
                widthMultiplier >= 2 -> 1
                else -> 0
            }
            val height = when {
                heightMultiplier >= 8 -> 7
                heightMultiplier >= 4 -> 6
                heightMultiplier >= 2 -> 1
                else -> 0
            }
            val size = (width shl 4) or height
            val sizeBytes = byteArrayOf(0x1D, 0x21, size.toByte())
            outputStream?.write(sizeBytes)
            true
        } catch (e: IOException) {
            false
        }
    }

    /**
     * Reset text size to normal
     */
    fun resetTextSize(): Boolean {
        return try {
            val sizeBytes = byteArrayOf(0x1D, 0x21, 0x00)
            outputStream?.write(sizeBytes)
            true
        } catch (e: IOException) {
            false
        }
    }

    fun isConnected(): Boolean = isConnected

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: ThermalPrinter? = null

        fun getInstance(context: Context): ThermalPrinter {
            return instance ?: synchronized(this) {
                instance ?: ThermalPrinter(context.applicationContext).also { instance = it }
            }
        }
    }
}
