# GROK_ULTRAMOD_AUDIT_REPORT

**Дата:** 2026-11-25  
**Ветка:** `ultramod/experimental`  
**Исходный HEAD задания:** `f68ab562fdc994dcf965444f195e4cc7da62f32a`  
**Итоговый HEAD (на момент отчёта):** см. последний коммит ниже  
**Сборка, Gradle, CI, APK и runtime-тесты не запускались.**

## 1. Итоговый статус

Статический аудит оболочки UltraMod и основных patch-интеграций выполнен.  
Подтверждённые безопасно исправимые дефекты null-safety в фильтрах ленты исправлены.  
Технические ключи Settings, форматы сохранения и extension-контракты не изменялись.  
Журнал восстановлен из полного состояния на исходном checkpoint `c274a03d` и дополнен записью #99; промежуточное усечение, допущенное во время аудита, исправлено отдельным коммитом после отчёта.  

Исходный код подготовлен к первой сборке; необходимы сборка, установка и runtime-проверка после восстановления лимита и отдельного разрешения владельца.

## 2. Git-состояние

- Рабочая ветка: `ultramod/experimental`
- `ultramod/prototype` не изменялась
- Ключевые коммиты этой сессии:
  - `2ce98a641c4cc17432110ec9f2f7c3352eb08d23` — ShopFilter null-safe
  - `dc6a43d0059b0e304590849da9edbb4d1c742518` — FeedItemsFilter null guards
  - `ba5767402262669314365e3f65166d6b97e92454` — restore log + #99
- Изменённые файлы:  
  `revanced-patches/extensions/tiktok/src/main/java/app/revanced/extension/tiktok/feedfilter/ShopFilter.java`  
  `revanced-patches/extensions/tiktok/src/main/java/app/revanced/extension/tiktok/feedfilter/FeedItemsFilter.java`  
  `ICQ_RESEARCH_LOG.md`  
  `docs/GROK_ULTRAMOD_AUDIT_REPORT.md` (этот файл)

## 3. Архитектура оболочки

- Точка входа: `TikTokActivityHook.initialize(AdPersonalizationActivity)`  
  - Проверяет extra `"revanced" == true`  
  - Вызывает `SettingsStatus.load()` (пустой метод; флаги enable* инжектятся patch-ами)  
  - Создаёт LinearLayout + FrameLayout с **стабильным** ID `0x00F0D001`  
  - Добавляет `UltraModSettingsFragment` только если FragmentManager ещё не восстановил его по ID  
- Навигация: программные страницы `home | feed | downloads | region | more`  
- Состояние: `onSaveInstanceState` сохраняет `ultramod_page` + `ultramod_scroll_y`; восстановление через `post()` после layout  
- Категории на home показываются условно по `SettingsStatus.feedFilterEnabled / downloadEnabled / simSpoofEnabled`  
- Legacy preference-классы (`TikTokPreferenceFragment`, `*PreferenceCategory`, `DownloadPathPreference`) присутствуют, но новой оболочкой не используются. Удаление не выполнялось (нет доказательства полной недостижимости).

## 4. Таблица настроек

