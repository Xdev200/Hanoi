# Production Release & Security Handoff Guide

This document contains key parameters, credential indices, and step-by-step instructions for deploying the **Hanoi** app bundle to the Google Play Store.

> [!IMPORTANT]
> Keep your keystore and passwords highly secure. Do not share your `.jks` file publicly or upload it to open version-control systems.

---

## 🔑 Release Signing Key Information

These details are integrated into the Gradle build configurations at [app/build.gradle.kts](file:///Users/madhusudandas/Desktop/AI_APPS/Hanoi/app/build.gradle.kts) and are required to compile and sign release packages:

| Parameter | Configuration Value |
| :--- | :--- |
| **Keystore File** | `app/hanoi-release.jks` |
| **Key Alias** | `hanoi-alias` |
| **Keystore Password** | `hanoipuzzle123` |
| **Key Password** | `hanoipuzzle123` |
| **Encryption standard** | RSA (2048-bit) |
| **Validity duration** | 10,000 Days (~27 Years) |

---

## 📂 Compiled Artifact Location

The highly optimized, compiled, and cryptographically signed Android App Bundle (AAB) is located at:
*   [app/build/outputs/bundle/release/app-release.aab](file:///Users/madhusudandas/Desktop/AI_APPS/Hanoi/app/build/outputs/bundle/release/app-release.aab)
*   **File Size**: `2.3 MB`

---

## 🚀 Step-by-Step Play Console Upload Guide

1.  **Log in**: Access the [Google Play Console](https://play.google.com/console/).
2.  **App Selection**: Select your app **Hanoi** from the dashboard.
3.  **Initiate Release**:
    *   Navigate to **Release** > **Production** (or **Internal testing** if you wish to run a private trial first).
    *   Click the **Create new release** button in the top right.
4.  **Upload App Bundle**:
    *   Locate the compiled [app-release.aab](file:///Users/madhusudandas/Desktop/AI_APPS/Hanoi/app/build/outputs/bundle/release/app-release.aab) on your Desktop/Disk.
    *   Drag and drop the `.aab` file into the upload box under the **App bundles** section.
5.  **Configure Metadata**:
    *   The Console will automatically parse the `versionCode: 1` and `versionName: "1.0.0"`.
    *   Enter release notes (e.g. *"First production release of Hanoi puzzle educational game, featuring 12 interactive levels, dynamic recursion trees, and customizable wood themes."*).
6.  **Review and Launch**:
    *   Click **Next** to run automated pre-launch checks.
    *   Review any warnings (standard recommendations).
    *   Click **Save and publish** to submit your app for review!

---

## 🛠️ Diagnostics & Signature Verification

If you ever need to manually verify the cryptographic signature alignment of your AAB, run this JDK command in the terminal:
```bash
jarsigner -verify -verbose -certs app/build/outputs/bundle/release/app-release.aab
```
A valid build will print `jar verified.` at the bottom of the log.
