# گزارش Build و تست (CI)

- commit: b92a5f1f81434d153479537c695040d08cf031c7
- تاریخ: 2026-10-20 13:53 UTC
- unit tests: failure
- assembleDebug: success
- بررسی حریم خصوصی (مانیفست ادغام‌شده): success
- lintDebug: success
- assembleRelease: success

## خلاصه تست‌های واحد
```
tests=608 failures=15 errors=0 skipped=0
FAIL ir.kharjyar.app.AutomaticLoanCalculationTest.form persists rate term total and migration: java.lang.AssertionError: version = 22
FAIL ir.kharjyar.app.CivicCenterAndProfileGuardTest.database and drawer expose civic center and future ready profile: java.lang.AssertionError
FAIL ir.kharjyar.app.RecoverableSpamFilterTest.quarantineIsRestorableAndMigrated: java.lang.AssertionError
FAIL ir.kharjyar.app.LifeRemindersGuardTest.recurring life reminders are persisted scheduled and navigable: java.lang.AssertionError
FAIL ir.kharjyar.app.VehiclesAndOilGuardTest.vehicle services migrate and drawer exposes destination: java.lang.AssertionError
FAIL ir.kharjyar.app.ObligationsAndSmsHistoryGuardTest.database migration adds debts payments and checks: java.lang.AssertionError
FAIL ir.kharjyar.app.CashAccountIntegrationGuardTest.cash account is integrated into model backup migration and account management: java.lang.AssertionError
FAIL ir.kharjyar.app.VehicleTypeOwnerAndMotorcyclePlateGuardTest.motorcycle plate has three over five digits and vehicle fields persist: java.lang.AssertionError
FAIL ir.kharjyar.app.SmartCheckAndVoicePauseTest.spoken check date bank and party are extracted locally: org.junit.ComparisonFailure: expected:<1405/08/[12]> but was:<1405/08/[01]>
FAIL ir.kharjyar.app.UtilityBillProfileMatcherTest.receiverAndDatabaseRequireProfileLink: java.lang.AssertionError
FAIL ir.kharjyar.app.ConfirmedSmartMutationTest.resolver selects unique transaction from amount and account: java.lang.AssertionError: expected: java.lang.Integer<10> but was: java.lang.Long<10>
FAIL ir.kharjyar.app.PaidInstallmentEditDeleteGuardTest.paid installments can be edited deleted or exempted from overdue warning: java.lang.AssertionError: version = 22
FAIL ir.kharjyar.app.BankAccountKindAndInterestGuardTest.accountFormAndMigrationPersistInterestRouting: java.lang.AssertionError
FAIL ir.kharjyar.app.FlexibleInstallmentPaymentGuardTest.installment payment accepts custom principal late fee and note: java.lang.AssertionError: version = 22
FAIL ir.kharjyar.app.ReminderModernHeroGuardTest.listAndEntryUseModernSummaryHeroes: java.lang.AssertionError
```

## نسخه انتشار
- حجم: 35M
- کلید اختصاصی: false
- امضا: معتبر
