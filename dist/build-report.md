# گزارش Build و تست (CI)

- commit: 537345867f876b2a946ad86ebbc25dca5cdeebc3
- تاریخ: 2026-10-02 21:00 UTC
- unit tests: failure
- assembleDebug: success
- بررسی حریم خصوصی (مانیفست ادغام‌شده): success
- lintDebug: success
- assembleRelease: success

## خلاصه تست‌های واحد
```
tests=498 failures=4 errors=0 skipped=0
FAIL ir.kharjyar.app.MarketPulseListDesignGuardTest.marketPulseUsesCompactListsAndRealRefreshComparison: java.lang.AssertionError
FAIL ir.kharjyar.app.DashboardCompactAccountCardGuardTest.accountCardIsUnifiedCompactAndExpandsOnlyWhenSelected: java.lang.AssertionError
FAIL ir.kharjyar.app.AmountLimitAndUniformAccountCardGuardTest.dashboard account area is stable and visual card is compact: java.lang.AssertionError
FAIL ir.kharjyar.app.MarketPulseFullPageGuardTest.popup opens a graphical full market page with metals and currencies: java.lang.AssertionError: ارزهای رایج
```

## نسخه انتشار
- حجم: 34M
- کلید اختصاصی: false
- امضا: معتبر
