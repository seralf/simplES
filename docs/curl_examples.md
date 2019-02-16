curl -H 'Content-Type: application/json' -X POST http://localhost:9200/_xpack/sql?format=txt  -d '{ "query": "DESCRIBE twitter" }'


curl -H 'Content-Type: application/json' -X POST "http://localhost:9200/cp2011/_analyze?pretty=true" -d '
{
  "analyzer": "whitespace",
  "text":     "The quick brown fox."
}
'


curl -H 'Content-Type: application/json' -X POST "http://localhost:9200/cp2011/_analyze?pretty=true" -d '
{
  "analyzer": "ita_analyzer",
  "text":     "Muratori e formatori in calcestruzzo"
}
'


curl -H 'Content-Type: application/json' -X POST "http://localhost:9200/cp2011/doc/498/_termvectors?pretty=true" -d '
{
  "fields" : ["skills_txt"],
  "offsets" : true,
  "positions" : true,
  "term_statistics" : true,
  "field_statistics" : true
}
'



## esempi (per test)

http://localhost:9200/cp2011/_search?q=muratore			2
http://localhost:9200/cp2011/_search?q=professore		800


NOTE: 
+ ex: autosta
	- OK http://localhost:9200/cp2011/_search?q=autist
	- KO http://localhost:9200/cp2011/_search?q=autista

