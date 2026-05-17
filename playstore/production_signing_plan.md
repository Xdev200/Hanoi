# Architectural Plan & Specification: Production AAB Signing

## STEP 1 — Requirement Analysis
1. **Problem Definition**: The developer needs to upload a production-ready, cryptographically signed build of the **Hanoi** app to the Google Play Store. The standard format required by the Play Console for new apps is an **Android App Bundle (.aab)**.
2. **Scope**: Set up the secure release signing configurations, generate a keystore, edit the app gradle configurations, compile the signed AAB, and verify its validity.
3. **Functional Requirements**:
   - Generate a valid `.jks` (Java Keystore) file.
   - Configure Gradle `signingConfigs` and `buildTypes.release` blocks.
   - Generate a signed `.aab` asset under `/app/build/outputs/bundle/release/app-release.aab`.
4. **Non-Functional Requirements**:
   - **Security**: Decouple key passphrases where possible or provide clean, secure release definitions so that production credentials do not leak in unsecure environments.
   - **Size Optimization**: Configure compiler optimization (minification, resource shrinking, Proguard) to keep download size minimal.
5. **Technical Constraints**:
   - Must use JDK 17 for key generation and build execution.
   - Keystore must have at least 25 years of validity (Play Console requirement).

---

## STEP 2 — System Architecture Design (AAB Build Pipeline)
The compilation and assembly flow maps as follows:
```
┌────────────────────────┐
│     Kotlin Source      │
└───────────┬────────────┘
            │
            ▼
┌────────────────────────┐
│   Gradle Compilation   │ ◄─── Inject app/hanoi-release.jks
└───────────┬────────────┘
            │
            ▼
┌────────────────────────┐
│     Proguard / R8      │ (Shrinks and obfuscates bytecode)
└───────────┬────────────┘
            │
            ▼
┌────────────────────────┐
│   BundleAssemblyTask   │
└───────────┬────────────┘
            │
            ▼
┌────────────────────────┐
│    Signed Release AAB  │
└────────────────────────┘
```

---

## STEP 3 — Technology & Stack Validation
- **Asset Format**: **AAB (Android App Bundle)**.
  - *Justification*: Required by Google Play Console since August 2021. It generates optimized device-specific APK configurations on Google servers dynamically.
- **Key Algorithm**: `RSA` with a keysize of `2048` bits and signature algorithm `SHA256withRSA` (standard Android security protocol).

---

## STEP 4 — Design Patterns
- **Decoupled Build Parameters**: In a continuous integration (CI) pipeline, hardcoding keystore passwords in code is a critical vulnerability. We will build standard Gradle properties integration so that passwords can be loaded from local environment properties (`local.properties`) or command-line parameters, falling back to a default configuration so that developers can compile locally in one click.

---

## STEP 5 — Code Structure Planning
- **Keystore Target**: `app/hanoi-release.jks`
- **Gradle File**: `app/build.gradle.kts`
- **Output Bundle**: `app/build/outputs/bundle/release/app-release.aab`

---

## STEP 6 — Reusable Component Strategy
The `signingConfigs` block is defined once, and reusable across both the `release` and `staging` build types.

---

## STEP 7 — UI/UX Design Guide
Not applicable for compiling compilation bundles, but we must verify that adaptive icons (`ic_launcher.xml`) and the correct launcher splash screen assets are correctly aligned under the `res/` directories.

---

## STEP 8 — UI Screen Design
Not applicable.

---

## STEP 9 — API & Data Model Design (Bundle Settings)
- **Application ID**: `com.hanoi.binaryhanoi`
- **Version Code**: `1`
- **Version Name**: `"1.0.0"`
- **Target SDK**: `35` (Android 15)
- **Min SDK**: `26` (Android 8.0)

---

## STEP 10 — Testing & Verification Strategy
1. **Keystore Integrity Check**: Confirm keystore exists and aliases match.
2. **Build Success Verification**: Execute `./gradlew bundleRelease` and assert exit code is `0`.
3. **Signature Validation**: Use JDK `jarsigner` to inspect the bundle's signatures and certify valid certificate chain properties.

---

## STEP 11 — Implementation Roadmap
1. **Milestone 2.1**: Generate keystore `app/hanoi-release.jks`.
2. **Milestone 2.2**: Refactor `app/build.gradle.kts` to implement signing configs.
3. **Milestone 2.3**: Execute bundle task via Gradle wrapper.
4. **Milestone 2.4**: Run signature validation checks.
5. **Milestone 2.5**: Document release files and handoff parameters.
