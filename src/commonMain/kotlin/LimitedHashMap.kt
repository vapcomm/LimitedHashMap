/*
 * (c) VAP Communications Group, 2022
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package online.vapcom.lhm

/**
 * HashMap with a limited values count (limited size).
 * If on insert() maxLoad is reached, the least popular key/value
 * pair will be removed to free space for a new one.
 * @param maxLoad maximum number of stored key-value pairs
 */
class LimitedHashMap<K : Any, V : Any>(private val maxLoad: Int) {

    val size get() = storage.size
    val keys: MutableSet<K> get() = storage.keys
    val values: MutableCollection<V> get() = storage.values.map { it.value }.toMutableList()

    // NOTE: On low maxLoad values, we set hashmap's initial capacity to maxLoad
    //      to almost eliminate map's rehash.
    private val storage = HashMap<K, KeyNode>(if (maxLoad > 256) 256 else maxLoad)

    // popularity list, most popular key is on a head, least popular is on a tail
    private val popList = PopList()

    /**
     * Insert new value, limiting capacity by deleting the least popular value in a map
     */
    operator fun set(key: K, value: V) = insert(key, value)

    /**
     * Insert new value, limiting capacity by deleting the least popular value in a map
     */
    fun insert(key: K, value: V) {
        if (storage.size >= maxLoad) {
            // remove the least popular key from hashmap
            val node = popList.removeLast()
            if (node != null) {
                storage.remove(node.key)
            }
        }

        val node = KeyNode(key, value)
        storage[key] = node

        popList.moveToHead(node)
    }

    /**
     * Return value by key or null if key not found
     */
    operator fun get(key: K): V? {
        val node = storage[key]

        return if (node == null) {
            null
        } else {
            popList.moveToHead(node)
            node.value
        }
    }

    /**
     * Clear hash map, remove all data
     */
    fun clear() {
        popList.removeAll()
        storage.clear()
    }

    internal fun popListToString(): String {
        return popList.toString()
    }

    /**
     * Node of PopList
     */
    inner class KeyNode(val key: K, val value: V) {
        var prev: KeyNode? = null
        var next: KeyNode? = null
    }

    /**
     * Doubly linked list used to found out popularity of values in a hashmap
     */
    inner class PopList {
        private var head: KeyNode? = null
        private var tail: KeyNode? = null

        /**
         * Add a new node or move a node to a list's head.
         */
        fun moveToHead(node: KeyNode) {
            if (head == null) { // empty queue
                node.prev = null
                node.next = null
                head = node
                tail = node
            } else {
                // move node if it's not first
                if (head !== node) {
                    if (node.prev != null || node.next != null) {
                        remove(node)
                    }

                    val oldHead = head
                    node.prev = null
                    node.next = head
                    head = node
                    if (oldHead == null) {
                        tail = node
                    } else oldHead.prev = node
                }
            }
        }

        /**
         * Remove least popular node from list's tail.
         * @return removed node or null if a list is empty
         */
        fun removeLast(): KeyNode? {
            val nodeToRemove = tail
            if (tail == null) {
                return null
            }

            nodeToRemove?.let {
                remove(it)
            }

            return nodeToRemove
        }

        fun removeAll() {
            var lastNode: KeyNode? = removeLast()
            while (lastNode != null) {
                lastNode = removeLast()
            }
        }

        private fun remove(node: KeyNode) {
            val prev = node.prev
            val next = node.next

            if (prev == null) {
                head = next
            } else prev.next = node.next

            if (next == null) {
                tail = prev
            } else next.prev = prev

            node.prev = null
            node.next = null
        }

        override fun toString(): String {
            val sb = StringBuilder()
            var node = head
            while (node != null) {
                sb.append(node.key)
                sb.append(":")
                sb.append(node.value)
                if (node.next != null) sb.append(",")

                node = node.next
            }

            return sb.toString()
        }
    }
}
