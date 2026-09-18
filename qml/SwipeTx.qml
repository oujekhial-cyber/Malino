import QtQuick
import QtQuick.Controls
import QtQuick.Layouts

Rectangle {
    width: ListView.view.width
    height: 96
    radius: 20
    color: root.theme === "light" ? "#FFFFFF" : "#182335"

    RowLayout {
        anchors.fill: parent
        anchors.margins: 12
        layoutDirection: Qt.RightToLeft

        ColumnLayout {
            Layout.fillWidth: true

            Text {
                text: modelData.bank
                font.bold: true
                Layout.fillWidth: true
                horizontalAlignment: Text.AlignRight
            }

            Text {
                text: root.money(modelData.amount)
                color: modelData.tx_type === "IN" ? "#16A884" : "#E55368"
                Layout.fillWidth: true
                horizontalAlignment: Text.AlignRight
            }

            Text {
                text: modelData.description || "بدون توضیح"
                color: "#718096"
                Layout.fillWidth: true
                horizontalAlignment: Text.AlignRight
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
            onClicked: appController.deleteTransaction(modelData.id)
        }
    }

    Dialog {
        id: editDialog

        property var editing: null

        modal: true
        width: Math.min(root.width - 24, 390)
        title: "ویرایش تراکنش"

        contentItem: TransactionEditor {
            editing: editDialog.editing

            onSaved: editDialog.close()
        }
    }
}
