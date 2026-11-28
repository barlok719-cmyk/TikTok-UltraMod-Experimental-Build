# GROK_ICQ_RESEARCH.md

**Independent research report**  
Repository: `barlok719-cmyk/TikTok-UltraMod-Experimental-Build`  
Date of analysis: 2026-11-28  
Source material: public files `ICQ_RESEARCH_LOG.md`, `ICQ_SMILES_BRIEF.md`, and related static DEX findings recorded therein.  
Scope: animated ICQ smileys (GIF) support in TikTok Ultra Mod — DM send/receive and comments.  
Method: read-only review of existing research journals and confirmed DEX anchors. No new APK decompilation, no source/workflow/APK changes, no builds, no patches.

---

## 1. Executive summary

Existing research has mapped a substantial portion of the **sticker / emoji / GIF panel infrastructure** used by DM (chatroom) and, partially, comments. A native page-model path for **GIPHY** exists and is wired into `StickerPanelAssemViewModel`, but GIPHY is **absent from the default category list** returned by the primary DM service implementation in the inspected APK. Consequently the user-visible “GIF” tab is **not yet proven** to be the `LX/0NKZ.GIPHY` page.

Specific class names requested in the task statement (`GifContent`, `GifEmojiViewModel`, `SearchGifViewHolder`, `CommentGifPublishRequestModel`) **do not appear** in any of the researched journals or code-search results inside this repository. They remain unconfirmed hypotheses or may originate from external / prior unpublished analysis. They must not be treated as proven anchors.

MessageListProtocolEnum contains a GIF-related protocol among specialised protocols; ordinary text and a dedicated GIF receive binder for ICQ custom animations are still unresolved.

**Can the stock TikTok GIF mechanism be reused for custom ICQ animations?**  
**Partially yes at the panel / page-model layer** (tab registration + page descriptor).  
**Not yet proven** for the full send → IM-Core payload → receive → animation display path, because:
- the concrete content model / payload fields for GIF messages are not traced;
- the receive-side renderer for GIF (or any custom animated content) is not identified;
- server-side identifiers / Giphy-style IDs required by the stock path are unknown.

---

## 2. Confirmed classes, methods and call chains (panel / selection side)

