package com.tinkerpop.gremlin.scala

import java.lang.{ Long ⇒ JLong }
import java.util.{ Comparator, List ⇒ JList, Map ⇒ JMap, Collection ⇒ JCollection }

import collection.JavaConversions._
import collection.mutable
import com.tinkerpop.gremlin._
import com.tinkerpop.gremlin.process._
import com.tinkerpop.gremlin.process.graph.GraphTraversal
import com.tinkerpop.gremlin.process.T
import com.tinkerpop.gremlin.structure._
import com.tinkerpop.gremlin.util.function.SConsumer
import com.tinkerpop.gremlin.util.function.SPredicate
import shapeless._
import shapeless.ops.hlist._

case class GremlinScala[Types <: HList, End](traversal: GraphTraversal[_, End]) {
  def toSeq(): Seq[End] = traversal.toList.toSeq
  def toList(): List[End] = traversal.toList.toList
  def toSet(): Set[End] = traversal.toList.toSet
  def head(): End = toList.head
  def headOption(): Option[End] = Option(head)
  /** execute pipeline - applies all side effects */
  def iterate() = {
    traversal.iterate()
    GremlinScala[Types, End](traversal)
  }

  def filter(p: End ⇒ Boolean) = GremlinScala[Types, End](traversal.filter(new SPredicate[Traverser[End]] {
    override def test(h: Traverser[End]): Boolean = p(h.get)
  }))

  def count()(implicit p: Prepend[Types, JLong :: HNil]) = GremlinScala[p.Out, JLong](traversal.count())

  def map[A](fun: End ⇒ A)(implicit p: Prepend[Types, A :: HNil]) =
    GremlinScala[p.Out, A](traversal.map[A] { t: Traverser[End] ⇒ fun(t.get) })

  def mapWithTraverser[A](fun: Traverser[End] ⇒ A)(implicit p: Prepend[Types, A :: HNil]) =
    GremlinScala[p.Out, A](traversal.map[A](fun))

  def path()(implicit p: Prepend[Types, Types :: HNil]) =
    GremlinScala[p.Out, Types](traversal.addStep(new TypedPathStep[End, Types](traversal)))

  def select()(implicit p: Prepend[Types, JMap[String, End] :: HNil]) =
    GremlinScala[p.Out, JMap[String, End]](traversal.select())

  def select(asLabels: Seq[String])(implicit p: Prepend[Types, JMap[String, End] :: HNil]) =
    GremlinScala[p.Out, JMap[String, End]](traversal.select(asLabels: JList[String]))

  def order() = GremlinScala[Types, End](traversal.order())
  def order(lessThan: (End, End) ⇒ Boolean) =
    GremlinScala[Types, End](traversal.order(new Comparator[Traverser[End]]() {
      override def compare(a: Traverser[End], b: Traverser[End]) =
        if (lessThan(a.get, b.get)) -1
        else 0
    }))

  def shuffle() = GremlinScala[Types, End](traversal.shuffle())

  def simplePath() = GremlinScala[Types, End](traversal.simplePath())
  def cyclicPath() = GremlinScala[Types, End](traversal.cyclicPath())

  def dedup() = GremlinScala[Types, End](traversal.dedup())

  def dedup[A](uniqueFun: End ⇒ A) = GremlinScala[Types, End](traversal.dedup(liftTraverser(uniqueFun)))

  def aggregate() = GremlinScala[Types, End](traversal.aggregate())
  def aggregate(sideEffectKey: String) = GremlinScala[Types, End](traversal.aggregate(sideEffectKey))

  def aggregate[A](preAggregateFunction: End ⇒ A) =
    GremlinScala[Types, End](traversal.aggregate(liftTraverser(preAggregateFunction)))

  def aggregate[A](sideEffectKey: String, preAggregateFunction: End ⇒ A) =
    GremlinScala[Types, End](traversal.aggregate(sideEffectKey, liftTraverser(preAggregateFunction)))

  def except(someObject: End) = GremlinScala[Types, End](traversal.except(someObject))
  def except(list: Iterable[End]) = GremlinScala[Types, End](traversal.except(list))
  /** not named `except` because type End could be String */
  def exceptVar(variable: String) = GremlinScala[Types, End](traversal.except(variable))

  /** keeps element on a probabilistic base - probability range: 0.0 (keep none) - 1.0 - keep all */
  def random(probability: Double) = GremlinScala[Types, End](traversal.random(probability))

  def range(low: Int, high: Int) = GremlinScala[Types, End](traversal.range(low, high))

  def retain(variable: String) = GremlinScala[Types, End](traversal.retain(variable))
  def retainOne(retainObject: End) = GremlinScala[Types, End](traversal.retain(retainObject))
  def retainAll(retainCollection: Seq[End]) = GremlinScala[Types, End](traversal.retain(retainCollection))

  def as(name: String) = GremlinScala[Types, End](traversal.as(name))
  def back[A](to: String)(implicit p: Prepend[Types, A :: HNil]) =
    GremlinScala[p.Out, A](traversal.back[A](to))

  def `with`[A <: AnyRef, B <: AnyRef](tuples: (A, B)*) = {
    val flattened = tuples.foldLeft(Seq.empty[AnyRef]) {
      case (acc, (k, v)) ⇒
        acc ++: Seq(k, v)
    }
    GremlinScala[Types, End](traversal.`with`(flattened: _*))
  }

  def label()(implicit p: Prepend[Types, String :: HNil]) =
    GremlinScala[p.Out, String](traversal.label())

  def sideEffect(traverse: Traverser[End] ⇒ Any) =
    GremlinScala[Types, End](traversal.sideEffect(
      new SConsumer[Traverser[End]] {
        override def accept(t: Traverser[End]) = traverse(t)
      })
    )

  // note that groupCount is a side effect step, other than the 'count' step..
  // https://groups.google.com/forum/#!topic/gremlin-users/5wXSizpqRxw
  def groupCount() = GremlinScala[Types, End](traversal.groupCount())

  def groupCount(sideEffectKey: String) = GremlinScala[Types, End](traversal.groupCount(sideEffectKey))

  def groupCount[A](preGroupFunction: End ⇒ A) =
    GremlinScala[Types, End](traversal.groupCount(liftTraverser(preGroupFunction)))

  def groupCount[A](sideEffectKey: String, preGroupFunction: End ⇒ A) =
    GremlinScala[Types, End](traversal.groupCount(sideEffectKey, liftTraverser(preGroupFunction)))

  def groupBy[A](keyFunction: End ⇒ A) =
    GremlinScala[Types, End](traversal.groupBy(liftTraverser(keyFunction)))

  def groupBy[A, B](keyFunction: End ⇒ A, valueFunction: End ⇒ B) =
    GremlinScala[Types, End](traversal.groupBy(
      liftTraverser(keyFunction), 
      liftTraverser(valueFunction)))

  def groupBy[A](sideEffectKey: String, keyFunction: End ⇒ A) =
    GremlinScala[Types, End](traversal.groupBy(
      sideEffectKey,
      liftTraverser(keyFunction)))

  //TODO change reduceFunction to type Traversable[B] => C
  def groupBy[A, B, C](
    keyFunction: End ⇒ A,
    valueFunction: End ⇒ B,
    reduceFunction: JCollection[_] ⇒ _) =
    GremlinScala[Types, End](traversal.groupBy(
      liftTraverser(keyFunction),
      liftTraverser(valueFunction),
      reduceFunction))

  def groupBy[A, B](sideEffectKey: String, keyFunction: End ⇒ A, valueFunction: End ⇒ B) =
    GremlinScala[Types, End](traversal.groupBy(
      sideEffectKey, 
      liftTraverser(keyFunction), 
      liftTraverser(valueFunction)))

  def groupBy[A, B, C](
    sideEffectKey: String,
    keyFunction: End ⇒ A,
    valueFunction: End ⇒ B,
    reduceFunction: JCollection[_] ⇒ _) =
    GremlinScala[Types, End](traversal.groupBy(
      sideEffectKey,
      liftTraverser(keyFunction), 
      liftTraverser(valueFunction), 
      reduceFunction))

  ///////////////////// BRANCH STEPS /////////////////////
  def jump(as: String) = GremlinScala[Types, End](traversal.jump(as))

  def jump(as: String, loops: Int) = GremlinScala[Types, End](traversal.jump(as, loops))

  def jump(as: String, ifPredicate: End ⇒ Boolean) =
    GremlinScala[Types, End](traversal.jump(as, liftTraverser(ifPredicate)))

  def jumpWithTraverser(as: String, ifPredicate: Traverser[End] ⇒ Boolean) =
    GremlinScala[Types, End](traversal.jump(as, ifPredicate))

  def jump(as: String, loops: Int, emitPredicate: End ⇒ Boolean) =
    GremlinScala[Types, End](traversal.jump(
      as, loops, liftTraverser(emitPredicate)))

  def jumpWithTraverser(as: String, loops: Int, emitPredicate: Traverser[End] ⇒ Boolean) =
    GremlinScala[Types, End](traversal.jump(as, loops, emitPredicate))

  def jump(as: String,
           ifPredicate: End ⇒ Boolean,
           emitPredicate: End ⇒ Boolean) =
    GremlinScala[Types, End](traversal.jump(
      as, 
      liftTraverser(ifPredicate),
      liftTraverser(emitPredicate)))

  def jumpWithTraverser(as: String,
           ifPredicate: Traverser[End] ⇒ Boolean,
           emitPredicate: Traverser[End] ⇒ Boolean) =
    GremlinScala[Types, End](traversal.jump(as, ifPredicate, emitPredicate))
}

