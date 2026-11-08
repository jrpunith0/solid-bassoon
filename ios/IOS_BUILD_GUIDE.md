# Creator Studio Pro – iOS Build & Deployment Guide (iPhone & iPad)

This document provides complete instructions for building, archiving, and deploying **Creator Studio Pro** to iOS devices (iPhone and iPad) using the shared cross-platform architecture.

---

## 1. System Requirements

- macOS Sonoma 14.0 or later
- Xcode 15.0 or later
- CocoaPods (`gem install cocoapods`)
- Apple Developer Account (for provisioning profile and TestFlight distribution)

---

## 2. Shared Cross-Platform Authentication & Cloud Sync

Creator Studio Pro uses the same unified credentials across:
- **Website** (`/web`)
- **Android Mobile & Tablet** (`/app`)
- **iOS iPhone & iPad** (`/ios`)

### Google Sign-In Setup for iOS:
1. Obtain the `GoogleService-Info.plist` from your Firebase / Google Cloud console.
2. Place `GoogleService-Info.plist` inside `/ios/App/App/`.
3. The URL scheme in `Info.plist` has been pre-configured with `com.googleusercontent.apps.creatorstudio`.

---

## 3. Installation & Pod Setup

Open Terminal and navigate to the `/ios` directory:

```bash
cd ios
pod install
```

This installs:
- `GoogleSignIn` (OAuth 2.0 login)
- `Firebase/Auth` (Universal account management)
- `Firebase/Firestore` (Cloud synchronizer)
- `Alamofire` & `SwiftyJSON` (Backend REST API integration)

---

## 4. Opening in Xcode

Open the generated workspace:

```bash
open App.xcworkspace
```

> **Important**: Always open `.xcworkspace`, not `.xcodeproj`.

---

## 5. Device Targets & Adaptive Form Factors

Creator Studio Pro natively supports:
- **iPhone**: Portrait and Landscape workflows with optimized bottom navigation.
- **iPad**: Full Split-Screen multitasking, Side Navigation Rail, and Apple Pencil annotation.

Supported Orientations configured in `Info.plist`:
- iPhone: Portrait, Landscape Left, Landscape Right
- iPad: Portrait, Portrait Upside Down, Landscape Left, Landscape Right

---

## 6. Permissions Configured

- `NSCameraUsageDescription`: Capture video assets and character model references.
- `NSMicrophoneUsageDescription`: Record voice-overs and studio podcasts.
- `NSPhotoLibraryUsageDescription`: Import graphics, storyboards, and Manga panels.
- `NSPhotoLibraryAddUsageDescription`: Save exported MP4 videos to Camera Roll.
- `NSSpeechRecognitionUsageDescription`: Audio transcription and subtitle generation.

---

## 7. Generating Archive for TestFlight / App Store

1. In Xcode, select **Any iOS Device (arm64)** as the build destination.
2. Select **Product > Archive**.
3. Once the archive succeeds, open the **Organizer** window.
4. Click **Distribute App** and choose **App Store Connect** for TestFlight or Direct Distribution.
