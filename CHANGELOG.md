# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Changed
- Rename `type` to `valueType` in column data models.
- Update anomaly data model.
- Add env variable controlled logback config.

## [0.3.5] - 2021-07-19
### Changed
- Register the timestamp converter globally.

## [0.3.4] - 2021-07-16
### Fixed
- Fix `SeriesResult*Selector` validation sequencing.

## [0.3.3] - 2021-07-16
### Fixed
- Add explicit defaults for primitive value properties.

## [0.3.2] - 2021-07-16
### Fixed
- Fix Timestamp to Instant conversion when querying data.

## [0.3.1] - 2021-07-14
### Changed
- Upgraded to `snoozy-starter` 0.7.4.
- Apply limit/offset pagination to all data fetches.

## [0.3.0] - 2021-03-19
### Added
 - Add support for `distinct` aggregation
