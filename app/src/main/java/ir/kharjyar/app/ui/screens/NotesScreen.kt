package ir.kharjyar.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.kharjyar.app.data.db.NoteEntity
import ir.kharjyar.app.ui.AppViewModel
import kotlinx.coroutines.launch

@Composable fun NotesScreen(vm:AppViewModel){val notes by vm.notes.collectAsState();val scope=rememberCoroutineScope();var editing by remember{mutableStateOf<NoteEntity?>(null)};var title by remember{mutableStateOf("")};var body by remember{mutableStateOf("")};var pinned by remember{mutableStateOf(false)}
 fun clear(){editing=null;title="";body="";pinned=false}
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text("یادداشت‌ها",style=MaterialTheme.typography.headlineSmall);OutlinedTextField(title,{title=it},label={Text("عنوان")},modifier=Modifier.fillMaxWidth());OutlinedTextField(body,{body=it},label={Text("متن یادداشت")},minLines=4,modifier=Modifier.fillMaxWidth());Row{Checkbox(pinned,{pinned=it});Text("سنجاق در بالای فهرست")};Button({scope.launch{val now=System.currentTimeMillis();val old=editing;if(old==null)vm.repo.db.noteDao().insert(NoteEntity(title=title,body=body,pinned=pinned,createdAt=now,updatedAt=now))else vm.repo.db.noteDao().update(old.copy(title=title,body=body,pinned=pinned,updatedAt=now));clear()}},enabled=title.isNotBlank()||body.isNotBlank(),modifier=Modifier.fillMaxWidth()){Text(if(editing==null)"ذخیره یادداشت" else "ذخیره تغییرات")};HorizontalDivider();notes.forEach{n->Card(Modifier.fillMaxWidth().clickable{editing=n;title=n.title;body=n.body;pinned=n.pinned}){Column(Modifier.padding(14.dp)){Text((if(n.pinned)"📌 " else "")+n.title,style=MaterialTheme.typography.titleMedium);Text(n.body,maxLines=5);TextButton({scope.launch{vm.repo.db.noteDao().delete(n.id)}}){Text("حذف",color=MaterialTheme.colorScheme.error)}}}}}
}
