# گزارش Build و تست (CI)

- commit: 08615edb9e8281c4bdc05047987a6fa999cb18d5
- تاریخ: 2026-09-25 19:21 UTC
- unit tests: failure
- assembleDebug: success
- بررسی حریم خصوصی (مانیفست ادغام‌شده): success
- lintDebug: success
- assembleRelease: success

## خلاصه تست‌های واحد
```
tests=269 failures=4 errors=0 skipped=0
FAIL ir.kharjyar.app.PaletteMigrationTest.all five palettes are available: java.lang.AssertionError: expected:<[SAKURA, VIOLET, LOTUS, OCEAN, GOLD]> but was:<[SAKURA, VIOLET, LOTUS, OCEAN, GOLD, MINIMAL_DAY, MINIMAL_NIGHT]>
FAIL ir.kharjyar.app.PaletteMigrationTest.every skin carries neon colors and a hero image: java.lang.AssertionError
FAIL ir.kharjyar.app.PaletteMigrationTest.there is exactly one light skin among the palettes: java.lang.AssertionError: expected:<1> but was:<2>
FAIL ir.kharjyar.app.TransactionParserTest.detects deposit from verbs: java.lang.AssertionError: expected:<0> but was:<1>
```

## نسخه انتشار
- حجم: 31M
- کلید اختصاصی: false
- امضا: معتبر
