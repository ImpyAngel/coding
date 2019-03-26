import java.io.File
import java.util.*

/**
 *
 * @autor Toropin Konstantin (impy.bian@gmail.com)
 */

data class Matrix(var n: Int, var m: Int, val inner: Array<Vector> = Array(n) { IntArray(m) { 0 } }) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Matrix

        if (n != other.n) return false
        if (m != other.m) return false
        if (!inner.contentDeepEquals(other.inner)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = n
        result = 31 * result + m
        result = 31 * result + inner.contentDeepHashCode()
        return result
    }

    fun copy(): Matrix = Matrix(n, m, inner.clone())
    override fun toString() = inner.joinToString("\n") { it.joinToString() }

    operator fun get(index: Int): Vector = inner[index]

    operator fun set(index: Int, value: Vector): Unit = inner.set(index, value)

    fun transpose() = Matrix(m, n, Array(m) { i -> IntArray(n) { j -> inner[j][i] } })

    fun swapLines(v: Int, u: Int): Matrix {

        val copy = copy()
        if (v != u) {
            copy[v] = inner[u].copyOf()
            copy[u] = inner[v].copyOf()
        }
        return copy
    }

    fun swapColons(v: Int, u: Int): Matrix {
        printlnd("TRY SWAP $v - $u")
        if (v == u) {
            return this
        }
        return transpose().swapLines(v, u).transpose()
    }

    // [first, last)
    // 0 1 [|2 3 4| 5) 6
    fun sliceByLines(first: Int = 0, last: Int = m): Matrix {
        val newM = last - first
        val prevInner = inner
        return Matrix(n, newM, Array(n) { i -> IntArray(newM) { j -> prevInner[i][j + first] } })
    }

    fun sliceOnTwoMatrix(headOfSecond: Int): Pair<Matrix, Matrix> {
        return sliceByLines(last = headOfSecond) to sliceByLines(first = headOfSecond)
    }

    fun addIInTail() = Matrix(n, m + n, Array(n) { i ->
        IntArray(m + n) { j ->
            if (j < m) {
                inner[i][j]
            } else {
                if (j - m == i) 1 else 0
            }
        }
    })

    fun addIinHead() = Matrix(n, m + n, Array(n) { i ->
        IntArray(m + n) { j ->
            if (j >= n) {
                inner[i][j - n]
            } else {
                if (j == i) 1 else 0
            }
        }
    })

    operator fun times(other: Matrix): Matrix {
        assert(m == other.n)
        return Matrix(
            n, other.m,
            inner.map {
                it * other
            }.toTypedArray()
        )
    }

    fun toSpanForm(): Matrix {
        //i believe that matrix is liner independent
        var ans = this.copy()
        var count = 0
        for (i in 0 until m) {
            var k = -1
            for (j in count until n) {
                if (ans[j][i] == 1) {
                    k = j
                    break
                }
            }
            if (k != -1) {
                ans = ans.swapLines(count, k)
                for (j in k + 1 until n) {
                    if (ans[j][i] == 1) {
                        ans[j] = ans[j] + ans[count]
                    }
                }
                count++
                if (count == n) break
            }
        }
        assert(count == n)

        printlnd("Before deleting end")
        val lastNonZeroes = mutableMapOf(ans[n - 1].lastNonZero() to n - 1)
        for (i in n - 2 downTo 0) {
            var curLast = ans[i].lastNonZero()
            while (lastNonZeroes.containsKey(curLast)) {
                ans[i] = ans[i] + ans[lastNonZeroes.getValue(curLast)]
                curLast = ans[i].lastNonZero()
            }
            lastNonZeroes[curLast] = i
        }
        printlnd("After deleting end")
        printlnd(ans)
        return ans
    }

    fun getColumn(index: Int): Vector = IntArray(n) {
        inner[it][index]
    }

    fun spans(): List<Span> =
        inner.mapIndexed { index, vec ->
            Span(vec.indexOfFirst { it == 1 }, vec.lastNonZero(), index)
        }

    private fun Vector.lastNonZero() = size - toList().reversed().indexOfFirst { it == 1 } - 1
}

fun createMatrix(file: File): Matrix {
    Scanner(file).use { scanner ->
        val n = scanner.nextInt()
        val m = scanner.nextInt()
        return Matrix(n, m, Array(n) { IntArray(m) { scanner.nextInt() } })
    }
}
