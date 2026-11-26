

## Run #53 — generic DM slot inventory (2026-11-23)
- Deep run `45171868724` succeeded; artifact `14816838925` inspected. Static DEX only.
- `LX/0NAA` = `BaseSkeletonData` with `slotsData: Map<LX/0N9i, SlotContent>`; `LX/0NAL` wraps slot data plus `shouldAssemble`. This confirms the generic slot handoff, not the text renderer.
- 32 `IMMessageListProtocol` implementations are inventoried, mostly specialized protocols. `MessageListProtocolEnum` has big-emoji/translation/Markdown/GIF/cards, but no ordinary plain-text enum. Ordinary text likely takes a separate default/legacy route; not yet proven.
- `LX/0N9i` is only an integer-key interface in this dump. Concrete key implementations, slot ID mapping, type-7 receive assembler, and TextContent-to-View setter remain unknown.
- Next: enumerate `0N9i` implementations and skeleton protocol implementations; trace constructors/registries filling `0NAA.LIZJ`, then map ordinary type 7 to its receive text binder. Do not patch yet. APK/patcher/runtime unchanged; no backend/two-device validation.


## Latest DM research (#54)
- Corrected slot-key analyzer; successful run scanned 491,343 classes / 2,107,378 methods.
- 10 concrete slot keys and 40 skeleton protocols found. All identified protocols are specialized; no generic plain-text receive PowerCell was found.
- `LX/0NAD` inserts arbitrary content into a keyed slot map; `LX/0N9p` is used repeatedly, but ordinary-text slot binding remains unproven.
- Next: resolve slot resource IDs and trace generic incoming text fallback. No APK patch/runtime change yet.


## Latest DM research (#55)
- Successful run `45179303287` decoded APK resource IDs and mapped the slot keys to concrete UI areas: avatar, username, message status, reactions, quote, notice, report page, feedback, and auxiliary side/card/media slots.
- `LX/0N9p` (`id/kzq`) is heavily used by side/card/media/voice-transcript slots, not proven to be ordinary text. `LX/0N9g` is the key registry; `LX/0YjV` is a subset registry.
- Ordinary incoming type-7 text renderer and final text setter remain unidentified. Next: trace generic/default skeleton data producers and actual TextContent-to-view binding. No APK patch/runtime change.


## Latest DM research (#56)
- Successful run `45184344925` added a focused TextContent/view-setter scan. Actual receive-side TextContent UI hits still resolve to big emoji and translation only; send-side hits are not receive rendering.
- `IMSkeletonLayoutPowerCell.W5` does not set message body text: it writes UUID/scene/msgType tags and tracker metadata. Ordinary incoming text binder remains unknown.
- User observed the same sticker picker panel in DMs and comments, with tabs Stickers / GIF / Emoji. Next: source-trace shared panel host and tab registry, targeting an ICQ tab beside Emoji; separately continue DM receive-text fallback. No APK patch/runtime change.


## Latest panel research (#57–#58)
- Successful DEX scan `45194114885`, artifact `14827906782`; 81 candidate classes / 379 candidate methods in sticker, input, comment and feed-reply packages.
- DM panel: `ChatroomEmojiPanelAssem` extends `BaseEmojiPanelAssem`; its `Iq(ViewGroup, int, boolean)` calls `LX/0NKn.LIZ(ViewGroup, Function0)` through singleton `LX/0NKm.LIZ`. Sticker API exposes `getImStickerInputPanelService(): LX/0NKm`.
- Feed comments have a separate `DetailFeedEmojiPanelFragment`. Same visual picker does not yet prove same source host/tab registry.
- Tab registration for Stickers/GIF/Emoji and service class definitions remain unresolved. Follow-up correction is running as workflow `45198991854` to include full sticker implementation/API packages and fix descriptor matching.
- Next: inspect resolved service definitions, `BaseEmojiPanelAssem`, `ChatRoomPanelAssemV2`, and comment fragment callers; target ICQ tab beside Emoji. No APK patch/runtime change.


