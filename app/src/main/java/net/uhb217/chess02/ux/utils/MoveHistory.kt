package net.uhb217.chess02.ux.utils

object MoveHistory {
    private var current: LinkedNode? = null
    private var tail: LinkedNode? = null
    public var length = 0

    fun push(move: String) {
        val newNode = LinkedNode(move, prev = tail)
        tail?.next = newNode
        tail = newNode
        current = tail
        length++
    }

    fun peek(): LinkedNode? = current

    fun moveBack(): String? {
        current = current?.prev ?: return null
        return current!!.fen
    }

    fun moveForward(): String? {
        current = current?.next ?: return null
        return current?.fen
    }

    fun isEmpty(): Boolean = tail == null

    fun canMoveForward(): Boolean = current?.next != null

    fun canMoveBack(): Boolean = current?.prev != null
    fun clear() {
        current = null
        tail = null
        length = 0
    }
}

data class LinkedNode(
    val fen: String? = null,
    var next: LinkedNode? = null,
    var prev: LinkedNode? = null
)
