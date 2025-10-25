# Quick Setup Guide - Secure App Signing

## ✅ What You've Already Done
- [x] Created keystore: `~/.android/keystores/cribbage-release-key.jks`
- [x] Paid $25 Play Store developer fee
- [x] Created custom cribbage app icon

## 🔐 Step 1: Set Up Secure Environment Variables

Run this script to set up your passwords securely:

```fish
./setup_fish_secrets.fish
```

**What this does**:
- Creates `~/.config/fish/secrets.fish` with your passwords (NOT in git)
- Updates `~/.config/fish/config.fish` with safe config (OK to commit)
- Adds `secrets.fish` to `.gitignore`
- Sets file permissions to 600

**Then reload your Fish config**:
```fish
source ~/.config/fish/config.fish
```

### Verify Setup
```fish
for var in CRIBBAGE_KEYSTORE_PATH CRIBBAGE_KEYSTORE_PASSWORD CRIBBAGE_KEY_ALIAS CRIBBAGE_KEY_PASSWORD
    if set -q $var
        echo "✅ $var is set"
    else
        echo "❌ $var is NOT set"
    end
end
```

---

## 🔧 Step 2: Update build.gradle

Add this signing configuration to `app/build.gradle`:

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
            minifyEnabled = true  // Enable code shrinking
            shrinkResources = true  // Remove unused resources
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

---

## 🏗️ Step 3: Build Release AAB

```fish
# Clean previous builds
./gradlew clean

# Build signed release bundle
./gradlew bundleRelease
```

**Output location**:
```
app/build/outputs/bundle/release/app-release.aab
```

---

## 📸 Step 4: Create Play Store Assets

### A. Screenshots (Required)
1. Install your app on a device or emulator
2. Capture at least 2 screenshots showing:
   - Main gameplay screen
   - Scoring screen
   - Game board with cards

**Use Android Studio**:
- Device Manager → Running device → Camera icon
- Or: `adb shell screencap -p /sdcard/screenshot.png`

### B. Feature Graphic (Required)
- **Size**: 1024 x 500 pixels
- **Format**: JPG or PNG
- **Tool**: Use Canva, Photoshop, or GIMP
- **Content**: Show your app icon + "Cribbage" text + tagline

### C. App Icon (Already Done!)
- ✅ You have `app_icon_512.png` ready to upload

---

## 📝 Step 5: Create Privacy Policy

Your app uses PerimeterX SDK, so you MUST have a privacy policy.

**Quick option**: Use a generator
- https://app-privacy-policy-generator.firebaseapp.com/
- https://www.privacypolicies.com/

**What to include**:
- App name and developer info
- What data is collected (analytics, crash reports)
- How data is used
- Third-party services (PerimeterX SDK)
- Contact information

**Host it**:
- GitHub Pages (free)
- Your personal website
- Any publicly accessible URL

---

## 🚀 Step 6: Play Console Setup

### A. Go to Play Console
https://play.google.com/console

### B. Create App
1. Click "Create app"
2. Fill in:
   - **App name**: Cribbage
   - **Language**: English (United States)
   - **Type**: Game
   - **Free or paid**: Free

### C. Complete Store Listing
Navigate to **Store listing** and fill in:

- **Short description** (80 chars):
  ```
  Classic cribbage card game with beautiful UI and strategic gameplay
  ```

- **Full description** (up to 4000 chars):
  ```
  Experience the classic card game of cribbage on your Android device!

  Features:
  • Classic cribbage rules
  • Beautiful, modern interface
  • Hand scoring with detailed breakdown
  • Pegging mechanics
  • Clean, intuitive design

  Perfect for both beginners learning cribbage and experienced players
  looking for a digital version of this timeless card game.
  ```

- **App icon**: Upload `app_icon_512.png`
- **Feature graphic**: Upload your 1024x500 graphic
- **Screenshots**: Upload at least 2 phone screenshots
- **Category**: Games → Card
- **Email**: Your support email
- **Privacy policy**: Your hosted privacy policy URL

### D. Content Rating
1. Navigate to **Content rating**
2. Complete questionnaire
3. For cribbage: Likely "Everyone" rating

### E. Data Safety
1. Navigate to **Data safety**
2. Declare what data you collect
3. Check PerimeterX SDK documentation for what it collects

### F. Upload Release
1. Navigate to **Testing → Internal testing** (or Closed/Production)
2. Click "Create new release"
3. Upload your AAB file
4. Add release notes
5. Click "Review" → "Start rollout"

---

## 📋 Pre-Launch Checklist

Before submitting to Play Store:

### Code
- [ ] All tests pass: `./gradlew test`
- [ ] Lint check passes: `./gradlew lint`
- [ ] Release build succeeds: `./gradlew bundleRelease`
- [ ] Version code/name updated in build.gradle

### Security
- [ ] Keystore backed up to safe location
- [ ] Environment variables set up correctly
- [ ] No hardcoded secrets in code
- [ ] .gitignore includes *.jks and keystore files

### Assets
- [ ] App icon (512x512 PNG) ✅
- [ ] Feature graphic (1024x500)
- [ ] At least 2 screenshots
- [ ] Privacy policy created and hosted

### Play Console
- [ ] Store listing completed
- [ ] Content rating completed
- [ ] Data safety section completed
- [ ] Privacy policy URL added
- [ ] Contact email provided

---

## 🔧 Troubleshooting

### "Environment variable not set"
```fish
# Check variables
env | grep CRIBBAGE

# Reload config
source ~/.config/fish/config.fish

# Verify secrets file exists
ls -l ~/.config/fish/secrets.fish
```

### "Wrong password"
```fish
# Re-run setup script
./setup_fish_secrets.fish
```

### "Build fails with signing error"
```fish
# Verify keystore exists
ls -l ~/.android/keystores/cribbage-release-key.jks

# Check keystore info (will prompt for password)
keytool -list -v -keystore ~/.android/keystores/cribbage-release-key.jks
```

---

## 📚 Additional Resources

- **Full Publishing Guide**: `PLAY_STORE_PUBLISHING_GUIDE.md`
- **Fish Shell Setup**: `FISH_SHELL_SETUP.md`
- **Icon Details**: `ICON_CREATION_SUMMARY.md`

---

## 🎯 Quick Command Reference

```fish
# Build release AAB
./gradlew bundleRelease

# Build release APK (for testing)
./gradlew assembleRelease

# Install release build locally
adb install app/build/outputs/apk/release/app-release.apk

# Run tests
./gradlew test

# Check keystore
keytool -list -v -keystore ~/.android/keystores/cribbage-release-key.jks

# Reload Fish config
source ~/.config/fish/config.fish
```

---

## ⏱️ Estimated Timeline

- **Step 1** (Environment setup): 5 minutes
- **Step 2** (build.gradle update): 5 minutes
- **Step 3** (Build AAB): 2 minutes
- **Step 4** (Screenshots): 30 minutes
- **Step 5** (Privacy policy): 20 minutes
- **Step 6** (Play Console): 1-2 hours
- **Google Review**: 1-3 days

**Total active time**: ~3 hours + review time

---

## 🎉 You're Almost There!

Once you complete these steps, your app will be submitted for review. Google typically reviews apps within 1-3 days. Good luck with your launch! 🚀
