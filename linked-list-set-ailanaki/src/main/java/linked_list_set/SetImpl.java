package linked_list_set;

import kotlinx.atomicfu.AtomicRef;


public class SetImpl implements Set {
    private interface NodeIntf{}


    private static class Node implements NodeIntf{
        AtomicRef<NodeIntf> next;
        int x;

        Node(int x, NodeIntf next) {
            this.next = new AtomicRef<>(next);
            this.x = x;
        }
    }

    private static class Removed implements NodeIntf{
       final Node next;

        private Removed(Node next) {
            this.next = next;
        }
    }

    private static class Window {
        Node cur, next;
    }

    private final Node head = new Node(Integer.MIN_VALUE, new Node(Integer.MAX_VALUE, null));

    /**
     * Returns the {@link Window}, where cur.x < x <= next.x
     */
    private Window findWindow(int x) {
        retry:
        while (true){
            Window w = new Window();
            w.cur = head;
            w.next = (Node) w.cur.next.getValue();
            while (w.next.x < x) {
                NodeIntf node = w.next.next.getValue();
                if (node instanceof Removed){
                    if (!w.cur.next.compareAndSet(w.next,((Removed) node).next)){
                        continue retry;
                    }else{
                        w.next = ((Removed) node).next;
                    }
                }else{
                    w.cur = w.next;
                    w.next = (Node) node;
                }
            }
            return w;
        }
    }

    @Override
    public boolean add(int x) {
        while(true){
            Window w = findWindow(x);
            if ((w.next.next.getValue() instanceof Node) && w.next.x == x) {
                return false;
            }
            Node node = new Node(x, w.next);
            if (w.cur.next.compareAndSet(w.next,node)){
                return true;
            }
        }

    }

    @Override
    public boolean remove(int x) {
        while(true){
            Window w = findWindow(x);
            if (w.next.x != x) {
                return false;
            }
            NodeIntf node = w.next.next.getValue();
            if (node instanceof Removed){
                return false;
            }
            Removed removed = new Removed((Node) node);
            if (w.next.next.compareAndSet(node,removed)){
                w.cur.next.compareAndSet(w.next,node);
                return true;
            }
        }

    }

    @Override
    public boolean contains(int x) {
        Window w = findWindow(x);
        return (w.next.next.getValue() instanceof Node) && w.next.x == x;
    }
}