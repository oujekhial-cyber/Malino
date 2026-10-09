# گزارش Build و تست (CI)

- commit: d6d451b3895deddb77cf1ae1fde5697dd9a8be2f
- تاریخ: 2026-10-09 06:50 UTC
- unit tests: failure
- assembleDebug: success
- بررسی حریم خصوصی (مانیفست ادغام‌شده): success
- lintDebug: success
- assembleRelease: success

## خلاصه تست‌های واحد
```
tests=537 failures=6 errors=0 skipped=0
FAIL ir.kharjyar.app.LoanDetailsHistoryGuardTest.loan list opens details with summary and paid installment history: java.lang.AssertionError: اقساط پرداخت‌شده
FAIL ir.kharjyar.app.RequiredCheckFieldsGuardTest.sayad amount and due date are required and obsolete fields are removed: java.lang.AssertionError
FAIL ir.kharjyar.app.CheckClearingTransactionGuardTest.clearing check requires account and writes matching confirmed transaction: java.lang.AssertionError: pendingClear=c
FAIL ir.kharjyar.app.ExpressiveDetailPagesGuardTest.loan details use elevated gradient and accented history cards: java.lang.AssertionError: accent.copy(alpha=.08f)
FAIL ir.kharjyar.app.SayadCheckDetailGuardTest.clicking check opens landscape sayad cheque visualization: java.lang.AssertionError: SayadCheckDetail(check,settings.moneyUnit){selectedCheck=null}
FAIL ir.kharjyar.app.CategoryGraphicIconsGuardTest.category list and editor use graphical category component: java.lang.AssertionError
```

## نسخه انتشار
- حجم: 34M
- کلید اختصاصی: false
- امضا: معتبر
