simplES
====================================

A Simple Elastic Search Tool POC


## example (GOT)

A simple example indexing descriptions for some of the Game of Thrones episodes.
.

1. launch

`simples.examples.got.MainGOT`


2. try the query:

`http://localhost:9200/got/_doc/_search?q=Tyrion`



## TODO: update to latest version


+ [ ] manage Future <-> Try

+ [x] updated jackson to fix vulnerability

SEE: https://github.com/seralf/simplES/network/alerts
```
<dependency>
  <groupId>com.fasterxml.jackson.core</groupId>
  <artifactId>jackson-databind</artifactId>
  <version>[2.9.8,)</version>
</dependency>
```


