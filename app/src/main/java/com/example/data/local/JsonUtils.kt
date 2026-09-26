package com.example.data.local

import com.example.data.local.entity.CustomFieldConfig
import org.json.JSONArray
import org.json.JSONObject

object JsonUtils {

    fun parseFieldsConfig(jsonStr: String): List<CustomFieldConfig> {
        val list = mutableListOf<CustomFieldConfig>()
        if (jsonStr.isBlank()) return list
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CustomFieldConfig(
                        fieldId = obj.optString("fieldId", "field_$i"),
                        label = obj.optString("label", ""),
                        instruction = obj.optString("instruction", ""),
                        placeholder = obj.optString("placeholder", ""),
                        isRequired = obj.optBoolean("isRequired", true),
                        order = obj.optInt("order", i)
                    )
                )
            }
        } catch (_: Exception) {
        }
        return list.sortedBy { it.order }
    }

    fun serializeFieldsConfig(fields: List<CustomFieldConfig>): String {
        val array = JSONArray()
        fields.forEach { f ->
            val obj = JSONObject()
            obj.put("fieldId", f.fieldId)
            obj.put("label", f.label)
            obj.put("instruction", f.instruction)
            obj.put("placeholder", f.placeholder)
            obj.put("isRequired", f.isRequired)
            obj.put("order", f.order)
            array.put(obj)
        }
        return array.toString()
    }

    fun parseSubmittedValues(jsonStr: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        if (jsonStr.isBlank()) return map
        try {
            val obj = JSONObject(jsonStr)
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = obj.optString(key, "")
            }
        } catch (_: Exception) {
        }
        return map
    }

    fun serializeSubmittedValues(values: Map<String, String>): String {
        val obj = JSONObject()
        values.forEach { (k, v) ->
            obj.put(k, v)
        }
        return obj.toString()
    }
}
