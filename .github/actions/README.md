# Reusable Composite Actions

Shared GitHub Actions used across `pull_request.yml`, `main.yml`, and `release.yml` to eliminate copy-paste between workflows.

## Actions

| Action | Description | Inputs |
|---|---|---|
| [`setup-android`](setup-android/action.yml) | Checkout, Java 17, Gradle setup | — |
| [`setup-keystore`](setup-keystore/action.yml) | Decode base64 keystore to runner temp | `keystore-base64` |
| [`run-unit-tests`](run-unit-tests/action.yml) | Run Gradle unit tests + JUnit report + artifact upload | `gradle-task` |
| [`run-screenshot-tests`](run-screenshot-tests/action.yml) | Run Roborazzi verification + upload diffs on failure | `gradle-task` |
| [`run-instrumentation-tests`](run-instrumentation-tests/action.yml) | KVM + AVD cache + emulator run + JUnit report | `gradle-task`, signing inputs (optional) |

## Usage pattern

Every job that needs the Android build environment starts with:

```yaml
steps:
  - uses: ./.github/actions/setup-android
```

For jobs that sign a release build, add keystore setup after:

```yaml
  - uses: ./.github/actions/setup-keystore
    with:
      keystore-base64: ${{ secrets.KEYSTORE_BASE64 }}
```

The decoded keystore is always written to `${{ runner.temp }}/keystore.jks`.

## Test actions

All three test actions accept a `gradle-task` input so the same action runs debug or release variants:

```yaml
# Debug (pull_request)
- uses: ./.github/actions/run-unit-tests
  with:
    gradle-task: testDebugUnitTest

# Release (main / release)
- uses: ./.github/actions/run-unit-tests
  with:
    gradle-task: testReleaseUnitTest
```

`run-instrumentation-tests` additionally accepts optional signing inputs (`signing-store-file`, `signing-store-password`, `signing-key-alias`, `signing-key-password`) — leave them empty for debug builds.

## Notes

- All test actions pass `--continue` to Gradle so every module's tests run even when one fails.
- `run-unit-tests` and `run-instrumentation-tests` require `permissions: checks: write` on the calling job for `mikepenz/action-junit-report` to post annotations.
- `setup-keystore` exposes a `keystore-path` output if a downstream step needs the path explicitly.
