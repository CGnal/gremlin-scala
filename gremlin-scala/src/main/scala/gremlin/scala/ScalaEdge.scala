package gremlin.scala

import shapeless._
import scala.collection.JavaConversions._

case class ScalaEdge(edge: Edge) extends ScalaElement[Edge] {
  override def element = edge

  override def setProperty(key: String, value: Any): Edge = {
    element.property(key, value)
    edge
  }

  def setProperties(properties: Map[String, Any]): Edge = {
    properties foreach { case (k, v) ⇒ setProperty(k, v) }
    edge
  }

  def setProperties[T <: Product: Marshallable](cc: T): Edge = {
    val (_, _, properties) = implicitly[Marshallable[T]].fromCC(cc)
    setProperties(properties)
    edge
  }

  override def removeProperty(key: String): Edge = {
    val p = property(key)
    if (p.isPresent) p.remove()
    edge
  }

  override def removeProperties(keys: String*): Edge = {
    keys foreach removeProperty
    edge
  }

  def toCC[T <: Product: Marshallable] =
    implicitly[Marshallable[T]].toCC(edge.id, edge.valueMap)

  override def start() = GremlinScala[Edge, HNil](__(edge))

  override def properties[A: DefaultsToAny]: Stream[Property[A]] =
    edge.properties[A](keys.toSeq: _*).toStream

  override def properties[A: DefaultsToAny](wantedKeys: String*): Stream[Property[A]] =
    edge.properties[A](wantedKeys: _*).toStream

  //TODO: wait until this is consistent in T3 between Vertex and Edge
  //currently Vertex.outE returns a GraphTraversal, Edge.inV doesnt quite exist
  //def inV() = GremlinScala[Vertex, HNil](edge.inV())
}
