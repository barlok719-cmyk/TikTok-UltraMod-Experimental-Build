

## Run #53 — generic DM slot and protocol provider inventory (2026-11-23)
- Deep workflow `45171868724` completed successfully; artifact `14816838925` downloaded and inspected. Analyzer commit: `3c2bf3abcd93d548a2e18b27e4617099071de8fe`; workflow commit: `61c7150f6dd4cf3fdc0833f9c5939d1dc3fb4bb5`. Static DEX inspection only.
- The slot model is now concrete: `LX/0NAA` is `BaseSkeletonData(message, skeletonLayoutPowerCellData, slotsData)`; its `LIZJ` is a map from `LX/0N9i` slot keys to `LX/0NAL` values. `LX/0NAL` is a generic `SlotContent(data, shouldAssemble)` wrapper. `LX/0NBI.LLJZIJLIL()` exposes this base skeleton data. This confirms the generic slot-content handoff from Run #52.
- The inventory lists 32 concrete classes implementing `IMMessageListProtocol`, mostly explicit specialized message/card protocols. The public `MessageListProtocolEnum` includes `TEXT_BIG_EMOJI`, `TRANSLATE_TEXT`, `MARKDOWN_MSG`, GIF and many card/system protocols, but no ordinary `TEXT`/plain-text protocol enum. This strengthens (but does not prove) that ordinary text uses a separate default/legacy route rather than a named special protocol.
- `LX/0N9i` itself is only an interface exposing integer `LIZIZ()`; this report does not resolve its concrete key implementations or the key-to-content-assembler mapping. The dumped `SkeletonMessageListProtocol` is a generic contract for send/receive skeleton models and PowerCells, not the missing ordinary text implementation.
- Crucially, the report still does not identify a standard incoming type-7 `TextContent` slot, a concrete plain-text receive assembler, or a visible text setter. We must not patch the slot host or assume `0N8x` is called for ordinary text.
- Next focused pass: enumerate every concrete implementation of `LX/0N9i` and `SkeletonMessageListProtocol`, find all constructors/registries that populate `0NAA.LIZJ`, resolve integer slot IDs to content component classes, then follow the ordinary message's type-7 mapping into its receive binder. DM only; comments remain out of scope.
- No APK, patcher, payload, or runtime behavior changed. No backend/two-device validation.


## Deep research #54 — concrete slot keys and protocol inventory (2026-11-23)
- Workflow `ICQ DM Slot Key Registry Research`, run `45175594507`, completed successfully; artifact `14818736785`.
- Corrected analyzer regex escaping before this run. Verified it scanned 491,343 classes and 2,107,378 methods (unlike the prior empty report).
- Found 10 concrete implementations of `LX/0N9i`; each returns an Android resource/view ID. IDs: `0x7f0a09d4`, `0x7f0a6b80`, `0x7f0a53a8`, `0x7f0a51c1` (shared by two keys), `0x7f0a1520`, `0x7f0a9d1d`, `0x7f0a3a9b`, `0x7f0a4cb3`, `0x7f0a51a9`.
- Found 40 concrete `SkeletonMessageListProtocol` implementations. Their send/receive PowerCell mappings and protocol enums are specialized (cards, media, voice, GIF, translation, big emoji, markdown, etc.); no generic plain-text/TEXT protocol or ordinary text receive PowerCell appears in this inventory.
- Confirmed `LX/0NAD.LIZIZ(BaseSkeletonData, LX/0N9p, Object, boolean)` clones the slot map and inserts `SlotContent` under a concrete slot key. Many calls use `LX/0N9p`, but current report does not identify the ordinary-text value producer.
- This narrows the architecture but does NOT yet prove the standard incoming text renderer or its binding. No APK patch or runtime changes; no device test.
- Next: resolve the 10 resource IDs to resource names, inspect all key consumers/producers and generic fallback path, then correlate with incoming type-7 TextContent.


## Deep research #55 — decoded DM slot resource IDs and consumer mapping (2026-11-23)
- Workflow `ICQ DM Slot Key Registry Research`, run `45179303287`, completed successfully; artifact `14820967816`. Both resource decoding and DEX enumeration steps succeeded.
- Decoded APK `public.xml` and mapped all 9 unique IDs for the 10 `LX/0N9i` key implementations:
  - `LX/0N9j` -> `id/bv8`; concrete consumers include `AvatarReusedSkeletonUIContent`, `GroupWithStoryAvatarReusedSkeletonUIContent`, and `BotAvatarReusedSkeletonUIContent` (avatar slot).
  - `LX/0N9v` -> `id/user_name_stub`; consumer `UsernameReusedSkeletonUIContent` (username slot).
  - `LX/0N9t` -> `id/pax`; consumers `SingleChatMessageStatusReusedSkeletonUIContent` and `GroupChatMessageStatusReusedSkeletonUIContent` (message-status slot).
  - `LX/0N9s` -> `id/ocy`; consumer `ReactionReusedSkeletonUIContent` (reaction slot).
  - `LX/0N9r` -> `id/u53`; consumers include `QuotedAreaReusedAssem`, `QuotedAreaReusedAssem2`, and `QuoteAreaContainerAssem` (quote slot).
  - `LX/0N9q` -> `id/po3`; consumer `NoticeReusedSkeletonUIContent` (notice slot).
  - `LX/0N9o` and `LX/0N9t` both map to `id/pax`; `LX/0N9o` is consumed by `InlineLabelReusedSkeletonUIContent`, while `LX/0N9t` is message status. Shared resource ID is an actual alias/overlap; do not treat names as a unique semantic guarantee.
  - `LX/0N9u` -> `id/dze`; consumer `ReportPageUIContentAssem` (report-page slot).
  - `LX/0N9p` -> `id/kzq`; consumer set is predominantly side-slot/card/media/voice transcript/streaming controls, plus pinned-message slot; not plain text.
  - `LX/0N9n` -> `id/pa_`; consumer `FeedBackReusedSkeletonUIContent` (feedback slot).
- `LX/0N9g` is a registry that constructs the ten key singletons. `LX/0YjV` groups a subset of six keys used for a separate slot set; neither exposes an ordinary message text binder.
- This run maps the auxiliary slot architecture, but still does not identify a standard incoming type-7 `TextContent` view or the final `TextView/Spannable` setter. The resource name `user_name_stub` is a layout ID, not evidence of message text rendering.
- Next research target: inspect the generic/default skeleton path and the message-to-`BaseSkeletonData` producer, especially all `LX/0NAD.LIZIZ` callsites and `LX/0N9p` content values; separately scan the actual view/layout binding for ordinary `TextContent` and `msgType=7`. Keep DM-only scope. No APK patch, runtime modification, or device test.


## Deep research #56 — focused TextContent and view-setter scan (2026-11-23)
- Workflow `ICQ DM Slot Key Registry Research`, run `45184344925`, completed successfully; artifact `14822598445`.
- Analyzer commit: `76dc9cd080290c78b532c6e8cb523c3034b32961`. The run decoded resources and scanned all DEX files; report now includes dedicated sections for DM TextContent access and DM text setter/Spannable methods.
- 76 method blocks mention TextContent, but most are the model's own constructors/accessors/copy/serialization or analytics and unrelated uses. Relevant UI accesses remain limited to `TextBigEmojiAssem.Xq` (big-emoji receive path) and `TextTranslateAssem.Yq/cr` (translation path). `SendMessageAssem.Pq` and `SendMessageVM` are outgoing path. No ordinary incoming type-7 TextContent view binder surfaced.
- The apparent generic `TextView.setText` in `IMSkeletonLayoutPowerCell.W5` is not message-body rendering: method binds message UUID, scene, and msgType into view tags, and handles tracker/light-interaction setup. The setter hit came from a generic method inventory and must not be treated as the chat text binder.
- Other text setters in the report belong to comments, usernames, translation/big-emoji, sticker-panel labels, voice, cards, or other unrelated UI. No direct incoming standard-text TextView/Spannable binding established.
- User's manual UI observation (2026-11-23): the composer/picker surface appears identical in DMs and comments; tapping the sticker icon opens one panel with tabs for Stickers, GIF, and Emoji. Treat as a valuable lead, not yet source-confirmed. The next pass should inspect shared sticker-panel host/tab registration and its callers in DM/comment composer, while continuing to trace ordinary DM receive-text fallback separately. Desired ICQ tab placement: adjacent to Emoji tab; no implementation yet.
- No APK patch, runtime modification, or device test. DM receive renderer remains unidentified.


## Deep research #57–#58 — shared sticker panel / service / comment composer trace (2026-11-23)
- Workflow `ICQ DM Slot Key Registry Research`, run `45194114885`, completed successfully; artifact `14827906782`. Static DEX inspection and resource decode completed.
- Expanded inventory found 81 candidate classes and 379 candidate methods across sticker, chat input, comment and feed-reply packages. Relevant concrete classes include `BaseEmojiPanelAssem` (base class of chat emoji panel), `ChatroomEmojiPanelAssem` (DM/chat emoji panel), `ChatRoomPanelAssemV2` (panel container), sticker implementation classes, and `DetailFeedEmojiPanelFragment` (feed comment/reply emoji panel).
- Confirmed `ChatroomEmojiPanelAssem` extends `BaseEmojiPanelAssem`. Its method `Iq(ViewGroup, int, boolean)` reads singleton `LX/0NKm.LIZ:LX/0NKn` and calls `LX/0NKn.LIZ(ViewGroup, Function0)`. This is a concrete service/callback integration point for the chat emoji panel. `IMStickerApi.getImStickerInputPanelService()` also returns `LX/0NKm`.
- The expanded report did NOT yet resolve the actual class definitions for `LX/0NKm` / `LX/0NKn`, nor identify the code that builds/registers the visible Stickers / GIF / Emoji tabs. The cross-reference section lists these descriptors but contains no resolved definition bodies. Therefore do not describe the service's internal behavior or the tab registry as proven.
- `DetailFeedEmojiPanelFragment` is confirmed as a separate feed comment/reply emoji panel class. This is not proof that comments and DMs share the same panel instance or tab host. User's visual observation that the picker looks the same remains a useful lead; source-level shared implementation is still unconfirmed.
- A follow-up analyzer correction was committed to include the broader `/im/sticker/impl/` and `/im/sticker/api/` packages and to fix obfuscated descriptor matching. Workflow run `45198991854` was queued at the time of this log update; inspect its artifact before drawing further conclusions.
- No APK patch, runtime modification, or device test. Next: resolve definitions/implementations for `LX/0NKm` and `LX/0NKn`, inspect `BaseEmojiPanelAssem` and `ChatRoomPanelAssemV2` for tab models/adapter/pager, then trace the caller/parent of `DetailFeedEmojiPanelFragment` and compare the panel setup. Desired result remains an ICQ tab adjacent to Emoji in the existing picker.


