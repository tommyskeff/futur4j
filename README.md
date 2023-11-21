# Futur4J

Futur4J is a powerful and intuitive open-source Java library that simplifies asynchronous task scheduling, inspired by the concept of JavaScript promises.

## Dependency
The Futur4J project is composed of multiple modules. It is required to include the `futur-api` module, and the other modules depend on it at runtime, however the others are optional and dependent on your use case.
### Gradle
```gradle
repositories {
    maven {
      url 'https://repo.tommyjs.dev/repository/maven-releases/'
    }
}

dependencies {
   compile 'dev.tommyjs:futur-api:1.0.0'
   compile 'dev.tommyjs:futur-standalone:1.0.0'
   compile 'dev.tommyjs:futur-reactor:1.0.0'
   compile 'dev.tommyjs:futur-reactive-streams:1.0.0'
}
```
### Gradle DSL
```dsl
repositories {
    maven("https://repo.tommyjs.dev/repository/maven-releases/")
}

dependencies {
    implementation("dev.tommyjs:futur-api:1.0.0")
    implementation("dev.tommyjs:futur-standalone:1.0.0")
    implementation("dev.tommyjs:futur-reactor:1.0.0")
    implementation("dev.tommyjs:futur-reactive-streams:1.0.0")
}
```
### Maven
```xml
<repositories>
    <repository>
        <id>tommyjs-repo</id>
        <url>https://repo.tommyjs.dev/repository/maven-releases/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>dev.tommyjs</groupId>
        <artifactId>futur-api</artifactId>
        <version>1.0.0</version>
    </dependency>
    <dependency>
        <groupId>dev.tommyjs</groupId>
        <artifactId>futur-standalone</artifactId>
        <version>1.0.0</version>
    </dependency>
    <dependency>
        <groupId>dev.tommyjs</groupId>
        <artifactId>futur-reactor</artifactId>
        <version>1.0.0</version>
    </dependency>
    <dependency>
        <groupId>dev.tommyjs</groupId>
        <artifactId>futur-reactive-streams</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```


## Getting started
Futur4J uses an underlying `Scheduler` instance to power both synchronous and asynchronous task execution. 

The library was originally built for use in Minecraft servers, and therefore has native support for a "main" thread with can be seamlessly switched to in a `Promise` chain (read more below).

It is recommended, but not required, to manually create a `Scheduler`. If this is not done, a single-threaded default `Scheduler` will be used.
```java
// check if scheduler has already been loaded
if (!Schedulers.isLoaded()) {
   Scheduler scheduler = ThreadPoolScheduler.create(6); // create a scheduler using an underlying thread pool (6 threads)
   Schedulers.setScheduler(scheduler); // initialize the scheduling 
}
```

The `futur-standalone` module has two scheduler implementations available. The `ExclusiveThreadPoolScheduler` operates one thread pool, and throws an exception when sync tasks are attempted to be executed.
The `ThreadPoolScheduler` uses a single-threaded "main" pool and a multi-threaded async pool.

For Minecraft, Bukkit, Velocity and BungeeCord support is coming soon. Feel free to make PRs for other external library support as modules.

### Using the scheduler
You can invoke tasks using the synchronous and asynchronous scheduler `Scheduler` methods. It is worth noting that exceptions in these tasks are swallowed silently, and it is therefore better to
use promise chains, or use the `Schedulers` utility class, which will wrap tasks in a try-catch and log to the standard error.

```java
scheduler.runDelayedSync(() -> {
    System.out.println("I was scheduled to execute 10 seconds later!");
}, 10L, TimeUnit.SECONDS);

Schedulers.runAsync(() -> {
    throw new RuntimeException("I will be caught by the exception wrapper in the Schedulers class!");
});
```

### Reacting to task completions
You may have recieved some form of "future" instance (a task that will complete at an unspecified time in the future). This may be a future4j-native `Promise` instance, or another form of future. In the case of the latter, you may obtain a `Promise`
 instance through the various available wrappers (e.g. `ReactorTransformer` for use with reactor core).

