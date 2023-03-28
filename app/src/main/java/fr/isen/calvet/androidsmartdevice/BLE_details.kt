package fr.isen.calvet.androidsmartdevice

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import fr.isen.calvet.androidsmartdevice.databinding.ActivityBleDetailsBinding
import java.util.*

class BLE_details : AppCompatActivity() {
    private lateinit var binding: ActivityBleDetailsBinding

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    private var bluetoothGatt: BluetoothGatt? = null

    private val serviceUUID = UUID.fromString("0000feed-cc7a-482a-984a-7f2ed5b3e58f")
    private val characteristicUUID = UUID.fromString("0000abcd-8e22-4541-9d4c-21edae82ed19")

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

        var ledBlueOn = false
        var ledGreenOn = false
        var ledRedOn = false

        hide()

        binding.led1.setOnClickListener {
            if(ledBlueOn) {
                binding.led1.clearColorFilter()
                sendCommand(byteArrayOf(0x00))
            } else {
                binding.led1.setColorFilter(Color.BLUE)
                sendCommand(byteArrayOf(0x01))
                binding.led2.clearColorFilter()
                binding.led3.clearColorFilter()
                ledGreenOn = false
                ledRedOn = false
            }
            ledBlueOn = !ledBlueOn
        }
        binding.led2.setOnClickListener {
            if(ledGreenOn) {
                binding.led2.clearColorFilter()
                sendCommand(byteArrayOf(0x00))
            } else {
                binding.led2.setColorFilter(Color.GREEN)
                sendCommand(byteArrayOf(0x02))
                binding.led1.clearColorFilter()
                binding.led3.clearColorFilter()
                ledBlueOn = false
                ledRedOn = false
            }
            ledGreenOn = !ledGreenOn
        }
        binding.led3.setOnClickListener {
            if(ledRedOn) {
                binding.led3.clearColorFilter()
                sendCommand(byteArrayOf(0x00))
            } else {
                binding.led3.setColorFilter(Color.RED)
                sendCommand(byteArrayOf(0x03))
                binding.led1.clearColorFilter()
                binding.led2.clearColorFilter()
                ledBlueOn = false
                ledGreenOn = false
            }
            ledRedOn = !ledRedOn
        }
    }

    fun show() {
        runOnUiThread {
            binding.group.visibility = View.VISIBLE
            binding.status.visibility = View.GONE
        }
    }

    fun hide() {
        runOnUiThread {
            binding.group.visibility = View.GONE
            binding.status.visibility = View.VISIBLE
        }
    }

    // Définissez une implémentation de BluetoothGattCallback pour gérer les événements de connexion.
    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    // Le périphérique a été connecté avec succès.
                    Log.d("STATUS", "Connected to GATT server.")
                    show()
                    // Découvrez les services GATT disponibles sur le périphérique.
                    bluetoothGatt?.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    // Le périphérique a été déconnecté.
                    Log.d("STATUS", "Disconnected from GATT server.")
                    hide()
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
                    val service = gatt?.getService(serviceUUID)
                    val characteristic = service?.getCharacteristic(characteristicUUID)
                    characteristic?.let { enableNotifications(it) }
                    // Vous pouvez maintenant parcourir les services et les caractéristiques du périphérique.
                }
                else -> {
                    Log.d("STATUS", "Service discovery failed: $status")
                }
            }
        }
        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            val value = characteristic?.toString()
            Log.d("Bluetooth", "Received value: $value")
        }
    }

    @SuppressLint("MissingPermission")
    fun enableNotifications(characteristic: BluetoothGattCharacteristic) {
        bluetoothGatt?.setCharacteristicNotification(characteristic, true)
    }
    @SuppressLint("MissingPermission")
    fun sendCommand(command: ByteArray) {
        val service = bluetoothGatt?.getService(serviceUUID)
        val characteristic = service?.getCharacteristic(characteristicUUID)
        characteristic?.setValue(command)
        bluetoothGatt?.writeCharacteristic(characteristic)
    }
}