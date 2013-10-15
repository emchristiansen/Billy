#Billy

This is a framework for testing local visual descriptors, eg SIFT, SURF, and BRIEF. 
It is similar to [VLBenchmarks](http://www.vlfeat.org/benchmarks/index.html) except it is written in Scala, plays well with OpenCV, and can run seamlessly on a desktop or a cluster.

Check out [Pilgrim](https://github.com/emchristiansen/Pilgrim) for a frontend to this framework.

Named after Billy Pilgrim in Slaughterhouse Five.
There's a quote from one of the aliens, where it describes humans' perspective as akin to someone looking at the world through a straw.
This isn't so different from the perspective of computer vision techniques which use local descriptors.
I'm currently trying to find that quote.

I've temporarily removed the cluster functionality; the code was ugly, passing around Scala scripts which would be compiled on the remote machines.
Hopefully [spores](http://docs.scala-lang.org/sips/pending/spores.html) will be viable in a few months, presenting a clean means of closure distribution.
When they are, I'll re-enable the cluster computation.

This project is under development. 
Stay tuned for documentation.

##Installation

You can use Billy in your SBT project by simply adding the
following dependency to your build file:

```scala
libraryDependencies += "st.sparse" %% "billy" % "0.1-SNAPSHOT"
```

You also need to add the Sonatype "snapshots" repository resolver to
your build file:

```scala
resolvers += Resolver.sonatypeRepo("snapshots")
```

[![Build Status](https://travis-ci.org/emchristiansen/Billy.png)](https://travis-ci.org/emchristiansen/Billy)
