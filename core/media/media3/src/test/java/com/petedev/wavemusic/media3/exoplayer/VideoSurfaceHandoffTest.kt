package com.petedev.wavemusic.media3.exoplayer

import android.view.SurfaceView
import android.view.ViewTreeObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.lang.reflect.Proxy

@androidx.annotation.OptIn(UnstableApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
@GraphicsMode(GraphicsMode.Mode.LEGACY)
class VideoSurfaceHandoffTest {
    // Model the window compositor separately from main-thread View properties. Setting alpha
    // does not commit a frame, and a prepared decoder can render as soon as it is attached.
    private open class CompositedSurface : SurfaceView(RuntimeEnvironment.getApplication()) {
        override fun isAttachedToWindow() = true
        override fun isHardwareAccelerated() = true

        fun commitFrame() {
            val capture = ViewTreeObserver::class.java.getDeclaredMethod("captureFrameCommitCallbacks")
                .apply { isAccessible = true }
            @Suppress("UNCHECKED_CAST")
            val callbacks = capture.invoke(viewTreeObserver) as? List<Runnable>
            callbacks?.forEach { it.run() }
        }
    }

    private class Decoder(val itemId: String, val trace: MutableList<String>) {
        val listeners = mutableListOf<Player.Listener>()
        val player = Proxy.newProxyInstance(Player::class.java.classLoader, arrayOf(Player::class.java)) { proxy, method, args ->
            when (method.name) {
                "getCurrentMediaItem" -> MediaItem.Builder().setMediaId(itemId).build()
                "addListener" -> { listeners += args!![0] as Player.Listener; null }
                "removeListener" -> { listeners -= args!![0] as Player.Listener; null }
                "clearVideoSurface" -> { trace += "$itemId:clear"; null }
                "setVideoSurfaceView" -> {
                    val surface = args!![0] as? SurfaceView
                    trace += "$itemId:bind:${surface?.alpha}"
                    // Model a preloaded decoder producing its first frame immediately on bind.
                    listeners.toList().forEach { it.onRenderedFirstFrame() }
                    null
                }
                "equals" -> proxy === args!![0]
                "hashCode" -> System.identityHashCode(proxy)
                "toString" -> "Decoder($itemId)"
                else -> when (method.returnType) {
                    Boolean::class.javaPrimitiveType -> false
                    Int::class.javaPrimitiveType -> 0
                    Long::class.javaPrimitiveType -> 0L
                    Float::class.javaPrimitiveType -> 0f
                    Double::class.javaPrimitiveType -> 0.0
                    else -> null
                }
            }
        } as Player
    }

    @Test
    fun audioToVideoCannotBindAnInitiallyOpaqueSurface() {
        val trace = mutableListOf<String>()
        val video = Decoder("incoming-video", trace)
        val forwarding = DelegatingForwardingPlayer(video.player)
        val surface = SurfaceView(RuntimeEnvironment.getApplication())
        assertEquals(1f, surface.alpha, 0f)
        forwarding.setVideoSurfaceView(surface)
        assertEquals(listOf("incoming-video:bind:0.0"), trace)
    }

    @Test
    fun cachedNextVideoIsHiddenBeforeTheOutgoingDecoderDisconnects() {
        val trace = mutableListOf<String>()
        val old = Decoder("outgoing", trace)
        val next = Decoder("cached-next", trace)
        val forwarding = DelegatingForwardingPlayer(old.player)
        val surface = SurfaceView(RuntimeEnvironment.getApplication())
        forwarding.setVideoSurfaceView(surface)
        surface.alpha = 1f // The UI has completed the outgoing video's fade.
        trace.clear()
        forwarding.addListener(object : Player.Listener {
            override fun onRenderedFirstFrame() {
                trace += "first-frame:${surface.alpha}"
            }
        })
        forwarding.swapDelegate(next.player)
        assertEquals(listOf("outgoing:clear", "cached-next:bind:0.0", "first-frame:0.0"), trace)
        assertEquals(0f, surface.alpha, 0f)
    }

    @Test
    fun replacingTheNativeViewHidesItsBufferBeforeFirstFrameCallbacks() {
        val trace = mutableListOf<String>()
        val decoder = Decoder("video", trace)
        val forwarding = DelegatingForwardingPlayer(decoder.player)
        val oldSurface = SurfaceView(RuntimeEnvironment.getApplication())
        forwarding.setVideoSurfaceView(oldSurface)
        oldSurface.alpha = 1f
        val replacement = SurfaceView(RuntimeEnvironment.getApplication())
        trace.clear()
        forwarding.setVideoSurfaceView(replacement)
        assertEquals(listOf("video:bind:0.0"), trace)
        assertEquals(1f, oldSurface.alpha, 0f)
        assertEquals(0f, replacement.alpha, 0f)
    }

    @Test
    fun clearingAnUnrelatedViewDoesNotDropTheTrackedHandoffSurface() {
        val trace = mutableListOf<String>()
        val old = Decoder("old", trace)
        val next = Decoder("next", trace)
        val forwarding = DelegatingForwardingPlayer(old.player)
        val surface = SurfaceView(RuntimeEnvironment.getApplication())
        forwarding.setVideoSurfaceView(surface)
        surface.alpha = 1f
        forwarding.clearVideoSurfaceView(SurfaceView(RuntimeEnvironment.getApplication()))
        trace.clear()
        forwarding.swapDelegate(next.player)
        assertEquals(listOf("old:clear", "next:bind:0.0"), trace)
    }

    @Test
    fun rebindingTheSameViewDoesNotRestartItsCompletedFade() {
        val trace = mutableListOf<String>()
        val decoder = Decoder("video", trace)
        val forwarding = DelegatingForwardingPlayer(decoder.player)
        val surface = SurfaceView(RuntimeEnvironment.getApplication())
        forwarding.setVideoSurfaceView(surface)
        surface.alpha = 1f
        trace.clear()
        forwarding.setVideoSurfaceView(surface)
        assertEquals(listOf("video:bind:1.0"), trace)
    }

    @Test
    fun selectingTheSameDecoderKeepsTheCurrentFrameVisible() {
        val trace = mutableListOf<String>()
        val decoder = Decoder("video", trace)
        val forwarding = DelegatingForwardingPlayer(decoder.player)
        val surface = SurfaceView(RuntimeEnvironment.getApplication())
        forwarding.setVideoSurfaceView(surface)
        surface.alpha = 1f
        trace.clear()
        forwarding.swapDelegate(decoder.player)
        assertEquals(emptyList<String>(), trace)
        assertEquals(1f, surface.alpha, 0f)
    }

    @Test
    fun preparedVideoCannotRenderBeforeTheCoverFrameIsCommitted() {
        val trace = mutableListOf<String>()
        val old = Decoder("outgoing", trace)
        val next = Decoder("cached-next", trace)
        val forwarding = DelegatingForwardingPlayer(old.player)
        val surface = CompositedSurface()
        forwarding.setVideoSurfaceView(surface)
        surface.alpha = 1f
        trace.clear()

        forwarding.swapDelegate(next.player, waitForVideoSurfaceFrame = true)
        assertEquals(listOf("outgoing:clear"), trace)
        assertEquals(next.player, forwarding.wrappedPlayer)

        trace += "cover:committed"
        surface.commitFrame()
        assertEquals(listOf("outgoing:clear", "cover:committed", "cached-next:bind:0.0"), trace)
    }

    @Test
    fun rapidNextDiscardsTheSupersededAttachment() {
        val trace = mutableListOf<String>()
        val old = Decoder("outgoing", trace)
        val skipped = Decoder("skipped", trace)
        val last = Decoder("last", trace)
        val forwarding = DelegatingForwardingPlayer(old.player)
        val surface = CompositedSurface()
        forwarding.setVideoSurfaceView(surface)
        trace.clear()

        forwarding.swapDelegate(skipped.player, waitForVideoSurfaceFrame = true)
        forwarding.swapDelegate(last.player, waitForVideoSurfaceFrame = true)
        surface.commitFrame()
        assertEquals(listOf("outgoing:clear", "skipped:clear", "last:bind:0.0"), trace)
    }

    @Test
    fun clearingTheSurfaceBeforeCommitCancelsItsPendingAttachment() {
        val trace = mutableListOf<String>()
        val old = Decoder("outgoing", trace)
        val next = Decoder("cached-next", trace)
        val forwarding = DelegatingForwardingPlayer(old.player)
        val surface = CompositedSurface()
        forwarding.setVideoSurfaceView(surface)
        trace.clear()

        forwarding.swapDelegate(next.player, waitForVideoSurfaceFrame = true)
        forwarding.clearVideoSurface()
        surface.commitFrame()
        assertEquals(listOf("outgoing:clear", "cached-next:clear"), trace)
    }

    @Test
    fun replacingTheSurfaceBeforeCommitDoesNotBindItTwice() {
        val trace = mutableListOf<String>()
        val old = Decoder("outgoing", trace)
        val next = Decoder("cached-next", trace)
        val forwarding = DelegatingForwardingPlayer(old.player)
        val surface = CompositedSurface()
        forwarding.setVideoSurfaceView(surface)
        trace.clear()

        forwarding.swapDelegate(next.player, waitForVideoSurfaceFrame = true)
        forwarding.setVideoSurfaceView(CompositedSurface())
        surface.commitFrame()
        assertEquals(listOf("outgoing:clear", "cached-next:bind:0.0"), trace)
    }

    @Test
    fun softwareWindowLeavesATraversalBeforeAttaching() {
        val trace = mutableListOf<String>()
        val old = Decoder("outgoing", trace)
        val next = Decoder("cached-next", trace)
        val forwarding = DelegatingForwardingPlayer(old.player)
        val frameCallbacks = mutableListOf<Runnable>()
        val surface = object : CompositedSurface() {
            override fun isHardwareAccelerated() = false
            override fun postOnAnimation(action: Runnable) { frameCallbacks += action }
        }
        forwarding.setVideoSurfaceView(surface)
        trace.clear()

        forwarding.swapDelegate(next.player, waitForVideoSurfaceFrame = true)
        assertEquals(listOf("outgoing:clear"), trace)
        frameCallbacks.removeAt(0).run()
        assertEquals(listOf("outgoing:clear"), trace)
        frameCallbacks.removeAt(0).run()
        assertEquals(listOf("outgoing:clear", "cached-next:bind:0.0"), trace)
    }
}
