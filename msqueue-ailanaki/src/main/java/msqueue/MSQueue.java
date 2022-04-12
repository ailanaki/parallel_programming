package msqueue;

import kotlinx.atomicfu.AtomicRef;

public class MSQueue implements Queue {
    private AtomicRef<Node> head;
    private AtomicRef<Node> tail;

    public MSQueue() {
        Node dummy = new Node(0);
        this.head = new AtomicRef<>(dummy);
        this.tail = new AtomicRef<>(dummy);
    }

    @Override
    public void enqueue(int x) {
        Node newTail = new Node(x);
        while (true) {
            Node curTail = tail.getValue();
            if (curTail.next.compareAndSet(null, newTail)) {
                tail.compareAndSet(curTail, newTail);
                return;
            } else {
                tail.compareAndSet(curTail, curTail.next.getValue());
            }
        }
    }

    @Override
    public int dequeue() {
        while (true) {
            Node curHead = head.getValue();
            Node curTail = tail.getValue();
            AtomicRef<Node> next = curHead.next;
            if (curHead == curTail){
                if (next.getValue() == null) return Integer.MIN_VALUE;
                tail.compareAndSet(curTail, next.getValue());
            }else if (head.compareAndSet(curHead, next.getValue())) {
                return next.getValue().x;
            }
        }
    }

    @Override
    public int peek() {
        while (true) {
            Node curHead = head.getValue();
            AtomicRef<Node> next = curHead.next;
            if (curHead == tail.getValue())
                return Integer.MIN_VALUE;
            if (head.getValue() == curHead) {
                return next.getValue().x;
            }
        }
    }

    private class Node {
        final int x;
        AtomicRef<Node> next = new AtomicRef<>(null);

        Node(int x) {
            this.x = x;
        }
    }
}