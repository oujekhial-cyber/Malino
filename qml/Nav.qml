import QtQuick
import QtQuick.Layouts

Rectangle {
    id: root

    property string text: ""
    property string target: "dashboard"
    property string current: "dashboard"
    signal navigate(string target)

    Layout.fillWidth: true
    height: 56
    radius: 16

    color: root.current === root.target ? "#E8F2FF" : "transparent"

    Text {
        anchors.centerIn: parent
        text: root.text
        color: appController.theme === "light" ? "#253858" : "#E6EDF3"
        font.pixelSize: 16
    }

    MouseArea {
        anchors.fill: parent

        onClicked: root.navigate(root.target)
    }
}
