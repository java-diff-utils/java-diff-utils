# java-diff-utils

## Status ##
[![Build Status](https://travis-ci.org/wumpz/java-diff-utils.svg?branch=master)](https://travis-ci.org/wumpz/java-diff-utils)     [![Codacy Badge](https://api.codacy.com/project/badge/Grade/7eba77f10bed4c2a8d08ac8dc8da4a86)](https://www.codacy.com/app/wumpz/java-diff-utils?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=wumpz/java-diff-utils&amp;utm_campaign=Badge_Grade)

After a while using it, at least for me, this one seems to be feature complete. If someone finds bugs or has improvement ideas, please file an issue. I wonder why that this project is a zero issue project.


## Intro ##
Diff Utils library is an OpenSource library for performing the comparison operations between texts: computing diffs, applying patches, generating unified diffs or parsing them, generating diff output for easy future displaying (like side-by-side view) and so on.

Main reason to build this library was the lack of easy-to-use libraries with all the usual stuff you need while working with diff files. Originally it was inspired by JRCS library and it's nice design of diff module.

**This is originally a fork of java-diff-utils from Google Code Archive.**

## Examples ##

Look [here](https://github.com/wumpz/java-diff-utils/wiki) to find more helpful informations and examples. 

These two outputs are generated using this java-diff-utils. The source code can also be found at the *Examples* page:

**Producing a one liner including all difference information.**

This is a test ~senctence~**for diffutils**.


**Producing a side by side view of computed differences.**

|original|new|
|--------|---|
|This is a test ~senctence~.|This is a test **for diffutils**.|
|This is the second line.|This is the second line.|
|~And here is the finish.~||


## Main Features ##

  * computing the difference between two texts.
  * capable to hand more than plain ascci. Arrays or List of any type that implements hashCode() and equals() correctly can be subject to differencing using this library
  * patch and unpatch the text with the given patch
  * parsing the unified diff format
  * producing human-readable differences
  * inline difference construction
  * Algorithms:
    * Myer
    * HistogramDiff using JGit Library

### Algoritms ###

* Myer's diff
* HistogramDiff 

But it can easily replaced by any other which is better for handing your texts. I have plan to add implementation of some in future.

### Changelog ###
  * Version 2.3-SNAPSHOT
  * Version 2.2
    * released at maven central
    * included checkstyle source code conventions
    * groupid changed to **com.github.wumpz**, due to maven central releasing
    * allow configurable splitting of lines to define the blocks to compare (words, characters, phrases).
  * Version 2.0
    * switch to maven and removed other artifacts
    * changed groupid to **com.github.java-diff-utils** due to different forks at github
    * updated maven plugins
    * JDK 1.8 compatibility, sorry if you have to stick with older versions
    * support for inline merge
    * restructured packages heavily
    * changed API 
    * changed Algorithm to provide only cursor positions
    * integrated JGit (Eclipse Licensed) to provide HistogramDiff to gain speed for large datasets 
    * removed all kinds of helper classes in favour of new JDK 8 function classes like Predicate
  * Version 1.2
    * JDK 1.5 compatibility
    * Ant build script
    * Generate output in unified diff format (thanks for Bill James)

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

### To Install ###

**This jar is not yet to get at maven central.**

Just add the code below to your maven dependencies:
```
<dependency>
    <groupId>com.github.wumpz</groupId>
    <artifactId>diffutils</artifactId>
    <version>2.2</version>
</dependency>
```
