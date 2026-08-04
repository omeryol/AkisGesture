# Akış Gesture - Project Guidelines & Release Standard

## Keystore & Signing Security
- Keystore credentials (passwords, alias) MUST NEVER be hardcoded into `app/build.gradle.kts` or any file tracked by Git.
- Keystore credentials must be stored in `keystore.properties` (which is included in `.gitignore`) and loaded dynamically at build time.

## GitHub Release Specification Standard
All GitHub Releases for `AkisGesture` MUST follow this exact structure:

1. **Release Title Standard**:
   `Akış Gesture vX.Y.Z` (e.g., `Akış Gesture v1.2.0`)

2. **Release Notes Body Standard**:
   ```markdown
   ## 🇹🇷 Türkçe

   [Türkçe detaylı yenilikler ve geliştirme özeti]

   ---

   ## 🇬🇧 English

   [English detailed feature highlights & changelog summary]

   ---

   ## 📦 İndirme / Downloads
   - **İmzalı APK (Signed APK)**: `AkisGesture-vX.Y.Z.apk`
   ```

3. **Versioning & Tagging**:
   - Always update `versionCode` and `versionName` in `app/build.gradle.kts`.
   - Update `CHANGELOG.md`, `README.md` (Turkish), and `README-en.md` (English).
   - Create signed APK (`gradlew assembleRelease`) and upload asset using `gh release create`.
