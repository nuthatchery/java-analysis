```
$JENA_HOME/bin/sparql --data=example.trig --query=accesspoints.sparql > accesspoints.ttl && rapper -i turtle -o dot accesspoints.ttl > accesspoints.dot && dot -Tpdf -O accesspoints.dot
```
