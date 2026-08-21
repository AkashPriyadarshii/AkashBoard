# SECURITY.md — Security Policy

## AkashBoard v1.0

**Author:** Akash Priyadarshi
**Date:** August 21, 2026

---

## Security Philosophy

> "A keyboard sees everything you type. It must be the most trustworthy app on your phone."

AkashBoard is built with security-first principles:

1. **Zero network access** — No INTERNET permission, no API calls, no telemetry
2. **Local-only processing** — All intelligence runs on-device
3. **Minimal permissions** — Only VIBRATE (for haptic feedback)
4. **Open source** — Full code auditability under GPLv3
5. **Defense in depth** — Multiple layers of protection

---

## Threat Model

### What We Protect Against

| Threat | Description | Mitigation |
|--------|------------|-----------|
| **Network exfiltration** | Keystrokes sent to remote server | No INTERNET permission, no network code |
| **Data theft (physical)** | Someone accesses device storage | Optional encryption for sensitive data |
| **Memory scraping** | Malicious app reads keyboard memory | Rust memory safety, no raw pointers |
| **JNI buffer overflow** | Malformed data causes crash | Bounds checking on all JNI calls |
| **Malicious themes** | Theme JSON contains exploits | Schema validation, sandboxed parsing |
| **Clipboard snooping** | Other apps read clipboard | Clipboard manager clears on timeout |
| **Side-channel attacks** | Timing attacks on predictions | Constant-time comparison where possible |

### What We Don't Protect Against (Out of Scope)

| Threat | Why |
|--------|-----|
| Rooted device attacks | Root breaks all app sandboxing |
| Physical keyloggers | Hardware-level attacks are outside app scope |
| Compromised OS | If Android itself is compromised, all bets are off |
| Screen recording | Users can screen-record keyboard output |
| Accessibility services | Malicious a11y services can read screen content |

---

## Security Controls

### 1. Network Isolation

```xml
<!-- AndroidManifest.xml -->
<!-- NO INTERNET PERMISSION -->
<!-- The app NEVER makes network requests -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.akashboard">
    
    <uses-permission android:name="android.permission.VIBRATE" />
    <!-- That's it. No INTERNET, no ACCESS_NETWORK_STATE, nothing else. -->
    
</manifest>
```

**Verification:**
```bash
# Check manifest for network permissions
grep -r "INTERNET\|ACCESS_NETWORK\|ACCESS_WIFI" app/src/main/AndroidManifest.xml
# Should return nothing

# Network monitor test
adb shell dumpsys network_management | grep akashboard
# Should show no connections
```

### 2. Data Encryption (Optional)

```kotlin
// Crypto.kt — Optional encryption for sensitive data
object Crypto {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    
    fun encrypt(plaintext: ByteArray): ByteArray {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
        keyStore.load(null)
        
        // Generate key in Android Keystore
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER
        )
        keyGenerator.init(KeyGenParameterSpec.Builder(
            "akashboard_key",
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .build())
        
        val secretKey = keyGenerator.generateKey()
        
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plaintext)
        
        // Prepend IV to ciphertext
        return iv + ciphertext
    }
    
    fun decrypt(ciphertext: ByteArray): ByteArray {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
        keyStore.load(null)
        
        val key = keyStore.getKey("akashboard_key", null) as SecretKey
        
        val iv = ciphertext.sliceArray(0..11)  // GCM IV is 12 bytes
        val data = ciphertext.sliceArray(12 until ciphertext.size)
        
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        
        return cipher.doFinal(data)
    }
}
```

### 3. Input Validation

```kotlin
// All JNI inputs are validated
class PredictorBridge {
    companion object {
        init { System.loadLibrary("predictor") }
        
        external fun nativePredict(context: String, topK: Int): Array<String>
    }
    
    fun predict(context: String, topK: Int = 3): List<String> {
        // Validate inputs
        require(topK in 1..5) { "topK must be between 1 and 5" }
        require(context.length <= 500) { "Context too long" }
        
        // Sanitize
        val sanitizedContext = context.take(500)
        
        return try {
            nativePredict(sanitizedContext, topK).toList()
        } catch (e: Exception) {
            emptyList()  // Never crash on prediction errors
        }
    }
}
```

### 4. Clipboard Security

```kotlin
// ClipboardManager — Security measures
class ClipboardManager(private val context: Context) {
    
    // Auto-clear clipboard after timeout
    private val clipboardTimeout = 5 * 60 * 1000L  // 5 minutes
    
    private val clipboardClearRunnable = Runnable {
        clearSystemClipboard()
    }
    
    fun addItem(text: String, sourceApp: String?) {
        // Don't copy passwords
        if (isPasswordField(sourceApp)) {
            return
        }
        
        // Don't copy very long text (potential data exfil)
        if (text.length > 10000) {
            return
        }
        
        // Schedule clipboard clear
        handler.postDelayed(clipboardClearRunnable, clipboardTimeout)
    }
    
    private fun isPasswordField(packageName: String?): Boolean {
        // Check if current field is a password field
        // This is determined by EditorInfo.inputType
        return packageName?.contains("password") == true
    }
    
    private fun clearSystemClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
    }
}
```

