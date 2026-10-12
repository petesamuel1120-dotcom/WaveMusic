package com.petedev.wavemusic.viewModel

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

class HomeFeedSessionTest {
    private val context = HomeFeedContext("account-a", "en", "US")

    @Test
    fun returningToAllAfterTenSecondsRestoresItsOwnFeedAndContinuation() {
        val clock = TestTimeSource()
        val session = HomeFeedSession<String>(clock)
        val all = session.beginRequest(null)
        assertNull(session.read(all, context))
        session.store(all, HomeFeedSession.Feed(listOf("all-song"), "all-page-2"))
        val relax = session.beginRequest("relax")
        assertNull(session.read(relax, context))
        session.store(relax, HomeFeedSession.Feed(listOf("relax-song"), "relax-page-2"))
        clock += 10.seconds

        assertEquals(HomeFeedSession.Feed(listOf("all-song"), "all-page-2"), session.read(session.beginRequest(null), context))
        assertEquals(HomeFeedSession.Feed(listOf("relax-song"), "relax-page-2"), session.read(session.beginRequest("relax"), context))
    }

    @Test
    fun pagesRemainCachedWithoutExtendingTheOriginalFreshnessWindow() {
        val clock = TestTimeSource()
        val session = HomeFeedSession<String>(clock)
        val request = session.beginRequest(null)
        session.read(request, context)
        session.store(request, HomeFeedSession.Feed(listOf("first"), "page-2"))
        clock += 4.minutes
        val paged = HomeFeedSession.Feed(listOf("first", "second"), null)
        assertTrue(session.updatePagination(request, paged))
        session.beginRequest("relax")
        assertEquals(paged, session.read(session.beginRequest(null), context))
        clock += 1.minutes
        assertNull(session.read(session.beginRequest(null), context))
    }

    @Test
    fun explicitRefreshBypassesOnlyTheSelectedCategory() {
        val session = HomeFeedSession<String>()
        val all = session.beginRequest(null)
        session.read(all, context)
        session.store(all, HomeFeedSession.Feed(listOf("all"), null))
        val relax = session.beginRequest("relax")
        session.store(relax, HomeFeedSession.Feed(listOf("relax"), null))
        assertNull(session.read(session.beginRequest(null), context, forceRefresh = true))
        assertNull(session.read(session.beginRequest(null), context))
        assertEquals(listOf("relax"), session.read(session.beginRequest("relax"), context)?.items)
    }

    @Test
    fun accountLanguageAndRegionChangesDiscardEveryPreviousCategory() {
        for (changed in listOf(context.copy(cookie = ""), context.copy(language = "de"), context.copy(region = "DE"))) {
            val session = HomeFeedSession<String>()
            val all = session.beginRequest(null)
            session.read(all, context)
            session.store(all, HomeFeedSession.Feed(listOf("all"), null))
            val relax = session.beginRequest("relax")
            session.store(relax, HomeFeedSession.Feed(listOf("relax"), null))
            assertNull(session.read(session.beginRequest(null), changed))
            assertNull(session.read(session.beginRequest("relax"), changed))
        }
    }

    @Test
    fun lateFeedAndPaginationResultsCannotOverwriteANewSelection() {
        val session = HomeFeedSession<String>()
        val oldAll = session.beginRequest(null)
        session.read(oldAll, context)
        session.store(oldAll, HomeFeedSession.Feed(listOf("all"), "page-2"))
        val relax = session.beginRequest("relax")
        session.store(relax, HomeFeedSession.Feed(listOf("relax"), null))
        assertFalse(session.store(oldAll, HomeFeedSession.Feed(listOf("late-all"), null)))
        assertFalse(session.updatePagination(oldAll, HomeFeedSession.Feed(listOf("wrong-page"), null)))
        val newAll = session.beginRequest(null)
        assertFalse(session.isCurrent(oldAll))
        assertTrue(session.isCurrent(newAll))
        assertEquals(listOf("all"), session.read(newAll, context)?.items)
        assertNull(session.read(oldAll, context.copy(cookie = "old-account")))
        assertEquals(listOf("relax"), session.read(session.beginRequest("relax"), context)?.items)
    }

    @Test
    fun successfulEmptyCategoryIsCached() {
        val session = HomeFeedSession<String>()
        val empty = HomeFeedSession.Feed<String>(emptyList(), null)
        val request = session.beginRequest("sleep")
        session.read(request, context)
        session.store(request, empty)
        session.beginRequest(null)
        assertEquals(empty, session.read(session.beginRequest("sleep"), context))
    }
}
