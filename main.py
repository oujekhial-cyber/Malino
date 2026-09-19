import os
import sys
from pathlib import Path

# Force Basic style via env var as well (extra safety for Android)
os.environ.setdefault("QT_QUICK_CONTROLS_STYLE", "Basic")

from PySide6.QtCore import QUrl, qWarning, Qt
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
    # (on Android \"Basic\" is the default anyway).
    if QQuickStyle is not None:
        QQuickStyle.setStyle("Basic")

    # High-DPI and organization for QStandardPaths
    app = QGuiApplication(sys.argv)
    app.setApplicationName("Malino")
    app.setOrganizationName("Malino")
    app.setOrganizationDomain("malino.app")
    app.setApplicationDisplayName("مالینو")

    icon = ROOT / "assets" / "malino_icon.svg"
    if icon.exists():
        app.setWindowIcon(QIcon(str(icon)))
    else:
        png_icon = ROOT / "assets" / "malino_icon.png"
        if png_icon.exists():
            app.setWindowIcon(QIcon(str(png_icon)))

    controller = AppController(ROOT)

    engine = QQmlApplicationEngine()
    # Ensure QML files can be resolved both via relative imports and via import path
    engine.addImportPath(str(ROOT / "qml"))
    engine.addImportPath(str(ROOT))
    engine.rootContext().setContextProperty("appController", controller)

    # Surface every QML problem in the logs (visible through adb logcat on
    # Android instead of dying silently with a black screen).
    def on_warnings(warnings):
        for w in warnings:
            try:
                qWarning(w.toString())
            except Exception:
                qWarning(str(w))

    engine.warnings.connect(on_warnings)

    # NOTE: on Qt 6 this signal emits a single QUrl (not a list) - iterating
    # over it raises TypeError and hides the real QML error (the very error
    # this handler exists to surface on a black screen).
    def on_creation_failed(url):
        try:
            qWarning("[Malino] QML object creation failed at: " + url.toString())
        except Exception:
            qWarning("[Malino] QML object creation failed")

    try:
        engine.objectCreationFailed.connect(on_creation_failed)
    except AttributeError:
        pass

    main_qml = ROOT / "qml" / "Main.qml"
    # Verify file exists before loading to avoid silent black screen
    if not main_qml.exists():
        qWarning(f"[Malino] Main QML not found: {main_qml}")
        return 1

    qWarning(f"[Malino] Loading QML: {main_qml}")
    engine.load(QUrl.fromLocalFile(str(main_qml)))

    if not engine.rootObjects():
        qWarning("[Malino] Failed to load " + str(main_qml) + " - rootObjects empty (black screen guard)")
        # Keep event loop briefly to flush logs on Android
        return 1

    qWarning("[Malino] QML loaded successfully, starting event loop")
    rc = app.exec()

    # Tear down in a safe order: the engine (which still references
    # appController in its root context) must die before the controller.
    del engine
    del controller
    return rc


if __name__ == "__main__":
    sys.exit(main())
