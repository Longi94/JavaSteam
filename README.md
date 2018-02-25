# JavaSteam

Work-in-progress Java port of [SteamKit2](https://github.com/SteamRE/SteamKit).

## Build
```gradlew build```

If encryption fails with "Illegal key size or default parameters" you need to download the [Unlimited Strength Jurisdiction Policy Files](http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html) and place them under `${java.home}/jre/lib/security/`
