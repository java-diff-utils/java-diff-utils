# java-diff-utils

## Status

[![Build Status](https://travis-ci.org/java-diff-utils/java-diff-utils.svg?branch=master)](https://travis-ci.org/java-diff-utils/java-diff-utils)

[![Build Status using Github Actions](https://github.com/java-diff-utils/java-diff-utils/workflows/Java%20CI%20with%20Maven/badge.svg)](https://github.com/java-diff-utils/java-diff-utils/actions?query=workflow%3A%22Java+CI+with+Maven%22)

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/002c53aa0c924f71ac80a2f65446dfdd)](https://www.codacy.com/gh/java-diff-utils/java-diff-utils/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=java-diff-utils/java-diff-utils&amp;utm_campaign=Badge_Grade)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.java-diff-utils/java-diff-utils/badge.svg)](http://maven-badges.herokuapp.com/maven-central/io.github.java-diff-utils/java-diff-utils)

## Intro

Diff Utils library is an OpenSource library for performing the comparison operations between texts: computing diffs, applying patches, generating unified diffs or parsing them, generating diff output for easy future displaying (like side-by-side view) and so on.

Main reason to build this library was the lack of easy-to-use libraries with all the usual stuff you need while working with diff files. Originally it was inspired by JRCS library and it's nice design of diff module.

**This is originally a fork of java-diff-utils from Google Code Archive.**

## API

Javadocs of the actual release version: [JavaDocs java-diff-utils](https://java-diff-utils.github.io/java-diff-utils/4.10/docs/apidocs/)

## Examples

Look [here](https://github.com/java-diff-utils/java-diff-utils/wiki) to find more helpful informations and examples.

These two outputs are generated using this java-diff-utils. The source code can also be found at the *Examples* page:

**Producing a one liner including all difference information.**

This is a test ~senctence~**for diffutils**.

**Producing a side by side view of computed differences.**

|original|new|
|--------|---|
|This is a test ~senctence~.|This is a test **for diffutils**.|
|This is the second line.|This is the second line.|
|~And here is the finish.~||

## Main Features

* computing the difference between two texts.
* capable to hand more than plain ascii. Arrays or List of any type that implements hashCode() and equals() correctly can be subject to differencing using this library
* patch and unpatch the text with the given patch
* parsing the unified diff format
* producing human-readable differences
* inline difference construction
* Algorithms:
  * Meyers Standard Algorithm
  * Meyers with linear space improvement
  * HistogramDiff using JGit Library

### Algorithms

* Meyer's diff
* HistogramDiff

But it can easily replaced by any other which is better for handing your texts. I have plan to add implementation of some in future.

## Source Code conventions

Recently a checkstyle process was integrated into the build process. java-diff-utils follows the sun java format convention. There are no TABs allowed. Use spaces.

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

Just add the code below to your maven dependencies:

```xml
<dependency>
    <groupId>io.github.java-diff-utils</groupId>
    <artifactId>java-diff-utils</artifactId>
    <version>4.12</version>
</dependency>
```

or using gradle:

```groovy
// https://mvnrepository.com/artifact/io.github.java-diff-utils/java-diff-utils
implementation "io.github.java-diff-utils:java-diff-utils:4.12"
```
