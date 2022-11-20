# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).
This project uses a custom versioning scheme (and not [Semantic Versioning](https://semver.org/spec/v2.0.0.html)).

## [4.13]

* API change: due to Issue #159: the author of the algorithm is Eugene Myers, therefore classes and methods were renamed accordingly

### Changed

## [4.11]

### Changed

* bugfixing new UnifiedDiff reader
  * header for each file
  * skip empty lines
* introduction of Meyers Diff Algorithm with Linear Space improvment (until matured this will not be the default diff algorithm)
* introduction of DiffAlgorithmFactory to set the default diff algorithm DiffUtils use (`DiffUtils.withDefaultDiffAlgorithmFactory(MeyersDiffWithLinearSpace.factory());`)

## [4.10]

### Changed

* bugfixing on new UnifiedDiff reader / writer for multifile usage
* bugfix for wrong DiffRow type while transforming from a patch that removed a line in one changeset
* introduced change position into UnifiedDiff reader
* introduced first version of conflict output possibility (like GIT merge conflict)
  * moved verification to `AbstractDelta`
  * introduced `ConflictOutput` to `Patch` to add optional behaviour to patch conflicts

## [4.9]

### Changed

* make patch serializable

## [4.8]

### Changed

* some bugfixes regarding unified diff writer
* UnifiedDiffReader improved for **deleted file mode** and better timestamp recognition
* UnifiedDiffReader improved for **new file mode** and better timestamp recognition

## [4.7]

### Changed 

* minor bug fixes
* optional include equal parts of original and revised data
* **API** change: removed DiffException completely
* added possibility to **process diffs** to for instance show whitespace characters

## [4.4] – 2019-11-06

### Changed

* java-diff-utils is now a multi module project. The main project java-diff-utils now comes without any dependencies.
* started reimplementation of unified diff tools
* Exchange `0 += 1` for `0 = 1` in UnifiedDiffUtils
* preview of new Unified Diff Reader / Writer. This is not yet feature complete but passes the tests of the old version.
  * feel free to issue some change requests for the api.
* introduces lineNormalizer extension point to e.g. change html code encoding. (issue #41)

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

[Unreleased]: https://github.com/java-diff-utils/java-diff-utils/compare/java-diff-utils-parent-4.5...HEAD
[4.5]: https://github.com/java-diff-utils/java-diff-utils/compare/java-diff-utils-parent-4.4...java-diff-utils-parent-4.5
[4.4]: https://github.com/java-diff-utils/java-diff-utils/compare/java-diff-utils-4.0...java-diff-utils-parent-4.4
[4.0]: https://github.com/java-diff-utils/java-diff-utils/compare/diffutils-3.0...java-diff-utils-4.0
[3.0]: https://github.com/java-diff-utils/java-diff-utils/compare/diffutils-2.2...diffutils-3.0
[2.2]: https://github.com/java-diff-utils/java-diff-utils/compare/diffutils-2.0...diffutils-2.2

