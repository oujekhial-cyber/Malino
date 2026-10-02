# گزارش Build و تست (CI)

- commit: 65d1ba14e18e4cb46f5699abee3cb80d3fd296b4
- تاریخ: 2026-10-02 10:45 UTC
- unit tests: failure
- assembleDebug: success
- بررسی حریم خصوصی (مانیفست ادغام‌شده): success
- lintDebug: success
- assembleRelease: success

## خلاصه تست‌های واحد
```
tests=484 failures=4 errors=0 skipped=0
FAIL ir.kharjyar.app.MarketChangePrecisionTest.smallRealChangesDoNotRoundToFalseZero: java.lang.AssertionError: ↗  +۰.۰۰۴۰٪
FAIL ir.kharjyar.app.MarketChangePrecisionTest.fallingPriceUsesAbsoluteNumberAndDownDirection: java.lang.AssertionError
FAIL ir.kharjyar.app.MarketPopupReadabilityGuardTest.market popup is anchored compact glassy and has solid rate cards: java.lang.AssertionError: panelBase.copy(alpha=if(darkMarket).86f else .84f)
FAIL ir.kharjyar.app.BankSmsHistoryFilteringGuardTest.history classifies and extracts before accepting mapped inferred or account matched bank: java.lang.AssertionError: !mappedSender&&!bankSender&&aid==null&&!bankInBody
```

## نسخه انتشار
- حجم: 32M
- کلید اختصاصی: false
- امضا: معتبر
