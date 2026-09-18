import QtQuick
import QtQuick.Controls
import QtQuick.Layouts
ColumnLayout { signal saved(); property var editing:null; spacing:10; layoutDirection:Qt.RightToLeft
    Text{text:"مبلغ ("+root.currency+")";Layout.fillWidth:true;horizontalAlignment:Text.AlignRight;color:root.fg}
    TextField{id:amount;Layout.fillWidth:true;placeholderText:"۰";inputMethodHints:Qt.ImhDigitsOnly;horizontalAlignment:Text.AlignRight
        onTextChanged:{let r=text.replace(/[^0-9]/g,"");let o="";while(r.length>3){o="٬"+r.slice(-3)+o;r=r.slice(0,-3)}o=r+o;if(o!==text){let p=o.length;text=o;cursorPosition=p}}
    }
    ComboBox{id:bank;Layout.fillWidth:true;model:appController.banks();displayText:currentText}
    Button{text:"+ افزودن بانک جدید";Layout.fillWidth:true;onClicked:{bankPopup.open()}}
    ComboBox{id:typ;Layout.fillWidth:true;model:["واریز","برداشت"]}
    TextField{id:desc;Layout.fillWidth:true;placeholderText:"بابت چه چیزی بود؟";horizontalAlignment:Text.AlignRight}
    RowLayout{Layout.fillWidth:true;Button{text:"انصراف";onClicked:parent.parent.saved()};Button{text:"ذخیره";Layout.fillWidth:true;onClicked:{let n=parseInt(amount.text.replace(/[^0-9]/g,""))||0;let t=typ.currentIndex===0?"IN":"OUT";if(editing)appController.updateTransaction(editing.id,n,t,bank.currentText,desc.text);else appController.addTransaction(n,t,bank.currentText,desc.text);saved()}}}
    Component.onCompleted:loadEdit()
    onEditingChanged:loadEdit()
    function loadEdit(){if(!editing)return;amount.text=String(editing.amount);typ.currentIndex=editing.tx_type==="IN"?0:1;desc.text=editing.description||"";let i=bank.model.indexOf(editing.bank);if(i>=0)bank.currentIndex=i}
    Popup{id:bankPopup;modal:true;width:parent.width;anchors.centerIn:Overlay.overlay;contentItem:ColumnLayout{spacing:8;Text{text:"بانک جدید";font.bold:true;Layout.fillWidth:true;horizontalAlignment:Text.AlignRight};TextField{id:newBank;placeholderText:"نام بانک";horizontalAlignment:Text.AlignRight};Button{text:"افزودن";Layout.fillWidth:true;onClicked:{if(newBank.text.trim().length){appController.addBank(newBank.text.trim());bank.model=appController.banks();bank.currentIndex=bank.model.indexOf(newBank.text.trim());newBank.text="";bankPopup.close()}}}}}
}
