# Futur4J

Futur4J is a powerful and intuitive open-source Java library that simplifies asynchronous task scheduling, inspired by the concept of JavaScript promises.

**This documentation is outdated. Please don't read it.**

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
