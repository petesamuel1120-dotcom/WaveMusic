package com.petedev.wavemusic.ui.component

import androidx.lifecycle.ViewModelStore
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestinationBuilder
import androidx.navigation.Navigator
import androidx.navigation.navigation
import javax.swing.SwingUtilities
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TabNavigationTest {
    private val tabs = listOf(BottomNavScreen.Home, BottomNavScreen.Search, BottomNavScreen.Library, BottomNavScreen.Analytics, BottomNavScreen.MixForYou)

    @Navigator.Name("test")
    private class TestNavigator : Navigator<NavDestination>() {
        override fun createDestination(): NavDestination = NavDestination(this)
    }

    private fun controller(): NavController = NavController().apply {
        setViewModelStore(ViewModelStore())
        val navigator = TestNavigator()
        navigatorProvider.addNavigator(navigator)
        graph = navigatorProvider.navigation(startDestination = BottomNavScreen.Home.destination::class) {
            tabs.forEach { destination(NavDestinationBuilder(navigator, it.destination::class, emptyMap())) }
        }
    }

    private fun NavController.select(tab: BottomNavScreen) {
        navigateToTabRoot(tab, activeTabRoot(tabs))
        assertTrue(currentDestination?.hasRoute(tab.destination::class) == true, "${tab.destination} tap opened ${currentDestination?.route}")
    }

    @Test
    fun homeTapDoesNotRestoreSearchAfterReturningFromSearch() = SwingUtilities.invokeAndWait {
        val nav = controller()
        listOf(BottomNavScreen.Search, BottomNavScreen.Home, BottomNavScreen.Library, BottomNavScreen.Home).forEach { nav.select(it) }
    }

    @Test
    fun libraryTapDoesNotRestoreSearchAfterReturningFromSearch() = SwingUtilities.invokeAndWait {
        val nav = controller()
        listOf(BottomNavScreen.Library, BottomNavScreen.Search, BottomNavScreen.Library, BottomNavScreen.Home, BottomNavScreen.Library).forEach { nav.select(it) }
    }

    @Test
    fun restoresRootStateAcrossRepeatedTabChanges() = SwingUtilities.invokeAndWait {
        val nav = controller()
        nav.currentBackStackEntry!!.savedStateHandle["scroll"] = 42
        nav.select(BottomNavScreen.Library)
        nav.currentBackStackEntry!!.savedStateHandle["scroll"] = 81
        repeat(20) {
            nav.select(BottomNavScreen.Search)
            nav.select(BottomNavScreen.Library)
            assertEquals(81, nav.currentBackStackEntry!!.savedStateHandle.get<Int>("scroll"))
            nav.select(BottomNavScreen.Home)
            assertEquals(42, nav.currentBackStackEntry!!.savedStateHandle.get<Int>("scroll"))
            nav.select(BottomNavScreen.Library)
        }
    }

    @Test
    fun repairsSearchStackPreviouslySavedUnderHome() = SwingUtilities.invokeAndWait {
        val nav = controller()
        nav.select(BottomNavScreen.Search)
        // Reproduce saved-state aliases left by older navigation code.
        nav.popBackStack(BottomNavScreen.Home.destination, inclusive = false, saveState = true)
        nav.navigate(BottomNavScreen.Library.destination) {
            popUpTo(BottomNavScreen.Home.destination) { inclusive = true; saveState = true }
        }
        nav.select(BottomNavScreen.Home)
        nav.select(BottomNavScreen.Library)
        nav.select(BottomNavScreen.Home)
    }

    @Test
    fun searchBackReturnsToThePageThatOpenedIt() = SwingUtilities.invokeAndWait {
        val nav = controller()
        nav.select(BottomNavScreen.Library)
        nav.select(BottomNavScreen.Search)
        assertTrue(nav.popBackStack())
        assertTrue(nav.currentDestination?.hasRoute(BottomNavScreen.Library.destination::class) == true)
    }
}
