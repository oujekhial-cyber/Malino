import QtQuick
import QtQuick.Controls
import QtQuick.Layouts

Item {
    id: root

    property string theme: appController ? appController.theme : "light"
    property string currency: appController ? appController.currency : "تومان"
    property color card: theme === "light" ? "#FFFFFF" : theme === "midnight" ? "#182335" : "#101D31"
    property color fg: theme === "light" ? "#102A43" : "#F5F8FF"
    property color muted: "#718096"

    Flickable {
        anchors.fill: parent
        contentWidth: width
        contentHeight: col.implicitHeight + 36
        clip: true

        ColumnLayout {
            id: col
            width: parent.width
            anchors.left: parent.left
            anchors.right: parent.right
            anchors.margins: 18

            layoutDirection: Qt.RightToLeft
            spacing: 14

            Text {
                text: "تنظیمات"
                font.pixelSize: 26
                font.bold: true
                color: root.fg
                Layout.fillWidth: true
                horizontalAlignment: Text.AlignRight
            }

            Rectangle {
                Layout.fillWidth: true
                height: 125
                radius: 22
                color: root.card
                border.color: root.theme === "light" ? "#E2E8F0" : "#2D3748"
                border.width: 1

                ColumnLayout {
                    anchors.fill: parent
                    anchors.margins: 16

                    Text {
                        text: "واحد پول"
                        font.pixelSize: 18
                        font.bold: true
                        color: root.fg
                        Layout.fillWidth: true
                        horizontalAlignment: Text.AlignRight
                    }

                    RowLayout {
                        Layout.fillWidth: true

                        Button {
                            text: "تومان"
                            checkable: true
                            checked: root.currency === "تومان"

                            onClicked: appController.setCurrency("تومان")
                        }

                        Button {
                            text: "ریال"
                            checkable: true
                            checked: root.currency === "ریال"

                            onClicked: appController.setCurrency("ریال")
                        }
                    }
                }
            }

            Rectangle {
                Layout.fillWidth: true
                height: 160
                radius: 22
                color: root.card
                border.color: root.theme === "light" ? "#E2E8F0" : "#2D3748"
                border.width: 1

                ColumnLayout {
                    anchors.fill: parent
                    anchors.margins: 16

                    Text {
                        text: "تم برنامه"
                        font.pixelSize: 18
                        font.bold: true
                        color: root.fg
                        Layout.fillWidth: true
                        horizontalAlignment: Text.AlignRight
                    }

                    RowLayout {
                        Layout.fillWidth: true

                        Button {
                            text: "روشن"
                            onClicked: appController.setTheme("light")
                        }

                        Button {
                            text: "نیمه‌شب"
                            onClicked: appController.setTheme("midnight")
                        }

                        Button {
                            text: "نئونی"
                            onClicked: appController.setTheme("neon")
                        }
                    }

                    Text {
                        text: "چند تم سبک و قابل تعویض"
                        color: root.muted
                        Layout.fillWidth: true
                        horizontalAlignment: Text.AlignRight
                    }
                }
            }
        }
    }
}
