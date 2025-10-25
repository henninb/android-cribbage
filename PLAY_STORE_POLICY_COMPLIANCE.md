# Google Play Store Policy Compliance Review
**App**: Cribbage
**Package**: com.brianhenning.cribbage
**Review Date**: October 25, 2025

---

## Executive Summary

**Overall Status**: ⚠️ **MOSTLY COMPLIANT** with recommended improvements

The Cribbage app is a card game application that is generally compliant with Google Play policies, but has some areas that should be addressed before submission to improve approval chances and user experience.

---

## ✅ Compliant Areas

### 1. App Content & Functionality
- **Type**: Single-player card game (Cribbage vs AI)
- **Content Rating**: Suitable for Everyone (no violence, gambling with real money, mature content)
- **Functionality**: Offline gameplay with local score tracking
- **Compliance**: ✅ Meets content policies

### 2. Privacy Policy
- **Status**: ✅ Created (PRIVACY_POLICY.md)
- **Hosted**: Pending (setup for GitHub Pages completed)
- **Content**: Properly discloses third-party SDK usage and data collection
- **Contact**: Email provided (henninb@gmail.com)

### 3. Permissions
- **INTERNET**: Declared for PerimeterX SDK
- **ACCESS_NETWORK_STATE**: Declared for network status
- **Justification**: Both justified by current SDK usage
- **Compliance**: ✅ Permissions are justified

### 4. App Signing & Security
- **Signing**: ✅ Configured with keystore
- **ProGuard/R8**: ✅ Enabled with comprehensive rules
- **Code Optimization**: ✅ minifyEnabled and shrinkResources enabled
- **Compliance**: ✅ Security best practices followed

### 5. Version Information
- **versionCode**: 1 (correct for initial release)
- **versionName**: 1.0.0 (follows semantic versioning)
- **targetSdk**: 35 (meets Play Store requirements)
- **Compliance**: ✅ Properly configured

### 6. Data Storage
- **User Data**: Game statistics stored locally in SharedPreferences
- **No Cloud Storage**: No server-side data collection by developer
- **Backup**: Default Android backup enabled
- **Compliance**: ✅ Transparent and minimal data collection

---

## ⚠️ Issues Requiring Attention

### 1. PerimeterX SDK Usage (PRIORITY: HIGH)

**Issue**: The app includes PerimeterX SDK (com.perimeterx.sdk:msdk:3.4.0), a security/fraud prevention SDK.

**Concerns**:
- PerimeterX is designed for apps that need bot/fraud protection
- Typically used by apps with user accounts, payments, or high-value transactions
- For an offline card game, this SDK appears **unnecessary**
- Adds ~1-2MB to app size and collects device/usage data
- May trigger additional scrutiny during Play Store review

**Location**:
- `app/build.gradle:61` - Dependency declaration
- `CribbageApplication.kt:15-28` - SDK initialization
- `AndroidManifest.xml:5-6` - Permissions required by SDK

**Recommendation**:
```
OPTION 1 (Recommended): Remove PerimeterX SDK
- Remove dependency from build.gradle
- Remove initialization from CribbageApplication.kt
- Update privacy policy to remove PerimeterX disclosure
- May be able to remove INTERNET permission if no other network use

OPTION 2: Keep with justification
- If you plan to add online features (multiplayer, leaderboards)
- Document the justification in Play Store description
- Ensure privacy policy clearly explains data collection
```

**Impact if not addressed**:
- May delay approval process
- Reviewers may request justification
- User concerns about unnecessary data collection

---

### 2. Debug Logging in Production Code (PRIORITY: MEDIUM)

**Issue**: Production code contains debug logging statements.

**Findings**:
- 37 occurrences of `Log.d()`, `Log.i()`, etc. across 4 files
- 2 occurrences of `println()` statements
- Debug logs can leak sensitive information
- Minor performance impact

**Affected Files**:
- `CribbageApplication.kt` (8 Log + 1 println)
- `MainActivity.kt` (4 Log)
- `CribbageMainScreen.kt` (24 Log)
- `UserAgentInterceptor.kt` (1 Log + 1 println)

**Recommendation**:
```kotlin
// OPTION 1: Remove debug logs (Log.d, Log.i)
// Keep only Log.w and Log.e for important warnings/errors

// OPTION 2: Use BuildConfig check
if (BuildConfig.DEBUG) {
    Log.d(TAG, "Debug message")
}

// OPTION 3: Enable ProGuard log removal (already configured)
// Uncomment in proguard-rules.pro:151-155
```

**Impact if not addressed**:
- Minor security concern (info disclosure)
- Minimal performance impact
- Not a blocker for approval

---

### 3. Unused Network Code (PRIORITY: LOW)

**Issue**: Unused Retrofit/OkHttp service interfaces present.

**Findings**:
- `LoginService.kt` - Retrofit interface (not used)
- `ScheduleService.kt` - Retrofit interface (not used)
- `UserAgentInterceptor.kt` - OkHttp interceptor (only used by PerimeterX)

**Recommendation**:
```bash
# Remove unused files (reduces confusion)
rm app/src/main/java/com/brianhenning/cribbage/LoginService.kt
rm app/src/main/java/com/brianhenning/cribbage/ScheduleService.kt
# UserAgentInterceptor only needed if keeping PerimeterX
```

**Impact if not addressed**:
- No functional impact
- Minor code cleanliness issue
- Not a blocker for approval