## Latest panel research (#59)
- Corrected scan 45198991854 and fast index 45198991407 both succeeded (commit 3c0a18e9b2077335ddc496f96cec806597c2796a).
- Wider scan still did not resolve definitions for LX/0NKm / LX/0NKn; only API return type and DM callsite are confirmed.
- Confirmed candidates: ChatroomEmojiPanelAssem extends BaseEmojiPanelAssem; ChatRoomPanelAssemV2 is the DM input-panel container; DetailFeedReplyMessageFragment is a separate feed-reply composer lead. Shared DM/comment tab host is not proven.
- Visible tab registry (Stickers/GIF/Emoji) remains unresolved. Next inspect complete panel/container setup and trace feed-reply picker host. Continue ordinary incoming type-7 TextContent research in parallel. No patch/runtime change.


## Research queue #60 — new scans running
- Expanded panel scan: assembler workflow `45201799460` in progress, targeting `BaseEmojiPanelAssem`, `ChatroomEmojiPanelAssem`, `ChatRoomPanelAssemV2`, feed-reply picker host, service references, and tab/pager setup.
- Expanded incoming-text scan: workflow `45203141225` in progress, targeting generic/default message adapter and receive-cell dispatch for ordinary type-7 TextContent. Fast index `45203141263` succeeded.
- No reports inspected yet; results pending. No APK patch/runtime change. Inspect both artifacts and log conclusions when finished.
## Latest panel discovery (#61)
- Successful incoming DEX scans 45203141225 / 45203198094 exposed LX/0NKm service interface, LX/0kOo implementation, and LX/0NKZ page enum.
- Native page enum includes SYSTEM_EMOJI, CUSTOM_EMOJI, GIPHY, and sticker categories. DM config lambda LX/0NFB gets page list from the service and passes LX/0NKQ into ChatroomEmojiPanelAssem.
- LX/0kOo.LIZJ() returns favourites, custom/system emoji, curated sets, and set stickers; GIPHY is a separate enum and its visibility/registration remains to trace.
- Comment MEMORY_COMMENT_AND_DM mode also calls the same IMStickerApi.getImStickerInputPanelService(), supporting shared service/configuration. Same exact UI host is not fully proven.
- Ordinary incoming type-7 TextContent renderer remains unresolved. No APK patch/runtime change.
- Assembler analyzer tuple bug fixed in commit 81ca0002; triggered reruns failed immediately without logs/artifacts. Corrected panel report pending.
## Follow-up #62 — GIF/GIPHY panel mapping
- StickerPanelAssemViewModel explicitly maps LX/0NKZ.GIPHY to page model LX/0Ymq. GIPHY therefore has a real page handler, not just an enum value.
- Current observed provider list and fallback list both omit GIPHY, so we still need to find which runtime provider/config adds it and verify whether it is the visible GIF tab or a separate panel.
- No APK patch/runtime change.
## Follow-up #63 — GIPHY model and DM list traced
- StickerPanelAssemViewModel.N93 maps LX/0NKZ.GIPHY to page model LX/0Ymq (fn() = 8, icon resource 0x7f010828). The page model exists, but its inherited item-list method is empty.
- DM lambda LX/0NFB supplies the page list from IMStickerInputPanelService.LIZJ(). BasicIMStickerImpl -> LX/0kOo returns favourites, custom/system emoji, curated sets, and sticker sets; it does not include GIPHY. Fallback LX/0NKo returns an empty list. Thus GIPHY is supported by dispatch but absent from the default DM list in this APK.
- BasicStickerPanelAssem is the pager/page host; BasicStickerPanelAssemPanelAssem is its parent panel assembler. Likely extension surface is category list + view-model mapping + page model/cell.
- Correction: comment MEMORY_COMMENT_AND_DM calls the same service only for LJ() state/sizing methods; shared LIZJ category list or identical host is not proven.
- User-visible GIF tab still not mapped to LX/0NKZ.GIPHY; it may be a separate surface or configuration/build difference. Incoming ordinary type-7 TextContent renderer remains unknown.
- Assembler workflow fallback fix committed in 14aa7ee4; awaiting Actions.
## Follow-up #64 — tab-strip adapter
- Native tab strip uses RecyclerView adapter LX/0fuk and LX/0Mi3 page models. fn() selects view type; type 5 uses text holder, other values use icon holder. GIPHY model LX/0Ymq has fn() = 8, so it is an icon tab.
- LX/0Ymq is only a descriptor shell (icon + type; inherited item list is empty), not a working Giphy content page in this APK.
- ICQ can likely be added through the same page-model list, but the inner page/item renderer must be traced before selecting new category vs reusing GIPHY. No APK patch/runtime change.
## Queue #65 — expanded scan blocked before execution
- Incoming analyzer now includes LX/0fuk, LX/0gKt, LX/0gKu and related tab/page models (commit 96354507).
- Runs 45283950048 and 45283950020 failed before any steps, with no logs/artifacts; assembler retry shows same pattern. Likely Actions job startup issue, not a confirmed analyzer bug. Avoid repeated trigger commits.
- Continue using successful artifact 14835435503; no APK patch/runtime change.
## Follow-up #66 — corrected comment-vs-DM panel mapping
- Correction: LX/0fuk tab adapter is only seen in CommentEmojiPanelAssem -> EmojiInputViewImplV2 -> LX/0kRW. Its LX/0kRX list only adds the system-emoji page; it is not the DM Stickers/GIF/Emoji tab strip.
- DM panel host is instead ChatroomEmojiPanelAssem -> service LX/0NKm -> BasicStickerPanelAssemPanelAssem -> BasicStickerPanelAssem / StickerPanelAssemViewModel.
- Expanded incoming scan to include the full generated DM assembly lambdas (AwS563..., AwS531...) and panel state types. Runs 45284240443/45284240484 failed before steps; Actions startup issue persists. No APK patch/runtime change.

