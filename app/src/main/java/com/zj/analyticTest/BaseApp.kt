package com.zj.analyticTest

import android.app.Application
import com.zj.analyticSdk.CCAnalytic

class BaseApp : Application() {


    override fun onCreate() {
        super.onCreate()
        /**初始化 CCAnalytic 埋点模块，配置参见 [CAConfig] */
        CCAnalytic.init(this, CAConfig)
    }
}