## Deep research #59 — corrected slot scan and fast panel index (2026-11-23)
- Follow-up workflow ICQ DM Slot Key Registry Research, run 45198991854, completed successfully; artifact 14831826719. Companion fast-index workflow 45198991407 also completed successfully; artifact 14830271928. Both used commit 3c0a18e9b2077335ddc496f96cec806597c2796a.
- The corrected deep report still lists LX/0NKm and LX/0NKn only as cross-referenced descriptors. It does not contain their class definitions or method bodies. Thus the wider package scope and descriptor-matching fix did not resolve these service types; the earlier assumption that their definitions would be present in the APK is not confirmed.
- The fast index independently confirms ChatroomEmojiPanelAssem in classes41.dex (extends BaseEmojiPanelAssem) and ChatRoomPanelAssemV2 in classes5.dex (extends BasePanelContainerAssem). The latter is a credible DM input-panel container to inspect, but its constructor only establishes InputFieldVM ownership; it does not establish the tab registry.
- The fast index identifies DetailFeedReplyMessageFragment.onViewCreated as a feed-reply composer lead. It is a different class from the DM chat panel, and no shared host or shared tab adapter is proven yet.
- The deep scan includes IMStickerApi.getImStickerInputPanelService(): LX/0NKm and the DM panel call LX/0NKn.LIZ(ViewGroup, Function0), but without service definitions these remain interface/call-site evidence only. Do not infer that this callback itself adds a tab.
- Status: picker integration point is narrowed to the DM panel service call and panel/container classes, but Stickers/GIF/Emoji tab construction and a reusable DM/comment host remain unresolved. No patch or runtime change.
- Next panel pass: inspect the full BaseEmojiPanelAssem, ChatroomEmojiPanelAssem, and ChatRoomPanelAssemV2 methods around view creation, adapter/pager setup, and panel state; trace DetailFeedReplyMessageFragment to its actual picker fragment/host; search all APK DEX files for the exact service descriptors as class definitions and references separately (including multidex split/package boundary possibilities). In parallel, resume the separate incoming ordinary type-7 TextContent task through the generic/default message-cell path; do not let panel research replace it.


## Research queue #60 — parallel panel and incoming-text scans launched (2026-11-23)
- Expanded `analyze_dm_assembler.py` to include DM sticker-panel hosts, panel service references, feed-reply composer entry points, and pager/tab-related signatures. This triggered assembler workflow run `45201799460` (`ICQ DM Skeleton Assembler Research`), which was still in progress at handoff.
- Expanded `analyze_dm_stickers.py` with additional ordinary-message adapter/view-holder/default receive candidates and dispatch signatures (`onBindViewHolder`, `onCreateViewHolder`, `getItemViewType`, `mapToPowerItem`, `BaseSkeletonData`, `TextContent`, and slot model references). Incoming pipeline workflow run `45203141225` (`ICQ DM Incoming Pipeline Research`) was confirmed in progress. Companion fast index run `45203141263` completed successfully.
- These are two independent static DEX research passes: panel/tab registration and ordinary incoming text fallback. No APK patch or runtime changes. Reports have not yet been inspected; conclusions are pending artifacts.
- Next: inspect both deep workflow artifacts after completion, identify concrete methods/callers, and update the journal with findings. Keep the ordinary incoming type-7 TextContent renderer as a separate tracked task; do not declare it solved based on generic adapter hits.
## Deep research #61 — incoming DM pipeline and sticker-panel service discovery (2026-11-24)
- Incoming workflow runs 45203141225 and 45203198094 completed successfully; artifacts 14835435503 and 14833402957 were inspected. Both reports are static inspection of the same 57-DEX APK; no APK modification or runtime validation.
- Panel discovery: LX/0NKm is defined in classes31.dex. It exposes LIZJ(): List<LX/0NKZ> for sticker-panel page types and LJIIJJI(..., LX/0NKQ) for handing a panel configuration to a UIAssem.
- Enum LX/0NKZ in classes32.dex explicitly contains SYSTEM_EMOJI, CUSTOM_EMOJI, FAV_STICKER, POPULAR_STICKER, GIPHY, SET_STICKERS, CURATED_SETS, AIMOJI, AISELF, and STREAK_STICKER. This confirms native page models for Emoji, Giphy and sticker categories; it does not prove which are visible in every configuration.
- Concrete implementation LX/0kOo (classes22.dex) implements LX/0NKm. Its LIZJ() returns four page types in order: FAV_STICKER, then CUSTOM_EMOJI (or SYSTEM_EMOJI when the feature check is false), then CURATED_SETS, then SET_STICKERS. GIPHY is a separate enum page type and is not in this returned list; its exact registration/visibility path remains to trace.
- DM builder lambda LX/0NFB calls getImStickerInputPanelService().LIZJ(), falls back to LX/0NKa.LIZ() if the list is empty, builds LX/0NKQ config, and calls LJIIJJI(0x7f0a5b69, ..., ChatroomEmojiPanelAssem, config). This is a concrete service-to-DM-panel configuration path. LX/0kOo.LJIIIZ(BasePanelContainerAssem) assembles BasicStickerPanelAssemPanelAssem into the supplied container.
- Comment-side reuse is source-supported at the service layer: CommentStickerPanelAssem routes MEMORY_COMMENT_AND_DM mode through the same IMStickerApi.getImStickerInputPanelService() and its LJ() service method (including panel sizing/state access). This supports shared service/configuration in the combined comment-and-DM mode, but does not prove every comment composer uses the identical fragment instance or tab host.
- LX/0NKn.LIZ(ViewGroup, Function0) wraps the sticker_panel tracking event and callback; it is not the tab registry.
- Ordinary incoming type-7 TextContent remains unresolved. The broad candidate inventory mostly contains auxiliary slots, previews, comments, translation and special emoji components; no standard incoming text bubble binder was proven.
- Assembler workflow 45201799460 failed because its report formatter indexed x[8] while the class tuple had eight fields. The analyzer was corrected in commit 81ca0002b3e257b6db248dc1eb19f9b1698ab882 to retain the panel flag and use the correct tuple shape. Triggered runs 45281885999 and 45281885930 failed immediately without usable job logs/artifacts; retries also failed. The source fix is committed, but a successful corrected full panel report is still pending.
- No APK patch, runtime modification, or device test. Next: rerun assembler scan when Actions is available; trace LX/0NKZ.GIPHY registration/visibility and how top-level Stickers/GIF/Emoji maps to page models; inspect BasicStickerPanelAssemPanelAssem and DM/comment panel config. Continue separate ordinary incoming type-7 renderer hunt.
## Follow-up #62 — GIPHY page mapping and fallback registry (2026-11-24)
- Further inspection of the successful incoming report found the full page-model dispatch in StickerPanelAssemViewModel (classes10.dex). It iterates the LX/0NKZ list supplied in LX/0NKQ and maps enum values to concrete page-model objects using the generated LX/0hSx switch table.
- Confirmed mapping: SYSTEM_EMOJI -> LX/0MiB; CUSTOM_EMOJI -> LX/0hSs; FAV_STICKER -> LX/0Ymr; POPULAR_STICKER -> LX/0Ykv; GIPHY -> LX/0Ymq. Other enum values map to their own AI/set/streak handlers.
- This proves GIPHY has a concrete page-model branch in the same native sticker-panel view-model; it is not merely a dead enum label. However, the two lists directly observed so far do not include GIPHY: LX/0kOo.LIZJ() returns favourites + custom/system emoji + curated sets + set stickers; LX/0NKa.LIZ() fallback returns favourites + popular stickers + custom/system emoji + AIMOJI + optional AISELF/STREAK + curated/set stickers. Therefore the exact runtime path that inserts GIPHY into the active DM list is still unresolved.
- This narrows the next panel task to locating the active IMStickerApi service provider/configuration that supplies a list containing GIPHY, plus confirming whether the user's visible GIF tab corresponds to LX/0NKZ.GIPHY or a separate top-level panel.
- No APK patch, runtime modification, or device test.
## Deep research #63 — GIPHY page-model dispatch and active DM page list (2026-11-24)
- Further inspection of successful incoming report artifact 14835435503 (57 DEX files) traced the GIPHY branch into StickerPanelAssemViewModel.N93(List). The method switches each LX/0NKZ category to a concrete LX/0Mi3 page model; the GIPHY case returns singleton LX/0Ymq.
- LX/0Ymq is a real page-model object extending LX/0Mi4. It returns fn() = 8 and an icon drawable resource 0x7f010828; its inherited item-list method returns an empty list. This proves a registered page-model slot exists for GIPHY, but does not by itself establish a functioning GIF feed or the visible GIF tab.
- DM panel builder LX/0NFB obtains getImStickerInputPanelService().LIZJ(), falls back to LX/0NKa.LIZ() only if empty, then passes that category list in LX/0NKQ to ChatroomEmojiPanelAssem. BasicIMStickerImpl provides the service through LX/0kOo; LX/0kOo.LIZJ() returns FAV_STICKER, CUSTOM_EMOJI or SYSTEM_EMOJI, CURATED_SETS, SET_STICKERS — no GIPHY. Fallback implementation LX/0NKo returns an empty list. Thus GIPHY is supported by the view-model switch but is not in the standard service's default DM category list in this APK.
- BasicStickerPanelAssem is the page host: it consumes StickerPanelAssemViewModel and uses a pager-like ViewPager2 surface; its content layout is 0x7f0d11d2. BasicStickerPanelAssemPanelAssem is its parent panel assembler with layout 0x7f0d11d3 and an LX/0NKL panel-state VM. This narrows the likely extension surface to the service category list, view-model category->page-model mapping, and page model/cell rendering. No code was changed in the APK.
- Correction to #61 wording: CommentStickerPanelAssem in MEMORY_COMMENT_AND_DM mode calls the same IMStickerApi input-panel service for the LJ() subservice (state/sizing/scroll/sticker-set state). Its inspected methods do not call LIZJ() or receive LX/0NKQ. Therefore shared service usage is confirmed, but shared page-list configuration or identical DM/comment tab host is NOT proven. User's observation of visually similar pickers remains a separate visual lead.
- The visible GIF tab reported by the user is not yet mapped to LX/0NKZ.GIPHY in this APK. Possibilities remain: a separate GIF surface, a feature/configuration path not present in the inspected provider list, or a version/build difference. Do not assume it is the GIPHY page without tracing the actual UI event and layout.
- Incoming ordinary type-7 TextContent remains unresolved. The targeted type-7/TextContent report still contains only PinnedMessagePreviewFactory as an explicit type-7+TextContent candidate, and that method creates a pinned-message preview, not a chat bubble. No standard incoming TextView/Spannable binder has been established.
- To recover the failed full assembler scan, workflow .github/workflows/icq-dm-assembler-research.yml was updated in commit 14aa7ee4ad47bd1749df8a1f7978140172a53c0e: increased successful-build lookup from 20 to 100 and added a verified fallback to retained build run 44996357364 (artifact 14746860531 was checked and is not expired). Awaiting the resulting Actions run; no APK patch/runtime changes.
## Follow-up #64 — native tab-strip adapter traced (2026-11-24)
- The top tab strip is backed by adapter LX/0fuk (classes18.dex), a RecyclerView adapter over List<LX/0Mi3>. getItemViewType() returns each page model's fn(); onBindViewHolder passes the LX/0Mi3 model into the matching tab holder.
- For view type 5, LX/0fuk creates text holder LX/0gKt; for other types it creates icon holder LX/0gKu. LX/0gKt binds getName() and special type-5 label text. Thus GIPHY model LX/0Ymq (fn() = 8) would be rendered through the icon-tab path using its in(Context) drawable.
- This proves the page-model list is also the native tab-strip data source: adding a model to the configured category list can create a corresponding top-level tab. It does not yet prove the inner page's sticker-data rendering or a GIF feed implementation.
- LX/0Ymq only overrides fn(), gn(), and in(Context); its inherited LX/0Mi4 item-list methods return null/empty defaults. It is therefore an icon/page descriptor shell in this APK, not a complete Giphy content implementation by itself. The only explicit GIPHY mapping callsite found is StickerPanelAssemViewModel.N93.
- Practical ICQ direction narrowed: reuse the native tab strip by supplying a new page model or repurposing the GIPHY page descriptor, then implement the page's item/render path. A new enum category requires updating the view-model switch mapping; reusing GIPHY avoids enum extension but requires replacing its icon/page model behavior. Do not choose between these until the page adapter/content binding is traced.
- No APK patch, runtime modification, or device test.
## Research queue #65 — tab-strip classes added to incoming scan; Actions runner issue (2026-11-24)
- Expanded tools/icq_smiles/analyze_dm_stickers.py in commit 963545075a45b3085e5130a07d67ac56d8b0349c to include native tab-strip adapter/view-holder and page-model infrastructure: LX/0fuk, LX/0fuf, LX/0gKt, LX/0gKu, LX/0kRW, LX/0kRX, LX/0Mi4, LX/0NKL, LX/0NKM, LX/0NKX, LX/0NKY.
- GitHub created incoming workflow run 45283950048 and fast index run 45283950020 for that commit, but both failed before executing any steps: job records have empty step arrays, no logs URL, and no artifacts. The assembler workflow retry 45283740504 has the same pre-step failure shape. This is not evidence of a source/analyzer exception; runner/job startup is currently unavailable or failing before step execution. Do not keep making no-op commits just to trigger Actions.
- The prior successful incoming artifact 14835435503 remains the inspected source of the confirmed tab-strip details. Continue analysis from that artifact until Actions can execute again; no APK patch/runtime changes.
## Deep research #66 — comment emoji adapter separated from DM sticker host (2026-11-24)
- Important correction to #64: adapter LX/0fuk is not yet proven to be the DM sticker tab-strip adapter. Its only constructor call in the inspected DEX is inside CommentEmojiPanelAssem.onCreateView, which creates EmojiInputViewImplV2 and then LX/0kRW. This is a comment-keyboard path.
- LX/0kRX, created by LX/0kRW, only adds LX/0MiB (system emoji page) when the config flag is enabled; it does not build the user's full Stickers/GIF/Emoji picker. Thus the LX/0fuk/LX/0gKt/LX/0gKu adapter chain is a comment emoji-input UI and must not be presented as the DM tab strip.
- The DM path is separately confirmed: ChatroomEmojiPanelAssem calls the input-panel service; LX/0kOo.LJIIIZ(BasePanelContainerAssem) assembles BasicStickerPanelAssemPanelAssem; that parent hosts BasicStickerPanelAssem and its StickerPanelAssemViewModel. This is the DM panel path to trace for the visible tabs.
- BasicStickerPanelAssemPanelAssem.onViewCreated uses a generated lambda AwS563S0100000_19_I1 with selector 0x99, while BasicStickerPanelAssem constructs AwS531S0100000_19_I1 callbacks. Their full bodies were truncated in the existing report. The incoming analyzer was expanded to include these lambda classes and raise their source limit to 10,000 lines, and to include LX/0NKL/LX/0NKM/LX/0NKX/LX/0NKY.
- Runs 45283950048/45283950020 and subsequent runs 45284240443/45284240484 failed before any workflow steps (job records have no steps/logs/artifacts). This is a repeated Actions startup failure, not a confirmed analyzer runtime error. Avoid more trigger-only commits; wait for Actions availability or use the retained DEX report.
- No APK patch/runtime modification/device test. Ordinary incoming type-7 TextContent binder remains unresolved.

