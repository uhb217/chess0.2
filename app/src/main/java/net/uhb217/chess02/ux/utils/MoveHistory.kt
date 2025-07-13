package net.uhb217.chess02.ux.utils

object MoveHistory {
    private var current: LinkedNode? = null
    private var tail: LinkedNode? = null

    fun push(move: String) {
        val newNode = LinkedNode(move, prev = tail)
        tail?.next = newNode
        tail = newNode
        current = tail
    }

    fun peek(): LinkedNode? = current

    fun moveBack(): LinkedNode? {
        current = current?.prev ?: return null
        return current
    }

    fun moveForward(): LinkedNode? {
        current = current?.next ?: return null
        return current
    }

    fun isEmpty(): Boolean = tail == null

    fun canMoveForward(): Boolean = current?.next != null

    fun canMoveBack(): Boolean = current?.prev != null
}

data class LinkedNode(
    val fen: String,
    var next: LinkedNode? = null,
    var prev: LinkedNode? = null
)
