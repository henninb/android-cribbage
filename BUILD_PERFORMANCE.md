# Build Performance Optimization Guide

## Current Build Times (Baseline: ~4m 11s)

### Bottleneck Analysis
1. **R8 Minification** (~60-90s) - Required for release builds
2. **Clean task** (~30-60s) - Removes all incremental compilation benefits
3. **KMM iOS targets** (~20-30s) - Compiles iOS frameworks for Android builds
4. **Kotlin compilation** (~40-60s) - Can be improved with caching

## Optimizations Implemented

### 1. Gradle Properties (gradle.properties)
```properties
# Memory & GC
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8 -XX:+UseParallelGC
kotlin.daemon.jvmargs=-Xmx2048m

# Parallel & Caching
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configureondemand=true

# Incremental Compilation
kotlin.incremental=true
kapt.incremental.apt=true
kapt.use.worker.api=true
```

**Expected Impact:** 20-30% faster builds

### 2. Build Scripts

#### Original: `run-bundle.sh` (Full Clean Build)
- Always runs `clean` - 4+ minutes
- Use for: Final releases, troubleshooting build issues

#### New: `run-bundle-fast.sh` (Incremental Build)
- Skips `clean` - 1-2 minutes on subsequent builds
- Use for: Development iterations, testing

### 3. Build Variants

#### Development Workflow
```bash
# Fast incremental bundle (recommended for development)
./run-bundle-fast.sh

# Debug APK (fastest - no minification)
./gradlew assembleDebug --console=plain

# Install to device directly (no bundle creation)
./gradlew installDebug --console=plain
```

#### Release Workflow
```bash
# Full clean release bundle
./run-bundle.sh

# Or manually
./gradlew clean bundleRelease --console=plain
```

## Expected Build Times After Optimization

| Build Type | First Build | Incremental | Use Case |
|------------|-------------|-------------|----------|
| `bundleRelease` (clean) | 4-5 min | N/A | Final releases |
| `bundleRelease` (incremental) | N/A | 1-2 min | Development testing |
| `assembleDebug` | 2-3 min | 30-60s | Development iterations |
| `installDebug` | 2-3 min | 30-60s | Device testing |

## Additional Optimization Options (Not Yet Implemented)

### A. Skip iOS Builds for Android-Only Development
Add to `gradle.properties`:
```properties
# Disable iOS targets temporarily (reduces shared module build time)
kotlin.mpp.enableGranularSourceSetsMetadata=false
```

### B. Local Build Cache Configuration
Add to root `build.gradle`:
```groovy
buildscript {
    repositories {
        maven { url 'https://plugins.gradle.org/m2/' }
    }
}

gradle.buildCache {
    local {
        directory = new File(rootDir, 'build-cache')
        removeUnusedEntriesAfterDays = 30
    }
}
```

### C. R8 Optimization (Faster but Less Aggressive)
Add to `app/proguard-rules.pro`:
```
# Use faster optimization passes for development
-optimizationpasses 3
```

### D. Compose Compiler Metrics (Disable for Faster Builds)
Remove from `gradle.properties`:
```properties
android.enableComposeCompilerReports=true
```

## Troubleshooting Slow Builds

1. **First build always slow?**
   - Normal - Gradle downloads dependencies and builds cache
   - Subsequent builds will be faster

2. **Incremental builds not working?**
   - Run `./gradlew clean` once to reset
   - Check for configuration changes in build files

3. **Out of memory errors?**
   - Increase `org.gradle.jvmargs` beyond 4096m
   - Close other applications

4. **Build cache not helping?**
   - Check `~/.gradle/caches/` size (delete if > 10GB)
   - Run `./gradlew clean cleanBuildCache`

## Monitoring Build Performance

### Enable Build Scans
```bash
./gradlew bundleRelease --scan
```
- Provides detailed breakdown of build time
- Identifies slow tasks and bottlenecks

### Profile Report
```bash
./gradlew bundleRelease --profile
```
- Generates HTML report in `build/reports/profile/`
- Shows task execution times

## Summary

**Quick Wins (Implemented):**
- ✅ Parallel builds enabled
- ✅ Build caching enabled
- ✅ Increased memory allocation
- ✅ Incremental compilation enabled
- ✅ Fast build script without clean

**Expected Results:**
- 🚀 First build: ~4 minutes (similar)
- 🚀 Incremental builds: ~1-2 minutes (60% faster)
- 🚀 Debug builds: ~30-60 seconds

**Recommendation:**
Use `./run-bundle-fast.sh` for all development work and reserve `./run-bundle.sh` for final release builds.
