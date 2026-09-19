import QtQuick
import QtQuick.Controls
import QtQuick.Layouts

Rectangle {
    id: root

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

    width: ListView.view ? ListView.view.width : 360
    height: 96
    radius: 20
    color: root.card
    border.color: theme === "light" ? "#E2E8F0" : "#2D3748"
    border.width: 1

    RowLayout {
        anchors.fill: parent
        anchors.margins: 12
        layoutDirection: Qt.RightToLeft

        ColumnLayout {
            Layout.fillWidth: true
            spacing: 2

            Text {
                text: modelData ? (modelData.bank || "بانک دیگر") : ""
                font.bold: true
                color: root.fg
                Layout.fillWidth: true
                horizontalAlignment: Text.AlignRight
            }

            Text {
                text: modelData ? root.money(modelData.amount) : ""
                color: modelData && modelData.tx_type === "IN" ? root.green : root.red
                Layout.fillWidth: true
                horizontalAlignment: Text.AlignRight
            }

            Text {
                text: modelData ? (modelData.description || "بدون توضیح") : ""
                color: root.muted
                Layout.fillWidth: true
                horizontalAlignment: Text.AlignRight
                elide: Text.ElideRight
            }
        }

        Button {
            text: "ویرایش"

            onClicked: {
                editDialog.editing = modelData
                editDialog.open()
            }
        }

        Button {
            text: "حذف"
            onClicked: {
                if (modelData && modelData.id !== undefined) {
                    appController.deleteTransaction(modelData.id)
                }
            }
        }
    }

    Dialog {
        id: editDialog

        property var editing: null

        modal: true
        width: Math.min(root.width - 24, 390)
        title: "ویرایش تراکنش"
        anchors.centerIn: Overlay.overlay

        background: Rectangle {
            radius: 18
            color: root.card
        }

        contentItem: TransactionEditor {
            editing: editDialog.editing

            onSaved: editDialog.close()
        }
    }
}