## Follow-up #67 — проверка ошибок Actions #54/#55
- Проверен реальный workflow: `.github/workflows/icq-dm-deep-research.yml`; триггеры и job/steps настроены, видимой YAML-причины отказа нет.
- У успешного #53 API показывает шаги 1–6; у #54/#55 `steps=null`, `logs_url=null`, пустой список шагов и нет логов/артефактов. Оба завершились за 2–3 секунды — до checkout и запуска анализатора.
- Значит, ошибки не доказывают проблему Python/DEX-кода; вероятнее всего сбой запуска job/runner на стороне Actions. Точную причину API не показывает.
- Ничего не менял и не запускал новые коммиты. Следующий шаг — один контролируемый retry после проверки доступности Actions. No APK patch/runtime change.

## Follow-up #68 — контролируемый повтор #55
- Один retry failed job выполнен успешно как действие API; attempt 2 получил job ID 138087710861.
- Повтор снова завершился failure примерно за 3 секунды с `steps=null`: до checkout и анализатора выполнение не дошло.
- Сбой запуска job воспроизводится; причина на стороне Actions/runner пока не раскрыта. Повторы остановлены, код/workflow не менял. Нужна проверка состояния Actions в GitHub владельцем репозитория. No APK patch/runtime change.


## Experimental branch handoff (2026-11-25)
- This journal was copied forward from `ultramod/next` into `ultramod/experimental`; earlier entries and caveats are preserved.
- Documentation-only transfer: no APK, patcher, runtime, or source-code changes.
- GitHub Actions capacity is currently exhausted/unavailable. Do not trigger workflows or retries until confirmed available.
- Continue from the last confirmed findings above; distinguish static evidence from assumptions and keep unresolved work visible.


---

## TikTok UltraMod — experimental handoff (2026-11-25)

