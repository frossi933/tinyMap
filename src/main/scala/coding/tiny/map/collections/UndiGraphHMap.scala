package coding.tiny.map.collections

import cats.implicits._
import cats.{Eq, Hash, Monoid, Order}
import coding.tiny.map.collections.Graph.{Adjacency, Edge}

import scala.annotation.tailrec
import scala.collection.immutable.{HashMap, HashSet}

class UndiGraphHMap[N: Hash, W: Monoid: Order] private (
    private val g: HashMap[N, Adjacency[N, W]]
) extends Graph[N, W] {

  val nodes: Vector[N]          = g.keys.toVector
  val edges: Vector[Edge[N, W]] = g.flatMap { case (n, adj) =>
    adj.map(a => Edge(n, a._1, a._2))
  }.toVector

  def isEmpty: Boolean = g.isEmpty

  def hasNode(node: N): Boolean = g.keys.iterator.contains(node)

  def hasEdge(edge: Edge[N, W]): Boolean =
    g.get(edge.nodeA).exists(adj => adj.contains(edge.nodeB))

  def insertNode(node: N): UndiGraphHMap[N, W] =
    if (!hasNode(node))
      new UndiGraphHMap(g.updated(node, Map()))
    else
      this

  def insertOrUpdateEdge(edge: Edge[N, W]): UndiGraphHMap[N, W] =
    if (!hasEdge(edge)) {
      val adjA    = g.getOrElse(edge.nodeA, Map())
      val adjB    = g.getOrElse(edge.nodeB, Map())
      val updated = g
        .updated(edge.nodeA, adjA + (edge.nodeB -> edge.weight))
        .updated(edge.nodeB, adjB + (edge.nodeA -> edge.weight))

      new UndiGraphHMap(updated)
    } else
      this

  def insertOrUpdateEdges(edges: Vector[Edge[N, W]]): UndiGraphHMap[N, W] = {
    val toInsertOrUpdate = edges.filterNot(hasEdge).distinct
    toInsertOrUpdate.foldLeft(this) { case (graph, edge) => graph.insertOrUpdateEdge(edge) }
  }

  def removeNodes(nodes: Vector[N]): UndiGraphHMap[N, W] =
    UndiGraphHMap(edges.filterNot(e => nodes.contains(e.nodeA) || nodes.contains(e.nodeB)))

  def removeEdges(edgesToRemove: Vector[Edge[N, W]]): UndiGraphHMap[N, W] =
    UndiGraphHMap(
      edges.filterNot(e => edgesToRemove.contains(e) || edgesToRemove.contains(e.reverse))
    )

  def adjacencyOf(node: N): Adjacency[N, W] = g.getOrElse(node, Map[N, W]())

  def neighboursOf(node: N): Vector[N] = adjacencyOf(node).keys.toVector

  def mapEdges[M: Hash, Z: Monoid: Order](f: Edge[N, W] => Edge[M, Z]): UndiGraphHMap[M, Z] =
    UndiGraphHMap(
      edges.map(f)
    )

  def shortestDistance(n: N, m: N): W = {
    @tailrec
    def shortestDistanceTailRec(
        start: N,
        end: N,
        fringe: Vector[(W, Vector[N])],
        visited: HashSet[N]
    ): W = {
      fringe match {
        case (dist, path @ (node +: tail)) +: restFringe =>
          if (node === end)
            dist
          else {
            val newPaths  = adjacencyOf(node)
              .filter { case (nextNode, _) => !visited.contains(nextNode) }
              .map { case (nextNode, weight) => (weight |+| dist, path.prepended(nextNode)) }
            val newFringe = (restFringe ++ newPaths).sortWith { case ((d1, _), (d2, _)) =>
              Order[W].lt(d1, d2)
            }

            shortestDistanceTailRec(start, end, newFringe, visited + node)
          }

        case _ => Monoid[W].empty
      }
    }

    shortestDistanceTailRec(n, m, Vector(Monoid[W].empty -> Vector(n)), HashSet.empty[N])
  }

  def reverse: Graph[N, W] = this

}

object UndiGraphHMap {

  def apply[N: Hash: Eq, W: Monoid: Order](edges: Vector[Edge[N, W]]): UndiGraphHMap[N, W] = {
    val edgesRev   = edges.map(e => Edge(e.nodeB, e.nodeA, e.weight))
    val mapEntries = edges
      .concat(edgesRev)
      .groupBy(e => e.nodeA)
      .view
      .mapValues(edges => edges.map(e => e.nodeB -> e.weight).toMap)
      .toSeq
    new UndiGraphHMap(HashMap(mapEntries: _*))
  }

}
