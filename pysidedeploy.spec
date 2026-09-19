[app]
title = Malino
project_dir = .
input_file = main.py
exec_directory = .
icon = assets/malino_icon.png

[python]
python_path = .
# left empty on purpose: the deploy tool would run `pip install --force <pkg>`
# (an invalid flag) whenever the installed version differs from the pin, which
# kills the whole deployment. Everything needed (buildozer, cython<3, ...) is
# installed explicitly by build_android.sh / the CI workflow instead.
android_packages =

[qt]
# Added QmlModels and QuickTemplates2 which are required by QuickControls2
# to avoid black screen due to missing QML types on Android
modules = Core,Gui,Qml,QmlModels,Quick,QuickControls2,QuickTemplates2

[android]
wheel_pyside =
wheel_shiboken =
plugins =

[buildozer]
mode = debug
arch = aarch64
