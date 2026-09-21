# معماری خرج‌یار

## لایه‌ها

```
UI (Compose)  ←  ViewModel (StateFlow)  ←  Repository  ←  Room / DataStore
                                          ↑
BroadcastReceiver → WorkManager Worker ───┘
```

- **UI:** Jetpack Compose + Material 3 + Navigation Compose. تک‌Activity (`MainActivity` از نوع `FragmentActivity` برای BiometricPrompt). کل درخت UI داخل `CompositionLocalProvider(LayoutDirection.Rtl)`.
- **ViewModel:** `AppViewModel` سراسری؛ جریان‌های داده (حساب‌ها، تراکنش‌ها، صف بررسی، تنظیمات) با `stateIn` به UI می‌رسند. وضعیت قفل برنامه هم اینجاست.
- **Repository:** `data/Repository.kt` — منطق پردازش پیامک، ساخت پیش‌نویس، تأیید تراکنش، تطبیق انتقال و خلاصه مالی. عملیات حساس داخل `db.withTransaction`.
- **Core (بدون وابستگی به Android UI، کاملاً تست‌پذیر):**
  - `core/text/Digits` — نرمال‌سازی ارقام فارسی/عربی/لاتین و پارس مبلغ با جداکننده‌های مختلف
  - `core/money/Money` — ریال/تومان دقیق با Long (بدون Float/Double)
  - `core/date/PersianDate` — تقویم جلالی (الگوریتم jalaali)، مرز روز با منطقه زمانی تهران
  - `core/sms/SmsClassifier` — تفکیک مالی/غیرمالی/مشکوک
  - `core/sms/SmsFingerprint` — اثر انگشت پایدار برای جلوگیری از تکرار
  - `core/sms/Extractor` — استخراج خودکار + اعمال قواعد قالب + اعتبارسنجی + پیشنهاد قواعد از نمونه
  - `core/sms/AccountMatcher` — تطبیق فرستنده + شناسه داخل متن به حساب
  - `core/transfer/TransferMatcher` — تطبیق دو سمت انتقال داخلی
  - `core/category/CategorySuggester` — پیشنهاد دسته قانون‌محور
  - `core/backup/BackupCrypto|BackupPayload|BackupManager` — بکاپ رمزنگاری‌شده نسخه‌دار

## مدل داده (Room، نسخه ۱، Schema در `app/schemas`)

| موجودیت | نقش |
|---|---|
| `AccountEntity` | حساب کاربر (عنوان، بانک، رنگ، شناسه ماسک‌شده، موجودی اولیه + زمان، بایگانی) |
| `AccountSenderEntity` | نگاشت سرشماره + شناسه داخل متن → حساب (چند-به-چند) |
| `SmsCandidateEntity` | پیامک مالی/مشکوک در صف بررسی؛ `fingerprint` یکتا (dedupe در سطح DB) |
| `SmsTemplateEntity` | قالب آموزش‌دیده: قواعد JSON، نمونه، واحد مبلغ، نسخه، فعال/غیرفعال |
| `TransactionEntity` | مبلغ ریالی Long، جهت (واریز/برداشت) جدا از ماهیت (درآمد/هزینه/انتقال)، وضعیت PENDING/CONFIRMED، `userEdited`، ارتباط با پیامک منبع و گروه انتقال |
| `TransferGroupEntity` | گروه انتقال داخلی با پرچم `incomplete` |
| `CategoryEntity` / `CategoryRuleEntity` | دسته‌ها (۱۶ دسته پیش‌فرض seed) و قوانین خودکار |

Migrationها: نسخه فعلی ۱ است؛ `exportSchema = true` فعال است و از این پس هر تغییر Schema باید Migration صریح داشته باشد (destructive migration استفاده نمی‌شود).

## جریان پیامک

