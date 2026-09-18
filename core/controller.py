from pathlib import Path
from PySide6.QtCore import QObject, Signal, Slot, Property, QTimer, QStandardPaths
from .db import DB
from .banks import BANKS
from .parser import parse, infer_rules

class AppController(QObject):
    changed=Signal(); currencyChanged=Signal(); themeChanged=Signal(); pendingChanged=Signal(); statsChanged=Signal(); toast=Signal(str)
    def __init__(self,root):
        super().__init__()
        self.db=DB(root/'data')
        self._currency=self.db.setting('currency','تومان')
        self._theme=self.db.setting('theme','light')
        self._pending={}
        self._pending_path=Path(QStandardPaths.writableLocation(QStandardPaths.AppDataLocation))/'pending_sms.txt'
        self._pending_path.parent.mkdir(parents=True,exist_ok=True)
        self._timer=QTimer(self); self._timer.timeout.connect(self._check_pending_file); self._timer.start(1200)

    @Property(str,notify=currencyChanged)
    def currency(self): return self._currency
    @Property(str,notify=themeChanged)
    def theme(self): return self._theme
    @Property(bool,notify=pendingChanged)
    def hasPending(self): return bool(self._pending)

    @Slot(result='QVariantList')
    def transactions(self): return self.db.rows()
    @Slot(result='QVariantList')
    def banks(self): return sorted(set(BANKS.keys() if isinstance(BANKS,dict) else BANKS) | set(self.db.banks()))
    @Slot(result='QVariantMap')
    def stats(self): return self.db.stats()
    @Slot(str)
    def setCurrency(self,v):
        if v in ('تومان','ریال'):
            self._currency=v; self.db.set_setting('currency',v); self.currencyChanged.emit(); self.changed.emit()
    @Slot(str)
    def setTheme(self,v):
        if v in ('light','midnight','neon'):
            self._theme=v; self.db.set_setting('theme',v); self.themeChanged.emit()

    @Slot(int,str,str,str)
    def addTransaction(self,amount,typ,bank,desc):
        if amount<=0: self.toast.emit('مبلغ را وارد کنید'); return
        self.db.add(amount=amount,tx_type=typ,bank=bank or 'بانک دیگر',description=desc); self.changed.emit(); self.statsChanged.emit(); self.toast.emit('تراکنش ثبت شد')
    @Slot(int,int,str,str,str)
    def updateTransaction(self,id,amount,typ,bank,desc):
        if amount<=0: self.toast.emit('مبلغ نامعتبر است'); return
        self.db.update(id,amount=amount,tx_type=typ,bank=bank or 'بانک دیگر',description=desc); self.changed.emit(); self.statsChanged.emit(); self.toast.emit('تراکنش ویرایش شد')
    @Slot(int)
    def deleteTransaction(self,id): self.db.delete(id); self.changed.emit(); self.statsChanged.emit(); self.toast.emit('تراکنش حذف شد')

    def _check_pending_file(self):
        try:
            if not self._pending_path.exists(): return
            text=self._pending_path.read_text(encoding='utf-8')
            lines=text.split('\n',1)
            if len(lines)<2: return
            sender,body=lines[0],lines[1]
            p=parse(sender,body,self.db.bank_rules()); p['sms_id']='android-sms'
            if p.get('amount',0)>0:
                self._pending=p; self.pendingChanged.emit(); self._pending_path.unlink(missing_ok=True)
        except Exception as e:
            self.toast.emit('خطا در خواندن پیامک')

    @Slot(str,str,result='QVariantMap')
    def parseSms(self,sender,body): return parse(sender,body,self.db.bank_rules())
    @Slot(str)
    def addBank(self,name):
        name=name.strip()
        if not name: self.toast.emit('نام بانک را وارد کنید'); return
        self.db.upsert_bank(name,'',0,'{}'); self.changed.emit(); self.toast.emit('بانک اضافه شد')

    @Slot(str,str)
    def learnBank(self,name,sms):
        name=name.strip(); sms=sms.strip()
        if not name: self.toast.emit('نام بانک را وارد کنید'); return
        if not sms: self.toast.emit('نمونه پیامک را وارد کنید'); return
        self.db.upsert_bank(name,sms,1,infer_rules(sms)); self.changed.emit(); self.toast.emit('قالب پیامک بانک ذخیره شد')
    @Slot(str,str,str)
    def receiveSms(self,sender,body,sms_id=''):
        p=parse(sender,body,self.db.bank_rules()); p['sms_id']=sms_id; self._pending=p; self.pendingChanged.emit()
    @Slot()
    def clearPending(self): self._pending={}; self.pendingChanged.emit()
    @Slot(str)
    def savePending(self,desc):
        if not self._pending: return
        p=self._pending
        self.db.add(amount=p['amount'],tx_type=p['type'],bank=p['bank'],description=desc,balance_after=p.get('balance'),raw_sms=p.get('raw',''),sms_id=p.get('sms_id',''))
        self._pending={}; self.pendingChanged.emit(); self.changed.emit(); self.statsChanged.emit(); self.toast.emit('پیامک به تراکنش تبدیل شد')
