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
