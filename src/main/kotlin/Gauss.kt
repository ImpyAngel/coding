/**
 *
 * @autor Toropin Konstantin (impy.bian@gmail.com)
 */

private fun Matrix.tryDiagonalise(shift: Int = 0): Matrix? {
    var ans = this.copy()
    for (i in 0 until n) {
        var k = -1
        for (j in i until n) {
            if (ans[j][i + shift] == 1) {
                k = j
                break
            }
        }
        if (k == -1) {
            return null
        }
        ans = ans.swapLines(i, k)
        for (j in k + 1 until n) {
            if (ans[j][i + shift] == 1) {
                ans[j] = ans[j] + ans[i]
            }
        }
    }
    for (i in 1 until n) {
        for (j in 0 until i) {
            if (ans[j][i + shift] == 1) {
                ans[j] = ans[j] + ans[i]
            }
        }
    }
    return ans
}

fun <T> deepAnalysis(
    prev: Set<Int>,
    maxValue: Int,
    maxDeep: Int,
    inStart: Boolean,
    operation: (Set<Int>) -> T?
): Pair<Set<Int>, T?> {
    if (prev.size == maxDeep) {
        return prev to operation(prev)
    }
    if (inStart) {
        for (j in (prev.max() ?: -1) + 1..maxValue) {
            deepAnalysis(prev + j, maxValue, maxDeep, inStart, operation).let {
                if (it.second != null) {
                    return it
                }
            }
        }
    } else {
        for (j in (prev.min() ?: maxValue + 1) - 1 downTo 0) {
            deepAnalysis(prev + j, maxValue, maxDeep, inStart, operation).let {
                if (it.second != null) {
                    return it
                }
            }
        }
    }
    return prev to null
}

private fun Matrix.shiftTo(set: Set<Int>, inStart: Boolean = true): Matrix {
    var ans = copy()
    if (inStart) {
        set.reversed().forEachIndexed { index, i ->
            ans = ans.swapColons(index, i)
        }
    } else {
        set.forEachIndexed { index, i ->
            ans = ans.swapColons(m - 1 - index, i)
        }
    }

    return ans
}

fun Matrix.diagonaliseWithSwap(inStart: Boolean = true): Pair<Set<Int>, Matrix> {
    assert(n <= m)
    val ans = deepAnalysis(setOf(), m - 1, n, inStart) {
        shiftTo(it, inStart).tryDiagonalise(if (inStart) 0 else m - n)
    }
    return ans.first to (ans.second ?: error("Can't diagonalise"))
}

fun createGbyH(H: Matrix): Matrix {
    val (set, diagonalizedP) = H.diagonaliseWithSwap(false)
    printlnd()
    printlnd(diagonalizedP)
    val Pt = diagonalizedP.sliceByLines(last = H.m - H.n).transpose()
    printlnd()
    printlnd(Pt)
    val IPt = Pt.addIinHead()
    printlnd()
    printlnd(IPt)
    printlnd(set)
    return IPt.shiftTo(set, false)
}

fun createHbyG(G: Matrix): Matrix {
    val (set, diagonalizedP) = G.diagonaliseWithSwap()
    printlnd()
    printlnd(diagonalizedP)
    val Pt = diagonalizedP.sliceByLines(first = G.m - G.n).transpose()
    printlnd()
    printlnd(Pt)
    val IPt = Pt.addIInTail()
    printlnd()
    printlnd(IPt)
    printlnd(set)
    return IPt.shiftTo(set)
}

fun testGisCorrectForH(G: Matrix, H: Matrix): Boolean {
    val ans = G * (H.transpose())
    println(ans)
    return ans.inner.all { it.isZero() }
}
