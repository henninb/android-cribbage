# Google Play Store Publishing Guide - Cribbage App

## Overview
This guide outlines all steps required to publish your Android Cribbage app to the Google Play Store.

**Status**: Play Store Developer Account ✓ (Paid $25 fee)

---

## 1. App Icon Creation

### Requirements
- **512x512 PNG**: High-resolution icon for Play Store listing (32-bit PNG, no transparency)
- **Adaptive Icons**: Already present in your app (mipmap folders)

### Current Status
✓ You have default Android launcher icons in all required densities
✗ Need custom cribbage-themed icon

### Action Items
- [ ] Design or commission a cribbage-themed icon (512x512 PNG)
  - Should feature recognizable cribbage elements (cards, pegs, board)
  - Must be clear and recognizable at small sizes
  - Follow Material Design guidelines
  - No transparency for Play Store asset (512x512)

- [ ] Generate adaptive icon variants
  - Use Android Studio: **Right-click app → New → Image Asset**
  - Import your 512x512 design
  - Generate all densities automatically
  - Creates both foreground and background layers

### Resources
- **Android Asset Studio**: https://romannurik.github.io/AndroidAssetStudio/
- **Material Design Icons**: https://material.io/design/iconography

---

## 2. App Signing Configuration

### Why It Matters
Google Play requires all apps to be signed with a release key. You'll need to:
1. Create a keystore (one-time setup)
2. Keep it secure (NEVER commit to git)
3. Configure signing in build.gradle

### Step-by-Step Keystore Creation

#### A. Generate Keystore
```bash
keytool -genkey -v -keystore ~/cribbage-release-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias cribbage-release
```

**Important Prompts**:
- **Password**: Choose a strong password (save it securely!)
- **Name**: Your name or company name
- **Organization**: Personal or company name
- **Location**: Your city
- **State/Province**: Your state
- **Country Code**: Two-letter code (e.g., US)

#### B. Secure Storage
```bash
# Move keystore to secure location (NOT in project directory)
mv ~/cribbage-release-key.jks ~/.android/keystores/cribbage-release-key.jks

# Set restrictive permissions
chmod 600 ~/.android/keystores/cribbage-release-key.jks
```

**CRITICAL**:
- ⚠️ NEVER commit keystore to version control
- ⚠️ BACKUP keystore to secure location (cloud, external drive)
- ⚠️ If lost, you CANNOT update your app (must publish new app)

#### C. Configure Signing in build.gradle

Add to `app/build.gradle`:

```gradle
android {
    // ... existing config ...

    signingConfigs {
        release {
            storeFile file(System.getenv("CRIBBAGE_KEYSTORE_PATH") ?: "${System.properties['user.home']}/.android/keystores/cribbage-release-key.jks")
            storePassword System.getenv("CRIBBAGE_KEYSTORE_PASSWORD")
            keyAlias System.getenv("CRIBBAGE_KEY_ALIAS") ?: "cribbage-release"
            keyPassword System.getenv("CRIBBAGE_KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled = true  // Enable for release!
            shrinkResources = true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

#### D. Set Environment Variables

Add to your `~/.bashrc` or `~/.zshrc`:

```bash
export CRIBBAGE_KEYSTORE_PATH="$HOME/.android/keystores/cribbage-release-key.jks"
export CRIBBAGE_KEYSTORE_PASSWORD="your_keystore_password"
export CRIBBAGE_KEY_ALIAS="cribbage-release"
export CRIBBAGE_KEY_PASSWORD="your_key_password"
```

Then reload: `source ~/.bashrc` (or `~/.zshrc`)

---

## 3. Build Configuration Updates

### A. Update Version Information

In `app/build.gradle`, update:

```gradle
defaultConfig {
    applicationId = "com.brianhenning.cribbage"
    versionCode = 1          // Increment for each release (1, 2, 3...)
    versionName = "1.0.0"    // User-facing version (1.0.0, 1.0.1, etc.)
    // ...
}
```

**Version Strategy**:
- `versionCode`: Integer that MUST increase with each release
- `versionName`: Semantic versioning (MAJOR.MINOR.PATCH)

### B. Enable Code Optimization

Update `buildTypes` in `app/build.gradle`:

```gradle
buildTypes {
    release {
        minifyEnabled = true        // Enable ProGuard/R8
        shrinkResources = true      // Remove unused resources
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
    }
}
```

### C. ProGuard Rules (if needed)

Check `app/proguard-rules.pro` and add rules if you encounter issues:

```proguard
# Keep Gson/Retrofit models
-keep class com.brianhenning.cribbage.models.** { *; }

