package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import TestGraph._

class FilterSpec extends TestBase {

  it("filters") {
    graph.V
      .filter { _.property("age").orElse(0) > 30 }
      .values[String]("name").toSet should be(Set("josh", "peter"))
  }

  it("has") {
    graph.V.has(Age, 35).value(Name).toSet shouldBe Set("peter")
  }

  it("has - sugar") {
    val g = TinkerGraph.open.asScala
    g + ("software", Name → "blueprints", Created → 2010)

    g.V.has(Name -> "blueprints").head <-- "dependsOn" --- (g + ("software", Name → "gremlin", Created → 2009))
    g.V.has(Name -> "gremlin").head <-- "dependsOn" --- (g + ("software", Name → "gremlinScala"))
    g.V.has(Name -> "gremlinScala").head <-- "createdBy" --- (g + ("person", Name → "mpollmeier"))

    g.V.toList().size shouldBe 4
    g.V.hasLabel("software").toList().size shouldBe 3
    g.V.hasLabel("person").toList().size shouldBe 1

    g.E.toList().size shouldBe 3
    g.E.hasLabel("dependsOn").toList().size shouldBe 2
    g.E.hasLabel("createdBy").toList().size shouldBe 1

    g.asJava.close()
  }

  it("hasNot") {
    graph.V.hasNot(Age, 35).value(Name).toSet shouldBe Set("lop", "marko", "josh", "vadas", "ripple")
  }

  describe("dedup") {
    it("dedups") {
      v(1).out.in.dedup().toList should be(v(1).out.in.toSet.toList)
    }

    // TODO: fix
    ignore("dedups by a given uniqueness function", org.scalatest.Tag("foo")) {
      v(1).out.in
        .dedup().by(_.property[String]("lang").orElse(null))
        .values[String]("name").toList should be(List("marko"))
    }
  }

  //TODO redo with where step
  // describe("except") {
  //   it("emits everything but a given object") {
  //     v(1).out.except(v(2).vertex).values[String]("name")
  //       .toSet should be(Set("lop", "josh"))
  //   }

  //   it("emits everything but an 'except' list") { 
  //     v(1).out.except(List(v(2).vertex)).values[String]("name")
  //       .toSet should be(Set("lop", "josh"))
  //   }

  //   it("emits everything unless the vertex is in a given aggregate variable") {
  //     v(1).out.aggregate("x")
  //       .out.exceptVar("x")
  //       .values[String]("name").toSet should be (Set("ripple"))
  //   }

  //   it("emits everything unless a property is in a given aggregate variable") {
  //     v(1).out
  //       .aggregate("x").by(_.value[String]("name"))
  //       .out.values[String]("name").exceptVar("x")
  //       .toSet should be (Set("ripple"))
  //   }
  // }
}
