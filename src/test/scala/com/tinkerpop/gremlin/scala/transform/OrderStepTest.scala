package com.tinkerpop.gremlin.scala.transform

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import com.tinkerpop.gremlin.scala.ScalaVertex
import com.tinkerpop.gremlin.scala.TestGraph
import org.scalatest.junit.JUnitRunner
import com.tinkerpop.gremlin.Tokens
import com.tinkerpop.pipes.transform.TransformPipe.Order

@RunWith(classOf[JUnitRunner])
class OrderStepTest extends FunSpec with ShouldMatchers with TestGraph {

  it("orders naturally") {
    graph.V.property("name").order().toScalaList should be(List("josh", "lop", "marko", "peter", "ripple", "vadas"))
  }

  it("orders in decremental order") {
    graph.V.property("name").order(Order.DECR).toScalaList should be(List("vadas", "ripple", "peter", "marko", "lop", "josh"))
  }

  it("orders names by given function") {
    graph.V.property("name").order {
      (left: String, right: String) ⇒ left.compareTo(right)
    }.toScalaList should be(List("josh", "lop", "marko", "peter", "ripple", "vadas"))
  }

  it("orders ages by given function") {
    graph.V.property("age").order {
      (left: Int, right: Int) ⇒ left.compareTo(right)
    }.toScalaList should be(List(27, 29, 32, 35))
  }

}
