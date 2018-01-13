package com.pyoinsoo.kotlin.okhttp3.common

import android.support.v4.util.Pair
import com.pyoinsoo.kotlin.okhttp3.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

/*
 * Created by pyoinsoo on 2017-01-11.
 */
class WeatherUtil {
    /*
     * SK Weather 서버에서 넘어온
     * JSON으로 넘어온 현재 하늘 상태(sky code)
     * 현재 하늘의 상태를 다음의 값으로 넘겨준다
     * Sky State에 따라 적절한 이미지로 배경화면과
     * 날씨 아이콘을 선정하여 화면에 표시한다
     */
    private enum class SkyAState {
        SKY_A00, SKY_A01, SKY_A02, SKY_A03, SKY_A04, SKY_A05,
        SKY_A06, SKY_A07, SKY_A08, SKY_A09, SKY_A10, SKY_A11,
        SKY_A12, SKY_A13, SKY_A14
    }
    companion object {
        /*
         * 날씨 상태에 따라 적절한 배경화면과 날씨아이콘을
         * 넘겨주기 위해 EnumMap를 설정한다
         */
        //배경화면용 Enum 값 설정
        private var skyABGEnumMap = EnumMap<SkyAState, Int>(SkyAState::class.java)

        //날씨아이콘용 Enum 값 설정
        private var skyAIconEnumMap = EnumMap<SkyAState, Int>(SkyAState::class.java)

        init {
            /*
             * 현재 하늘의 상태에 따라 Back Ground Image를 설정한다
             */
            with(skyABGEnumMap){
                put(SkyAState.SKY_A00, R.drawable.bg_01)
                put(SkyAState.SKY_A01, R.drawable.bg_01)
                put(SkyAState.SKY_A02, R.drawable.bg_04)
                put(SkyAState.SKY_A03, R.drawable.bg_04)
                put(SkyAState.SKY_A04, R.drawable.bg_06)
                put(SkyAState.SKY_A05, R.drawable.bg_05)
                put(SkyAState.SKY_A06, R.drawable.bg_06)
                put(SkyAState.SKY_A07, R.drawable.bg_04)
                put(SkyAState.SKY_A08, R.drawable.bg_06)
                put(SkyAState.SKY_A09, R.drawable.bg_05)
                put(SkyAState.SKY_A10, R.drawable.bg_06)
                put(SkyAState.SKY_A11, R.drawable.bg_04)
                put(SkyAState.SKY_A12, R.drawable.bg_06)
                put(SkyAState.SKY_A13, R.drawable.bg_05)
                put(SkyAState.SKY_A14, R.drawable.bg_06)
            }
            /*
             * 현재 sky code에 따라 날씨아이콘을 설정한다
             */
            with(skyAIconEnumMap){
                put(SkyAState.SKY_A00, R.drawable.ic_01)
                put(SkyAState.SKY_A01, R.drawable.ic_01)
                put(SkyAState.SKY_A02, R.drawable.ic_02)
                put(SkyAState.SKY_A03, R.drawable.ic_03)
                put(SkyAState.SKY_A07, R.drawable.ic_03)
                put(SkyAState.SKY_A04, R.drawable.ic_04)
                put(SkyAState.SKY_A08, R.drawable.ic_04)
                put(SkyAState.SKY_A05, R.drawable.ic_05)
                put(SkyAState.SKY_A12, R.drawable.ic_05)
                put(SkyAState.SKY_A06, R.drawable.ic_08)
                put(SkyAState.SKY_A10, R.drawable.ic_08)
                put(SkyAState.SKY_A14, R.drawable.ic_08)
                put(SkyAState.SKY_A09, R.drawable.ic_06)
                put(SkyAState.SKY_A11, R.drawable.ic_09)
                put(SkyAState.SKY_A13, R.drawable.ic_07)
            }
        }
        /*
         * 현재 sky code에 따라 Back Ground Image와 날씨 icon
         * 을 Pair담아 리턴
         */
        fun currentABGIconCondition(code: String): Pair<Int, Int> {
            val enumCode = SkyAState.valueOf(code)
            val bgResValue = skyABGEnumMap[enumCode]
            val iconResValue = skyAIconEnumMap[enumCode]
            return Pair.create(bgResValue, iconResValue)
        }

        //현재 날짜
        private val  yyyyMMddFormet = SimpleDateFormat(MyApplication.myApplication.getString(R.string.yyyyMMmmFormat))
        //오전/오후를 나타냄
        private val amPmFormat = SimpleDateFormat("a ")
        //현재 시간/분
        private val minuteSecondFormat = SimpleDateFormat("h : mm")

        private val currentTimeMillis = System.currentTimeMillis()

        /*
         * 현재 날짜,시간과 요일을 알아낸다
         */
        fun currentDate() = yyyyMMddFormet.format(Date(currentTimeMillis))
        fun amPm() = amPmFormat.format(Date(currentTimeMillis))
        fun minuteSecond() = minuteSecondFormat.format(Date(currentTimeMillis))

        /*
         * 자외선 지수 0~20: 매우좋음, 21~60: 좋음, 61~120: 약간나쁨
         * 121~200: 나쁨, 201~300: 매우나쁨
         * 오후 6시 이후 자외선 값은 0 이다
         */
        fun getUvrayMessage(jsonValue:String = "0"): Pair<Int,String> {

            //원의  값을 설정하기 위함(원의 값은 0~100)
            var changedValue:Int

            //원안에 표시할 메세지를 설정한다
            var stateMessage:String

            /*
             * 문자열 소숫점으로 넘어온 값을 반올림하고 Int로 변환
             */
            when(jsonValue.toDouble().roundToInt()){
                in 0..20 ->{
                    stateMessage ="매우\n좋음"
                    changedValue = 18
                }
                in 21..60 ->{
                    stateMessage = "좋음"
                    changedValue = 38
                }
                in 61..120 ->{
                    stateMessage = "약간\n나쁨"
                    changedValue = 65
                }
                in 121..200 ->{
                    stateMessage = "나쁨"
                    changedValue = 80
                }
                else  ->{
                    stateMessage = "매우\n나쁨"
                    changedValue = 90
                }
            }
            return Pair(changedValue,stateMessage)
        }
        /*
          농도(㎍/㎥)
          - 0~30: 좋음, 31~80: 보통, 81~120: 약간나쁨
           121~200: 나쁨, 201~300: 매우나쁨
         */
        fun getDustMessage(jsonValue:String = "0") : Pair<Int,String>{

            //원을 그리기 위한 값(원의 값은 0~100)
            var changedValue:Int

            //원안에 표시할 문자열
            var stateMessage:String

            /*
             * 문자열 소숫점으로 넘어온 값을 반올림하고 Int로 변환
             */
            when(jsonValue.toDouble().roundToInt()){
                in 0..30 ->{
                    stateMessage = "아주\n좋음"
                    changedValue = 20
                }
                in 31..40 ->{
                    stateMessage = "좋음"
                    changedValue = 30
                }
                in 41..80 ->{
                    stateMessage = "보통"
                    changedValue = 50
                }
                in 81..120 ->{
                    stateMessage = "약간\n나쁨"
                    changedValue = 60
                }
                in 121..200 ->{
                    stateMessage = "나쁨"
                    changedValue = 75
                }
                in 201..300 ->{
                    stateMessage = "매우\n나쁨"
                    changedValue = 90
                }
                else ->{
                    stateMessage = "외출\n금지"
                    changedValue = 100
                }
            }
            return Pair(changedValue,stateMessage)
        }
    }
}