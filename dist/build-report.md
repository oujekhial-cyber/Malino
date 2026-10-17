# گزارش Build و تست (CI)

- commit: e6d90d501fda298f59e0fcd4cfbb36b505deaaf5
- تاریخ: 2026-10-17 21:09 UTC
- unit tests: failure
- assembleDebug: success
- بررسی حریم خصوصی (مانیفست ادغام‌شده): success
- lintDebug: success
- assembleRelease: success

## خلاصه تست‌های واحد
```
tests=545 failures=2 errors=0 skipped=0
FAIL ir.kharjyar.app.SimpleTransactionEntryGuardTest.dailyFormIsShortAndAdvancedFieldsAreProgressive: java.lang.AssertionError: The confusing dual direction/nature picker must not return
FAIL ir.kharjyar.app.TransactionArrowDirectionGuardTest.deposit points inward green and withdrawal points outward red everywhere: java.lang.AssertionError
```

## نسخه انتشار
- حجم: 34M
- کلید اختصاصی: false
- امضا: معتبر