- `ultramod/experimental` is based on `ultramod/prototype` commit `3fbb0c2b1714a4d6deb1e8553c7df914d7308add`. The two ICQ journals from `next` were preserved and carried forward.
- No TikTok source code was changed; no Actions/build was run because capacity is exhausted. Wait for the user's confirmation before triggering workflows.
- Static APK review (existing prototype artifact, run `44897473511`, APK `tiktok-rv-original.apk`, 57 DEX files):
  - **Seekbar:** current patch forces `LX/06Bt.LJI(Aweme)` to true, bypassing lifecycle/page/story/mask/prohibited checks, and forces every `setSeekBarShowType` call to `0x64`. Strong likely cause of sticky seekbar after navigation. Fix should be selective, not global.
  - **External browser:** two `OpenBrowserMethod` bridges already support `use_external_browser=true` and launch `ACTION_VIEW`; another IAB route exists. Need identify all callers and filter external HTTP(S) hosts while keeping TikTok links/deep links internal.
  - **Profile cover:** `ProfileBackgroundComponent` exists, but profile root registration uses `profile_bg_in_allow_list`; component also requires server data with `hasBackground=true`. Client-side force alone may not unlock accounts lacking backend eligibility/data.
  - **Purple theme:** build-time purple is `#8E44AD`; resource/XML and audited DEX colors are recolored selectively. Cyan/neutral and dynamic/server/image colors are intentionally or structurally outside the pass, explaining partial coverage.
- Artifact build commit `42ab53377a7508a7db0a559ce7599a3dc4604876` is 64 commits behind current prototype. Seekbar patch/fingerprint blobs are identical across `main`, `prototype`, `next`, and `experimental`; recheck app version/fingerprints before next build.
- Next: verify current APK version, refine seekbar safely, trace browser callers, trace profile background API/allowlist, audit remaining accent values. All findings are static; no device runtime confirmation yet.


### Addendum — exact route notes (2026-11-25)
- Browser bridges are `com.ss.android.ugc.aweme.bullet.bridge.framework.OpenBrowserMethod` and `com.ss.android.ugc.aweme.fe.method.OpenBrowserMethod`. Both read `use_external_browser` (default false): true launches ACTION_VIEW/BROWSABLE; false opens `type=webview`. Another separate IAB route is `TtopIABServiceImpl.LIZ`, with URI host/path checks. No global setting found in TikTok Settings.java; Live auto-login strings are unrelated.
- Profile root calls `LX/0Ngv.LJFF` before registering `profile_background`. The `ProfileBackgroundExp` snapshot reads `profile_bg_in_allow_list` per UID. The component separately checks device/performance score and requires `ProfileBgInfo.hasBackground()`; bypassing one gate may not create backend data.
- Seekbar `LX/06Bt.LJI(Aweme)` rejects story immersive/guide cards, not-resumed page, non-current video, no-progressbar, story, inverse experiment, masked video, prompt overlay and prohibited video. Patched player-status handling calls this method before updating show type. The setter patch overwrites every incoming show type with `0x64`.


### Profile banner follow-up (2026-11-25)
- User changed TikTok region to Japan; banner option still absent. Region spoof does not necessarily change public IP or the account-scoped AB allow-list snapshot TikTok's server uses.
- Separate outcomes: editor button visible, editor/upload works, server accepts/persists, profile API returns background. These are not equivalent.
- Public `TikProfileBanner` targets TikTok 47.1.3 only and hooks obfuscated `X.0OSG`; do not copy names into our different APK version. Its README says own profile may not return the saved background; verify from a second account.
- Public Morphe/Metra source has a stronger approach: find the gate structurally using `profile_bg_in_allow_list` and `profile_bg_enable_consumption_group`, preserve the avatar-layout check, and adjust the account snapshot sent with `getSelfUserInfo` / `getOtherUserInfo` requests. Gate-only unlock can open editor but fail to return saved backgrounds. Server may still reject writes.
- Next: inspect exact APK's gate holder + AB snapshot reader/request callers. No source code changed; public hooks are references only, not drop-in patches.


