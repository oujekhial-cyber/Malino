package ir.kharjyar.app.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

@Composable fun ReceiptImagePicker(currentPath:String,onPath:(String)->Unit){val context=LocalContext.current;var cameraUri by remember{mutableStateOf<android.net.Uri?>(null)}
 fun save(uri:android.net.Uri){val dir=File(context.filesDir,"receipts").apply{mkdirs()};val file=File(dir,"${System.currentTimeMillis()}.jpg");context.contentResolver.openInputStream(uri)?.use{i->file.outputStream().use{o->i.copyTo(o)}};onPath(file.absolutePath)}
 val gallery=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){it?.let(::save)};val camera=rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()){if(it)cameraUri?.let(::save)}
 fun open(){val f=File(context.cacheDir,"receipt-camera.jpg");val uri=FileProvider.getUriForFile(context,"${context.packageName}.files",f);cameraUri=uri;camera.launch(uri)}
 val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){if(it)open()}
 Column(verticalArrangement=Arrangement.spacedBy(6.dp)){Text("تصویر رسید",style=MaterialTheme.typography.labelLarge);Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedButton({if(ContextCompat.checkSelfPermission(context,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED)open()else permission.launch(Manifest.permission.CAMERA)},Modifier.weight(1f)){Text("دوربین")};OutlinedButton({gallery.launch("image/*")},Modifier.weight(1f)){Text("گالری")}};if(currentPath.isNotBlank())Text("✓ تصویر رسید ذخیره شد",color=MaterialTheme.colorScheme.primary)}
}
