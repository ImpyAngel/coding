/**
 *
 * @autor Toropin Konstantin (impy.bian@gmail.com)
 */

typealias Vec = List<Int>

const val DEBUG = false

fun printlnd(any: Any? = "") {
    if (DEBUG) {
        println(any)
    }
}

class Coding(val H: Matrix) {
    val n get() = H.m
    val k get() = H.m - H.n
    val speed get() = k / n.toDouble()

    val G by lazy {
        createGbyH(H).apply {
            printlnd("G is")
            printlnd(this)
            printlnd()
            assert(testGisCorrectForH(this, H))
        }
    }

    val words by lazy {
        printlnd("All words:")
        genAllVectors(k).map {
            (it * G).also {
                printlnd(it.joinToString())
            }
        }
    }
    val spectrum by lazy {
        printlnd("Spectrum is")
        words.groupBy {
            it.sum()
        }.map { (d, words) ->
            d to words.size
        }.sortedBy{ it.first }
    }

    val d by lazy {
        words.drop(1).foldRight(Int.MAX_VALUE) { it, was ->
            Math.min(was, it.sum())
        }
    }
// 1 >> size
    private fun genAllVectors(size: Int) = List(1 shl size) { acc ->
        IntArray(size) { i ->
            (acc shr i) % 2
        }
    }

    fun code(info: Vector): Vector {
        val ans = info * G
        assert((ans * H.transpose()).isZero())
        return ans
    }

    fun decode(code: Vector): Vector {
        return code * H.transpose()
    }

    val syndromeTable by lazy {
        createAllSyndromes()
    }

    private fun createAllSyndromes(): Map<Vec, Vec> {
        val table = mutableMapOf<Vec, Vec>()
        val vectors = genAllVectors(H.m)
        vectors.forEach { error ->
            val syndrome = error * H.transpose()
            if (!syndrome.isZero()) {
                val prev = table[syndrome.toList()]
                if (prev == null || prev.sum() > error.sum()) {
                    table[syndrome.toList()] = error.toList()
                }
            }
        }
        return table
    }

    private fun createSyndromeTable(errors: Collection<Vector>): Map<Vec, Vec> {
        val table = mutableMapOf<Vec, Vec>()
        errors.forEach { error ->
            val syndrome = error * H.transpose()
            table[syndrome.toList()] = error.toList()
        }
        return table
    }

    private fun genAllOnceErrors(): Set<Vector> {
        val errors = mutableSetOf<Vector>()
        for (i in 0 until n) {
            errors.add(IntArray(n) { j -> if (i == j) 1 else 0 })
        }
        return errors
    }

    fun syndromeDecoding(code: Vector): Vector {
        val syndrome = decode(code).toList()
        printlnd("TABLE IS -")
        syndromeTable.forEach { key, value ->
            printlnd("${key.toList()} --> ${value.toList()}")
        }

        printlnd("Our syndrome is ${syndrome}")
        if (syndrome.toIntArray().isZero()) {
            printlnd("No syndrome")
            return code
        }
        if (syndromeTable.containsKey(syndrome)) {
            printlnd("Error is ${syndromeTable.getValue(syndrome)}")
            return code + syndromeTable.getValue(syndrome).toIntArray()
        }
        error("Can't decode this")
    }
}

val info1 = mutableListOf(0, 1, 1, 1, 1, 0).toIntArray()
val error = mutableListOf(0, 0, 0, 0, 0, 0, 0, 0, 1, 0).toIntArray()