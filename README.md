Bewl (original Java version)
============================

This is the original Java version of Bewl from 2008-2011, which I ported to
Clojure and then Scala when the limitations of Java became too painful.

It's Java 1.7 code but at least you can now run it with Gradle:

    ./gradlew test

to run all the tests

    ./gradlew run

which runs the empty 'spike' class BewlSpike (add your own code here).

There are some (very) rough notes about the history and motivation of the project in doc/.

In brief:

- I define a Topos interface and an class FiniteSets which implements it.
- You can do calculations with products, equalizers, exponentials and subobject classifiers.
- There is also a way to define algebraic structures and their theories (Group, Monoid etc.)
and some simple constructions using these (automorphism group of an object, etc).

Unfortunately the Java type system is not really adequate, so the code is littered with casts,
and the lack of support for expressive DSLs means that doing any more sophisticated topos
calculations would be unbearably cumbersome.
 
See the [Scala version](http://github.com/fdilke/bewl) where most of these issues are overcome and
Bewl reaches new heights of topos-crunching DSL glory!
