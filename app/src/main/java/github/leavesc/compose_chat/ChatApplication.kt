package github.leavesc.compose_chat

import android.app.Application
import github.leavesc.compose_chat.cache.AppThemeCache
import github.leavesc.compose_chat.logic.ComposeChat
import github.leavesc.compose_chat.ui.weigets.CoilImageLoader
import github.leavesc.compose_chat.utils.ContextHolder

/**
 * @Author: leavesC
 * @Date: 2021/6/18 23:52
 * @Desc:
 * @Github：https://github.com/leavesC
 */
class ChatApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ContextHolder.init(context = this)
        CoilImageLoader.init(context = this)
        ComposeChat.accountProvider.init(context = this)
        AppThemeCache.init()
    }

}