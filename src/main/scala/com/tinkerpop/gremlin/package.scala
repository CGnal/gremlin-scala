package com.tinkerpop.gremlin

import java.util.function.{ Function ⇒ JFunction, Predicate ⇒ JPredicate }

import com.tinkerpop.gremlin.process.Traverser
import com.tinkerpop.gremlin.scala.GremlinScala._
import shapeless._
import shapeless.ops.hlist._

package object scala {
  type Vertex = structure.Vertex
  type Edge = structure.Edge
  type Element = structure.Element
  type Graph = structure.Graph

  implicit def wrap(v: Vertex) = ScalaVertex(v)
  implicit def wrap(e: Edge) = ScalaEdge(e)
  implicit def wrap(g: Graph) = ScalaGraph(g)
  implicit def unwrap(g: ScalaGraph) = g.graph

  implicit def toElementSteps[Labels <: HList, End <: Element](gremlinScala: GremlinScala[Labels, End]) =
    new GremlinElementSteps(gremlinScala)

  implicit def toVertexSteps[Labels <: HList, End <: Vertex](gremlinScala: GremlinScala[Labels, End]) =
    new GremlinVertexSteps(gremlinScala)

  implicit def toEdgeSteps[Labels <: HList, End <: Edge](gremlinScala: GremlinScala[Labels, End]) =
    new GremlinEdgeSteps(gremlinScala)

  //TODO make vertexSteps extend elementSteps and return VertexSteps here
  implicit def toElementSteps(v: ScalaVertex): GremlinElementSteps[HNil, Vertex] = v.start

  implicit def toElementSteps(e: ScalaEdge): GremlinElementSteps[HNil, Edge] = e.start

  implicit def toJavaFunction[A, B](f: Function1[A, B]) = new JFunction[A, B] {
    override def apply(a: A): B = f(a)
  }

  implicit def toJavaPredicate[A](f: Function1[A, Boolean]) = new JPredicate[A] {
    override def test(a: A): Boolean = f(a)
  }

  implicit def liftTraverser[A, B](fun: A ⇒ B): Traverser[A] ⇒ B =
    { t: Traverser[A] ⇒ fun(t.get) }
}