## Profile Background target update #72 (2026-11-25)
- User confirmed UltraMod's TikTok version is **47.9.3** (public release code 2024709030, dated 20 Nov 2026).
- TikProfileBanner is explicitly for 47.1.3; its obfuscated hook is not reusable.
- Morphe/Hushfeed uses structural discovery of the AB gate and account-scoped request snapshot, but its documented obfuscated examples are 47.1.4 / earlier, not verified for 47.9.3. A separate release's general 47.9.3 TikTok target update does not prove Profile Background compatibility.
- Next: inspect the exact 47.9.3 base APK used by UltraMod and re-find the gate, layout check, per-account snapshot reader and profile request callers. Old prototype artifact is not a substitute.
- Documentation only; no source/APK changes, no Actions run. Wait for user confirmation before any workflow.


## Profile Background — 47.9.3 build artifact verified (#73)
- Latest successful prototype build run `44930595699` (2026-11-21, commit `3fbb0c2b`) artifact `14718266818` was inspected read-only. Job log confirms package `com.zhiliaoapp.musically`, versionCode `2024709030`, versionName **47.9.3**. No upload from user needed.
- Workflow uses `VERSION_MODE=auto` with no pinned TikTok version, so a future build can silently pick up a newer version. Always verify version before patch compatibility assumptions.
- Latest subsequent prototype patching run `45287709435` failed and has no artifact.
- Next: statically inspect the downloaded 47.9.3 APK for exact profile-background gate, layout guard, and account-scoped AB snapshot reader. Make the patch fail closed on unknown/unmatched versions; optionally pin 47.9.3 for controlled builds. No source/workflow changes and no Actions run.

## Profile Background — 47.9.3 exact DEX findings (#74, 2026-11-25)
- Inspected the original APK from successful prototype artifact `44930595699`; exact version is 47.9.3 / 2024709030. Read-only only.
- Feature classes and image/video editor/API/repository exist in 47.9.3. `ProfileBgInfo` has `hasBackground()`, `isImage()` and `isVideo()`.
- Exact gate holder `LX/0Ngv` survives. `LIZ()` reads `profile_bg_in_allow_list` under `ProfileBackgroundExp` and uses `getCurUserId()` plus a UID-scoped snapshot/cache path.
- `LX/0Ngv.LJFF(ZZ):Z` is still called by `ProfileRootComponent.Dq` before profile-background component registration. It calls `LIZ()`, `LX/0Bbm.LIZLLL` twice, and another experiment-backed boolean: this is a compound gate, not a safe unconditional-true target.
- `ProfilePlatformViewModel.R93/S93` also call `LIZ()`. Exact propagation into `getSelfUserInfo` / `getOtherUserInfo` request serialization remains to be traced.
- Next: resolve both `0Bbm.LIZLLL` calls and preserve the layout/performance guard; trace the UID snapshot into self/other profile requests. Structural match must be unique and fail closed. No patch/runtime/backend validation, no Actions run.

## Profile Background — request parameters traced in 47.9.3 (#75, 2026-11-25)
- LX/0NhE snapshot = UID + integer profile_bg_in_allow_list value. LX/0Ngv.LIZ() refreshes the snapshot when the current account UID changes.
- R93 passes the snapshot value as profile_bg_in_allow_list to /tiktok/user/profile/self/v1 (getSelfUserInfo).
- S93 passes the same snapshot value as profile_bg_in_allow_list to /tiktok/user/profile/other/v1 (getOtherUserInfo), plus a separate lazy integer as profile_bg_enable_consumption_group.
- API parameter annotations were read from DEX. Self request has only the allow-list key; other request has both keys. Do not treat them as interchangeable.
- LJFF(Z) is compound: it checks the snapshot allow-list and calls LX/0Bbm.LIZLLL() twice. That getter's backing lambda logs isEnableAvatarOpt. It also reads another lazy AB integer LX/12TO.LIZ. Preserve these guards; no unconditional-true patch.
- Server eligibility/save/readback remains unverified. Static analysis only; no source/APK change, no Actions run.
- Next: resolve LX/12TO.LIZ and the LJFF boolean argument, then trace editor save and API response/readback.

