# گزارش Build و تست (CI)

- commit: 539593c105ba306b115408b6e73b9200c137da72
- تاریخ: 2026-10-03 13:29 UTC
- unit tests: failure
- assembleDebug: success
- بررسی حریم خصوصی (مانیفست ادغام‌شده): success
- lintDebug: success
- assembleRelease: success

## خلاصه تست‌های واحد
```
tests=510 failures=3 errors=0 skipped=0
FAIL ir.kharjyar.app.SaveFeedbackAndReturnGuardTest.profile transaction and account saves confirm then pop when workflow is complete: java.lang.AssertionError
FAIL ir.kharjyar.app.RequestedUiAndIranGoldGuardTest.drawer is compact and labels are explicit: java.lang.AssertionError
FAIL ir.kharjyar.app.BankSmsHistoryFilteringGuardTest.history classifies and extracts before accepting mapped inferred or account matched bank: java.lang.AssertionError: SmsClassifier.classify(body)!=SmsKind.FINANCIAL_LIKELY
```

## نسخه انتشار
- حجم: 34M
- کلید اختصاصی: false
- امضا: معتبر
