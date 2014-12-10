package com.tinkerpop.gremlin.scala

import scala.collection.JavaConversions._

import com.tinkerpop.gremlin.process.T
import com.tinkerpop.gremlin.process.Traverser
import com.tinkerpop.gremlin.structure._
import shapeless._

trait ScalaElement[ElementType <: Element] {
  def element: ElementType
  def start(): GremlinScala[ ElementType, HNil]

  def id: AnyRef = element.id
  def label(): String = element.label

  def keys(): Set[String] = element.keys.toSet
  def hiddenKeys: Set[String] = element.hiddenKeys.toSet

  def setProperty(key: String, value: Any): Unit = element.property(key, value)
  def setHiddenProperty(key: String, value: Any): Unit = element.property(Graph.Key.hide(key), value)
  def setProperties(properties: Map[String, Any]): Unit =
    properties foreach { case (k, v) ⇒ setProperty(k, v) }

  def removeProperty(key: String): Unit = {
    val p = property(key)
    if (p.isPresent) p.remove
  }

  def property[A](key: String): Property[A] = element.property[A](key)
  def hiddenProperty[A](key: String): Property[A] = element.property[A](Graph.Key.hide(key))

  def properties(wantedKeys: String*): Seq[Property[Any]] = {
    val requiredKeys = if(!wantedKeys.isEmpty) wantedKeys else keys
    requiredKeys map property[Any] toSeq
  }

  def hiddenProperties(wantedKeys: String*): Seq[Property[Any]] = {
    val requiredKeys = if(!wantedKeys.isEmpty) wantedKeys else hiddenKeys
    requiredKeys map hiddenProperty[Any] toSeq
  }

  def propertyMap(wantedKeys: String*): Map[String, Any] = {
    val requiredKeys = if(!wantedKeys.isEmpty) wantedKeys else keys
    requiredKeys map { key ⇒ (key, getValue(key)) } toMap
  }

  def hiddenPropertyMap(wantedKeys: String*): Map[String, Any] = {
    val requiredKeys = if(!wantedKeys.isEmpty) wantedKeys else hiddenKeys
    requiredKeys map { key ⇒ (key, getHiddenValue(key)) } toMap
  }

  // note: this may throw an IllegalStateException - better use `value`
  def getValue[A](key: String): A = element.value[A](key)

  def value[A](key: String): Option[A] = {
    val p = property[A](key)
    if (p.isPresent) Some(p.value)
    else None
  }

  // note: this may throw an IllegalStateException - better use `hiddenValue`
  def getHiddenValue[A](key: String): A = element.value[A](Graph.Key.hide(key))

  def hiddenValue[A](key: String): Option[A] = {
    val p = hiddenProperty[A](key)
    if (p.isPresent) Some(p.value)
    else None
  }

  def valueMap(): Map[String, Any] =
    keys map { key ⇒ (key, getValue(key)) } toMap

  def valueOrElse[A](key: String, default: ⇒ A): A = property[A](key).orElse(default)

  def remove(): Unit = element.remove()
}

case class ScalaVertex(vertex: Vertex) extends ScalaElement[Vertex] {
  override def element = vertex

  def out() = GremlinScala[ Vertex, HNil](vertex.out())
  def out(labels: String*) = GremlinScala[ Vertex, HNil](vertex.out(labels: _*))

  def outE() = GremlinScala[ Edge, HNil](vertex.outE())
  def outE(labels: String*) = GremlinScala[ Edge, HNil](vertex.outE(labels: _*))

  def in() = GremlinScala[ Vertex, HNil](vertex.in())
  def in(labels: String*) = GremlinScala[ Vertex, HNil](vertex.in(labels: _*))

  def inE() = GremlinScala[ Edge, HNil](vertex.inE())
  def inE(labels: String*) = GremlinScala[ Edge, HNil](vertex.inE(labels: _*))

  def both() = GremlinScala[ Vertex, HNil](vertex.both())
  def both(labels: String*) = GremlinScala[ Vertex, HNil](vertex.both(labels: _*))

  def bothE() = GremlinScala[ Edge, HNil](vertex.bothE())
  def bothE(labels: String*) = GremlinScala[ Edge, HNil](vertex.bothE(labels: _*))

  def addEdge(label: String, inVertex: ScalaVertex, properties: Map[String, Any]): ScalaEdge = {
    val e = ScalaEdge(vertex.addEdge(label, inVertex.vertex))
    e.setProperties(properties)
    e
  }

  def addEdge(id: AnyRef, label: String, inVertex: ScalaVertex, properties: Map[String, Any]): ScalaEdge = {
    val e = ScalaEdge(vertex.addEdge(label, inVertex.vertex, T.id, id))
    e.setProperties(properties)
    e
  }

  def withSideEffect[A](key: String, value: A) = start.withSideEffect(key, value)

  def start() = GremlinScala[ Vertex, HNil](vertex.start)
}

case class ScalaEdge(edge: Edge) extends ScalaElement[Edge] {
  override def element = edge

  def withSideEffect[A](key: String, value: A) = start.withSideEffect(key, value)

  def start() = GremlinScala[ Edge, HNil](edge.start)

  //TODO: wait until this is consistent in T3 between Vertex and Edge
  //currently Vertex.outE returns a GraphTraversal, Edge.inV doesnt quite exist
  //def inV() = GremlinScala[ Vertex, HNil](edge.inV())
}

