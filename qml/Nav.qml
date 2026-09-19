import QtQuick
import QtQuick.Layouts

Rectangle {
    id: root

    property string text: ""
    property string target: "dashboard"
    property string current: "dashboard"
    signal navigate(string target)

    property string theme: appController ? appController.theme : "light"

    Layout.fillWidth: true
    height: 56
    radius: 16

    color: root.current === root.target ? (root.theme === "light" ? "#E8F2FF" : "#1E3A5F") : "transparent"

    Text {
        anchors.centerIn: parent
        text: root.text
        color: (appController ? appController.theme : "light") === "light" ? "#253858" : "#E6EDF3"
        font.pixelSize: 16
    }

    MouseArea {
        anchors.fill: parent

        onClicked: root.navigate(root.target)
    }
}
