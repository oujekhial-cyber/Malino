import QtQuick
import QtQuick.Controls
import QtQuick.Layouts

Item {
    id: root

    property string theme: appController ? appController.theme : "light"
    property color card: theme === "light" ? "#FFFFFF" : theme === "midnight" ? "#182335" : "#101D31"
    property color fg: theme === "light" ? "#102A43" : "#F5F8FF"
    property color muted: "#718096"
    property color blue: "#1677FF"

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
            spacing: 12

            Text {
                text: "بانک‌ها و پیامک"
                font.pixelSize: 26
                font.bold: true
                color: root.fg
                Layout.fillWidth: true
                horizontalAlignment: Text.AlignRight
            }

            Text {
                text: "نمونه پیامک را وارد کن تا الگوی مبلغ و موجودی ذخیره شود."
                color: root.muted
                Layout.fillWidth: true
                horizontalAlignment: Text.AlignRight
                wrapMode: Text.WordWrap
            }

            Rectangle {
                Layout.fillWidth: true
                height: bankForm.implicitHeight + 24
                radius: 18
                color: root.card
                border.color: root.theme === "light" ? "#E2E8F0" : "#2D3748"
                border.width: 1

                ColumnLayout {
                    id: bankForm
                    anchors.left: parent.left
                    anchors.right: parent.right
                    anchors.top: parent.top
                    anchors.margins: 12
                    spacing: 8
                    layoutDirection: Qt.RightToLeft

                    TextField {
                        id: bname
                        placeholderText: "نام بانک"
                        Layout.fillWidth: true
                        horizontalAlignment: Text.AlignRight
                    }

                    TextArea {
                        id: sms
                        placeholderText: "آخرین پیامک بانک را اینجا بگذار..."
                        Layout.fillWidth: true
                        Layout.preferredHeight: 120
                        wrapMode: TextArea.Wrap
                        horizontalAlignment: Text.AlignRight
                    }

                    Button {
                        text: "ذخیره بانک و یادگیری قالب"
                        Layout.fillWidth: true
                        onClicked: {
                            appController.learnBank(bname.text, sms.text)
                            bname.text = ""
                            sms.text = ""
                        }
                    }
                }
            }

            Text {
                text: "بانک‌های شناخته‌شده"
                font.pixelSize: 19
                font.bold: true
                color: root.fg
                Layout.fillWidth: true
                horizontalAlignment: Text.AlignRight
                Layout.topMargin: 8
            }

            ListView {
                Layout.fillWidth: true
                Layout.preferredHeight: Math.min(400, count * 60)
                model: appController ? appController.banks() : []
                clip: true
                spacing: 6
                interactive: false

                delegate: Rectangle {
                    width: ListView.view.width
                    height: 54
                    radius: 15
                    color: root.card
                    border.color: root.theme === "light" ? "#EDF2F7" : "#2D3748"
                    border.width: 1

                    Text {
                        anchors.centerIn: parent
                        text: modelData
                        color: root.fg
                        font.pixelSize: 14
                    }
                }
            }
        }
    }
}
