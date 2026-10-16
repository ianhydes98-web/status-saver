# Status Saver

**Lightweight • Super Fast • Super Reliable • Super Secure**

A clean, modern WhatsApp Status Saver built with Jetpack Compose and Clean Architecture principles.

## Features
- Automatically discovers statuses from all known WhatsApp & WhatsApp Business folders
- Clean dark-mode UI with Images / Videos tabs
- Full-screen preview for photos & videos
- One-tap save to `Pictures/WhatsApp Statuses/`
- **Auto Save** – saves statuses the moment you watch them in WhatsApp
- Zero internet permission
- No ads, no tracking, no analytics
- Highly modular – easy to add new features later

## Auto Save
Enable **Auto Save Statuses** in Settings.  
A lightweight foreground service watches the `.Statuses` folders using `FileObserver` and immediately saves any new media that appears (i.e. right after you view a status in WhatsApp).

- Works across WhatsApp & WhatsApp Business
- Survives device reboot
- Low-priority silent notification
- Extremely battery-friendly

## Tech Stack
- Kotlin
- Jetpack Compose + Material 3
- MVVM + Repository pattern
- Coil (image loading)
- Coroutines + StateFlow
- FileObserver + Foreground Service (Auto Save)
- MediaStore (modern gallery saving)

## How to Build
1. Open the project in Android Studio (Ladybug or newer recommended)
2. Sync Gradle
3. Run on a **real device** (emulator has limited access to WhatsApp folders)

## Permissions
- `MANAGE_EXTERNAL_STORAGE` (Android 11+) – required to read the hidden `.Statuses` folder
- `READ_MEDIA_IMAGES` / `READ_MEDIA_VIDEO` (Android 13+)
- `FOREGROUND_SERVICE` + `POST_NOTIFICATIONS` (for Auto Save)

## Architecture
```
ui/          → Screens + ViewModels (presentation)
data/        → Repository + Models
service/     → AutoSaveService + BootReceiver
util/        → Permission & Prefs helpers
```
Ready for future features (folders, Business support, etc.)
