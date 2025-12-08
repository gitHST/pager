# ✅ Release Checklist – Android App

Use this checklist before releasing a version of the app on GitHub.

---

## 📦 Code & Project

- [ ] Code compiles with no errors or warnings
- [ ] Comment out the restorefromdb lines in MainActivity
- [ ] Removed unused screens from nav bar and nav host
- [ ] All lint and static analysis issues resolved ./gradlew ktlintFormat
- [ ] Version code & version name updated (`build.gradle.kts`)
- [ ] Dark mode still works
- [ ] Proguard/R8 rules configured

---

## 🔐 Security

- [ ] No API keys or secrets committed
- [ ] `.gitignore` includes `local.properties`, `.keystore`, etc.
- [ ] GitLeaks or similar run to check for secrets

---

## 🧪 Testing

- [ ] All unit tests pass
- [ ] Manual testing across devices, orientations, themes
- [ ] Accessibility basics tested (TalkBack, font scaling)
- [ ] Crash handling tested (if integrated)

---

## 🚀 Build & Release

- [ ] Signed release APK/AAB generated
  - Build > Generate Signed Bundle / APK. 
  - Select APK > Next. 
  - Choose your keystore file, key alias, and passwords. 
  - Select release as the build variant. 
  - Check the box V2 (Full APK Signature). 
  - Click Finish. 
  - The signed APK will be in: 
  - app/release
- [ ] Proguard mapping file saved
- [ ] Changelog written for this version
- [ ] Release tag created (e.g. `v1.0.0`)
- [ ] APK/AAB uploaded to GitHub Releases

---

## 📁 GitHub Repo

- [ ] `README.md` includes:
    - [ ] Project description
    - [ ] Updated version badge (Don't forget pls!)
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
