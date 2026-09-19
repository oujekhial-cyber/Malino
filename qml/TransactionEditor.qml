import QtQuick
import QtQuick.Controls
import QtQuick.Layouts

ColumnLayout {
    id: root

    signal saved()
    property var editing: null

    property string theme: appController.theme
    property string currency: appController.currency
    property color fg: theme === "light" ? "#102A43" : "#F5F8FF"

    spacing: 10
    layoutDirection: Qt.RightToLeft

    Text {
        text: "مبلغ (" + root.currency + ")"
        Layout.fillWidth: true
        horizontalAlignment: Text.AlignRight
        color: root.fg
    }

    TextField {
        id: amount

        Layout.fillWidth: true
        placeholderText: "۰"
        inputMethodHints: Qt.ImhDigitsOnly
        horizontalAlignment: Text.AlignRight

        onTextChanged: {
            var r = text.replace(/[^0-9]/g, "")
            var o = ""

            while (r.length > 3) {
                o = "٬" + r.slice(-3) + o
                r = r.slice(0, -3)
            }

            o = r + o

            if (o !== text) {
                var p = o.length
                text = o
                cursorPosition = p
            }
        }
    }

    ComboBox {
        id: bank

        Layout.fillWidth: true
        model: appController.banks()
    }

    Button {
        text: "+ افزودن بانک جدید"
        Layout.fillWidth: true

        onClicked: bankPopup.open()
    }

    ComboBox {
        id: typ
        Layout.fillWidth: true
        model: ["واریز", "برداشت"]
    }

    TextField {
        id: desc

        Layout.fillWidth: true
        placeholderText: "بابت چه چیزی بود؟"
        horizontalAlignment: Text.AlignRight
    }

    RowLayout {
        Layout.fillWidth: true

        Button {
            text: "انصراف"

            onClicked: root.saved()
        }

        Button {
            text: "ذخیره"
            Layout.fillWidth: true

            onClicked: {
                var n = parseInt(amount.text.replace(/[^0-9]/g, "")) || 0
                var t = typ.currentIndex === 0 ? "IN" : "OUT"

                if (root.editing) {
                    appController.updateTransaction(
                        root.editing.id,
                        n,
                        t,
                        bank.currentText,
                        desc.text
                    )
                } else {
                    appController.addTransaction(
                        n,
                        t,
                        bank.currentText,
                        desc.text
                    )
                }

                root.saved()
            }
        }
    }

    Component.onCompleted: root.loadEdit()

    onEditingChanged: root.loadEdit()

    function loadEdit() {
        if (!root.editing)
            return

        amount.text = String(root.editing.amount)
        typ.currentIndex = root.editing.tx_type === "IN" ? 0 : 1
        desc.text = root.editing.description || ""

        var i = bank.model.indexOf(root.editing.bank)

        if (i >= 0)
            bank.currentIndex = i
    }

    Popup {
        id: bankPopup

        modal: true
        width: parent.width
        anchors.centerIn: Overlay.overlay

        contentItem: ColumnLayout {
            spacing: 8

            Text {
                text: "بانک جدید"
                font.bold: true
                Layout.fillWidth: true
                horizontalAlignment: Text.AlignRight
            }

            TextField {
                id: newBank

                placeholderText: "نام بانک"
                horizontalAlignment: Text.AlignRight
            }

            Button {
                text: "افزودن"
                Layout.fillWidth: true

                onClicked: {
                    if (newBank.text.trim().length) {
                        appController.addBank(newBank.text.trim())

                        bank.model = appController.banks()
                        bank.currentIndex =
                            bank.model.indexOf(newBank.text.trim())

                        newBank.text = ""
                        bankPopup.close()
                    }
                }
            }
        }
    }
}
