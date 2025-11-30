# 📚 **SmartVoice Reader**

### *Offline Text‑to‑Speech & Translation App*

> A fast, privacy‑focused mobile app that reads, translates, and saves
> text **100% offline** --- with natural, high‑quality voices.

------------------------------------------------------------------------

## 🌟 **Overview**

**SmartVoice Reader** turns any text into speech using on‑device AI
models.\
No internet. No tracking. No ads. Just fast, private, offline text
reading and translation.

------------------------------------------------------------------------

## 🚀 **Features**

### 🗣️ **Offline Text‑to‑Speech**

-   High‑quality local TTS models\
-   Reads pasted, typed, or imported text\
-   Adjustable speed & pitch (future update)

### 🌍 **Offline Translation**

-   Translate text before or after reading\
-   Works in airplane mode\
-   Multilingual (expandable)

### 🎧 **Audio Export**

-   Save generated speech as audio files\
-   Re‑listen anytime\
-   Share recordings with other apps

### 💾 **Text Library**

-   Save unlimited text notes\
-   Search, favorite, and organize content

### 🎨 **Modern UI**

-   Smooth, clean interface with Jetpack Compose\
-   Light/Dark theme\
-   Minimal, distraction‑free design

### ⚙️ **Optimized Performance**

-   Works on low‑end devices\
-   Uses lightweight TFLite/ONNX models\
-   Low battery and CPU usage

------------------------------------------------------------------------

## 🔒 **Privacy First**

-   No data leaves your device\
-   No analytics, no tracking\
-   Fully functional without Internet

------------------------------------------------------------------------

## 📱 **Use Cases**

-   Listen to articles and notes\
-   Translate while traveling offline\
-   Study hands‑free\
-   Language learning\
-   Accessibility support\
-   Convert text to audio for later playback

------------------------------------------------------------------------

## 🛠️ **Tech Stack**

-   **Language:** Kotlin\
-   **UI:** Jetpack Compose\
-   **Models:** Silero/Coqui TTS, Distilled MT models\
-   **Inference:** ONNX Runtime Mobile / TensorFlow Lite\
-   **Storage:** Room Database + local file system

------------------------------------------------------------------------

## 📦 **Project Structure**

    SmartVoiceReader/
     ├── app/
     │   ├── ui/             # Compose screens
     │   ├── data/           # Room + repositories
     │   ├── tts/            # Offline TTS engine
     │   ├── translate/      # Offline translation
     │   ├── audio/          # Playback & exporting
     │   └── utils/          # Shared helpers
     └── models/
         ├── tts_model.onnx
         └── translation_model.onnx

------------------------------------------------------------------------

## 🗺️ **Roadmap**

### 🌱 v1.0 --- MVP

-   Offline TTS\
-   Text saving\
-   Basic offline translation\
-   Audio exporting\
-   Light/Dark theme

### 🌿 v1.2 --- Improvements

-   Multiple voices\
-   Speed/pitch controls\
-   Better compression\
-   UI polish

### 🌳 v2.0 --- Advanced

-   Offline OCR (image → text)\
-   Offline summarization\
-   Background reading\
-   User voice packs

------------------------------------------------------------------------

## 🤝 **Contributing**

Pull requests, model improvements, or UI ideas are welcome!

------------------------------------------------------------------------

## 📧 **Contact**

benamaraamine39@gmail.com\
For feedback, suggestions, or bug reports.
