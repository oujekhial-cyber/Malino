import QtQuick
import QtQuick.Controls
import QtQuick.Layouts

Item {
    property var s: appController.stats()

    function refresh() {
        s = appController.stats()
    }

    Connections {
        target: appController

        function onChanged() {
            refresh()
        }
    }

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

            spacing: 14
            layoutDirection: Qt.RightToLeft

            Text {
                text: "خلاصه مالی"
                font.pixelSize: 28
                font.bold: true
                color: root.fg
                Layout.fillWidth: true
                horizontalAlignment: Text.AlignRight
            }

            Rectangle {
                Layout.fillWidth: true
                height: 178
                radius: 28
                color: root.card

                ColumnLayout {
                    anchors.fill: parent
                    anchors.margins: 22
                    spacing: 8

                    Text {
                        text: "مانده خالص ثبت‌شده"
                        color: root.muted
                        Layout.fillWidth: true
                        horizontalAlignment: Text.AlignRight
                    }

                    Text {
                        text: root.money(s.net)
                        font.pixelSize: 32
                        font.bold: true
                        color: s.net >= 0 ? root.green : root.red
                        Layout.fillWidth: true
                        horizontalAlignment: Text.AlignRight
                    }

                    Text {
                        text: s.count + " تراکنش ثبت شده"
                        color: root.muted
                        Layout.fillWidth: true
                        horizontalAlignment: Text.AlignRight
                    }
                }
            }

            RowLayout {
                Layout.fillWidth: true
                spacing: 10

                Metric {
                    title: "واریزی کل\n" + root.money(s.totalIn)
                    accent: root.green
                }

                Metric {
                    title: "برداشت کل\n" + root.money(s.totalOut)
                    accent: root.red
                }
            }

            Rectangle {
                Layout.fillWidth: true
                height: 142
                radius: 22
                color: root.card

                ColumnLayout {
                    anchors.fill: parent
                    anchors.margins: 16
                    spacing: 6

                    Text {
                        text: "این ماه"
                        font.pixelSize: 18
                        font.bold: true
                        color: root.fg
                        Layout.fillWidth: true
                        horizontalAlignment: Text.AlignRight
                    }

                    Text {
                        text: "ورودی: " + root.money(s.monthIn)
                        color: root.green
                        Layout.fillWidth: true
                        horizontalAlignment: Text.AlignRight
                    }

                    Text {
                        text: "خروجی: " + root.money(s.monthOut)
                        color: root.red
                        Layout.fillWidth: true
                        horizontalAlignment: Text.AlignRight
                    }

                    Text {
                        text: "خالص ماه: " + root.money(s.monthNet)
                        font.bold: true
                        color: root.fg
                        Layout.fillWidth: true
                        horizontalAlignment: Text.AlignRight
                    }
                }
            }

            Text {
                text: "آخرین تراکنش‌ها"
                font.pixelSize: 20
                font.bold: true
                color: root.fg
                Layout.fillWidth: true
                horizontalAlignment: Text.AlignRight
            }

            Repeater {
                model: Math.min(5, appController.transactions().length)

                delegate: TxCard {
                    width: col.width
                    tx: appController.transactions()[index]
                }
            }
        }
    }
}
