import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicInteger

class Trie {
  private val root: Node = Node()

  val size
    get() = AtomicInteger().also { cnt -> blockingVisitEach { cnt.incrementAndGet() } }.get()

  val words
    get() = mutableListOf<String>().also { wordList -> blockingVisitEach { word -> wordList += word } }

  fun add(word: String) = root.add(word)

  fun blockingVisitEach(block: suspend (str: String) -> Unit) = runBlocking { root.visit("", block) }

  suspend fun suspendingVisitEach(block: suspend (str: String) -> Unit) = root.visit("", block)

  fun contains(word: String) = root.contains(word, "")

  override fun toString() = words.toString()

  companion object {
    fun trieOf(vararg elements: String) = Trie().apply { elements.forEach { add(it) } }
  }
}