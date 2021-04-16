# Costa Rica electoral roll in Elasticsearch

This project contains a simple example of how to import the Costa Rica electoral roll into Elastiscsearch.


# Build and run

To build this project use:
  `mvn clean package install -U`
  
To run this project use:

  `java -cp target/padrones-1.0-SNAPSHOT.jar fmendez.padron.Importer --input=src/test/resources/PADRON100.TXT --esHosts=http://localhost:9200/ --esIndex=padron --schemaless=true`
  
  Replace _input_, _esHosts_ and _esIndex_ by your target Elasticsearch elements.
  
# Frameworks used

 - https://beam.apache.org/


