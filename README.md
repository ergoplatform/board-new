# board-new [![Build Status](https://travis-ci.org/ergoplatform/board-new.svg?branch=master)](https://travis-ci.org/ergoplatform/board-new)

To run this board you need to start mongo. There is two options.

1. Run mongod from you local machine with default `localhost:27017` url

2. Run via docker in the root of this project run `docker-compose up`

The API will be available by default on `localhost:8080` but you can configure it via adjusting `application.conf` file.

Go to `localhost:8080/swagger/` to see OpenApi docs.