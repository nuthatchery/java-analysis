# Java Bytecode Fact Extractor

## Usage
From the root folder of this project:

* Build a `jar` file of this project with the command `mvn package`; output `jar` file is in `target/` subfolder
* Try applying it to itself:
```
java -jar target/jvm-fact-extractor-0.0.1-SNAPSHOT.jar  target/jvm-fact-extractor-0.0.1-SNAPSHOT.jar
```
* Output is in `/tmp/data.trig` (in [TriG](https://en.wikipedia.org/wiki/TriG_(syntax)) syntax); you can also specify output file with `-o filename`, or use a persistent [Jena TDB store](https://jena.apache.org/documentation/tdb/) with `-d dbDir`.

### With Graal

Run the application (App.java) in graal-getting-started. It'll load `/tmp/data.trig`, set up a few inference rules, run a couple of queries, and then expect further queries on stdin.
