import QtQuick
import QtQuick.Controls
import QtQuick.Layouts

Item {
    id: root

    property string theme: appController.theme
    property string currency: appController.currency
    property color card: theme === "light" ? "#FFFFFF" : theme === "midnight" ? "#182335" : "#101D31"
    property color fg: theme === "light" ? "#102A43" : "#F5F8FF"
    property color muted: "#718096"
    property color green: "#16A884"
    property color red: "#E55368"
    property var s: ({})
    property var recentTxs: []

    function money(n) {
        var v = Math.round(Number(n) || 0)
        var sign = v < 0 ? "−" : ""
        var s = String(Math.abs(v))
        var o = ""

        while (s.length > 3) {
            o = "٬" + s.slice(-3) + o
            s = s.slice(0, -3)
        }

        return sign + s + o + " " + root.currency
    }

    function refresh() {
        s = appController.stats()
        var all = appController.transactions()
        recentTxs = all.slice(0, Math.min(5, all.length))
    }

    Component.onCompleted: refresh()

    Connections {
        target: appController

        function onChanged() {
            root.refresh()
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
                        text: root.money(root.s.net)
                        font.pixelSize: 32
                        font.bold: true
                        color: root.s.net >= 0 ? root.green : root.red
                        Layout.fillWidth: true
                        horizontalAlignment: Text.AlignRight
                    }

                    Text {
                        text: root.s.count + " تراکنش ثبت شده"
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
                    Layout.fillWidth: true
                    title: "واریزی کل\n" + root.money(root.s.totalIn)
                    accent: root.green
                }

                Metric {
                    Layout.fillWidth: true
                    title: "برداشت کل\n" + root.money(root.s.totalOut)
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
                        text: "ورودی: " + root.money(root.s.monthIn)
                        color: root.green
                        Layout.fillWidth: true
                        horizontalAlignment: Text.AlignRight
                    }

                    Text {
                        text: "خروجی: " + root.money(root.s.monthOut)
                        color: root.red
                        Layout.fillWidth: true
                        horizontalAlignment: Text.AlignRight
                    }

                    Text {
                        text: "خالص ماه: " + root.money(root.s.monthNet)
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
                model: root.recentTxs.length

                delegate: TxCard {
                    width: col.width
                    tx: root.recentTxs[index]
                }
            }
        }
    }
}
