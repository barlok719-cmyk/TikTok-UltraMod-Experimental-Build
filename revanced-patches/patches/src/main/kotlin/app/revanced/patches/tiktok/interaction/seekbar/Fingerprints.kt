package app.revanced.patches.tiktok.interaction.seekbar

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.shouldShowSeekBarMethod by gettingFirstMethodDeclaratively(
    "can not show seekbar, state: 1, not in resume"
)

internal val BytecodePatchContext.seekBarShowTypeSelectorMethod by gettingFirstMethodDeclaratively {
    definingClass("LX/06Bt;")
    name("LJIIJJI")
    returnType("I")
    parameterTypes("Z")
}
