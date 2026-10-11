@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.maxrave.simpmusic.ui.component

import com.maxrave.simpmusic.ui.theme.LocalAdvancedDialogAnimations
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.*

private fun descendants(node: SemanticsNode): List<SemanticsNode> = listOf(node) + node.children.flatMap(::descendants)
private fun text(node: SemanticsNode) = node.config.getOrNull(SemanticsProperties.Text)?.joinToString(" ") { it.text }.orEmpty()
class DialogMotionSourceTest {
 @kotlin.test.Test
 fun sheetRowsUseTheirOwnTapAndRejectUnrelatedOrigins() {
 for (observeSheet in listOf(false,true)) {
  val origins=DialogOriginTracker()
  val sheet=mutableStateOf(false)
  val dialog=mutableStateOf(false)
  var captured: DialogMotionOrigin?=null
  val scene=ImageComposeScene(360,800) {
   CompositionLocalProvider(LocalAdvancedDialogAnimations provides true, LocalDialogOrigins provides origins) {
    MaterialTheme {
     Box(Modifier.fillMaxSize().dialogMotionSource()) {
      Button(onClick={sheet.value=true},modifier=Modifier.align(Alignment.TopEnd).padding(12.dp)) { Text("More") }
      // Match NowPlayingBottomSheet: dialog is composed beside, before, the sheet window.
      if(dialog.value) AppDialogMotion({dialog.value=false}) { motion,_ -> captured=motion.origin }
      if(sheet.value) ModalBottomSheet(
       onDismissRequest={sheet.value=false},
       
       sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true),
      ) {
       Card((if(observeSheet) Modifier.dialogMotionSource(clearPreviousOrigin=true) else Modifier).fillMaxWidth().padding(bottom=24.dp)) {
        Text("Lyrics Provider",Modifier.fillMaxWidth().then(if(observeSheet) Modifier.dialogMotionClickable { dialog.value=true } else Modifier.clickable { dialog.value=true }).padding(24.dp))
        Text("Another dialog",Modifier.fillMaxWidth().then(if(observeSheet) Modifier.dialogMotionClickable { dialog.value=true } else Modifier.clickable { dialog.value=true }).padding(24.dp))
       }
      }
     }
    }
   }
  }
  var time=0L
  fun frames(n:Int=45) {repeat(n){time+=16_000_000;scene.render(time).close()}}
  fun row(label:String)=scene.semanticsOwners.flatMap {descendants(it.rootSemanticsNode)}.first {text(it)==label && it.config.getOrNull(SemanticsActions.OnClick)!=null}
  fun tap(position:Offset){scene.sendPointerEvent(PointerEventType.Press,position,type=PointerType.Touch);scene.sendPointerEvent(PointerEventType.Release,position,type=PointerType.Touch);frames(2)}
  frames()
  val opener=row("More").boundsInRoot.center
  tap(opener);frames()
  val source=row("Lyrics Provider").boundsInRoot.center
  tap(source)
  check(dialog.value)
  if(!observeSheet) {
   check(captured==null) {"An unanchored row reused an unrelated opener: $captured"}
   println("Passed: unanchored sheet rows use the centered fallback")
  } else {
   check((captured!!.center-source).getDistance()<1f) {"First dialog origin $captured != row tap $source"}
   dialog.value=false;frames()
   val second=row("Another dialog").boundsInRoot.center
   tap(second);check((captured!!.center-second).getDistance()<1f)
   dialog.value=false;frames()
   // Waiting past the old tap timeout must still use the newly tapped row.
   Thread.sleep(1250);tap(source);check((captured!!.center-source).getDistance()<1f)
   dialog.value=false;frames()
   // A drag clears the previous tap rather than anchoring a later non-touch action.
   origins.record(Rect(opener-Offset(10f,10f),opener+Offset(10f,10f)))
   scene.sendPointerEvent(PointerEventType.Press,source,type=PointerType.Touch)
   scene.sendPointerEvent(PointerEventType.Move,source+Offset(0f,-80f),type=PointerType.Touch)
   scene.sendPointerEvent(PointerEventType.Release,source+Offset(0f,-80f),type=PointerType.Touch)
   frames();check(origins.takeRecent()==null)
   // Re-entering the sheet invalidates the opener even before a new pointer gesture.
   sheet.value=false;frames();origins.record(Rect(opener-Offset(10f,10f),opener+Offset(10f,10f)));sheet.value=true;frames()
   check(origins.takeRecent()==null)
   row("Lyrics Provider").config[SemanticsActions.OnClick].action!!.invoke();frames(2)
   check(dialog.value && (captured!!.center-source).getDistance()<1f) {"Accessibility entry inherited an unrelated origin"}
   println("Passed: first and repeated row taps, another row, delayed opening, drag clearing, and accessibility entry")
  }
  scene.close()
 }
}

}
