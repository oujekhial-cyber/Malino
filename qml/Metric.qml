import QtQuick
import QtQuick.Layouts
Rectangle {
    id: root
    property string title: ""
    property color accent: "#1677FF"
    property string theme: appController.theme
    Layout.fillWidth: true
    height: 100
    radius: 20
    color: root.theme === "light" ? "#FFFFFF" : root.theme === "midnight" ? "#182335" : "#101D31"
    Text {
        anchors.centerIn: parent
        text: root.title
        color: root.accent
        font.pixelSize: 16
        font.bold: true
        horizontalAlignment: Text.AlignHCenter
    }
}
