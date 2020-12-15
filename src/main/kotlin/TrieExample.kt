import Trie.Companion.trieOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.FileReader
import kotlin.time.measureTimedValue


object TrieExample {

  @JvmStatic
  fun main(args: Array<String>) {
    val trie1 = trieOf("test", "of", "sentence", "trie", "data", "structure")
    val trie2 = trieOf("test", "of", "sentence", "trie", "data", "structure")

    trie1.blockingVisitEach { println(it) }

    println(trie1.size)
    println(trie1.words)

//    println("Same1: " + measureTimedValue { triesAreTheSame1(trie1, trie2) })
//    println("Same2: " + measureTimedValue { triesAreTheSame2(trie1, trie2) })
//    println("Same3: " + measureTimedValue { triesAreTheSame3(trie1, trie2) })
//    println("Same4: " +measureTimedValue { triesAreTheSame4(trie1, trie2) })

    val dict = sequenceOfWords("/usr/share/dict/words").toList()

    val words1 = Trie().apply { repeat(10000) { add(dict.random().let { "$it" }) } }
    val words2 = words1//Trie().apply { repeat(100) { add(words.random()) } }

    println("Same1: " + measureTimedValue { triesAreTheSame1(words1, words2) })
    println("Same2: " + measureTimedValue { triesAreTheSame2(words1, words2) })
    println("Same3: " + measureTimedValue { triesAreTheSame3(words1, words2) })
    println("Same4: " + measureTimedValue { triesAreTheSame4(words1, words2) })

    println(words1.size)
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
        val c1 = Channel<String>(Channel.RENDEZVOUS)
        val c2 = Channel<String>(Channel.RENDEZVOUS)

        launch { trie1.suspendingVisitEach { c1.send(it) }; c1.close() }
        launch { trie2.suspendingVisitEach { c2.send(it) }; c2.close() }

        c1.consumeAsFlow().zip(c2.consumeAsFlow()) { i, j -> i == j }.firstOrNull { !it } ?: true
      }

  fun triesAreTheSame4(trie1: Trie, trie2: Trie) =
    if (trie1.size != trie2.size)
      false
    else
      runBlocking {
        val f1 = channelFlow { trie1.suspendingVisitEach { send(it) } }
        val f2 = channelFlow { trie2.suspendingVisitEach { send(it) } }

        f1.zip(f2) { i, j -> i == j }.firstOrNull { !it } ?: true
      }

  fun sequenceOfWords(fileName: String) =
    sequence {
      BufferedReader(FileReader(fileName)).use {
        while (true) yield(it.readLine() ?: break)
      }
    }
}