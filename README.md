# TFC-Freezer

TFC-Freezer is a Minecraft Forge 1.20.1 addon for TerraFirmaCraft. It adds powered freezer appliances that slow TFC food decay by applying a custom freezing food trait.

## Features

- Block freezer with a 27-slot food inventory and Forge Energy support.
- Portable freezer item with its own internal inventory and energy storage.
- Freezer UI power controls and energy display.
- Freezer open and closed block models with TerraFirmaCraft barrel open and close sounds.
- `freezing` food trait with a configurable decay modifier.

## Requirements

- Minecraft 1.20.1
- Forge 47.x
- Java 17
- TerraFirmaCraft
- JEI, optional at compile time/runtime integration
- Patchouli, optional at compile time/runtime integration

The dependency versions used by this workspace are defined in `gradle.properties`.

## Building

This project uses Gradle 8.8. If dependency downloads are slow in the local development environment, run Gradle with the local proxy:

```powershell
$env:JAVA_OPTS='-Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=7890 -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=7890'
gradle compileJava processResources --no-daemon
```

If the Gradle wrapper scripts are restored to the project, `.\gradlew.bat build --no-daemon` can be used instead.

## License

TFC-Freezer is licensed under the European Union Public Licence, version 1.2 (`EUPL-1.2`).

Minecraft, Forge, TerraFirmaCraft, JEI, Patchouli, and any other third-party projects remain under their own licenses.
