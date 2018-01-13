package com.pyoinsoo.kotlin.okhttp3.common

import android.app.Application

/*
 * Created by pyoinsoo on 2018-01-11.
 * insoo.pyo@gmail.com
 */
class MyApplication : Application() {
    /*
     * 싱글턴 사용을 위해 동반객체로 Application객체를 선언
     */
    companion object {
        lateinit var myApplication : MyApplication
            private set
    }
    override fun onCreate() {
        super.onCreate()
        myApplication = this
    }
}