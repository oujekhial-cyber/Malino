import QtQuick
import QtQuick.Window 2.15
import QtQuick.Controls
import QtQuick.Layouts

ApplicationWindow {
    id: root
    visible: true
    width: 430
    height: 900
    minimumWidth: 360
    minimumHeight: 640
    title: "مالینو"
    // On Android, ensure window is visible and not hidden behind splash
    visibility: Window.AutomaticVisibility
    color: bg

    property string page: "dashboard"
    property bool drawerOpen: false
    // Fallback to defaults if controller not yet ready (prevents black screen from undefined bindings)
    property string currency: appController ? appController.currency : "تومان"
    property string theme: appController ? appController.theme : "light"

    property color bg: theme === "light" ? "#F4F8FC" : theme === "midnight" ? "#0D1117" : "#09111F"
    property color card: theme === "light" ? "#FFFFFF" : theme === "midnight" ? "#182335" : "#101D31"
    property color fg: theme === "light" ? "#102A43" : "#F5F8FF"
    property color muted: "#718096"
    property color blue: "#1677FF"
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

        return sign + s + o + " " + currency
    }

    Component.onCompleted: {
        console.log("[Malino] Main.qml completed, theme=" + theme + " currency=" + currency)
    }

    Connections {
        target: appController

        function onCurrencyChanged() {
            root.currency = appController.currency
        }

        function onThemeChanged() {
            root.theme = appController.theme
        }

        function onToast(m) {
            toastText.text = m
            toastTimer.restart()
        }
    }

    // Main content with solid background to avoid black screen
    Rectangle {
        anchors.fill: parent
        color: root.bg

        ColumnLayout {
            anchors.fill: parent
            spacing: 0

            Rectangle {
                Layout.fillWidth: true
                height: 78
                color: root.card

                RowLayout {
                    anchors.fill: parent
                    anchors.margins: 16
                    layoutDirection: Qt.RightToLeft

                    ToolButton {
                        text: "☰"
                        font.pixelSize: 25

                        onClicked: root.drawerOpen = true
                    }

                    ColumnLayout {
                        Layout.fillWidth: true

                        Text {
                            text: "مالینو"
                            font.pixelSize: 24
                            font.bold: true
                            color: root.fg
                            Layout.fillWidth: true
                            horizontalAlignment: Text.AlignRight
                        }

                        Text {
                            text: root.page === "dashboard"
                                  ? "داشبورد مالی"
                                  : root.page === "transactions"
                                  ? "تراکنش‌ها"
                                  : root.page === "banks"
                                  ? "بانک‌ها و پیامک"
                                  : "تنظیمات"

                            color: root.muted
                            Layout.fillWidth: true
                            horizontalAlignment: Text.AlignRight
                        }
                    }
                }
            }

            Loader {
                id: pageLoader
                Layout.fillWidth: true
                Layout.fillHeight: true
                clip: true
                asynchronous: false

                source: root.page === "dashboard"
                        ? "Dashboard.qml"
                        : root.page === "transactions"
                        ? "Transactions.qml"
                        : root.page === "banks"
                        ? "Banks.qml"
                        : "Settings.qml"

                onStatusChanged: {
                    if (status === Loader.Error) {
                        console.warn("[Malino] Loader error loading " + source)
                    }
                }
            }
        }
    }

    Rectangle {
        visible: root.drawerOpen
        anchors.fill: parent
        color: "#00000066"
        z: 10

        MouseArea {
            anchors.fill: parent
            onClicked: root.drawerOpen = false
        }

        Rectangle {
            anchors.top: parent.top
            anchors.bottom: parent.bottom
            anchors.right: parent.right
            width: 300
            color: root.card

            ColumnLayout {
                anchors.fill: parent
                anchors.margins: 20
                layoutDirection: Qt.RightToLeft

                Text {
                    text: "مالینو"
                    font.pixelSize: 32
                    font.bold: true
                    color: root.fg
                    Layout.fillWidth: true
                    horizontalAlignment: Text.AlignRight
                }

                Text {
                    text: "مدیریت هوشمند پول"
                    color: root.muted
                    Layout.fillWidth: true
                    horizontalAlignment: Text.AlignRight
                }

                Item {
                    Layout.preferredHeight: 15
                }

                Nav {
                    text: "داشبورد"
                    target: "dashboard"
                    current: root.page

                    onNavigate: (target) => {
                        root.page = target
                        root.drawerOpen = false
                    }
                }

                Nav {
                    text: "تراکنش‌ها"
                    target: "transactions"
                    current: root.page

                    onNavigate: (target) => {
                        root.page = target
                        root.drawerOpen = false
                    }
                }

                Nav {
                    text: "بانک‌ها و پیامک"
                    target: "banks"
                    current: root.page

                    onNavigate: (target) => {
                        root.page = target
                        root.drawerOpen = false
                    }
                }

                Nav {
                    text: "تنظیمات"
                    target: "settings"
                    current: root.page

                    onNavigate: (target) => {
                        root.page = target
                        root.drawerOpen = false
                    }
                }

                Item {
                    Layout.fillHeight: true
                }

                Text {
                    text: "مالینو • نسخه 1.1"
                    color: root.muted
                    Layout.alignment: Qt.AlignHCenter
                }
            }
        }
    }

    Rectangle {
        id: pending

        anchors.bottom: parent.bottom
        anchors.horizontalCenter: parent.horizontalCenter

        width: parent.width - 28
        height: 188
        radius: 26

        color: root.card
        visible: appController ? appController.hasPending : false
        z: 30
        border.color: root.theme === "light" ? "#E2E8F0" : "#2D3748"
        border.width: 1

        ColumnLayout {
            anchors.fill: parent
            anchors.margins: 18
            layoutDirection: Qt.RightToLeft
            spacing: 8

            Text {
                text: "پیامک بانکی جدید"
                font.pixelSize: 20
                font.bold: true
                color: root.fg
                Layout.fillWidth: true
                horizontalAlignment: Text.AlignRight
            }

            Text {
                text: "یک تراکنش از بانک شناسایی شد"
                color: root.muted
                Layout.fillWidth: true
                horizontalAlignment: Text.AlignRight
            }

            TextField {
                id: pendingDesc
                placeholderText: "برای چی بود؟"
                Layout.fillWidth: true
                horizontalAlignment: Text.AlignRight
            }

            RowLayout {
                Layout.fillWidth: true

                Button {
                    text: "نادیده گرفتن"
                    onClicked: appController.clearPending()
                }

                Button {
                    text: "ثبت تراکنش"
                    Layout.fillWidth: true

                    onClicked: {
                        appController.savePending(pendingDesc.text)
                    }
                }
            }
        }
    }

    Rectangle {
        anchors.bottom: parent.bottom
        anchors.bottomMargin: 22
        anchors.horizontalCenter: parent.horizontalCenter

        width: 260
        height: 46
        radius: 23

        color: root.theme === "neon" ? "#0EA5FF" : "#102A43"

        visible: toastText.text.length > 0
        z: 50

        Text {
            id: toastText
            anchors.centerIn: parent
            color: "white"
        }

        Timer {
            id: toastTimer
            interval: 2200
            onTriggered: toastText.text = ""
        }
    }
}
