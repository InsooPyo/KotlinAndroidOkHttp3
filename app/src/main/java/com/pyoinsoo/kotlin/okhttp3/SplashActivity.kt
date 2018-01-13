package com.pyoinsoo.kotlin.okhttp3

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import com.pyoinsoo.kotlin.okhttp3.common.MyApplication

/*
 * Created by pyoinsoo on 2017-01-11.
 * insoo.pyo@gmail.com
 *
 * 스플래쉬 화면
 * 현재 단말기가 네트웍에 연결되지 있지 않으면
 * 메세지 출력후 종료 한다
 */
class SplashActivity : AppCompatActivity() {
    private val handler = Handler()
    private var networkInfo: NetworkInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*
         * 현재 단말기의 네트웍 정보가 담겨있는 NetworkInfo객체를 얻어온다
         */
        val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        networkInfo = manager?.activeNetworkInfo
    }

    override fun onStart() {
        super.onStart()

        handler.postDelayed({

            val networkStatus: Boolean = networkInfo?.isConnectedOrConnecting ?: false
            if (networkStatus) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
              Toast.makeText(MyApplication.myApplication,
                             "네트웍이 연결되지않아 종료합니다",Toast.LENGTH_SHORT).show()
              finish()
            }
        }, 1000)
    }
}
