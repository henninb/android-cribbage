# Fish Shell Environment Setup for Cribbage App Signing

## Four Methods to Set Up Environment Variables

### Method 1: Separate Secrets File (⭐ RECOMMENDED - Git-Safe)

Run the provided setup script:

```fish
./setup_fish_secrets.fish
```

This will:
- Create `~/.config/fish/secrets.fish` with passwords (NOT in git)
- Add safe config to `~/.config/fish/config.fish` (safe to commit)
- Add `secrets.fish` to `.gitignore`
- Set restrictive permissions (600)
- Variables available in all new Fish sessions

After running, reload your config:
```fish
source ~/.config/fish/config.fish
```

**File Structure**:
```
~/.config/fish/
├── config.fish          # Safe to commit (no secrets)
├── secrets.fish         # NEVER COMMIT (contains passwords)
└── .gitignore          # Contains: secrets.fish
```

**What goes where**:
- `config.fish`: CRIBBAGE_KEYSTORE_PATH, CRIBBAGE_KEY_ALIAS, source secrets.fish
- `secrets.fish`: CRIBBAGE_KEYSTORE_PASSWORD, CRIBBAGE_KEY_PASSWORD

**Pros**: Secure, git-safe, automatic, persistent
**Cons**: Need to keep secrets.fish backed up separately

---

### Method 1b: Manual Secrets File Setup

If you prefer manual setup:

1. Copy the template:
```fish
cp secrets.fish.template ~/.config/fish/secrets.fish
```

2. Edit with your passwords:
```fish
nano ~/.config/fish/secrets.fish
```

3. Set permissions:
```fish
chmod 600 ~/.config/fish/secrets.fish
```

4. Add to `config.fish`:
```fish
# Cribbage App Signing Configuration
set -gx CRIBBAGE_KEYSTORE_PATH "$HOME/.android/keystores/cribbage-release-key.jks"
set -gx CRIBBAGE_KEY_ALIAS "cribbage-release"

# Source secrets file (not in git)
if test -f ~/.config/fish/secrets.fish
    source ~/.config/fish/secrets.fish
end
```

5. Add to `.gitignore`:
```fish
echo "secrets.fish" >> ~/.config/fish/.gitignore
```

**Pros**: Same as Method 1, more control
**Cons**: Manual steps

---

### Method 2: Universal Variables (Secure + Persistent)

Fish has universal variables that persist across sessions in Fish's encrypted database:

```fish
# Set universal variables (only need to do once)
set -Ux CRIBBAGE_KEYSTORE_PATH "$HOME/.android/keystores/cribbage-release-key.jks"
set -Ux CRIBBAGE_KEY_ALIAS "cribbage-release"

# Set passwords as universal variables (Fish encrypts these)
read -sP "Keystore password: " password; and set -Ux CRIBBAGE_KEYSTORE_PASSWORD $password
read -sP "Key password: " password; and set -Ux CRIBBAGE_KEY_PASSWORD $password
```

**Pros**: Most secure, encrypted storage, persistent
**Cons**: Variables stored in Fish's internal database

To view universal variables:
```fish
set -U | grep CRIBBAGE
```

To remove them later:
```fish
set -eU CRIBBAGE_KEYSTORE_PATH
set -eU CRIBBAGE_KEYSTORE_PASSWORD
set -eU CRIBBAGE_KEY_ALIAS
set -eU CRIBBAGE_KEY_PASSWORD
```

---

### Method 3: Session-Only Variables (Maximum Security)

For maximum security, enter passwords each time you build:

```fish
# Add to config.fish (without passwords)
set -gx CRIBBAGE_KEYSTORE_PATH "$HOME/.android/keystores/cribbage-release-key.jks"
set -gx CRIBBAGE_KEY_ALIAS "cribbage-release"
```

Then before building, set passwords in current session:
```fish
read -sP "Keystore password: " CRIBBAGE_KEYSTORE_PASSWORD; and set -gx CRIBBAGE_KEYSTORE_PASSWORD $CRIBBAGE_KEYSTORE_PASSWORD
read -sP "Key password: " CRIBBAGE_KEY_PASSWORD; and set -gx CRIBBAGE_KEY_PASSWORD $CRIBBAGE_KEY_PASSWORD
```

**Pros**: Maximum security, no password storage
**Cons**: Must enter passwords for each build session

---

## Verifying Environment Variables

After setting up, verify the variables are set:

```fish
echo $CRIBBAGE_KEYSTORE_PATH
echo $CRIBBAGE_KEY_ALIAS
# Don't echo passwords (security risk), but you can check if they're set:
if set -q CRIBBAGE_KEYSTORE_PASSWORD
    echo "✅ Keystore password is set"
else
    echo "❌ Keystore password is NOT set"
end
```

Or check all at once:
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

## Security Best Practices

### File Permissions
```fish
# Secure your keystore
chmod 600 ~/.android/keystores/cribbage-release-key.jks

# Secure your Fish config
chmod 600 ~/.config/fish/config.fish
```

### Backup Your Keystore (CRITICAL!)
```fish
# Backup to external drive
cp ~/.android/keystores/cribbage-release-key.jks /path/to/backup/

# Or create encrypted backup
tar czf - ~/.android/keystores/cribbage-release-key.jks | gpg -c > cribbage-keystore-backup.tar.gz.gpg
```

### .gitignore Protection
Make sure your `.gitignore` includes:
```
*.jks
*.keystore
local.properties
```

---

## Testing the Setup

Once environment variables are set, test with:

```fish
./gradlew bundleRelease
```

If successful, you'll see:
```
BUILD SUCCESSFUL
```

Output location:
```
app/build/outputs/bundle/release/app-release.aab
```

---

## Troubleshooting

### "Environment variable not set" error

Check if variables exist:
```fish
env | grep CRIBBAGE
```

If missing, source your config:
```fish
source ~/.config/fish/config.fish
```

### "Wrong password" error

The passwords you entered don't match your keystore. Try again:
```fish
read -sP "Keystore password: " password; and set -gx CRIBBAGE_KEYSTORE_PASSWORD $password
```

### "Keystore not found" error

Verify keystore exists:
```fish
ls -l ~/.android/keystores/cribbage-release-key.jks
```

If missing, you need to create it again.

---

## Fish Shell Cheat Sheet

```fish
# Set global exported variable (current session)
set -gx VAR_NAME "value"

# Set universal variable (persistent, all sessions)
set -Ux VAR_NAME "value"

# Unset global variable
set -e VAR_NAME

# Unset universal variable
set -eU VAR_NAME

# List all universal variables
set -U

# Check if variable is set
if set -q VAR_NAME
    echo "Variable is set"
end

# Reload Fish config
source ~/.config/fish/config.fish
```

---

## Recommended Approach

**For Development**: Use Method 3 (Universal Variables)
- Secure enough for personal development
- Convenient - passwords persist across sessions
- Fish stores them in encrypted format

**For CI/CD**: Use secure secret management
- GitHub Secrets
- GitLab CI/CD Variables
- Environment-specific configuration

**For Open Source**: Use Method 4 (Session-Only)
- Never commit passwords
- Maximum security
- Slight inconvenience worth it

---

## Next Steps

After setting up environment variables:

1. ✅ Verify variables are set
2. Update `app/build.gradle` with signing configuration
3. Build release AAB: `./gradlew bundleRelease`
4. Test the signed build

See `PLAY_STORE_PUBLISHING_GUIDE.md` for complete instructions.
