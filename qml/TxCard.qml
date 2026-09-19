import QtQuick
import QtQuick.Layouts

Rectangle {
    id: root

    property var tx: null
    property string theme: appController ? appController.theme : "light"
    property string currency: appController ? appController.currency : "تومان"
    property color card: theme === "light" ? "#FFFFFF" : theme === "midnight" ? "#182335" : "#101D31"
    property color fg: theme === "light" ? "#102A43" : "#F5F8FF"
    property color muted: "#718096"
    property color green: "#16A884"
    property color red: "#E55368"

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

    width: parent ? parent.width : 360
    height: 88
    radius: 20
    color: root.card
    border.color: theme === "light" ? "#E2E8F0" : "#2D3748"
    border.width: 1
    visible: root.tx !== null

    RowLayout {
        anchors.fill: parent
        anchors.margins: 13
        layoutDirection: Qt.RightToLeft

        ColumnLayout {
            Layout.fillWidth: true
            spacing: 2

            Text {
                text: root.tx ? (root.tx.bank || "بانک دیگر") : ""
                font.bold: true
                color: root.fg
                Layout.fillWidth: true
                horizontalAlignment: Text.AlignRight
            }

            Text {
                text: root.tx ? (root.tx.description || "بدون توضیح") : ""
                color: root.muted
                Layout.fillWidth: true
                horizontalAlignment: Text.AlignRight
                elide: Text.ElideRight
            }
        }

        Text {
            text: root.tx ? root.money(root.tx.amount) : ""
            font.bold: true
            color: root.tx && root.tx.tx_type === "IN" ? root.green : root.red
        }
    }
}
