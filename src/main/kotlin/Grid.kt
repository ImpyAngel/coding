/**
 *
 * @autor Toropin Konstantin (impy.bian@gmail.com)
 */

import com.mxgraph.view.mxGraph
import javax.swing.JFrame
import com.mxgraph.swing.mxGraphComponent

data class Span(val first: Int, val last: Int, val index: Int)

typealias Edge = Pair<Int, Boolean>

typealias Edges = List<Edge>

fun List<Span>.toComplexityProfile(): List<Int> = List(size) { index ->
    this.filter {
        it.first <= index && it.last >= index
    }.size
}

class Grid(val strangeMode: Boolean = false) {
    data class Node(val name: String, val index: Int, var links: Edges = listOf())

    data class Tier(var nodes: List<Node>, val name: String = "")

    val tiers: MutableList<Tier> = mutableListOf()
    fun display() {
        val frame = GridFrame()
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setSize(frame.allX, frame.allY)
        println(frame.allY)
        frame.isVisible = true

    }

    inner class GridFrame() : JFrame("Grid") {
        private val xNode = 40.0
        private val xSpace = 80.0
        private val yNode = 20.0
        private val ySpace = 50.0
        private val ySpan = 50.0

        var allX = 0
        var allY = 0

        init {
            allX = (tiers.size) * (xSpace + xNode).toInt()

            val graph = mxGraph()
            val parent = graph.defaultParent
            graph.model.beginUpdate()
            try {
                var maxY = 0
                var preves = listOf<Pair<Any, Edges>>()
                tiers.forEachIndexed { xIndex, namedTier ->
                    val x = xIndex * (xNode + xSpace)
                    val (tier, name) = namedTier
                    if (name != "") graph.insertVertex(parent, null, name, x, 0.0, xNode, yNode)
                    val thisTier = mutableListOf<Pair<Any, Edges>>()
                    if (tier.size > maxY) maxY = tier.size
                    tier.forEachIndexed { yIndex, rowV ->
                        var y = yIndex * (yNode + ySpace) + ySpan

                        if (strangeMode) {
                            y += if (yIndex % 2 == 0 && xIndex % 2 == 0) ySpace / 2 else 0.0 +
                                    if (yIndex % 3 == 0) ySpace / 3 else 0.0
                        }

                        printlnd("Add vertex in $x-$y")
                        val v = graph.insertVertex(
                            parent,
                            null,
                            rowV.name,
                            x,
                            y,
                            xNode,
                            yNode
                        )
                        thisTier += v to rowV.links
                    }
                    preves.forEach { (v, nexts) ->
                        nexts.forEach { (u, edge) ->
                            graph.insertEdge(parent, null, if (edge) 1 else 0, v, thisTier[u].first)
                        }
                    }
                    preves = thisTier
                }
                allY = ((maxY) * (ySpace + yNode) + ySpan).toInt()
            } finally {
                graph.model.endUpdate()
            }

            val graphComponent = mxGraphComponent(graph)
            contentPane.add(graphComponent)
        }
    }

    private fun <T> genAllSub(subsets: List<Set<T>>, notUsed: List<T>): List<Set<T>> {
        if (notUsed.isEmpty()) {
            return subsets
        } else {
            return genAllSub(subsets + subsets.map { it + notUsed[0] }, notUsed.drop(1))
        }
    }

    private fun Vector.partialSum(set: Collection<Int>) = set.sumBy { this[it] } % 2

    fun createBySpan(matrix: Matrix, span: List<Span>) {
        assert(tiers.isEmpty())
        val n = matrix.m
        val active = mutableMapOf<Int, Span>()
        var prevTier = listOf(Node("", 0) to setOf<Pair<Int, Int>>())
        var prevActive = listOf<Int>()
        var pointer = 0
        for (i in 0 until n) {
            if (span.size > pointer && span[pointer].first == i) {
                active += span[pointer].last to span[pointer]
                pointer++
            }
            val deleted = if (active.remove(i) != null) 1 else 0
            val indexes = active.values.map { it.index }
            val curTier = genAllSub(listOf(setOf()), indexes).mapIndexed { index, subset ->
                indexes.map {
                    (it to if (subset.contains(it)) 1 else 0)
                }.let {
                    Node(it.joinToString(separator = "") {
                        it.second.toString()
                    }, index) to it.toSet()

                }
            }
            printlnd("$i active is \n$active")
            printlnd("tier is \n$curTier")
            prevTier.forEach { (prevNode, prevList) ->
                val links = mutableListOf<Edge>()
                curTier.forEach { (curNode, curList) ->
                    val intersect = prevList.intersect(curList)
                    if (intersect.size == prevList.size - deleted) {
                        val union = prevList.union(curList).filter { it.second == 1 }.map { it.first }
                        links.add(curNode.index to (matrix.getColumn(i).partialSum(union) % 2 == 1))
                    }
                }
                prevNode.links = links
            }
            tiers.add(Tier(prevTier.map { it.first }, prevActive.joinToString(separator = " ")))
            prevActive = indexes
            prevTier = curTier
        }
        tiers.add(Tier(prevTier.map { it.first }, prevActive.joinToString(separator = " ")))
        printlnd("All tiers are \n$tiers")
    }

    fun createByH(H: Matrix) {
        assert(tiers.isEmpty())
        val Ht = H.transpose()
        val m = H.m
        var prevTier = listOf((Ht[0] * 0).toList())
//        val combinations = setOf(prevTier[0].toList())
        for (i in 0 until m) {
            val line = Ht[i]
            val curTier = prevTier.toMutableList()
            tiers.add(Tier(prevTier.mapIndexed { index, value ->
                val links = mutableListOf(Edge(index, false))
                val ifAdd = value + line
                if (!curTier.contains(ifAdd)) {
                    curTier += ifAdd
                }
                links += curTier.indexOf(ifAdd) to true
                Node(value.joinToString(separator = ""), index, links)
            }))
            prevTier = curTier
        }
        tiers.add(Tier(listOf(Node((Ht[0] * 0).joinToString(separator = ""), 0))))
        printlnd("Tier before remove: \n $tiers")
        val map = mutableMapOf(0 to 0)
        for (i in m - 1 downTo 0) {
            val nextIndexesSize = tiers[i + 1].nodes.size
            val newNodes = tiers[i].nodes.filter {
                it.links = it.links.map {
                    it.copy(first = map[it.first] ?: Int.MAX_VALUE)
                }.filter { it.first < nextIndexesSize }
                !it.links.isEmpty()
            }
            map.clear()

            tiers[i].nodes = newNodes.sortedBy { it.index }.mapIndexed { index, it ->
                map[it.index] = index
                it.copy(index = index)
            }
        }
        printlnd("Tier after remove: \n $tiers")
    }
}