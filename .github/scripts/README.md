# .github/scripts

Shell scripts used by CI workflows. Stored as committed files rather than inline `script:` blocks in workflow YAML because `reactivecircus/android-emulator-runner` runs each line of an inline script as a separate `/usr/bin/sh -c` invocation, which breaks variable scoping and multi-line control flow.

| Script | Used by |
|--------|---------|
| `smoke-test.sh` | `main.yml` — installs the release APK on the emulator and verifies the app launches without crashing |
