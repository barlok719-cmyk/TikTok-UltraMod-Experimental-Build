package app.revanced.patches.tiktok.interaction.seekbar

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val SEEK_BAR_HOLDER = "LX/06Bt;"
private const val AWEME_DESCRIPTOR = "Lcom/ss/android/ugc/aweme/feed/model/Aweme;"
private const val INVERSE_EXPERIMENT_HOLDER = "LX/0991;"
private const val INVERSE_EXPERIMENT_METHOD = "LIZ"
private const val CURRENT_AWEME_FIELD = "LLJLL"

@Suppress("unused")
val showSeekbarPatch = bytecodePatch(
    name = "Show seekbar",
    description = "Shows the seekbar on eligible ordinary videos while preserving TikTok's native context and hide-state checks.",
) {
    compatibleWith(
        "com.ss.android.ugc.trill",
        "com.zhiliaoapp.musically",
    )

    apply {
        // Keep the native eligibility method intact. Bypass only the inverse
        // experiment check that otherwise suppresses the ordinary-video path.
        val eligibilityMethod = shouldShowSeekBarMethod
        if (eligibilityMethod.definingClass != SEEK_BAR_HOLDER ||
            eligibilityMethod.name != "LJI" ||
            eligibilityMethod.parameterTypes.map { it.toString() } != listOf(AWEME_DESCRIPTOR) ||
            eligibilityMethod.returnType != "Z"
        ) {
            throw PatchException(
                "Show seekbar: unexpected eligibility method " +
                    "${eligibilityMethod.definingClass}->${eligibilityMethod.name}" +
                    "(${eligibilityMethod.parameterTypes.joinToString()})${eligibilityMethod.returnType}.",
            )
        }

        val eligibilityImplementation = eligibilityMethod.implementation
            ?: throw PatchException("Show seekbar: eligibility method has no implementation.")
        val eligibilityInstructions = eligibilityImplementation.instructions.toList()
        val inverseGateIndexes = eligibilityInstructions.indices.filter { index ->
            val reference = eligibilityInstructions[index].getReference<MethodReference>()
            reference != null &&
                reference.definingClass == INVERSE_EXPERIMENT_HOLDER &&
                reference.name == INVERSE_EXPERIMENT_METHOD &&
                reference.parameterTypes.map { it.toString() } == listOf(AWEME_DESCRIPTOR) &&
                reference.returnType == "Z"
        }
        if (inverseGateIndexes.size != 1) {
            throw PatchException(
                "Show seekbar: expected one inverse-experiment check in eligibility method, found ${inverseGateIndexes.size}.",
            )
        }

        val inverseMoveResultIndex = inverseGateIndexes.single() + 1
        val inverseMoveResult = eligibilityInstructions.getOrNull(inverseMoveResultIndex)
        if (inverseMoveResult?.opcode != Opcode.MOVE_RESULT) {
            throw PatchException("Show seekbar: inverse-experiment call is not followed by move-result.")
        }
        val inverseResultRegister = (inverseMoveResult as? OneRegisterInstruction)?.registerA
            ?: throw PatchException("Show seekbar: could not resolve inverse-experiment result register.")
        if (inverseResultRegister !in 0..15) {
            throw PatchException("Show seekbar: inverse-experiment result register is not encodable.")
        }

        // The following if-nez consumes this result. Set only this experiment
        // result to false; lifecycle, page, story, drag, mask, prompt and
        // prohibited-video checks continue to run unchanged.
        eligibilityMethod.addInstructions(
            inverseMoveResultIndex + 1,
            "const/4 v$inverseResultRegister, 0x0",
        )

        // TikTok's feed controller computes a native show type in this method.
        // Promote it to the existing visible type only when the full native
        // eligibility method accepts the controller's current Aweme.
        val selectorMethod = seekBarShowTypeSelectorMethod
        if (selectorMethod.definingClass != SEEK_BAR_HOLDER ||
            selectorMethod.name != "LJIIJJI" ||
            selectorMethod.parameterTypes.map { it.toString() } != listOf("Z") ||
            selectorMethod.returnType != "I"
        ) {
            throw PatchException(
                "Show seekbar: unexpected show-type selector " +
                    "${selectorMethod.definingClass}->${selectorMethod.name}" +
                    "(${selectorMethod.parameterTypes.joinToString()})${selectorMethod.returnType}.",
            )
        }

        val selectorImplementation = selectorMethod.implementation
            ?: throw PatchException("Show seekbar: show-type selector has no implementation.")
        if (selectorImplementation.registerCount != 5) {
            throw PatchException(
                "Show seekbar: expected 5 registers in show-type selector, found ${selectorImplementation.registerCount}.",
            )
        }

        val selectorInstructions = selectorImplementation.instructions.toList()
        val currentAwemeFields = selectorInstructions.filter { instruction ->
            val reference = instruction.getReference<FieldReference>()
            reference != null &&
                reference.definingClass == SEEK_BAR_HOLDER &&
                reference.name == CURRENT_AWEME_FIELD &&
                reference.type == AWEME_DESCRIPTOR
        }
        if (currentAwemeFields.size != 1) {
            throw PatchException(
                "Show seekbar: expected one current-Aweme field in show-type selector, found ${currentAwemeFields.size}.",
            )
        }

        val returnIndexes = selectorInstructions.indices.filter { index ->
            selectorInstructions[index].opcode == Opcode.RETURN
        }
        if (returnIndexes.size != 2) {
            throw PatchException(
                "Show seekbar: expected two native return paths in show-type selector, found ${returnIndexes.size}.",
            )
        }

        val returnRegisters = returnIndexes.map { index ->
            (selectorInstructions[index] as? OneRegisterInstruction)?.registerA
                ?: throw PatchException("Show seekbar: could not resolve a show-type return register.")
        }.distinct()
        if (returnRegisters != listOf(2)) {
            throw PatchException(
                "Show seekbar: unexpected show-type return register(s): ${returnRegisters.joinToString()}.",
            )
        }

        // Insert in reverse order so the original instruction indexes remain valid.
        returnIndexes.sortedDescending().forEach { returnIndex ->
            val keepNativeTypeLabel = "show_seekbar_keep_native_type_$returnIndex"
            selectorMethod.addInstructions(
                returnIndex,
                """
                    iget-object v1, p0, $SEEK_BAR_HOLDER->$CURRENT_AWEME_FIELD:$AWEME_DESCRIPTOR
                    if-eqz v1, :$keepNativeTypeLabel
                    invoke-virtual {p0, v1}, $SEEK_BAR_HOLDER->LJI($AWEME_DESCRIPTOR)Z
                    move-result v0
                    if-eqz v0, :$keepNativeTypeLabel
                    const/16 v2, 0x64
                    :$keepNativeTypeLabel
                """,
            )
        }
    }
}
