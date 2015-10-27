![logo](https://github.com/mpollmeier/gremlin-scala/raw/master/doc/images/gremlin-scala-logo.png)
[![Build Status](https://secure.travis-ci.org/mpollmeier/gremlin-scala.png?branch=master)](http://travis-ci.org/mpollmeier/gremlin-scala)
 [![Join the chat at https://gitter.im/mpollmeier/gremlin-scala](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/mpollmeier/gremlin-scala?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

## Gremlin-Scala for Apache Tinkerpop 3

A wrapper to use [Apache Tinkerpop3](https://github.com/apache/incubator-tinkerpop) - a JVM graph traversal library - from Scala.

* Scala friendly function signatures, aiming to be close to the Scala collection library.
  * use standard Scala functions - no need to worry about how to implement things like `java.util.function.BiPredicate`
* Beautiful DSL to create vertices and edges
* Type safe traversals
* Minimal runtime overhead - only allocates additional instances if absolutely necessary
* Nothing is hidden away, you can always easily access the underlying Gremlin-Java objects if needed, e.g. to access graph db specifics things like indexes

### Getting started
The [examples project](https://github.com/mpollmeier/gremlin-scala-examples) comes with working examples for different graph databases. Typically you just need to add a dependency on `"com.michaelpollmeier" %% "gremlin-scala" % "SOME_VERSION"` and one for the graph db of your choice to your `build.sbt`.

### Using the sbt console
* tl;dr: `sbt gremlin-scala/console`
* start `sbt` in the root project
```
> projects
[info]     gremlin-scala
[info]     macros
[info]   * root
>
```
* Next, change to the gremlin-scala project using `project gremlin-scala`
* Finally, to test out the API in a REPL type `console` 

### Vertices and edges with type safe properties

```scala
import gremlin.scala._
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
val graph = TinkerGraph.open.asScala

// Keys for properties which can later be used for type safe traversals
val Founded = Key[String]("founded")
val Distance = Key[Int]("distance")
val Name = Key[String]("name")
val Population = Key[Int]("population")

// create labelled vertex
val paris = graph + "Paris"

// create vertex with typed properties
val london = graph + ("London", Founded → "43 AD")

// create labelled edges 
paris --- "OneWayRoad" --> london
paris <-- "OtherWayAround" --- london
paris <-- "Eurostar" --> london

// create edge with typed properties
paris --- ("Eurostar", Distance → 495) --> london

// type safe access to properties
london.property(Founded) //Property(founded->43 AD)
london.value2(Founded) //43 AD
paris.out("Eurostar").value(Founded).head //43 AD
paris.outE("Eurostar").value(Distance).head //495
```

More working examples in [SchemaSpec](https://github.com/mpollmeier/gremlin-scala/blob/master/gremlin-scala/src/test/scala/gremlin/scala/SchemaSpec.scala).

### Other means to access properties

```scala
v1.keys // Set(Key("name"), Key("age"))
v1.property(Name) // "marko"
v1.valueMap // Map("name" → "marko", "age" → 29)
v1.valueMap("name", "age") // Map("name" → "marko", "age" → 29)
v1.properties("name", "age").length // 2
v1.properties.length // 2
```

More working examples in [ElementSpec](https://github.com/mpollmeier/gremlin-scala/blob/master/gremlin-scala/src/test/scala/gremlin/scala/ElementSpec.scala)

### Compiler helps to eliminate invalid traversals
Gremlin-Scala aims to helps you at compile time as much as possible. Take this simple example:

```scala
import gremlin.scala._
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
val graph = TinkerGraph.open.asScala
graph.V.outE.inV  //compiles
graph.V.outE.outE //does _not_ compile
```

In Gremlin-Groovy there's nothing stopping you to create the second traversal - it will explode at runtime, as outgoing edges do not have outgoing edges. In Gremlin-Scala this simply doesn't compile.

### Type safe traversals
You can label any step using `as(StepLabel)` and the compiler will infer the correct types for you in the select step using an HList (a type safe list, i.e. the compiler knows the types of the elements of the list). In Gremlin-Java and Gremlin-Groovy you get a `Map[String, Any]`, so you have to cast to the type you *think* it will be, which is ugly and error prone. For example:

```scala
// use :paste in Scala REPL
import gremlin.scala._
import shapeless._
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
def g = TinkerFactory.createModern.asScala

val a = StepLabel[Vertex]()
val b = StepLabel[Edge]()
val c = StepLabel[Double]()

// select all labelled steps
g.V(1).as(a)
.outE.as(b)
.select
.toList
// returns Vertex :: Edge :: HNil for each path

// select subset of labelled steps
g.V(1)
.outE("created").as(b)
.value("weight").as(c)
.select(b :: c :: HNil)
.head

// returns e[9][1-created->3] :: 0.4 :: HNil
// return type is: Edge :: Double :: HNil
```

More working examples in [SelectSpec](https://github.com/mpollmeier/gremlin-scala/blob/master/gremlin-scala/src/test/scala/gremlin/scala/SelectSpec.scala). Kudos to [shapeless](https://github.com/milessabin/shapeless/) and Scala's sophisticated type system that made this possible. 

### Saving / loading case classes
You can save and load case classes as a vertex - this is still experimental but pretty cool. Note: this does _not_ work in a REPL, you have to put it into a test. For examples check out the [MarshallerSpec](https://github.com/mpollmeier/gremlin-scala/blob/master/gremlin-scala/src/test/scala/gremlin/scala/MarshallerSpec.scala).

```scala
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph
val graph = TinkerGraph.open.asScala
case class Example(i: Int, s:Option[String])

it("load a vertex into a case class") {
  val graph = TinkerGraph.open.asScala
  val example = Example(Int.MaxValue, Some("optional value"))
  val v = graph.addVertex(example)
  v.toCC[Example] shouldBe example
}
```

Note that you can also use Options as the example shows.
Thanks to <a href="https://github.com/joan38">joan38</a> for <a href="https://github.com/mpollmeier/gremlin-scala/pull/66">contributing</a> this feature!

### Some more advanced traversals
Here are some examples of more complex traversals from the [examples repo](https://github.com/mpollmeier/gremlin-scala-examples/) which you can easily run on your machine:

What is `Die Hard's` average rating?
```scala
// use :paste in Scala REPL
graph.V.has("movie", "name", "Die Hard")
  .inE("rated")
  .values("stars")
  .mean
  .head
```

Get the maximum number of movies a single user rated
```scala
// use :paste in Scala REPL
g.V.hasLabel("person")
  .flatMap(_.outE("rated").count)
  .max
  .head
```

What 80's action movies do 30-something programmers like?
Group count the movies by their name and sort the group count map in decreasing order by value.
```scala
// use :paste in Scala REPL
g.V
  .`match`(
    __.as("a").hasLabel("movie"),
    __.as("a").out("hasGenre").has("name", "Action"),
    __.as("a").has("year", P.between(1980, 1990)),
    __.as("a").inE("rated").as("b"),
    __.as("b").has("stars", 5),
    __.as("b").outV().as("c"),
    __.as("c").out("hasOccupation").has("name", "programmer"),
    __.as("c").has("age", P.between(30, 40))
  )
  .select[Vertex]("a")
  .map(_.value[String]("name"))
  .groupCount()
  .order(Scope.local).by(Order.valueDecr)
  .limit(Scope.local, 10)
  .head
```

What is the most liked movie in each decade?
```
// use :paste in Scala REPL
g.V()
  .hasLabel("movie")
  .where(_.inE("rated").count().is(P.gt(10)))
  .group { v ⇒
    val year = v.value[Integer]("year")
    val decade = (year / 10)
    (decade * 10): Integer
  }
  .map { moviesByDecade ⇒
    val highestRatedByDecade = moviesByDecade.mapValues { movies ⇒
      movies.toList
        .sortBy { _.inE("rated").values("stars").mean().head }
        .reverse.head //get the movie with the highest mean rating
    }
    highestRatedByDecade.mapValues(_.value[String]("name"))
  }
  .order(Scope.local).by(Order.keyIncr)
  .head
```

## Help - it's open source!
If you would like to help, here's a list of things that needs to be addressed:
* add more graph databases and examples into the [examples project](https://github.com/mpollmeier/gremlin-scala-examples)
* port over more TP3 steps - see [TP3 testsuite](https://github.com/apache/incubator-tinkerpop/tree/master/gremlin-test/src/main/java/org/apache/tinkerpop/gremlin/process/traversal/step) and [Gremlin-Scala StandardTests](https://github.com/mpollmeier/gremlin-scala/blob/master/gremlin-scala/src/test/scala/gremlin/scala/GremlinStandardTestSuite.scala)
* fill this readme and provide other documentation, or how-tos, e.g. a blog post or tutorial

## Further reading
For more information about Gremlin see the [Gremlin docs](http://tinkerpop.incubator.apache.org/docs/3.0.0-incubating/) and the [Gremlin users mailinglist](https://groups.google.com/forum/#!forum/gremlin-users).
Please note that while Gremlin-Scala is very close to the original Gremlin, there a slight differences to Gremlin-Groovy - don't be afraid, they hopefully all make sense to a Scala developer ;)

Random links:
* [Shortest path algorithm with Gremlin-Scala 3.0.0 (Michael
  Pollmeier)](http://www.michaelpollmeier.com/2014/12/27/gremlin-scala-shortest-path/)
* [Shortest path algorithm with Gremlin-Scala 2.4.1 (Stefan Bleibinhaus)](http://bleibinha.us/blog/2013/10/scala-and-graph-databases-with-gremlin-scala)
