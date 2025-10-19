package com.seafile.seadroid2.view.webview

import android.app.Activity
import android.content.Context
import android.content.MutableContextWrapper
import android.os.Looper
import android.os.MessageQueue.IdleHandler
import com.seafile.seadroid2.R
import com.seafile.seadroid2.SeadroidApplication
import java.util.Stack

class PreloadWebView private constructor() {
    private object Holder {
        val INSTANCE = PreloadWebView()
    }

    fun preload() {
        Looper.myQueue().addIdleHandler(IdleHandler {
            if (mCachedWebViewStack.size < CACHED_WEB_VIEW_MAX_NUM) {
                mCachedWebViewStack.push(buildWebView())
            }
            false
        })
    }

    private fun buildWebView(): SeaWebView {
        val webView = SeaWebView(MutableContextWrapper(SeadroidApplication.getAppContext()))
        webView.setId(R.id.webview)
        return webView
    }

    /**
     * context must be a Class extends Activity
     */
    fun getWebView(context: Context?): SeaWebView {
        val isActivity = context is Activity
        require(isActivity) { "context must be a Class extends Activity" }

        if (mCachedWebViewStack.isEmpty()) {
            val webView = buildWebView()
            val contextWrapper = webView.getContext() as MutableContextWrapper
            contextWrapper.setBaseContext(context)
            return webView
        }

        val webView: SeaWebView = mCachedWebViewStack.pop()
        val contextWrapper = webView.getContext() as MutableContextWrapper
        contextWrapper.setBaseContext(context)
        return webView
    }

    companion object {
        private const val CACHED_WEB_VIEW_MAX_NUM = 2
        private val mCachedWebViewStack = Stack<SeaWebView>()

        fun getInstance(): PreloadWebView {
            return Holder.INSTANCE
        }
    }
}
