# گزارش Build و تست (CI)

- commit: feadf0d94016381c2772c3f39af5743bf8fa8b5a
- تاریخ: 2026-09-29 15:03 UTC
- unit tests: failure
- assembleDebug: success
- بررسی حریم خصوصی (مانیفست ادغام‌شده): success
- lintDebug: success
- assembleRelease: success

## خلاصه تست‌های واحد
```
tests=411 failures=5 errors=0 skipped=0
FAIL ir.kharjyar.app.WidgetSettingsTest.opacity maps to alpha fraction: java.lang.AssertionError: expected:<0.92> but was:<0.5>
FAIL ir.kharjyar.app.WidgetSettingsTest.widget defaults are sane: java.lang.AssertionError: expected:<92> but was:<50>
FAIL ir.kharjyar.app.WidgetBitmapSizeTest.opacity default keeps widget mostly opaque: java.lang.AssertionError: پیش‌فرض باید خوانا باشد
FAIL ir.kharjyar.app.MarketPriceParserTest.parses Persian dollar digits and ignores percentage: java.lang.AssertionError: expected:<2221000> but was:<null>
FAIL ir.kharjyar.app.SettingsPolishGuardTest.requested Persian labels are corrected: java.lang.AssertionError
```

## نسخه انتشار
- حجم: 31M
- کلید اختصاصی: false
- امضا: معتبر
