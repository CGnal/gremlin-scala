package com.tinkerpop.gremlin.scala.transform

import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory
import com.tinkerpop.gremlin.test.ComplianceTest
import com.tinkerpop.gremlin.scala._
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TraversalStepsTest extends FunSpec with ShouldMatchers with TestGraph {

  describe("vertice adjacency") {
    it("gets all vertices") {
      graph.V.toScalaList should have size (6)
    }

    it("gets the out vertices") {
      graph.v(1).out.property("name").toScalaList should be(List("vadas", "josh", "lop"))
    }

    it("gets the in vertices") {
      graph.v(2).in.property("name").toScalaList should be(List("marko"))
    }

    it("gets both in and out vertices") {
      graph.v(4).both.property("name").toScalaList should be(List("marko", "ripple", "lop"))
    }
  }

  describe("edge adjacency") {
    it("gets all edges") {
      graph.E.toScalaList should have size (6)
    }

    it("follows out edges") {
      graph.v(1).outE.label.toScalaList should be(List("knows", "knows", "created"))
    }

    it("follows in edges") {
      graph.v(2).inE.label.toScalaList should be(List("knows"))
    }

    it("follows both edges") {
      graph.v(4).bothE.label.toScalaList should be(List("knows", "created", "created"))
    }
  }

  describe("edge / vertex adjacency") {
    it("follows out edges and in vertices") {
      graph.v(1).outE.inV.property("name").toScalaList should be(List("vadas", "josh", "lop"))
    }

    it("follows in edges and out vertices") {
      graph.v(2).inE.outV.property("name").toScalaList should be(List("marko"))
    }
  }

  describe("vertex edge label adjacency") {
    it("follows out edges by label") {
      graph.v(1).out("knows").property("name").toScalaList should be(List("vadas", "josh"))
      graph.v(1).outE("knows").inV.property("name").toScalaList should be(List("vadas", "josh"))
    }

    it("follows out edges by labels") {
      graph.v(1).out("knows", "created").property("name").toScalaList should be(List("vadas", "josh", "lop"))
      graph.v(1).outE("knows", "created").inV.property("name").toScalaList should be(List("vadas", "josh", "lop"))
    }

    it("traverses multiple steps") {
      graph.v(1).out.out.property("name").toScalaList should be(List("ripple", "lop"))
      graph.v(1).out.out.out.property("name").toScalaList should be(Nil)
    }
  }

}
