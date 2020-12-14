import TrieCompare.Trie.Companion.trieOf
import TrieCompare.triesAreTheSame1
import TrieCompare.triesAreTheSame2
import TrieCompare.triesAreTheSame3
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue

class TrieTest : StringSpec(
  {
    "Test unequal tries" {
      val trie1 = trieOf("test", "of", "trie", "data")
      val trie2 = trieOf("test", "of", "trie")

      triesAreTheSame1(trie1, trie2).shouldBeFalse()
      triesAreTheSame2(trie1, trie2).shouldBeFalse()
      triesAreTheSame3(trie1, trie2).shouldBeFalse()
    }

    "Test equal tries" {
      val trie1 = trieOf("test", "of", "trie", "data")
      val trie2 = trieOf("test", "of", "trie", "data")

      triesAreTheSame1(trie1, trie2).shouldBeTrue()
      triesAreTheSame2(trie1, trie2).shouldBeTrue()
      triesAreTheSame3(trie1, trie2).shouldBeTrue()
    }

    "Test repeat tries" {
      val trie1 = trieOf("test", "of", "trie", "data", "test", "of", "trie", "data")
      val trie2 = trieOf("test", "of", "trie", "data")

      triesAreTheSame1(trie1, trie2).shouldBeTrue()
      triesAreTheSame2(trie1, trie2).shouldBeTrue()
      triesAreTheSame3(trie1, trie2).shouldBeTrue()
    }
  })