# گزارش Build و تست (CI)

- commit: 3ff49522d62577934d819deac9b5c68cc503015c
- تاریخ: 2026-10-03 05:12 UTC
- unit tests: failure
- assembleDebug: success
- بررسی حریم خصوصی (مانیفست ادغام‌شده): success
- lintDebug: success
- assembleRelease: success

## خلاصه تست‌های واحد
```
tests=500 failures=2 errors=0 skipped=0
FAIL ir.kharjyar.app.LatestRequestedRefinementsTest.bank balance snapshots remain separate from estimated balance: java.lang.AssertionError
FAIL ir.kharjyar.app.SwipeAndBankLogoGuardTest.bank logo is watermarked on the account card: java.lang.AssertionError: واترمارک باید کم‌رنگ باشد
```

## نسخه انتشار
- حجم: 34M
- کلید اختصاصی: false
- امضا: معتبر