# Keep Compose
-keep class androidx.compose.** { *; }

# Keep your API interfaces
-keep interface com.brianhenning.cribbage.** { *; }
```

### D. Update Target SDK

**Current**: targetSdk = 34
**Recommendation**: Update to 35 (Play Store will eventually require it)

```gradle
defaultConfig {
    targetSdk = 35  // Update from 34
}
```

---

## 4. Security & Privacy Compliance

### A. Remove Debug Code

Search for and remove/disable:
- `Log.d()`, `Log.v()` statements
- `println()` debug statements
- Test API endpoints
- Hardcoded credentials or API keys

```bash
# Search for debug logs
grep -r "Log\." app/src/main/java/
grep -r "println" app/src/main/java/
```

### B. Secure API Keys

If you have API keys:
- Use BuildConfig fields
- Store in `local.properties` (not committed)
- Or use environment variables

Example in `build.gradle`:

```gradle
buildTypes {
    release {
        buildConfigField "String", "API_KEY", "\"${System.getenv('API_KEY')}\""
    }
}
```

### C. Permissions Review

Current permissions in AndroidManifest.xml:
- ✓ `INTERNET` - Required for network features
- ✓ `ACCESS_NETWORK_STATE` - Required for network status

**Action**: Verify these are actually needed for your app's functionality.

### D. Privacy Policy

**Required if**:
- You collect user data
- You use third-party SDKs that collect data
- You have in-app purchases
- You display ads

**Your app**:
- Uses `com.perimeterx.sdk` (may collect data)
- Has network permissions

**Action**:
- [x] Create a privacy policy - ✓ See PRIVACY_POLICY.md
- [x] Update contact email in PRIVACY_POLICY.md - ✓ henninb@gmail.com
- [x] Host it on a publicly accessible URL - ✓ GitHub Pages enabled
- [ ] Push updated privacy policy to GitHub (PENDING - removes PerimeterX)
- [ ] Include in Play Store listing

**Privacy Policy URL**: https://henninb.github.io/android-cribbage/privacy-policy

**Hosting Options** (COMPLETED - using GitHub Pages):
1. **GitHub Pages** (Recommended - Free):
   - Create a `docs` folder in your repo
   - Copy PRIVACY_POLICY.md to docs/privacy-policy.md
   - Enable GitHub Pages in repo settings
   - URL will be: https://henninb.github.io/android-cribbage/privacy-policy.html

2. **GitHub Gist**:
   - Create a public gist with PRIVACY_POLICY.md
   - Use the raw URL (e.g., https://gist.githubusercontent.com/...)

3. **Personal Website**:
   - Host on your own domain if you have one

4. **Free hosting services**:
   - Netlify, Vercel, or similar

---

## 5. Play Store Assets Preparation

### Required Assets

#### A. App Icon (High-res)
- **512 x 512 pixels**
- **32-bit PNG** (no transparency)
- Must match your app's launcher icon

#### B. Feature Graphic
- **1024 x 500 pixels**
- **JPG or 24-bit PNG** (no transparency)
- Showcases your app (cards, cribbage board, gameplay)

#### C. Screenshots (Required)
**Phone** (minimum 2, maximum 8):
- At least **2** screenshots
- **Dimensions**:
  - 16:9 aspect ratio (e.g., 1920x1080, 1280x720)
  - OR 9:16 portrait (e.g., 1080x1920)
- **Format**: PNG or JPG, 24-bit
- **Max size**: 8MB each

**Tablet** (optional but recommended):
- 7" and 10" tablet screenshots
- Same requirements as phone

#### D. Video (Optional)
- YouTube video URL
- Showcases gameplay

### Screenshot Tips
- Use Android Studio's emulator to capture screens
- Show key features (game board, scoring, pegging)
- Add text overlays highlighting features
- Show actual gameplay, not just splash screen

### Tools for Asset Creation
- **Feature Graphic**: Canva, Photoshop, GIMP
- **Screenshots**: Android Studio Device Manager
- **Icon Design**: Adobe Illustrator, Figma, Inkscape

---

## 6. Build Release APK/AAB

### A. Build App Bundle (Recommended)

Google Play prefers Android App Bundles (.aab):

```bash
# Clean previous builds
./gradlew clean

