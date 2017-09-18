# elton

[![Build Status](https://travis-ci.org/globalbioticinteractions/elton.svg?branch=master)](https://travis-ci.org/globalbioticinteractions/elton) [![Release](https://jitpack.io/v/org.globalbioticinteractions/elton.svg)](https://jitpack.io/#org.globalbioticinteractions/elton) [![standard-readme compliant](https://img.shields.io/badge/standard--readme-OK-green.svg?style=flat-square)](https://github.com/RichardLitt/standard-readme)

 A commandline tool for [GloBI](https://globalbioticinteraction.org)

<a href="http://globalbioticinteractions.org/">
  <img src="http://www.globalbioticinteractions.org/assets/globi.svg" height="120">
</a>

## Table of Contents

- [Install](#install)
- [Usage](#usage)
- [Building](#building)
- [Contribute](#contribute)
- [License](#license)

## Install

### Official releases

You can use this project by including `elton.jar` from one of the [releases](https://github.com/org.globalbioticinteractions/elton/releases).

### Maven, Gradle, SBT

Package managers are supported through [JitPack](https://jitpack.io/#globalbioticinteractions/elton/) which supports Maven, Gradle, SBT, etc.

for Maven, add the following sections to your pom.xml:
```
  <repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
  </repositories>

  <dependencies>
    <dependency>
      <groupId>org.globalbioticinteractions</groupId>
      <artifactId>elton</artifactId>
      <version>v0.3.2</version>
    </dependency>
  </dependencies>
```

### Building

* Clone this repository
* Run `mvn package`
* Copy `target/elton-0.3.2-jar-with-dependencies.jar`
* Run tests using `mvn test`.

## Usage

Print usage
```sh
java -jar elton.jar [command] [command options]
```

with

```
Usage: <main class> [command] [command options]
  Commands:
    list      List Available Datasets
      Usage: list [options] namespace1, namespace2, ...
        Options:
          --cache-dir, -c
            cache directory
            Default: ./datasets
          --offline, -o
            offline
            Default: false

    update      Update Datasets with Local Repository
      Usage: update [options] namespace1, namespace2, ...
        Options:
          --cache-dir, -c
            cache directory
            Default: ./datasets

    names      List Dataset (Taxon) Names For Local Datasets
      Usage: names [options] namespace1, namespace2, ...
        Options:
          --cache-dir, -c
            cache directory
            Default: ./datasets

    check      Check Dataset Accessibility
      Usage: check [options] namespace1, namespace2, ...
        Options:
          --cache-dir, -c
            cache directory
            Default: ./datasets

    version      Show Version
      Usage: version
```

## Contribute

Feel free to join in. All welcome. Open an [issue](https://github.com/globalbioticinteractions/elton/issues)!

## License

[GPL](LICENSE)
