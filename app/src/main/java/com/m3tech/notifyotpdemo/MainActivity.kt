package com.m3tech.notifyotpdemo

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.FirebaseApp
import com.m3tech.m3notify.AppSingleton
import com.m3tech.m3notify.AppSingleton.decryptOTPWithIV
import com.m3tech.m3notify.AppSingleton.encryptOTPWithIV
import com.m3tech.m3notify.AppSingleton.getSecretKeyFromBase64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Handle edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Collect the data from AppSingleton's MutableStateFlow
        lifecycleScope.launch {
            AppSingleton.dataFlow.collect { value ->
                println("Received OTP Data: $value")

                // The Base64-encoded secret key
                val secretKeyBase64 = "nB9k/Rrt/egXSgE+gR8Xmw=="
                val secretKey = getSecretKeyFromBase64(secretKeyBase64)

                // Simulate IV (this should come from the server or encryption context)
                val iv = "HlCfvTOiLrSfQf0BWEJq4Q=="

                // Decrypt OTP using the collected data (encrypted OTP)
                val decryptedOtp = decryptOTPWithIV(value, iv, secretKey)
                println("Decrypted OTP: $decryptedOtp")

                // Update the OTP field in the UI
                withContext(Dispatchers.Main) {
                    findViewById<AppCompatEditText>(R.id.otp_view).setText(decryptedOtp)
                }
            }
        }

        // Clear button click listener
        findViewById<AppCompatButton>(R.id.clear_btn).setOnClickListener {
            AppSingleton.updateData("")
            findViewById<AppCompatEditText>(R.id.otp_view).setText("")

            // Encrypt a sample OTP for demonstration
            val secretKeyBase64 = "nB9k/Rrt/egXSgE+gR8Xmw=="
            val secretKey = getSecretKeyFromBase64(secretKeyBase64)

            val otp = "123456"
            println("Original OTP: $otp")

            // Encrypt OTP and get the Base64-encoded ciphertext and IV
            val (encryptedOtp, iv) = encryptOTPWithIV(otp, secretKey)
            println("Encrypted OTP: $encryptedOtp")
            println("IV used for encryption: $iv")

            // Decrypt OTP using the same IV
            val decryptedOtp = decryptOTPWithIV(encryptedOtp, iv, secretKey)
            println("Decrypted OTP: $decryptedOtp")
        }

        // Request notification permission (for Android 13 and above)
        requestNotificationPermissionFlow(this)
        getDeviceId(this)
    }

    // Function to check notification permission and emit status using Flow
    private fun checkNotificationPermissionFlow(context: Context) = flow {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            emit(notificationManager.areNotificationsEnabled())
        } else {
            // No need to check for permission on versions below Android 13
            emit(true)
        }
    }

    // Function to request notification permission if not granted
    private fun requestNotificationPermissionFlow(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Check notification permission using the Flow
            lifecycleScope.launch(Dispatchers.Main) {
                checkNotificationPermissionFlow(activity).collect { isGranted ->
                    if (!isGranted) {
                        // If permission is not granted, ask the user to enable notifications in settings
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, activity.packageName)
                        }
                        activity.startActivity(intent)
                        Toast.makeText(activity, "Please enable notifications for this app.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            // No need to request permission on versions below Android 13
            Toast.makeText(activity, "Notifications are automatically allowed on this version.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getDeviceId(context: Context): String {
        val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        Log.i("DeviceID", "Device ID: $deviceId")
        return deviceId
    }
}