# Build release bundle
./gradlew bundleRelease

# Output location:
# app/build/outputs/bundle/release/app-release.aab
```

### B. Alternative: Build APK

```bash
# Build release APK
./gradlew assembleRelease

# Output location:
# app/build/outputs/apk/release/app-release.apk
```

### C. Test Release Build Locally

```bash
# Install release APK on device (for testing)
adb install app/build/outputs/apk/release/app-release.apk

# Or use bundletool to test AAB
bundletool build-apks --bundle=app/build/outputs/bundle/release/app-release.aab \
  --output=app-release.apks \
  --ks=~/.android/keystores/cribbage-release-key.jks \
  --ks-key-alias=cribbage-release

bundletool install-apks --apks=app-release.apks
```

---

## 7. Play Console Setup

### A. Create App in Play Console

1. Go to https://play.google.com/console
2. Click **Create app**
3. Fill in:
   - **App name**: "Cribbage" (or your preferred name)
   - **Default language**: English (United States)
   - **App or game**: Game
   - **Free or paid**: Free
4. Accept declarations and create

### B. Complete Store Listing

Navigate to **Store listing** in left sidebar:

1. **App details**:
   - **App name**: Cribbage
   - **Short description**: (80 chars max)
     - "Classic cribbage card game with beautiful UI and scoring"
   - **Full description**: (4000 chars max)
     - Describe gameplay, features, rules
     - Mention hand scoring, pegging, multiple game modes

2. **App icon**: Upload 512x512 PNG

3. **Feature graphic**: Upload 1024x500 graphic

4. **Screenshots**:
   - Upload phone screenshots (minimum 2)
   - Upload tablet screenshots (optional)

5. **App categorization**:
   - **App category**: Games → Card
   - **Tags**: Card game, Classic games

6. **Contact details**:
   - **Email**: Your support email
   - **Phone** (optional): Support phone
   - **Website** (optional): App website

7. **Privacy policy**: URL to your privacy policy (REQUIRED)

### C. Content Rating

1. Navigate to **Content rating**
2. Fill out questionnaire
3. For a cribbage game:
   - No violence, sexual content, drugs, etc.
   - Likely rating: **Everyone** or **Everyone 10+**

### D. App Access

1. Navigate to **App access**
2. If app has no restricted features: Select "All functionality is available"
3. If you have login/special features: Provide test credentials

### E. Ads

1. Navigate to **Ads**
2. Declare if your app contains ads
3. For your app: Likely "No" (unless you added ads)

### F. Data Safety

1. Navigate to **Data safety**
2. Complete questionnaire about:
   - What data you collect
   - How it's used
   - Security practices
3. For cribbage app with no user accounts:
   - Likely collecting: App interactions, Crash logs
   - Check what PerimeterX SDK collects

### G. App Content

Complete all sections:
- News apps (N/A)
- COVID-19 contact tracing (N/A)
- Data safety
- Government apps (N/A)

---

## 8. Release Management

### A. Choose Release Track

Options:
1. **Internal testing**: Limited to 100 testers (fast approval)
2. **Closed testing**: Limited to email list (moderate approval)
3. **Open testing**: Anyone can join (moderate approval)
4. **Production**: Public release (full review)

**Recommendation**: Start with **Closed testing** or **Internal testing**

### B. Create Release

1. Navigate to **Testing → Closed testing** (or Internal)
2. Click **Create new release**
3. Upload AAB/APK:
   - Drag and drop `app-release.aab`
   - Google Play will analyze (may take a few minutes)
4. **Release name**: e.g., "1.0.0 - Initial Release"
5. **Release notes**:
   ```
   Initial release features:
   - Classic cribbage gameplay
   - Hand scoring
   - Pegging mechanics
   - Clean, modern UI
   ```
6. Click **Save** → **Review release** → **Start rollout**

### C. Review & Publishing

- **Review time**: Typically 1-3 days
- **Status**: Check in Play Console dashboard
- **Issues**: Google will notify you of any policy violations

---

## 9. Pre-Launch Checklist

Before submitting, verify:

### Code Quality
- [ ] All tests pass: `./gradlew test`
- [ ] No lint errors (critical): `./gradlew lint`
- [ ] ProGuard/R8 build succeeds
- [ ] Release build installs and runs on device

### Security
- [ ] Keystore created and secured
- [ ] Keystore backed up to safe location
- [ ] No hardcoded API keys or secrets
- [ ] Debug logs removed/disabled
- [ ] Permissions minimized and justified

### Assets
- [ ] Custom cribbage icon created (512x512)
- [ ] Adaptive icons generated for all densities
- [ ] Feature graphic created (1024x500)
- [ ] At least 2 phone screenshots
- [ ] Privacy policy created and hosted

### App Configuration
- [ ] Version code/name updated
- [ ] Target SDK is 34+ (ideally 35)
- [ ] Release signing configured
- [ ] ProGuard enabled for release builds
- [ ] App builds successfully: `./gradlew bundleRelease`

### Play Store
- [ ] Store listing completed
- [ ] Content rating questionnaire completed
- [ ] Data safety section completed
- [ ] Privacy policy URL added
- [ ] Contact email provided
- [ ] Release notes written

---

## 10. Post-Launch

### Monitoring
- Monitor crash reports in Play Console
- Respond to user reviews
- Track installation metrics

### Updates
- Increment `versionCode` for each update
- Update `versionName` following semantic versioning
- Provide detailed release notes
- Test thoroughly before each release

### Promotion
- Share on social media
- Request reviews from testers
- Respond to user feedback

---

## Useful Commands Reference

```bash
# Build release AAB
./gradlew clean bundleRelease

