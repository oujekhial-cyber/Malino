#!/usr/bin/env python3
"""SQLite smoke test for recurrence schema and unique occurrence, not a Room migration test."""
from pathlib import Path
import re, sqlite3
src = Path('app/src/main/java/ir/kharjyar/app/data/db/KharjYarDatabase.kt').read_text()
section = src.split('val MIGRATION_28_29', 1)[1].split('val MIGRATION_27_28', 1)[0]
statements = re.findall(r'db\.execSQL\("([^"]+)"\)', section)
assert len(statements) == 3
con = sqlite3.connect(':memory:')
for sql in statements: con.execute(sql)
con.execute('INSERT INTO recurring_transactions (accountId, amountRial, direction, nature, categoryId, description, nextAt, anchorDay, enabled) VALUES (1,100,1,2,NULL,\'test\',123,1,1)')
con.execute('INSERT INTO recurring_occurrences (recurringId,dueAt,transactionId) VALUES (1,123,7)')
try:
    con.execute('INSERT INTO recurring_occurrences (recurringId,dueAt,transactionId) VALUES (1,123,8)')
except sqlite3.IntegrityError:
    pass
else:
    raise AssertionError('duplicate occurrence was accepted')
print('recurring migration and unique occurrence SQL: OK')
