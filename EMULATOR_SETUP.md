# Lighter Emulator Setup for Better Performance

## Quick Start

Good news: **KVM is already working!** ✓

You just need to create the lighter emulator.

## Step 1: Create the Lighter Emulator

1. **Open Android Studio**
2. Go to **Tools** → **Device Manager**
3. Click **Create Device** (the + button)
4. **Select Hardware**:
   - Choose **Pixel 4** or **Pixel 5** (NOT Pixel 8a)
   - Click **Next**
5. **Select System Image**:
   - Click the **x86 Images** tab
   - Select **UpsideDownCake** (API Level 34)
   - If not downloaded, click **Download** next to it
   - Click **Next**
6. **Verify Configuration**:
   - AVD Name: `Pixel_4_API_34`
   - Click **Show Advanced Settings**
   - Set **RAM**: 1536 MB
   - Set **VM Heap**: 256 MB
   - Set **Graphics**: Software - GLES 2.0
   - Click **Finish**

## Step 2: Run the Lighter Emulator

### Option A: Automatic (Recommended)
```bash
./start-emulator.sh
```
Wait for it to start, then in another terminal:
```bash
./run-new.sh
```

### Option B: Manual Start + Auto Deploy
Just run:
```bash
./run-new.sh
```
Follow the instructions to start the emulator manually, then run again.

## Performance Improvements

Compared to your current Pixel 8a setup:

| Setting | Old (Pixel 8a) | New (Pixel 4) | Impact |
|---------|----------------|---------------|--------|
| Screen Size | 1080 x 2400 | 1080 x 2280 | Smaller = faster rendering |
| API Level | 35/36 | 34 | Older = more stable |
| RAM | ~4096 MB | 1536 MB | 62% less memory usage |
| GPU | Hardware | Software | More compatible |
| Snapshots | Enabled | Disabled | Faster startup |

**Expected improvement**: 40-60% faster, much more responsive

## Troubleshooting

### Emulator still slow?
Try even lighter settings:
- RAM: 1024 MB (minimum)
- Use Pixel 3 instead of Pixel 4

### Can't find API 34?
- In SDK Manager: **Tools** → **SDK Manager** → **SDK Platforms**
- Check **Show Package Details**
- Find **Android 14.0 (API 34)** and install

### Emulator crashes?
Check logs:
```bash
tail -f /tmp/emulator.log
```

## Switching Back to Old Emulator

If you need the Pixel 8a emulator:
```bash
./run.sh  # Uses the old emulator
```

## Clean Up Old Emulator (Optional)

To free up disk space:
1. Device Manager → Click menu (⋮) on Pixel 8a
2. Select **Delete**
