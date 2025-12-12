# Pager
![Version](https://img.shields.io/badge/version-0.4.0-blue.svg)
![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)
![Platform](https://img.shields.io/badge/platform-Android-green.svg)
![Made with Kotlin](https://img.shields.io/badge/made%20with-Kotlin-7F52FF.svg)
![UI: Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-purple.svg)

Pager is an all in one android app for browsing and reviewing books and sharing your thoughts with your friends, or the world!
## Goals for major milestones
### `Version 0.1.0 "Footnote"`

| Feature                                                                                                    | Screenshots                                                                                                                             |
|------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| The ability to add books to diary by manually typing the name and author, and giving your optional review. | <table><tr><td><img src="img/0.1.0/AddPage.png" width="200"/></td><td><img src="img/0.1.0/AddPopup.png" width="200"/></td></tr></table> |
| The ability to view all your books and reviews from the diary page, ordered by date reviewed.              | <img src="img/0.1.0/Diary.png" alt="img.png" width="200"/>                                                                              |


### `Version 0.2.0 "Alexandria"`

| Feature                                                                     | Screenshots                                                                                                                                                         |
|-----------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| The ability to add books to diary by searching for them using an online API | <table><tr><td><img src="img/0.2.0/searchbar.png" alt="img.png" width="200"/></td><td><img src="img/0.2.0/search.png" alt="img.png" width="200"/></td></tr></table> |
| The ability to rate books from 1 - 10                                       | <img src="img/0.2.0/rating.png" alt="img_3.png" width="200"/>                                                                                                       |
| The ability to mark reviews for spoilers                                    | <img src="img/0.2.0/spoilers.png" alt="img_2.png" width="200"/>                                                                                                     |
| The ability to mark reviews as public, just friends, or private             | <img src="img/0.2.0/publicity.png" alt="img_1.png" width="200"/>                                                                                                    |
| The ability to mark any date as the date read                               | <img src="img/0.2.0/date.png" alt="img_4.png" width="200"/>                                                                                                         |
| The ability to see book covers in diary                                     | <img src="img/0.2.0/diary.png" alt="img_5.png" width="200"/>                                                                                                        |
| The ability to delete reviews                                               | <img src="img/0.2.0/delete.png" alt="img_6.png" width="200"/>                                                                                                       |
| The ability to edit review text, publicity, rating, and spoilers            | <img src="img/0.2.0/edit.png" alt="img_7.png" width="200"/>                                                                                                         |

### `Version 0.3.0 "Housekeeping"`
- Development
  - Code style
    - Integrate ktlint
    - Configure auto formatting with gradle task
  - Code coverage
    - Set up JaCoCo for unit test coverage
  - Testing
    - Implement JUnit 5 for unit testing
    - Use MockK for mocking dependencies
    - Add basic test coverage
  - Structure & Code Quality
    - Modularize code
    - Refactor and clean up code
  - CI/CD
    - Set up Github Actions to automate:
      - Lint checks
      - Unit tests
      - Coverage Reporting
  - Disable landscape mode

### `Version 0.4.0 "Voltaire"`

| Feature                                                                         | Screenshots                                                                                                                                                        |
|---------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| The ability to view books in a carousel, with their quotes below                | <img src="img/0.4.0/carousel.png" alt="img_1.png" width="200"/>                                                                                                    |
| The ability to add quotes by writing them with optional page number             | <img src="img/0.4.0/writequote.png" alt="img_2.png" width="200"/>                                                                                                  |
| The ability to view all quotes in one scrollable page                           | <img src="img/0.4.0/allquotes.png" alt="img_3.png" width="200"/>                                                                                                   |
| The ability to take a picture of the page, scan for text, and select your quote | <table><tr><td><img src="img/0.4.0/scan1.png" alt="img_4.png" width="200"/></td><td><img src="img/0.4.0/scan2.png" alt="img_5.png" width="200"/></td></tr></table> |
| The ability to trim the selected text to your quote and edit it afterwards      | <img src="img/0.4.0/trimquote.png" alt="img_6.png" width="200"/>                                                                                                   |

### `Version 0.5.0 "Binding"`
- Firebase
  - Move db to firebase
  - The ability to login, log out, and delete all data
  - The ability to be logged in and synced across multiple devices
- Settings
  - Will add a settings page
  - The ability to set preferences
- Customization
  - The ability to change theme
  - The ability to change the layout of the diary

### `Version 1.0.0 "Bookworm"`
- View books
  - The ability to sort diary by date reviewed, rating, title, name of author, and date published.
- Search
  - The ability to do a general search of books, and view all their information
- UI
  - Fully polished User Interface
  - Full dark mode integration

### `Version 2.0.0 "Book Club"`
- Online 
  - The ability to share your reviews with friends and the world
  - The ability to follow other users and see their reviews
      - Others reviews with spoilers will be hidden and revealed by button press
  - The ability to comment on other users reviews
  - The ability to like other users reviews
  - The ability to "Recommend" books to people, and have it show on their device
- Explore
  - The explore page will show books your friends like, recommended books, etc
- Notifications
  - The ability to receive notifications about friends, comments etc

## Possible feature ideas
#### Book calendar
#### Statistics page
## Contact me
Email: Lukew2048@gmail.com
