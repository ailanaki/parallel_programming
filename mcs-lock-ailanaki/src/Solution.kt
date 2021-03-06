import java.util.concurrent.atomic.AtomicReference

class Solution(val env: Environment) : Lock<Solution.Node> {
    private val tail = AtomicReference<Node>(null);

    override fun lock(): Node {
        val my = Node() // сделали узел
        my.locked.value = true;
        val pred = tail.getAndSet(my);
        if (pred != null){
            pred.next.set(my);
            while (my.locked.value){
                env.park();
            }
        }
        return my // вернули узел
    }

    override fun unlock(node: Node) {
       while (node.next.get() == null){
            if (tail.compareAndSet(node, null)){
                return;
            }else{
                while(node.next.get() == null){
                    //pass
                }
            }
        }
        node.next.value.locked.value = false;
        env.unpark(node.next.value.thread);
    }

    class Node {
        val thread = Thread.currentThread() // запоминаем поток, которые создал узел
        val locked = AtomicReference<Boolean>(false);
        val next = AtomicReference<Node>(null);
    }
}