# Phoenix  API
[![Latest](https://img.shields.io/nexus/r/ws.mia/phoenix-api?server=https://parthenon.mia.ws&label=latest)](https://parthenon.mia.ws/#browse/browse:maven-releases:ws%2Fmia%2Fphoenix-api)  
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
        <version>1.2.7</version>
    </dependency>
</dependencies>
```

**Gradle** - add to your `build.gradle`:
```groovy
repositories {
    maven { url 'https://parthenon.mia.ws/repository/maven-public/' }
}

dependencies {
    implementation 'ws.mia:phoenix-api:1.2.7'
}
```

## Usage

```java
PhoenixClient client = new PhoenixHttpClient("https://phoenix.example.com");
List<Route> routes = client.getRoutes();
```