package coding.tiny.map.collections

import cats._
import coding.tiny.map.collections.Graph.{Adjacency, Edge}

abstract class Graph[N: Eq, W: Monoid: Order] {

  val nodes: Vector[N]
  val edges: Vector[Edge[N, W]]

  def isEmpty: Boolean
  def hasNode(node: N): Boolean
  def hasEdge(edge: Edge[N, W]): Boolean

  def insertNode(node: N): Graph[N, W]
  def insertOrUpdateEdge(edge: Edge[N, W]): Graph[N, W]

  def removeNodes(nodes: Vector[N]): Graph[N, W]
  def removeEdges(edges: Vector[Edge[N, W]]): Graph[N, W]

  def adjacencyOf(node: N): Adjacency[N, W]
  def neighboursOf(node: N): Vector[N]

  def shortestDistance(n: N, m: N): W

  def reverse: Graph[N, W]

}

object Graph {

  type Adjacency[N, W] = Map[N, W]

  case class Edge[N, W](nodeA: N, nodeB: N, weight: W) {
    def reverse: Edge[N, W] = Edge(nodeB, nodeA, weight)
  }
  object Edge                                          {
    implicit def eqEdge[N, W]: Eq[Edge[N, W]] = Eq.fromUniversalEquals
  }
}
