from pathlib import Path
import sqlite3
from datetime import datetime, timedelta

class DB:
    def __init__(self, root: Path):
        self.path = root / "malino.db"
        self.path.parent.mkdir(parents=True, exist_ok=True)
        self.init()

    def cx(self):
        c = sqlite3.connect(self.path)
        c.row_factory = sqlite3.Row
        return c

    def init(self):
        with self.cx() as c:
            c.execute('''CREATE TABLE IF NOT EXISTS transactions(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                amount INTEGER NOT NULL,
                tx_type TEXT NOT NULL,
                bank TEXT NOT NULL,
                description TEXT DEFAULT '',
                balance_after INTEGER,
                timestamp TEXT NOT NULL,
                raw_sms TEXT DEFAULT '',
                sms_id TEXT DEFAULT ''
            )''')
            c.execute('''CREATE TABLE IF NOT EXISTS banks(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT UNIQUE NOT NULL,
                sample_sms TEXT DEFAULT '',
                learned INTEGER DEFAULT 0,
                rules TEXT DEFAULT ''
            )''')
            # Upgrade databases created by v1/v2.
            cols = {r[1] for r in c.execute('PRAGMA table_info(banks)').fetchall()}
            if 'rules' not in cols:
                c.execute("ALTER TABLE banks ADD COLUMN rules TEXT DEFAULT ''")
            c.execute('''CREATE TABLE IF NOT EXISTS settings(
                key TEXT PRIMARY KEY,
                value TEXT NOT NULL
            )''')

    def rows(self):
        with self.cx() as c:
            return [dict(x) for x in c.execute(
                'SELECT * FROM transactions ORDER BY timestamp DESC').fetchall()]

    def add(self, **kw):
        with self.cx() as c:
            c.execute('''INSERT INTO transactions(
                amount,tx_type,bank,description,balance_after,timestamp,raw_sms,sms_id
            ) VALUES(?,?,?,?,?,?,?,?)''', (
                kw['amount'], kw['tx_type'], kw.get('bank', 'بانک دیگر'),
                kw.get('description', ''), kw.get('balance_after'),
                kw.get('timestamp', datetime.now().isoformat(timespec='seconds')),
                kw.get('raw_sms', ''), kw.get('sms_id', '')
            ))

    def update(self, id, **kw):
        with self.cx() as c:
            c.execute('''UPDATE transactions
                         SET amount=?,tx_type=?,bank=?,description=?
                         WHERE id=?''', (
                kw['amount'], kw['tx_type'], kw.get('bank', 'بانک دیگر'),
                kw.get('description', ''), id
            ))

    def delete(self, id):
        with self.cx() as c:
            c.execute('DELETE FROM transactions WHERE id=?', (id,))

    def setting(self, k, d):
        with self.cx() as c:
            r = c.execute('SELECT value FROM settings WHERE key=?', (k,)).fetchone()
            return r['value'] if r else d

    def set_setting(self, k, v):
        with self.cx() as c:
            c.execute('''INSERT INTO settings(key,value) VALUES(?,?)
                         ON CONFLICT(key) DO UPDATE SET value=excluded.value''', (k, v))

    def banks(self):
        with self.cx() as c:
            return [r['name'] for r in c.execute('SELECT name FROM banks ORDER BY name').fetchall()]

    def bank_rules(self):
        with self.cx() as c:
            return [dict(r) for r in c.execute(
                'SELECT name,sample_sms,learned,rules FROM banks').fetchall()]

    def upsert_bank(self, name, sms='', learned=0, rules=''):
        with self.cx() as c:
            c.execute('''INSERT INTO banks(name,sample_sms,learned,rules)
                         VALUES(?,?,?,?)
                         ON CONFLICT(name) DO UPDATE SET
                           sample_sms=excluded.sample_sms,
                           learned=excluded.learned,
                           rules=excluded.rules''',
                      (name, sms, learned, rules))

    def stats(self):
        now = datetime.now()
        today = now.date().isoformat()
        week_start = (now - timedelta(days=now.weekday())).date().isoformat()
        month_start = now.replace(day=1).date().isoformat()
        with self.cx() as c:
            rows = [dict(r) for r in c.execute(
                'SELECT amount,tx_type,timestamp FROM transactions').fetchall()]
        def calc(start):
            ins = outs = 0
            for r in rows:
                if r['timestamp'][:10] >= start:
                    if r['tx_type'] == 'IN': ins += int(r['amount'])
                    else: outs += int(r['amount'])
            return ins, outs
        total_in = sum(int(r['amount']) for r in rows if r['tx_type'] == 'IN')
        total_out = sum(int(r['amount']) for r in rows if r['tx_type'] == 'OUT')
        di, do = calc(today)
        wi, wo = calc(week_start)
        mi, mo = calc(month_start)
        return {
            'totalIn': total_in, 'totalOut': total_out, 'net': total_in-total_out,
            'todayIn': di, 'todayOut': do, 'todayNet': di-do,
            'weekIn': wi, 'weekOut': wo, 'weekNet': wi-wo,
            'monthIn': mi, 'monthOut': mo, 'monthNet': mi-mo,
            'count': len(rows)
        }
