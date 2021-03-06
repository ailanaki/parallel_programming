import kotlinx.atomicfu.*

class FAAQueue<T> {
    private val head: AtomicRef<Segment> // Head pointer, similarly to the Michael-Scott queue (but the first node is _not_ sentinel)
    private val tail: AtomicRef<Segment> // Tail pointer, similarly to the Michael-Scott queue

    init {
        val firstNode = Segment()
        head = atomic(firstNode)
        tail = atomic(firstNode)
    }

    /**
     * Adds the specified element [x] to the queue.
     */
    fun enqueue(x: T) {
        while (true){
            val tail =  this.tail.value;
            val enqIdx = tail.enqIdx.getAndIncrement();
            if (enqIdx >= SEGMENT_SIZE) {
                val newTail = Segment(x)
                if (tail.next.compareAndSet(null,newTail)){
                    this.tail.compareAndSet(tail,newTail);
                    return;
                }else{
                    tail.next.value?.let { this.tail.compareAndSet(tail, it) };
                }
            }else if(this.tail.value.elements[enqIdx].compareAndSet(null,x)){
                return;
            }
        }
    }

    /**
     * Retrieves the first element from the queue
     * and returns it; returns `null` if the queue
     * is empty.
     */
    fun dequeue(): T? {
        while (true) {
            val head = this.head.value;
            val deIdx = head.deqIdx.getAndIncrement();
            if (deIdx >= SEGMENT_SIZE) {
               val headNext = head.next.value ?: return null
                this.head.compareAndSet(head, headNext)
                continue;
            }
            val res = head.elements[deIdx].getAndSet(DONE) ?: continue
            return res as T?
        }
    }

    /**
     * Returns `true` if this queue is empty;
     * `false` otherwise.
     */
    val isEmpty: Boolean get() {
        while (true) {
            val cur = head.value;
            if (cur.isEmpty) {
                val next = cur.next.value ?: return true;
                head.compareAndSet(cur,next);
                continue
            } else {
                return false
            }
        }
    }
}

private class Segment {
    val next = atomic<Segment?>(null);
    val enqIdx = atomic(0) // index for the next enqueue operation
    val deqIdx = atomic(0) // index for the next dequeue operation
    val elements = atomicArrayOfNulls<Any>(SEGMENT_SIZE)

    constructor() // for the first segment creation

    constructor(x: Any?) { // each next new segment should be constructed with an element
        enqIdx.getAndSet(1);
        elements[0].getAndSet(x);
    }

    val isEmpty: Boolean get() = deqIdx.value >= enqIdx.value || deqIdx.value >= SEGMENT_SIZE

}

private val DONE = Any() // Marker for the "DONE" slot state; to avoid memory leaks
const val SEGMENT_SIZE = 2 // DO NOT CHANGE, IMPORTANT FOR TESTS

