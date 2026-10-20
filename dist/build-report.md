# گزارش Build و تست (CI)

- commit: f27dc7cb883bc5a0dabf12f3194304172a71b303
- تاریخ: 2026-10-20 05:10 UTC
- unit tests: failure
- assembleDebug: success
- بررسی حریم خصوصی (مانیفست ادغام‌شده): success
- lintDebug: success
- assembleRelease: success

## خلاصه تست‌های واحد
```
tests=586 failures=3 errors=0 skipped=0
FAIL ir.kharjyar.app.NotificationTransactionEditRouteGuardTest.phone and in app notifications expose transaction editing: java.lang.AssertionError
FAIL ir.kharjyar.app.ExperimentalPersianAiGuardTest.AI creates validated draft while command center requires confirmation: java.lang.AssertionError
FAIL ir.kharjyar.app.MetalAssetPurchaseRateGuardTest.default one gram is selected completely on first focus: java.lang.AssertionError
```

## نسخه انتشار
- حجم: 35M
- کلید اختصاصی: false
- امضا: معتبر
