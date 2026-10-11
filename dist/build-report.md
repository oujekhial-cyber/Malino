# گزارش Build و تست (CI)

- commit: 172062d0c486330d442cec38ee105f8a03bcb9e8
- تاریخ: 2026-10-11 06:02 UTC
- unit tests: failure
- assembleDebug: success
- بررسی حریم خصوصی (مانیفست ادغام‌شده): success
- lintDebug: success
- assembleRelease: success

## خلاصه تست‌های واحد
```
tests=542 failures=2 errors=0 skipped=0
FAIL ir.kharjyar.app.VehicleVerticalPlateReorderGuardTest.drop can cross multiple cards and displaced cards settle slowly: java.lang.AssertionError
FAIL ir.kharjyar.app.ReorderEdgeAutoScrollGuardTest.all reorderable lists auto scroll at physical window edges: java.lang.AssertionError: rowState.scrollBy(requested)
```

## نسخه انتشار
- حجم: 34M
- کلید اختصاصی: false
- امضا: معتبر