```
SMS_RECEIVED → SmsReceiver (goAsync، کوتاه):
  1. اتصال بخش‌های پیامک چندبخشی (getMessagesFromIntent)
  2. SmsClassifier: غیرمالی → رها می‌شود (ذخیره نمی‌شود)
  3. درج idempotent با fingerprint (OnConflict=IGNORE) → تکراری = خروج
  4. enqueueUniqueWork(SmsProcessWorker, KEY: فقط smsId — نه متن پیامک)

SmsProcessWorker:
  5. AccountMatcher: Unknown/Ambiguous → status=NEEDS_ACCOUNT + اعلان «معرفی حساب»
  6. اعمال قالب‌های فعال فرستنده + استخراج خودکار؛ بهترین نتیجه انتخاب می‌شود
  7. مبلغ/جهت مبهم یا اطمینان LOW → status=NEEDS_TEMPLATE + اعلان «آموزش قالب»
  8. موفق → درج TransactionEntity(PENDING) + status=DRAFT_READY + اعلان «برای چه بود؟»
```

- اعلان‌ها فقط به `MainActivity` با extra مقصد deep link می‌کنند؛ اگر قفل برنامه فعال باشد، `AppRoot` تا احراز هویت فقط صفحه قفل را نشان می‌دهد (اعلان قفل را دور نمی‌زند).
- پس از معرفی حساب یا ذخیره قالب، **همان پیامک اولیه** با `processWithAccount` دوباره پردازش می‌شود.
- «بعداً» یا کنارگذاشتن اعلان چیزی را حذف نمی‌کند؛ پیش‌نویس در صف بررسی و فهرست تراکنش‌ها با برچسب «در انتظار تأیید» می‌ماند.
- ویرایش دستی کاربر (`userEdited=true`) توسط پردازش خودکار بازنویسی نمی‌شود (پردازش مجدد فقط برای پیامک بدون تراکنش انجام می‌شود).

## موتور قالب

- قواعد anchor-based: «متن برچسب قبل از مقدار» + نوع مقدار (NUMBER/DATE/TIME/TEXT_LINE) + حداکثر فاصله. از موقعیت ثابت کاراکتر استفاده نمی‌شود و Regex آزاد از کاربر گرفته نمی‌شود (ایمن در برابر ReDoS).
- `suggestRules`: از نمونه + مقادیر تأییدشده کاربر، لنگر هر فیلد را از متنِ قبل از مقدار می‌سازد.
- `validateRules`: مبلغ الزامی، لنگر غیرخالی و کوتاه، نقش غیرتکراری.
- قبل از ذخیره، قالب روی همان نمونه اجرا و نتیجه نمایش داده می‌شود.

## پول و تاریخ

- ذخیره: ریال، `Long`؛ تبدیل تومان با `Math.multiplyExact`. نمایش تومان باقی‌مانده ریالی را صریحاً می‌نویسد («۱۵٬۰۰۰ تومان و ۵ ریال»).
- واحد قالب پیامک (`amountUnit`) مستقل از واحد نمایش برنامه است.
- زمان: epoch millis؛ مرز روز/ماه شمسی با `Asia/Tehran` محاسبه می‌شود. اگر تاریخ از پیامک استخراج نشود، زمان دریافت با پرچم `timeIsApproximate` و برچسب «زمان دریافت» استفاده می‌شود.

## بکاپ

- Snapshot منطقی JSON (نسخه‌دار) از همه جدول‌ها + تنظیمات غیرحساس؛ نه کپی فایل DB (به‌خاطر WAL).
- فرمت فایل: `KHRJBKP1 | version | iterations | salt(16) | nonce(12) | AES-256-GCM(payload)`؛ GCM صحت/اصالت را تضمین می‌کند؛ nonce هر بار تازه است.
- Restore: اعتبارسنجی کامل قبل از هر تغییری؛ سپس جایگزینی کامل در یک تراکنش DB.

## ویجت

- Glance؛ داده‌ها در `provideGlance` از Repository خوانده می‌شوند. با قفل فعال، اعداد پیش‌فرض «••••» مگر تنظیم صریح. `WidgetUpdater.requestUpdate` پس از هر تغییر داده/تنظیمات صدا زده می‌شود. افزودن با `requestPinAppWidget` (تأیید لانچر/کاربر لازم است).