## Follow-up #67 — диагностика ошибок Actions #54/#55 (2026-11-24)
- Проверены фактические workflow-файлы: ошибки #54/#55 принадлежат `.github/workflows/icq-dm-deep-research.yml` (workflow name: ICQ DM Incoming Pipeline Research). Файл корректно найден в репозитории; триггеры push по ветке `ultramod/next` и workflow_dispatch присутствуют, job `inspect-incoming` задан на `ubuntu-latest`, шаги checkout/APK lookup/toolchain/analyzer/upload перечислены.
- Сопоставление с успешным запуском #53 (45203198094): у успешного job есть полный список шагов 1–6 и post-checkout. У #54 (45283950048) и #55 (45284240443) API возвращает `steps: null`, `logs_url: null`; отдельный запрос шагов даёт пустой массив, логи отсутствуют (GitHub BlobNotFound), артефактов нет.
- Оба неуспешных запуска были созданы и завершились за 2–3 секунды, до первого шага. Изменения analyzer в коммитах 96354507 и 2b39f1e не исполнялись, поэтому эти ошибки не доказывают Python/DEX-анализаторную ошибку. По доступным данным наиболее вероятен сбой создания/старта job на стороне GitHub Actions/runner; точная внутренняя причина не раскрывается доступными API.
- YAML/триггеры incoming workflow не дают видимой причины отказа. Код и workflow не менялись; новых триггерных коммитов не делали. Следующий безопасный диагностический шаг — один контролируемый повтор failed job после подтверждения доступности Actions, либо проверка статуса Actions/runner в интерфейсе GitHub владельцем репозитория. Не запускать серии повторов.
- No APK patch/runtime modification/device test.

## Follow-up #68 — контролируемый retry запуска #55 (2026-11-24)
- Выполнен один повтор только failed job последнего запуска #55 через GitHub Actions rerun job; API подтвердил `success: true`.
- Повтор получил attempt 2 и новый job ID 138087710861, но снова завершился failure примерно за 3 секунды. У повторного job `steps: null`; запуск не дошёл до checkout/первого шага.
- Это подтверждает, что ошибка воспроизводится на уровне старта job, а не связана с содержимым анализатора, который не был запущен. Точная причина остаётся неизвестной из-за отсутствия шагов и логов.
- Дальнейшие автоматические повторы остановлены. Нужна проверка страницы Actions/runner и настроек/статуса GitHub со стороны владельца репозитория либо восстановление инфраструктуры Actions; изменения workflow или Python сейчас не обоснованы.
- No APK patch/runtime modification/device test.


## Experimental branch handoff — 2026-11-25
- The two ICQ research journals were carried forward from `ultramod/next` into `ultramod/experimental` without rewriting or removing earlier findings.
- This handoff is documentation-only. No APK, patcher, runtime behavior, or source code was changed as part of the journal transfer.
- GitHub Actions capacity is currently exhausted/unavailable for this project. Do not trigger workflows or retry failed jobs until the user confirms capacity is available.
- Continue ICQ research from the last confirmed findings above. Keep hypotheses separate from confirmed DEX evidence; preserve corrections and unresolved items in subsequent entries.


---

## Research #69 — TikTok UltraMod experimental handoff (2026-11-25)

### Branch and safety state
- Working branch: `ultramod/experimental`, initially branched from `ultramod/prototype` at `3fbb0c2b1714a4d6deb1e8553c7df914d7308add`.
- The two pre-existing ICQ journals from `ultramod/next` were copied into this branch with their earlier entries preserved.
- No TikTok source/patch files were changed during this investigation. No GitHub Actions workflow or build was started; Actions capacity is unavailable/exhausted. Do not trigger workflows or retries until the user confirms capacity has returned.

### APK evidence used
- Retrieved the existing `tiktok-ultramod-prototype` workflow artifact from run `44897473511` (artifact `14704629262`, created 2026-11-21). It contains `tiktok-rv-original.apk` (the original-accent build variant) and the color-audit reports.
- The artifact was built at commit `42ab53377a7508a7db0a559ce7599a3dc4604876`; current prototype commit `3fbb0c2b1714a4d6deb1e8553c7df914d7308add` is 64 commits ahead. The intervening diff is concentrated in accent tooling/workflows and build configuration; the seekbar patch files themselves are unchanged. Reconfirm the TikTok app version against the next available build before shipping bytecode fingerprints.
- Static inspection covered the APK's 57 DEX files. This is static evidence only; no runtime test on a device has been performed.

### Seekbar — confirmed over-broad patch
- `ShowSeekbarPatch.kt` has the same blob SHA in `main`, `ultramod/prototype`, `ultramod/next`, and `ultramod/experimental`: `7c8a0e914ef711152f402d727544ee90ead15cac`. `Fingerprints.kt` is also identical across all four branches.
- In the inspected APK, the target method is `LX/06Bt.LJI(Aweme):Z` in `classes53.dex`. It explicitly rejects multiple contexts: immersive story feed, story guide card, page state 1/not resumed, state 2/cannot drag, state 3/not current Aweme, state 4/cannot show progress bar, state 5/story video, state 6/inverse experiment, state 7/video mask, state 14/prompt overlay, and prohibited videos.
- Current patch calls `returnEarly(true)`, bypassing all of those checks, not merely the ordinary-video progress-bar restriction.
- The second patch targets `LX/05tI.setSeekBarShowType(I)V` and overwrites its incoming type register with `0x64`. This affects every call to that setter, including calls that would otherwise request another native show/hide mode.
- The feed seekbar controller also receives lifecycle, video-change, player-status, and page-state events. Player-status handling calls the patched eligibility method before updating show type. This makes the forced-true + forced-type combination a strong static explanation for a bar reappearing or remaining visible after navigation. Runtime confirmation is still required.
- Safer direction: preserve resume/page/story/mask/prohibited checks and bypass only the specific native restriction that prevents the desired ordinary-video seekbar. Do not globally overwrite every show type. No code change made yet.

### External browser — candidate hook identified
- No TikTok-specific external-browser patch or preference was found in the repository's TikTok patch/settings sources.
- The inspected APK contains two bridge implementations named `OpenBrowserMethod`. Their methods read a JSON `url` and optional `use_external_browser` boolean. When true, they create an `ACTION_VIEW` intent with `CATEGORY_BROWSABLE`; otherwise they request an in-app `webview` container.
- This is a promising hook for a user setting, but it only covers links routed through these bridges. Another IAB route exists (`TtopIABServiceImpl`), and the APK also contains Custom Tabs handling and an `IAB_FORCE_EXTERNAL_BROWSER` review result.
- Any implementation must filter by URI scheme and host: keep TikTok profile/video/deep links and app schemes internal; open only genuinely external HTTP(S) hosts in the system browser. Need to map callers/entry points before patching. No code change made.

### Profile background / wide profile cover — component and gates found
- The APK contains `com.ss.android.ugc.profile.platform.business.background.ProfileBackgroundComponent`, with `ProfileBgBizData`, `SmartImageView` and multiple layout containers. It is the relevant profile-background component, distinct from generic promotional profile banners.
- `ProfileRootComponent.Dq()` registers the `profile_background` component only when `LX/0Ngv.LJFF(...)` allows it. The related experiment snapshot reads `profile_bg_in_allow_list` from `ProfileBackgroundExp`.
- The component then requires `ProfileBgBizData.getProfileBgInfo().hasBackground()` before drawing the background. Video backgrounds have an additional performance/device check using the `profile_background` feature score.
- The APK also contains a profile-background crop/edit flow and `ProfileBgImagePreviewActivity`. This confirms client-side UI exists, but availability appears gated by account/experiment and server-provided background data. Forcing component registration alone may show nothing if the response contains no background data or the server does not permit setting one.
- Next research: trace the profile API response/model and the exact entry gate before deciding whether a safe client-side unlock is possible. No code change made.

### Purple accent — reason coverage is partial
- The workflow exposes a build-time accent preset; purple resolves to `#8E44AD`. TikTok's extension settings do not contain a runtime accent toggle; this is selected at build time.
- Resource pass recolors typed color values in `resources.arsc` and compiled XML attributes. DEX pass only rewrites full-width `const` instructions whose original colors are present in the audited resource-pair map.
- The palette intentionally preserves alpha and excludes cyan/neutral colors; tests explicitly assert cyan is not recolored. Colors outside the red/magenta family, dynamically supplied/server colors, bitmap/vector artwork not represented as typed color values, and other runtime-generated values may remain unchanged.
- Therefore partial purple coverage is consistent with the current selective recoloring design; it is not evidence that the purple preset itself failed. More precise coverage requires an APK color audit and tracing remaining values, not a blanket replacement of all integers.

