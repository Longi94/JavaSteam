# JavaSteam
[![Build Status](https://travis-ci.org/Longi94/JavaSteam.svg?branch=master)](https://travis-ci.org/Longi94/JavaSteam) [![codecov](https://codecov.io/gh/Longi94/JavaSteam/branch/master/graph/badge.svg)](https://codecov.io/gh/Longi94/JavaSteam) 
[![Discord](https://img.shields.io/discord/420907597906968586.svg)](https://discord.gg/8F2JuTu)

Work-in-progress Java port of [SteamKit2](https://github.com/SteamRE/SteamKit). JavaSteam targets Java 7.

## Download

Currently only [snapshot builds](https://oss.sonatype.org/content/repositories/snapshots/in/dragonbra/javasteam/1.0.0-SNAPSHOT/) are available on Sonatype's snapshot repository.

If you get a `java.security.InvalidKeyException: Illegal key size or default parameters` exception when trying to encrypt a message you need to download the [Unlimited Strength Jurisdiction Policy Files](http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html) and place them under `${java.home}/jre/lib/security/`. See [this stackoverflow question](https://stackoverflow.com/questions/6481627/java-security-illegal-key-size-or-default-parameters).

**1. Add the snapshot repository to your build.**

Gradle
```groovy
repositories {
    maven {
        url 'https://oss.sonatype.org/content/repositories/snapshots/'
    }
}
```

Maven
```xml
<repository>
  <id>snapshots-repo</id>
  <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
</repository>
```

**2. Add the JavaSteam dependency to your project.**

Gradle
```groovy
implementation 'in.dragonbra:javasteam:1.0.0-SNAPSHOT'
```

Maven
```xml
<dependency>
  <groupId>in.dragonbra</groupId>
  <artifactId>javasteam</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```

**3. Add the appropriate cryptography dependency to your project. JavaSteam depends on this.**

Gradle
```groovy
implementation 'org.bouncycastle:bcprov-jdk15on:1.69'  // NON-ANDROID ONLY
implementation 'com.madgag.spongycastle:prov:1.58.0.0' // ANDROID ONLY
```

Maven
```xml
<dependency> <!-- NON-ANDROID ONLY -->
  <groupId>org.bouncycastle</groupId>
  <artifactId>bcprov-jdk15on</artifactId>
  <version>1.69</version>
</dependency>
<dependency> <!-- ANDROID ONLY -->
    <groupId>com.madgag.spongycastle</groupId>
    <artifactId>prov</artifactId>
    <version>1.58.0.0</version>
</dependency>
```

## Getting Started

You can head to the very short [Getting Started](https://github.com/Longi94/JavaSteam/wiki/Getting-started) page or take a look at the [samples](https://github.com/Longi94/JavaSteam/tree/master/javasteam-samples/src/main/java/in/dragonbra/javasteamsamples) to get you started with using this library. There some [open-source projects](https://github.com/Longi94/JavaSteam/wiki/Samples) too you can check out.

## Build

```./gradlew build```

## Contributing

Contributions to the repository are always welcome! Checkout the [contribution guidelines](CONTRIBUTING.md) to get started.

## Other

Join the [discord server](https://discord.gg/8F2JuTu) if you have any questions related or unrelated to this repo or just want to chat!

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
