package fr.isen.calvet.androidsmartdevice

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import fr.isen.calvet.androidsmartdevice.databinding.ActivityBleDetailsBinding

class BLE_details : AppCompatActivity() {
    private lateinit var binding: ActivityBleDetailsBinding

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    private var bluetoothGatt: BluetoothGatt? = null

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBleDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val deviceName = intent.getStringExtra("Device_name")
        val deviceAddress = intent.getStringExtra("Device_address")
        binding.deviceName.text = deviceName

        val device = bluetoothAdapter!!.getRemoteDevice(deviceAddress)
        bluetoothGatt = device.connectGatt(this, false, gattCallback)
        /*if(bluetoothGatt != null) {
            Log.d("STATUS", "CONNECTED")
        } else {
            Log.d("STATUS", "ERROR NOT WORKING")
        }*/

    }

    // Définissez une implémentation de BluetoothGattCallback pour gérer les événements de connexion.
    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    // Le périphérique a été connecté avec succès.
                    Log.d("STATUS", "Connected to GATT server.")
                    // Découvrez les services GATT disponibles sur le périphérique.
                    bluetoothGatt?.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    // Le périphérique a été déconnecté.
                    Log.d("STATUS", "Disconnected from GATT server.")
                    // Fermez la connexion.
                    bluetoothGatt?.close()
                }
                else -> {
                    Log.d("STATUS", "Connection state changed: $newState")
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    // Les services GATT du périphérique ont été découverts avec succès.
                    Log.d("STATUS", "Services discovered successfully.")
                    // Vous pouvez maintenant parcourir les services et les caractéristiques du périphérique.
                }
                else -> {
                    Log.d("STATUS", "Service discovery failed: $status")
                }
            }
        }
    }
/*
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
            }
        }
    }*/
}