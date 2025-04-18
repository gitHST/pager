# ✅ Release Checklist – Android App

Use this checklist before releasing a version of the app on GitHub.

---

## 📦 Code & Project

- [ ] Code compiles with no errors or warnings
- [ ] All lint and static analysis issues resolved
- [ ] Logs removed or wrapped with debug flags
- [ ] No hardcoded strings (use `strings.xml`)
- [ ] Version code & version name updated (`build.gradle.kts`)
- [ ] Dependencies updated (`libs.versions.toml`)
- [ ] Database migrations implemented and tested (Room)
- [ ] Unused resources/imports removed
- [ ] Proguard/R8 rules configured
- [ ] Jetpack Compose previews build (if used)

---

## 🔐 Security

- [ ] No API keys or secrets committed
- [ ] `.gitignore` includes `local.properties`, `.keystore`, etc.
- [ ] GitLeaks or similar run to check for secrets

---

## 🧪 Testing

- [ ] All unit tests pass
- [ ] All UI tests pass (if any)
- [ ] Manual testing across devices, orientations, themes
- [ ] Accessibility basics tested (TalkBack, font scaling)
- [ ] Crash handling tested (if integrated)

---

## 🚀 Build & Release

- [ ] Signed release APK/AAB generated
- [ ] Proguard mapping file saved
- [ ] Changelog written for this version
- [ ] Release tag created (e.g. `v1.0.0`)
- [ ] APK/AAB uploaded to GitHub Releases

---

## 📁 GitHub Repo

- [ ] `README.md` includes:
    - [ ] Project description
    - [ ] Screenshots or demo GIFs
    - [ ] Build/setup instructions
    - [ ] Tech stack/libraries
    - [ ] Contact or credit info
- [ ] `LICENSE` file included
- [ ] `CONTRIBUTING.md` added (if open source)
- [ ] GitHub Issues or Discussions enabled (if relevant)
- [ ] CI/CD (GitHub Actions etc.) working

---

✅ Ready to ship!