case class ScalaGraph(graph: Graph) extends AnyVal {
  def addVertex(): ScalaVertex = ScalaVertex(graph.addVertex())
  def addVertex(id: AnyRef): ScalaVertex = addVertex(id, Map.empty)
  def addVertex(id: AnyRef, properties: Map[String, Any]): ScalaVertex = {
    val v = ScalaVertex(graph.addVertex(T.id, id))
    v.setProperties(properties)
    v
  }

  /** get vertex by id */
  def v(id: AnyRef): Option[ScalaVertex] = graph.v(id) match {
    case v: Vertex ⇒ Some(ScalaVertex(v))
    case _         ⇒ None
  }

  /** get edge by id */
  def e(id: AnyRef): Option[ScalaEdge] = graph.e(id) match {
    case e: Edge ⇒ Some(ScalaEdge(e))
    case _       ⇒ None
  }

  /** get all vertices */
  def V() = GremlinScala[Vertex :: HNil, Vertex](graph.V.asInstanceOf[GraphTraversal[_, Vertex]])
  /** get all edges */
  def E() = GremlinScala[Edge :: HNil, Edge](graph.E.asInstanceOf[GraphTraversal[_, Edge]])
}

object GS {
  // GS(graph) as a shorthand for GremlinScala(graph)
  def apply(graph: Graph) = GremlinScala(graph)
}

