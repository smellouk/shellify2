# Terms of Service — Shellify

**Last updated: May 23, 2026**

---

## 1. Introduction

These Terms of Service ("Terms") govern your use of the Shellify application ("App"). By installing or using Shellify, you agree to these Terms. If you do not agree, you must not use the App.

Shellify is a local-first Android application that allows users to create isolated, web-view-based containers from websites they choose. It does not operate as a hosted service and does not provide cloud accounts or centralized infrastructure.

---

## 2. Nature of the Service

Shellify is:

- a local software tool installed on your device,
- a web-view based launcher for third-party websites you select,
- fully user-controlled, with all data stored on your device,
- and not a content provider, cloud service, or platform.

---

## 3. User Responsibility

You are responsible for:

- the websites you access through Shellify,
- compliance with all applicable laws and regulations in your jurisdiction,
- compliance with the terms and policies of any third-party website or service you use,
- and any content, data, or activity conducted through the App.

Shellify does not control, verify, or endorse third-party websites.

---

## 4. Acceptable Use

You agree not to use Shellify to:

- violate applicable laws or regulations,
- infringe intellectual property rights of any party,
- bypass paywalls, authentication systems, or technical protection measures without authorization,
- distribute malware, harmful code, or exploit tools,
- engage in fraud, phishing, harassment, or abusive activity,
- impersonate individuals, organizations, brands, or official services,
- share or distribute links, app configurations, backup files, or session data intended for phishing, credential theft, malware distribution, or unlawful activity,
- or use Shellify in a way that violates any third-party service's terms of use.

Shellify is a general-purpose, user-controlled tool. Its use for unlawful or harmful purposes is expressly prohibited. We reserve the right to update, suspend, restrict, or discontinue Shellify or specific features at any time, particularly in cases involving security, abuse prevention, legal compliance, or violations of these Terms.

---

## 5. No Affiliation

Shellify is not affiliated with, endorsed by, sponsored by, or officially connected to any third-party website or service accessible through the App, unless explicitly stated.

---

## 6. Open Source & Branding

The Shellify source code is made available under an open-source license. That license governs your use of the code.

The "Shellify" name, logo, and project branding are identifiers of the official project and are not granted under the open-source license. You may not use the Shellify branding in a way that suggests endorsement, affiliation, or official status for derivative works without prior permission. See the NOTICE file in the source repository for full details.

---

## 7. Backup & Data Responsibility

Shellify may generate encrypted backup files stored locally on your device.

You acknowledge that:

- backups may contain cookies, session tokens, or other sensitive data you have chosen to save,
- backups are encrypted with a password you set and are fully under your control,
- loss of your backup password results in permanent, irreversible loss of access to that backup — Shellify cannot recover or access backup contents,
- you are solely responsible for storing backup files securely and understanding what they contain.

---

## 8. JavaScript Functionality Bridges

To provide certain in-app features, Shellify injects small JavaScript snippets ("bridges") into the web pages you open. These bridges run exclusively inside the isolated WebView container for the app you are using and serve only functional purposes:

- **Notification bridge** — intercepts the browser's `Notification` API to route web push requests through Android's notification system, but only for apps where you have enabled notifications.
- **Media and control bridge** — enables page-level controls (e.g. back navigation, display settings) to interact with the Android system.
- **Translation bridge** — injects a Google-hosted translation script when you explicitly activate in-page translation for a specific app. This involves a network request to Google's servers; see Section 9 for details.

None of these bridges transmit data outside your device beyond what is necessary for the feature they enable. No script is injected silently — each bridge activates only when the corresponding feature is in use.

---

## 9. Third-Party Services

To provide certain features, Shellify may interact with third-party services including but not limited to:

- translation APIs (Google Translate),
- favicon and icon retrieval endpoints,
- icon libraries (Simple Icons via CDN),
- and the optional Mozilla GeckoView engine.

These services are governed exclusively by their own terms and privacy policies. Shellify is not responsible for their availability, accuracy, or data practices.

**Brand Icons.** Brand logos and icons displayed within the App — including icons sourced from Simple Icons — are trademarks of their respective brand owners. Shellify uses them solely to identify web apps you choose to add, in a nominative and non-commercial context. This does not imply affiliation with, endorsement by, or sponsorship by any brand.

---

## 10. Disclaimer of Warranty

Shellify is provided **"as is"** and **"as available"**, without warranty of any kind.

To the maximum extent permitted by applicable law:

- no express or implied warranties are provided, including warranties of merchantability, fitness for a particular purpose, or non-infringement,
- continued availability of any feature is not guaranteed,
- and Shellify does not warrant that the App will be uninterrupted, error-free, or secure at all times.

---

## 11. Limitation of Liability

To the maximum extent permitted by applicable law, Shellify and its developers are not liable for:

- content, actions, or data practices of third-party websites accessed through the App,
- user activity conducted through the App,
- data loss, device damage, or security incidents arising from use of the App or third-party services,
- or any direct, indirect, incidental, consequential, or punitive damages arising from your use or inability to use the App.

---

## 12. Changes to These Terms

We may update these Terms from time to time. Material changes will be communicated through the App or on our website. Continued use of Shellify after changes take effect constitutes acceptance of the revised Terms.

---

## 13. Governing Law

These Terms are governed by and construed in accordance with the laws of the Federal Republic of Germany, without regard to conflict-of-law principles, unless mandatory local consumer protection law in your jurisdiction requires otherwise.

---

## 14. Contact

For legal inquiries: [contact@shellify.app](mailto:contact@shellify.app)
