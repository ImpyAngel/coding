/**
 *
 * @autor Toropin Konstantin (impy.bian@gmail.com)
 */
typealias Vector = IntArray

operator fun List<Int>.plus(other: Vector): List<Int> {
    assert(size == other.size)
    return Array(size) { i ->
        (get(i) + other[i]) % 2
    }.toList()
}

operator fun Vector.plus(other: Vector): Vector {
    assert(size == other.size)
    return Array(size) { i ->
        (get(i) + other[i]) % 2
    }.toIntArray()
}

operator fun Vector.times(scalar: Int): Vector {
    return Array(size) { i ->
        (get(i) * scalar) % 2
    }.toIntArray()
}

operator fun Vector.times(other: Vector): Vector {
    assert(size == other.size)
    return Array(size) { i ->
        (get(i) * other[i])
    }.toIntArray()
}

operator fun Vector.times(other: Matrix): Vector {
    assert(size == other.n)
    return IntArray(other.m) { j ->
        this.mapIndexed { i, value ->
            other[i][j] * value
        }.sum() % 2
    }
}

fun Vector.isZero() = sum() == 0
