# TestModule

Sample **LSPosed** module that makes a target app load a class from the module instead of its own copy.

## How it works

1. `ModuleConfig` names the target package and the classes to replace.
2. For each one, the module has a **smali** file at the matching path under
   `app/src/main/smali/` (sample: `com/example/targetapp/util/Greeter.smali`).
3. At build time the `assembleSmali` Gradle task assembles all smali into
   `assets/replacements.dex` (smali 3.0.9, API 27).
4. When the target app starts, `MainHook` hooks `ClassLoader.loadClass(String)`. Requests for a
   replaced name on the app's class loader are answered from that dex through a child-first
   `InMemoryDexClassLoader`, which still resolves every other class from the app.

The replacement must keep every public member the app uses (names, descriptors, access flags).
Classes the app loaded before the hook ran cannot be swapped.

## Use it on your own app

1. Edit `app/src/main/java/com/hajs/testmodule/ModuleConfig.java` (`TARGET_PACKAGE`, `REPLACED_CLASSES`).
2. Update `app/src/main/res/values/arrays.xml` with the same package (recommended scope).
3. Replace `Greeter.smali` with your class, at `app/src/main/smali/<package path>/<Class>.smali`.
   Tip: get the original smali with `apktool d target.apk` and edit it.
4. Build, install, enable the module in LSPosed, tick the target app in its scope, force-stop the app.

```bash
gradle :app:assembleDebug     # or add a Gradle wrapper with `gradle wrapper`
```

Requires JDK 17 and the Android SDK. Logs show up in the LSPosed log with the `[TestModule]` prefix.

## Releases (semantic-release)

Releases are fully automated by `.github/workflows/release.yml` using
[semantic-release](https://semantic-release.gitbook.io/) and
[Conventional Commits](https://www.conventionalcommits.org/).

| Branch | Result |
| ------ | ------ |
| `dev`  | prerelease, e.g. `v1.0.0-beta.1` (GitHub prerelease, `beta` channel) |
| `main` | stable release, e.g. `v1.0.0` |

On every push, semantic-release computes the next version from commit messages, then:
writes `version.properties`, builds the release APK, updates `CHANGELOG.md`, commits both back
(`chore(release): … [skip ci]`), tags, and publishes a GitHub release with the APK attached.

| Commit | Bump |
| ------ | ---- |
| `fix: …` | patch |
| `feat: …` | minor |
| `feat!: …` or a `BREAKING CHANGE:` footer | major |
| `docs:`, `chore:`, `refactor:` … | no release |

### Signing (optional)

Without secrets the release APK is signed with the debug key (installable, not for distribution).
For a real key, add these repository secrets: `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`,
`ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`.

### Branch protection note

The release commit is pushed back to the branch by `GITHUB_TOKEN`. If `main`/`dev` are protected,
allow GitHub Actions to bypass the rules, or use a PAT in `GITHUB_TOKEN`'s place.
