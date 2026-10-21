# گزارش Build و تست (CI)

- commit: acbd7acf01625745cf696c889740ea6451c3f43f
- تاریخ: 2026-10-21 02:39 UTC
- unit tests: failure
- assembleDebug: success
- بررسی حریم خصوصی (مانیفست ادغام‌شده): success
- lintDebug: success
- assembleRelease: success

## خلاصه تست‌های واحد
```
tests=617 failures=4 errors=0 skipped=0
FAIL ir.kharjyar.app.SwipeAndBankLogoGuardTest.row action needs distance and rejects fast drawer flick: java.lang.AssertionError: حرکت سریع باید از حذف/ویرایش کارت جلوگیری کند
FAIL ir.kharjyar.app.FastGlobalDrawerGestureGuardTest.fast drawer gesture wraps every screen and observes before children: java.lang.AssertionError
FAIL ir.kharjyar.app.FastGlobalDrawerGestureGuardTest.drawer flick is directional and predominantly horizontal: java.lang.AssertionError
FAIL ir.kharjyar.app.FastGlobalDrawerGestureGuardTest.fast swipe never triggers row edit or delete: java.lang.AssertionError
```

## نسخه انتشار
- حجم: 35M
- کلید اختصاصی: false
- امضا: معتبر
