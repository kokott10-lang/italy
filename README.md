# 이탈리아 탐험대 – APK 빌드

## 방법 A: Android Studio (가장 쉬움)
1. Android Studio 설치 후 이 폴더(android)를 `Open`
2. 메뉴 Build > Build Bundle(s) / APK(s) > Build APK(s)
3. 완료 알림의 `locate`를 누르면 app-debug.apk 가 있어요.
4. 폰으로 옮겨 설치(출처 불명 앱 허용 필요)

## 방법 B: GitHub Actions
1. 이 폴더 내용을 새 GitHub 저장소 루트에 올리기
2. Actions 탭 > Build APK > Run workflow
3. 끝나면 Artifacts에서 italy-explorer-apk 다운로드(release APK, 디버그 서명)

HTML을 고치려면 app/src/main/assets/index.html 을 교체 후 다시 빌드하세요.
