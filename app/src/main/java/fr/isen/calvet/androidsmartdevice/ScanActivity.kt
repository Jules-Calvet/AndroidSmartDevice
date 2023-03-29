package fr.isen.calvet.androidsmartdevice

import android.Manifest.permission
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import fr.isen.calvet.androidsmartdevice.databinding.ActivityScanBinding


class ScanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScanBinding
    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private lateinit var bluetoothLeScanner : BluetoothLeScanner

    private val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            permission.BLUETOOTH_CONNECT,
            permission.BLUETOOTH_SCAN,
            permission.ACCESS_FINE_LOCATION,
            permission.ACCESS_COARSE_LOCATION
        )
    } else {
        arrayOf(
            permission.ACCESS_FINE_LOCATION,
            permission.ACCESS_COARSE_LOCATION
        )
    }

    private var scanning = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bluetoothLeScanner = bluetoothAdapter!!.bluetoothLeScanner


        if (REQUIRED_PERMISSIONS.all {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }) {
            if (REQUIRED_PERMISSIONS.all {
                    ActivityCompat.shouldShowRequestPermissionRationale(this, it)
            }) {
                // Expliquer à l'utilisateur pourquoi la permission est nécessaire
                AlertDialog.Builder(this)
                    .setTitle("Autoriser la permission Bluetooth")
                    .setMessage("L'application a besoin de la permission Bluetooth pour détecter les appareils à proximité.")
                    .setPositiveButton("OK") { _, _ ->
                        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, 1)
                    }
                    .setNegativeButton("Annuler", null)
                    .show()
            } else {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, 1)
            }
        } else {
            if (bluetoothAdapter == null) {
                Toast.makeText(this, "Le Bluetooth n'est pas disponible sur ce téléphone", Toast.LENGTH_SHORT).show()
            } else if (!bluetoothAdapter!!.isEnabled) {
                Toast.makeText(this, "Le Bluetooth est désactivé", Toast.LENGTH_SHORT).show()
            } else {
                // Bluetooth est activé, continuer avec les interactions Bluetooth
                binding.floatingActionButton.setOnClickListener {
                    if(!scanning) {
                        binding.textView3.setText(R.string.scanning)
                        binding.floatingActionButton.setImageResource(android.R.drawable.ic_media_pause)
                        binding.progressBar.isIndeterminate = true
                        scanLeDevice(bluetoothLeScanner/*, scanning*/)
                    } else {
                        binding.textView3.setText(R.string.toScan)
                        binding.floatingActionButton.setImageResource(android.R.drawable.ic_media_play)
                        binding.progressBar.isIndeterminate = false
                        scanLeDevice(bluetoothLeScanner/*, scanning*/)
                    }

                    //scanning = !scanning

                    binding.recyclerView.layoutManager = LinearLayoutManager(this)
                    /*val devices: ArrayList<String> = ArrayList<String>()
                    devices.add("Jules")
                    devices.add("Lab_IOT")
                    devices.add("Lab")
                    devices.add("Jules_IOT")*/
                    /*binding.recyclerView.adapter = AdapterDevicesList(leDeviceListAdapter) {

                    }*/
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if(bluetoothAdapter!!.isEnabled && REQUIRED_PERMISSIONS.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }) {
            Log.d("SCANNING", "$scanning")
            if(scanning) {
                scanLeDevice(bluetoothLeScanner/*, scanning*/)
                binding.textView3.setText(R.string.toScan)
                binding.floatingActionButton.setImageResource(android.R.drawable.ic_media_play)
                binding.progressBar.isIndeterminate = false
            }
        }
    }

    @Suppress("DEPRECATION")
    private val handler = Handler()
    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000

    @SuppressLint("MissingPermission")
    private fun scanLeDevice(bluetoothLeScanner: BluetoothLeScanner/*, scanning : Boolean*/) {
        //var scanningBle = scanning
        if (!scanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                scanning = false
                bluetoothLeScanner.stopScan(leScanCallback)
                binding.textView3.setText(R.string.toScan)
                binding.floatingActionButton.setImageResource(android.R.drawable.ic_media_play)
                binding.progressBar.isIndeterminate = false

            }, SCAN_PERIOD)
            scanning = true
            bluetoothLeScanner.startScan(leScanCallback)
        } else {
            scanning = false
            bluetoothLeScanner.stopScan(leScanCallback)
        }
    }

    private val leDeviceListAdapter = Devices()
    // Device scan callback.
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            leDeviceListAdapter.addDevice(result.device, result.rssi)
            binding.recyclerView.adapter = AdapterDevicesList(leDeviceListAdapter) { device, position ->
                val intent = Intent(this@ScanActivity, BLE_details::class.java)
                intent.putExtra("Device_name", device.device_name[position])
                intent.putExtra("Device_address", device.MAC[position])
                startActivity(intent)
                onPause()
            }
            //leDeviceListAdapter.notifyDataSetChanged()
        }
    }

    class Devices {
        var device_name: ArrayList<String> = ArrayList()
        var MAC: ArrayList<String> = ArrayList()
        var distance : ArrayList<Int> = ArrayList()
        var size = 0

        @SuppressLint("MissingPermission")
        fun addDevice(device: BluetoothDevice, rssi : Int) {
            if (!device.name.isNullOrBlank()) {
                if(!MAC.contains(device.address)) {
                    device_name.add(device.name)
                    MAC.add(device.address)
                    distance.add(rssi)
                    size++
                    Log.d("Device", "${device.name} + $MAC")
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // La permission a été accordée
                startActivity(Intent(this, ScanActivity::class.java))
            } else {
                Toast.makeText(
                    this,
                    "Les permissions sont nécessaires pour utiliser la fonction de scan",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }
}