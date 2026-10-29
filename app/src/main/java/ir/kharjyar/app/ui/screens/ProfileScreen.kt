package ir.kharjyar.app.ui.screens

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ir.kharjyar.app.core.identity.Username
import ir.kharjyar.app.data.db.UserProfileEntity
import ir.kharjyar.app.ui.AppViewModel
import ir.kharjyar.app.ui.components.HeroCard
import ir.kharjyar.app.ui.components.SkinCard
import ir.kharjyar.app.ui.components.showSavedMessage
import ir.kharjyar.app.ui.theme.LocalAppSkin
import java.io.File
import kotlinx.coroutines.launch

/** حساب محلی کاربر؛ مدل برای اتصال امن به هویت سرور در آینده آماده است. */
@Composable
fun ProfileScreen(vm: AppViewModel, nav: NavHostController) {
    val profile by vm.repo.db.civicDao().observeProfile().collectAsState(initial = null)
    val settings by vm.settings.collectAsState()
    val skin = LocalAppSkin.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var username by remember(profile) { mutableStateOf(profile?.username.orEmpty()) }
    var name by remember(profile) { mutableStateOf(profile?.displayName.orEmpty()) }
    var imagePath by remember(profile) { mutableStateOf(profile?.imagePath.orEmpty()) }
    var message by remember { mutableStateOf<String?>(null) }
    val usernameValid = Username.isValid(username)
    val avatar = imagePath.takeIf(String::isNotBlank)
        ?.let { remember(it) { BitmapFactory.decodeFile(it)?.asImageBitmap() } }

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
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HeroCard(Modifier.fillMaxWidth().height(205.dp), neon = settings.cardShine) {
            Box(Modifier.fillMaxSize().padding(18.dp)) {
                Box(Modifier.size(150.dp).align(Alignment.TopStart).offset(x=(-55).dp,y=(-70).dp)
                    .background(skin.onHero.copy(.06f),CircleShape))
                Column(Modifier.fillMaxSize(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
                    Box(contentAlignment=Alignment.Center) {
                        Box(Modifier.size(100.dp).shadow(14.dp,CircleShape,ambientColor=skin.accent.copy(.35f),spotColor=skin.accent.copy(.28f))
                            .clip(CircleShape).background(Brush.radialGradient(listOf(skin.accent.copy(.36f),skin.cardColor)))
                            .border(2.dp,skin.onHero.copy(.72f),CircleShape),contentAlignment=Alignment.Center) {
                            if(avatar!=null) Image(avatar,null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
                            else Icon(Icons.Filled.Person,null,tint=skin.onHero,modifier=Modifier.size(52.dp))
                        }
                        Surface(onClick={picker.launch("image/*")},modifier=Modifier.align(Alignment.BottomEnd).size(34.dp),shape=CircleShape,color=skin.accent,shadowElevation=6.dp,border=androidx.compose.foundation.BorderStroke(2.dp,skin.cardColor)) {
                            Box(contentAlignment=Alignment.Center){Icon(Icons.Filled.CameraAlt,"انتخاب عکس پروفایل",tint=Color.White,modifier=Modifier.size(17.dp))}
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(name.ifBlank{"حساب کاربری خرج‌یار"},color=skin.onHero,fontWeight=FontWeight.Black,style=MaterialTheme.typography.titleLarge,maxLines=1)
                    Text(if(usernameValid)"@${username}" else "پروفایل شخصی و امن شما",color=skin.onHero.copy(.76f),style=MaterialTheme.typography.bodySmall)
                }
            }
        }

        SkinCard(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(13.dp)) {
                Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(9.dp)) {
                    Box(Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(skin.accent.copy(.14f)),contentAlignment=Alignment.Center){Icon(Icons.Filled.VerifiedUser,null,tint=skin.accent)}
                    Column { Text(if(profile==null)"ساخت حساب کاربری" else "ویرایش حساب کاربری",fontWeight=FontWeight.Black,style=MaterialTheme.typography.titleMedium);Text("اطلاعات شما فقط روی همین دستگاه نگهداری می‌شود",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                HorizontalDivider(color=skin.accent.copy(.16f))
                OutlinedTextField(value=name,onValueChange={name=it},label={Text("نام نمایشی")},leadingIcon={Icon(Icons.Filled.Badge,null)},singleLine=true,shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth())
                // LTR اجباری باعث می‌شود @ از نظر فیزیکی سمت چپ نام انگلیسی بماند.
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    OutlinedTextField(
                        value=username,
                        onValueChange={username=Username.sanitize(it);message=null},
                        label={Text("English username")},prefix={Text("@",fontWeight=FontWeight.Bold)},
                        supportingText={Text(if(username.isNotEmpty()&&!usernameValid)"3–30 characters: a–z, 0–9, _" else "example: ali_1370")},
                        isError=username.isNotEmpty()&&!usernameValid,singleLine=true,
                        textStyle=MaterialTheme.typography.bodyLarge.copy(textDirection=TextDirection.Ltr),
                        keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Ascii,imeAction=ImeAction.Done),
                        shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth()
                    )
                }
                message?.let { Text(it,color=MaterialTheme.colorScheme.error,style=MaterialTheme.typography.bodySmall,textAlign=TextAlign.Center,modifier=Modifier.fillMaxWidth()) }
                Button(
                    onClick={
                        if(!usernameValid){message="نام کاربری باید ۳ تا ۳۰ نویسه و فقط شامل حروف انگلیسی، عدد یا زیرخط باشد";return@Button}
                        scope.launch {
                            if(username!=profile?.username&&vm.repo.db.civicDao().usernameCount(username)>0) message="این نام کاربری تکراری است"
                            else {
                                vm.repo.db.civicDao().saveProfile(UserProfileEntity(username=username,displayName=name.trim(),imagePath=imagePath,remoteId=profile?.remoteId,syncPending=true))
                                showSavedMessage(context, "پروفایل"); nav.popBackStack()
                            }
                        }
                    },enabled=usernameValid,shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth().height(52.dp)
                ){Icon(Icons.Filled.VerifiedUser,null);Spacer(Modifier.width(8.dp));Text(if(profile==null)"ساخت حساب و ورود" else "ذخیره تغییرات",fontWeight=FontWeight.Bold)}
                Text("نام کاربری فقط با حروف انگلیسی ساخته می‌شود و برای قابلیت‌های آنلاین آینده آماده خواهد بود.",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant,textAlign=TextAlign.Center,modifier=Modifier.fillMaxWidth())
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}
