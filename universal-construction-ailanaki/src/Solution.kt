/**
 * @author :Yakupova Aisha
 */
class Solution : AtomicCounter {
    // объявите здесь нужные вам поля
    private val head: Node = Node(0);
    private val tail: ThreadLocal<Node> = ThreadLocal.withInitial{head};


    override fun getAndAdd(x: Int): Int {
        while (true) {
            val result = tail.get().x;
            val node = Node(result + x);
            tail.set(tail.get().next.decide(node));
            if (tail.get() == node) {
                return result;
            }
        }
    }

    // вам наверняка потребуется дополнительный класс
    private class Node(val x: Int, val next: Consensus<Node> = Consensus<Node>()) {}
}
