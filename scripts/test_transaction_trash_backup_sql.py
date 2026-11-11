#!/usr/bin/env python3
"""Smoke-test the real transaction DAO's trash and backup queries using SQLite."""
import re
import sqlite3
from pathlib import Path

source = Path('app/src/main/java/ir/kharjyar/app/data/db/Daos.kt').read_text().split('interface TransactionDao {', 1)[1].split('interface TransferDao {', 1)[0]
def query_for(method):
    pattern = r'@Query\("([^"\n]+)"\)\s*(?:suspend )?fun ' + re.escape(method) + r'\('
    match = re.search(pattern, source)
    assert match, f'missing query for {method}'
    return match.group(1)

db = sqlite3.connect(':memory:')
db.execute('CREATE TABLE transactions (id INTEGER PRIMARY KEY, deletedAt INTEGER)')
db.executemany('INSERT INTO transactions VALUES (?, ?)', [(1, None), (2, 12345)])
assert db.execute(query_for('allOnce')).fetchall() == [(1, None)]
assert db.execute(query_for('allIncludingTrashOnce')).fetchall() == [(1, None), (2, 12345)]
db.execute(query_for('restoreFromTrash'), {'id': 2})
assert db.execute(query_for('allOnce')).fetchall() == [(1, None), (2, None)]
print('transaction trash backup queries: OK')
