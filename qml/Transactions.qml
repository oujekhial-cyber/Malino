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
    property color green: "#16A884"
    property color red: "#E55368"

    property bool dialogOpen: false
    property var editing: null

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

    ColumnLayout {
        anchors.fill: parent
        anchors.margins: 18
        layoutDirection: Qt.RightToLeft
        spacing: 12

        RowLayout {
            Layout.fillWidth: true
            layoutDirection: Qt.RightToLeft
            spacing: 10

            Text {
                text: "تراکنش‌ها"
                font.pixelSize: 26
                font.bold: true
                color: root.fg
                Layout.fillWidth: true
                horizontalAlignment: Text.AlignRight
            }

            Button {
                text: "+ ثبت تراکنش"
                onClicked: {
                    root.editing = null
                    root.dialogOpen = true
                }
            }
        }

        Text {
            text: "لیست تراکنش‌های ثبت شده با قابلیت ویرایش و حذف"
            color: root.muted
            Layout.fillWidth: true
            horizontalAlignment: Text.AlignRight
            wrapMode: Text.WordWrap
        }

        ListView {
            id: txList
            Layout.fillWidth: true
            Layout.fillHeight: true
            model: appController ? appController.transactions() : []
            clip: true
            spacing: 9
            delegate: SwipeTx {}

            // Empty state
            Text {
                anchors.centerIn: parent
                visible: txList.count === 0
                text: "هنوز تراکنشی ثبت نشده است."
                color: root.muted
                font.pixelSize: 15
            }
        }
    }

    Connections {
        target: appController
        function onChanged() {
            if (txList) {
                txList.model = appController.transactions()
            }
        }
    }

    // Dialog for new transaction - use Popup overlay
    Popup {
        id: newTxPopup
        modal: true
        visible: root.dialogOpen
        width: Math.min(parent.width - 24, 400)
        height: Math.min(parent.height - 100, 520)
        anchors.centerIn: Overlay.overlay
        closePolicy: Popup.CloseOnEscape | Popup.CloseOnPressOutside

        background: Rectangle {
            radius: 22
            color: root.card
            border.color: root.theme === "light" ? "#E2E8F0" : "#2D3748"
            border.width: 1
        }

        contentItem: TransactionEditor {
            editing: root.editing
            onSaved: {
                root.dialogOpen = false
                newTxPopup.close()
            }
        }

        onClosed: root.dialogOpen = false
    }

    // Also keep TransactionForm compatibility (Dialog based)
    TransactionForm {
        id: legacyForm
        visible: false
    }
}