object GremlinScala {
  def apply(graph: Graph) = ScalaGraph(graph)

  class GremlinElementSteps[Types <: HList, End <: Element](gremlinScala: GremlinScala[Types, End])
      extends GremlinScala[Types, End](gremlinScala.traversal) {

    def properties(keys: String*)(implicit p: Prepend[Types, Property[Any] :: HNil]) =
      GremlinScala[p.Out, Property[Any]](traversal.properties(keys: _*)
        .asInstanceOf[GraphTraversal[_, Property[Any]]])

    def propertyMap(keys: String*)(implicit p: Prepend[Types, JMap[String, Any] :: HNil]) =
      GremlinScala[p.Out, JMap[String, Any]](traversal.propertyMap(keys: _*))

    def value[A](key: String)(implicit p: Prepend[Types, A :: HNil]) =
      GremlinScala[p.Out, A](traversal.value[A](key))
    def value[A](key: String, default: A)(implicit p: Prepend[Types, A :: HNil]) =
      GremlinScala[p.Out, A](traversal.value[A](key, default))

    //TODO return a scala map. problem: calling .map adds a step to the pipeline which changes the result of path...
    def values(keys: String*)(implicit p: Prepend[Types, JMap[String, AnyRef] :: HNil]) =
      GremlinScala[p.Out, JMap[String, AnyRef]](traversal.values(keys: _*))

    def has(key: String) = GremlinScala[Types, End](traversal.has(key))

    def has(key: String, value: Any) = GremlinScala[Types, End](traversal.has(key, value))

    def has(accessor: T, value: Any) = GremlinScala[Types, End](traversal.has(accessor, value))

    def has(key: String, predicate: T, value: Any) = GremlinScala[Types, End](traversal.has(key, predicate, value))

    def has(key: String, t: T, value: Seq[_]) = GremlinScala[Types, End](traversal.has(key, t, asJavaCollection(value)))

    def has(accessor: T, predicate: T, value: Any) = GremlinScala[Types, End](traversal.has(accessor, predicate, value))

    def has(accessor: T, predicate: T, value: Seq[_]) = GremlinScala[Types, End](traversal.has(accessor, predicate, asJavaCollection(value)))

    // def has(key: String, predicate: (End, ??) ⇒ Boolean, value: Any) = GremlinScala[Types, End](traversal.has(key, predicate, value))

    def has(label: String, key: String, value: Any) = 
      GremlinScala[Types, End](traversal.has(label, key, value))

    def has(label: String, key: String, value: Seq[_]) = GremlinScala[Types, End](traversal.has(label, key, asJavaCollection(value)))

    def has(label: String, key: String, predicate: T, value: Any) = GremlinScala[Types, End](traversal.has(label, key, predicate, value))

    def has(label: String, key: String, predicate: T, value: Seq[_]) = GremlinScala[Types, End](traversal.has(label, key, predicate, asJavaCollection(value)))

    // def has(label: String, key: String, predicate: (End, ??) ⇒ Boolean, value: Any) = GremlinScala[Types, End](traversal.has(label, key, predicate, value))

    def hasNot(key: String) = GremlinScala[Types, End](traversal.hasNot(key))

    /* startValue: greaterThanEqual
   * endValue: less than */
    def interval[A, B](key: String, startValue: Comparable[A], endValue: Comparable[B]) =
      GremlinScala[Types, End](traversal.interval(key, startValue, endValue))
  }

