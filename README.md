# JavaSteam
[![Java CI/CD](https://github.com/Longi94/JavaSteam/actions/workflows/javasteam-build-push.yml/badge.svg)](https://github.com/Longi94/JavaSteam/actions/workflows/javasteam-build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/in.dragonbra/javasteam)](https://mvnrepository.com/artifact/in.dragonbra/javasteam)
[![Discord](https://img.shields.io/discord/420907597906968586.svg)](https://discord.gg/8F2JuTu)

Java port of [SteamKit2](https://github.com/SteamRE/SteamKit). JavaSteam targets Java 11.

## Download

Latest version is available through [Maven](https://mvnrepository.com/artifact/in.dragonbra/javasteam)

If you get a `java.security.InvalidKeyException: Illegal key size or default parameters` exception when trying to encrypt a message you need to download the [Unlimited Strength Jurisdiction Policy Files](http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html) and place them under `${java.home}/jre/lib/security/`. See [this stackoverflow question](https://stackoverflow.com/questions/6481627/java-security-illegal-key-size-or-default-parameters).

**1. Add the repository to your build.**

Gradle
```groovy
repositories {
    mavenCentral()
}
```

Maven
```xml
<repository>
  <id>central</id>
  <url>https://repo.maven.apache.org/maven2</url>
</repository>
```

**2. Add the JavaSteam dependency to your project.**

Gradle
```groovy
implementation 'in.dragonbra:javasteam:x.y.z'
```

Maven
```xml
<dependency>
    <groupId>in.dragonbra</groupId>
    <artifactId>javasteam</artifactId>
    <version>x.y.z</version>
</dependency>
```

**3. Add the appropriate cryptography dependency to your project. JavaSteam depends on this.**

[Android | Spongy Castle](https://mvnrepository.com/artifact/com.madgag.spongycastle/prov)

[Non-Android | Bouncy Castle](https://mvnrepository.com/artifact/org.bouncycastle/bcprov-jdk18on)

**4. (Optional) Working with protobufs.**

If you plan on working with protobuf builders directly to perform actions a handler doesn't support, you will need to add the protobuf-java dependency.

Note: To eliminate any errors or warnings, you should try and match the same version JavaSteam uses.<br>You can find the latest version being used [here](https://github.com/Longi94/JavaSteam/blob/master/gradle/libs.versions.toml).

[Protobuf Java](https://mvnrepository.com/artifact/com.google.protobuf/protobuf-java)

## Getting Started

You can head to the very short [Getting Started](https://github.com/Longi94/JavaSteam/wiki/Getting-started) page or take a look at the [samples](https://github.com/Longi94/JavaSteam/tree/master/javasteam-samples/src/main/java/in/dragonbra/javasteamsamples) to get you started with using this library. 

There some [open-source projects](https://github.com/Longi94/JavaSteam/wiki/Samples) too you can check out.

The [wiki](https://github.com/Longi94/JavaSteam/wiki) may also be useful to check out for other info.

## Build

Full build:<br>

```./gradlew build -x signMavenJavaPublication```

Generated classes:<br>

```./gradlew generateProto generateSteamLanguage generateProjectVersion```

## Contributing

Contributions to the repository are always welcome! Checkout the [contribution guidelines](CONTRIBUTING.md) to get started.

## Other

Join the [discord server](https://discord.gg/8F2JuTu) if you have any questions related or unrelated to this repo or just want to chat!

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
