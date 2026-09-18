import QtQuick
import QtQuick.Controls
import QtQuick.Layouts

Page {
    id: page

    background: Rectangle {
        color: "#0c0a11"
    }

    ColumnLayout {
        anchors.fill: parent
        anchors.margins: 20
        spacing: 14

        Label {
            text: "تراکنش‌ها"
            color: "white"
            font.pixelSize: 24
            font.bold: true
            Layout.fillWidth: true
        }

        Label {
            text: "لیست تراکنش‌های ثبت شده"
            color: "#9ca3af"
            font.pixelSize: 14
            Layout.fillWidth: true
        }

        Rectangle {
            Layout.fillWidth: true
            Layout.fillHeight: true
            radius: 18
            color: "#15121d"
            border.color: "#272231"
            border.width: 1

            ColumnLayout {
                anchors.fill: parent
                anchors.margins: 18
                spacing: 12

                Label {
                    text: "تراکنش‌های اخیر"
                    color: "#f3f4f6"
                    font.pixelSize: 18
                    font.bold: true
                }

                Label {
                    text: "هنوز تراکنشی ثبت نشده است."
                    color: "#8b8795"
                    font.pixelSize: 14
                    Layout.fillWidth: true
                    horizontalAlignment: Text.AlignHCenter
                    verticalAlignment: Text.AlignVCenter
                    Layout.fillHeight: true
                }
            }
        }
    }
}
