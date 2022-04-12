package dijkstra

import kotlinx.atomicfu.atomic
import java.util.*
import java.util.concurrent.Phaser
import java.util.concurrent.locks.ReentrantLock
import kotlin.Comparator
import kotlin.concurrent.thread

private val NODE_DISTANCE_COMPARATOR = Comparator<Node> { o1, o2 -> Integer.compare(o1!!.distance, o2!!.distance) }

// Returns `Integer.MAX_VALUE` if a path has not been found.
val counter = atomic(1);
fun shortestPathParallel(start: Node) {
    val workers = Runtime.getRuntime().availableProcessors()
    // The distance to the start node is `0`
    start.distance = 0
    // Create a priority (by distance) queue and add the start node into it
    val q = PriorityMultiQueue(workers, NODE_DISTANCE_COMPARATOR)
    q.add(start)
    // Run worker threads and wait until the total work is done
    val onFinish = Phaser(workers + 1) // `arrive()` should be invoked at the end by each worker
    counter.lazySet(1);
    repeat(workers) {
        thread {
            while (true) {
                val cur: Node = q.poll() ?: if (counter.compareAndSet(0,0)) break else continue;
                for (e in cur.outgoingEdges) {
                    val curDist = cur.distance + e.weight;
                    while (true){
                        val dist = e.to.distance;
                        if (curDist >= dist){
                            break;
                        }
                        if (e.to.casDistance(dist, curDist)) {
                            q.add(e.to);
                            counter.incrementAndGet();
                            break;
                        }
                    }

                }
                counter.decrementAndGet();
            }
            onFinish.arrive()
        }
    }
    onFinish.arriveAndAwaitAdvance()
}
class PriorityMultiQueue<V>(T: Int, private val comparator: Comparator<V>){

    private class BlockedQueue<V>(capacity: Int, comparator: Comparator<V>){
        private val lock = ReentrantLock();
        private val q: PriorityQueue<V> = PriorityQueue(capacity,comparator);

        fun getLock(): ReentrantLock{
            return lock;
        }

        fun getQueue(): PriorityQueue<V>{
            return q;
        }

    }
    private val count = 2 * T;
    private val queues = List(count) {BlockedQueue(T, comparator)}
    private val random = Random();

    fun add(elem : V):Boolean{
         while(true){
             val q = queues[random.nextInt(count)];
             if (q.getLock().tryLock()){
                 try{
                     q.getQueue().add(elem);
                     return true;
                 }finally {
                     q.getLock().unlock();
                 }
             }
         }
    }

    fun poll(): V?{
        retry@ while(true){
            val q1 = queues[random.nextInt(count)];
            val q2 = queues[random.nextInt(count)];
            if (q1.getLock().tryLock()){
                try{
                    if (q2.getLock().tryLock()){
                        try{
                            val elem1 = q1.getQueue().peek();
                            val elem2 = q2.getQueue().peek();
                            if (elem1 == elem2 && elem1 == null){
                                break@retry;
                            }
                            if (elem1 == null){
                                return q2.getQueue().poll();
                            }
                            if(elem2 == null){
                                return q1.getQueue().poll();
                            }
                            return if (comparator.compare(elem1,elem2) < 0){
                                q1.getQueue().poll();
                            }else{
                                q2.getQueue().poll();
                            }
                        }finally {
                            q2.getLock().unlock();
                        }
                    }
                }finally {
                    q1.getLock().unlock();
                }
            }
        }
        return null;
    }
}