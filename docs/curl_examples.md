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


curl -H 'Content-Type: application/json' -X POST "http://localhost:9200/cp2011/01/_termvectors?pretty=true" -d '
{

GET /twitter/_doc/1/_termvectors
