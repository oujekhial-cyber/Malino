# گزارش Build و تست (CI)

- commit: 6335c7e46df012b918701cc60bda9fa2cead45a6
- تاریخ: 2026-09-27 14:46 UTC
- unit tests: failure
- assembleDebug: success
- بررسی حریم خصوصی (مانیفست ادغام‌شده): success
- lintDebug: success
- assembleRelease: success

## خلاصه تست‌های واحد
```
tests=320 failures=4 errors=0 skipped=0
FAIL ir.kharjyar.app.PaletteMigrationTest.default palette is sakura: java.lang.AssertionError: expected:<SAKURA> but was:<MINIMAL_DAY>
FAIL ir.kharjyar.app.DisplaySettingsTest.defaults are rial persian sakura with shine: java.lang.AssertionError: expected:<SAKURA> but was:<MINIMAL_DAY>
FAIL ir.kharjyar.app.SettingsPagesAndWidgetWeatherGuardTest.settings cards navigate to dedicated pages: java.lang.AssertionError
FAIL ir.kharjyar.app.SayadCheckParserTest.extracts labeled sayad check fields: java.lang.AssertionError: expected: java.lang.Integer<42000000> but was: java.lang.Long<42000000>
```

## نسخه انتشار
- حجم: 31M
- کلید اختصاصی: false
- امضا: معتبر
