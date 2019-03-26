/**
 *
 * @autor Toropin Konstantin (impy.bian@gmail.com)
 */

data class Polynomial(val values: List<Int>, val overflowed: Boolean = false) {
    val n = values.size
    override fun toString(): String = if (isZero()) "0" else values.foldIndexed("") { index, acc, i ->
        if (i == 0)
            acc
        else
            (if (acc.isEmpty())
                acc
            else
                "$acc + ") +
                    when (index) {
                        0 -> "1"
                        1 -> "x"
                        else -> "x^$index"
                    }
    }

    operator fun times(other: Polynomial): Polynomial {
        var overflow = false
        val ans = MutableList(n) { 0 }
        values.forEachIndexed { iIndex, i ->
            other.values.forEachIndexed { jIndex, j ->
                if (i * j != 0) {
                    if (iIndex + jIndex >= n) overflow = true
                    ans[(iIndex + jIndex) % n] += 1
                }
            }
        }
        return Polynomial(ans.map { it % 2 }, overflow)
    }

    operator fun plus(other: Polynomial): Polynomial {
        assert(other.n == n)
        return Polynomial(List(n) { (values[it] + other.values[it]) % 2 }, overflowed or other.overflowed)
    }

    operator fun div(other: Polynomial): Polynomial = divAndMod(other).first
    operator fun rem(other: Polynomial): Polynomial = divAndMod(other).second

    fun pow() = values.lastIndexOf(1)

    private fun divAndMod(other: Polynomial): Pair<Polynomial, Polynomial> {
        val maxJ = other.pow()
        var ans = zero()
        var mode = copy()
        while (true) {
            val maxI = mode.pow()
            if (maxI < maxJ) break
            val temp = pow(maxI - maxJ)
            val sub = temp * other
            ans += temp
            mode += sub
        }
        return ans to mode
    }


    fun allPrimeDividers(): List<Polynomial> {
        if (isPow()) {
            List(this.pow()) { pow(1) }
        } else {
            for (i in 3 until (1 shl n)) {
                val candidate = Polynomial(List(n) { (i shr it) % 2 })
                if (this.rem(candidate).isZero()) {
                    return listOf(candidate) + (this / candidate).allPrimeDividers()
                }
            }
        }
        return emptyList()
    }

    fun allDividers(): Set<Polynomial> {
        val ans = mutableSetOf<Polynomial>()
        ans.divs(allPrimeDividers(), listOf(), -1)
        return ans
    }

    fun MutableSet<Polynomial>.divs(primaries: List<Polynomial>, prevs: List<Polynomial>, prevNumber: Int) {
        for (i in prevNumber + 1 until primaries.size) {
            divs(primaries, prevs + primaries[i], i)
        }
        if (prevs.isNotEmpty()) this.add(prevs.fold(pow(0)) { a, b  ->  a * b })
    }

    fun toVector() = values.dropLast(1).toIntArray()

    fun isZero() = values.sum() == 0
    fun isPow() = values.sum() <= 1
    fun isOne() = pow() == 0
    fun isHighPow() = pow() == n - 1

    fun zero() = Polynomial(List(n) { 0 })
    fun pow(pow: Int) = Polynomial(List(n) { if (it == pow) 1 else 0 })
    fun x() = pow(1)

    operator fun inc() = this * x()

    companion object {
        fun zero(n: Int) = Polynomial(List(n) { 0 })
        fun pow(n: Int, pow: Int) = Polynomial(List(n) { if (it == pow) 1 else 0 })
        fun one(n: Int) = pow(n, 0)
        fun create(n: Int, vararg coefficients :Int)= coefficients.fold(zero(n)) { acc, i ->
            acc + pow(n, i)
        }
    }
}

data class CircleCode(val n: Int = 7, val g: Polynomial = findGenerate(n)) {
    val f by lazy { f(n) }
    val dualCode by lazy { CircleCode(n, f / g) }
    val G by lazy {
        var acc = g.copy()
        val k = n - g.pow()
        Matrix(k, n, Array(k) {
            acc++.toVector()
        }).also {
            printlnd("Was matrix: \n$it")
        }
    }

    val coding by lazy {
        Coding(createHbyG(G))
    }

    companion object {
        fun f(n: Int): Polynomial = Polynomial.pow(n + 1, n) + Polynomial.pow(n + 1, 0)
        fun findGenerate(n: Int): Polynomial {
            val basePoly = f(n)
            val dividers = basePoly.allPrimeDividers()
            printlnd("Dividers for $basePoly is \n${dividers.joinToString("\n")}")
            return when(dividers.size) {
                1 -> dividers[0]
                2 -> dividers[1]
                else -> dividers[0] * dividers[1]
            }
        }
    }
}

fun createField(p: Polynomial) : List<Polynomial> {
    assert(p.isHighPow()) // i don't want think about else
    val n = p.n
    assert(n > 2)
    val ans = mutableListOf(Polynomial.zero(n), Polynomial.one(n))
    val x = Polynomial.create(n, 3, 2)
    var alpha = Polynomial.pow(n, 1)
    while (!alpha.isOne()) {
        println(alpha)
        ans.add(alpha)
        alpha = (alpha * x) % p
    }
    return ans
}

fun allCircleCodes(n: Int) {
    CircleCode.f(n).allDividers().forEach {
        if (!(it.isPow() || it.isHighPow() || it.values[0] == 0)) {
            val code = CircleCode(n, it)
            val speed = code.coding.speed
            val d = code.coding.d
            val dualcode = code.dualCode.g
            println("g is $it\nspeed is $speed\nd is $d\n" +
                    "dual code is $dualcode")
        }
    }
}

val p6 = Polynomial.pow(7, 6) + Polynomial.pow(7, 1) + Polynomial.one(7)
val p4first = Polynomial.create(5, 4, 1, 0)
val p4second = Polynomial.create(5, 4, 3, 2, 1, 0)

fun duals(p: Polynomial) {
    val field = createField(p)
    val fieldWithDuals = field.map { i ->
        i to field.firstOrNull { j->
            ((i * j) % p).isOne()
        }
    }
    fieldWithDuals.forEach {
        println("${it.first} --- ${it.second}")
    }
}

fun miniMain() {
    duals(p4second)
}