## Profile Background — save path and gate refined (#76, 2026-11-25)
- LJFF(Z) requires a valid UID snapshot, allowListValue == 1, and LX/0Bbm.LIZLLL() == true. The latter resolves to isEnableAvatarOpt. Its two branches perform the same check. Static analysis only.
- Correction: LX/12TO.LIZ appears only in a fallback block skipped by the observed constant-true branch; do not treat it as an active Profile Background gate.
- ProfileBgApi.commitProfileBackground posts to /aweme/v1/commit/user/ with profile_bg_type, aweme_id, image_uri, offset_y_ratio. Clear/image/video repository operations all call this endpoint.
- ProfileBgApi.fetchUserPublicVideos uses /aweme/v1/aweme/post/ with user_id, max_cursor, count, scene, filter_private, sec_user_id.
- ProfileBackgroundComponent parses component JSON into ProfileBgBizData and renders from ProfileBgInfo (media type, video/Aweme data, offset, image URL list). Editor availability, backend save, and returned profile data are separate stages.
- Server eligibility/save/readback remains unverified. No patch, runtime test, or Actions run.
- Next: trace editor entry points and response fields into ProfileBgBizData; map profile_bg_type only from verified callsites.

## Profile Background — commit types and response model (#77, 2026-11-25)
- Verified profile_bg_type values from repository call arguments: 0 = clear, 1 = image, 2 = video. All use /aweme/v1/commit/user/.
- ProfileBgInfo.hasBackground() requires mediaType > 0; isImage() checks 1; isVideo() checks 2.
- Response model: ProfileBgBizData has profileBgInfo; ProfileBgInfo has imageUrlList, mediaType, videoInfo; ProfileBgVideoInfo has autoPlay, awemeInfo, offsetYRatio.
- ProfileBackgroundComponent parses component JSON into ProfileBgBizData and renders image/video only from populated ProfileBgInfo. A visible editor or successful local action does not prove server save/readback.
- Backend eligibility and save/readback remain unverified. Static analysis only; no patch, runtime test or Actions run.


## Profile Background — editor path and proposed minimal experiment (#78, 2026-11-25)
- On TikTok 47.9.3, `ProfileBackgroundComponent.Jq()` checks `LX/0Ngv.LJFF(Z)` and opens the native **Profile Background Action Sheet**. Native flows lead to `ProfileBgImagePreviewActivity`, `ProfileBgVideoListActivity`, and `ProfileBgVideoPreviewActivity`; no custom editor is needed.
- `LX/0het` is the repository implementation. It has separate clear/image/video commit methods using the common `ProfileBgApi.commitProfileBackground` endpoint, plus public-video fetching. Static analysis confirms the save plumbing, not server acceptance.
- Response path: component JSON is passed through `ProfileBackgroundComponent.gr/Wr` into `Vr(String)` -> `ProfileBgBizData`; `Er/Br/gs` render only when response `ProfileBgInfo` is populated. A visible editor or commit attempt is not proof of a displayed background.
- Proposed minimal client experiment (not applied): make the AB read for `profile_bg_in_allow_list` in `LX/0Ngv.LIZ()` resolve to 1 before the existing UID-scoped snapshot is created. This feeds both the registration gate and self/other profile request values. Preserve the `isEnableAvatarOpt` check and all unrelated gates; do not force `LJFF` true.
- Remaining uncertainty is server-side eligibility and readback. Test only in a separate experimental APK/account: open editor, save image, reload, verify own and second-account view, then test video/clear separately. Capture server error/response. If backend does not return component data, further UI bypasses are futile.
- Read-only analysis only. No patch, no prototype changes, no Actions run. Stop static research here unless a concrete runtime result identifies a specific gap.


