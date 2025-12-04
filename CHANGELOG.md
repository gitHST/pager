
# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

---

## [0.1.0] - 2025-04-18
### Added
- Initial release of the app.
- Jetpack Compose UI.
- Project structure and database schema.
- Basic Compose screens.
- Book review diary.
- Add books ability
- Local Room database for offline storage.
- Application icon

### Fixed

### Changed

---
## [0.2.0] - 2025-04-23
### Added
- OpenLibrary API integration for book search.
- Book cover image loading
- Book Rating and reviewing system
- Edit and delete functionality to reviews

### Fixed
- Minor UI theme issues

### Changed
- New "Add book" page layout and functionality

---
## [0.2.1] - 2025-04-24
### Added

### Fixed
- Minification was causing API issues, so removed for now
- a few other minor bug fixes

### Changed

---
## [0.2.2] - 2025-04-24
### Added
- Default "No Cover" image for books without covers
- Loading wheel between submitting book and navigation
### Fixed
- Transparency issue with search bar
- Retain active status when search query deleted
- Fixed roundness issues with covers

### Changed
- Search results box shrinks and grows with keyboard (untested on smaller devices)
- Review box fits screen size nicely
- Clicking search now hides keyboard

---
## [0.3.0] - 2025-12-04
### Added
- Complete Quotes system, including:
    - Carousel view for quotes per book
    - All Quotes view with sort controls and scrolling
    - Add Quote modal
    - Edit Quote modal with edit/delete functionality
    - Page number input
- Scanner for quotes that uses ML Kit to extract text from images
- Swipe logic improvements:
- New edit/delete confirmation dialog matching visual style of Review deletion dialog

### Fixed
- Page number field clipping issue in Add/Edit Quote modals
- Visual alignment issues in All Quotes and Carousel quotes list
- Test tasks failing due to incompatible JDK and Jacoco conflicts

### Changed
- Updated swipe animations for Quotes tab transitions.