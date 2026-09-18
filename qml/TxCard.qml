import QtQuick
import QtQuick.Layouts

Rectangle {
    property var tx: modelData

    width: parent ? parent.width : 360
    height: 88
    radius: 20
    color: root.card

    RowLayout {
        anchors.fill: parent
        anchors.margins: 13
        layoutDirection: Qt.RightToLeft

        ColumnLayout {
            Layout.fillWidth: true

            Text {
                text: tx.bank || "بانک دیگر"
                font.bold: true
                color: root.fg
                Layout.fillWidth: true
                horizontalAlignment: Text.AlignRight
            }

            Text {
                text: tx.description || "بدون توضیح"
                color: root.muted
                Layout.fillWidth: true
                horizontalAlignment: Text.AlignRight
            }
        }

        Text {
            text: root.money(tx.amount)
            font.bold: true
            color: tx.tx_type === "IN" ? root.green : root.red
        }
    }
}
