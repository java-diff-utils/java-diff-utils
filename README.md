# java-diff-utils

## Status

[![Build Status](https://travis-ci.org/java-diff-utils/java-diff-utils.svg?branch=master)](https://travis-ci.org/java-diff-utils/java-diff-utils)

[![Build Status using GitHub Actions](https://github.com/java-diff-utils/java-diff-utils/workflows/Java%20CI%20with%20Maven/badge.svg)](https://github.com/java-diff-utils/java-diff-utils/actions?query=workflow%3A%22Java+CI+with+Maven%22)

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/002c53aa0c924f71ac80a2f65446dfdd)](https://www.codacy.com/gh/java-diff-utils/java-diff-utils/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=java-diff-utils/java-diff-utils&amp;utm_campaign=Badge_Grade)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.java-diff-utils/java-diff-utils/badge.svg)](http://maven-badges.herokuapp.com/maven-central/io.github.java-diff-utils/java-diff-utils)

## Intro

Diff Utils library is an open source library for performing comparison operations between texts: computing diffs, applying patches, generating unified diffs or parsing them, generating diff output for easy future displaying (like side-by-side view) and so on.

The main reason to build this library was the lack of easy-to-use libraries with all the usual stuff you need while working with diff files. Originally it was inspired by JRCS library and its nice design of diff module.

**This is originally a fork of java-diff-utils from Google Code Archive.**

## GPG Signature Validation

The GPG signing key in [KEYS] is used for this project's artifacts.

## API

Javadocs of the actual release version: [Javadocs java-diff-utils](https://java-diff-utils.github.io/java-diff-utils/4.10/docs/apidocs/)

## Examples

Look [here](https://github.com/java-diff-utils/java-diff-utils/wiki) to find more helpful information and examples.

These two outputs are generated using java-diff-utils. The source code can also be found on the [Examples](https://github.com/java-diff-utils/java-diff-utils/wiki/Examples) page:

**Producing a one liner including all difference information.**

```java
// Create a configured DiffRowGenerator
DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .mergeOriginalRevised(true)
                .inlineDiffByWord(true)
                .oldTag(f -> "~")      // Introduce markdown style for strikethrough
                .newTag(f -> "**")     // Introduce markdown style for bold
                .build();

// Compute the differences for two test texts
List<DiffRow> rows = generator.generateDiffRows(
                Arrays.asList("This is a test sentence."),
                Arrays.asList("This is a test for diffutils."));

System.out.println(rows.get(0).getOldLine());
```

This is a test ~sentence~**for diffutils**.

**Producing a side-by-side view of computed differences.**

```java
DiffRowGenerator generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .inlineDiffByWord(true)
                .oldTag(f -> "~")
                .newTag(f -> "**")
                .build();
List<DiffRow> rows = generator.generateDiffRows(
                Arrays.asList("This is a test sentence.", "This is the second line.", "And here is the finish."),
                Arrays.asList("This is a test for diffutils.", "This is the second line."));

System.out.println("|original|new|");
System.out.println("|--------|---|");
for (DiffRow row : rows) {
    System.out.println("|" + row.getOldLine() + "|" + row.getNewLine() + "|");
}
```

|original|new|
|--------|---|
|This is a test ~sentence~.|This is a test **for diffutils**.|
|This is the second line.|This is the second line.|
|~And here is the finish.~||

## Main Features

* Computing the difference between two texts.
* Capable of handling more than plain ASCII. Arrays or lists of any type that implement `hashCode()` and `equals()` correctly can be subject to differencing using this library
* Patch and unpatch the text with the given patch
* Parsing the unified diff format
* Producing human-readable differences
* Inline difference construction
* Algorithms:
  * Myers standard algorithm
  * Myers with linear space improvement
  * HistogramDiff using the JGit library

### Algorithms

* Myers diff
* HistogramDiff

But it can easily be replaced by any other which is better for handling your texts. I have a plan to add the implementation of some in the future.

## Source Code conventions

Recently a checkstyle process was integrated into the build process. java-diff-utils follows the Sun Java format convention. There are no tabs allowed. Use spaces.

```java
public static <T> Patch<T> diff(List<T> original, List<T> revised,
    BiPredicate<T, T> equalizer) throws DiffException {
    if (equalizer != null) {
        return DiffUtils.diff(original, revised,
        new MyersDiff<>(equalizer));
    }
    return DiffUtils.diff(original, revised, new MyersDiff<>());
}
```

This is a valid piece of source code:

* blocks without braces are not allowed
* after control statements (if, while, for) a whitespace is expected
* the opening brace should be in the same line as the control statement

### To Install

Just add the code below to your Maven dependencies:

```xml
<dependency>
    <groupId>io.github.java-diff-utils</groupId>
    <artifactId>java-diff-utils</artifactId>
    <version>4.15</version>
</dependency>
```

or using Gradle:

```groovy
// https://mvnrepository.com/artifact/io.github.java-diff-utils/java-diff-utils
implementation "io.github.java-diff-utils:java-diff-utils:4.12"
```
