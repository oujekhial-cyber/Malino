package ir.kharjyar.app.ui.screens
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.data.db.UserProfileEntity
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.showSavedMessage
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import java.io.File

@Composable fun ProfileScreen(vm:AppViewModel,nav:NavHostController){val profile by vm.repo.db.civicDao().observeProfile().collectAsState(initial=null);val scope=rememberCoroutineScope();val context=LocalContext.current;var username by remember(profile){mutableStateOf(profile?.username.orEmpty())};var name by remember(profile){mutableStateOf(profile?.displayName.orEmpty())};var imagePath by remember(profile){mutableStateOf(profile?.imagePath.orEmpty())};var message by remember{mutableStateOf<String?>(null)}
 val picker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){uri->if(uri!=null){val dir=File(context.filesDir,"profiles").apply{mkdirs()};val f=File(dir,"profile.jpg");context.contentResolver.openInputStream(uri)?.use{input->f.outputStream().use{input.copyTo(it)}};imagePath=f.absolutePath}}
 Column(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text("پروفایل کاربری",style=MaterialTheme.typography.headlineSmall);imagePath.takeIf{it.isNotBlank()}?.let{remember(it){BitmapFactory.decodeFile(it)?.asImageBitmap()}}?.let{Image(it,null,Modifier.size(96.dp).clip(CircleShape))};OutlinedButton({picker.launch("image/*")},Modifier.fillMaxWidth()){Text("انتخاب عکس پروفایل")};OutlinedTextField(name,{name=it},label={Text("نام نمایشی")},modifier=Modifier.fillMaxWidth());OutlinedTextField(username,{username=it.lowercase().filter{c->c.isLetterOrDigit()||c=='_'}.take(30)},label={Text("نام کاربری")},supportingText={Text("فعلاً روی این گوشی یکتا است؛ ساختار برای اتصال آینده به سرور آماده شده است.")},modifier=Modifier.fillMaxWidth());Button({scope.launch{if(username.length<3){message="نام کاربری حداقل ۳ نویسه باشد"}else if(vm.repo.db.civicDao().usernameCount(username)>0){message="این نام کاربری تکراری است"}else{vm.repo.db.civicDao().saveProfile(UserProfileEntity(username=username,displayName=name,imagePath=imagePath,remoteId=profile?.remoteId,syncPending=true));showSavedMessage(context,"پروفایل");nav.popBackStack()}}},enabled=username.isNotBlank(),modifier=Modifier.fillMaxWidth()){Text("ذخیره پروفایل")};message?.let{Text(it,color=MaterialTheme.colorScheme.primary)}}
}
