package ir.kharjyar.app.ui.screens

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ir.kharjyar.app.core.identity.Username
import ir.kharjyar.app.data.db.UserProfileEntity
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.showSavedMessage
import java.io.File
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(vm: AppViewModel, nav: NavHostController) {
    val profile by vm.repo.db.civicDao().observeProfile().collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var username by remember(profile) { mutableStateOf(profile?.username.orEmpty()) }
    var name by remember(profile) { mutableStateOf(profile?.displayName.orEmpty()) }
    var imagePath by remember(profile) { mutableStateOf(profile?.imagePath.orEmpty()) }
    var message by remember { mutableStateOf<String?>(null) }
    val usernameValid = Username.isValid(username)

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val dir = File(context.filesDir, "profiles").apply { mkdirs() }
            val file = File(dir, "profile.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { input.copyTo(it) }
            }
            imagePath = file.absolutePath
        }
    }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("پروفایل کاربری", style = MaterialTheme.typography.headlineSmall)
        imagePath.takeIf { it.isNotBlank() }
            ?.let { remember(it) { BitmapFactory.decodeFile(it)?.asImageBitmap() } }
            ?.let { Image(it, null, Modifier.size(96.dp).clip(CircleShape)) }
        OutlinedButton({ picker.launch("image/*") }, Modifier.fillMaxWidth()) { Text("انتخاب عکس پروفایل") }
        OutlinedTextField(name, { name = it }, label = { Text("نام نمایشی") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            value = username,
            onValueChange = { username = Username.sanitize(it); message = null },
            label = { Text("نام کاربری انگلیسی") },
            prefix = { Text("@") },
            supportingText = {
                Text(if (username.isNotEmpty() && !usernameValid) "حداقل ۳ نویسه؛ فقط حروف انگلیسی کوچک، عدد و _" else "نمونه: ali_1370")
            },
            isError = username.isNotEmpty() && !usernameValid,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Ltr),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                if (!usernameValid) {
                    message = "نام کاربری باید ۳ تا ۳۰ نویسه و فقط شامل حروف انگلیسی، عدد یا زیرخط باشد"
                    return@Button
                }
                scope.launch {
                    if (vm.repo.db.civicDao().usernameCount(username) > 0) {
                        message = "این نام کاربری تکراری است"
                    } else {
                        vm.repo.db.civicDao().saveProfile(
                            UserProfileEntity(username=username, displayName=name, imagePath=imagePath, remoteId=profile?.remoteId, syncPending=true)
                        )
                        showSavedMessage(context, "پروفایل")
                        nav.popBackStack()
                    }
                }
            },
            enabled = usernameValid,
            modifier = Modifier.fillMaxWidth()
        ) { Text("ذخیره پروفایل") }
        message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
