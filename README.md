# Futur4J

Futur4J is a powerful and intuitive open-source Java library that simplifies asynchronous task scheduling, inspired by the concept of JavaScript promises.

## Dependency
The Futur4J project has a `futur-api` module that provides the core functionality, and a `futur-lazy` module that provides additional static versions of factory methods. It is
recommended to use the main module for customization of logging and execution.
### Gradle
```gradle
repositories {
    maven {
      url 'https://repo.tommyjs.dev/repository/maven-releases/'
    }
}

dependencies {
   compile 'dev.tommyjs:futur-api:2.4.0'
   compile 'dev.tommyjs:futur-lazy:2.4.0'
}
```
### Gradle DSL
```kotlin
repositories {
    maven("https://repo.tommyjs.dev/repository/maven-releases/")
}

dependencies {
    implementation("dev.tommyjs:futur-api:2.4.0")
    implementation("dev.tommyjs:futur-lazy:2.4.0")
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
        <version>2.4.0</version>
    </dependency>
    <dependency>
        <groupId>dev.tommyjs</groupId>
        <artifactId>futur-lazy</artifactId>
        <version>2.4.0</version>
    </dependency>
</dependencies>
```