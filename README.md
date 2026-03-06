# Fylio

Simple class scheduling system. Main purpose is to learn Clojure and related technologies. All data is stored in memory.
So there is no need to run a database.

The project uses [Lacinia](https://github.com/walmartlabs/lacinia) for GraphQL processing and [Lacinia-pedestal](https://github.com/walmartlabs/lacinia-pedestal) for http serving of GraphQL requests.

## Installation

Download from https://github.com/fylio/fylio

## Usage

This will run the project in development mode, and start a REPL:

    $ clj -M:dev
    user=>

### Useful functions

#### Start a GraphiQL REPL:

    user=> (start)

This will open a browser window with GraphiQL. You can explore the GraphQL API and execute queries.

The GraphQL API will be available at http://localhost:8888/api

#### Execute a GraphQL query

    user=> (q "{ userById(id: \"1234\") { id firstName }}")
