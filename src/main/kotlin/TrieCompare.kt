import TrieCompare.Trie.Companion.trieOf
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.measureTimedValue


object TrieCompare {

  @JvmStatic
  fun main(args: Array<String>) {
    val trie1 = trieOf("test", "of", "sentence", "trie", "data", "structure")
    val trie2 = trieOf("test", "of", "sentence", "trie", "data", "structure")

    trie1.visitEach { println(it) }

    println(trie1.size)
    println(trie1.words)

    println(measureTimedValue { triesAreTheSame1(trie1, trie2) })
    println(measureTimedValue { triesAreTheSame2(trie1, trie2) })
    println(measureTimedValue { triesAreTheSame3(trie1, trie2) })
  }

  class Trie {
    private val root: Node = Node()

    val size
      get() = AtomicInteger().also { cnt -> visitEach { cnt.incrementAndGet() } }.get()

    val words
      get() = mutableListOf<String>().also { wordList -> visitEach { word -> wordList += word } }

    fun add(word: String) = root.add(word)

    fun visitEach(block: suspend (str: String) -> Unit) = runBlocking { root.visit("", block) }

    fun contains(word: String) = root.contains(word, "")

    override fun toString() = words.toString()

    companion object {
      fun trieOf(vararg elements: String) = Trie().apply { elements.forEach { add(it) } }
    }
  }

  class Node {
    private var isWord = false
    private val children = mutableMapOf<Char, Node>()

    fun add(word: String) {
      if (word.isNotEmpty())
        children.computeIfAbsent(word.first()) { Node() }.add(word.substring(1))
      else
        isWord = true
    }

    fun contains(wordToMatch: String, prefix: String): Boolean =
      if (wordToMatch == prefix)
        true
      else
        children
          .map { (k, v) -> k to v }
          .asSequence()
          //.onEach { println("Comparing $wordToMatch and ${prefix+it.first}") }
          .map { it.second.contains(wordToMatch, prefix + it.first) }
          .firstOrNull { it } ?: false

    suspend fun visit(prefix: String, block: suspend (str: String) -> Unit) {
      children.onEach { (key, node) ->
        if (node.isWord)
          block.invoke(prefix + key)
        node.visit(prefix + key, block)
      }
    }

  }

  fun triesAreTheSame1(trie1: Trie, trie2: Trie) =
    if (trie1.size != trie2.size)
      false
    else {
      trie1.words.sorted() == trie2.words.sorted()
    }

  fun triesAreTheSame2(trie1: Trie, trie2: Trie) =
    if (trie1.size != trie2.size)
      false
    else
      trie1.words
        .asSequence()
        .map { trie2.contains(it) }
        .firstOrNull { !it } ?: true

  fun triesAreTheSame3(trie1: Trie, trie2: Trie) =
    if (trie1.size != trie2.size)
      false
    else
      runBlocking {
        val f1 = channelFlow { trie1.visitEach { send(it) } }
        val f2 = channelFlow { trie2.visitEach { send(it) } }

        f1.zip(f2) { i, j -> i == j }.firstOrNull { !it } ?: true
      }
}