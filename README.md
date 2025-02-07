**Linkora** is the ultimate tool for organizing links on Android or desktop, with syncing capability
via a [self-hostable sync server](https://github.com/LinkoraApp/sync-server). Whether you need to
save a quick link or manage them in detailed folders, Linkora gets it done.

## Download

[<img src="https://github.com/user-attachments/assets/a50513b3-dbf8-48c1-bff8-1f4215fefbb9"
alt="Get it on GitHub"
height="80">](https://github.com/sakethpathike/Linkora/releases)

## Features

- Save and organize links with ease.
- Categorize links into folders and subfolders.
- Highlight important links for quick access.
- Archive old links to keep things tidy.
- Customize link names to your preference.
- Share directly from other apps (Android-specific feature).
- Sort and search links and folders quickly.
- Import and export data easily.
- Auto-recognize link images and titles.
- Add folders to your **_Panels_** for instant home screen access.
- Localization supported via a central localization server. The server code
  can be found [here](https://github.com/LinkoraApp/LinkoraLocalizationServer).
- Sync data with a [self-hostable server](https://github.com/LinkoraApp/sync-server)

## Screenshots

### Tablet/Desktop Screenshots

|                    |                    |
|--------------------|--------------------|
| ![](assets/t1.png) | ![](assets/t2.png) |
| ![](assets/t3.png) | ![](assets/t5.png) |

### Mobile Screenshots

|                    |                    |                    |                    |
|--------------------|--------------------|--------------------|--------------------|
| ![](assets/m1.png) | ![](assets/m2.png) | ![](assets/m3.png) | ![](assets/m4.png) |
| ![](assets/m5.png) | ![](assets/m6.png) | ![](assets/m7.png) | ![](assets/m8.png) |

|                         Sharing links from other apps     (Android-specific)                          |
|:-----------------------------------------------------------------------------------------------------:|
| <video src="https://github.com/user-attachments/assets/65fdbdb9-83da-4d83-9dd9-2fa3e3504bc0"></video> |

## Tech Stack

- **Kotlin**: Built entirely in Kotlin.
- **Jetpack Compose**: Fully Compose-based UI.
- **Material 3**: Modern Material Design components.
- **Room**: Efficient local storage solution.
- **Ktor Client**: For making network requests.
- **Kotlin Coroutines**: Smooth background processing.
- **Kotlin Flows**: For handling asynchronous data streams.
- **Kotlin Channels**: For real-time updates when communicating with different platforms from shared
  code and in some other cases for UI events.
- **Kotlinx Serialization**: For server and vxTwitter API response handling.
- **jsoup**: Custom implementation for importing HTML was easier as jsoup was integrated to scrape
  HTML metadata and import HTML-based files. jsoup is also used for remote website scraping, which
  helps detect titles and images.
- **Coil**: For image loading.
- **Architecture Components**: DataStore, Navigation, and ViewModel.

## Join the Community

[![](https://discord.com/api/guilds/1214971383352664104/widget.png?style=banner2)](https://discord.gg/ZDBXNtv8MD)

Join the Discord for regular updates and discussions related to this project.

### License

This project is licensed under the MIT License.