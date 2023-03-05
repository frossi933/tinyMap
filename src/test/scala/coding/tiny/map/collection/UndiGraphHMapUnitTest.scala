package coding.tiny.map.collection

import coding.tiny.map.collections.Graph.Edge
import coding.tiny.map.collections.UndiGraphHMap
import coding.tiny.map.utils.Extensions.OptionOps
import org.scalatest.flatspec.AnyFlatSpec

class UndiGraphHMapUnitTest extends AnyFlatSpec {

  private implicit val simpleEdgeOrdering: Ordering[Edge[String, Int]] =
    Ordering.by(e => e.nodeA + e.nodeB)

  "Make factory method" should "create an empty graph if it's called with an empty list of edges" in {
    val graph = UndiGraphHMap.make[String, Int](Vector.empty)

    assert(graph.isEmpty)
    assert(graph.nodes.isEmpty)
    assert(graph.edges.isEmpty)
  }

  "Make factory method" should "create an undirected graph containing all the edges and nodes present in the argument list and their opposites" in {

    val edges = Vector(
      Edge("A", "B", 10),
      Edge("C", "B", 10),
      Edge("A", "D", 10)
    )
    val graph = UndiGraphHMap.make[String, Int](edges)

    assert(!graph.isEmpty)
    assert(graph.nodes.sorted === Vector("A", "B", "C", "D"))
    assert(edges.forall(graph.edges.contains))
    assert(edges.map(_.reverse).forall(graph.edges.contains))
  }

  "Updated method" should "add the new edges and their opposites, and not delete any existent edges" in {

    val edges = Vector(
      Edge("A", "B", 10),
      Edge("C", "B", 10),
      Edge("A", "D", 10)
    )
    val graph = UndiGraphHMap.make[String, Int](edges)

    val edgesToAdd = Vector(
      Edge("D", "E", 20),
      Edge("E", "B", 20)
    )
    val graphToAdd = UndiGraphHMap.make[String, Int](edgesToAdd)

    val updated = graph.updated(graphToAdd)

    assert(updated.nodes.sorted === Vector("A", "B", "C", "D", "E"))
    assert(edges.forall(updated.edges.contains))
    assert(edges.map(_.reverse).forall(updated.edges.contains))
    assert(edgesToAdd.forall(updated.edges.contains))
    assert(edgesToAdd.map(_.reverse).forall(updated.edges.contains))
  }

  "Updated method" should "overwrite weight of already existent edges" in {

    val edges = Vector(
      Edge("A", "B", 10),
      Edge("C", "B", 10),
      Edge("A", "D", 10)
    )
    val graph = UndiGraphHMap.make[String, Int](edges)

    val edgesToUpdate = Vector(
      Edge("A", "B", 20),
      Edge("B", "C", 30)
    )
    val graphToUpdate = UndiGraphHMap.make[String, Int](edgesToUpdate)

    val updated = graph.updated(graphToUpdate)

    assert(updated.nodes.sorted === Vector("A", "B", "C", "D"))
    assert(edgesToUpdate.forall(updated.edges.contains))
    assert(edgesToUpdate.map(_.reverse).forall(updated.edges.contains))
    assert(!updated.hasEdge(Edge("A", "B", 10)))
    assert(!updated.hasEdge(Edge("C", "B", 10)))
  }

  "Shortest distance method" should "return None if start and end are not connected" in {
    val edges = Vector(
      Edge("A", "B", 10),
      Edge("C", "B", 10),
      Edge("D", "E", 10)
    )
    val graph = UndiGraphHMap.make[String, Int](edges)

    val maybeDistAD = graph.shortestDistance("A", "D")
    val maybeDistAE = graph.shortestDistance("A", "E")
    val maybeDistBD = graph.shortestDistance("B", "D")
    val maybeDistCD = graph.shortestDistance("C", "D")

    assert(maybeDistAD.isEmpty)
    assert(maybeDistAE.isEmpty)
    assert(maybeDistBD.isEmpty)
    assert(maybeDistCD.isEmpty)
  }

  "Shortest distance method" should "return None if start or end doesn't belong to the graph" in {
    val edges = Vector(
      Edge("A", "B", 10),
      Edge("C", "B", 10),
      Edge("A", "D", 10)
    )
    val graph = UndiGraphHMap.make[String, Int](edges)

    val distAE = graph.shortestDistance("A", "E")
    val distEB = graph.shortestDistance("E", "B")

    assert(distAE.isEmpty)
    assert(distEB.isEmpty)
  }

  "Shortest distance method" should "return 0 if start and end are the same node" in {
    val edges = Vector(
      Edge("A", "B", 10),
      Edge("C", "B", 10),
      Edge("A", "D", 10)
    )
    val graph = UndiGraphHMap.make[String, Int](edges)

    val distA = graph.shortestDistance("A", "A").getOrFail
    val distB = graph.shortestDistance("B", "B").getOrFail
    val distC = graph.shortestDistance("C", "C").getOrFail

    assert(distA === 0)
    assert(distB === 0)
    assert(distC === 0)
  }

  "Shortest distance method" should "calculate the right value considering all the available paths" in {
    val edges = Vector(
      Edge("A", "B", 100),
      Edge("A", "C", 10),
      Edge("C", "B", 10)
    ) // A-C-B is shorter than A-B
    val graph = UndiGraphHMap.make[String, Int](edges)

    val distAB = graph.shortestDistance("A", "B").getOrFail

    assert(distAB === 20)
  }

  "Shortest distance method" should "calculate the same value from A to B than from B to A" in {
    val edges = Vector(
      Edge("A", "B", 100),
      Edge("A", "C", 10),
      Edge("C", "B", 10)
    )
    val graph = UndiGraphHMap.make[String, Int](edges)

    val distAB = graph.shortestDistance("A", "B").getOrFail
    val distBA = graph.shortestDistance("B", "A").getOrFail

    assert(distAB === distBA)
  }

  "Shortest distance method" should "calculate the right value for more complex graphs" in {
    val edges = Vector(
      Edge("A", "B", 100),
      Edge("A", "C", 30),
      Edge("C", "D", 200),
      Edge("B", "F", 300),
      Edge("D", "E", 80),
      Edge("F", "E", 50),
      Edge("D", "H", 90),
      Edge("E", "H", 30),
      Edge("E", "G", 150),
      Edge("F", "G", 70),
      Edge("H", "G", 50)
    )
    val graph = UndiGraphHMap.make[String, Int](edges)
//    A --100-- B --300--  F
//    |                   |  \
//    30                  50   70
//    |                   /       \
//    C --200-- D --80--  E --150-- G
//               \        |       /
//                90     30      /
//                   \   /      /
//                     H -----50

    val distAB = graph.shortestDistance("A", "B").getOrFail
    val distAE = graph.shortestDistance("A", "E").getOrFail
    val distBD = graph.shortestDistance("B", "D").getOrFail
    val distAH = graph.shortestDistance("A", "H").getOrFail
    val distAG = graph.shortestDistance("A", "G").getOrFail
    val distBG = graph.shortestDistance("B", "G").getOrFail
    val distBH = graph.shortestDistance("B", "H").getOrFail

    assert(distAB === 100)
    assert(distAE === 310)
    assert(distBD === 330)
    assert(distAH === 320)
    assert(distAG === 370)
    assert(distBG === 370)
    assert(distBH === 380)
  }
}
