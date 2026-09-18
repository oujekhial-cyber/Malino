#!/usr/bin/env bash
set -e
# این اسکریپت برای بعد از ایجاد deployment files توسط pyside6-android-deploy است.
# هدف: کپی کردن Receiver/Manifest پروژه در قالب Android تولیدشده.
find . -type d -name "src" -path "*android*" | head -n 1 || true
printf '%s\n' "Android native receiver source is under android/src/com/malino/app/MalinoSmsReceiver.java"
printf '%s\n' "Manifest permissions are under android/AndroidManifest.xml"
