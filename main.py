import sys
from pathlib import Path

from PySide6.QtCore import QUrl, qWarning
from PySide6.QtGui import QGuiApplication, QIcon
from PySide6.QtQml import QQmlApplicationEngine

try:
    from PySide6.QtQuickControls2 import QQuickStyle
except ImportError:
    QQuickStyle = None

from core.controller import AppController

ROOT = Path(__file__).resolve().parent


def main():
    # Pin the Controls style so desktop and Android look identical
    # (on Android "Basic" is the default anyway).
    if QQuickStyle is not None:
        QQuickStyle.setStyle("Basic")

    app = QGuiApplication(sys.argv)
    app.setApplicationName("Malino")
    app.setApplicationDisplayName("مالینو")
    icon = ROOT / "assets" / "malino_icon.svg"
    if icon.exists():
        app.setWindowIcon(QIcon(str(icon)))

    controller = AppController(ROOT)

    engine = QQmlApplicationEngine()
    engine.rootContext().setContextProperty("appController", controller)

    # Surface every QML problem in the logs (visible through adb logcat on
    # Android instead of dying silently with a black screen).
    engine.warnings.connect(
        lambda warnings: [qWarning(w.toString()) for w in warnings]
    )

    def on_creation_failed(errors):
        for error in errors:
            qWarning("[Malino] QML error: " + error.toString())

    try:
        engine.objectCreationFailed.connect(on_creation_failed)
    except AttributeError:
        pass

    main_qml = ROOT / "qml" / "Main.qml"
    engine.load(QUrl.fromLocalFile(str(main_qml)))

    if not engine.rootObjects():
        qWarning("[Malino] Failed to load " + str(main_qml))
        return 1

    rc = app.exec()

    # Tear down in a safe order: the engine (which still references
    # appController in its root context) must die before the controller.
    del engine
    del controller
    return rc


if __name__ == "__main__":
    sys.exit(main())
