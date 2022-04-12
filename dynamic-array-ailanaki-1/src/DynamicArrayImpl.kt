import kotlinx.atomicfu.*

class DynamicArrayImpl<E> : DynamicArray<E> {
    private val core = atomic(Core<E>(INITIAL_CAPACITY))
    private val rsize = atomic(0)

    override fun get(index: Int): E {
        if (index >= size) throw IllegalArgumentException();
        while (true) {
            val core = core.value;
            val value = core.getValue(index);
            if (value is Value<*>) {
                if (value.x != null) return value.x;
            } else {
                move(core)
            }
        }
    }

    override fun put(index: Int, element: E) {
        if (index >= size) throw IllegalArgumentException();
        while (true) {
            val core = core.value;
            val value = core.getValue(index);
            if (value is MovedValue<*>) {
                move(core)
            } else {
                if (core.getArray()[index].compareAndSet(value, Value(element))) return
            }

        }
    }

    override fun pushBack(element: E) {
        while (true) {
            val size = size;
            val core = core.value;
            if (core.capacity <= size) {
                move(core)
            } else {
                if (core.getArray()[size].compareAndSet(null, Value(element))) {
                    rsize.incrementAndGet();
                    return;
                }
            }
        }
    }

    override val size: Int
        get() {
            return rsize.value;
        }

    private fun move(core: Core<E>) {
        core.next.value ?: core.next.compareAndSet(null, Core(2 * core.capacity))
        val next = core.next.value ?: return
        for (i in 0 until core.capacity) {
            var value: Node<E?>
            value = core.getArray()[i].value as Node<E?>;
            while (value is Value<*> && !core.getArray()[i].compareAndSet(value, MovedValue(value.x))) {
                value = core.getArray()[i].value as Node<E?>;
            }
            next.getArray()[i].compareAndSet(null, Value(value.x));
        }
        this.core.compareAndSet(core, next)
    }
}

private class Core<E>(
    val capacity: Int,
) {
    private val array = atomicArrayOfNulls<Node<E?>>(capacity)
    val next: AtomicRef<Core<E>?> = atomic(null);
    fun getValue(index: Int): Node<E?>? {
        return array[index].value;
    }

    fun getArray(): AtomicArray<Node<E?>?> {
        return array;
    }
}

private open class Node<E>(val x: E)
private class Value<E>(x: E) : Node<E>(x);
private class MovedValue<E>(x: E) : Node<E>(x);

private const val INITIAL_CAPACITY = 1 // DO NOT CHANGE ME