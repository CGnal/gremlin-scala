package gremlin.scala.dsl

import gremlin.scala._
import shapeless._
import shapeless.ops.hlist.Tupler
import shapeless.ops.product.ToHList

trait Constructor[DomainType, LabelsDomain <: HList] {
  type GraphType
  type LabelsGraph <: HList
  type StepsType
  def apply(raw: GremlinScala[GraphType, LabelsGraph]): StepsType
}

object Constructor extends LowPriorityConstructorImplicits {
  type Aux[DomainType, LabelsDomain <: HList, GraphTypeOut, LabelsGraphOut <: HList, StepsTypeOut] = Constructor[DomainType, LabelsDomain] {
    type GraphType = GraphTypeOut
    type LabelsGraph = LabelsGraphOut 
    type StepsType = StepsTypeOut
  }
}

trait LowPriorityConstructorImplicits extends LowestPriorityConstructorImplicits {

  /* TODO: derive LabelsGraph via implicit: labelsConverter: Converter.Aux[LabelsDomain, LabelsGraph] */
  def forBaseType[A, LabelsDomain <: HList, LabelsGraph1 <: HList](implicit converter: Converter.Aux[A, A]) = new Constructor[A, LabelsDomain] {
    type GraphType = A
    type LabelsGraph = LabelsGraph1
    type StepsType = Steps[A, A, LabelsDomain, LabelsGraph]
    def apply(raw: GremlinScala[GraphType, LabelsGraph]) = new Steps[A, A, LabelsDomain, LabelsGraph](raw)
  }

  implicit def forUnit[LabelsDomain <: HList, LabelsGraph <: HList] = forBaseType[Unit, LabelsDomain, LabelsGraph]
  implicit def forString[LabelsDomain <: HList, LabelsGraph <: HList] = forBaseType[String, LabelsDomain, LabelsGraph]
  implicit def forInt[LabelsDomain <: HList, LabelsGraph <: HList] = forBaseType[Int, LabelsDomain, LabelsGraph]
  implicit def forDouble[LabelsDomain <: HList, LabelsGraph <: HList] = forBaseType[Double, LabelsDomain, LabelsGraph]
  implicit def forFloat[LabelsDomain <: HList, LabelsGraph <: HList] = forBaseType[Float, LabelsDomain, LabelsGraph]
  implicit def forBoolean[LabelsDomain <: HList, LabelsGraph <: HList] = forBaseType[Boolean, LabelsDomain, LabelsGraph]
  implicit def forInteger[LabelsDomain <: HList, LabelsGraph <: HList] = forBaseType[Integer, LabelsDomain, LabelsGraph]
  implicit def forJDouble[LabelsDomain <: HList, LabelsGraph <: HList] = forBaseType[java.lang.Double, LabelsDomain, LabelsGraph]
  implicit def forJFloat[LabelsDomain <: HList, LabelsGraph <: HList] = forBaseType[java.lang.Float, LabelsDomain, LabelsGraph]

  def forDomainNode[
    DomainType <: DomainRoot,
    LabelsDomain <: HList,
    LabelsGraph1 <: HList,
    StepsTypeOut <: NodeSteps[DomainType, LabelsDomain, LabelsGraph1]](
    constr: GremlinScala[Vertex, LabelsGraph1] => StepsTypeOut) = new Constructor[DomainType, LabelsDomain] {
    type GraphType = Vertex
    type StepsType = StepsTypeOut
    type LabelsGraph = LabelsGraph1

    def apply(raw: GremlinScala[GraphType, LabelsGraph]): StepsTypeOut = constr(raw)
  }

  implicit def forList[
    A,
    AGraphType,
    LabelsDomain <: HList,
    LabelsGraph1 <: HList,
    AStepsType](implicit aConverter: Converter.Aux[A, AGraphType]) = new Constructor[List[A], LabelsDomain] {
    type GraphType = List[AGraphType]
    type LabelsGraph = LabelsGraph1
    type StepsType = Steps[List[A], List[AGraphType], LabelsDomain, LabelsGraph]
    def apply(raw: GremlinScala[GraphType, LabelsGraph]) =
      new Steps[List[A], List[AGraphType], LabelsDomain, LabelsGraph](raw)
  }

  implicit def forSet[
    A,
    AGraphType,
    LabelsDomain <: HList,
    LabelsGraph1 <: HList,
    AStepsType](implicit aConverter: Converter.Aux[A, AGraphType]) = new Constructor[Set[A], LabelsDomain] {
    type GraphType = Set[AGraphType]
    type LabelsGraph = LabelsGraph1
    type StepsType = Steps[Set[A], Set[AGraphType], LabelsDomain, LabelsGraph]
    def apply(raw: GremlinScala[GraphType, LabelsGraph]) =
      new Steps[Set[A], Set[AGraphType], LabelsDomain, LabelsGraph](raw)
  }

  implicit val forHNil = new Constructor[HNil, HNil] {
    type GraphType = HNil
    type LabelsGraph = HNil
    type StepsType = Steps[HNil, HNil, HNil, HNil]
    def apply(raw: GremlinScala[HNil, HNil]) = new Steps[HNil, HNil, HNil, HNil](raw)
  }

  implicit def forHList[
    H,
    HGraphType,
    LabelsDomain <: HList,
    LabelsGraph1 <: HList,
    HStepsType,
    T <: HList,
    TGraphType <: HList,
    TStepsType](
    implicit
    hConstr: Constructor.Aux[H, LabelsDomain, HGraphType, LabelsGraph1, HStepsType],
    tConstr: Constructor.Aux[T, LabelsDomain, TGraphType, LabelsGraph1, TStepsType],
    converter: Converter.Aux[H :: T, HGraphType :: TGraphType]) =
      new Constructor[H :: T, LabelsDomain] {
        type GraphType = HGraphType :: TGraphType
        type LabelsGraph = LabelsGraph1
        type StepsType = Steps[H :: T, HGraphType :: TGraphType, LabelsDomain, LabelsGraph]
        def apply(raw: GremlinScala[GraphType, LabelsGraph]): StepsType =
          new Steps[H :: T, HGraphType :: TGraphType, LabelsDomain, LabelsGraph](raw)
    }
}

trait LowestPriorityConstructorImplicits {
  // for all Products, e.g. tuples, case classes etc
  implicit def forGeneric[
    T, Repr <: HList,
    GraphTypeHList <: HList,
    GraphTypeTuple <: Product,
    LabelsDomain <: HList,
    LabelsGraph1 <: HList,
    StepsType0 <: StepsRoot,
    EndDomainHList <: HList,
    EndDomainTuple <: Product
  ](implicit
    gen: Generic.Aux[T, Repr],
    constr: Constructor.Aux[Repr, LabelsDomain, GraphTypeHList, LabelsGraph1, StepsType0],  
    graphTypeTupler: Tupler.Aux[GraphTypeHList, GraphTypeTuple], 
    eq: StepsType0#EndDomain0 =:= EndDomainHList,
    tupler: Tupler.Aux[EndDomainHList, EndDomainTuple],
    converter: Converter.Aux[T, GraphTypeTuple]) =
    new Constructor[T, LabelsDomain] {
      type GraphType = GraphTypeTuple
      type LabelsGraph = LabelsGraph1
      type StepsType = Steps[T, GraphType, LabelsDomain, LabelsGraph]
      def apply(raw: GremlinScala[GraphType, LabelsGraph]): StepsType =
        new Steps[T, GraphType, LabelsDomain, LabelsGraph](raw)
    }
}
