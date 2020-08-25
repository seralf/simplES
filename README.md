simplES
====================================

A Simple Elastic Search Tool.

This is a small POC for local testing / mocking a proper installation.


## example (GOT)

A simple example indexing descriptions for some of the Game of Thrones episodes.
.

1. launch

`simples.examples.got.MainGOT`


2. try the query:

`http://localhost:9200/got/_doc/_search?q=Tyrion`



## TODO

+ [ ] update deprecated TransportClient to new RestClient (for ES 8+)
+ [ ] check latest libraries version
+ [ ] improve loading of settings / mapping from local JSON file
+ [ ] improve the exception handling
+ [ ] improve `Future` <-> `Try` interaction
+ [x] updated jackson to fix vulnerability

