package com.pyoinsoo.kotlin.okhttp3.common

import com.pyoinsoo.kotlin.okhttp3.R
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit

/*
 * Created by pyoinsoo on 2018-01-11.
 * insoo.pyo@gmail.com
 * OkhttpClient를 싱글톤으로 만들고
 * Intercepter를 세팅한다
 */
object OkHttp3Manager {

    private val ALL_TIMEOUT = 10L

    /*
     * SK 개발자 Key를 등록한다
     */
    private val SK_API_KEY =
            MyApplication.myApplication.resources.getString(R.string.sk_weather_api_key)

    /*
     * Native HTTP통신을 위해 OkHttp3를 선언
     */
    private var okHttpClient: OkHttpClient

    /*
     * Request Header을 세팅하는 Interceptor
     * 요청을 보낼때 마다 다음을 기본적으로 세팅한다
     */
    private  class HeaderSettingInterceptor : Interceptor {

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {

            val chainRequest = chain.request()
            /*
             * Response Type과 SK 개발자Key을 등록한다
             */
            val request = chainRequest.newBuilder().apply{
                addHeader("Accept", "application/json")
                addHeader("appKey", SK_API_KEY)
            }.build()

            return chain.proceed(request)
        }
    }
    init{
        /*
         * HTTP 상태를 알아보기 위해 Http Login Interceptor를 설정한다
         */
        val httpLogging = HttpLoggingInterceptor()
        httpLogging.level = HttpLoggingInterceptor.Level.BASIC

        /*
         * OkHttp3 통신에 Interceptor와 Socket(I/O) 환경을 설정
         */
        okHttpClient = OkHttpClient().newBuilder().apply {

            addInterceptor(httpLogging)
            addInterceptor(HeaderSettingInterceptor())
            connectTimeout(ALL_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(ALL_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(ALL_TIMEOUT, TimeUnit.SECONDS)

        }.build()
    }
    fun getOkHttpClient() = okHttpClient

    /*
     * 날씨 정보를 요청하는 SK HOST
     */
    const val SK_SCHEME = "http"
    const val SK_HOST = "apis.skplanetx.com"

    /*
     * SK Weather Center로 가는  URL과 Query String
     * 을 만들어 낸다
     * 좌표는 서울의 중심좌표이다
     */
    fun makeHttpURL(targetURL:String) : HttpUrl {
        return HttpUrl.Builder().apply{
                   scheme(SK_SCHEME)
                   host(SK_HOST)
                   addPathSegments(targetURL)
                   addQueryParameter("version","1")
                   addQueryParameter("lat","37.572978")
                   addQueryParameter("lon","126.989061")
               }.build()
    }
}