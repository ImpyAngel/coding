/**
 *
 * @autor Toropin Konstantin (impy.bian@gmail.com)
 */

fun Boolean.toInt() = if (this) 1 else 0

class Decoder(val grid: Grid = Grid()) {
    data class MaxProbabilityNode(val nexts: List<Int>, val logProbability: Double)

    val tiers get() = grid.tiers

    private fun viterbi(input: List<Probability>): ViterbiAnswer {
        assert(tiers.size == input.size + 1)
        assert(tiers[tiers.size - 1].nodes.size == 1)
        var lastTier = listOf(MaxProbabilityNode(listOf(), 0.0))
        for (i in input.size - 1 downTo 0) {
            val curProbability = { edge: Edge ->
                lastTier[edge.first].logProbability + if (edge.second) input[i].is1 else input[i].is0
            }
            val nodes = tiers[i].nodes
            val thisTier = List(nodes.size) { j ->
                nodes[j].links.maxBy(curProbability)!!.let {
                    MaxProbabilityNode(lastTier[it.first].nexts + it.second.toInt(), curProbability(it))
                }
            }
            lastTier = thisTier
        }
        return ViterbiAnswer(lastTier[0].nexts.reversed(), lastTier[0].logProbability)
    }

    data class Probability(val is0: Double, val is1: Double)

    data class ViterbiAnswer(val code: List<Int>, val probability: Double)

    fun viterbiByHard(word: Vector, p: Double = 10e-3): ViterbiAnswer {
        val logProbabilities = word.map {
            val is0 = if (it == 0) 1.0 - p else p
            val is1 = 1 - is0
            Probability(Math.log(is0), Math.log(is1))
        }
        printlnd("input probabilities is \n$logProbabilities")
        val ans = viterbi(logProbabilities)
        printlnd("log ans is $ans")
        return ans.copy(probability = Math.exp(ans.probability))
    }

    fun viterbiBySoft(probabilitiesOfOnes: List<Double>): ViterbiAnswer {
        val logProbabilities = probabilitiesOfOnes.map {
            val is1 = it
            val is0 = 1.0 - is1
            Probability(Math.log(is0), Math.log(is1))
        }
        printlnd("input probabilities is \n$logProbabilities")
        val ans = viterbi(logProbabilities)
        printlnd("log ans is $ans")
        return ans.copy(probability = Math.exp(ans.probability))
    }

    fun bkdrHardInput(word: Vector, p: Double = 1e-4): List<Double> {
        val probs = word.map {
            val is0 = if (it == 0) 1.0 - p else p
            val is1 = 1 - is0
            Probability(is0, is1)
        }
        return bkdr(probs)
    }

    fun bkdrHardAnswer(probabilitiesOfOnes: List<Double>) = bkdrOnes(probabilitiesOfOnes).map { if (it < 1e-4) 0 else 1 }

    fun bkdrOnes(probabilitiesOfOnes: List<Double>) = bkdr(probabilitiesOfOnes.map { Probability(1.0 - it, it) })

    private fun priorProbability(symbol: Boolean) = 0.5

    fun bkdr(probabilities: List<Probability>): List<Double> {
        printlnd("probabilities are $probabilities")
        val m = tiers.size - 1
        //gammas
        val probTiers: List<Tier> = tiers.map {
            Tier(it.nodes.mapIndexed { i, node ->
                Node(links = node.links.map {
                    val condPrior = if (it.second) probabilities[i].is1 else probabilities[i].is0
                    ProbEdge(it.first, it.second, condPrior * priorProbability(it.second))
                })
            })
        }
        probTiers[0].nodes[0].alpha = 1.0
        probTiers[m].nodes[0].beta = 1.0
        //alphas
        for (i in 1..m) {
            probTiers[i - 1].nodes.forEach { node ->
                node.links.forEach { edge ->
                    val nextNode = probTiers[i].nodes[edge.indexNext]
                    nextNode.alpha += node.alpha * edge.gamma
                }
            }
        }
        for (i in m - 1 downTo 0) {
            var isZero = 0.0
            var isOne = 0.0
            probTiers[i].nodes.forEach { node ->
                node.links.forEach { edge ->
                    val nextBeta = probTiers[i + 1].nodes[edge.indexNext].beta
                    node.beta += edge.gamma * nextBeta
                    edge.sigma = node.alpha * edge.gamma * nextBeta
                    if (edge.value) isOne += Math.log(edge.sigma) else isZero += Math.log(edge.sigma)
                }
            }
            probTiers[i].lambda = isOne - isZero
        }
        printlnd(probTiers)
        return probTiers.dropLast(1).map {
            it.lambda
        }
    }

    private data class Node(
        var alpha: Double = 0.0,
        var beta: Double = 0.0,
        val links: List<ProbEdge> = listOf()
    )

    private data class ProbEdge(
        val indexNext: Int,
        val value: Boolean,
        val gamma: Double = 0.0,
        var sigma: Double = 0.0
    )

    private data class Tier(val nodes: List<Node>, var lambda: Double = 0.0) {
        override fun toString() = "lambda is $lambda\n nodes are\n" + nodes.joinToString(separator = "\n")
    }
}

