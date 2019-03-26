@file:Suppress("unused")

import java.io.File

/**
 *
 * @autor Toropin Konstantin (impy.bian@gmail.com)
 */

fun firstTask(coding: Coding) = coding.speed

fun secondTask(coding: Coding) = coding.d
fun forthTask(coding: Coding) = coding.G
fun fifthTask(coding: Coding, message: Vector) = coding.code(message)
fun seventhTask(coding: Coding) = coding.syndromeTable.map { (syndrome, value) ->
    "$syndrome->$value"
}.joinToString("\n")

fun eightTask(coding: Coding) = coding.G.toSpanForm()
fun eightTaskAdd(coding: Coding) = coding.G.toSpanForm().spans().toComplexityProfile()
fun ninthTask(coding: Coding) {
    val spanForm = coding.G.toSpanForm()
    Grid().apply {
        createBySpan(spanForm, spanForm.spans())
        display()
    }
}

fun tenthTask(coding: Coding) {
    Grid().apply {
        createByH(coding.H)
        display()
    }
}

fun viterbiHardTask(coding: Coding) {
    val grid = Grid().apply {
        createByH(coding.H)
    }
    val decodeAns = coding.words[5]
    val wordForDecode = decodeAns + error // not for all
    println("we have ${wordForDecode.toList()}")
    println("we want ${decodeAns.toList()}")
    println(Decoder(grid).viterbiByHard(wordForDecode))
}

fun bkdrHardInputSoftOutput(coding: Coding) {
    val grid = Grid().apply {
        createByH(coding.H)
    }
    val decodeAns = coding.words[5]
    val wordForDecode = decodeAns + error // not for all
    println("we want ${decodeAns.toList()}")
    println("we have ${wordForDecode.toList()}")
    println(Decoder(grid).bkdrHardInput(wordForDecode))
}

fun allCodesRanges() {
    for (i in 3..9) {
        println("for $i:")
        allCircleCodes(i)
    }
}


fun main(vararg args: String) {
    val fileName = args.getOrNull(0) ?: "src/main/resources/H1.txt"
    val coding = Coding(createMatrix(File(fileName)))
    println(coding.H)
}