# Build release APK
./gradlew assembleRelease

# Run tests
./gradlew test

# Run lint checks
./gradlew lint

# Install release build locally
adb install app/build/outputs/apk/release/app-release.apk

# Check signing info
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk

# Generate keystore
keytool -genkey -v -keystore ~/.android/keystores/cribbage-release-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 -alias cribbage-release
```

---

## Resources

- **Play Console**: https://play.google.com/console
- **Android Publishing Guide**: https://developer.android.com/studio/publish
- **App Signing**: https://developer.android.com/studio/publish/app-signing
- **Asset Guidelines**: https://support.google.com/googleplay/android-developer/answer/9866151
- **Policy Guidelines**: https://play.google.com/about/developer-content-policy/

---

## Timeline Estimate

- **Icon creation**: 2-4 hours
- **Signing setup**: 30 minutes
- **Build configuration**: 1 hour
- **Assets creation**: 3-5 hours
- **Privacy policy**: 1 hour
- **Play Console setup**: 2-3 hours
- **Testing release build**: 1-2 hours
- **Google review**: 1-3 days

**Total**: ~2-3 days of work + Google review time

---

## Next Steps

1. **Immediate**:
   - Generate keystore
   - Configure signing in build.gradle
   - Test release build

2. **Design**:
   - Create cribbage-themed icon
   - Generate adaptive icons
   - Create feature graphic
   - Capture screenshots

3. **Compliance**:
   - Write privacy policy
   - Host privacy policy online
   - Review data collection practices

4. **Launch**:
   - Complete Play Console setup
   - Upload first release to testing track
   - Gather feedback
   - Promote to production

Good luck with your launch! 🎉
