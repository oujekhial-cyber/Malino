# مالینو (Malino) — PySide6 + Qt Quick/QML Android

نسخه 1.1 شامل:
- داشبورد واقعی با محاسبه واریز، برداشت و خالص روز/هفته/ماه
- واحد پول تومان/ریال در کل فرم‌ها و نمایش‌ها
- جداکننده سه‌رقمی هنگام تایپ مبلغ
- ویرایش و حذف تراکنش
- انتخاب بانک از لیست + افزودن بانک جدید از داخل فرم
- یادگیری قالب پیامک با ذخیره قواعد استخراج مبلغ/موجودی از نمونه پیامک
- تشخیص پایه بانک و نوع واریز/برداشت
- دریافت پیامک بانکی با BroadcastReceiver اندروید و اعلان
- سه تم روشن، نیمه‌شب و نئونی
- SQLite محلی

## ساخت APK
ساخت PySide6 اندروید روی میزبان Unix انجام می‌شود و ابزار رسمی Qt برای این کار `pyside6-android-deploy` است. برای APK، حالت debug استفاده می‌شود؛ برای AAB حالت release را تنظیم کنید.

فایل `build_android.sh` آماده‌سازی SDK/NDK و wheelهای Android را انجام می‌دهد و سپس deployment را اجرا می‌کند.

## نکته مهم SMS
گیرنده SMS با `android.add_src` و یک `p4a/hook.py` به template مانیفست تزریق می‌شود. این بخش باید روی GitHub Actions/Ubuntu یا Linux واقعی build شود تا ادغام Android و Gradle در محیط واقعی تأیید شود.

## GitHub
نام پیشنهادی مخزن: `Malino`

```bash
git init
git branch -M main
git remote add origin git@github.com:oujekhial-cyber/Malino.git
git add .
git commit -m "Malino 1.1 PySide6 Android"
git push -u origin main
```

## اجرای محلی دسکتاپ
برای تست UI روی Linux/Windows با PySide6 دسکتاپ:

```bash
pip install -r requirements.txt
python main.py
```

نسخه Android باید با wheel مخصوص Android و ابزار `pyside6-android-deploy` ساخته شود.
