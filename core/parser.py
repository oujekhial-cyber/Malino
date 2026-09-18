import json
import re

BANKS = {
    "بانک توسعه تعاون":["توسعه تعاون","TTBANK","TOSE TAAVON"],
    "بانک ملی ایران":["بانک ملی","BMI","MELLI"],
    "بانک ملت":["بانک ملت","MELLAT"],
    "بانک صادرات ایران":["بانک صادرات","SADERAT"],
    "بانک تجارت":["بانک تجارت","TEJARAT"],
    "بانک رفاه کارگران":["بانک رفاه","REFAH"],
    "بانک کشاورزی":["بانک کشاورزی","KESHAAVARZI"],
    "بانک مسکن":["بانک مسکن","MASKAN"],
    "بانک پارسیان":["پارسیان","PARSIAN"],
    "بانک پاسارگاد":["پاسارگاد","PASARGAD"],
    "بانک سامان":["بانک سامان","SAMAN"],
    "بانک اقتصاد نوین":["اقتصاد نوین","ENBANK"],
    "بانک دی":["بانک دی","DAYBANK"],
    "بانک آینده":["بانک آینده","AYANDEH"],
    "بانک گردشگری":["بانک گردشگری","TOURISM"],
    "بانک شهر":["بانک شهر","CITYBANK"],
    "بانک سپه":["بانک سپه","SEPAH"],
    "بانک قرض‌الحسنه مهر ایران":["مهر ایران","MEHR IRAN"],
    "بانک قرض‌الحسنه رسالت":["رسالت","RESALAT"],
    "بانک خاورمیانه":["خاورمیانه","MIDDLE EAST BANK"],
    "بانک کارآفرین":["کارآفرین","KARAFARIN"],
    "بانک ایران‌زمین":["ایران زمین","IRAN ZAMIN"],
    "بانک سینا":["بانک سینا","SINA"],
    "بانک سرمایه":["بانک سرمایه","SARMAYEH"],
    "بلوبانک":["بلوبانک","BLUEBANK"],
    "بانکینو":["بانکینو","BANKINO"],
}
IN=('واریز','واریزی','بستانکار','دریافت','بستان')
OUT=('برداشت','برداشتی','خرید','بدهکار','پرداخت','خروج')

_DIGITS = str.maketrans('۰۱۲۳۴۵۶۷۸۹٠١٢٣٤٥٦٧٨٩','01234567890123456789')
def norm(s):
    return (s or '').translate(_DIGITS)

def nums(s):
    out=[]
    for x in re.findall(r'(?<!\d)\d[\d,\.\s]{2,}(?!\d)', norm(s)):
        d=re.sub(r'\D','',x)
        if 3<=len(d)<=15:
            out.append(int(d))
    return out

def _extract_after(text, anchors, limit=90):
    pattern = r'(?:' + '|'.join(re.escape(a) for a in anchors) + r')[^0-9]{0,' + str(limit) + r'}([0-9][0-9,\.\s]{2,})'
    m=re.search(pattern, text, re.I)
    if not m: return None
    d=re.sub(r'\D','',m.group(1))
    return int(d) if 3<=len(d)<=15 else None

def infer_rules(sample):
    s=norm(sample)
    rules={}
    if re.search(r'(مبلغ|واریز|واریزی|برداشت|خرید|بدهکار|بستانکار|پرداخت)',s,re.I):
        rules['amount_anchors']=['مبلغ','واریز','واریزی','برداشت','خرید','بدهکار','بستانکار','پرداخت']
    if re.search(r'(موجودی|مانده|balance)',s,re.I):
        rules['balance_anchors']=['موجودی','مانده','balance']
    return json.dumps(rules,ensure_ascii=False)

def parse(sender, body, learned_banks=None):
    learned_banks = learned_banks or []
    b=norm(body)
    t=(sender+' '+b)
    upper=t.upper()
    typ='IN' if any(x.upper() in upper for x in IN) else 'OUT' if any(x.upper() in upper for x in OUT) else 'IN'
    bank='بانک دیگر'
    bank_rule={}
    for name,keys in BANKS.items():
        if any(k.upper() in upper for k in keys): bank=name; break
    if bank=='بانک دیگر':
        for item in learned_banks:
            name=item.get('name','')
            if name and name in t:
                bank=name
                try: bank_rule=json.loads(item.get('rules') or '{}')
                except Exception: bank_rule={}
                break
    amount=None
    anchors=bank_rule.get('amount_anchors') if bank_rule else None
    if anchors:
        amount=_extract_after(b,anchors)
    if amount is None:
        amount=_extract_after(b,['مبلغ','واریز','واریزی','برداشت','خرید','بدهکار','بستانکار','پرداخت'])
    allnums=nums(b)
    if amount is None and allnums:
        # Prefer values that are not obvious dates/card/account fragments.
        candidates=[n for n in allnums if n>=1000 and len(str(n)) not in (8,10,16)]
        amount=candidates[0] if candidates else allnums[0]
    balance=None
    anchors=bank_rule.get('balance_anchors') if bank_rule else None
    if anchors: balance=_extract_after(b,anchors)
    if balance is None: balance=_extract_after(b,['موجودی','مانده','BALANCE'])
    return {'amount':amount or 0,'type':typ,'bank':bank,'balance':balance,'raw':b}
