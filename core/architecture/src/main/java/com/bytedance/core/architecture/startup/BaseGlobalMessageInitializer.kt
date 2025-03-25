package com.bytedance.core.architecture.startup

import android.app.Application
import android.content.Context
import androidx.startup.Initializer
import com.bytedance.core.architecture.message.MessageManager
import com.bytedance.core.architecture.message.collector.ToastMessageCollector
import com.bytedance.core.architecture.message.controller.DefaultMessageController

/**
 * 描述：
 *
 * @author zhangrq
 * createTime 2024/8/12 下午3:14
 */
class BaseGlobalMessageInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        // 初始化全局消息
        MessageManager.initGlobalMessage(
            context as Application,
            DefaultMessageController(),
            ToastMessageCollector(context)
        )
//        MessageManager.initGlobalMessage(
//            context as Application,
//            DefaultMessageController(),
//            SnakbarViewMessageCollector(null)
//        )
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf()
    }
}