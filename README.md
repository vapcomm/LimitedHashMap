# LimitedHashMap

Hashmap with limited capacity. Suitable for fast "in memory" caches.
If on new value insertion maximum capacity is reached, the least popular key/value pair will be removed from hashmap to free place for a new one.

Custom doubly linked list is used to sort the least used values, it is fast for rearranging values and removing value in list's tail. 

## Usage

Simply add `LimitedHashMap.kt` to your main source tree and `LimitedHashMapTest.kt` to your tests,
or use the whole project as a KMM library for JVM or Kotlin Native.

#### Example
```kotlin
val map = LimitedHashMap<String, String>(3)

map.insert("one", "1")
map.insert("two", "2")
map.insert("three", "3")
map.insert("four", "4")     // this will pop the first value "one -> 1" from the hashmap

println("keys: ${map.keys}")
println("values: ${map.values}")
```

Will print:
```
keys: [two, three, four]
values: [2, 3, 4]
```

## License

MIT license is used, feel free to do what you need with LimitedHashMap class and its tests.
