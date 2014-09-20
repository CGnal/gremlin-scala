package com.tinkerpop.gremlin.scala

import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import com.tinkerpop.blueprints._
import com.tinkerpop.blueprints.impls.tg.TinkerGraph
import scala.collection.JavaConversions._
import scala.collection.mutable.Map

class SampleUsageTest extends FunSpec with ShouldMatchers with TestGraph {

  it("iterates all vertices and edges") {
    val graph = TinkerGraphFactory.createTinkerGraph
    GremlinScalaPipeline(graph).V.toList.size shouldBe 6
    GremlinScalaPipeline(graph).E.toList.size shouldBe 6

    GremlinScalaPipeline.fromElements(graph.getVertices("lang", "java")).toSet map (
      _.getProperty[String]("name")) shouldBe Set("lop", "ripple")
  }

  describe("Usage with Tinkergraph") {
    it("finds all names of vertices") {
      vertices.name.toSet should be(Set("lop", "vadas", "marko", "peter", "ripple", "josh"))
    }

    it("has different ways to get the properties of a vertex") {
      val vertex: ScalaVertex = graph.v(1)

      //dynamic invocation for property is untyped and may return null, like the groovy dsl
      vertex.name should be("marko")
      vertex.nonExistentProperty should equal(null: Any)

      //apply method returns Any type
      vertex("age") should be(29)
      vertex("nonExistentProperty") should equal(null: Any)

      //property returns Some[A] if element present and of type A, otherwise None
      vertex.property[Integer]("age") should be(Some(29))
      // vertex.property[Int]("age") should be(Some(29)) //only works from Scala 2.11
      vertex.property[String]("age") should be(None)
      vertex.property("nonExistentProperty") should equal(None)
    }

    it("finds everybody who is over 30 years old") {
      vertices.filter { v ⇒
        v.property[Integer]("age") match {
          case Some(age) if age > 30 ⇒ true
          case _                     ⇒ false
        }
      }.propertyMap.toSet should be(Set(
        Map("name" -> "peter", "age" -> 35),
        Map("name" -> "josh", "age" -> 32)))
    }

    it("finds who marko knows") {
      val marko = graph.v(1)
      marko.out("knows").map { _("name") }.toSet should be(Set("vadas", "josh"))
    }

    it("finds who marko knows if a given edge property `weight` is > 0.8") {
      val marko = graph.v(1)
      marko.outE("knows").filter { e ⇒
        e.property[java.lang.Float]("weight") match {
          case Some(weight) if weight > 0.8 ⇒ true
          case _                            ⇒ false
        }
      }.inV.propertyMap.toSet should be(Set(Map("name" -> "josh", "age" -> 32)))
    }

    it("finds all vertices") {
      vertices.count should be(6)
      vertices.propertyMap.toSet should be(Set(
        Map("name" -> "lop", "lang" -> "java"),
        Map("age" -> 27, "name" -> "vadas"),
        Map("name" -> "marko", "age" -> 29),
        Map("name" -> "peter", "age" -> 35),
        Map("name" -> "ripple", "lang" -> "java"),
        Map("name" -> "josh", "age" -> 32)))
    }

    it("shuffles the names around randomly") {
      //will produce different output every time you run it
      println(vertices.name.shuffle.toSet)
    }

    describe("Usage with empty Graph") {
      it("creates a vertex with properties") {
        val graph = new TinkerGraph
        val id = 42
        val vertex = graph.addV(id)
        vertex.setProperty("key", "value")

        graph.v(id).property[String]("key") should be(Some("value"))
      }

      it("creates vertices without specific ids") {
        val graph = new TinkerGraph
        graph.addV()
        graph.addV()
        graph.V.count should be(2)
      }

      it("creates edges between vertices") {
        val graph = new TinkerGraph
        val v1 = graph.addV()
        val v2 = graph.addV()
        graph.addE(v1, v2, "label")

        val foundVertices = v1.out("label").toSet
        foundVertices.size should be(1)
        foundVertices.toList(0) should be(v2)
      }
    }

    describe("Graph navigation") {
      it("follows outEdge and inVertex") {
        graph.v(1).outE("created").inV.name.toSet should be(Set("lop"))
      }
    }
  }

}
