# گزارش Build و تست (CI)

- commit: 12b56f30bf8d3535e238812d6abcba847e183af9
- تاریخ: 2026-09-28 07:41 UTC
- unit tests: failure
- assembleDebug: success
- بررسی حریم خصوصی (مانیفست ادغام‌شده): success
- lintDebug: success
- assembleRelease: success

## خلاصه تست‌های واحد
```
tests=356 failures=4 errors=0 skipped=0
FAIL ir.kharjyar.app.AccountNumberMatcherTest.matches masked card suffix in bank sms: java.lang.AssertionError: expected:<Single(accountId=1)> but was:<Ambiguous(accountIds=[1, 2])>
FAIL ir.kharjyar.app.IranianTrafficFinePlateTest.links only a unique matching vehicle: java.lang.AssertionError: expected:<7> but was:<null>
FAIL ir.kharjyar.app.IranianTrafficFinePlateTest.matches common saved plate arrangements: java.lang.AssertionError
FAIL ir.kharjyar.app.IranianTrafficFinePlateTest.extracts Iran plate despite Arabic ye spaces and dash: java.lang.AssertionError: expected:<69|ی|519|83> but was:<null>
```

## نسخه انتشار
- حجم: 31M
- کلید اختصاصی: false
- امضا: معتبر