  class GremlinVertexSteps[Types <: HList, End <: Vertex](gremlinScala: GremlinScala[Types, End])
      extends GremlinScala[Types, End](gremlinScala.traversal) {

    def out()(implicit p: Prepend[Types, Vertex :: HNil]) = GremlinScala[p.Out, Vertex](traversal.out())
    def out(labels: String*)(implicit p: Prepend[Types, Vertex :: HNil]) = GremlinScala[p.Out, Vertex](traversal.out(labels: _*))
    def out(branchFactor: Int, labels: String*)(implicit p: Prepend[Types, Vertex :: HNil]) = GremlinScala[p.Out, Vertex](traversal.out(branchFactor, labels: _*))

    def outE()(implicit p: Prepend[Types, Edge :: HNil]) = GremlinScala[p.Out, Edge](traversal.outE())
    def outE(labels: String*)(implicit p: Prepend[Types, Edge :: HNil]) = GremlinScala[p.Out, Edge](traversal.outE(labels: _*))
    def outE(branchFactor: Int, labels: String*)(implicit p: Prepend[Types, Edge :: HNil]) = GremlinScala[p.Out, Edge](traversal.outE(branchFactor, labels: _*))

    def in()(implicit p: Prepend[Types, Vertex :: HNil]) = GremlinScala[p.Out, Vertex](traversal.in())
    def in(labels: String*)(implicit p: Prepend[Types, Vertex :: HNil]) = GremlinScala[p.Out, Vertex](traversal.in(labels: _*))
    def in(branchFactor: Int, labels: String*)(implicit p: Prepend[Types, Vertex :: HNil]) = GremlinScala[p.Out, Vertex](traversal.in(branchFactor, labels: _*))

    def inE()(implicit p: Prepend[Types, Edge :: HNil]) = GremlinScala[p.Out, Edge](traversal.inE())
    def inE(labels: String*)(implicit p: Prepend[Types, Edge :: HNil]) = GremlinScala[p.Out, Edge](traversal.inE(labels: _*))
    def inE(branchFactor: Int, labels: String*)(implicit p: Prepend[Types, Edge :: HNil]) = GremlinScala[p.Out, Edge](traversal.inE(branchFactor, labels: _*))

    def both()(implicit p: Prepend[Types, Vertex :: HNil]) = GremlinScala[p.Out, Vertex](traversal.both())
    def both(labels: String*)(implicit p: Prepend[Types, Vertex :: HNil]) = GremlinScala[p.Out, Vertex](traversal.both(labels: _*))
    def both(branchFactor: Int, labels: String*)(implicit p: Prepend[Types, Vertex :: HNil]) = GremlinScala[p.Out, Vertex](traversal.both(branchFactor, labels: _*))

    def bothE()(implicit p: Prepend[Types, Edge :: HNil]) = GremlinScala[p.Out, Edge](traversal.bothE())
    def bothE(labels: String*)(implicit p: Prepend[Types, Edge :: HNil]) = GremlinScala[p.Out, Edge](traversal.bothE(labels: _*))
    def bothE(branchFactor: Int, labels: String*)(implicit p: Prepend[Types, Edge :: HNil]) = GremlinScala[p.Out, Edge](traversal.bothE(branchFactor, labels: _*))
  }

  class GremlinEdgeSteps[Types <: HList, End <: Edge](gremlinScala: GremlinScala[Types, End])
      extends GremlinScala[Types, End](gremlinScala.traversal) {

    def inV(implicit p: Prepend[Types, Vertex :: HNil]) = GremlinScala[p.Out, Vertex](traversal.inV)

    def outV(implicit p: Prepend[Types, Vertex :: HNil]) = GremlinScala[p.Out, Vertex](traversal.outV)

    def bothV()(implicit p: Prepend[Types, Vertex :: HNil]) = GremlinScala[p.Out, Vertex](traversal.bothV())

    def otherV()(implicit p: Prepend[Types, Vertex :: HNil]) = GremlinScala[p.Out, Vertex](traversal.otherV())
  }
}
