# Encryption Demo App 🔐

A comprehensive Android application demonstrating modern cryptographic patterns, performance benchmarks, and hardware-backed security using Jetpack Compose and Google Tink.

## 🚀 Project Overview

The **Encryption Demo App** is designed to provide developers with a clear, hands-on exploration of the various ways to implement security in Android. It covers everything from low-level Java APIs to high-level audited libraries and hardware-isolated key storage.

## ✨ Features

### 1. Manual KeyGenerator (JCA)
Explore the standard **Java Cryptography Architecture (JCA)**.
- **Symmetric**: AES (GCM, CBC, CTR, ECB) and 3DES.
- **Asymmetric**: RSA-OAEP encryption and decryption.
- **Dynamic Config**: Manually choose block modes, padding schemes, and key sizes.
- **Educational**: Integrated help dialogs explaining algorithms and security trade-offs.

### 2. Android Keystore System
Demonstrates **hardware-backed security** (TEE and StrongBox).
- **Extraction Prevention**: Keys are generated and stored in secure hardware; raw key material never enters the app's memory.
- **Biometric Ready**: Logic for user-authentication requirements and hardware enforcement.
- **Hardware Status**: Real-time detection and display of "Hardware Backed: YES/NO" status.
- **StrongBox**: Support for dedicated security chips on modern devices (Android 9+).

### 3. Google Tink
Showcases Google's modern, safe, and audited cryptographic library.
- **Symmetric (AEAD)**: Uses authenticated encryption with templates like `AES256_GCM`.
- **Hybrid (Asymmetric)**: Full ECIES and DHKEM_AES workflows.
- **Isolated Simulation**: A unique 4-step simulation of a Server-to-Client key handshake:
    1. Server generates Public/Private pair.
    2. Client receives and "Imports" the key into a local vault.
    3. Client encrypts using the local vault.
    4. Server decrypts using its private vault.
- **Production-Grade**: Implements `AndroidKeysetManager` for persistence and **Master Key Wrapping** via the Android Keystore.

### 4. Performance Benchmarking -- Coming Soon!
A high-precision timing engine to compare implementation overhead.
- **Three-Way Comparison**: Side-by-side results for Manual JCA, Keystore, and Tink.
- **Metrics**: Captures encryption and decryption times in milliseconds using `System.nanoTime()`.
- **Hardware Detection**: Explicitly identifies if a benchmarked operation used secure hardware.
- **Additive Results**: Run multiple configurations and see them added to a comparative table.

## 🏗️ Architecture

The project follows a clean, decoupled architecture:

- **UI Layer**: Built with **Jetpack Compose**, **Material 3**, and the experimental **Navigation 3**.
- **State Layer**: **ViewModels** coordinate UI state and user interactions.
- **Service Layer**: Core logic is extracted into standalone, reusable services (`JcaService`, `KeystoreService`, `TinkService`) to ensure consistency between demo screens and benchmarks.
- **Dependency Injection**: Powered by **Koin**, allowing for clean service sharing and testability.

## 🛠️ Technical Stack

- **Language**: Kotlin 2.x
- **UI**: Jetpack Compose (Material 3)
- **Navigation**: Navigation 3 (State-based)
- **DI**: Koin 4.2.1
- **Security**: Google Tink 1.21.0, Android Keystore (JCA)
- **Serialization**: KotlinX Serialization

## 📦 Setup & Installation

1.  Clone the repository.
2.  Open in **Android Studio Ladybug (or newer)**.
3.  Ensure you have **JDK 11** or newer configured.
4.  Sync Gradle and run the `app` module.

> [!NOTE]
> To test **StrongBox** or **Biometric** features, it is recommended to run the app on a physical device (e.g., Google Pixel 3+) or a properly configured emulator.

## 📜 License

This project is open-sourced under the MIT License.
