package com.accident.detection

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID

class BluetoothService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var socket: BluetoothSocket? = null
    private val sppUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

    override fun onCreate() {
        super.onCreate()
        startForeground(NotificationHelper.NOTIFICATION_ID, NotificationHelper.baseNotification(this))
        scope.launch { connectAndListen() }
    }

    private suspend fun connectAndListen() {
        try {
            val adapter = BluetoothAdapter.getDefaultAdapter()
                ?: throw IllegalStateException("Bluetooth not supported")
            val device: BluetoothDevice = adapter.bondedDevices.firstOrNull { it.name == "HC-06" }
                ?: throw IllegalStateException("Pair with HC-06 first")

            socket = device.createRfcommSocketToServiceRecord(sppUUID)
            adapter.cancelDiscovery()
            socket!!.connect()

            val reader = BufferedReader(InputStreamReader(socket!!.inputStream))
            var line: String?

            // use scope.isActive to check cancellation
            while (scope.isActive && socket!!.isConnected) {
                line = reader.readLine()
                if (line != null) {
                    NotificationHelper.pushMessage(this@BluetoothService, line)
                } else {
                    break
                }
            }
        } catch (e: Exception) {
            Log.e("BluetoothService", "Error", e)
        } finally {
            stopSelf()
        }
    }

    override fun onDestroy() {
        socket?.close()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
