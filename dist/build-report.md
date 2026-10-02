# گزارش Build و تست (CI)

- commit: 8ae78d7a2726628afd36896d8d2f9d244c0d9da5
- تاریخ: 2026-10-02 08:16 UTC
- unit tests: failure
- assembleDebug: success
- بررسی حریم خصوصی (مانیفست ادغام‌شده): success
- lintDebug: success
- assembleRelease: success

## خلاصه تست‌های واحد
```
tests=472 failures=9 errors=0 skipped=0
FAIL ir.kharjyar.app.LatestRequestedRefinementsTest.bank balance snapshots remain separate from estimated balance: java.lang.AssertionError
FAIL ir.kharjyar.app.SwipeAndBankLogoGuardTest.entry form speaks about the amount, not the direction: java.lang.AssertionError
FAIL ir.kharjyar.app.SwipeAndBankLogoGuardTest.category picker can create a new category: java.lang.AssertionError: ManualEntryScreen دسته‌بندی جدید را وصل نکرده
FAIL ir.kharjyar.app.SwipeAndBankLogoGuardTest.transfer is picked upfront instead of asking what the money was: java.lang.AssertionError
FAIL ir.kharjyar.app.MarketPulseListDesignGuardTest.marketPulseUsesCompactListsAndRealRefreshComparison: java.lang.AssertionError
FAIL ir.kharjyar.app.DashboardCompactAccountCardGuardTest.accountDetailsAndFlowsLiveBelowShortCard: java.lang.AssertionError
FAIL ir.kharjyar.app.AmountLimitAndUniformAccountCardGuardTest.dashboard account cards share one stable height: java.lang.AssertionError
FAIL ir.kharjyar.app.LatestUiRoundGuardTest.internal transfers create two linked rows without category: java.lang.AssertionError
FAIL ir.kharjyar.app.BankSmsImportTitleGuardTest.bank sms import uses requested retrieval title: java.lang.AssertionError
```

## نسخه انتشار
- حجم: 31M
- کلید اختصاصی: false
- امضا: معتبر
