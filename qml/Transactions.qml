import QtQuick
import QtQuick.Controls
import QtQuick.Layouts
Item{property bool dialogOpen:false;property var editing:null;ColumnLayout{anchors.fill:parent;anchors.margins:18;layoutDirection:Qt.RightToLeft;RowLayout{Layout.fillWidth:true;Text{text:"تراکنش‌ها";font.pixelSize:26;font.bold:true;Layout.fillWidth:true;horizontalAlignment:Text.AlignRight};Button{text:"+ ثبت تراکنش";onClicked:{editing=null;dialogOpen=true}}};ListView{id:tx;Layout.fillWidth:true;Layout.fillHeight:true;model:appController.transactions();clip:true;spacing:9;delegate:SwipeTx{}}}Connections{target:appController;function onChanged(){tx.model=appController.transactions()}}TransactionForm{visible:dialogOpen;editing:editing;onClosed:dialogOpen=false}}