### Next steps
1. Confirm the exact TikTok version in the next available prototype APK and refresh fingerprints if needed.
2. Design a selective seekbar fix that retains native lifecycle and content exclusions.
3. Trace the external-link bridge callers and add an opt-in preference plus safe host/scheme filtering.
4. Trace profile-background response data and allow-list semantics; do not force the gate without understanding backend behavior.
5. Use the existing color audit to identify remaining non-purple UI values and classify their source before extending the palette.


### Addendum #70 — exact DEX routes and guard conditions (2026-11-25)

#### Browser bridge details
- Bullet bridge class: `com.ss.android.ugc.aweme.bullet.bridge.framework.OpenBrowserMethod`. Its `handle(JSONObject, ...)` reads `url`, validates the HTTP(S) prefix, and dispatches to `openHttp(JSONObject)`.
- FE bridge class: `com.ss.android.ugc.aweme.fe.method.OpenBrowserMethod`. Its `handle(...)` similarly routes to `LIZJ(JSONObject)`.
- Both implementations read `use_external_browser` with default `false`. True creates an `android.intent.action.VIEW` intent with `CATEGORY_BROWSABLE`; false returns a `type=webview` request to the in-app browser. This is a confirmed, concrete hook point, but not proof that every link click uses it.
- `TtopIABServiceImpl.LIZ(...)` is another native IAB route. Its bytecode parses URI scheme/host/path and checks rules before creating an ACTION_VIEW intent. Treat it as a separate route; do not globally force it until callers and intended scope are understood.
- The strings `external_browser` and `link_to_external_browser` found in unrelated Live auto-login/e-commerce code are not evidence of a global TikTok setting. No global TikTok setting was found in the extension's Settings.java.

#### Profile background gate details
- `ProfileRootComponent.Dq()` calls `LX/0Ngv.LJFF(...)` before registering the component key `profile_background`.
- `LX/0Ngv.LIZ()` creates a `ProfileBackgroundExp` snapshot keyed by current UID and reads `profile_bg_in_allow_list`; the snapshot stores UID and allow-list value. This supports an account/experiment eligibility gate.
- `ProfileBackgroundComponent.Sr()` also performs a device/performance check using the `profile_background` feature score when deciding whether to support the background/video behavior. This is separate from account allow-list eligibility.
- Even after registration, `ProfileBgBizData` must provide a background and `ProfileBgInfo.hasBackground()` must be true. Thus there are at least three distinct conditions: component allow-list, device/performance support for some behavior, and actual background data. Do not assume bypassing one enables the feature end-to-end.

#### Seekbar exact exclusions
- In `LX/06Bt.LJI(Aweme)`, native false-return reasons are explicit log states: story immersive feed; story guide card; page not resumed; cannot drag; not current Aweme; no progress bar; story video; inverse experiment; video mask; prompt overlay; prohibited video.
- `onPlayerControllerVideoStatusEvent` calls `LJI(Aweme)` before updating pause/show-type state. Since the patch forces that method to true, events can continue into show-type updates in contexts the native code rejects.
- The `setSeekBarShowType(I)` patch overwrites the incoming argument register at method entry with `0x64`; this is global to that setter, not limited to normal feed playback.


### Research #71 — profile banner region vs account gate; external implementation comparison (2026-11-25)

#### Region change is not sufficient
- User reports switching TikTok region to Japan did not reveal the profile background/banner option.
- Region spoof can change client-visible country/SIM/time-zone values, but does not necessarily change public IP, server-side account eligibility, or the server's per-account AB snapshot. A Japan selection alone is not a reliable test of the feature gate.
- Distinguish three states when testing: (1) Edit profile exposes Add/Change background; (2) the client can open editor and upload; (3) TikTok server accepts and persists the background and returns it in profile data. Success at (1) does not prove (3).

#### Public implementation comparison
- Reviewed public LSPosed/LSPatch project `vanquzie/TikProfileBanner`. Its README targets TikTok 47.1.3 only (other versions explicitly untested). It hooks a master gate and a low-device gate; README warns the banner is saved server-side but TikTok does not send it back when viewing one's own profile, so verify from a second account. Do not transplant its obfuscated names blindly: its source uses `X.0OSG`, while our inspected APK uses a different obfuscated gate holder (`LX/0Ngv`).
- Reviewed public Morphe/Metra profile-background patch source. It identifies the AB keys `profile_bg_in_allow_list` and `profile_bg_enable_consumption_group`; finds the holder class structurally rather than relying only on one obfuscated name; and hooks the boolean gate while preserving the avatar-layout check.
- Critical additional finding in that implementation: opening the editor alone was insufficient. TikTok's own-profile and other-profile requests (`getSelfUserInfo`, `getOtherUserInfo`) send an account-scoped snapshot of `profile_bg_in_allow_list`. If the snapshot remains 0, the server may omit an already-saved background from profile responses. The patch therefore also changes the value in the request snapshot. This is a stronger model than merely forcing component registration or showing the editor.
- The Metra README describes the feature as opening TikTok's native profile-background editor on accounts not yet offered it, and explicitly warns that TikTok servers may still refuse to save a background for an ineligible account. Its region spoof documentation also says it cannot hide the public IP seen by TikTok servers and may not replace server/network region checks.
- Metra's feature-gate preset includes both `profile_bg_in_allow_list:INT:1` and `profile_bg_enable_consumption_group:INT:3`; this is evidence of the two experiment keys, not proof that those values alone guarantee server-side write permission.

#### Implications for UltraMod
1. Do not treat Japan region spoof as sufficient or advise repeated country switching.
2. Do not copy TikProfileBanner's `X.0OSG` method names; its target is 47.1.3 and our artifact's class names differ.
3. For our APK version, identify the exact AB holder, boolean gate and avatar-layout check, plus the per-account AB snapshot reader used by `getSelfUserInfo` / `getOtherUserInfo` requests.
4. A robust client patch would need to preserve layout/device checks, expose the editor, and ensure the account snapshot used in profile requests is consistent. Even then, server-side write authorization can still reject uploads.
5. No source code changed in this investigation. Treat public implementations as technical references, not drop-in patches; validate against the exact APK version and keep a feature toggle/default-off until tested.


### Research #72 — exact target version confirmed: TikTok 47.9.3 (2026-11-25)
- User confirmed that the TikTok version installed in UltraMod is **47.9.3**. Treat this as the target version for the next Profile Background investigation; do not use the old prototype APK as if it were 47.9.3.
- Public release metadata identifies 47.9.3 as build/version code 2024709030, published 20 Nov 2026. This is useful for obtaining/verifying the matching base APK, but does not establish which exact variant or package the user's UltraMod contains.
- Rechecked public TikProfileBanner: its README explicitly targets 47.1.3 and says other versions are untested. Its obfuscated hook is not a valid reference for 47.9.3.
- Rechecked public Morphe/Hushfeed ProfileBackgroundPatch.kt: the implementation locates the gate holder structurally using the AB keys profile_bg_in_allow_list and profile_bg_enable_consumption_group, a boolean gate and account-switch setter; it also finds the account-scoped allow-list snapshot reader used by profile requests. Its comments document concrete obfuscated examples for 47.1.4 (and older 47.0.3), not 47.9.3. The structural strategy is a research lead, not proof that the same method shape remains in 47.9.3.
- A separate public Morphe patch release mentions updating general TikTok patch targets/hooks to 47.9.3; that release note does not claim its Profile Background patch is validated on 47.9.3. Do not conflate general app-version support with this feature's compatibility.
- Next required input for reliable static matching: obtain the exact 47.9.3 base APK used by the user's UltraMod (or the current installed UltraMod APK). Then verify package/version code, locate the gate holder, gate/layout check, account-scoped snapshot reader and request callers in that exact DEX set. If only the old 57-DEX artifact is available, conclusions remain limited to that older artifact.
- No source code, APK, patcher, or workflow changed. No GitHub Actions run. Do not start Actions until the user confirms capacity is available. No source patch should be made without explicit approval.


### Research #73 — exact 47.9.3 APK found in successful prototype build (2026-11-25)
- User said the APK is already present in the branch's builds; located the latest successful `Automate TikTok Patching` run on `ultramod/prototype`: run `44930595699`, commit `3fbb0c2b1714a4d6deb1e8553c7df914d7308add`, created 2026-11-21. Artifact `14718266818` (`tiktok-ultramod-prototype`) was downloaded read-only.
- The job log's own APK verification step reports package `com.zhiliaoapp.musically`, versionCode `2024709030`, versionName **47.9.3**. This confirms the successful prototype build already used the exact TikTok version the user reported. The artifact includes `tiktok-rv-original.apk` (the original TikTok input APK) plus audit files.
- The same job log shows `VERSION_MODE: auto` and blank `TIKTOK_VERSION`; therefore a later build may download a newer TikTok version automatically. A future build must re-check the resulting APK's package/version before treating any patch as compatible.
- The newest prototype patching run currently listed is run `45287709435` on 2026-11-23 and failed; no artifact was produced. No new build was triggered during this investigation.
- Implication for Profile Background: we now have a 47.9.3 base APK available from the successful artifact, so the next static pass can inspect the exact DEX files without asking the user to upload anything. Do not assume the 47.1.x public hooks match. Build a fail-closed structural fingerprint for the exact 47.9.3 gate + layout check + account-scoped allow-list reader; if a later auto-downloaded TikTok version does not match uniquely, the patch should stop with an explicit unsupported-version error rather than patching a guessed method. If desired, pin TikTok 47.9.3 for a controlled build; otherwise maintain explicitly tested per-version fingerprints.
- Read-only artifact download and log inspection only. No source code, APK, patcher, workflow, or Actions state was changed; no workflow was run.

## Profile Background — exact 47.9.3 DEX anchors (#74, 2026-11-25)
- Inspected the original TikTok APK extracted from successful prototype artifact `44930595699`, verified as versionName `47.9.3` / versionCode `2024709030`. Read-only DEX parsing; no APK/source/workflow changes and no Actions run.
- The feature is present in this exact APK: `ProfileBackgroundComponent`, `ProfileBgBizData`, `ProfileBgInfo`, `ProfileBgImagePreviewActivity`, `ProfileBgVideoListActivity`, `ProfileBgVideoPreviewActivity`, `ProfileBgApi`, and `ProfileBgRepository` are present across multidex. `ProfileBgInfo` exposes `hasBackground()`, `isImage()` and `isVideo()`.
- The exact 47.9.3 APK still has the gate holder `LX/0Ngv`. Its `LIZ()` method contains the strings `ProfileBackgroundExp` and `profile_bg_in_allow_list`. The method also calls `getCurUserId()` and builds/reads a snapshot with log strings `getMineSnapshot` and `createSnapshot uid`. This is direct evidence that the allow-list value is handled through a UID-scoped snapshot/cache path in 47.9.3, not merely a static global boolean.
- `LX/0Ngv.LJFF(ZZ):Z` remains present. `ProfileRootComponent.Dq` calls it during profile-root setup before the `profile_background` component registration path. In 47.9.3, `LJFF` calls `LX/0Ngv.LIZ()` and invokes `LX/0Bbm.LIZLLL` twice, then reads another boolean/experiment-backed value. Therefore the gate is a compound condition; do not replace the whole method with unconditional true or discard the `0Bbm.LIZLLL` checks without resolving their semantics.
- `ProfilePlatformViewModel.R93` and `S93` also call `LX/0Ngv.LIZ()`. This gives additional request/profile-model integration points, but this pass has not yet proven the precise data flow from this snapshot into both `getSelfUserInfo` and `getOtherUserInfo` request serialization.
- This is a materially stronger version-specific match than the older 47.1.x public patches: the holder and core methods survived into 47.9.3 with the same obfuscated class/method anchors. It is still not sufficient to write a safe patch: resolve the two `0Bbm.LIZLLL` calls and their arguments/return meaning; inspect the `ProfileRootComponent.Dq` branch and avatar-layout/performance guard; then trace the UID snapshot into self/other profile request payloads. Structural matching should verify all anchors uniquely and fail closed on mismatch.
- Static analysis only. No device/runtime test, no backend eligibility or save/readback test. No source/APK/workflow modifications; do not run Actions until the user confirms capacity is restored.

