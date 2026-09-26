# گزارش Build و تست (CI)

- commit: f64e3fe0758291b566e6991ad39df09114c0a7b1
- تاریخ: 2026-09-26 05:12 UTC
- unit tests: failure
- assembleDebug: success
- بررسی حریم خصوصی (مانیفست ادغام‌شده): success
- lintDebug: success
- assembleRelease: success

## خلاصه تست‌های واحد
```
tests=272 failures=2 errors=0 skipped=0
FAIL ir.kharjyar.app.SwipeAndBankLogoGuardTest.deposit and withdraw chips open the matching transaction list: java.lang.AssertionError: میان‌بر واریز نیست
FAIL ir.kharjyar.app.LatestUiRoundGuardTest.home summary chips open filtered transaction screen: java.lang.AssertionError
```

## نسخه انتشار
- حجم: 31M
- کلید اختصاصی: false
- امضا: معتبر
