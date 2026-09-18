import QtQuick
import QtQuick.Layouts

Rectangle {
    property string text: ""
    property string target: "dashboard"

    Layout.fillWidth: true
    height: 56
    radius: 16

    color: root.page === target ? "#E8F2FF" : "transparent"

    Text {
        anchors.centerIn: parent
        text: parent.parent.text
        color: root.theme === "light" ? "#253858" : "#E6EDF3"
        font.pixelSize: 16
    }

    MouseArea {
        anchors.fill: parent

        onClicked: {
            root.page = target
            root.drawerOpen = false
        }
    }
}
