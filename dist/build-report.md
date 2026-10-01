# گزارش Build و تست (CI)

- commit: de9a444d4f4bfa17ebc9a793f3ff93410195e84c
- تاریخ: 2026-10-01 07:46 UTC
- unit tests: failure
- assembleDebug: success
- بررسی حریم خصوصی (مانیفست ادغام‌شده): success
- lintDebug: success
- assembleRelease: success

## خلاصه تست‌های واحد
```
tests=457 failures=2 errors=0 skipped=0
FAIL ir.kharjyar.app.CivicListFirstGuardTest.civic messages support sms import and explicit manual utility bill entry: java.lang.AssertionError
FAIL ir.kharjyar.app.UnifiedMoneyAmountFieldsGuardTest.money forms pass selected unit and convert input to rial storage: java.lang.AssertionError: unit missing in CivicCenterScreen.kt
```

## نسخه انتشار
- حجم: 31M
- کلید اختصاصی: false
- امضا: معتبر
