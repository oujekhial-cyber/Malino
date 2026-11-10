"""Exercise the actual Room SQL with SQLite without building an APK."""
import pathlib
import re
import sqlite3
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]
DEBT = (ROOT / 'app/src/main/java/ir/kharjyar/app/data/db/ObligationDaos.kt').read_text()
LOAN = (ROOT / 'app/src/main/java/ir/kharjyar/app/data/db/PlanningDaos.kt').read_text()

def sql(source, method):
    match = re.search(r'@Query\("([^"]+)"\)\s+(?:suspend\s+)?fun\s+' + method + r'\(', source)
    if not match:
        raise AssertionError('Missing DAO query: ' + method)
    return match.group(1)

class TrashSqlTest(unittest.TestCase):
    def setUp(self):
        self.db = sqlite3.connect(':memory:')
        self.db.executescript('''
            CREATE TABLE debt_people (id INTEGER PRIMARY KEY, name TEXT, deletedAt INTEGER);
            CREATE TABLE debts (id INTEGER PRIMARY KEY, personId INTEGER, settled INTEGER, dueAt INTEGER, deletedAt INTEGER);
            CREATE TABLE debt_payments (id INTEGER PRIMARY KEY, debtId INTEGER, paidAt INTEGER);
            CREATE TABLE loans (id INTEGER PRIMARY KEY, closed INTEGER, nextDueAt INTEGER, deletedAt INTEGER);
            CREATE TABLE loan_installments (id INTEGER PRIMARY KEY, loanId INTEGER, paid INTEGER, dueAt INTEGER);
            INSERT INTO debt_people VALUES (1,'a',NULL),(2,'b',NULL);
            INSERT INTO debts VALUES (1,1,0,1,NULL),(2,1,0,2,NULL),(3,2,0,3,NULL);
            INSERT INTO debt_payments VALUES (1,1,1),(2,2,2),(3,3,3);
            INSERT INTO loans VALUES (1,0,1,NULL),(2,0,1,NULL);
            INSERT INTO loan_installments VALUES (1,1,0,1),(2,2,0,1);
        ''')
    def run_sql(self, source, method, **params):
        return self.db.execute(sql(source, method), params).fetchall()
    def test_individual_and_person_trash_restore_and_delete(self):
        self.run_sql(DEBT, 'trashDebt', id=2, at=100)
        self.assertEqual([r[0] for r in self.run_sql(DEBT, 'observeDebts')], [1,3])
        self.run_sql(DEBT, 'trashPerson', id=1, at=200)
        self.assertEqual([r[0] for r in self.run_sql(DEBT, 'observeDebts')], [3])
        self.assertEqual([r[0] for r in self.run_sql(DEBT, 'observePayments')], [3])
        self.run_sql(DEBT, 'restorePerson', id=1)
        self.assertEqual([r[0] for r in self.run_sql(DEBT, 'observeDebts')], [1,3])
        self.assertEqual([r[0] for r in self.run_sql(DEBT, 'observeTrashDebts')], [2])
        self.run_sql(DEBT, 'trashPerson', id=1, at=300)
        self.run_sql(DEBT, 'deletePaymentsOfPerson', personId=1)
        self.run_sql(DEBT, 'deleteDebtsOfPerson', personId=1)
        self.run_sql(DEBT, 'deletePerson', personId=1)
        self.assertEqual(self.db.execute('SELECT id FROM debt_payments').fetchall(), [(3,)])
    def test_loan_children_hidden_until_restore_or_permanent_delete(self):
        self.run_sql(LOAN, 'trash', id=1, at=100)
        self.assertEqual([r[0] for r in self.run_sql(LOAN, 'observeInstallments')], [2])
        self.run_sql(LOAN, 'restore', id=1)
        self.assertEqual(len(self.run_sql(LOAN, 'observeInstallments')), 2)
        self.run_sql(LOAN, 'trash', id=1, at=100)
        self.assertEqual(len(self.run_sql(LOAN, 'expired', cutoff=99)), 0)
        self.assertEqual(len(self.run_sql(LOAN, 'expired', cutoff=100)), 1)
        self.run_sql(LOAN, 'deleteInstallments', loanId=1)
        self.run_sql(LOAN, 'deleteLoan', id=1)
        self.assertEqual(self.db.execute('SELECT id FROM loan_installments').fetchall(), [(2,)])

if __name__ == '__main__':
    unittest.main()
