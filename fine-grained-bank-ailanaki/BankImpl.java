import java.util.concurrent.locks.ReentrantLock;

/**
 * Bank implementation.
 *
 * <p>:TODO: This implementation has to be made thread-safe.
 *
 * @author :Yakupova Aisha
 */
public class BankImpl implements Bank {


    /**
     * An array of accounts by index.
     */
    private final Account[] accounts;

    /**
     * Creates new bank instance.
     *
     * @param n the number of accounts (numbered from 0 to n-1).
     */
    public BankImpl(int n) {
        accounts = new Account[n];
        for (int i = 0; i < n; i++) {
            accounts[i] = new Account();
        }
    }

    @Override
    public int getNumberOfAccounts() {
        return accounts.length;
    }

    /**
     * <p>:TODO: This method has to be made thread-safe.
     */
    @Override
    public long getAmount(int index) {
        lock(index);
        long amount = accounts[index].amount;
        unlock(index);
        return amount;
    }

    /**
     * <p>:TODO: This method has to be made thread-safe.
     */
    @Override
    public long getTotalAmount() {
        long sum = 0;
        for (Account account : accounts) {
            account.lock.lock();
        }
        for (Account account : accounts) {
            sum += account.amount;
            account.lock.unlock();
        }
        return sum;
    }

    /**
     * <p>:TODO: This method has to be made thread-safe.
     */
    @Override
    public long deposit(int index, long amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Invalid amount: " + amount);
        lock(index);
        Account account = accounts[index];
        if (amount > MAX_AMOUNT || account.amount + amount > MAX_AMOUNT) {
            unlock(index);
            throw new IllegalStateException("Overflow");
        }
        account.amount += amount;
        long result = account.amount;
        unlock(index);
        return (result);
    }

    /**
     * <p>:TODO: This method has to be made thread-safe.
     */
    @Override
    public long withdraw(int index, long amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Invalid amount: " + amount);
        lock(index);
        Account account = accounts[index];
        if (account.amount - amount < 0) {
            unlock(index);
            throw new IllegalStateException("Underflow");
        }
        account.amount -= amount;
        long result = account.amount;
        unlock(index);
        return result;
    }

    /**
     * <p>:TODO: This method has to be made thread-safe.
     */
    @Override
    public void transfer(int fromIndex, int toIndex, long amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Invalid amount: " + amount);
        if (fromIndex == toIndex)
            throw new IllegalArgumentException("fromIndex == toIndex");
        if (fromIndex < toIndex) {
            lock(fromIndex, toIndex);
        } else {
            lock(toIndex, fromIndex);
        }
        Account from = accounts[fromIndex];
        Account to = accounts[toIndex];
        if (amount > from.amount) {
            unlock(fromIndex, toIndex);
            throw new IllegalStateException("Underflow");
        } else if (amount > MAX_AMOUNT || to.amount + amount > MAX_AMOUNT) {
            unlock(fromIndex, toIndex);
            throw new IllegalStateException("Overflow");
        }
        from.amount -= amount;
        to.amount += amount;
        unlock(fromIndex, toIndex);
    }

    private void lock(int first, int second) {
        accounts[first].lock.lock();
        accounts[second].lock.lock();
    }

    private void unlock(int fromIndex, int toIndex) {
        accounts[toIndex].lock.unlock();
        accounts[fromIndex].lock.unlock();
    }

    private void lock(int index) {
        accounts[index].lock.lock();
    }

    private void unlock(int index) {
        accounts[index].lock.unlock();
    }

    /**
     * Private account data structure.
     */
    static class Account {
        /**
         * Amount of funds in this account.
         */
        ReentrantLock lock = new ReentrantLock();
        long amount;
    }
}
