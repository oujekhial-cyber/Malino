# وضعیت Build و تست (صادقانه)

تاریخ: 2026-09-22

## نتیجه آخرین اجرا (بازطراحی ظاهر — نسخه ۱)

commit: `43b0601` — شاخه `arena/01a0c765-malino` — run id: `35688676743`

| مرحله | نتیجه |
|---|---|
| `:app:testDebugUnitTest` | ✅ موفق — `tests=101 failures=0 errors=0 skipped=0` |
| `:app:assembleDebug` | ✅ موفق |
| `:app:lintDebug` | ✅ موفق |

خروجی‌ها در پوشه `dist/`:

- `dist/KharjYar-debug.apk` — APK دیباگ قابل نصب (۶۵.۵ مگابایت)
- `dist/KharjYar-debug.apk.sha256` — `30bbe32a92772f700904d3b53e1bd1d51a50f5f1538e45f21c62c8562857211d`
- `dist/build-report.md`, `dist/unittest-tail.log`, `dist/assemble-tail.log`, `dist/lint-tail.log`
- `dist/schemas/` — Schema های Room

## کجا Build اجرا شد

- **در سندباکس ایجنت:** Build اجرا **نشد**. سندباکس به `dl.google.com` /
  `maven.google.com` / `services.gradle.org` / `repo1.maven.org` دسترسی شبکه ندارد و JDK هم
  نصب‌شدنی نبود؛ بنابراین `./gradlew` قابل اجرا نبود. این محدودیت صریحاً اعلام می‌شود.
- **در GitHub Actions:** workflow «Build KharjYar Android» روی همین شاخه اجرا شد و
  خروجی‌های بالا را به‌صورت خودکار به `dist/` کامیت کرد. نتیجه بالا از همان اجرا خوانده شده است.

## تست‌های واحد افزوده‌شده در این تغییر

- `AmountInputTest` — نرمال‌سازی ورودی مبلغ (فارسی/عربی/لاتین و جداکننده‌ها)، حذف صفرهای ابتدایی،
  سقف ارقام، جداسازی سه‌رقمی، و **پایداری نگاشت مکان‌نما** در `VisualTransformation`
  (این تست یک باگ واقعی در محاسبه موقعیت مکان‌نما را گرفت که اصلاح شد).
- `AccountBalanceTest` — مانده برآوردی حساب: موجودی اولیه + تراکنش‌های تأییدشده،
  اولویت آخرین مانده پیامکی وقتی جدیدتر است، نادیده‌گرفتن تراکنش‌های تأییدنشده و حساب‌های دیگر.
- `PaletteMigrationTest` — چهار تم جدید و پیش‌فرض بودن تم «شیشه‌ای».

## پوشش تست‌های واحد موجود

- `DigitsTest`, `MoneyTest`, `PersianDateTest`, `SmsClassifierTest`, `ExtractorTest`,
  `AccountMatcherTest`, `TransferMatcherTest`, `BackupCryptoTest`, `FingerprintAndCategoryTest`

## تست دستگاه

روی هیچ دستگاه یا شبیه‌ساز واقعی اجرا نشده است (در سندباکس در دسترس نبود). موفق اعلام نمی‌شود.
ظاهر جدید (تم‌ها، منوی کشویی، انیمیشن کارت‌ها، رفتار کیبورد) فقط از نظر کامپایل و تست واحد
تأیید شده؛ تأیید بصری نیاز به نصب APK روی دستگاه دارد.
