[app]
title = Malino
project_dir = .
input_file = main.py
exec_directory = .
icon = assets/malino_icon.svg

[python]
python_path = .
android_packages = buildozer==1.5.0,cython==0.29.33

[qt]
modules = Core,Gui,Qml,Quick,QuickControls2

[android]
wheel_pyside =
wheel_shiboken =
plugins =

[buildozer]
mode = debug
arch = aarch64
