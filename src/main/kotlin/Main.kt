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


fun main(vararg args: String) {
    val fileName = args.getOrNull(0) ?: "src/main/resources/H1.txt"
    val coding = Coding(createMatrix(File(fileName)))
    ninthTask(coding)
}