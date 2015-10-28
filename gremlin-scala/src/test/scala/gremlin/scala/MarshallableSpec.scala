package gremlin.scala

import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
import org.scalatest.WordSpec
import org.scalatest.Matchers
import shapeless.test.illTyped

case class CCSimple(
  s: String,
  i: Int
)

case class CCWithOption(i: Int, s: Option[String])

case class CCWithOptionId(
  s: String,
  @id id: Option[Int]
)

case class CCWithLabel(
  s: String,
  l: Long,
  o: Option[String],
  seq: Seq[String],
  map: Map[String, String],
  nested: NestedClass
)

@label("the label")
case class CCWithLabelAndId(
  s: String,
  @id id: Int,
  l: Long,
  o: Option[String],
  seq: Seq[String],
  map: Map[String, String],
  nested: NestedClass
) { def randomDef = ??? }

case class NestedClass(s: String)

class NoneCaseClass(s: String)

class MarshallableSpec extends WordSpec with Matchers {

  "marshals / unmarshals case classes" which {

    "only have simple members" in new Fixture {
      val cc = CCSimple("text", 12)
      val v = graph.addVertex(cc)

      val vl = graph.V(v.id).head()
      vl.label shouldBe cc.getClass.getSimpleName
      vl.valueMap should contain("s" → cc.s)
      vl.valueMap should contain("i" → cc.i)
    }

    "contain options" in new Fixture {
      val ccWithOption = CCWithOption(Int.MaxValue, Some("optional value"))
      val v = graph.addVertex(ccWithOption)
      v.toCC[CCWithOption] shouldBe ccWithOption
    }

    "use @label and @id annotations" in new Fixture {
      val v = graph.addVertex(ccWithLabelAndId)

      v.toCC[CCWithLabelAndId] shouldBe ccWithLabelAndId

      val vl = graph.V(v.id).head()
      vl.label shouldBe "the label"
      vl.id shouldBe ccWithLabelAndId.id
      vl.valueMap should contain("s" → ccWithLabelAndId.s)
      vl.valueMap should contain("l" → ccWithLabelAndId.l)
      vl.valueMap should contain("o" → ccWithLabelAndId.o)
      vl.valueMap should contain("seq" → ccWithLabelAndId.seq)
      vl.valueMap should contain("map" → ccWithLabelAndId.map)
      vl.valueMap should contain("nested" → ccWithLabelAndId.nested)
    }

    "have an Option @id annotation" in new Fixture {
      val cc = CCWithOptionId("text", Some(12))
      val v = graph.addVertex(cc)

      v.toCC[CCWithOptionId] shouldBe cc

      val vl = graph.V(v.id).head()
      vl.label shouldBe cc.getClass.getSimpleName
      vl.id shouldBe cc.id.get
      vl.valueMap should contain("s" → cc.s)
    }

    trait Fixture {
      val graph = TinkerGraph.open.asScala

      val ccWithLabelAndId = CCWithLabelAndId(
        "some string",
        Int.MaxValue,
        Long.MaxValue,
        Some("option type"),
        Seq("test1", "test2"),
        Map("key1" → "value1", "key2" → "value2"),
        NestedClass("nested")
      )
    }
  }

  "can't persist a none product type (none case class or tuple)" in {
    illTyped {
      """
        val graph = TinkerGraph.open.asScala
        graph.addVertex(new NoneCaseClass("test"))
      """
    }
  }

}
