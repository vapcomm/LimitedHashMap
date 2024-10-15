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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LimitedHashMapTest {

    /**
     * Smoke test
     */
    @Test
    fun emptyMap() {
        val map = LimitedHashMap<String, String>(3)
        assertNull(map["one"])
        assertEquals("", map.popListToString())
    }

    /**
     * Check for removing of the least popular key during insertion of a new one
     */
    @Test
    fun insert() {
        val map = LimitedHashMap<String, String>(3)

        map.insert("one", "1")
        assertEquals("one:1", map.popListToString())
        assertEquals(1, map.size)

        map.insert("two", "2")
        assertEquals("two:2,one:1", map.popListToString())
        assertEquals(2, map.size)

        assertEquals("1", map["one"])
        assertEquals("2", map["two"])

        map.insert("three", "3")
        assertEquals("three:3,two:2,one:1", map.popListToString())
        assertEquals(3, map.size)

        map.insert("four", "4")
        assertEquals("four:4,three:3,two:2", map.popListToString())
        assertEquals(3, map.size)

        // "one" was the least popular, and removed during "four" insertion
        assertNull(map["one"])
        assertEquals("2", map["two"])
        assertEquals("3", map["three"])
        assertEquals("4", map["four"])

        assertEquals("[two, three, four]", map.keys.toString())
        assertEquals("[2, 3, 4]", map.values.toString())
    }

    /**
     * Check all cases of PopList's changes
     */
    @Test
    fun get() {
        val map = LimitedHashMap<String, String>(3)

        map.insert("one", "1")
        map.insert("two", "2")
        map.insert("three", "3")
        assertEquals("three:3,two:2,one:1", map.popListToString())

        // last node
        assertEquals("1", map["one"])
        assertEquals("one:1,three:3,two:2", map.popListToString())

        // middle node
        assertEquals("3", map["three"])
        assertEquals("three:3,one:1,two:2", map.popListToString())

        // first node, nothing changed
        assertEquals("3", map["three"])
        assertEquals("three:3,one:1,two:2", map.popListToString())
    }

    @Test
    fun clear() {
        val map = LimitedHashMap<String, String>(3)
        map.insert("one", "1")
        map.insert("two", "2")
        map.insert("three", "3")
        assertEquals("three:3,two:2,one:1", map.popListToString())
        assertEquals(3, map.size)
        map.clear()
        assertEquals("", map.popListToString())
        assertEquals(0, map.size)
    }

    @Test
    fun remove() {
        val map = LimitedHashMap<String, String>(4)
        map.insert("one", "1")
        map.insert("two", "2")
        map.insert("three", "3")
        map.insert("four", "4")
        assertEquals("four:4,three:3,two:2,one:1", map.popListToString())
        assertEquals(4, map.size)

        map.remove("unknown") // nothing removed
        assertEquals("four:4,three:3,two:2,one:1", map.popListToString())
        assertEquals(4, map.size)

        map.remove("two")   // middle element
        assertEquals("four:4,three:3,one:1", map.popListToString())
        assertEquals(3, map.size)

        map.remove("one")   // tail element
        assertEquals("four:4,three:3", map.popListToString())
        assertEquals(2, map.size)

        map.remove("four")   // head element
        assertEquals("three:3", map.popListToString())
        assertEquals(1, map.size)

        map.remove("three")  // last element
        assertEquals("", map.popListToString())
        assertEquals(0, map.size)

        map.remove("three")  // last element again

        // and everything still works fine
        map.insert("five", "5")
        assertEquals("five:5", map.popListToString())
        assertEquals(1, map.size)
    }

}