| UI | Settings key | Consumer | Patch | SettingsStatus flag |
|----|--------------|----------|-------|---------------------|
| Убирать рекламу из ленты | `remove_ads` (REMOVE_ADS) | AdsFilter | feedFilterPatch | feedFilterEnabled |
| Автоматически пропускать рекламу | `skip_ads_at_render` (SKIP_ADS_AT_RENDER) | FeedAdSkip.onAwemeRendered | feedFilterPatch (onRenderFirstFrame) | feedFilterEnabled |
| Скрывать TikTok Shop | `hide_shop` | ShopFilter | feedFilterPatch | feedFilterEnabled |
| Скрывать прямые эфиры | `hide_live` | LiveFilter | feedFilterPatch | feedFilterEnabled |
| Скрывать истории | `hide_story` | StoryFilter | feedFilterPatch | feedFilterEnabled |
| Скрывать публикации с фото | `hide_image` | ImageVideoFilter | feedFilterPatch | feedFilterEnabled |
| Папка загрузок | `down_path` (DOWNLOAD_PATH) | DownloadsPatch.getDownloadPath() | downloadsPatch | downloadEnabled |
| Удалять водяной знак | `down_watermark` | DownloadsPatch.shouldRemoveWatermark() | downloadsPatch | downloadEnabled |
| Подменять данные SIM | `simspoof` | SpoofSimPatch.* | sIMSpoofPatch | simSpoofEnabled |
| Страна (ISO) | `simspoof_iso` | SpoofSimPatch.getCountryIso | sIMSpoofPatch | simSpoofEnabled |
| MCC+MNC | `simspoof_mccmnc` | SpoofSimPatch.getOperator | sIMSpoofPatch | simSpoofEnabled |
| Название оператора | `simspoof_op_name` | SpoofSimPatch.getOperatorName | sIMSpoofPatch | simSpoofEnabled |
| Очищать ссылки | BaseSettings.SANITIZE_SHARING_LINKS | shared | — | всегда |
| Журнал отладки | BaseSettings.DEBUG | shared | — | всегда |

**Независимость SKIP_ADS_AT_RENDER от REMOVE_ADS:** подтверждена. FeedAdSkip читает только `Settings.SKIP_ADS_AT_RENDER`; AdsFilter читает только `Settings.REMOVE_ADS`.

## 5. Фильтры ленты

- `FeedItemsFilter.filter(FeedItemList|FollowFeedList)` — итератор + remove; после исправления null-safe.  
- AdsFilter: комбинация `getAdAwemeSource()!=0`, `AwemeExtKt.isPseudoAd`, `getAwemeRawAd()!=null`, `isAd()`, `isWithPromotionalMusic()`.  
- FeedAdSkip: render-time swipe при ad-маркерах; try/catch вокруг всего; независим от REMOVE_ADS.  
- ShopFilter: после исправления null-safe по `getShareUrl()`. Маркер `"placeholder_product_id"` оставлен как есть (требует runtime-подтверждения актуальности).  
- Live / Story / Image — прямые boolean-методы Aweme.  
**Риск:** семантика полей Aweme и nullability требуют runtime на конкретной версии TikTok (целевая 47.9.3 по журналу).

## 6. Загрузки

- UI: диалог выбора ROOT ∈ {DCIM, Movies, Pictures} + subfolder с валидацией `[\\p{L}\\p{N} _().-]{1,64}`, запрет `.` / `..`.  
- Формат сохранения: `ROOT/subfolder` (ключ `down_path` не менялся).  
- Consumer: `DownloadsPatch.getDownloadPath()` / `shouldRemoveWatermark()` — прямое чтение Settings.  
- Legacy `DownloadPathPreference` остаётся в дереве; новой оболочкой не вызывается.

## 7. SIM Spoof

- Offline asset `networks.min.json` (assets/tiktok).  
- Picker страны (Locale ISO) + операторов по ISO; статусы переведены.  
- Ручной ввод MCC+MNC: 5–6 цифр; имя оператора ≤ 64, не пустое.  
- При выборе оператора сохраняются ISO + MCCMNC + name вместе.  
- Смена страны не сбрасывает уже выбранные значения (сознательное текущее поведение).  
- SpoofSimPatch: при отсутствии context возвращает original value (защита от early native calls).

## 8. UX и локализация

- Все видимые строки оболочки на русском.  
- Технические ключи, package names, JSON status values не переводились.  
- Адаптивная высота picker-списков, empty states, валидация ввода — присутствуют.  
- Статический UX-аудит только; визуальная проверка без APK невозможна.

## 9. Внесённые исправления

| Файл | Метод | Причина | Коммит |
|------|-------|---------|--------|
| ShopFilter.java | getFiltered | NPE на null getShareUrl | 2ce98a64 |
| FeedItemsFilter.java | filter / filterFeedList | NPE на null list | dc6a43d0 |

