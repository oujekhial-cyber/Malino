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

ابزار رسمی Qt برای ساخت اندروید `pyside6-android-deploy` است. برای APK از حالت
debug و برای AAB از حالت release استفاده می‌شود.

```bash
./build_android.sh
```

این اسکریپت سه مرحله دارد و همه نسخه‌ها را قفل (pin) می‌کند:

1. **تولید buildozer.spec** — `pyside6-android-deploy` فایل buildozer.spec
   مخصوص Qt (بوت‌استرپ qt، jarها، recipeهای PySide6/shiboken6) را می‌سازد.
2. **پچ buildozer.spec** — اسکریپت `scripts_patch_buildozer.py` نسخه
   python-for-android را به `v2024.01.21` قفل می‌کند (آخرین نسخه‌ای که هم
   بوت‌استرپ qt را دارد و هم پایتون 3.11 می‌سازد؛ شاخه develop فعلی p4a پایتون
   3.14 می‌سازد و با wheelهای cp311 ناسازگار است) و مجوزها و
   `MalinoSmsReceiver` را دوباره اضافه می‌کند، چون spec تولیدی آن‌ها را حمل
   نمی‌کند.
3. **ساخت نهایی** — p4a روی `v2024.01.21` کلون و توسط `scripts_patch_p4a.py`
   پچ می‌شود (سازگاری Qt 6.8 + فیکس کرش `bundled_libs`) و بعد
   `buildozer android debug` با spec پچ‌شده اجرا می‌شود.

## چرا برنامه قبلی بعد از ساخت اجرا نمی‌شد؟

- ابزار Qt شاخه `develop` کتابخانه python-for-android را hardcode کرده است.
  CI این شاخه را «امروز» کلون می‌کند و پایتون 3.14 می‌سازد، در حالی که
  wheelهای اندروید PySide6 6.8 از نوع cp311 هستند؛ نتیجه: APK بدون اجرا بسته
  می‌شود.
- ابزار Qt در پایان اجرا، `buildozer.spec` و پوشه `deployment/` را پاک می‌کند و
  خطاهای build را هم بلعیده و با کد خروج 0 خارج می‌شود؛ برای همین CI «سبز» می‌ماند.
- `android.add_src` و `p4a.hook` (دریافت SMS) فقط در spec دستی پروژه بودند و در
  spec تولیدی ابزار از دست می‌رفتند.
- چند فایل QML به `root` ارجاع می‌دادند در حالی که `id: root` در همان فایل
  تعریف نشده بود؛ رنگ/تم/مبلغ‌ها در همه صفحات از کار می‌افتاد.
- `main.py` در صورت شکست بارگذاری QML بدون هیچ پیامی `return 1` می‌کرد.
- بعد از ساخت، `QtLoader.java` (داخل jar خود Qt) هر کتابخانه‌ای را که در
  `res/values/libs.xml` آمده باشد از `lib/` دایرکتوری APK با `System.load`
  بارگذاری می‌کند. اما این لیست از `[qt] modules` در `pysidedeploy.spec`
  می‌آید، در حالی که recipe PySide6 فقط کتابخانه‌هایی را در APK قرار می‌دهد
  که واقعاً در wheel وجود دارند. هر module‌ای که `libQt6<Module>_<arch>.so`-اش
  در wheel نباشد (مثلاً `QmlModels` و `QuickTemplates2`) در لیست می‌ماند ولی
  فایلش در APK نیست؛ نتیجه: APK ساخته می‌شود، CI سبز است، ولی برنامه هنگام
  استارتاپ — پیش از شروع Python — با `java.lang.UnsatisfiedLinkError` کرش
  می‌کند. `scripts_patch_p4a.py` حالا این لیست را با کتابخانه‌های واقعاً
  bundled (فیکس `bundled_libs`) تطبیق می‌دهد و ورودی‌های فاقد فایل را حذف
  می‌کند؛ برای تشخیص مشکل‌های بعدی، لیست واقعی کتابخانه‌های bundled هم هنگام
  استارت در logcat چاپ می‌شود (`bundled_libs (N): [...]`).

## نکته SMS

گیرنده SMS در `android/src/com/malino/app/MalinoSmsReceiver.java` است؛ با
`android.add_src` کامپایل و با `p4a/hook.py` به مانیفست تزریق می‌شود. مجوزهای
`RECEIVE_SMS`، `READ_SMS` و `POST_NOTIFICATIONS` توسط `scripts_patch_buildozer.py`
اضافه می‌شوند.

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

GitHub Actions روی push به `main` APK می‌سازد و به‌عنوان artifact
(`Malino-Android`) آپلود می‌کند. مرحله «Verify APK contents» بررسی می‌کند که
APK واقعاً شامل Qt، پایتون 3.11 و گیرنده SMS باشد.

## اجرای محلی دسکتاپ

```bash
pip install -r requirements.txt
python main.py
```
