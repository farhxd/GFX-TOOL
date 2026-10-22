# Open Source GFX Tool

### Device-Specific Game Configuration & Optimization for Android

An open-source Android application built with native Java to customize supported mobile game configurations, explore device-specific graphics settings, and enable high-frame-rate options where supported.

Open Source GFX Tool combines Android filesystem engineering, multi-version storage access, and specialized configuration-processing logic to give users greater control over their gaming configurations.

**Status:** Working Beta · Under Testing  
**Platform:** Android 6–16 (API 23–36)  
**Language:** Java  
**Project Type:** Native Android Application

> The project is under active testing. Compatibility and optimization results vary by device, Android version, game version, and available configuration options.

---

## Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Engineering Highlights](#engineering-highlights)
- [Architecture](#architecture)
- [Android Storage Compatibility](#android-storage-compatibility)
- [How It Works](#how-it-works)
- [Technology Stack](#technology-stack)
- [Compatibility and Limitations](#compatibility-and-limitations)
- [Building from Source](#building-from-source)
- [Testing](#testing)
- [Contributing](#contributing)
- [Roadmap](#roadmap)
- [Disclaimer](#disclaimer)
- [License](#license)

---

## Overview

Modern Android versions impose increasingly strict restrictions on application access to shared and app-specific storage. This creates a significant engineering challenge for tools that need to interact with existing game configuration files, especially files located in `Android/data` and `Android/obb`.

This GFX Tool addresses this challenge through a multi-strategy storage-access implementation and a specialized file-handling library designed for complex filesystem operations.

The application combines these capabilities with game-specific configuration logic to apply device-aware graphics and frame-rate settings.

### Project Goals

- Simplify supported mobile game configuration customization.
- Provide storage-access strategies for different Android versions.
- Separate complex file operations from optimization logic.
- Apply device-specific settings to supported games.
- Explore higher frame-rate configurations, including 120 FPS where compatible.
- Maintain a codebase that can be tested, extended, and improved by the open-source community.

## Key Features

### Graphics and Performance Configuration

- **Graphics customization:** Modify supported game configuration options.
- **High-FPS configurations:** Apply supported frame-rate settings, including 90 FPS and 120 FPS configurations where applicable.
- **Device-specific optimization:** Tailor supported configuration choices to device characteristics.
- **Smoothness-oriented tuning:** Apply configuration changes intended to improve the gaming experience.
- **Game configuration management:** Work with existing configuration files instead of relying exclusively on in-game menus.

### Advanced Android File Management

- **Multi-strategy storage access:** Select an appropriate file-access mechanism based on the platform and available permissions.
- **Complex filesystem operations:** Use a specialized file-handling library for configuration-related file management.
- **Modern Android support:** Account for scoped-storage restrictions and Storage Access Framework limitations.
- **Shizuku integration:** Support additional filesystem access workflows where conventional APIs are insufficient and Shizuku-authorized operations are available.

## Engineering Highlights

This project is intended to demonstrate practical Android engineering beyond basic UI development.

### 1. Cross-Version Filesystem Access

Android storage behavior varies significantly across platform versions. The application incorporates different approaches to filesystem access, including:

- Java NIO filesystem APIs where direct access is permitted.
- `DocumentFile` for working with user-authorized document trees.
- `DocumentsContract` for Storage Access Framework operations.
- Shizuku-assisted access where supported and authorized.

The engineering challenge is not simply opening a file; it is handling different filesystem access capabilities while preserving correct file-operation behavior.

### 2. Specialized File-Handling Library

The application includes a dedicated file-handling library for complex filesystem operations.

Its role is to support the file operations required by the configuration workflow while reducing the complexity of dealing with different storage-access mechanisms.

This layer is a key part of the project because configuration processing depends on reliable file discovery, access, modification, and error handling.

### 3. Device-Aware Configuration Processing

Rather than treating every device as identical, the application uses specialized optimization logic to select or apply configurations intended for supported device and game combinations.

This approach provides a foundation for maintaining game-specific rules and accounting for differences in supported settings.

### 4. Existing Configuration Modification

The application works with existing game configuration files to apply supported changes.

This requires careful handling of file paths, permissions, file contents, and write operations. Original configurations should be backed up wherever the implementation supports it, allowing recovery from invalid or unwanted changes.

### 5. High-Frame-Rate Configuration

One of the application's notable capabilities is its logic for enabling 120 FPS configuration options in specific supported mobile games.

Actual results depend on whether the game recognizes the setting, whether the device can sustain the target frame rate, and whether the game or its platform imposes additional restrictions.

---

## Architecture

The application can be understood as a set of cooperating responsibilities.

```text
                 Android Application
                         |
                         v
             Game Configuration Workflow
                         |
              +----------+----------+
              |                     |
              v                     v
       Device Assessment      Game-Specific Rules
              |                     |
              +----------+----------+
                         |
                         v
             Configuration Processing
                         |
                         v
              File-Handling Library
                         |
             +-----------+-----------+
             |           |           |
             v           v           v
          Java NIO    SAF APIs    Shizuku
                     DocumentFile   Access
                     DocumentsContract
                         |
                         v
               Supported Game Files
```

The diagram illustrates the main conceptual responsibilities; it is not a claim that every operation follows this exact runtime sequence.

### Architectural Principles

- **Separation of concerns:** Keep configuration logic distinct from storage-access implementation where the codebase permits.
- **Platform awareness:** Account for different Android storage capabilities.
- **Controlled modification:** Restrict operations to intended files and directories.
- **Failure handling:** Handle inaccessible paths, unavailable providers, and failed file operations.
- **Extensibility:** Make it easier to add support for new games and configuration formats.

---

## Android Storage Compatibility

Accessing game files is one of the most technically challenging parts of this project.

| API / Mechanism | Intended role |
|---|---|
| `java.nio.file` | Filesystem operations where direct access is available |
| `DocumentFile` | User-authorized document and directory access |
| `DocumentsContract` | Storage Access Framework document operations |
| Shizuku API | Supported, separately authorized filesystem access workflows |

### Android Version Considerations

| Android versions | Compatibility approach |
|---|---|
| Android 6–9 | Direct filesystem operations where permitted |
| Android 10 | Legacy and Storage Access Framework behavior, depending on target SDK and permissions |
| Android 11–13 | Scoped-storage restrictions, document-provider access, and Shizuku-assisted workflows |
| Android 14–16 | Modern storage restrictions and supported access methods |

These ranges describe the intended compatibility strategy, not a guarantee that every feature has been verified on every release.

**Important:** `DocumentFile` and `DocumentsContract` do not bypass Android's storage restrictions. Shizuku also requires appropriate setup and authorization and does not guarantee unrestricted access to every protected directory.

---

## How It Works

The configuration workflow is built around a sequence of operations:

1. **Select a supported game** and its relevant configuration workflow.
2. **Assess available device information** needed for the applicable configuration.
3. **Choose suitable settings** using the supported optimization logic.
4. **Resolve the target files** through the available storage-access mechanism.
5. **Read and process configuration data** using the application's file-handling and configuration logic.
6. **Apply supported modifications** to the relevant files.
7. **Evaluate the result** by launching the game and checking its behavior.

The exact sequence and available operations depend on the game and storage-access method.

---

## Technology Stack

| Technology | Purpose |
|---|---|
| Java | Native Android application development |
| Android SDK | Platform APIs and application integration |
| Java NIO | Filesystem operations where permitted |
| Storage Access Framework | User-authorized document and directory access |
| `DocumentFile` | Document-provider abstraction |
| `DocumentsContract` | Document and URI-based operations |
| Shizuku API | Additional authorized storage-access workflows |
| OpenGL / Vulkan concepts | Graphics configuration considerations |
| Gradle | Build and dependency management |

---

## Compatibility and Limitations

The project targets Android 6–16, but individual capabilities depend on the environment.

### Factors Affecting Compatibility

- Android version and target SDK.
- Device manufacturer and filesystem behavior.
- Granted storage permissions and document-provider support.
- Shizuku availability and authorization.
- Game version and configuration format.
- Hardware capabilities and display refresh rate.
- Game-enforced graphics and frame-rate limits.

### Performance Disclaimer

Modifying a configuration file does not guarantee higher FPS, lower latency, reduced lag, or better frame pacing. Actual performance depends on the game engine, device hardware, thermal limits, firmware, and whether the game accepts the modified settings.

Compatibility claims should be evaluated against tested device and game combinations rather than assumed from the Android version alone.

---

## Building from Source

### Prerequisites

- Android Studio.
- A compatible JDK and Android SDK.
- Gradle dependencies required by the project.
- A compatible Android device or emulator.
- Shizuku, if needed for the selected workflow.

### Setup

Clone the repository:

```bash
git clone https://github.com/farhxd/GFX-TOOL.git
```

Open the cloned project in Android Studio and allow Gradle synchronization to complete.

Build the debug APK from the project root:

**Windows PowerShell**

```powershell
.\gradlew.bat assembleDebug
```

**Linux / macOS**

```bash
./gradlew assembleDebug
```

The generated APK is typically located under:

```text
app/build/outputs/apk/debug/
```

Replace the placeholder repository URL with the actual project URL. Build requirements may vary with the project's Gradle and Android Gradle Plugin versions.

---

## Testing

As a working beta, the project benefits from systematic compatibility and regression testing.

Important test areas include:

- File reading and writing across supported access methods.
- Directory traversal and missing-file handling.
- Permission denial and permission revocation.
- Document-provider failures and inaccessible URIs.
- Shizuku unavailable, unauthorized, or disconnected states.
- Configuration validation and failed-write recovery.
- Backup and restoration behavior, where implemented.
- Game-specific configuration correctness.
- Android-version-specific behavior.
- Regression testing after configuration-engine changes.

### Compatibility Reporting

When reporting a bug, include:

- Device model.
- Android version.
- Game and game version.
- Storage-access method.
- Steps to reproduce.
- Relevant sanitized logs.

Do not publish personal information, access tokens, or private configuration files.

---

## Roadmap

Potential areas for future development:

- [ ] Publish a verified device and game compatibility matrix.
- [ ] Expand configuration support for additional games.
- [ ] Improve automated configuration validation.
- [ ] Add comprehensive backup and restoration tests.
- [ ] Strengthen file-operation error handling and recovery.
- [ ] Add unit tests for configuration-processing logic.
- [ ] Improve documentation of storage-access decisions.
- [ ] Add reproducible performance-testing procedures.
- [ ] Welcome community contributions and compatibility reports.

---

## Contributing

Contributions are welcome from Android developers, testers, and open-source enthusiasts.

1. Fork the repository.
2. Create a focused feature branch.
3. Make your changes with clear commit messages.
4. Test the relevant functionality.
5. Document compatibility changes and known limitations.
6. Submit a pull request describing the problem and solution.

For substantial changes, open an issue first to discuss the proposed implementation.

---

## Security and Responsible Use

This application is designed for game configuration customization and Android development experimentation.

- Back up original configuration files before modification.
- Request only the permissions needed for the intended operation.
- Respect game terms of service and platform policies.
- Do not use the application to bypass account security or gain unfair advantages in online gameplay.
- Report security issues privately to the repository maintainer rather than publicly disclosing exploitable details.

---

## Disclaimer

Open Source GFX Tool is an independent project and is not affiliated with or endorsed by any game developer unless explicitly stated.

Game names and trademarks belong to their respective owners. Modified configurations may be rejected or reset by game updates, and some settings may be restricted by the game or device.

The software is provided on an as-is basis, subject to the terms of the project's chosen open-source license.

## License

See [`LICENSE`](LICENSE) for the applicable license.

If no license has been added yet, select an appropriate open-source license before accepting external contributions or granting reuse rights.

---

**Built with Java, Android APIs, and a focus on solving real-world mobile filesystem and configuration-engineering challenges.**
