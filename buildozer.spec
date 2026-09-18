[app]
title = Malino
package.name = malino
package.domain = com.malino
source.dir = .
source.include_exts = py,qml,svg,xml,java,txt
requirements = python3,shiboken6,PySide6
orientation = portrait
fullscreen = 0
android.permissions = RECEIVE_SMS,READ_SMS,POST_NOTIFICATIONS
android.api = 35
android.minapi = 28
android.archs = arm64-v8a
android.add_src = %(source.dir)s/android/src
p4a.hook = %(source.dir)s/p4a/hook.py
android.allow_backup = True

[buildozer]
log_level = 2
warn_on_root = 0
bin_dir = ./bin
mode = debug
arch = aarch64

[qt]
# Populated by pyside6-android-deploy / Qt bootstrap.

[android]
# Filled by build_android.sh after qtpip downloads the Android wheels.
wheel_pyside =
wheel_shiboken =
plugins =

