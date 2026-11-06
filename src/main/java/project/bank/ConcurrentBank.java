package project.bank;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentBank {
    private final List<BankAccount> accounts = new ArrayList<>();
    private final Lock bankLock = new ReentrantLock();

    public BankAccount createAccount(double initialBalance) {
        bankLock.lock();
        try {
            BankAccount account = new BankAccount(initialBalance);
            accounts.add(account);
            return account;
        } finally {
            bankLock.unlock();
        }
    }

    public void transfer(BankAccount from, BankAccount to, double amount) {
        if (from.getId().equals(to.getId())) {
            return;
        }
        Lock firstLock = from.getId().compareTo(to.getId()) < 0 ? from.getLock() : to.getLock();
        Lock secondLock = firstLock == from.getLock() ? to.getLock() : from.getLock();

        firstLock.lock();
        secondLock.lock();
        try {
            if (from.getBalance() >= amount) {
                if (from.withdraw(amount)) {
                    to.deposit(amount);
                }
            }
        } finally {
            secondLock.unlock();
            firstLock.unlock();
        }
    }

    public double getTotalBalance() {
        bankLock.lock();
        try {
            for (BankAccount account : accounts) {
                account.getLock().lock();
            }

            double total = 0;
            for (BankAccount account : accounts) {
                total += account.getBalance();
            }

            return total;
        } finally {
            for (BankAccount account : accounts) {
                account.getLock().unlock();
            }
            bankLock.unlock();
        }
    }
}