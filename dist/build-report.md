# گزارش Build و تست (CI)

- commit: 9ef63b9d50d9817394cf981a80ed769f08d60338
- تاریخ: 2026-09-25 16:45 UTC
- unit tests: failure
- assembleDebug: success
- بررسی حریم خصوصی (مانیفست ادغام‌شده): success
- lintDebug: success
- assembleRelease: success

## خلاصه تست‌های واحد
```
tests=254 failures=2 errors=0 skipped=0
FAIL ir.kharjyar.app.RealBankSmsSamplesTest.sepah full short year date is automatic: java.lang.AssertionError: expected:<252080213010571> but was:<null>
FAIL ir.kharjyar.app.RealBankSmsSamplesTest.refah compact date and stuck labels are automatic: java.lang.AssertionError: تاریخ اشتباه است: 1406/04/14 14:32
```

## نسخه انتشار
- حجم: 31M
- کلید اختصاصی: false
- امضا: معتبر
