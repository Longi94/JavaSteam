# JavaSteam
[![Build Status](https://travis-ci.org/Longi94/JavaSteam.svg?branch=master)](https://travis-ci.org/Longi94/JavaSteam) [![codecov](https://codecov.io/gh/Longi94/JavaSteam/branch/master/graph/badge.svg)](https://codecov.io/gh/Longi94/JavaSteam)

Work-in-progress Java port of [SteamKit2](https://github.com/SteamRE/SteamKit).

## Download

Currently only [snapshot builds](https://oss.sonatype.org/content/repositories/snapshots/in/dragonbra/javasteam/1.0.0-SNAPSHOT/) are available on Sonatype's snapshot repository.

### Gradle

```groovy
repositories {
    maven {
        url 'https://oss.sonatype.org/content/repositories/snapshots/'
    }
}

dependencies {
    compile 'org.bouncycastle:bcprov-jdk15on:1.59'
    compile 'in.dragonbra:javasteam:1.0.0-SNAPSHOT'
}
```

#### Android

```groovy
dependencies {
    compile 'com.madgag.spongycastle:prov:1.58.0.0'
    compile 'in.dragonbra:javasteam:1.0.0-SNAPSHOT'
}
```

### Maven

```xml
<repositories>
  <repository>
    <id>snapshots-repo</id>
    <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcprov-jdk15on</artifactId>
    <version>1.59</version>
  </dependency>
  <dependency>
    <groupId>in.dragonbra</groupId>
    <artifactId>javasteam</artifactId>
    <version>1.0.0-SNAPSHOT</version>
  </dependency>
</dependencies>
```

#### Android

```xml
<dependencies>
  <dependency>
    <groupId>com.madgag.spongycastle</groupId>
    <artifactId>prov</artifactId>
    <version>1.58.0.0</version>
  </dependency>
  <dependency>
    <groupId>in.dragonbra</groupId>
    <artifactId>javasteam</artifactId>
    <version>1.0.0-SNAPSHOT</version>
  </dependency>
</dependencies>
```

## Getting Started

With the lack of tutorials there are [samples](https://github.com/Longi94/JavaSteam/tree/master/javasteam-samples/src/main/java/in/dragonbra/javasteamsamples) to get you started with using this library.

## Build

```./gradlew build```

If encryption fails with "Illegal key size or default parameters" you need to download the [Unlimited Strength Jurisdiction Policy Files](http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html) and place them under `${java.home}/jre/lib/security/`

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
