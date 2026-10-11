package com.maxrave.simpmusic.expect.ui

import android.view.ViewGroup
import android.view.View
import android.webkit.CookieManager
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

actual fun createWebViewCookieManager(): WebViewCookieManager =
    object : WebViewCookieManager {
        override fun getCookie(url: String): String {
            // hasCookies() only reports whether the WebView has any cookies at all; it does not
            // guarantee that this URL has one. Android may return null during the Google → YouTube
            // redirect, so always normalize the URL-specific result before handing it to Kotlin.
            return CookieManager.getInstance().getCookie(url).orEmpty()
        }

        override fun removeAllCookies() {
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
        }
    }

@Composable
actual fun PlatformWebView(
    state: MutableState<WebViewState>,
    initUrl: String,
    aboveContent: @Composable (BoxScope.() -> Unit),
    onPageFinished: (String) -> Unit,
) {
    Box {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    // Avoid the WebView Vulkan functor path. It crashes in drawVk on some
                    // devices (including the supplied OnePlus log) during Google sign-in.
                    setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                    layoutParams =
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                    webViewClient =
                        object : WebViewClient() {
                            override fun onPageFinished(
                                view: WebView?,
                                url: String?,
                            ) {
                                url?.let {
                                    onPageFinished(it)
                                }
                            }
                        }
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    CookieManager.getInstance().setAcceptCookie(true)
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                    loadUrl(initUrl)
                }
            },
            modifier = Modifier.fillMaxSize(),
            onRelease = { webView ->
                webView.stopLoading()
                webView.loadUrl("about:blank")
                webView.removeAllViews()
                webView.destroy()
            },
        )
        aboveContent()
    }
}

@Composable
actual fun DiscordWebView(
    state: MutableState<WebViewState>,
    aboveContent: @Composable (BoxScope.() -> Unit),
    onLoginDone: (String) -> Unit
) {
    val url = "https://discord.com/login"
    Box {
        AndroidView(factory = {
            WebView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = object : WebViewClient() {

                    @Deprecated("Deprecated in Java")
                    override fun shouldOverrideUrlLoading(
                        webView: WebView,
                        url: String,
                    ): Boolean {
                        stopLoading()
                        if (url.endsWith("/app")) {
                            loadUrl(JS_SNIPPET)
                        }
                        return false
                    }
                }
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true

                if (android.os.Build.MANUFACTURER.equals(MOTOROLA, ignoreCase = true)) {
                    settings.userAgentString = SAMSUNG_USER_AGENT
                }
                webChromeClient = object : WebChromeClient() {
                    override fun onJsAlert(
                        view: WebView,
                        url: String,
                        message: String,
                        result: JsResult,
                    ): Boolean {
                        onLoginDone(message)
                        return true
                    }
                }
                loadUrl(url)
            }
        })
        aboveContent()
    }
}

const val JS_SNIPPET =
    "javascript:(function()%7Bvar%20i%3Ddocument.createElement('iframe')%3Bdocument.body.appendChild(i)%3Balert(i.contentWindow.localStorage.token.slice(1,-1))%7D)()"
private const val MOTOROLA = "motorola"
private const val SAMSUNG_USER_AGENT =
    "Mozilla/5.0 (Linux; Android 14; SM-S921U; Build/UP1A.231005.007) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Mobile Safari/537.363"
