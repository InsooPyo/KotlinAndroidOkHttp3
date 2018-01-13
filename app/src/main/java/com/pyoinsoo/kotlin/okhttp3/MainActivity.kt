package com.pyoinsoo.kotlin.okhttp3

import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.util.Pair
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.gson.Gson
import com.pyoinsoo.kotlin.okhttp3.common.MyApplication
import com.pyoinsoo.kotlin.okhttp3.common.OkHttp3Manager
import com.pyoinsoo.kotlin.okhttp3.common.WeatherUtil
import com.pyoinsoo.kotlin.okhttp3.jsondata.CurrentWeather
import com.pyoinsoo.kotlin.okhttp3.jsondata.Minutely
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.HttpUrl
import okhttp3.Request
import okhttp3.Response
import kotlin.math.roundToInt

/*
 * Created by pyoinsoo on 2017-01-11.
 * insoo.pyo@gmail.com
 *
 * 현재 날씨 및 미세먼지/자외선 지수를 보여주는 화면
 */

//현재 실시간 날씨 URL Path
const val WEATHER_CURRENT_MINUTELY = "weather/current/minutely"

//현재 미세먼지 URL Path
const val WEATHER_DUST = "weather/dust"

//현재 자외선 지수 URL Path
const val WEATHER_WINDEX_UVINDEX = "weather/windex/uvindex"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //현재 날짜와 시간을 가져와 위젯에 뿌려준다
        tvTodayDate.text = WeatherUtil.currentDate()
        tvTodayAmPm.text = WeatherUtil.amPm()
        tvTodayHhMm.text = WeatherUtil.minuteSecond()
    }

    override fun onResume() {
        super.onResume()

        /*
         * 병렬로 네트웍을 연결한다
         */
        //현재 날씨(최저/최고포함)상태를 알아온다
        WeatherRESTRequest().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                WEATHER_CURRENT_MINUTELY)

        //현재 미세먼지 정보를 가져온다
        WeatherRESTRequest().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                WEATHER_DUST)

        //현재 자외선 정보를 가져온다
        WeatherRESTRequest().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                WEATHER_WINDEX_UVINDEX)

    }
    /*
     * 각 각의 요청정보를 실행하는 AsyncTask 클래스
     */
    inner class WeatherRESTRequest : AsyncTask<String, Unit, CurrentWeather>() {

        var restTargetURL: String? = null

        override fun doInBackground(vararg params: String): CurrentWeather? {

            //요청(URL)의 종류(현재날씨,미세먼지,자외선)
            restTargetURL = params[0]


            val toRESTServer = OkHttp3Manager.getOkHttpClient()

            //요청URL정보(질의문자열 포함)를 가져온다
            val httpURL: HttpUrl = OkHttp3Manager.makeHttpURL(params[0].trim())

            val request = Request.Builder().apply {
                                               url(httpURL)
                                            }.build()
            //요청보내고 결과를 가져온다
            val response:Response = toRESTServer.newCall(request).execute()

            //JSON을 Data 객체로 변환(JSON 직렬화)
            var currentWeather:CurrentWeather? = null

            /*
             * OkHttp3의 Response는 닫아준다
             * use() 함수는 Closeable를 구현한 객체를 사용 후
             * 닫아주는 역할을 한다(try..catch..finally를 대신)
             */
            response.use {
                if (response.isSuccessful) {
                    val gson = Gson()
                    /*
                     * 넘어온 json을 gson을 이용해
                     * 객체화 한다
                     */
                    currentWeather = gson.fromJson(response.body()?.string(),
                                                   CurrentWeather::class.java)
                }
            }
            /*
             * GSON을 통해 변환된 객체를 리턴한다
             */
            return currentWeather
        }

        override fun onPostExecute(result: CurrentWeather?) {
            super.onPostExecute(result)

            result?.let {
                //요청 URL정보에 따라 화면을 갱신하는 함수를 호출한다
                when (restTargetURL) {

                    WEATHER_CURRENT_MINUTELY -> setUICurrentWeather(result)

                    WEATHER_DUST -> setUIDust(result)

                    WEATHER_WINDEX_UVINDEX -> setUIUvindex(result)

                    else -> Unit
                }
            } ?: Toast.makeText(
                    MyApplication.myApplication,
                    "데이터를 받지 못했네요",
                    Toast.LENGTH_SHORT
            ).show()
        }
    }

    /*
     * JSON을 파싱한 데이터를 넘겨 받는다
     * 현재 날씨(온도)를 화면에 갱신
     */
    private fun setUICurrentWeather(data: CurrentWeather) {
        val minutely: Minutely? = data.weather.minutely?.get(0)

        /*
         * 현재날씨및 오늘의 최저/최고 온도를 가져온다
         * minutely 인스턴스가 null이 아니라면 현재기온과 오늘 최저/최저
         * 기온을 알아와 UI를 업데이트 한다
         */
        minutely?.let {

            tvTodayCurrentTemperature.text = it.temperature.tc.toDouble().roundToInt().toString()
            maxTv.text = it.temperature.tmax.toDouble().roundToInt().toString()
            minTv.text = it.temperature.tmin.toDouble().roundToInt().toString()

            /*
             * 현재 하늘상태(sky code)맞는 Icon으로
             * background image및 Weather Icon 을 세팅한다
             */
            val pair: Pair<Int, Int> = WeatherUtil.currentABGIconCondition(it.sky.code)

            mainRootview.setBackgroundResource(pair.first!!)
            ivCurrentWeatherIcon.setImageResource(pair.second!!)
        }
    }
    /*
     * 미세먼지 정보 화면 갱신
     */
    private fun setUIDust(data: CurrentWeather) {

        val dustValue = data.weather.dust?.get(0)!!.pm10.value
        val pair = WeatherUtil.getDustMessage(dustValue.trim())

        dustCircular.progressValue = pair.first!!.toFloat()
        tvDustGrade.text = pair.second
    }
    /*
     * 자외선 화면을 갱신
     * 18시 이후엔 자외선 정보가 기본값인 0으로 넘어온다
     */
    private fun setUIUvindex(data: CurrentWeather) {
        var uvValue = data.weather.wIndex?.uvindex!!.get(0).day00.index
        /*
         * 18시 이후엔 json값이 empty가 된다
         */
        if(uvValue.isNullOrEmpty()){
            uvValue = "0"
        }
        val pair = WeatherUtil.getUvrayMessage(uvValue)
        uvCircular.progressValue = pair.first!!.toFloat()
        tvUvStateMessage.text = pair.second
    }
}