---

### 4. Privacy Policy Hosting (PRIORITY: HIGH)

**Issue**: Privacy policy created but not yet hosted publicly.

**Current Status**:
- ✅ Policy created (PRIVACY_POLICY.md, docs/privacy-policy.md)
- ⚠️ Not yet committed to git
- ⚠️ GitHub Pages not yet enabled

**Action Required**:
1. Commit privacy policy files to git
2. Push to GitHub
3. Enable GitHub Pages in repo settings (Settings → Pages → Source: docs folder)
4. Verify URL works: https://henninb.github.io/android-cribbage/privacy-policy
5. Use this URL in Play Store listing

**Impact if not addressed**:
- **BLOCKER** - Cannot submit without hosted privacy policy URL
- Play Store requires publicly accessible privacy policy

---

## 📋 Play Store Data Safety Section

When filling out the Data Safety section in Play Console:

### Data Collection
**Question**: Does your app collect or share user data?
**Answer**: YES (if keeping PerimeterX) / NO (if removed)

### If YES (PerimeterX SDK present):

**Data Types Collected**:
- ✅ Device or other IDs (by PerimeterX)
- ✅ App interactions (by PerimeterX)
- ✅ Crash logs (by PerimeterX)

**Data Usage**:
- ✅ Fraud prevention, security, and compliance

**Data Sharing**:
- ✅ Data shared with PerimeterX (now Human Security)

**Data Security**:
- ✅ Data is encrypted in transit
- ✅ Data is encrypted at rest
- ❌ Users cannot request deletion (handled by PerimeterX)

### If NO (PerimeterX removed):

**Data Types Collected**:
- Only local game statistics (not transmitted)

**All questions**: NO

This is the ideal scenario for a simple offline game.

---

## 🎯 Recommended Actions Before Submission

### High Priority (Required)

1. **[ ] Decide on PerimeterX SDK**
   - Remove if no online features planned (recommended)
   - OR justify and keep for future features

2. **[ ] Host Privacy Policy**
   - Commit files to git
   - Enable GitHub Pages
   - Verify URL works
   - Update Play Store listing

3. **[ ] Update Privacy Policy if removing PerimeterX**
   - Remove PerimeterX section
   - Update data collection statements
   - Simplify to local-only data

### Medium Priority (Recommended)

4. **[ ] Remove/Disable Debug Logging**
   - Remove Log.d(), Log.i(), println()
   - OR uncomment ProGuard log removal rules
   - Keep Log.w() and Log.e() for critical issues

5. **[ ] Clean Up Unused Code**
   - Remove LoginService.kt
   - Remove ScheduleService.kt
   - Remove UserAgentInterceptor.kt (if removing PerimeterX)

### Low Priority (Optional)

6. **[ ] Test Release Build Thoroughly**
   - Install app-release.apk on device
   - Verify all functionality works
   - Check for ProGuard-related crashes
   - Test game statistics persistence

7. **[ ] Update INTERNET Permission**
   - If removing PerimeterX, evaluate if INTERNET is still needed
   - Remove if no network features planned

---

## 🔍 Content Rating Guidance

For Play Console Content Rating questionnaire:

**Expected Rating**: Everyone or Everyone 10+

**Questionnaire Answers**:
- Violence: No
- Sexual content: No
- Bad language: No
- Controlled substances: No
- Gambling: No (cribbage scoring is not real-money gambling)
- User interaction: No (offline single-player only)
- Share location: No
- Personal info sharing: No (or minimal if PerimeterX present)

---

## ✅ Pre-Launch Checklist

Use this checklist before submitting:

### Code & Build
- [x] Release build compiles successfully
- [ ] ProGuard/R8 optimization tested
- [ ] No critical lint errors
- [ ] Version code/name updated
- [ ] Signing configuration tested
- [ ] App installs and runs on test device

### Privacy & Security
- [ ] Privacy policy hosted and accessible
- [ ] PerimeterX decision made (keep or remove)
- [ ] Debug logs removed/disabled
- [ ] No hardcoded secrets or API keys
- [ ] Permissions justified and minimal

### Play Store Assets (Separate task)
- [ ] 512x512 app icon created
- [ ] Feature graphic created (1024x500)
- [ ] Screenshots captured (min 2)
- [ ] Store listing text written
- [ ] Privacy policy URL added

### Play Console
- [ ] Store listing completed
- [ ] Content rating completed
- [ ] Data safety section completed
- [ ] Pricing set (Free)
- [ ] Countries selected

---

## 📞 Support Information

If you need clarification on any policy requirements:
- **Play Console Help**: https://support.google.com/googleplay/android-developer
- **Policy Center**: https://play.google.com/about/developer-content-policy/
- **Developer Documentation**: https://developer.android.com/distribute/play-policies

---

## Final Recommendation

**Before submitting to Play Store**:

1. **Remove PerimeterX SDK** (unless you have specific plans for online features)
2. **Host privacy policy** on GitHub Pages
3. **Remove debug logging**
4. **Clean up unused network code**
5. **Test release build thoroughly**

These changes will:
- ✅ Improve approval chances
- ✅ Reduce app size
- ✅ Minimize data collection concerns
- ✅ Simplify privacy policy
- ✅ Provide better user experience

**Estimated time to complete**: 1-2 hours

---

*This review is based on Google Play Developer Program Policies as of October 2025. Policies are subject to change. Always review the latest policies before submission.*
