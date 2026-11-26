package app.revanced.patches.tiktok.profile

import app.revanced.patcher.*
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference

private const val PROFILE_BACKGROUND_HOLDER = "LX/0Ngv;"
private const val PROFILE_BACKGROUND_READER = "LIZ"
private const val PROFILE_BACKGROUND_ALLOW_LIST_KEY = "profile_bg_in_allow_list"

/*
 * TikTok 47.9.3: LX/0Ngv.LIZ() reads profile_bg_in_allow_list and stores the
 * result in its UID-scoped LX/0NhE snapshot. That same snapshot controls the
 * component-registration gate and is sent by both self/other profile requests.
 *
 * Keep the fingerprint narrow. If TikTok moves or changes the holder/read path,
 * the patch must fail rather than silently changing an unrelated AB value.
 */
internal val BytecodePatchContext.profileBackgroundAllowListReader by gettingFirstMethodDeclaratively {
    definingClass(PROFILE_BACKGROUND_HOLDER)
    name(PROFILE_BACKGROUND_READER)
    returnType("LX/0NhE;")
    parameterTypes()
}

@Suppress("unused")
val profileBackgroundPatch = bytecodePatch(
    name = "Profile background",
    description = "Enables the native TikTok profile background editor by setting its client-side " +
        "allow-list snapshot to enabled. TikTok's server may still reject saving or omit the " +
        "background from profile responses.",
) {
    compatibleWith("com.zhiliaoapp.musically")

    apply {
        val reader = profileBackgroundAllowListReader
        if (reader.definingClass != PROFILE_BACKGROUND_HOLDER ||
            reader.name != PROFILE_BACKGROUND_READER ||
            reader.parameterTypes.isNotEmpty() ||
            reader.returnType != "LX/0NhE;"
        ) {
            throw PatchException(
                "Profile background: expected ${PROFILE_BACKGROUND_HOLDER}->${PROFILE_BACKGROUND_READER}() " +
                    "to return the account-scoped snapshot, found " +
                    "${reader.definingClass}->${reader.name}(${reader.parameterTypes.joinToString()})${reader.returnType}",
            )
        }

        val implementation = reader.implementation
            ?: throw PatchException("Profile background: allow-list reader has no implementation.")
        val instructions = implementation.instructions.toList()

        val keyIndexes = instructions.indices.filter { index ->
            instructions[index].getReference<StringReference>()?.string == PROFILE_BACKGROUND_ALLOW_LIST_KEY
        }
        if (keyIndexes.size != 1) {
            throw PatchException(
                "Profile background: expected one $PROFILE_BACKGROUND_ALLOW_LIST_KEY key in " +
                    "${reader.definingClass}->${reader.name}, found ${keyIndexes.size}.",
            )
        }

        // The AB store's integer read follows this key and its result is consumed
        // immediately by the existing snapshot-construction logic.
        val keyIndex = keyIndexes.single()
        val readIndex = (keyIndex + 1 until instructions.size).firstOrNull { index ->
            instructions[index].getReference<MethodReference>()?.returnType == "I"
        } ?: throw PatchException(
            "Profile background: no integer AB read follows $PROFILE_BACKGROUND_ALLOW_LIST_KEY.",
        )

        val moveResultIndex = readIndex + 1
        val moveResult = instructions.getOrNull(moveResultIndex)
        if (moveResult?.opcode != Opcode.MOVE_RESULT) {
            throw PatchException(
                "Profile background: integer AB read is not followed by move-result; refusing to patch.",
            )
        }

        val valueRegister = (moveResult as? OneRegisterInstruction)?.registerA
            ?: throw PatchException("Profile background: could not resolve the AB result register.")
        if (valueRegister !in 0..255) {
            throw PatchException(
                "Profile background: AB result register v$valueRegister is outside the supported encoding range.",
            )
        }

        val forceEnabled = if (valueRegister <= 15) {
            "const/4 v$valueRegister, 0x1"
        } else {
            "const/16 v$valueRegister, 0x1"
        }

        // Override only the value stored in the account-scoped snapshot. The
        // original avatar-layout/isEnableAvatarOpt checks in LJFF(Z) remain intact.
        reader.addInstructions(moveResultIndex + 1, forceEnabled)
    }
}
