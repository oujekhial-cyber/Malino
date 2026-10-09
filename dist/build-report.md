# گزارش Build و تست (CI)

- commit: cc5a223fdcab5cf8499a94dcf244af30d485644d
- تاریخ: 2026-10-09 09:20 UTC
- unit tests: failure
- assembleDebug: success
- بررسی حریم خصوصی (مانیفست ادغام‌شده): success
- lintDebug: success
- assembleRelease: success

## خلاصه تست‌های واحد
```
tests=542 failures=2 errors=0 skipped=0
FAIL ir.kharjyar.app.PersonDebtLedgerGuardTest.payment history retains date amount and note: java.lang.AssertionError: payment.note.ifBlank
FAIL ir.kharjyar.app.WidgetOptionsGuardTest.home carousel snaps one card at a time: java.lang.AssertionError: چسبیدن کارت‌ها فعال نیست
```

## نسخه انتشار
- حجم: 34M
- کلید اختصاصی: false
- امضا: معتبر
