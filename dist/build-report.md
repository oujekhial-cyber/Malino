# گزارش Build و تست (CI)

- commit: 306856ece1eddb1fd8b8bcffa8c4d9b4f711ae5a
- تاریخ: 2026-10-18 15:04 UTC
- unit tests: failure
- assembleDebug: success
- بررسی حریم خصوصی (مانیفست ادغام‌شده): success
- lintDebug: success
- assembleRelease: success

## خلاصه تست‌های واحد
```
tests=551 failures=9 errors=0 skipped=0
FAIL ir.kharjyar.app.CivicCenterAndProfileGuardTest.database and drawer expose civic center and future ready profile: java.lang.AssertionError
FAIL ir.kharjyar.app.RecoverableSpamFilterTest.quarantineIsRestorableAndMigrated: java.lang.AssertionError
FAIL ir.kharjyar.app.LifeRemindersGuardTest.recurring life reminders are persisted scheduled and navigable: java.lang.AssertionError
FAIL ir.kharjyar.app.VehiclesAndOilGuardTest.vehicle services migrate and drawer exposes destination: java.lang.AssertionError
FAIL ir.kharjyar.app.ObligationsAndSmsHistoryGuardTest.database migration adds debts payments and checks: java.lang.AssertionError
FAIL ir.kharjyar.app.CashAccountIntegrationGuardTest.cash account is integrated into model backup migration and account management: java.lang.AssertionError
FAIL ir.kharjyar.app.VehicleTypeOwnerAndMotorcyclePlateGuardTest.motorcycle plate has three over five digits and vehicle fields persist: java.lang.AssertionError
FAIL ir.kharjyar.app.UtilityBillProfileMatcherTest.receiverAndDatabaseRequireProfileLink: java.lang.AssertionError
FAIL ir.kharjyar.app.BankAccountKindAndInterestGuardTest.accountFormAndMigrationPersistInterestRouting: java.lang.AssertionError
```

## نسخه انتشار
- حجم: 34M
- کلید اختصاصی: false
- امضا: معتبر
