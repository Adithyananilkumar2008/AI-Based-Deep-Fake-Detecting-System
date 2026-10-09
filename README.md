# 🛡️ AI-Based Deep Fake Detecting System

An Android app that helps you check whether an **image, video, or audio file** is real or AI-generated / manipulated. It combines classic digital-forensics techniques with AI analysis and shows the result as a clear **forensic score** and report.

---

## 📌 Overview

Deepfakes and AI-generated media are getting harder to spot with the naked eye. This project puts a deepfake detector in your pocket: pick a file, run the scan, and get an easy-to-read verdict backed by forensic evidence.

---

## ✨ Features

- 🔍 **Media analysis** – scan images, video and audio for signs of manipulation
- 🧪 **Forensic Workbench** – detailed breakdown of the techniques used to reach the verdict
- 📊 **Forensic Score** – a single score showing how likely the file is Authentic or Manipulated
- 🤖 **AI Generator Signature Detection** – flags traces left by AI image/video generators
- 🗂️ **Scan History** – every scan is saved so you can review it later
- 📄 **Forensic Report** – generate a "Deepfake Detector Forensic Report" for a scan
- 👤 **User accounts** – sign in with Firebase Authentication (Google sign-in supported)
- 🔐 **Biometric lock** – fingerprint / face unlock support
- ☁️ **Cloud sync** – scan records are synced to Cloud Firestore
- 🌙 Modern UI built with Jetpack Compose and Material 3

---

## 🔬 Detection Techniques

| Technique | What it checks |
|---|---|
| **Error Level Analysis (ELA)** | Finds areas of an image that were edited or re-compressed |
| **Temporal ELA Inspection** | Applies ELA frame by frame to detect tampering in video |
| **EXIF & Parameter Metadata** | Looks for missing, edited or suspicious metadata and generator parameters |
| **PRNU Sensor Noise Fingerprinting** | Checks whether the noise pattern matches a real camera sensor |
| **Sensor Noise Uniformity** | Detects unnatural noise patterns typical of synthetic images |
| **Audio Noise Floor** | Detects unnaturally clean or artificial background noise in audio |
| **High-Frequency Cutoff** | Spots frequency limits that are common in synthetic / cloned voices |
| **AI Generator Signature** | Detects known fingerprints left by AI generation tools |

---

## 🛠️ Tech Stack

| Area | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Local database | Room |
| Authentication | Firebase Authentication (Google sign-in, biometric) |
| Cloud database | Cloud Firestore |
| AI | Firebase AI (Gemini) |
| Image loading | Coil |
| Min / Target SDK | Android 7.0 (API 24) / API 36 |

---

## 📱 App Screens

- **Login / Sign up**
- **Analysis** – choose a file and run a scan
- **Forensic Workbench** – see detailed results
- **History** – browse and re-scan previous files

> 📷 Add your screenshots here, for example:
> `![Home](screenshots/home.png)`

---

## 🚀 Getting Started

### Install the APK (easiest)
1. Download the `.apk` file from the **Releases** section of this repository.
2. On your Android phone, allow **Install from unknown sources** when asked.
3. Open the APK and tap **Install**.
4. Launch **Deepfake Detector** and sign in.

### Build from source
1. Install [Android Studio](https://developer.android.com/studio) (free).
2. Clone this repository:
   ```bash
   git clone https://github.com/Adithyananilkumar2008/AI-Based-Deep-Fake-Detecting-System.git
   ```
3. Open the project in Android Studio and let Gradle sync.
4. Add your own Firebase config file (`google-services.json`) to the `app/` folder.
5. Click **Run ▶** to start the app on an emulator or a connected phone.

---

## 📂 Project Structure (overview)

```
app/
 ├── data/        # Room database and scan records
 ├── ui/          # Screens, auth view model, theme
 └── MainActivity # App entry point with tabs
```

---

## ⚠️ Disclaimer

This is an academic prototype. Detection results are **estimates, not legal proof**. No detector is 100% accurate, so always verify important media using multiple sources.

---

## 🔮 Future Improvements

- Dedicated trained deep-learning model running on-device
- Real-time camera / live-call deepfake detection
- Support for more file formats
- Downloadable PDF reports
- iOS version

---

## 👨‍💻 Author

**Adithyan A B**
GitHub: [@Adithyananilkumar2008](https://github.com/Adithyananilkumar2008)