You can then add listeners to the `Promise`, which will be executed asynchronously on promise "completion". A promise is deemed completed once the task has either concluded successfully or an exception has been thrown. You can access this information in a `PromiseCompletion`, 
which is passed to promise listeners. Promise listeners should not throw exceptions, and will print a stack trace if they do to avoid silent failure. However, callbacks in promise chain methods are permitted to throw exceptions as they will be passed down the chain. 
```java
Promise<String> promise = doSomeAsyncProcessing();
promise.addListener(ctx -> {
    if (ctx.isError()) {
        System.out.println("Error! Oh no!");
    } else {
        String result = ctx.getResult();
        System.out.println(result);
    }
});
```

### Promise chains
`Promise` also contains convenience wrapper methods to avoid checking the completion for exceptions every time. These methods also return a new promise which will resolve once the original promise, and then handler callback have finished execution. We can use this feature to create
"promise chains", which are a sequence of promise handlers. These handlers are permitted to throw exceptions, which will be passed down promise chains until they are handled. If exceptions are not handled properly, they will be silently swallowed. It is recommend to append `logExceptions()`
to chains unless exceptions are handled explicitly with a custom listener. This will simply log exceptions in the promise chain to the standard error.

You can also use the sync and async methods to choose which executor is chosen for task execution. The ability to seamlessly switch between executors comes in handy with applications where you must be on the "main thread" to perform non-threadsafe operations.

`Promise.start()` can be used to create a "resolved" promise. This is useful when starting a promise chain or returning an already-completed promise (common in compose callbacks, mentioned below).

```java
Promise.start().thenSupplyAsync(() -> {
    // do some async processing
    return "Hello World!";
}).thenApplyAsync(input -> {
    // convert string to upper case
    return input.toUpperCase();
}).thenConsumeSync(result -> {
    // display result
    System.out.println("Result: " + result);
}).logExceptions(); // log exceptions to the standard error
```

The promise chain methods follow Java convention of `Supplier`, `Consumer` and `Runnable` through the `thenSupply`, `thenConsume` and `thenRun` methods. There is also `thenApply` (which acts as a Java `Function`), and `thenCompose`, which is explained below. Futur4J has its own implementations of the Java concepts though to allow for
exceptions within handlers.

The `thenCompose` method is similar to the concept in the Java `CompletableFuture` API. You are able to return another `Promise` within a promise handler, and a `Promise<Promise<T>>` will be wrapped up into just a `Promise<T>`. This is often useful when using an external library that returns some sort of "future" inside a handler.

```java
String username = "abc123";
mySuperSecretApi.usernameToId(username) // returns Promise<UUID>
    .thenComposeAsync(id -> {
        return userManager.fetchFromDatabase(id); // returns Promise<User>
    }).thenConsumeSync(playerData -> {
        System.out.println(username + " has " + playerData.getCoins() + " coins!");
    }).logExceptions();
```

### Utility methods
The `Promises` utility class provides many helpful methods for working with promises and groups of promises. 

`Promises.combine(Promise<K>, Promise<V>)` combines two promises into one `Promise<Entry<K, V>>`.

`Promises.erase(Promise)` erases the type on a `Promise` instance and returns a `Promise<Void>`. This is also supported for lists of promises with `Promises.all(List<Promise>)`, which will return a `Promise<Void>` representing the future whereby all promises have completed.

If all promises are of identical type, you can use `Promises.combine(List<Promise<T>>)` which will return one `Promise<List<T>>`, similarly representing the future whereby all promises have completed.

This can also be applied to key-value scenarios in the same way, where you can provide a mapper function to be applied to all elements and combine into a single promise.





### Future wrappers
External libaries provide asynchronous ways to interact with their output in all shapes and sizes. Futur4J currently has wrappers for the following libraries/frameworks:
- Reactive Streams (via futur-reactive-streams)
- Reactor Core (via futur-reactor)
- Java CompletableFuture (via `Promises.wrap` in futur-api)

Coming Soon:
- Lettuce (redis futures)
- Netty futures (maybe?)