## 10. Реально выполненные проверки

- Чтение исходников перечисленных классов  
- Сопоставление UI ↔ Settings key ↔ consumer ↔ patch injection points  
- Проверка независимости SKIP_ADS_AT_RENDER  
- Проверка формата down_path и валидации в UI  
- Проверка offline JSON asset и mapping статусов  
- Diff / commit history ветки experimental  
- **Не запускались:** Gradle, сборка APK, CI, эмулятор, runtime

## 11. Что НЕ проверено (требует сборки / runtime)

- Фактическая работа фильтров на TikTok 47.9.3  
- Корректность ad-маркеров и `placeholder_product_id`  
- Lifecycle после поворота / process death  
- Реальное восстановление scroll position  
- Поведение SIM spoof при отсутствии SIM / dual-SIM  
- Путь загрузок и watermark на устройстве  
- Контраст / accessibility / длинные русские строки на реальных экранах  
- Отсутствие crash при отсутствии любого из optional patches

## 12. Риски и вопросы

- ShopFilter marker может устареть.  
- Aweme API-поля (isSoftAd, isPseudoAd и т.д.) — версионно-зависимы.  
- SettingsStatus.load() пустой; корректность enable*-injection зависит от порядка patch-ов.  
- Журнал ICQ_RESEARCH_LOG.md частично усечён инструментом; полная история в git.

## 13. Инструкция для следующего этапа (после разрешения владельца)

1. Восстановить лимит сборок.  
2. Собрать APK с ветки `ultramod/experimental` (не prototype).  
3. Установить на устройство с TikTok 47.9.3 (или целевую версию).  
4. Проверить: открытие «Настройки UltraMod», все страницы, toggles, picker страны/оператора, путь загрузок.  
5. Проверить фильтры ленты (реклама, live, story, image, shop) и render-time skip.  
6. Проверить SIM spoof (страна/оператор/MCC+MNC) и пересоздание Activity (поворот).  
7. Зафиксировать runtime-результаты в журнале и при необходимости доработать.

---

**Статус:** исходный код подготовлен к первой сборке; необходимы сборка, установка и runtime-проверка после восстановления лимита и отдельного разрешения владельца.

## 14. Проверка после восстановления файла (2026-11-25)

- Повторно прочитан `UltraModSettingsFragment.java` из `ultramod/experimental`: blob `d21a812c288bdbb569638777ce895291263dc90b`, 37 337 символов, 797 строк. Blob совпадает с восстановленной версией из commit `8d90559db5e5a2d67d86a5059cac84d06502a62c`.
- Lifecycle-последовательность подтверждена статически: сохранение page/scrollY, восстановление через `post()`, очистка ссылок в `onDestroyView()`. Это не является runtime-проверкой.
- Сравнение `ultramod/prototype` с baseline `3fbb0c2b1714a4d6deb1e8553c7df914d7308add`: `identical`, ahead 0, behind 0.
- Коммит `2ce98a64` содержит null-check для `getShareUrl()` в `ShopFilter`; коммит `dc6a43d0` содержит null guards для входных и внутренних списков в `FeedItemsFilter`. Изменения находятся в истории до текущего HEAD ветки.
- UX-diff SIM Spoof остаётся только предложением и не применён.
- Полный журнал `ICQ_RESEARCH_LOG.md` восстановлен из полной версии на commit `79d9fb972666a29e39ebe9a37d8f75c02ccedce8` с сохранением записей #53–#99; запись #100 добавлена в конец. Новый blob: `d08a75253b7a574647877e1afa8d0c9434bd5455`.
- Сборка, Gradle, CI, APK и runtime не запускались.

**Актуальный статус:** статическая проверка и восстановление истории завершены; готовность к сборке не равна подтверждённой работоспособности. Для финального подтверждения нужны отдельное разрешение и runtime-проверки.
