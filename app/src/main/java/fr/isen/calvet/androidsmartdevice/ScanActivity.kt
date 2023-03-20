package fr.isen.calvet.androidsmartdevice

import android.Manifest
import android.Manifest.permission
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
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
    private var scanning = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)



        if (ContextCompat.checkSelfPermission(this, permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission.BLUETOOTH_CONNECT)) {
                // Expliquer à l'utilisateur pourquoi la permission est nécessaire
                AlertDialog.Builder(this)
                    .setTitle("Autoriser la permission Bluetooth")
                    .setMessage("L'application a besoin de la permission Bluetooth pour détecter les appareils à proximité.")
                    .setPositiveButton("OK") { _, _ ->
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 1)
                    }
                    .setNegativeButton("Annuler", null)
                    .show()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(permission.BLUETOOTH_CONNECT), 1)
            }
        }
        else
        {
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
                    } else {
                        binding.textView3.setText(R.string.toScan)
                        binding.floatingActionButton.setImageResource(android.R.drawable.ic_media_play)
                        binding.progressBar.isIndeterminate = false
                    }

                    scanning = !scanning

                    binding.recyclerView.layoutManager = LinearLayoutManager(this)
                    val devices: ArrayList<String> = ArrayList<String>()
                    devices.add("Jules")
                    devices.add("Lab_IOT")
                    devices.add("Lab")
                    devices.add("Jules_IOT")
                    binding.recyclerView.adapter = AdapterDevicesList(devices)
                }
            }
        }

        /*when {
            ContextCompat.checkSelfPermission(
                CONTEXT,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
            }
            shouldShowRequestPermissionRationale("We can't scan devices if not enabled") -> {
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected, and what
            // features are disabled if it's declined. In this UI, include a
            // "cancel" or "no thanks" button that lets the user continue
            // using your app without granting the permission.
            showInContextUI("I understand but no")
        }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                    Manifest.permission.BLUETOOTH)
            }
        }*/

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