# Tiny Map - The shortest distance - Coding exercise
***

Tiny Maps are modeled as Undirected Weighted Graphs where cities are nodes, roads are edges and distances are weights. 
For each new road added, the graph creates two edges, one for each direction.
Tiny Maps are stored in a local running instance of Redis


## How To Run

### Setup Redis
Install and run redis-server on your machine following the [official instructions](https://redis.io/docs/getting-started/installation/)

### Run
```
$> sbt run
```

### Test
```
$> sbt test
```

## Dependencies 
This project is built using the following libraries
* [Cats](https://github.com/typelevel/cats) & [CatsEffects](https://github.com/typelevel/cats-effect)
* [Http4s](https://github.com/http4s/http4s)
* [Circe](https://github.com/circe/circe)
* [Newtype](https://github.com/estatico/scala-newtype)
* [Refined](https://github.com/fthomas/refined)
* [Redis4Cats](https://github.com/profunktor/redis4cats)
* [ScalaTest](https://github.com/scalatest/scalatest)

## REST API

API to run CRUD operations on Tiny Maps and to get the shortest distance between two cities

### Get all the maps - `GET /maps`

Request

    curl -i -H 'Accept: application/json' http://localhost:8080/maps

Response

    HTTP/1.1 200 OK
    Content-Type: application/json
    Date: Sat, 04 Mar 2023 20:00:43 GMT
    Content-Length: 2

    []

### Create a new Map - `POST /maps`

Request

    curl --location --request POST 'http://localhost:8080/maps' --header 'Content-Type: application/json' --data-raw '{"map": [{"A": {"B": 100,"C": 30}},{"B": {"C": 30}}]}'

Response

    HTTP/1.1 200 OK
    Content-Type: application/json
    Date: Sat, 04 Mar 2023 20:00:43 GMT
    Content-Length: 129

    {"id":"2a4c7742-5aff-4201-8733-d87c4d911055","graph":{"A":{"B":100.0,"C":30.0},"B":{"C":30.0,"A":100.0},"C":{"A":30.0,"B":30.0}}}

### Get a specific Map - `GET /map/:id`

Request

    curl --location --request GET 'http://localhost:8080/maps/2a4c7742-5aff-4201-8733-d87c4d911055'

Response

    HTTP/1.1 200 OK
    Content-Type: application/json
    Date: Sat, 04 Mar 2023 20:00:43 GMT
    Content-Length: 129

    {"id":"2a4c7742-5aff-4201-8733-d87c4d911055","graph":{"A":{"B":100.0,"C":30.0},"B":{"C":30.0,"A":100.0},"C":{"A":30.0,"B":30.0}}}

### Get a non-existent Map - `GET /maps/:non-existent-id`

Request

    curl --location --request GET 'http://localhost:8080/maps/d6ad2427'

Response

    HTTP/1.1 404 Not Found
    Content-Type: text/plain; charset=UTF-8
    Date: Sun, 05 Mar 2023 12:24:22 GMT

### Insert new roads to a Map - `PUT /maps/:id`

Request

    curl --location --request PUT 'http://localhost:8080/maps/2a4c7742-5aff-4201-8733-d87c4d911055' --header 'Content-Type: application/json' --data-raw '{ "map": [{ "A": {"I":70, "J":150} }]}'

Response 

    HTTP/1.1 200 OK
    Content-Type: application/json
    Date: Sun, 05 Mar 2023 12:27:03 GMT
    Content-Length: 179

    {"id":"2a4c7742-5aff-4201-8733-d87c4d911055","graph":{"J":{"A":150.0},"A":{"B":100.0,"C":30.0,"J":150.0,"I":70.0},"I":{"A":70.0},"B":{"C":30.0,"A":100.0},"C":{"A":30.0,"B":30.0}}}

### Update existent roads in a Map - `PUT /maps/:id`

Request

    curl --location --request PUT 'http://localhost:8080/maps/2a4c7742-5aff-4201-8733-d87c4d911055' --header 'Content-Type: application/json' --data-raw '{ "map": [{ "A": {"I":170, "J":50} }]}'

Response

    HTTP/1.1 200 OK
    Content-Type: application/json
    Date: Sun, 05 Mar 2023 12:28:09 GMT
    Content-Length: 179
    
    {"id":"2a4c7742-5aff-4201-8733-d87c4d911055","graph":{"J":{"A":150.0},"A":{"B":100.0,"C":30.0,"J":150.0,"I":70.0},"I":{"A":70.0},"B":{"C":30.0,"A":100.0},"C":{"A":30.0,"B":30.0}}}

### Delete existent roads in a Map - `DELETE /maps/:id`

Request

    curl --location --request DELETE 'http://localhost:8080/maps/2a4c7742-5aff-4201-8733-d87c4d911055' --header 'Content-Type: application/json' --data-raw '{ "map": [{ "A": {"B":100} }] }'

Response

    HTTP/1.1 200 OK
    Content-Type: application/json
    Date: Sun, 05 Mar 2023 12:30:53 GMT
    Content-Length: 159

    {"id":"2a4c7742-5aff-4201-8733-d87c4d911055","graph":{"J":{"A":150.0},"A":{"C":30.0,"J":150.0,"I":70.0},"I":{"A":70.0},"B":{"C":30.0},"C":{"A":30.0,"B":30.0}}}

### Calculate the shortest distance between two cities - `GET /maps/:id/shortestDistance`

Request

    curl --location --request GET 'http://localhost:8080/maps/2a4c7742-5aff-4201-8733-d87c4d911055/shortestDistance' --header 'Content-Type: application/json' --data-raw '{ "start": "A", "end": "B"}'

Response

    HTTP/1.1 200 OK
    Content-Type: application/json
    Date: Sun, 05 Mar 2023 12:31:59 GMT
    Content-Length: 17

    {"distance":60.0}