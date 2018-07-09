# Java Bytecode Fact Extractor

## Usage
From the root folder of this project:

* Build a `jar` file of this project with the command `mvn package`; output `jar` file is in `target/` subfolder
* Try applying it to itself:
```
java -jar target/jvm-fact-extractor-0.0.1-SNAPSHOT.jar  target/jvm-fact-extractor-0.0.1-SNAPSHOT.jar
```
* Output is in `/tmp/data.trig` (in [TriG](https://en.wikipedia.org/wiki/TriG_(syntax)) syntax); you can also specify output file with `-o filename`, or use a persistent [Jena TDB store](https://jena.apache.org/documentation/tdb/) with `-d dbDir`.

* Run with -s to invoke SPARQL server at http://localhost:3330/

* Run with -jung to invoke JFrame Jung visualisation of the (manually in-code selected) graph

Interesting queries: 
http://localhost:3330/dataset?query=PREFIX m: <http://model.nuthatchery.org/maven/> SELECT ?x ?p ?y FROM <http://db.nuthatchery.org/java/guava-18.0.jar> WHERE {?x ?p ?y}

```
PREFIX nh: <http://model.nuthatchery.org/common/>
PREFIX m: <http://model.nuthatchery.org/maven/project/>
PREFIX j: <http://model.nuthatchery.org/java/>
PREFIX jType: <http://model.nuthatchery.org/java/types/>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>
prefix owl: <http://www.w3.org/2002/07/owl#>

SELECT ?g ?m ?code  ?op # (group_concat(?code;separator=', ') as ?codes)
WHERE {
  GRAPH ?g {
    ?cls nh:name "org/apache/commons/collections/set/UnmodifiableSet" .
    ?m j:memberOf <this:org/apache/commons/collections/set/UnmodifiableSet> .
    ?m nh:defines ?t .
    ?m j:code  / rdf:rest* / rdf:first ?inv .
    ?inv j:call ?code .
    ?inv (j:varOperand|j:constantOperand|j:memberOperand|j:labelOperand|j:typeOperand) / nh:idName? ?op .
    FILTER (!isBlank(?op)) .
  }
}
#GROUP BY ?g ?cls ?m ?code  ?op
```

##### Find all methods that call themselves (directly)
```
PREFIX nh: <http://model.nuthatchery.org/common/>
PREFIX m: <http://model.nuthatchery.org/maven/project/>
PREFIX j: <http://model.nuthatchery.org/java/>
PREFIX jType: <http://model.nuthatchery.org/java/types/>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>
prefix owl: <http://www.w3.org/2002/07/owl#>

SELECT ?g ?class ?caller
WHERE {
  GRAPH ?g {
    #?class nh:name "org/apache/commons/collections/set/UnmodifiableSet" .
    ?caller j:memberOf ?class.
    ?caller j:code  / rdf:rest* / rdf:first ?anInstruction .
    
          {?anInstruction j:call j:invokevirtual}
    UNION {?anInstruction j:call j:invokespecial}
    UNION {?anInstruction j:call j:invokeinterface}
    UNION {?anInstruction j:call j:invokedynamic} .

    ?anInstruction j:memberOperand ?callee .
    FILTER (?caller = ?callee) .
  }
}
```

http://localhost:3330/dataset?query=PREFIX m: <http://model.nuthatchery.org/maven/project/> SELECT ?x ?p ?y FROM <http://db.nuthatchery.org/java/jvm-fact-extractor-0.0.1-SNAPSHOT.jar> WHERE {?x ?p ?y}

http://localhost:3330/dataset?query=PREFIX m: <http://model.nuthatchery.org/maven/project/> SELECT ?x ?p ?y FROM <http://db.nuthatchery.org/java/jvm-fact-extractor-0.0.1-SNAPSHOT.jar> WHERE {?x m:artifactID ?y}

### With Graal

Run the application (App.java) in graal-getting-started. It'll load `/tmp/data.trig`, set up a few inference rules, run a couple of queries, and then expect further queries on stdin.

# Ontology conventions

* To make an ontology model: `OntModel m = ModelFactory.createOntologyModel()`
* Name space must be added manually
* Class names are capitalised `CamelCase`
   * Make them with `m.createClass(NS + uriString)`
* Property names are uncapitalised `camelCase`
   * Make them with `m.createProperty(NS + uriString)`
   
## Building models

Create a typed resource:
```
Resource coord1 = model.createResource(depUri, MavenFacts.MavenCoordinate);
Resource coord2 = model.createResource(depUri, MavenFacts.MavenCoordinate);
```

Add property:
```
coord1.addProperty(MavenFacts.dependsOn, coord2);
```

### Inference
If `MavenFacts.dependsOn` is set up with `MavenFacts.MavenCoordinate` as `rdf:range` and `rdf:domain`, we can also infer that `coord1` and `coord2` are Maven coordinates.

This also means that *anything* used with `MavenFacts.dependsOn` is a Maven coordinate, including something like:
```
coord1.addProperty(MavenFacts.dependsOn, JAVA_SE_8)
```

With `owl:inverseOf` we can also infer something like `coord2 mvn:hasDep coord1`.
