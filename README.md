## Development

Run in development mode with:

```
lein figwheel
```

Then connect to http://localhost:3449/.

## Deployment

Build for deployment with:

```
lein do clean, uberjar
```

Start server with:

```
java -jar target/todo-0.1.0.jar
```

Then connect to http://localhost:3000/.