## Profile Background — experimental patch added (#79, 2026-11-25)
- User approved an experimental-only patch. Added `revanced-patches/patches/src/main/kotlin/app/revanced/patches/tiktok/profile/ProfileBackgroundPatch.kt`.
- It narrowly matches `LX/0Ngv.LIZ()LX/0NhE;`, requires one `profile_bg_in_allow_list` key and an integer AB read followed by `move-result`, then writes 1 into that result register before the UID-scoped snapshot is built.
- Preserves `LJFF(Z)` and its avatar-opt/layout checks; does not alter the consumption-group key or other AB values.
- Workflow selects it only for original `com.zhiliaoapp.musically` builds and makes failure fatal. Asia builds are unchanged. The anchor is for the observed 47.9.3 DEX; newer auto-downloaded versions must match or fail closed.
- No Gradle build, Actions run, APK build, or device/backend test was performed. Patch compilation/application is not yet verified. `ultramod/prototype` remains untouched.


## Seekbar refinement (#80, 2026-11-25)
- Reworked the experimental patch: it no longer forces the whole eligibility method true or overwrites every seekbar setter call.
- It bypasses only the inverse `enable_video_seekbar_visible` experiment check, then selects visible type `0x64` only when the native eligibility method accepts the current feed Aweme.
- Native exclusions (story, lifecycle/page state, drag, mask, prompt and prohibited videos) and other setter show/hide modes are preserved.
- Changes are only in `ultramod/experimental`; no Actions/Gradle/APK/device test. Compilation and runtime behavior remain unverified.


## UltraMod settings UI — Russian visible-text pass (#81, 2026-11-25)
- On `ultramod/experimental`, changed the visible entry label to "Настройки UltraMod", translated all audited settings categories/options/dialogs into Russian, and removed visible ReVanced branding from the About screen.
- Download folder radio labels are Russian; persisted folder tokens (`DCIM`, `Movies`, `Pictures`) remain unchanged. All setting keys/defaults and hidden identifiers remain unchanged.
- About now displays UltraMod version/description/Telegram only; no patches version is shown.
- Country ISO and operator MCC/MNC/name remain the existing editable fields; only their visible text was translated. A country/operator picker and visual redesign were intentionally not included in this text-only pass.
- `ultramod/prototype` untouched. No Actions, Gradle, APK build, install, or runtime test; source compilation remains unverified.


## UltraMod custom shell — first implementation (#82, 2026-11-25)
- Added a programmatic dark/purple UltraMod home screen with branded hero, navigation cards, and separate pages for feed, downloads, region, and additional settings.
- Reused existing Setting objects and keys; toggles persist through Setting.save(), and reboot-required settings show a localized Russian restart prompt. Download path tokens remain unchanged.
- Added a searchable ISO country selector (Russian country names). It currently updates only the country ISO; MCC/MNC and operator name are still manual. No operator dataset or automatic country/operator mapping yet.
- Updated the active activity hook to launch the new shell. Legacy preference classes remain present but inactive.
- Experimental branch only. No build, Actions, APK, install, or runtime test; compilation and UI behavior are unverified.


## MCC/MNC directory research (#83, 2026-11-25)
- Identified mcc-mnc.org `networks.json` as a candidate operator source: 2,679 records, fields include ISO, MCC, MNC, operator and brand; data is stated to be free to use with attribution.
- This could populate the existing region settings as a country -> operator selection flow. Dataset not yet imported or validated; no automatic operator picker has been implemented.


## UltraMod #84 — shell audit
- Static review of `UltraModSettingsFragment`, activity hook and settings definitions completed on `ultramod/experimental`.
- Fixed stale region-row display after manual MCC/MNC or operator-name save; region page now refreshes immediately.
- The candidate mcc-mnc.org dataset has 2,679 records and explicit attribution terms, but full JSON could not be materialized intact. A 3,386-entry GitHub mirror was reviewed but not imported because its aggregated data provenance/license needs clarification.
- Country -> operator picker is still pending. Preserve MNC as text (including leading zeros) and do not claim automatic operator selection is available.
- No build/Actions/APK/runtime verification. `ultramod/prototype` untouched.
