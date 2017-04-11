# java-diff-utils
Diff Utils library is an OpenSource library for performing the comparison operations between texts: computing diffs, applying patches, generating unified diffs or parsing them, generating diff output for easy future displaying (like side-by-side view) and so on.

Main reason to build this library was the lack of easy-to-use libraries with all the usual stuff you need while working with diff files. Originally it was inspired by JRCS library and it's nice design of diff module.

**This is originally a fork of java-diff-utils from Google Code Archive.**

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/7eba77f10bed4c2a8d08ac8dc8da4a86)](https://www.codacy.com/app/wumpz/java-diff-utils?utm_source=github.com&utm_medium=referral&utm_content=wumpz/java-diff-utils&utm_campaign=badger)
[![Build Status](https://travis-ci.org/wumpz/java-diff-utils.svg?branch=master)](https://travis-ci.org/wumpz/java-diff-utils)

## Examples ##

Look [here](https://github.com/wumpz/java-diff-utils/wiki) to find more helpful informations and examples. 

## Main Features ##

  * computing the difference between two texts.
  * capable to hand more than plain ascci. Arrays or List of any type that implements hashCode() and equals() correctly can be subject to differencing using this library
  * patch and unpatch the text with the given patch
  * parsing the unified diff format
  * producing human-readable differences
  * inline difference construction

### Algoritms ###

This library implements Myer's diff algorithm. But it can easily replaced by any other which is better for handing your texts. I have plan to add implementation of some in future.

### Changelog ###
  * Version 1.4
    * switch to maven and removed other artifacts
    * changed groupid to **com.github.java-diff-utils** due to different forks at github
    * updated maven plugins
    * JDK 1.8 compatibility
    * support for inline merge 
    * restructured packages
    * changed API 
  * Version 1.2
    * JDK 1.5 compatibility
    * Ant build script
    * Generate output in unified diff format (thanks for Bill James)

### To Install ###

**This jar is not yet to get at maven central.**

Just add the code below to your maven dependencies:
```
<dependency>
    <groupId>com.github.java-diff-utils</groupId>
    <artifactId>diffutils</artifactId>
    <version>1.2.1</version>
</dependency>
```
