This project is a Demo project for [graphql-clj](https://github.com/tendant/graphql-clj) and [GraphiQL](https://github.com/graphql/graphiql). You can start trying Clojure with GraphQL in a few minutes.

This project was bootstrapped with [Create React App](https://github.com/facebookincubator/create-react-app).

### Start server

    lein ring server-headless

### Access graphiql from

    http://localhost:3002/index.html

#### Sample queries

```
query {
  human (id:"1002") {
    id
    name
    friends {
      id
      name
      friends {
        id
      }
    }
  }
}
```

To get all characters, try searching without an ID:

```
query {
  characters {
    name
    friends {
      name
    }
  }
}

```

Specifically, Humans and Droids are both arrays as a return type, although they support passing an individual ID:

```
query {
  human(id: "1001") {
    name
  }
  
  droid(id: "2000") {
    name
  }
}
```

If you want to select more than one ID, then pass an array of IDs to the `humans` query type.

```
query Q($list: [String!]){
  humans(ids: $list){
    name
  }
}

```

Try these example variables:

```
{
  "list": ["1001", "1002", "1003"]
}
```

The same is possible with the `droids` type.

```
query Q($list: [String!]){
  droids(ids: $list){
    name
    primaryFunction
  }
}
```

Valid IDs for droids to test variables are `"2000"` and `"2001"`.

#### Sample mutations

```
mutation{
  createHuman (name:"testname", friends:[]) {
    id
  }
}
```

### Build Application (HTML & JS)

_Note: Not required, unless you want to make changes to Javascript code_

    npm install

    npm run build

