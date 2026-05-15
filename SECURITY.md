# Security Policy

## Supported Versions

Only the latest release of Shellify receives security fixes.

| Version | Supported |
|---------|-----------|
| Latest  | ✅        |
| Older   | ❌        |

## Reporting a Vulnerability

**Do not open a public GitHub issue for security vulnerabilities.**

Use GitHub's private vulnerability reporting instead:
1. Go to the [Security tab](../../security) of this repository.
2. Click **"Report a vulnerability"**.
3. Fill in the details and submit.

Alternatively, e-mail **contact@shellify.app** with:

- A clear description of the vulnerability.
- Steps to reproduce or a proof-of-concept.
- The potential impact and affected versions.

### What to expect

| Step | Timeline |
|------|----------|
| Acknowledgement | Within 72 hours |
| Status update | Within 7 days |
| Fix or mitigation | Within 90 days (critical issues prioritised) |

We will coordinate disclosure timing with you. Public disclosure happens only after a fix is available or the 90-day window closes, whichever comes first.

## Scope

The following are **in scope**:

- Data leakage between isolated WebView containers.
- Bypass of biometric lock or encrypted-backup protections.
- Remote code execution via crafted web content.
- Cryptographic weaknesses in `core/crypto` or `core/security`.

The following are **out of scope**:

- Vulnerabilities in third-party libraries (report upstream; note them here if Shellify can mitigate).
- Issues requiring physical access to an already-unlocked device.
- Self-XSS or attacks requiring the user to install a malicious app alongside Shellify.

## Preferred Languages

English.
