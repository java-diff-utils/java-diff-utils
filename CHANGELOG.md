# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).
This project uses a custom versioning scheme (and not [Semantic Versioning](https://semver.org/spec/v2.0.0.html)).

## [Unreleased]

### Changed

* Exchange `0 += 1` for `0 = 1` in UnifiedDiffUtils

## [4.0] – 2019-01-09

### Changed

* moved to organisation **java-diff-utils**
* changed groupid to **io.github.java-diff-utils** and artifact id to **java-diff-utils**

## [3.0] – 2018-10-18

### Added

* Introduced a process listener to diff algorithms. For long running
  diffs one could implement some progress information.
* automatic module name for JDK 9 and higher usage

### Changed

* changed generation of inline diffes, if there are different linefeeds within one diff, then these are excluded from the diff block.

### Removed

* Due to licensing issues Delta.java and DiffAlgorithm.java were removed.

## [2.2] – 2017-11-09

### Added

* released at maven central
* included checkstyle source code conventions
* allow configurable splitting of lines to define the blocks to compare (words, characters, phrases).

### Changed

* groupid changed to **com.github.wumpz**, due to maven central releasing

## [2.0] – 2017-08-14

### Added

* support for inline merge
* integrated JGit (Eclipse Licensed) to provide HistogramDiff to gain speed for large datasets

### Changed

* switch to maven and removed other artifacts
* changed groupid to **com.github.java-diff-utils** due to different forks at github
* updated maven plugins
* JDK 1.8 compatibility, sorry if you have to stick with older versions
* restructured packages heavily
* changed API
* changed Algorithm to provide only cursor positions

### Removed

* removed all kinds of helper classes in favour of new JDK 8 function classes like Predicate

## 1.2

### Added

* JDK 1.5 compatibility
* Ant build script
* Generate output in unified diff format (thanks for Bill James)

[Unreleased]: https://github.com/java-diff-utils/java-diff-utils/compare/java-diff-utils-4.0...HEAD
[4.0]: https://github.com/java-diff-utils/java-diff-utils/compare/diff-utils-3.0...java-diff-utils-4.0
[3.0]: https://github.com/java-diff-utils/java-diff-utils/compare/diff-utils-2.2...diff-utils-3.0
[2.2]: https://github.com/java-diff-utils/java-diff-utils/compare/diff-utils-2.0...diff-utils-2.2

