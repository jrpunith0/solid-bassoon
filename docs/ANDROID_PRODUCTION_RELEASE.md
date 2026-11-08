# Android production release setup

The repository's debug APK is for development/testing only. A Play Console upload must use the signed release Android App Bundle (AAB) produced by `Android production release`.

## One-time GitHub configuration

Create a GitHub Actions environment named **production** and add these environment secrets:

| Secret | Value |
|---|---|
| `RELEASE_KEYSTORE_BASE64` | Base64-encoded upload keystore file (`.jks` or `.keystore`) |
| `RELEASE_STORE_PASSWORD` | Keystore password |
| `RELEASE_KEY_PASSWORD` | Password for the `upload` key alias |

Add this environment variable:

| Variable | Value |
|---|---|
| `RELEASE_CERT_SHA256` | SHA-256 fingerprint of the upload certificate, 64 hexadecimal characters; colons and letter case are normalized |

The workflow expects the key alias to be exactly `upload`. It fails closed if any secret/variable is missing, the keystore cannot be decoded/opened, the alias is absent, or the keystore certificate fingerprint does not match the configured fingerprint.

### Prepare the keystore value

Encode the keystore locally; never commit the keystore or put its passwords in source control.

Linux/macOS:
```sh
base64 -w 0 upload-keystore.jks
```

macOS alternative:
```sh
base64 < upload-keystore.jks | tr -d '\n'
```

Copy the output directly into the GitHub environment secret `RELEASE_KEYSTORE_BASE64`.

### Obtain the expected certificate fingerprint

Run locally:
```sh
keytool -list -v -keystore upload-keystore.jks -alias upload
```
Copy the SHA-256 certificate fingerprint into the GitHub environment variable `RELEASE_CERT_SHA256`. Do not use a debug certificate fingerprint. Protect the keystore and passwords as production signing credentials.

## Run a release

1. Open **Actions → Android production release → Run workflow** to build from the selected branch, or push a version tag matching `v*` (for example `v1.0.0`).
2. The workflow checks the signing inputs and certificate fingerprint before building.
3. It runs release unit tests and builds `app-release.aab`.
4. It verifies the AAB JAR signature and extracts the signer certificate fingerprint from the AAB itself. The extracted fingerprint must match `RELEASE_CERT_SHA256`.
5. Only after all checks succeed does it upload the `android-release-aab` artifact.

If a check fails, no release AAB artifact is uploaded. The temporary keystore is removed at the end of the job.

## Play Console handoff

Download `android-release-aab` from the successful Actions run and upload the AAB to the appropriate Play Console testing track. Review Play Console's app signing, policy, store listing, data safety, target API, and testing requirements separately. A successful CI build is not itself Play Store approval.

**Important:** If Play App Signing is enabled, the local upload key is the upload certificate; Google Play manages the app-signing key used for distribution. Keep a secure offline backup of the upload keystore.
