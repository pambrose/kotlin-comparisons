import TreeCompare.Tree.Companion.treeOf
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.measureTimedValue


object TreeCompare {

  @JvmStatic
  fun main(args: Array<String>) {
    val tree1 = treeOf("test", "of", "sentence", "trie", "data", "structure")
    val tree2 = treeOf("test", "of", "sentence", "trie", "data", "structure")

    tree1.visitEach { println(it) }

    println(tree1.size)
    println(tree1.words)

    println(measureTimedValue { treesAreTheSame1(tree1, tree2) })
    println(measureTimedValue { treesAreTheSame2(tree1, tree2) })
    println(measureTimedValue { treesAreTheSame3(tree1, tree2) })
  }

  class Tree {
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
      fun treeOf(vararg elements: String) = Tree().apply { elements.forEach { add(it) } }
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

  fun treesAreTheSame1(tree1: Tree, tree2: Tree) =
    if (tree1.size != tree2.size)
      false
    else {
      tree1.words.sorted() == tree2.words.sorted()
    }

  fun treesAreTheSame2(tree1: Tree, tree2: Tree) =
    if (tree1.size != tree2.size)
      false
    else
      tree1.words
        .asSequence()
        .map { tree2.contains(it) }
        .firstOrNull { !it } ?: true

  fun treesAreTheSame3(tree1: Tree, tree2: Tree) =
    if (tree1.size != tree2.size)
      false
    else
      runBlocking {
        val f1 = channelFlow { tree1.visitEach { send(it) } }
        val f2 = channelFlow { tree2.visitEach { send(it) } }

        f1.zip(f2) { i, j -> i == j }.firstOrNull { !it } ?: true
      }
}