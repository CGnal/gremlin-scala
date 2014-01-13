package com.tinkerpop.gremlin.scala

import com.tinkerpop.gremlin.Tokens
import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec

class GremlinTest extends FunSpec with ShouldMatchers with TestGraph {
  it("specifies the right version") {
    Gremlin.version should be(Tokens.VERSION)
  }

  it("specifies the right language") {
    Gremlin.language should be("gremlin-scala")
  }

  describe("ScalaElement equality and hashCode are based on their id") {
    it("equals") {
      graph.v(1) == graph.v(1) should be(true)
      graph.v(1) == graph.v(2) should be(false)
    }

    it("uses the right hashCodes") {
      graph.v(1).hashCode should be(graph.v(1).hashCode)
      graph.v(1).hashCode should not be (graph.v(2).hashCode)

      Set(graph.v(1)) contains (graph.v(1)) should be(true)
      Set(graph.v(1)) contains (graph.v(2)) should be(false)
    }
  }
}

trait TestGraph {
  val graph: ScalaGraph = TinkerGraphFactory.createTinkerGraph
  //def vertices = graph.V
  //def edges = graph.E

  val v1 = graph.v(1)
  val v2 = graph.v(2)
  val v3 = graph.v(3)
  val v4 = graph.v(4)
  val v5 = graph.v(5)
  val v6 = graph.v(6)

  def print(pipeline: GremlinScala[_,_]) = println(pipeline.toList)
}
