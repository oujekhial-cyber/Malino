# گزارش Build و تست (CI)

- commit: 6675b9d2cc2aaa88a1ee9039be602d7bcde8adfc
- تاریخ: 2026-09-30 11:45 UTC
- unit tests: failure
- assembleDebug: success
- بررسی حریم خصوصی (مانیفست ادغام‌شده): success
- lintDebug: success
- assembleRelease: success

## خلاصه تست‌های واحد
```
tests=441 failures=9 errors=0 skipped=0
FAIL ir.kharjyar.app.CivicCenterAndProfileGuardTest.database and drawer expose civic center and future ready profile: java.lang.AssertionError
FAIL ir.kharjyar.app.LifeRemindersGuardTest.recurring life reminders are persisted scheduled and navigable: java.lang.AssertionError
FAIL ir.kharjyar.app.VehiclesAndOilGuardTest.vehicle services migrate and drawer exposes destination: java.lang.AssertionError
FAIL ir.kharjyar.app.ObligationsAndSmsHistoryGuardTest.database migration adds debts payments and checks: java.lang.AssertionError
FAIL ir.kharjyar.app.MarketPopupAndFreshDefaultsGuardTest.market pulse is centered in app header and uses slow dismissible popup: java.lang.AssertionError: fillMaxSize().background(Color.Black.copy(alpha=.20f)).clickable{closePanel()}
FAIL ir.kharjyar.app.CashAccountIntegrationGuardTest.cash fund balance uses initial cash plus confirmed inflows and outflows: java.lang.AssertionError: expected: java.lang.Integer<1150000> but was: java.lang.Long<1150000>
FAIL ir.kharjyar.app.VehicleTypeOwnerAndMotorcyclePlateGuardTest.motorcycle plate has three over five digits and vehicle fields persist: java.lang.AssertionError
FAIL ir.kharjyar.app.SmsClassifierTest.common Iranian dynamic password variants are silently ignored: java.lang.AssertionError: رمز دوم یک‌بار مصرف: 987654 مبلغ خرید 3,500,000 ریال expected:<NON_FINANCIAL> but was:<FINANCIAL_LIKELY>
FAIL ir.kharjyar.app.SaveFeedbackAndReturnGuardTest.profile transaction and account saves confirm then pop when workflow is complete: java.lang.AssertionError
```

## نسخه انتشار
- حجم: 31M
- کلید اختصاصی: false
- امضا: معتبر
