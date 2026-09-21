package ir.kharjyar.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import ir.kharjyar.app.notify.Notifier
import ir.kharjyar.app.ui.AppRoot
import ir.kharjyar.app.ui.AppViewModel
import androidx.activity.viewModels

/**
 * FragmentActivity برای پشتیبانی BiometricPrompt.
 * اعلان‌ها به مقصد مناسب deep link می‌شوند؛ قفل برنامه با اعلان دور زده نمی‌شود
 * (AppRoot تا باز شدن قفل فقط صفحه قفل را نشان می‌دهد).
 */
class MainActivity : FragmentActivity() {

    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel.onAppStart()

        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> viewModel.onAppBackground()
                Lifecycle.Event.ON_START -> viewModel.onAppForeground()
                else -> {}
            }
        })

        val initialDest = parseDest(intent)
        setContent {
            AppRoot(
                viewModel = viewModel,
                initialDestination = initialDest,
                onRequestBiometric = { onSuccess -> showBiometricPrompt(onSuccess) }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // مقصد اعلان جدید از طریق state ویومدل به UI می‌رسد
        parseDest(intent)?.let { PendingDest.value = it }
    }

    private fun parseDest(intent: Intent?): String? {
        intent ?: return null
        val dest = intent.getStringExtra(Notifier.EXTRA_DEST) ?: return null
        val smsId = intent.getLongExtra(Notifier.EXTRA_SMS_ID, -1)
        val txId = intent.getLongExtra(Notifier.EXTRA_TX_ID, -1)
        return when (dest) {
            Notifier.DEST_CONFIRM_TX -> if (txId > 0) "tx/$txId" else "review"
            Notifier.DEST_NEEDS_ACCOUNT -> if (smsId > 0) "accountFromSms/$smsId" else "review"
            Notifier.DEST_NEEDS_TEMPLATE -> if (smsId > 0) "template/$smsId" else "review"
            else -> null
        }
    }

    fun canUseBiometric(): Boolean {
        val bm = BiometricManager.from(this)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_WEAK or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        return bm.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }

    /** احراز هویت برای فعال‌سازی قفل یا بازکردن آن. */
    fun authenticate(onSuccess: () -> Unit) = showBiometricPrompt(onSuccess)

    private fun showBiometricPrompt(onSuccess: () -> Unit) {
        val executor = ContextCompat.getMainExecutor(this)
        val prompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }
        })
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("باز کردن خرج‌یار")
            .setSubtitle("برای مشاهده اطلاعات مالی هویت خود را تأیید کنید")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()
        prompt.authenticate(info)
    }

    companion object {
        /** مقصد ناوبری معلق از اعلان (وقتی Activity زنده است). */
        val PendingDest = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    }
}
