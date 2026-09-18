import QtQuick
import QtQuick.Layouts
Rectangle { property string title:""; property color accent:"#1677FF"; Layout.fillWidth:true; height:100; radius:20; color:root.card
    Text { anchors.centerIn:parent; text:title; color:accent; font.pixelSize:16; font.bold:true; horizontalAlignment:Text.AlignHCenter }
}
