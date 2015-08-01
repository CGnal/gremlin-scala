package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.scalatest.FunSpec
import org.scalatest.Matchers

case class ExampleClass(s: String, i: Int, l: Long, o: Option[String], seq: Seq[String], map: Map[String, String])

class MarshallerSpec extends FunSpec with Matchers {
  val example = ExampleClass(
    "some string",
    Int.MaxValue,
    Long.MaxValue,
    Some("option type"),
    Seq("test1", "test2"),
    Map("key1" -> "value1", "key2" -> "value2")
  )

  it("saves a case class as a vertex") {
    val graph = TinkerGraph.open

    val v = graph.addVertex(example)
    v.valueMap should contain ("s" → example.s)
    v.valueMap should contain ("i" → example.i)
    v.valueMap should contain ("l" → example.l)
    v.valueMap should contain ("o" → example.o.get)
    v.valueMap should contain ("seq" → example.seq)
    v.valueMap should contain ("map" → example.map)

    v shouldBe ScalaVertex(graph.V.toList.head)
  }

  it("converts a Vertex into a case class") {
    val graph = TinkerGraph.open

    val v = graph.addVertex(example)

    v.toCC[ExampleClass] shouldBe example
  }
}
