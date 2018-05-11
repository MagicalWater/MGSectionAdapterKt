package org.magicalwater.mgkotlin.mgsectionadapterkt.utils

import android.util.Log
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlin.reflect.KClass

/**
 * Created by magicalwater on 2018/1/13.
 * 解析所有有關json的東西, 無論是序列化或者反序列化
 */
class MGJsonDataParseUtils {

    companion object {

        //反序列化, 將json變成物件
        fun deserialize(json: String, deserialize: KClass<out Any>): Any? {

            val objectMapper = jacksonObjectMapper()
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false)

            //這邊使用 try catch 以免發生例外
            var ins: Any? = null
            try {
                ins = objectMapper.readValue(json, deserialize.java)
            } catch (e: Exception) {
                Log.w("MGJsonDataParseUtils", "解析時發生錯誤, 原因: ${e.message}")
                e.printStackTrace()
            }

            Log.w("MGJsonDataParseUtils", "解析成功")
            return ins
        }

        //序列化, 將物件變成json字串
        fun serialize(data: Any): String {
            val objectMapper = jacksonObjectMapper()
            return objectMapper.writeValueAsString(data) ?: ""
        }
    }
}