## Profile Background — request parameter trace completed (#75, 2026-11-25)
- Continued read-only DEX/annotation analysis of the exact original TikTok 47.9.3 APK from prototype artifact 44930595699. No APK/source/workflow changes; no Actions run.
- Resolved LX/0NhE as a two-field snapshot: LIZ = UID (String), LIZIZ = integer allow-list value. LX/0Ngv.LIZ() checks the current account UID against the cached snapshot; on a cache miss/account change it reads profile_bg_in_allow_list and creates a new snapshot for that UID.
- Confirmed the allow-list value is not only used by the component gate: ProfilePlatformViewModel.R93 extracts Snapshot.LIZIZ and passes it as profile_bg_in_allow_list to UpdateProfileUserAPI.getSelfUserInfo.
- Confirmed the same flow for other profiles: ProfilePlatformViewModel.S93 extracts Snapshot.LIZIZ and passes it as profile_bg_in_allow_list to getOtherUserInfo. It separately reads the lazy integer value from LX/0Ngv.LIZJ and passes it as profile_bg_enable_consumption_group.
- Parsed API parameter annotations and endpoint paths directly from DEX:
  - getSelfUserInfo -> /tiktok/user/profile/self/v1; parameters in order: scene_id, from, profile_optimize_exp, header_simplify_exp, left_align_exp, feature_center_exp, profile_bg_in_allow_list, page_width, profile_inbox_exp.
  - getOtherUserInfo -> /tiktok/user/profile/other/v1; parameters in order: scene_id, sec_user_id, user_id, teen_sec_user_id, from, profile_optimize_exp, header_simplify_exp, left_align_exp, profile_bg_in_allow_list, profile_bg_enable_consumption_group.
- LX/0Ngv.LJFF(Z) is a compound component-registration gate. It reads the snapshot allow-list integer and invokes LX/0Bbm.LIZLLL() twice. Resolved the latter getter's backing lambda LX/0BdL; its log label is isEnableAvatarOpt. It also reads another lazy AB/config integer (LX/12TO.LIZ). Preserve these checks; do not replace the entire gate with unconditional true. Exact semantic meaning of the boolean argument and the LX/12TO value remains to be established.
- Important precision: the two profile_bg_* request parameters are not interchangeable. Self-profile request includes profile_bg_in_allow_list only; other-profile request includes both allow-list and consumption-group values. A safe experiment must keep per-account snapshot consistency and handle the additional other-profile parameter separately.
- This establishes client-side request plumbing, not server-side entitlement. It does not prove the backend will accept a save or return a background for an account. No runtime/device/backend test.
- Next: finish mapping LX/12TO.LIZ and the boolean argument to LJFF, then inspect the component/editor save/readback path and API response model. Any future patch must structurally match all required anchors and fail closed on mismatch.

## Profile Background — save endpoint and gate control-flow refinement (#76, 2026-11-25)
- Continued static inspection of exact TikTok 47.9.3 APK. No source/APK/workflow changes; no Actions run.
- Refined LX/0Ngv.LJFF(Z)Z control flow: it obtains the UID-aware snapshot, requires a non-null snapshot and allowListValue == 1, then checks LX/0Bbm.LIZLLL(). The latter getter resolves to the LX/0BdL lazy property whose log label is isEnableAvatarOpt. The input boolean selects one of two duplicated branches; both require the same avatar-opt flag to be true. This is static control-flow interpretation of the current DEX, not a runtime test.
- Correction to #75: the apparent extra read of LX/12TO.LIZ is in a fallback block reached only if a local value initialized to 1 becomes zero; the branch condition returns earlier while that value remains 1. Treat this block as unreachable/dead in the observed control flow, not as an active required Profile Background AB gate. Do not label LX/12TO.LIZ as a profile-background experiment.
- Resolved the ProfileBgApi interface in DEX. commitProfileBackground posts to /aweme/v1/commit/user/ and has parameter annotations: profile_bg_type, aweme_id, image_uri, offset_y_ratio. This is the common commit endpoint for the repository's clear/image/video operations.
- Repository coroutine methods in LX/0het log distinct failure paths commitClearBackground, commitImageBackground, and commitVideoBackground, and all call the same ProfileBgApi.commitProfileBackground endpoint. This confirms the editor's save path is a real server write, not a local-only profile decoration.
- ProfileBgApi.fetchUserPublicVideos is a separate endpoint /aweme/v1/aweme/post/ with parameters user_id, max_cursor, count, scene, filter_private, sec_user_id.
- ProfileBackgroundComponent.Vr(String) parses component JSON into ProfileBgBizData. Its Er(ProfileBgBizData) path reads ProfileBgInfo, media type, video info, auto-play, Aweme ID, offset ratio and image URL list to construct the rendered background state. This confirms that display requires populated profile component data returned to the client; merely exposing the editor does not create that data.
- Combined with #75: client-side path has three distinct stages: component registration gate (UID snapshot allow-list + avatar-opt guard), commit request to backend (/aweme/v1/commit/user/), and subsequent profile component data parsing/rendering (ProfileBgBizData/ProfileBgInfo). Success at one stage does not prove success at the next.
- Remaining unknowns: backend eligibility and persistence behavior for this account; exact values/semantics of profile_bg_type; whether save endpoint accepts writes for accounts outside server eligibility; exact response/readback behavior after commit. No runtime/device/backend test.
- Next: inspect editor UI entry points and call chains into LX/0het clear/image/video methods; map profile_bg_type values only if grounded by callsites; trace how getSelfUserInfo/getOtherUserInfo response fields become ProfileBgBizData. Do not patch until the complete path is understood and the user explicitly approves a concrete source change.

