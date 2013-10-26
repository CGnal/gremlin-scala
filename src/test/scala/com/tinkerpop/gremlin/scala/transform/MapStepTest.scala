package com.tinkerpop.gremlin.scala.transform

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import com.tinkerpop.gremlin.scala.ScalaVertex
import com.tinkerpop.gremlin.scala.TestGraph

class MapStepTest extends FunSpec with ShouldMatchers with TestGraph {

  it("maps the label of an edge to it's length") {
    edges.label.map { _.size }.toScalaList should be(List(7, 5, 7, 5, 7, 7))
    edges.label.transform { _.size }.toScalaList should be(List(7, 5, 7, 5, 7, 7))
  }

  it("maps the age property of all vertices") {
    vertices.property("age").map { age: Integer ⇒ age * 2 }.toScalaList should be(List(54, 58, 70, 64))
  }

  it("gets the name and the age as tuples") {
    vertices.map { v: ScalaVertex ⇒ (v("name"), v("age")) }.toScalaList should be(List(
      ("lop", null),
      ("vadas", 27),
      ("marko", 29),
      ("peter", 35),
      ("ripple", null),
      ("josh", 32)))
  }

}
