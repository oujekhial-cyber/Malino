import sys
from pathlib import Path
from PySide6.QtGui import QGuiApplication, QIcon
from PySide6.QtQml import QQmlApplicationEngine
from PySide6.QtCore import QUrl
from core.controller import AppController

ROOT = Path(__file__).resolve().parent

def main():
    app = QGuiApplication(sys.argv)
    app.setApplicationName("Malino")
    app.setApplicationDisplayName("مالینو")
    icon = ROOT / "assets" / "malino_icon.svg"
    if icon.exists(): app.setWindowIcon(QIcon(str(icon)))
    controller = AppController(ROOT)
    engine = QQmlApplicationEngine()
    engine.rootContext().setContextProperty("appController", controller)
    engine.load(QUrl.fromLocalFile(str(ROOT / "qml" / "Main.qml")))
    if not engine.rootObjects(): return 1
    return app.exec()

if __name__ == "__main__": sys.exit(main())
