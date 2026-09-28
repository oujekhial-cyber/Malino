# گزارش Build و تست (CI)

- commit: 90d133d595315b63a62dc190c9a16ce7bce123bf
- تاریخ: 2026-09-28 23:55 UTC
- unit tests: failure
- assembleDebug: success
- بررسی حریم خصوصی (مانیفست ادغام‌شده): success
- lintDebug: success
- assembleRelease: success

## خلاصه تست‌های واحد
```
tests=396 failures=6 errors=0 skipped=0
FAIL ir.kharjyar.app.CivicListFirstGuardTest.civic financial data remains sms only: java.lang.AssertionError
FAIL ir.kharjyar.app.CivicCenterAndProfileGuardTest.database and drawer expose civic center and future ready profile: java.lang.AssertionError
FAIL ir.kharjyar.app.CompleteBackupGuardTest.backup includes all financial personal civic and private image data but excludes templates: java.lang.AssertionError
FAIL ir.kharjyar.app.ListFirstBackNavigationGuardTest.registration subpages consume first back and reveal their own list: java.lang.AssertionError: DebtsScreen.kt
FAIL ir.kharjyar.app.BackupScopeTest.theme is excluded from backup payload: java.lang.AssertionError: تم نباید در بکاپ ذخیره شود
FAIL ir.kharjyar.app.AssetProfessionalUiGuardTest.assets have graphical portfolio and swipe edit delete: java.lang.AssertionError
```

## نسخه انتشار
- حجم: 31M
- کلید اختصاصی: false
- امضا: معتبر
