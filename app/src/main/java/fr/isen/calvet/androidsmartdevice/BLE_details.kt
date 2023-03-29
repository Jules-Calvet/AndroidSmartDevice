@file:Suppress("DEPRECATION")

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
    private val characteristicLedUUID = UUID.fromString("0000abcd-8e22-4541-9d4c-21edae82ed19")
    private val characteristicButtonUUID = UUID.fromString("00001234-8e22-4541-9d4c-21edae82ed19")
    private val configNotifications = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    private var notifications = false

    private var ledBlueOn = false
    private var ledGreenOn = false
    private var ledRedOn = false

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

        hide()

        binding.led1.setOnClickListener {
            toggleLed("blue")
        }
        binding.led2.setOnClickListener {
            toggleLed("green")
        }
        binding.led3.setOnClickListener {
            toggleLed("red")
        }
    }

    @SuppressLint("MissingPermission")
    override fun onPause() {
        super.onPause()
        bluetoothGatt?.close()
    }

    fun toggleLed(color: String) {
        reset(color)
        when(color) {
            "blue" -> {
                if(ledBlueOn) {
                    sendCommand(byteArrayOf(0x00))
                } else {
                    binding.led1.setColorFilter(Color.BLUE)
                    sendCommand(byteArrayOf(0x01))
                }
                ledBlueOn = !ledBlueOn
            }
            "green" -> {
                if(ledGreenOn) {
                    sendCommand(byteArrayOf(0x00))
                } else {
                    binding.led2.setColorFilter(Color.GREEN)
                    sendCommand(byteArrayOf(0x02))
                }
                ledGreenOn = !ledGreenOn
            }
            "red" -> {
                if(ledRedOn) {
                    sendCommand(byteArrayOf(0x00))
                } else {
                    binding.led3.setColorFilter(Color.RED)
                    sendCommand(byteArrayOf(0x03))
                }
                ledRedOn = !ledRedOn
            }
        }
    }

    fun reset(color: String) {
        binding.led1.clearColorFilter()
        binding.led2.clearColorFilter()
        binding.led3.clearColorFilter()
        when(color) {
            "blue" -> {
                ledGreenOn = false
                ledRedOn = false
            }
            "green" -> {
                ledBlueOn = false
                ledRedOn = false
            }
            "red" -> {
                ledBlueOn = false
                ledGreenOn = false
            }
        }
    }

    //Show connect tools in the UI
    fun show() {
        runOnUiThread {
            binding.group.visibility = View.VISIBLE
            binding.status.visibility = View.GONE
            binding.reconnect.visibility = View.GONE
        }
    }

    //Hide connect tools in the UI
    fun hide() {
        runOnUiThread {
            binding.group.visibility = View.GONE
            binding.status.visibility = View.VISIBLE
        }
    }

    @SuppressLint("MissingPermission")
    fun reconnect() {
        val device = bluetoothAdapter!!.getRemoteDevice(intent.getStringExtra("Device_address"))
        bluetoothGatt = device.connectGatt(this, false, gattCallback)
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
                    runOnUiThread {
                        binding.reconnect.visibility = View.VISIBLE
                    }
                    binding.reconnect.setOnClickListener {
                        reconnect()
                    }
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
                    val characteristicButton = service?.getCharacteristic(characteristicButtonUUID)
                    binding.checkBox.setOnClickListener {
                        if(!notifications) {
                            characteristicButton?.let { enableNotifications(it) }
                        } else {
                            characteristicButton?.let { disableNotifications(it) }
                        }
                    }
                }
                else -> {
                    Log.d("STATUS", "Service discovery failed: $status")
                }
            }
        }
        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            if(characteristic?.uuid == characteristicButtonUUID) {
                val value = characteristic?.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)
                runOnUiThread {
                    binding.nombre.text = value.toString()
                }

                Log.d("Bluetooth", "Received value: $value")
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun enableNotifications(characteristic: BluetoothGattCharacteristic) {
        val descriptor = characteristic.getDescriptor(configNotifications)
        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        bluetoothGatt?.writeDescriptor(descriptor)
        bluetoothGatt?.setCharacteristicNotification(characteristic, true)
        notifications = true
    }
    @SuppressLint("MissingPermission")
    fun disableNotifications(characteristic: BluetoothGattCharacteristic) {
        bluetoothGatt?.setCharacteristicNotification(characteristic, false)
        notifications = false
    }

    @SuppressLint("MissingPermission")
    fun sendCommand(command: ByteArray) {
        val service = bluetoothGatt?.getService(serviceUUID)
        val characteristic = service?.getCharacteristic(characteristicLedUUID)
        characteristic?.value = command
        bluetoothGatt?.writeCharacteristic(characteristic)
    }
}