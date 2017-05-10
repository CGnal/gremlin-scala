package gremlin.scala.dsl

import gremlin.scala._
import shapeless._
import scala.collection.mutable

/** root type for all domain types */
trait DomainRoot extends Product {
  // type Underlying
}

/** just a helper trait for extracting type members for Steps */
trait StepsRoot {
  type EndDomain0
  type EndGraph0
  def raw: GremlinScala[EndGraph0, HNil]
}

class Steps[EndDomain, EndGraph](val raw: GremlinScala[EndGraph, HNil])(
  implicit converter: Converter.Aux[EndDomain, EndGraph]) extends StepsRoot {
  type EndDomain0 = EndDomain
  type EndGraph0 = EndGraph

  /* executes traversal and converts results into cpg domain type */
  def toList(): List[EndDomain] = raw.toList.map(converter.toDomain)
  def toSet(): Set[EndDomain] = raw.toSet.map(converter.toDomain)
  def head(): EndDomain = converter.toDomain(raw.head)
  def headOption(): Option[EndDomain] = raw.headOption.map(converter.toDomain)

  def hasId[NewSteps](id: AnyRef)(
    implicit
    isElement: EndGraph <:< Element,
    constr: Constructor.Aux[EndDomain, EndGraph, NewSteps]): NewSteps =
    constr(raw.hasId(id))

  def map[NewEndDomain, NewEndGraph, NewSteps <: StepsRoot](fun: EndDomain ⇒ NewEndDomain)(
    implicit
    newConverter: Converter.Aux[NewEndDomain, NewEndGraph],
    constr: Constructor.Aux[NewEndDomain, NewEndGraph, NewSteps]): NewSteps =
      constr {
        raw.map { endGraph: EndGraph =>
          newConverter.toGraph(fun(converter.toDomain(endGraph)))
        }
      }

  def flatMap[NewSteps <: StepsRoot](fun: EndDomain ⇒ NewSteps)(
    implicit
    constr: Constructor.Aux[NewSteps#EndDomain0, NewSteps#EndGraph0, NewSteps],
    newConverter: Converter[NewSteps#EndDomain0]
  ): NewSteps =
      constr {
        raw.flatMap { endGraph: EndGraph =>
          val newSteps: NewSteps = fun(converter.toDomain(endGraph))
          newSteps.raw.asInstanceOf[GremlinScala[NewSteps#EndGraph0, HNil]]
          // not sure why I need the cast here - should be safe though
        }
      }

}

/* Root class for all your vertex based DSL steps
 * TODO: add support for as/select - currently always HNil
 * TODO: add support for using Edge instead of Vertex?
 */
class NodeSteps[EndDomain <: DomainRoot](override val raw: GremlinScala[Vertex, HNil])(
  implicit marshaller: Marshallable[EndDomain]) extends Steps[EndDomain, Vertex](raw)(
  Converter.forDomainNode[EndDomain](marshaller, raw.traversal.asAdmin.getGraph.get)) {

  /** Aggregate all objects at this point into the given collection, e.g. `mutable.ArrayBuffer.empty[EndDomain]`
    * Uses eager evaluation (as opposed to `store`() which lazily fills a collection)
    */
  def aggregate[NewSteps](into: mutable.Buffer[EndDomain])(
    implicit constr: Constructor.Aux[EndDomain, Vertex, NewSteps]): NewSteps =
    constr(
      raw.sideEffect{ v: Vertex =>
        into += v.toCC[EndDomain]
      }
    )

  // def filter(predicate: Self => Steps[_]): Self = {
  //   val rawWithFilter: GremlinScala[EndGraph, HNil] =
  //     /* TODO: remove cast */
  //     raw.filter{ gs => predicate(construct(gs.asInstanceOf[GremlinScala[Vertex, HNil]])).raw }
  //   construct(rawWithFilter)
  // }

  def filterOnEnd[NewSteps](predicate: EndDomain => Boolean)(
    implicit constr: Constructor.Aux[EndDomain, Vertex, NewSteps]): NewSteps =
    constr(
      raw.filterOnEnd { v: Vertex =>
        predicate(v.toCC[EndDomain])
      }
    )

  // def or(traversals: (Self => Steps[_])*) : Self = {
  //   val foo = traversals.map(
  //     trav => { gs : GremlinScala[Vertex, HNil] => trav(construct(gs)).raw } )
  //   construct(raw.or(foo :_*))
  // }

  // def filterRaw(predicate: GremlinScala[EndGraph, _] => GremlinScala[_, _]): Self =
  //   construct(raw.filter(predicate))

  // def dedup(): Self = construct(raw.dedup())
}
