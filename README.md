# elton

[![Build Status](https://travis-ci.org/globalbioticinteractions/elton.svg?branch=master)](https://travis-ci.org/globalbioticinteractions/elton) [![standard-readme compliant](https://img.shields.io/badge/standard--readme-OK-green.svg?style=flat-square)](https://github.com/RichardLitt/standard-readme)
[![DOI](https://zenodo.org/badge/103732996.svg)](https://zenodo.org/badge/latestdoi/103732996)

 A commandline tool for [GloBI](https://globalbioticinteraction.org) named after ecologist [Charles S. Elton](https://en.wikipedia.org/wiki/Charles_Sutherland_Elton), author of [Animal Ecology](https://doi.org/10.5962/bhl.title.7435). Can be used in combination with [elton-archive](https://github.com/globalbioticinteractions/elton-archive).

<a href="http://globalbioticinteractions.org/">
  <img src="http://www.globalbioticinteractions.org/assets/globi.svg" height="120">
</a>

## Table of Contents

- [Install](#install)
- [Usage](#usage)
- [Examples](#examples)
- [Building](#building)
- [Contribute](#contribute)
- [License](#license)

## Install

### Official releases

You can use this project by including `elton.jar` from one of the [releases](https://github.com/globalbioticinteractions/elton/releases).

### Maven, Gradle, SBT
Elton is made available through a [maven](https://maven.apache.org) repository.

To include elton in your project, add the following sections to your pom.xml (or equivalent for sbt, gradle etc):
```
  <repositories>
    <repository>
        <id>depot.globalbioticinteractions.org</id>
        <url>https://depot.globalbioticinteractions.org/release</url>
    </repository>
  </repositories>

  <dependencies>
    <dependency>
      <groupId>org.globalbioticinteractions</groupId>
      <artifactId>elton</artifactId>
      <version>0.3.2</version>
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
      Usage: list [options] [namespace1] [namespace2] ...
        Options:
          --cache-dir, -c
            cache directory
            Default: ./datasets
          --offline, -o
            offline
            Default: false

    update      Update Datasets with Local Repository
      Usage: update [options] [namespace1] [namespace2] ...
        Options:
          --cache-dir, -c
            cache directory
            Default: ./datasets

    names      List Dataset (Taxon) Names For Local Datasets
      Usage: names [options] [namespace1] [namespace2] ...
        Options:
          --cache-dir, -c
            cache directory
            Default: ./datasets

    interactions      List Interacting Taxon Pairs For Local Datasets
      Usage: interactions [options] [namespace1] [namespace2] ...
        Options:
          --cache-dir, -c
            cache directory
            Default: ./datasets

    nanopubs      Generate Nanopubs Describing Interactions in Local Datasets
      Usage: nanopubs [options] [namespace1] [namespace2] ...
        Options:
          --cache-dir, -c
            cache directory
            Default: ./datasets

    check      Check Dataset Accessibility
      Usage: check [options] [namespace1] [namespace2] ...
        Options:
          --cache-dir, -c
            cache directory
            Default: ./datasets
          --offline, -o
            offline
            Default: false

    version      Show Version
      Usage: version
```

## Examples 

List all datasets 

```java -jar elton.jar list```

Update / download single dataset and cache locally in ./dataset folder

```java -jar elton.jar update globalbioticinteractions/template-dataset```

Update / download all datasets (might take a while)

```java -jar elton.jar update```

Note that elton is using [a rate-limited GitHub APIs](https://developer.github.com/v3/#rate-limiting). If you are seeing "Forbidden" http errors, suggest to provide OAUTH key/secret like:

``` java -Dgithub.client.id=[client id] -Dgithub.client.secret=[client secret] -jar elton.jar update```

Please refer to GitHub API documentation like https://developer.github.com/v3/#rate-limiting learn more about getting a client id/secret pair.

List interactions of all locally cached datasets

```java -jar elton.jar interactions```

Note that you can retrieve an archived (and versioned) copy of existing species interaction datasets using [Elton's archive](https://github.com/globalbiotincinteractions/elton-archive) .

## Contribute

Feel free to join in. All welcome. Open an [issue](https://github.com/globalbioticinteractions/elton/issues)!

## License

[GPL](LICENSE)
