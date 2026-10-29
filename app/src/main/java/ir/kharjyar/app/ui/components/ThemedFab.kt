package ir.kharjyar.app.ui.components

import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import ir.kharjyar.app.ui.theme.LocalAppSkin

@Composable
private fun themedFabColors():Pair<Color,Color>{
 val skin=LocalAppSkin.current
 val background=skin.fabGradient.firstOrNull()?:skin.accent
 val foreground=if(background.luminance()>.58f)Color(0xFF17121B) else Color.White
 return background to foreground
}

/** دکمه افزودن با رنگ اختصاصی هر پوسته، به‌جای رنگ ثابت Material. */
@Composable
fun ThemedFloatingActionButton(onClick:()->Unit,modifier:Modifier=Modifier,content:@Composable ()->Unit){
 val (background,foreground)=themedFabColors()
 FloatingActionButton(onClick=onClick,modifier=modifier,containerColor=background,contentColor=foreground,content=content)
}

@Composable
fun ThemedExtendedFloatingActionButton(onClick:()->Unit,modifier:Modifier=Modifier,icon:@Composable ()->Unit,text:@Composable ()->Unit){
 val (background,foreground)=themedFabColors()
 ExtendedFloatingActionButton(onClick=onClick,modifier=modifier,containerColor=background,contentColor=foreground,icon=icon,text=text)
}
