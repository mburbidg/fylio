# Fylio

Simple class scheduling system. Main purpose is to learn Clojure and related technologies. Data is stored in a Postgres
database running locally in a docker container.

The project uses [Lacinia](https://github.com/walmartlabs/lacinia) for GraphQL processing and [Lacinia-pedestal](https://github.com/walmartlabs/lacinia-pedestal) for http serving of GraphQL requests.

## Setup

Clone from https://github.com/fylio/fylio

Fylio stores data in a Postgres database. Run the following command to run Postgres locally in a docker container:

    docker compose up -d

To create the database schema, and load some initial test data run the following command from the root of the project:

    ./bin/setup-db.sh

To run the Postgres CLI, run the following command from the root of the project:

    ./bin/psql.sh

## Usage

Once the database is running and test data is loaded, the project can be run in development mode and a REPL started
using the following command:

    $ clj -M:dev
    user=>

### Useful functions

#### Start a GraphiQL REPL:

    user=> (start)

This will open a browser window with GraphiQL. You can explore the GraphQL API and execute queries.

The GraphQL API will be available at http://localhost:8888/api

#### Execute a GraphQL query

    user=> (q "{ userById(id: \"1234\") { id firstName }}")

## Tests

Tests can be run using the following command:

    clj -T:build test
