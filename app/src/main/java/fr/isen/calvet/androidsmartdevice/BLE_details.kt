package fr.isen.calvet.androidsmartdevice

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import fr.isen.calvet.androidsmartdevice.databinding.ActivityBleDetailsBinding

class BLE_details : AppCompatActivity() {
    private lateinit var binding: ActivityBleDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBleDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val deviceName = intent.getStringExtra("Device_name")
        binding.deviceName.text = deviceName
    }
}