package app.revanced.patches.tiktok.interaction.seekbar

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.shouldShowSeekBarMethod by gettingFirstMethodDeclaratively(
    "can not show seekbar, state: 1, not in resume"
)

internal val BytecodePatchContext.seekBarShowTypeSelectorMethod by gettingFirstMethodDeclaratively {
    definingClass("LX/06Bt;")
    name("LJIIJJI")
    returnType("I")
    parameterTypes("Z")
}
