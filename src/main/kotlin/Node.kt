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
        .sortedBy { it.first }
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