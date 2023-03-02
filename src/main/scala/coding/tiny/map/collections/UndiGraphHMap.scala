package coding.tiny.map.collections

import cats.{Eq, Hash, Monoid, Order}
import cats.implicits._
import coding.tiny.map.collections.Graph.{Adjacency, Edge}

import scala.annotation.tailrec
import scala.collection.immutable.{HashMap, HashSet, TreeSet}

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
    g.get(edge.nodeA).exists(adj => adj.contains(edge.nodeB)) &&
      g.get(edge.nodeB).exists(adj => adj.contains(edge.nodeA))

  def insertNode(node: N): Graph[N, W] =
    if (!hasNode(node))
      new UndiGraphHMap(g.updated(node, Map()))
    else
      this

  def insertEdge(edge: Edge[N, W]): Graph[N, W] =
    if (!hasEdge(edge)) {
      val adjA    = g.getOrElse(edge.nodeA, Map())
      val adjB    = g.getOrElse(edge.nodeB, Map())
      val updated = g
        .updated(edge.nodeA, adjA + (edge.nodeB -> edge.weight))
        .updated(edge.nodeB, adjB + (edge.nodeA -> edge.weight))

      new UndiGraphHMap(updated)
    } else
      this

  def removeNodes(nodes: Vector[N]): Graph[N, W] =
    UndiGraphHMap(edges.filterNot(e => nodes.contains(e.nodeA) || nodes.contains(e.nodeB)))

  def removeEdges(edgesToRemove: Vector[Edge[N, W]]): Graph[N, W] =
    UndiGraphHMap(edgesToRemove.filterNot(edgesToRemove.contains))

  def adjacencyOf(node: N): Adjacency[N, W] = g.getOrElse(node, Map[N, W]())

  def neighboursOf(node: N): Vector[N] = adjacencyOf(node).keys.toVector

  def mapEdges[M: Hash, Z: Monoid: Order](f: Edge[N, W] => Edge[M, Z]): Graph[M, Z] =
    UndiGraphHMap(
      edges.map(f)
    )

  private def shortestDistDijkstra(n: N, m: N): W = {

    @tailrec
    def shortestDistanceTailRec(
        n: N,
        m: N,
        fringe: Vector[(W, Vector[N])],
        visited: HashSet[N]
    ): W = {
      fringe match {
        case (dist, path @ (node +: tail)) +: restFringe =>
          if (node === m)
            dist
          else {
            val newPaths  = adjacencyOf(node)
              .filter { case (nextNode, _) => !visited.contains(nextNode) }
              .map { case (nextNode, weight) => (weight |+| dist, path.prepended(nextNode)) }
            val newFringe = (restFringe ++ newPaths).sortWith { case ((d1, _), (d2, _)) =>
              Order[W].lt(d1, d2)
            }

            shortestDistanceTailRec(n, m, newFringe, visited + node)
          }

        case _ => Monoid[W].empty
      }
    }

    shortestDistanceTailRec(n, m, Vector(Monoid[W].empty -> Vector(n)), HashSet.empty[N])
  }

  override def shortestPath(n: N, m: N): Vector[Edge[N, W]] = ???

  override def shortestDistance(n: N, m: N): W = shortestDistDijkstra(n, m)

  override def reverse: Graph[N, W] = this

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