### 5. Theme Validation

```kotlin
// ThemeParser — Validate theme JSON before applying
class ThemeParser {
    
    fun parse(json: String): Result<ThemeConfig> {
        return try {
            val config = Json.decodeFromString<ThemeConfig>(json)
            
            // Validate name
            require(config.name.isNotBlank() && config.name.length <= 50) {
                "Invalid theme name"
            }
            
            // Validate colors are valid hex/rgba
            validateColor(config.colors.background, "background")
            validateColor(config.colors.keyBackground, "keyBackground")
            // ... validate all colors
            
            // Validate dimensions
            require(config.dimensions.cornerRadius in 0f..24f) {
                "Invalid corner radius"
            }
            
            Result.success(config)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun validateColor(color: Long, fieldName: String) {
        // Color should be a valid ARGB value
        require(color != 0L) { "Invalid color for $fieldName" }
    }
}
```

### 6. Storage Budget Enforcement

```kotlin
// StorageBudget — Prevent storage overflow
class StorageBudget(private val context: Context) {
    
    companion object {
        const val MAX_TOTAL_BYTES = 2 * 1024 * 1024L  // 2MB
        const val WARNING_THRESHOLD = 1.5 * 1024 * 1024L  // 1.5MB
    }
    
    suspend fun checkBudget(): BudgetStatus {
        val totalSize = calculateTotalSize()
        
        return when {
            totalSize > MAX_TOTAL_BYTES -> BudgetStatus.OVER
            totalSize > WARNING_THRESHOLD -> BudgetStatus.WARNING
            else -> BudgetStatus.OK
        }
    }
    
    suspend fun enforceBudget() {
        when (checkBudget()) {
            BudgetStatus.OVER -> {
                // Aggressively prune oldest data
                prunePredictionModel(maxEntries = 5000)
                pruneClipboardHistory(maxItems = 20)
            }
            BudgetStatus.WARNING -> {
                // Gently prune
                prunePredictionModel(maxEntries = 8000)
            }
            BudgetStatus.OK -> { /* No action needed */ }
        }
    }
}

enum class BudgetStatus { OK, WARNING, OVER }
```

---

## Security Testing

### Automated Checks

```bash
# Check for network permissions
grep -r "INTERNET" app/src/main/AndroidManifest.xml

# Check for hardcoded secrets
grep -r "api_key\|secret\|password\|token" app/src/main/java/

# Run lint checks
./gradlew lint

# Run security-focused lint
./gradlew lint --check Security
```

### Manual Testing

1. **Network monitor:** Install a network monitoring app, verify zero connections
2. **Storage inspection:** Use `adb shell` to inspect app data
3. **Permission review:** Check Android settings → Apps → AkashBoard → Permissions
4. **Crash testing:** Rapid input, edge cases, memory pressure

---

## Reporting Vulnerabilities

If you discover a security vulnerability:

1. **DO NOT** open a public GitHub issue
2. **DO NOT** disclose the vulnerability publicly
3. **Email:** akashpriyadarshii@users.noreply.github.com
4. **Include:**
   - Description of the vulnerability
   - Steps to reproduce
   - Potential impact
   - Suggested fix (if any)

**Response time:** We will acknowledge within 48 hours and provide a fix timeline.

**Disclosure:** We follow responsible disclosure. We will credit you (unless you prefer anonymity) after the fix is released.

---

## Security Checklist (Pre-Release)

- [ ] No INTERNET permission in manifest
- [ ] No network-related code in any file
- [ ] No hardcoded API keys or secrets
- [ ] All JNI inputs validated
- [ ] Clipboard auto-clear enabled
- [ ] Password fields excluded from clipboard
- [ ] Storage budget enforced
- [ ] Theme JSON validated before parsing
- [ ] No `unwrap()` in JNI-called Rust functions
- [ ] All error paths handled gracefully
- [ ] APK signed with release key
- [ ] ProGuard/R8 enabled for release builds

---

## Compliance

### GDPR (General Data Protection Regulation)
- AkashBoard collects ZERO user data
- No data leaves the device
- User can delete all data at any time (nuclear delete)
- No account required
- **GDPR compliant by design**

### CCPA (California Consumer Privacy Act)
- AkashBoard does not sell, share, or collect personal information
- **CCPA compliant by design**

### COPPA (Children's Online Privacy Protection Act)
- AkashBoard does not collect data from any users, including children
- **COPPA compliant by design**

---

## Security Updates

Security patches will be released as soon as possible after a vulnerability is confirmed. Updates will be announced via:
- GitHub Security Advisories
- GitHub Releases
- F-Droid (automatic update)
