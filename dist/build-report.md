# گزارش Build و تست (CI)

- commit: c002142687eb930e7933674cd9d91b5c923266ff
- تاریخ: 2026-10-01 21:32 UTC
- unit tests: failure
- assembleDebug: success
- بررسی حریم خصوصی (مانیفست ادغام‌شده): success
- lintDebug: success
- assembleRelease: success

## خلاصه تست‌های واحد
```
tests=465 failures=2 errors=0 skipped=0
FAIL ir.kharjyar.app.MarketPulseFullPageGuardTest.popup opens a graphical full market page with metals and currencies: java.lang.AssertionError: منبع عمومی TGJU
FAIL ir.kharjyar.app.OtpSmsPrivacyGuardTest.non financial otp exits ingestion before fingerprint and database row creation: java.lang.AssertionError
```

## نسخه انتشار
- حجم: 31M
- کلید اختصاصی: false
- امضا: معتبر
