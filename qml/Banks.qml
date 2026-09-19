import QtQuick
import QtQuick.Controls
import QtQuick.Layouts
Item {
    id: root
    property string theme: appController.theme
    property color card: theme === "light" ? "#FFFFFF" : theme === "midnight" ? "#182335" : "#101D31"
    property color fg: theme === "light" ? "#102A43" : "#F5F8FF"
    property color muted: "#718096"
ColumnLayout{anchors.fill:parent;anchors.margins:18;layoutDirection:Qt.RightToLeft;spacing:12
    Text{text:"بانک‌ها و پیامک";font.pixelSize:26;font.bold:true;color:root.fg;Layout.fillWidth:true;horizontalAlignment:Text.AlignRight}
    Text{text:"نمونه پیامک را وارد کن تا الگوی مبلغ و موجودی ذخیره شود.";color:root.muted;Layout.fillWidth:true;horizontalAlignment:Text.AlignRight;wrapMode:Text.WordWrap}
    TextField{id:bname;placeholderText:"نام بانک";Layout.fillWidth:true;horizontalAlignment:Text.AlignRight}
    TextArea{id:sms;placeholderText:"آخرین پیامک بانک را اینجا بگذار...";Layout.fillWidth:true;height:140;wrapMode:TextArea.Wrap;horizontalAlignment:Text.AlignRight}
    Button{text:"ذخیره بانک و یادگیری قالب";Layout.fillWidth:true;onClicked:{appController.learnBank(bname.text,sms.text);bname.text="";sms.text=""}}
    Text{text:"بانک‌های شناخته‌شده";font.pixelSize:19;font.bold:true;color:root.fg;Layout.fillWidth:true;horizontalAlignment:Text.AlignRight}
    ListView{Layout.fillWidth:true;Layout.fillHeight:true;model:appController.banks();clip:true;spacing:6;delegate:Rectangle{width:ListView.view.width;height:54;radius:15;color:root.card;Text{anchors.centerIn:parent;text:modelData;color:root.fg}}}
}}
