package ir.kharjyar.app.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import ir.kharjyar.app.core.card.CardScan
import ir.kharjyar.app.core.card.CardScanParser
import ir.kharjyar.app.core.text.Digits
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

/**
 * اسکن کارت بانکی با دوربین.
 *
 * تصویر فقط در حافظه پردازش می‌شود: هیچ عکسی ذخیره نمی‌شود و هیچ بایتی از گوشی
 * بیرون نمی‌رود. تشخیص متن با مدل ML Kit انجام می‌شود که داخل خود APK است، پس
 * برنامه همچنان بدون اینترنت و بدون مجوز شبکه کار می‌کند.
 *
 * نتیجه فریم‌ها روی هم انباشته می‌شود (شماره کارت در یک فریم، انقضا در فریم بعد)
 * و هر مقدار فقط وقتی پذیرفته می‌شود که آزمون‌های اعتبارسنجی را رد کند.
 *
 * @param onResult با «تأیید» صدا زده می‌شود؛ بستن بدون نتیجه فقط onDismiss است.
 */
@Composable
fun CardScannerDialog(
    onDismiss: () -> Unit,
    onResult: (CardScan) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var granted by remember { mutableStateOf(hasCameraPermission(context)) }
    var denied by remember { mutableStateOf(false) }
    var cameraError by remember { mutableStateOf<String?>(null) }
    var torchOn by remember { mutableStateOf(false) }
    var scan by remember { mutableStateOf(CardScan()) }
    var camera by remember { mutableStateOf<Camera?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { ok ->
        granted = ok
        denied = !ok
    }

    LaunchedEffect(Unit) {
        if (!granted) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // شماره کارت و انقضا و CVV2 که پیدا شد، کار تمام است
    LaunchedEffect(scan.hasCardBasics) {
        if (scan.hasCardBasics) {
            delay(500) // یک لحظه مکث تا کاربر تیک‌ها را ببیند
            onResult(scan)
        }
    }

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            // داخل Dialog، TextureView از SurfaceView قابل اتکاتر است
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    DisposableEffect(granted) {
        if (!granted) return@DisposableEffect onDispose { }

        val executor = Executors.newSingleThreadExecutor()
        val recognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val providerFuture = ProcessCameraProvider.getInstance(context)

        providerFuture.addListener({
            try {
                val provider = providerFuture.get()
                val preview = Preview.Builder().build().apply {
                    setSurfaceProvider(previewView.surfaceProvider)
                }
                val analysis = ImageAnalysis.Builder()
                    // ۶۴۰×۴۸۰ پیش‌فرض برای خواندن ارقام ریز کارت کافی نیست
                    .setResolutionSelector(
                        ResolutionSelector.Builder()
                            .setResolutionStrategy(
                                ResolutionStrategy(
                                    Size(1280, 720),
                                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                                )
                            )
                            .build()
                    )
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analysis.setAnalyzer(executor, CardAnalyzer(recognizer) { text ->
                    val found = CardScanParser.parse(text)
                    if (!found.isEmpty) scan = scan.mergedWith(found)
                })
                provider.unbindAll()
                camera = provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis
                )
            } catch (e: Exception) {
                cameraError = "دوربین در دسترس نیست. اطلاعات کارت را دستی وارد کنید."
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            runCatching { providerFuture.get().unbindAll() }
            camera = null
            executor.shutdown()
            recognizer.close()
        }
    }

    LaunchedEffect(torchOn, camera) {
        runCatching { camera?.cameraControl?.enableTorch(torchOn) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

            if (granted && cameraError == null) {
                AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

                // کادر راهنما با نسبت کارت بانکی
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(0.9f)
                        .aspectRatio(1.586f)
                        .border(2.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
                )
            }

            // ---------- نوار بالا ----------
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "بستن", tint = Color.White)
                }
                if (granted && cameraError == null) {
                    IconButton(onClick = { torchOn = !torchOn }) {
                        Icon(
                            if (torchOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                            contentDescription = if (torchOn) "خاموش کردن چراغ" else "روشن کردن چراغ",
                            tint = Color.White
                        )
                    }
                }
            }

            // ---------- پایین: راهنما، یافته‌ها و دکمه‌ها ----------
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when {
                        cameraError != null -> Text(
                            cameraError!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )

                        !granted -> {
                            Text("اجازه دوربین", style = MaterialTheme.typography.titleSmall)
                            Text(
                                if (denied)
                                    "برای اسکن کارت به دوربین نیاز است. اگر پیام اجازه دیگر نمایش داده نمی‌شود، " +
                                        "از تنظیمات برنامه دسترسی دوربین را روشن کنید."
                                else "برای خواندن اطلاعات کارت، اجازه دوربین را بدهید.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = {
                                    if (denied) openAppSettings(context)
                                    else permissionLauncher.launch(Manifest.permission.CAMERA)
                                }) { Text(if (denied) "باز کردن تنظیمات" else "اجازه می‌دهم") }
                                TextButton(onClick = onDismiss) { Text("انصراف") }
                            }
                        }

                        else -> {
                            Text(
                                "کارت را داخل کادر و زیر نور یکنواخت بگیرید. " +
                                    "هر چه خوانده شود همین‌جا تیک می‌خورد.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            FoundRow("شماره کارت", scan.cardNumber, group = true)
                            FoundRow("تاریخ انقضا", scan.expiry)
                            FoundRow("CVV2", scan.cvv2)
                            FoundRow("شبا", scan.iban)
                            Text(
                                "عنوان حساب را خودتان انتخاب می‌کنید و شماره حساب را اگر روی کارت نبود " +
                                    "دستی وارد کنید.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onResult(scan) },
                                    enabled = !scan.isEmpty,
                                    modifier = Modifier.weight(1f)
                                ) { Text("استفاده از این اطلاعات") }
                                TextButton(onClick = onDismiss) { Text("انصراف") }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** یک سطر «پیدا شد / هنوز پیدا نشده». */
@Composable
private fun FoundRow(label: String, value: String, group: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        if (value.isBlank()) {
            Text(
                "—",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    Digits.toPersian(if (group) value.chunked(4).joinToString(" ") else value),
                    style = MaterialTheme.typography.bodyMedium
                )
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "پیدا شد",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
    }
}

/**
 * هر فریم دوربین را به تشخیص‌دهنده متن می‌دهد.
 * فریم‌ها فقط در حافظه‌اند و بلافاصله بعد از پردازش آزاد می‌شوند.
 */
private class CardAnalyzer(
    private val recognizer: TextRecognizer,
    private val onText: (String) -> Unit
) : ImageAnalysis.Analyzer {

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val media = imageProxy.image
        if (media == null) {
            imageProxy.close()
            return
        }
        val input = InputImage.fromMediaImage(media, imageProxy.imageInfo.rotationDegrees)
        recognizer.process(input)
            // شنونده پیش‌فرض روی رشته اصلی اجرا می‌شود؛ به‌روزرسانی state امن است
            .addOnSuccessListener { result -> onText(result.text) }
            .addOnCompleteListener { imageProxy.close() }
    }
}

private fun hasCameraPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED

private fun openAppSettings(context: Context) {
    runCatching {
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }
}
