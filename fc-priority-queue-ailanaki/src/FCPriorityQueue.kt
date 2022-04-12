import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.atomicArrayOfNulls
import java.util.*
import kotlin.random.Random

class FCPriorityQueue<E : Comparable<E>> {
    private val q = PriorityQueue<E>()
    private val locked = atomic(false)
    private val size = Runtime.getRuntime().availableProcessors();
    private val queries = atomicArrayOfNulls<Request<E>>(Runtime.getRuntime().availableProcessors())

    /**
     * Retrieves the element with the highest priority
     * and returns it as the result of this function;
     * returns `null` if the queue is empty.
     */
    fun poll(): E? {
        val curReq = Request{ q.poll() };
        request(curReq);
        return curReq.result;
    }

    /**
     * Returns the element with the highest priority
     * or `null` if the queue is empty.
     */
    fun peek(): E? {
        val curReq = Request{ q.peek() };
        request(curReq);
        return curReq.result;
    }

    /**
     * Adds the specified element to the queue.
     */
    fun add(element: E) {
        request(Request{
            q.add(element)
            null
        });
    }

    private fun request(request: Request<E>) : E? {
        var i = Random.nextInt(0,size);
        while(!queries[i].compareAndSet(null, request)){
            i++;
            i %= size;
        }
        while (true) {
            if (!locked.value && locked.compareAndSet(false,true)) {
                for (j in 0 until size) {
                    val curReq = queries[j].value;
                    if (curReq != null) {
                        curReq.result = curReq.op.invoke();
                        curReq.status = Status.FINISHED;
                        queries[j].compareAndSet(curReq, null);
                    }
                }
                locked.value = false;
                return request.result;
            } else {
                if (request.status == Status.FINISHED) {
                    return request.result
                }
            }
        }
    }

    private class Request<E>(val op:()->E?){
        var status = Status.WAIT;
        var result:E? = null;
    }
    enum class Status{
        WAIT, FINISHED
    }
}