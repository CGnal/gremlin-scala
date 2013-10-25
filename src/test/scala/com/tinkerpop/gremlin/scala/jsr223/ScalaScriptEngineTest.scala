package com.tinkerpop.gremlin.scala.jsr223

import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory
import com.tinkerpop.gremlin.scala.ScalaVertex._
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.tinkerpop.blueprints._
import com.tinkerpop.blueprints.impls.tg.TinkerGraph
import scala.collection.JavaConversions._
import scala.collection.mutable.Map
import javax.script.ScriptEngineManager
import javax.script.ScriptException
import com.tinkerpop.gremlin.scala.Imports

@RunWith(classOf[JUnitRunner])
class ScalaScriptEngineTest extends FunSpec with ShouldMatchers {

  describe("ScriptEngine") {
    val factory = new ScriptEngineManager
    val engine = factory.getEngineByName("gremlin-scala")

    ignore("runs a simple command") {
      engine.eval(""" "dummy response" """).toString should endWith("dummy response")
    }

    ignore("remembers the defined vals") {
      engine.eval("val a = 40").toString should be("a: Int = 40")
      engine.eval("val b = a + 2").toString should be("b: Int = 42")
    }

    ignore("throws ScriptException for invalid scala") {
      intercept[ScriptException] {
        engine.eval("this is no valid scala code")
      }
    }

    ignore("can create a graph") {
      engine.eval("val g = TinkerGraphFactory.createTinkerGraph").toString should
        be("g: com.tinkerpop.blueprints.impls.tg.TinkerGraph = tinkergraph[vertices:6 edges:6]")
    }

  }

}
