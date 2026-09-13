# Сборка под Android (NexaProxy)

## Требования

- **JDK 21** (например Microsoft Build of OpenJDK 21)
- **Android SDK** — Platform 37, Build-Tools 37.0.0, Platform-Tools, NDK r29
- **Go 1.27+ и Go Mobile** (модуль `golang.org/x/mobile/cmd/gomobile`) — только для пересборки ядра
- **Gradle 9.x** (обёртка `gradlew`)

Пути к SDK/локали указываются в **`local.properties`** (файл в репозиторий не включается).

## Подпись (локально, не в CI)

B подписи используется `release.keystore`, пароли и псевдоним берутся из `local.properties`:

```properties
KEYSTORE_PASS=...
ALIAS_NAME=nexaproxy
ALIAS_PASS=...
```

При отсутствии `KEYSTORE_PASS` в `local.properties` release-сборка собирается **без подписи** (unsigned) — так поведёт себя CI.

## Сборка

```bash
# Windows — ядро/ассеты по необходимости (локально)
.\library\core\build.bat
.\gradlew.bat :app:downloadAssets

# Windows — APK (флавор по умолчанию)
.\gradlew.bat :app:assembleOssRelease

# Windows — legacy-флавор (Android 5.0+)
.\gradlew.bat :app:assembleLegacyRelease

# Linux/macOS
./gradlew :app:assembleOssRelease
```

Артефакты: `app\build\outputs\apk\oss\release\`.

## Flavor`ы

| Flavor        | Min SDK | Назначение                        |
|---------------|---------|-----------------------------------|
| `oss`         | 23      | по умолчанию                      |
| `legacy`      | 21      | старые устройства (Android 5.0+)  |

## Проверка (быстрая, без APK)

```bash
./gradlew :app:compileOssReleaseKotlin
```

Полный прогон Room/KSP-миграций требует Room-схем из `app/schemas/` (включены в репозиторий).

## CI

Готовые пайплайны — `.github/workflows/`:

- `build.yml` — сборка APK на каждый push/PR.
- `pages.yml` — сборка и публикация этого раздела документации на GitHub Pages.

Публикация в релизы GitHub выполняется вручную (см. `RELEASING`).
