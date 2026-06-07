# Phoenix  API
Models and HTTP client for [Phoenix](https://gh.mia.ws/phoenix-core).

## Download

**Maven** - add to your `pom.xml`:
```xml
<repositories>
    <repository>
        <id>nexus-public</id>
        <url>https://parthenon.mia.ws/repository/maven-public/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>ws.mia</groupId>
        <artifactId>phoenix-api</artifactId>
        <version>1.2.6</version>
    </dependency>
</dependencies>
```

**Gradle** - add to your `build.gradle`:
```groovy
repositories {
    maven { url 'https://parthenon.mia.ws/repository/maven-public/' }
}

dependencies {
    implementation 'ws.mia:phoenix-api:1.2.6'
}
```

## Usage

```java
PhoenixHttpClient client = new PhoenixHttpClient("https://phoenix.example.com");
List<Route> routes = client.getRoutes();
```