All items below are taken from successful static DEX inspections recorded in `ICQ_RESEARCH_LOG.md` (primarily runs #61–#66 and related follow-ups). Source APK was a 57-DEX TikTok build (later confirmed 47.9.3 in other research).

### 2.1 Core service and page enum

| Symbol | Location (from research) | Role |
|--------|--------------------------|------|
| `IMStickerApi.getImStickerInputPanelService()` | API surface | Returns `LX/0NKm` |
| `LX/0NKm` | classes31.dex | Service interface. Key methods: `LIZJ(): List<LX/0NKZ>` (page categories), `LJIIJJI(..., LX/0NKQ)` (hand config to UIAssem) |
| `LX/0kOo` | classes22.dex | Concrete implementation of `LX/0NKm` (BasicIMStickerImpl path) |
| `LX/0NKZ` | classes32.dex | Enum of page types: `SYSTEM_EMOJI`, `CUSTOM_EMOJI`, `FAV_STICKER`, `POPULAR_STICKER`, **`GIPHY`**, `SET_STICKERS`, `CURATED_SETS`, `AIMOJI`, `AISELF`, `STREAK_STICKER` |
| `LX/0NKa` / `LX/0NKo` | — | Fallback list providers (empty or alternative sets) |

**Confirmed chain (DM panel configuration):**
```
IMStickerApi.getImStickerInputPanelService()
  → LX/0NKm (impl LX/0kOo)
  → LIZJ() → List<LX/0NKZ>
  → (if empty) LX/0NKa.LIZ() / LX/0NKo
  → build LX/0NKQ config
  → LX/0NKm.LJIIJJI(layoutId=0x7f0a5b69, …, ChatroomEmojiPanelAssem, config)
```

`LX/0kOo.LIZJ()` returns, in order:
1. `FAV_STICKER`
2. `CUSTOM_EMOJI` (or `SYSTEM_EMOJI` depending on feature flag)
3. `CURATED_SETS`
4. `SET_STICKERS`

**GIPHY is not present in this list.**

### 2.2 View-model mapping and page models

| Symbol | Role |
|--------|------|
| `StickerPanelAssemViewModel` (classes10.dex) | Maps `List<LX/0NKZ>` → concrete page models |
| `StickerPanelAssemViewModel.N93(List)` | Switch / dispatch via generated table `LX/0hSx` |
| Mapping | `SYSTEM_EMOJI` → `LX/0MiB`; `CUSTOM_EMOJI` → `LX/0hSs`; `FAV_STICKER` → `LX/0Ymr`; `POPULAR_STICKER` → `LX/0Ykv`; **`GIPHY` → `LX/0Ymq`** |
| `LX/0Ymq` | Page-model singleton extending `LX/0Mi4`. Overrides: `fn() = 8`, icon resource `0x7f010828`, `gn()`, `in(Context)`. Inherited item-list methods return empty/null. |

**Proven:** GIPHY has a dedicated page-model branch.  
**Not proven:** that this page model ever receives a non-empty item list or drives a visible GIF feed in the inspected APK.

### 2.3 Panel hosts (DM vs comment)

**DM path (confirmed):**
```
ChatroomEmojiPanelAssem (extends BaseEmojiPanelAssem)
  → Iq(ViewGroup, int, boolean)
  → LX/0NKm.LIZ / LX/0NKn.LIZ(ViewGroup, Function0)
  → LX/0kOo.LJIIIZ(BasePanelContainerAssem)
  → assembles BasicStickerPanelAssemPanelAssem
  → hosts BasicStickerPanelAssem + StickerPanelAssemViewModel
```
Layouts (from research): content `0x7f0d11d2`, parent panel `0x7f0d11d3`. Panel-state VM: `LX/0NKL`.

**Comment path (partially confirmed):**
- `CommentStickerPanelAssem` in `MEMORY_COMMENT_AND_DM` mode re-uses `IMStickerApi.getImStickerInputPanelService()` for state/sizing methods (`LJ()` family).
- It does **not** call `LIZJ()` or receive `LX/0NKQ` in the inspected methods → shared service, but **not proven identical page list / tab host**.
- Separate comment keyboard path: `CommentEmojiPanelAssem` → `EmojiInputViewImplV2` → `LX/0kRW` / `LX/0kRX` (only system-emoji page). Adapter `LX/0fuk` belongs to this comment path, **not** the DM sticker tab strip.

**User observation (UI only):** same visual picker with tabs Stickers / GIF / Emoji in both DM and comments. Source-level identity of the host is still open.

### 2.4 Tab-strip adapter (comment path only)

- `LX/0fuk` (classes18.dex): RecyclerView adapter over `List<LX/0Mi3>`.
- `getItemViewType()` = page model `fn()`.
- Type 5 → text holder `LX/0gKt`; other types → icon holder `LX/0gKu`.
- Therefore a model with `fn() = 8` (`LX/0Ymq`) would use the icon-tab path.

**Correction recorded in research #66:** this adapter is only observed under the comment emoji path; it is **not** the proven DM tab-strip adapter.

---

## 3. DM message skeleton / receive side (relevant to GIF display)

### 3.1 Slot architecture (confirmed)

- `LX/0NAA` = `BaseSkeletonData(message, skeletonLayoutPowerCellData, slotsData)`.
- `slotsData` (`LIZJ`) is `Map<LX/0N9i, LX/0NAL>`.
- `LX/0NAL` = `SlotContent(data, shouldAssemble)`.
- `LX/0NAD.LIZIZ(BaseSkeletonData, LX/0N9i, Object, boolean)` inserts/clones slot content.
- 10 concrete `LX/0N9i` keys mapped to resource IDs (avatar, username, status, reaction, quote, notice, report, feedback, side/card/media `id/kzq`, etc.). None of them is proven to be the ordinary message-body text or GIF content slot.

### 3.2 Protocols

- 32+ implementations of `IMMessageListProtocol` / `SkeletonMessageListProtocol`.
- `MessageListProtocolEnum` includes specialised entries: `TEXT_BIG_EMOJI`, `TRANSLATE_TEXT`, `MARKDOWN_MSG`, **GIF**, cards, system messages, etc.
- **No ordinary plain-text / TEXT protocol enum** found. Ordinary type-7 text is suspected to use a default/legacy route; still unproven.
- GIF is present as a specialised protocol, but the concrete send/receive PowerCell, content model and view binder for GIF messages were **not** enumerated in the available reports.

### 3.3 TextContent / ordinary receive binder

Multiple focused scans (runs #56, #61, later) found UI accesses only for:
- big-emoji path (`TextBigEmojiAssem.Xq`);
- translation path (`TextTranslateAssem`);
- send-side helpers.

No standard incoming type-7 `TextContent` → TextView / Spannable binder was identified. `IMSkeletonLayoutPowerCell.W5` only writes tags (UUID, scene, msgType) and tracker metadata.

**Implication for ICQ GIF:** if custom animations are delivered as a specialised content type (or as GIF protocol), the corresponding receive PowerCell / slot assembler is still unknown. Re-using the ordinary text path is not currently viable because that path itself is unresolved.

---

## 4. What is proven vs what remains hypothesis

### Proven (static DEX evidence recorded in the journals)

1. Native page enum contains `GIPHY`.
2. `StickerPanelAssemViewModel` maps `GIPHY` → page model `LX/0Ymq` (fn=8, icon 0x7f010828).
3. DM panel is driven by `ChatroomEmojiPanelAssem` → `LX/0NKm` / `LX/0kOo` → `BasicStickerPanelAssem*`.
4. Default category list from `LX/0kOo.LIZJ()` does **not** include GIPHY.
5. Comment path re-uses the same service for some state methods but does not share the proven page-list configuration.
6. Message skeleton uses keyed slots; GIF appears in the protocol enum among specialised types.
7. Ordinary incoming text binder and GIF content model / renderer remain unidentified.

### Hypotheses / unconfirmed

- The user-visible “GIF” tab **is** (or is not) the `LX/0NKZ.GIPHY` page.
- Classes named `GifContent`, `GifEmojiViewModel`, `SearchGifViewHolder`, `CommentGifPublishRequestModel` exist in the APK and participate in the flows described in the task.
- Stock Giphy / GIF message payload can accept arbitrary custom animated resources (local files or custom IDs) without server-side Giphy identifiers.
- A new ICQ page model can be inserted into the category list and will automatically obtain a working item grid + send path.
- GIF messages travel through the same IM Core path as stickers and can be rendered by an existing PowerCell when a suitable content object is supplied.

None of the above may be treated as fact until additional static or runtime evidence is obtained.

---

## 5. Feasibility of using the stock TikTok GIF mechanism for custom ICQ animations

| Layer | Feasibility | Required data / work |
|-------|-------------|----------------------|
| Tab / panel registration | High (static evidence) | Add `GIPHY` (or new enum value) to the list returned by `LIZJ()` / service config, or replace `LX/0Ymq` behaviour. Need icon, optional title. |
| Page content / item list | Medium / unknown | `LX/0Ymq` currently returns empty item list. Must locate the real GIF feed provider, data model of a GIF item, and the RecyclerView / cell binder inside the page. |
| Selection → message formation | Unknown | Click handler on a GIF item, construction of the outgoing message object, and the call into IM Core are not traced. |
| IM Core send / server payload | Unknown | What identifiers (Giphy ID, URL, local asset key, msgType, content type) the server expects. Custom ICQ GIFs may require a different content type or local-only rendering. |
| Receive / display | Unknown | Incoming GIF protocol PowerCell, slot key, and animation view (likely a custom ImageView / Lottie / GIF decoder) are not identified. |
| Comments | Partial | Service reuse exists; full tab host and publish request model for GIF comments are untraced. |

**Conclusion:** the panel layer is the only part with enough confirmed surface to begin experimentation. Full end-to-end ICQ GIF support via the stock path is **not yet proven possible**; it remains a research hypothesis that depends on discovering the content model and receive binder.

---

## 6. Concrete next steps for implementation research

Strictly research-only; no patches until the required anchors are confirmed.

1. **Locate the active GIPHY insertion path**  
   Search all implementations / overrides of `LIZJ()` and all places that mutate the `List<LX/0NKZ>` or `LX/0NKQ` after the service returns. Determine under which AB / config / build flavour GIPHY appears.

2. **Trace `LX/0Ymq` consumers and item population**  
   Find every call site that obtains the GIPHY page model and any method that populates its item list or creates cells. Identify the GIF item data class and the network / local source of GIF metadata.

3. **Map selection → send**  
   From a GIF item click (or long-press) follow the call chain into message construction and the IM Core send API. Record the exact content type / msgType / fields written for a GIF message.

4. **Map receive → display**  
   From `MessageListProtocolEnum` GIF entry locate the corresponding `SkeletonMessageListProtocol` implementation, its receive PowerCell, the slot key it writes, and the final view that plays the animation.

5. **Search for the task-specific class names**  
   In the next successful DEX index run, explicitly search for strings / class names: `GifContent`, `GifEmojiViewModel`, `SearchGifViewHolder`, `CommentGifPublishRequestModel`, and related “Gif” / “Giphy” content classes. If they exist, add them to the call-graph.

6. **Comment publish path**  
   Trace `CommentGifPublishRequestModel` (if found) or the general comment GIF publish request; compare payload shape with DM GIF messages.

7. **Decide reuse strategy only after 1–4**  
   - Reuse GIPHY page model + replace item source and send payload, **or**  
   - Introduce a new `LX/0NKZ` value + new page model + new protocol,  
   - or treat ICQ GIFs as local-only stickers that never hit the server GIF path.

8. **Preserve research hygiene**  
   - Distinguish static evidence from runtime observation.  
   - Do not patch until the receive binder and payload fields are known.  
   - Keep Actions capacity checks before any new analyser runs.

---

## 7. Source references (existing repository files)

- `ICQ_RESEARCH_LOG.md` — primary journal (runs #53–#68 and experimental handoff).
- `ICQ_SMILES_BRIEF.md` — condensed brief of the same panel / GIPHY findings.
- Successful artefacts referenced inside the log (not re-downloaded for this report): 14835435503, 14833402957, 14831826719, 14827906782, etc.
- No smali line numbers are present in the journals; all anchors are class/method names and resource IDs extracted by the analysers.

---

## 8. Explicit non-actions taken by this report

- No existing files were modified.
- No APK, smali, patcher, workflow or source code was edited.
- No builds, decompilations or Actions runs were triggered.
- No branches or repository settings were changed.
- Only this new file `GROK_ICQ_RESEARCH.md` was added.

---

*End of independent research report.*
