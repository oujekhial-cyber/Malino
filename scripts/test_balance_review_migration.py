#!/usr/bin/env python3
"""Check the 27→28 SQL with SQLite; does not substitute for Room/device migration tests."""
from pathlib import Path
import re
import sqlite3

text = Path('app/src/main/java/ir/kharjyar/app/data/db/KharjYarDatabase.kt').read_text()
section = text.split('val MIGRATION_27_28', 1)[1].split('val MIGRATION_26_27', 1)[0]
statements = re.findall(r'db\.execSQL\("([^"]+)"\)', section)
assert len(statements) == 2
connection = sqlite3.connect(':memory:')
connection.execute('CREATE TABLE accounts (id INTEGER PRIMARY KEY, programBalanceOffsetRial INTEGER)')
connection.execute('INSERT INTO accounts VALUES (1, 500)')
for sql in statements:
    connection.execute(sql)
assert connection.execute('SELECT id, programBalanceOffsetRial, anchorReviewBalanceRial, anchorReviewAt FROM accounts').fetchone() == (1, 500, None, None)
print('balance review migration SQL: OK')
