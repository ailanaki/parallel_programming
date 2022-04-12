package stack;

import kotlinx.atomicfu.AtomicRef;

import java.util.concurrent.atomic.AtomicIntegerArray;

import java.util.concurrent.ThreadLocalRandom;

public class StackImpl implements Stack {
    private static class Node {
        final AtomicRef<Node> next;
        final int x;

        Node(int x, Node next) {
            this.next = new AtomicRef<>(next);
            this.x = x;
        }
    }


    // head pointer
    private AtomicRef<Node> head = new AtomicRef<>(null);
    private final int n = 10, one = Integer.MIN_VALUE + 1, two = Integer.MIN_VALUE + 2, cnt = 5;
    private AtomicIntegerArray elimination = new AtomicIntegerArray(n);
    private ThreadLocalRandom rand = ThreadLocalRandom.current();

    private int index = -1;

    public StackImpl() {
        for (int i = 0; i < n; i++) {
            elimination.set(i, one);
        }
    }

    @Override
    public void push(int x) {
        if (basicPush(x)) return;
        if (x == one || x == two) {
            basicPush(x);
            return;
        }
        int randInt = rand.nextInt(n);
        for (int i = 0; i < cnt; i++) {
            int ind = (randInt + i) % n;
            if (elimination.compareAndSet(ind, one, x)) {
                int j = 0;
                while (j != 1000 && elimination.get(ind) != two) {
                    j++;
                }
                if (elimination.compareAndSet(ind, x, one)) {
                    break;
                }
                slowPush(x);
                break;
            }
        }
        slowPush(x);
    }

    @Override
    public int pop() {
        Integer res = basicPop();
        if (res != null) return res;
        int randInt = rand.nextInt(n);
        for (int i = 0; i < cnt; i++) {
            int ind = (randInt + i) % n;
            res = elimination.get(ind);
            if (res != one && res != two) {
                if (elimination.compareAndSet(ind, res, two)) {
                    return res;
                }
            }
        }
        return slowPop();
    }

    private boolean basicPush(int x) {
        Node curHead = head.getValue();
        Node newHead = new Node(x, curHead);
        if (head.compareAndSet(curHead, newHead)) {
            return true;
        }
        return false;
    }

    private Integer basicPop() {
        Node curHead = head.getValue();
        if (curHead == null) return Integer.MIN_VALUE;
        if (head.compareAndSet(curHead, curHead.next.getValue())) {
            return curHead.x;
        }
        return null;
    }

    private void slowPush(int x) {
        while (!basicPush(x)) {
        }
        ;
    }

    private int slowPop() {
        Integer res = null;
        while (res == null) {
            res = basicPop();
        }
        ;
        return res;
    }
}
