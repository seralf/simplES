


https://www.elastic.co/guide/en/elasticsearch/reference/5.5/docs-bulk.html

POST _bulk
{ "index" : { "_index" : "test", "_type" : "type1", "_id" : "1" } }
{ "field1" : "value1" }
{ "delete" : { "_index" : "test", "_type" : "type1", "_id" : "2" } }
{ "create" : { "_index" : "test", "_type" : "type1", "_id" : "3" } }
{ "field1" : "value3" }
{ "update" : {"_id" : "1", "_type" : "type1", "_index" : "test"} }
{ "doc" : {"field2" : "value2"} }


https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/java-docs-bulk-processor.html