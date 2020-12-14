import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.runBlocking
import kotlin.time.measureTimedValue

object ListCompare {

  @JvmStatic
  fun main(args: Array<String>) {
    // val list1 = SlowList(listOf(2, 3, 4, 5, 6, 7))
    // val list2 = SlowList(listOf(2, 3, 4, 5, 6, 7))
    val list1 = SlowList(listOf("test", "of", "sentence", "trie", "data", "structure"))
    val list2 = SlowList(listOf("test", "of", "sentence", "trie", "data", "structure"))

    list1.visitEach { println(it) }

    println(measureTimedValue { listsAreTheSame1(list1, list2) })
    println(measureTimedValue { listsAreTheSame2(list1, list2) })
    println(measureTimedValue { listsAreTheSame3(list1, list2) })
  }

  class SlowList<T>(list: List<T>) : List<T> by list {
    fun elementAt(pos: Int): T {
      //Thread.sleep(1000)
      return this[pos]
    }

    fun visitEach(block: (T) -> Unit) = forEach { block.invoke(it) }
  }

  fun <T> listsAreTheSame1(list1: SlowList<T>, list2: SlowList<T>): Boolean {
    if (list1.size != list2.size)
      return false

    for ((index, item) in list1.withIndex())
      if (item != list2.elementAt(index))
        return false

    return true
  }

  fun <T> listsAreTheSame2(list1: SlowList<T>, list2: SlowList<T>) =
    if (list1.size != list2.size)
      false
    else
      list1
        .asSequence()
        .mapIndexed { index, item -> item == list2.elementAt(index) }
        .firstOrNull { !it } ?: true

  fun <T> listsAreTheSame3(list1: SlowList<T>, list2: SlowList<T>) =
    if (list1.size != list2.size)
      false
    else
      runBlocking {
        val f1 = flow { for (item in list1) emit(item) }
        val f2 = flow { for (item in list2) emit(item) }

        f1.zip(f2) { i, j -> i == j }.firstOrNull { !it } ?: true
      }
}