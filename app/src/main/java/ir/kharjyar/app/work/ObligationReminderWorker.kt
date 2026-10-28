package ir.kharjyar.app.work
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ir.kharjyar.app.KharjYarApp
import ir.kharjyar.app.core.money.Money
import ir.kharjyar.app.core.money.MoneyUnit
import ir.kharjyar.app.data.db.*
import ir.kharjyar.app.notify.Notifier
class ObligationReminderWorker(context:Context,params:WorkerParameters):CoroutineWorker(context,params){
 override suspend fun doWork():Result{
  val app=applicationContext as KharjYarApp;val now=System.currentTimeMillis()
  // پاک‌سازی دفاعی برای داده‌های تسویه‌شده از نسخه‌های قبلی، بازیابی بکاپ یا race احتمالی UI/Worker.
  app.database.debtDao().allDebtsOnce().filter{it.settled}.forEach{Notifier.cancelDebtReminder(applicationContext,it.id)}
  app.database.checkDao().allOnce().filter{it.status!=CheckStatus.PENDING}.forEach{ObligationReminderCleanup.cancelCheck(applicationContext,app.database,it)}
  val debts=app.database.debtDao().dueReminders(now);val checks=app.database.checkDao().dueReminders(now);val installments=app.database.loanDao().due(now).filterNot{it.noInstallment};val loans=app.database.loanDao().allLoansOnce().associateBy{it.id};val people=app.database.debtDao().allPeopleOnce().associateBy{it.id}
  app.database.reminderDao().due(now).forEach{LifeReminderWorker.schedule(applicationContext,it.id,now)}
  if(android.os.Build.VERSION.SDK_INT>=33&&ContextCompat.checkSelfPermission(applicationContext,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return Result.success();val manager=NotificationManagerCompat.from(applicationContext)
  debts.forEach{d->val relation=if(d.kind==DebtKind.RECEIVABLE)"طلب" else "بدهی";manager.notify(Notifier.TAG_DEBT_REMINDER,d.id.toInt(),notification("موعد $relation ${people[d.personId]?.name.orEmpty()}","${d.title} • ${Money.format(d.amountRial,MoneyUnit.RIAL)}","debts/${d.id}",(21000+d.id).toInt()))}
  checks.forEach{c->manager.notify(Notifier.TAG_CHECK_REMINDER,c.id.toInt(),notification(if(c.direction==CheckDirection.ISSUED)"موعد پرداخت چک" else "موعد وصول چک","${c.counterparty} • ${Money.format(c.amountRial,MoneyUnit.RIAL)}","checks/${c.id}",(31000+c.id).toInt()))}
  installments.forEach{i->manager.notify(Notifier.TAG_INSTALLMENT_REMINDER,i.id.toInt(),notification("موعد پرداخت قسط ${i.number}","${loans[i.loanId]?.title.orEmpty()} • ${Money.format(i.amountRial,MoneyUnit.RIAL)}","loans/${i.loanId}/${i.id}",(41000+i.id).toInt()))}
  return Result.success()
 }
 private fun notification(title:String,text:String,route:String,code:Int)=NotificationCompat.Builder(applicationContext,KharjYarApp.CHANNEL_REVIEW).setSmallIcon(ir.kharjyar.app.R.drawable.ic_notification_kharjyar).setColor(ContextCompat.getColor(applicationContext,ir.kharjyar.app.R.color.notification_accent)).setContentTitle(title).setContentText(text).setStyle(NotificationCompat.BigTextStyle().bigText(text)).setCategory(NotificationCompat.CATEGORY_REMINDER).setPriority(NotificationCompat.PRIORITY_HIGH).setContentIntent(Notifier.routePendingIntent(applicationContext,code,route)).setOnlyAlertOnce(true).setAutoCancel(true).build()
}