## Profile Background — commit type values and response model (#77, 2026-11-25)
- Continued static analysis of exact TikTok 47.9.3 APK. No source/APK/workflow changes; no Actions run.
- Resolved the repository's profile_bg_type constants from the actual argument registers passed to ProfileBgApi.commitProfileBackground: 0 = clear background (LX/0het.LIZ), 1 = image background (LX/0het.LIZIZ), 2 = video background (LX/0het.LIZJ). These are client-side request type values, distinct from the separate ProfileBgInfo.mediaType response field, which also uses 1=image and 2=video.
- Confirmed ProfileBgInfo.hasBackground() returns true only when mediaType > 0; isImage() checks mediaType == 1; isVideo() checks mediaType == 2. Thus a server response with no positive mediaType is treated as no background even if the editor is available.
- Parsed model fields from DEX class_data: ProfileBgBizData contains autoPlay, needGuide, profileBgInfo; ProfileBgInfo contains imageUrlList, mediaType, videoInfo; ProfileBgVideoInfo contains autoPlay, awemeInfo, offsetYRatio. DEX strings include matching serialized keys such as profile_bg_info, image_url_list, media_type, video_info, auto_play, need_guide and aweme_info.
- ProfileBackgroundComponent.Vr(String) deserializes the component JSON into ProfileBgBizData. Er(ProfileBgBizData) reads mediaType and then consumes image URL list or videoInfo (including Aweme ID and offset ratio) to construct the rendered state. This ties successful display to server-returned component payload, not merely a locally enabled control.
- Combined request/save/readback picture: profile self/other APIs carry allow-list context (#75); commit endpoint writes one of clear/image/video modes (#76/#77); subsequent profile response must contain populated ProfileBgBizData/ProfileBgInfo with mediaType > 0 for the component to render a background.
- Still unverified: backend account eligibility, acceptance/persistence of commit requests for non-allow-listed accounts, exact server-side validation/error behavior, and whether saved data is returned to the same account on subsequent profile fetch. No runtime/device/backend test.
- Next: trace ProfileBackgroundComponent's editor-entry click path and generated callbacks into the three repository methods; inspect response plumbing from profile API models into component JSON. Keep any proposed patch limited to a uniquely matched client gate and request snapshot; never assume this grants server-side eligibility.


## Profile Background — editor entry and end-to-end client path (#78, 2026-11-25)
- Completed the bounded static pass on the exact TikTok 47.9.3 APK extracted from successful UltraMod prototype artifact 44930595699. Read-only DEX inspection; no APK/source/workflow edits and no GitHub Actions run.
- Traced the own-profile UI entry:
  - `ProfileBackgroundComponent.Jq()` checks the profile-background gate `LX/0Ngv.LJFF(Z)` before constructing the native `Profile Background Action Sheet` (analytics label `Click Social Avatar Edit Action Sheet`).
  - The action-sheet callbacks route to the native background helpers. `LX/0hYh.LIZ(...)` launches `ProfileBgVideoListActivity`; `LX/0hYY.LIZJ(...)` launches `ProfileBgVideoPreviewActivity` with video ID/play URL/cover/dimensions. The image helper `LX/0hbu.LIZJ(...)` launches `ProfileBgImagePreviewActivity` with `extra_image_url_list`, `extra_enable_save`, and `extra_enable_change`.
  - This is the app's own editor/preview flow; no custom editor needs to be built.
- Traced adjacent operations:
  - `ProfileBgVideoListViewModel.y93(...)` loads the user's public videos through the background repository's `fetchPublicVideos` operation.
  - `LX/0het` is the obfuscated `ProfileBgRepository` implementation. Its three commit coroutine methods call the common `ProfileBgApi.commitProfileBackground` endpoint for clear/image/video; the fourth method fetches public videos. The endpoint and request types are recorded in #76/#77.
  - Image preview has a native bitmap processing/save flow; video preview is a separate native activity. The repository exposes distinct clear/image/video commit paths. This static pass confirms the functional layers, but does not claim a device-observed successful commit callback or backend acceptance.
- Traced response-to-render path:
  - The profile component update reaches `ProfileBackgroundComponent.gr(Lcom/google/gson/n;)` / `Wr(...)`, which passes the component JSON to `Vr(String)` for `ProfileBgBizData` deserialization.
  - `Er(ProfileBgBizData)` extracts `ProfileBgInfo`; `Br()` / `gs(...)` select image or video rendering from the populated response model. An absent/empty `ProfileBgInfo` is treated as no background. Therefore the editor and commit endpoint do not by themselves guarantee a displayed banner: a later profile response must include populated component data.
- Minimal client-side experiment proposed (not applied): override the value read for `profile_bg_in_allow_list` in `LX/0Ngv.LIZ()` to 1 **before** constructing the existing UID-scoped `LX/0NhE` snapshot. This single source value feeds both the `LJFF(Z)` component gate and the `getSelfUserInfo` / `getOtherUserInfo` request parameters, preserving the account-scoped snapshot semantics. Keep the `isEnableAvatarOpt` checks in `LJFF(Z)` intact; do not force the entire gate true, remove device/layout guards, or alter unrelated AB values. This is a candidate to test, not a guarantee; if the avatar-opt check itself is false on the test device, diagnose that separately rather than blindly bypassing it.
- Practical test sequence after explicit approval of a patch: install only a separately built experimental APK; verify the action sheet and native image/video editors appear on the test account; attempt one image save; restart/reload the profile; inspect the same profile and a second account's view; then test video/clear separately. Record exact UI/network error and whether the profile response contains `profile_background` data. Do not test first on the user's working prototype install/account if a disposable test account is available.
- Decision boundary: client code path is sufficiently mapped to propose a narrowly scoped patch. Remaining unknown is server-side eligibility: whether this account's commit is accepted and whether the server returns populated `ProfileBgBizData` afterward. If the server rejects or omits the data, further client UI changes will not solve that. Stop static research here unless a concrete runtime result identifies a specific missing client branch.
- No source patch was made. `ultramod/prototype` remains untouched; `ultramod/experimental` contains research notes only. No Actions run.


## Profile Background — experimental client patch added (#79, 2026-11-25)
- User explicitly approved adding the proposed patch to `ultramod/experimental`; approval did not include modifying `ultramod/prototype` or running GitHub Actions.
- Added `revanced-patches/patches/src/main/kotlin/app/revanced/patches/tiktok/profile/ProfileBackgroundPatch.kt`.
- The patch uses a fail-closed fingerprint for the exact 47.9.3 holder/method `LX/0Ngv.LIZ()LX/0NhE;`. It requires the method to contain exactly one `profile_bg_in_allow_list` string, locates the following integer-returning AB read and its `move-result`, then inserts a constant 1 into that same result register immediately after the move-result.
- This changes only the allow-list value before the existing UID-scoped snapshot is constructed. It does not replace `LX/0Ngv.LJFF(Z)`, does not bypass its `isEnableAvatarOpt` / avatar-layout checks, and does not modify `profile_bg_enable_consumption_group` or other AB values.
- The patch is compatible only with package `com.zhiliaoapp.musically`. It is selected only for the workflow's `original` variant. The workflow now treats a Profile background patch failure as fatal for original builds, so a changed upstream DEX fingerprint cannot silently produce an APK without this experiment. Asia variant remains unchanged.
- The workflow still uses automatic TikTok version resolution by default. The patch is deliberately anchored to the observed 47.9.3 DEX shape; if a newer upstream version changes the holder, return type, key count, integer read, or move-result shape, patching should fail closed. Confirm the downloaded APK version in build logs before interpreting a result.
- No GitHub Actions run, Gradle build, APK build, install, or device/backend test was performed. Therefore Kotlin compilation and actual patch application remain unverified. The next validation step, when Actions capacity is available and user approves running it, is a controlled original-package build pinned to 47.9.3, followed by the device test plan in #78.
- `ultramod/prototype` was not modified. Only `ultramod/experimental` source/workflow and its research journals were changed.


## Research #80 — seekbar patch narrowed to native eligibility (2026-11-25)

- Revisited the seekbar against the exact TikTok 47.9.3 build. Important artifact correction: the workflow names its **signed patched output** `tiktok-rv-original.apk`; it is not the raw downloaded TikTok APK. The two leading instructions in `LX/06Bt.LJI(Aweme)` (`const/4 v0, 1; return v0`) are exactly the current patch's `returnEarly(true)` insertion. The original native eligibility logic remains immediately after those injected instructions, so the old patch was bypassing it.
- Confirmed the native eligibility method contains separate exclusions for immersive story feed, story guide cards, page-not-resumed state, drag restrictions, non-current Aweme, no-progressbar videos, story videos, inverse experiment, video masks, prompt overlays, and prohibited videos.
- Traced the inverse-experiment call `LX/0991.LIZ(Aweme)` to lazy key `enable_video_seekbar_visible` (reader lambda `LX/0994.invoke()`). The patch now overrides only this call's boolean result to false at the callsite inside the seekbar eligibility method. It no longer forces the whole eligibility method to true.
- Confirmed the feed controller's show-type selector is `LX/06Bt.LJIIJJI(Z):I`. It reads the current Aweme from field `LLJLL`, has two return paths using register v2, and uses the common seekbar setter. The patch now checks `LX/06Bt.LJI(currentAweme)` immediately before each return and changes the returned type to `0x64` only when all remaining native eligibility checks pass. A null Aweme or failed eligibility preserves the original native type.
- Removed the global `setSeekBarShowType` override. The setter's original show/hide modes are now preserved for every other caller instead of forcing all calls to `0x64`.
- Added fail-closed checks for exact method descriptors, exactly one inverse-experiment call followed by `move-result`, the selector's expected register layout/current-Aweme field, and its two return paths. If the 47.9.3 structure changes, the patch should fail instead of modifying a guessed method.
- Source changes committed only to `ultramod/experimental`: `ShowSeekbarPatch.kt` and `Fingerprints.kt`. `ultramod/prototype` remains untouched. No Actions workflow, Gradle build, APK build, install, or runtime test was performed. Kotlin compilation and patch application remain unverified.
- Expected behavior from static control flow: ordinary eligible feed videos can use the visible seekbar type; story/lifecycle/drag/mask/prompt/prohibited-video exclusions and unrelated setter states remain native. This is a static expectation, not device confirmation.


## UltraMod settings UI — Russian visible-text pass (#81, 2026-11-25)
- User explicitly approved translating/rebranding only the user-visible settings UI. Work was performed only on `ultramod/experimental`; `ultramod/prototype` was not modified.
- Updated the TikTok settings entry label from "ReVanced settings" to "Настройки UltraMod".
- Translated visible category names and preference labels/summaries: feed filtering, downloads, SIM/region settings, miscellaneous options, link sanitization, and debug logging.
- Translated restart/confirmation dialog strings and the download-path dialog title. The visible folder choices now use Russian labels (Камера/DCIM, Фильмы, Изображения), while their stored values remain exactly `DCIM`, `Movies`, and `Pictures`.
- Replaced the About preference's visible ReVanced references with UltraMod-only copy. Removed the displayed patches version; the visible About text now shows the UltraMod version, description, and Telegram handle.
- Preserved all preference keys, defaults, setting behavior, internal package/class names, method names, Intent extra `revanced`, patcher metadata, and other hidden implementation identifiers. No country-picker redesign was included: ISO/MCC/MNC remain existing text settings, only their visible labels and descriptions were translated.
- No UI redesign, new banner, new country/operator dataset, or settings migration was performed; those are outside this text-only scope.
- Static source edits only. No Gradle build, GitHub Actions, APK build, install, or runtime verification was performed. Compilation and on-device display remain unverified.


## Research #82 — first native UltraMod shell implementation (2026-11-25)

- User approved moving from the Russian text pass to a full custom settings shell. Work is isolated to `ultramod/experimental`; `ultramod/prototype` was not changed.
- Added `UltraModSettingsFragment.java`, a programmatic Android View-based interface. It uses a dark graphite/purple palette, branded gradient hero, home cards, section pages, custom toggle rows, action rows, and native dialogs. It adds no XML resources or third-party UI dependencies.
- Updated `TikTokActivityHook.initialize()` to open the new fragment. The existing TikTok entry point and `AdPersonalizationActivity` integration remain in place.
- Added pages for feed filtering, downloads, region/SIM settings, and additional options. Existing `BooleanSetting` / `StringSetting` objects are used directly; their keys, defaults, patch-side consumers, and persisted values were not renamed.
- Toggle changes are saved through `Setting.save()`; settings marked as requiring restart use a Russian-language restart prompt that calls the existing shared `Utils.restartApp()` implementation. Download location selection retains the existing DCIM/Movies/Pictures value tokens and saves the existing `down_path` key.
- Added a searchable country list using Android's built-in ISO country list and Russian locale display names. Selecting a country updates only the existing `simspoof_iso` setting. MCC/MNC and operator name remain separate editable values; a verified operator/carrier dataset and linked country-to-operator selection are **not** implemented yet.
- The previous `TikTokPreferenceFragment` and category classes remain in the source tree but are no longer the active UI entry path. They were retained rather than deleted during this first pass.
- This is an initial implementation, not a verified release. No Gradle build, GitHub Actions, APK build, install, or device/runtime test was run. Java compilation, host-activity rendering, dialog appearance, persistence after relaunch, and restart flow remain unverified.


## Research #83 — candidate MCC/MNC operator directory (2026-11-25)

- Investigated a real operator directory for the next region-picker phase. The mcc-mnc.org downloadable `networks.json` declares 2,679 network records, generated 2026-11-23 and last updated 2026-11-13. Its metadata says "Free to use with attribution to mcc-mnc.org".
- The records include `iso`, `mcc`, `mnc`, `operator`, and `brand`; MCC/MNC values are represented as strings and the documentation explicitly warns that leading zeros in MNC must be preserved.
- This is a suitable candidate for a country -> operator selector that can populate the existing `simspoof_iso`, `simspoof_mccmnc`, and `simspoof_op_name` settings together. The source distinguishes legal operator name from consumer-facing brand.
- Dataset not yet imported: complete file retrieval, duplicate/blank/status review, data-size check, and final attribution placement remain to be done before bundling it in the extension. Do not treat this as a completed operator picker.
- Source reviewed: https://mcc-mnc.org/downloads/networks.json and https://mcc-mnc.org/downloads. No project code was changed for this research item.


## UltraMod shell static audit #84 (2026-11-25)
- Rechecked `ultramod/experimental` at the start of this pass; branch head was `0e4e124f3ba54c5d3abbff439eb5b7b999a69e8b`. `ultramod/prototype` was not modified.
- Reviewed the custom settings fragment, activity hook, settings definitions and setting-status holder. Existing setting keys/defaults remain intact; the shell uses the established `Setting.save()` path.
- Found a small UX defect: editing MCC/MNC or operator name saved the value but left the displayed row stale until navigating away and back. Fixed the save handler to rebuild the region page immediately (`994d9a482b4a3903a6cebc9e705129d4c4b3210b`).
- MCC/MNC source review: `mcc-mnc.org` provides a 2,679-row dataset with explicit free-use/attribution terms; leading-zero MNC values are strings and must remain strings. Its bulk JSON is visible through the web viewer but could not be reliably materialized as a complete source file in this pass.
- A GitHub mirror (`voorz/plmn-index`) has a 3,386-entry JSON file and MIT repository license, but its README says it aggregates mcc-mnc.com, Wikipedia and other sources; the data provenance/licensing is not sufficiently clear to substitute it silently. Not imported.
- Operator picker therefore remains unimplemented. Do not claim country -> operator selection exists. Next: obtain the original source file intact or validate a clearly licensed complete mirror, then compact/filter records, preserve MNC leading zeros, and add source attribution.
- No build, Gradle, Actions, APK, install, or runtime test was run. Java compilation remains unverified.


## UltraMod operator directory — source verification #85 (2026-11-25)

- Re-opened the original bulk source, `https://mcc-mnc.org/downloads/networks.json`, and verified its embedded metadata: 2,679 records; generated 2026-11-23; last updated 2026-11-13; explicit licence text: "Free to use with attribution to mcc-mnc.org".
- Confirmed the schema includes `iso`, `mcc`, `mnc`, `operator`, and `brand`. MCC/MNC are strings; MNC values such as `01` must not be converted to integers. The source documentation says a public deployment should link back to mcc-mnc.org.
- The source is now verified as a legally usable candidate with explicit attribution terms. However, the complete JSON could be viewed through the web reader but could not be downloaded/materialized intact into the working environment in this pass. Therefore no partial or hand-copied dataset was added.
- Rechecked the current shell source. The region page still has three independent rows; selecting a country saves only `simspoof_iso`. The current operator-name and MCC/MNC fields remain manual inputs. The next implementation must replace these three independent actions with a country-filtered operator picker and save the three existing settings together. Keep the stored ISO format compatible with the existing lowercase ISO values, and preserve MCC/MNC as strings.
- No source code or dataset was changed in this research pass. `ultramod/prototype` remains untouched. No Gradle, build, GitHub Actions, APK, install, or runtime test was run.


## #86 — Контракт SIM-spoof и проверка источника операторов (2026-11-25)

- Продолжен статический аудит только в `ultramod/experimental`; `ultramod/prototype` не изменялась.
- Проверен фактический потребитель настроек: `SpoofSimPatch.java` читает три отдельных значения — ISO страны, MCC+MNC и имя оператора — и возвращает их напрямую для SIM- и network-вызовов. Дополнительной нормализации имени оператора на стороне extension нет.
- Проверен патч-инжектор: MCC+MNC используется как строка, поэтому сохранение MNC с ведущим нулём обязательно. Существующие строковые настройки совместимы с этим и должны остаться без переименования.
- Поле `brand` в каталоге — короткое пользовательское имя сети; поле `operator` — юридическое/полное имя. С учётом текущего значения по умолчанию `LMT` в настройку имени следует сохранять `brand`, а при пустом brand использовать operator. В диалоге можно показывать бренд и код, не подменяя MCC+MNC.
- Найден и проверен открытый репозиторий `pbakondy/mcc-mnc-list`: JSON содержит 898 620 символов, MIT-лицензия требует сохранить уведомление. Но его README приводит 2 189 записей, а доступная копия выглядит старой; поэтому не выбран как окончательный справочник для UltraMod.
- Более полный источник MCC-MNC.dev документирует 3 042 сети, строковые MCC/MNC и CC BY-SA 4.0, но его статический файл нельзя надёжно перенести в текущую рабочую ветку через доступные каналы в этом проходе. Не добавлять частичный или заведомо устаревший справочник.
- Вывод для реализации: оставить ISO в нижнем регистре; операторский список фильтровать по ISO без учёта регистра; сохранять MCC+MNC как исходную строку; показывать бренд с резервным названием оператора; не сбрасывать три значения при отмене диалога.
- Изменения кода и данных не вносились. Сборки, Gradle, GitHub Actions и тесты не запускались.


## #87 — Структура справочника и готовность UI к операторскому выбору (2026-11-25)

- Повторно проверен полный статический файл MCC-MNC.dev: `https://mcc-mnc.dev/data/networks.min.json`. Он опубликован без API-ключа, содержит 3 042 записи и имеет размер около 1,0 MB; формат — массив объектов с вложенным `country.iso2`, строковыми `mcc`/`mnc`, `brand`, `operator`, `status` и `plmn`.
- Подтверждено, что нулевое дополнение MNC значимо. Для UltraMod достаточно импортировать только поля, нужные интерфейсу: ISO2, MCC, MNC, бренд/имя и статус; enrichment/диапазоны частот в приложение не нужны.
- Проверена лицензия на странице About Data: CC BY-SA 4.0; обязательная атрибуция MCC-MNC.dev и share-alike для производного набора. Следовательно, при включении файла в проект нужно положить рядом короткое уведомление с источником, лицензией и ссылкой.
- Ссылка Source on GitHub на сайте ведёт на профиль/организацию, но не на доступный репозиторий с исходным dump; поиск в доступном GitHub-коннекторе обнаружил только клиентские библиотеки, не сам JSON. Прямое скачивание опубликованного JSON в рабочую среду повторно не удалось. Не подменять источник клиентской библиотекой и не создавать фиктивную базу.
- Статически проверена оболочка UltraMod: текущая страница региона уже использует собственный AlertDialog с поиском для страны; Android `org.json` доступен через платформу без отдельной Gradle-зависимости. Модуль TikTok не имеет отдельной JSON-библиотеки в dependencies, поэтому встроенный JSON-ресурс + платформенный парсер — наиболее лёгкий путь без новых зависимостей.
- План UI уточнён: действие «Страна» сохраняет ISO; действие «Оператор» открывает фильтрованный по текущему ISO список; выбор оператора атомарно сохраняет существующие `SIM_SPOOF_ISO`, `SIMSPOOF_MCCMNC`, `SIMSPOOF_OP_NAME`. Существующие ключи, значения по умолчанию и формат строки не менять. Операторский список должен отображать бренд, MCC+MNC и статус; операционные сети — выше, остальные статусы не удалять из исходного справочника.
- Изменения кода/ресурсов не внесены: без полного JSON нельзя корректно завершить офлайн-справочник. Изменён только журнал. Сборки, Gradle, Actions и тесты не запускались.


## Run #88 — offline SIM operator catalog integrated (2026-11-25)
- Retrieved the complete uploaded `networks.min.json` blob from GitHub and parsed it as JSON: 3,042 records; 2,934 records have a standard two-letter ISO2; 226 distinct standard ISO2 country codes. The non-standard/empty ISO2 records (including `GE-AB`) are excluded from the ordinary country picker match.
- Added an offline operator picker to the UltraMod region page. It filters by the currently selected ISO2, supports search by operator name/MCC+MNC/status, sorts Operational records first, and retains other statuses in the catalog. Operator name uses `brand` with `operator` fallback. MCC and MNC remain strings concatenated without numeric conversion, preserving leading zeros.
- Selecting a row saves the existing ISO, MCC+MNC and operator-name settings. Existing setting keys and defaults are unchanged. Manual MCC+MNC and operator-name editing remains available. Empty country results and parse/read failures leave settings unchanged.
- Added `assets/NOTICE-MCC-MNC.txt` with MCC-MNC.dev source attribution and CC BY-SA 4.0 license notice.
- No Gradle dependencies added. No builds, Gradle tasks, GitHub Actions, APK/runtime tests were run. Static source/data inspection only.


## #89 — UltraMod home layout polish (2026-11-25)
- Продолжен статический аудит оболочки на `ultramod/experimental`; базовая ветка `ultramod/prototype` не затрагивалась.
- Исправлено адаптивное размещение карточек главного экрана: если для категории доступна только одна карточка (например, часть функций отключена конфигурацией сборки), она теперь занимает всю ширину строки вместо половины экрана. Для двух карточек сохранена равная двухколоночная сетка.
- Изменение локальное: затронут только расчёт ширины карточек главного экрана; настройки, их ключи/значения, навигационные идентификаторы и patch-side consumers не менялись.
- Проверена правка по исходному коду и контексту вызовов. Сборка, Gradle, GitHub Actions, APK, установка и runtime-тесты не запускались; визуальное поведение на устройстве пока не подтверждено.


## #90 — UltraMod navigation state hardening (2026-11-25)
- Восстановление последней открытой страницы теперь проверяет сохранённый идентификатор по известному набору страниц оболочки. Неизвестное/устаревшее значение безопасно возвращает пользователя на главный экран вместо пустой страницы.
- Изменение касается только восстановления UI-состояния; имена существующих страниц, настройки и их контракты не менялись.
- Статическая проверка исходника выполнена. Сборка, Gradle, Actions, APK и runtime-проверки не запускались.


## #91 — UltraMod toggle rows interaction polish (2026-11-25)
- Строки переключателей на страницах настроек теперь реагируют на нажатие по свободной области карточки: нажатие по тексту/карточке переключает тот же Switch, а нажатие непосредственно по Switch сохраняет стандартное поведение.
- Сохранение по-прежнему выполняется существующим обработчиком Switch через исходный BooleanSetting; restart prompt для настроек с rebootApp не обходится.
- Изменение локальное для оболочки. Сборка, Gradle, Actions, APK, установка и runtime-тесты не запускались.


## #92 — Проверка имени папки загрузок (2026-11-25)
- В диалоге выбора папки добавлена проверка имени подпапки: допускаются буквы Unicode, цифры, пробелы, дефис, подчёркивание, точка и круглые скобки; длина ограничена 64 символами.
- Запрещены пустые после нормализации специальные имена `.` и `..`, разделители каталогов и зарезервированные файловые символы. Пустой ввод по-прежнему использует прежнее значение по умолчанию `TikTok`.
- При ошибочном имени значение не сохраняется: поле показывает ошибку, диалог остаётся открытым. Сохранение по-прежнему использует существующий ключ `down_path` и формат `ROOT/subfolder`.
- Проверка статическая по исходнику. Сборка, Gradle, Actions, APK, установка и runtime-тесты не запускались.


## #93 — Независимое управление пропуском рекламы и проверка SIM-ввода (2026-11-25)
- Исправлена связка двух переключателей рекламы: `SKIP_ADS_AT_RENDER` теперь самостоятельно управляет пролистыванием рекламы на этапе рендера. Ранее этот механизм дополнительно зависел от `REMOVE_ADS`, поэтому выключение фильтрации полностью отключало отдельный переключатель автопропуска вопреки его подписи.
- Ручной ввод MCC+MNC ограничен 6 символами и проверяется на 5 или 6 цифр (3 цифры MCC и 2/3 цифры MNC). Некорректное значение не сохраняется, диалог остаётся открытым.
- Ручное название оператора ограничено 64 символами и не может быть пустым.
- После смены страны интерфейс явно просит выбрать оператора заново, чтобы обновить MCC+MNC под новую страну. Существующие значения не очищаются автоматически.
- Перепроверено: `SettingsStatus.load()` вызывается при открытии оболочки, а флаги доступности устанавливаются инъекциями соответствующих патчей; скрытые поля `CLEAR_DISPLAY` и `REMEMBERED_SPEED` обслуживаются своими patch-классами и не являются пользовательскими переключателями оболочки.
- Сборка, Gradle, GitHub Actions, APK, установка и runtime-тесты не запускались. Поведение подтверждено только статическим чтением и сопоставлением исходников.


## #94 — Поисковые списки и компактные экраны UltraMod (2026-11-25)
- Продолжен статический UX-аудит оболочки на `ultramod/experimental`; `ultramod/prototype` не затрагивалась.
- В поисковых списках стран и операторов добавлено явное пустое состояние «Ничего не найдено», чтобы пустой результат фильтрации не выглядел как сломанный/пустой диалог.
- Высота списков теперь адаптируется к высоте экрана (в пределах 180–360 dp), вместо фиксированных 360 dp. Это снижает риск переполнения диалога на компактных устройствах.
- Сохранены существующие настройки, ключи, данные каталога, алгоритмы фильтрации и порядок сортировки.
- Проверена правка по исходному коду и контексту Android View API. Сборка, Gradle, GitHub Actions, APK, установка и runtime-тесты не запускались; фактический вид диалогов на устройствах ещё требует проверки.


## #95 — Перевод статусов операторов в интерфейсе (2026-11-25)
- Проверен полный справочник операторов в `networks.min.json`: 3 042 записи и 13 уникальных статусов.
- Все статусы, которые пользователь видит в списке операторов, теперь отображаются на русском языке (например, Operational → «Работает», Not operational → «Не работает», Planned → «Планируется»).
- Исходные значения статусов внутри каталога и сортировка Operational-first не менялись; поиск поддерживает как исходное, так и русское название статуса.
- Имена операторов, MCC/MNC и структура данных сохранены без изменений. Сборка и runtime-проверки не запускались.


## #96 — Локализация видимого расположения загрузок (2026-11-25)
- Продолжен перевод видимой части оболочки: подпись главного экрана «TIKTOK EXTENSION» заменена на «РАСШИРЕНИЕ TIKTOK».
- Отображаемый путь загрузок теперь показывает локализованное название корневой папки («Камера (DCIM)», «Фильмы», «Изображения») вместо внутреннего системного токена.
- Внутренние значения `DCIM`, `Movies`, `Pictures`, формат `ROOT/subfolder` и ключ `down_path` не менялись; сохранение и потребители пути не затронуты.
- Проверка только статическая, сборка и runtime-проверки отложены до восстановления лимита.


## #97 — Стабильный контейнер Fragment при пересоздании Activity (2026-11-25)
- В `TikTokActivityHook` динамический ID контейнера заменён на постоянный ID, чтобы FragmentManager мог сопоставить восстановленный Fragment с новым деревом View после пересоздания Activity.
- Инициализация теперь добавляет оболочку только если Fragment в этом контейнере ещё не восстановлен; существующий экземпляр не заменяется без необходимости.
- Это сохраняет сохранённую страницу оболочки через штатный `onSaveInstanceState` и уменьшает риск потерять UI-состояние при повороте/пересоздании Activity.
- Проверено статически по Android Fragment API и текущему lifecycle hook. Runtime-проверка пересоздания Activity и сборка будут выполнены после восстановления лимита.


## #98 — Восстановление прокрутки оболочки после пересоздания Activity (2026-11-25)
- В состояние Fragment теперь сохраняется не только текущая страница, но и вертикальная позиция ScrollView.
- После пересоздания Activity оболочка возвращает пользователя на ту же страницу и к прежней позиции прокрутки; обычный переход между страницами по-прежнему открывает новую страницу сверху.
- Ссылка на ScrollView очищается в `onDestroyView()`; восстановление прокрутки выполняется после layout через `post()`, чтобы размеры контента уже были рассчитаны.
- Проверка статическая. Реальное восстановление после поворота экрана/пересоздания Activity будет проверено только после сборки и запуска на устройстве.


## #99 — Null-safety в фильтрах ленты (2026-11-25)
- **ShopFilter.java**: результат `getShareUrl()` теперь проверяется на `null` до вызова `contains`, чтобы отсутствующий URL не приводил к NPE.
- **FeedItemsFilter.java**: добавлены ранние проверки `null` для `FeedItemList`, `FollowFeedList` и внутреннего списка; null-элементы `FollowFeedList` и null `Aweme` по-прежнему пропускаются.
- Контракты Settings, patch-injection и критерий Shop-фильтра (`placeholder_product_id`) не менялись.
- Проверка статическая. Совместимость с реальными данными TikTok и runtime-поведение требуют отдельной сборки и проверки на устройстве.

## #100 — Восстановление целостности UltraModSettingsFragment и статический аудит (2026-11-25)
- Файл `UltraModSettingsFragment.java` восстановлен из проверенного blob `d21a812c288bdbb569638777ce895291263dc90b` (коммит `8d90559db5e5a2d67d86a5059cac84d06502a62c`). Повторное чтение подтвердило 797 строк и совпадение blob SHA.
- Подтверждены по исходнику lifecycle-механизмы: сохранение текущей страницы и scrollY, восстановление прокрутки через `post()` после layout, очистка ссылок в `onDestroyView()`.
- Ветка `ultramod/prototype` не изменялась: сравнение с baseline `3fbb0c2b1714a4d6deb1e8553c7df914d7308add` вернуло identical, ahead 0 / behind 0.
- Ранее внесённые null-safety изменения в `ShopFilter` и `FeedItemsFilter` остаются частью предыдущих коммитов; отдельная runtime-проверка не выполнялась.
- UX-diff для SIM Spoof подготовлен в отчёте, но не применён к большому Java-файлу.
- Сборка, Gradle, CI, APK и runtime-тесты не запускались.

## #101 — Контрольная точка UltraMod перед ожиданием восстановления CI (2026-11-25)
- Зафиксировано состояние ветки `ultramod/experimental`; базовая ветка `ultramod/prototype` остаётся неприкосновенной.
- Оболочка настроек: главная страница и категории рекламы, фильтров, загрузок и SIM Spoof; русификация видимых элементов; поиск стран и операторов по офлайн-каталогу; валидация; сохранение выбранной страницы и позиции прокрутки; стабильный Fragment-контейнер и обработка lifecycle.
- SIM Spoof: справочник операторов MCC-MNC.dev интегрирован офлайн с атрибуцией CC BY-SA 4.0; выбор страны/оператора связан с существующими настройками ISO, MCC+MNC и имени оператора. Ручной ввод сохранён.
- UX-правки оболочки: адаптивная ширина карточек, безопасное восстановление неизвестной страницы, нажатие по строке переключателя, проверка имени папки загрузок, пустые состояния и адаптивная высота поисковых списков, перевод статусов операторов и видимых путей загрузок.
- Исправления фильтров: null-safety в ShopFilter и FeedItemsFilter; независимая работа переключателя пропуска рекламы на этапе рендера.
- Исследовательский журнал содержит записи по #101; отчёт аудита Grok восстановлен. UltraModSettingsFragment.java восстановлен и статически проверен; prototype сравнивалась с baseline и оставалась identical.
- Не завершены/не подтверждены: сборка APK и компиляция; установка и runtime-проверки; фактическая проверка UI, фильтров, SIM Spoof, поворота экрана и длительной работы на устройстве. Предложенные дополнительные UX-улучшения SIM Spoof (динамические подсказки/summary и обновление страницы региона при переключении) не внесены.
- Подпись APK уже настроена в существующей GitHub-схеме; не генерировать новый keystore и не менять ключи/пароли. GitVerse и self-hosted runner не настраивать пока не потребуется.
- До восстановления лимита GitHub Actions сборки, Gradle, Actions и runtime-тесты не запускать. Продолжать только безопасную статическую работу в `ultramod/experimental`; после восстановления лимита выполнить согласованную сборку и проверку APK.

## #102 — Подготовка альтернативной сборки GitVerse (2026-11-25)
- В ветке `ultramod/experimental` добавлен отдельный workflow `.gitverse/workflows/tiktok-patcher.yml` (коммит `0c1cce87240c1abefa327616f30b4c98e8e19f80`). Исходный GitHub workflow `.github/workflows/tiktok-patcher.yml` не изменён.
- Workflow предназначен для ручного запуска через GitVerse; автоматический запуск по расписанию и при импорте/пуше не включён.
- Сохранена существующая последовательность загрузки TikTok APK, сборки ReVanced Patches, merge/patch, проверки пакета/версии/сертификата и подписи APK. Используются прежние секреты подписи `KEYSTORE_B64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`; новый keystore не создавался.
- Для GitHub Packages заменены автоматические GitHub credentials на отдельные GitVerse variable `GH_PACKAGES_USERNAME` и secret `GH_PACKAGES_TOKEN`; требуется токен GitHub с правом чтения пакетов. Секреты `GPLAYDL_AUTH_JSON_B64` и переменная `GPLAYDL_DISPENSER` сохранены по исходной схеме.
- Убрана публикация GitHub Release; результатом GitVerse workflow будет APK artifact с хранением 7 дней. Для первого запуска рекомендуется вариант `original`, чтобы ограничить расход времени и места.
- Используются версии стандартных actions, приведённые к документированным совместимым major versions (checkout v4, setup-python v6, setup-java v4, upload-artifact v4).
- Выполнена статическая проверка добавленного YAML по ключевым условиям: только ручной запуск, отсутствие GitHub Release/GITHUB_TOKEN и GitHub-only actor/ref/token expressions, наличие секретов подписи и GitHub Packages. Фактическая проверка синтаксиса раннером и сборка не выполнялись.
- GitVerse-репозиторий ещё не создан/не подключён; секреты и переменные GitVerse ещё не добавлены. CI/CD и запуск сборки не включались. GitHub workflow и исходники мода не изменялись; `ultramod/prototype` не затрагивалась.

## #103 — Исправление подготовки Android SDK для GitVerse workflow (2026-11-25)
- По первому реальному запуску GitVerse зафиксировано: TikTok 47.9.3 скачан и ZIP/APK проверен успешно (лог: «No errors detected in compressed data», базовый APK 461 MB); сборка остановилась на поиске `/build-tools/*/aapt`, exit code 2.
- Причина в workflow: Android SDK Build Tools не устанавливались явно, а дальнейшие шаги рассчитывают на `ANDROID_SDK_ROOT/build-tools/*/aapt` и `apksigner`.
- В исходной ветке `ultramod/experimental` обновлён `.gitverse/workflows/tiktok-patcher.yml`: добавлены `android-actions/setup-android@v3`, установка `platform-tools` и `build-tools;35.0.0`, настройка `ANDROID_HOME`/`ANDROID_SDK_ROOT` и предварительная проверка `aapt`/`apksigner`. Коммит: `f1c7296264457e3949e6063a3fa7e19b2f3bddb2`.
- Изменение внесено в GitHub-источник. Импортированный GitVerse-репозиторий `AlexVer/tiktok-ultramod-build2` является отдельной копией и автоматически не синхронизируется; для применения исправления его workflow-файл необходимо обновить отдельно. Никакие секреты, keystore, исходники приложения и логика загрузки/патчинга не менялись.
- Повторный запуск CI, Gradle и сборка после изменения не выполнялись; совместимость нового SDK setup action с конкретным GitVerse runner ещё предстоит подтвердить первым запуском.


## #104 — Исправление списка Android SDK packages для GitVerse (2026-11-25)
- По логу GitVerse run 2213050 установлено, что `sdkmanager` получил строку `platform-tools\\nbuild-tools;35.0.0` как одно имя пакета и завершился с exit code 1.
- Причина подтверждена исходником `android-actions/setup-android@v3`: вход `packages` разбивается методом `.split(' ')`, а не по строкам. Поэтому YAML block scalar с двумя строками передал некорректный единый пакет.
- В `.gitverse/workflows/tiktok-patcher.yml` списка пакетов изменён на одну строку: `packages: platform-tools build-tools;35.0.0`. Это сохраняет установку обоих требуемых компонентов и совместимо с логикой action. Коммит: `51f80b0d7c7001900c67025d749f5f813ed67b4d`.
- Изменение сохранено в GitHub-источнике ветки `ultramod/experimental`. Отдельный GitVerse-репозиторий автоматически не синхронизируется; перед следующим запуском нужно перенести обновлённый workflow туда.
- Не запускались GitHub Actions, GitVerse CI, Gradle или сборка APK. Не менялись исходники приложения, скачивание TikTok, секреты, keystore и подпись APK. `ultramod/prototype` не затрагивалась.


## #105 — Защита загрузки полного универсального TikTok APK для GitVerse (2026-11-25)
- В .gitverse/workflows/tiktok-patcher.yml переработано сопоставление версий APKPure и прямых ссылок: URL извлекается из соответствующей записи версии по маркеру APKJ, вместо привязки к последней встретившейся печатной строке. Если структура ответа не распознана, загрузка останавливается.
- Для APKPure запрашиваются обе архитектуры (arm64-v8a и armeabi-v7a). Для варианта original Google Play device-specific arm64 download больше не используется: workflow требует ровно один standalone APK через endpoint /b/APK/, запрещает XAPK/split APK и сверяет package name, versionName и versionCode с выбранной версией.
- Проверка универсальности перенесена на результат merge: tools/validate_apk_merge.py получил флаг --require-universal-original, который требует один исходный APK и обе группы native libraries (arm64-v8a, armeabi-v7a), а также проверяет сохранность DEX/native/assets payloads. После этого aapt повторно сверяет package/version merged APK с исходником.
- Существующая подпись и secrets не менялись; ultramod/prototype не изменялась. Выполнен только локальный синтетический тест логики проверки ZIP payloads и двух ABI; YAML/runner integration, реальная загрузка, Gradle, GitVerse workflow и APK-сборка не запускались.
- Изменения находятся в GitHub-источнике ветки ultramod/experimental (6 коммитов после #104; итоговый workflow commit 5c9c3b6b4576a0533457f3ff7cc45a5a088b9de1). GitVerse AlexVer/tiktok-ultramod-build2 остаётся отдельной копией и требует отдельного переноса workflow и tools/validate_apk_merge.py перед единственным согласованным запуском.
