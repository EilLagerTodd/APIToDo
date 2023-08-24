package com.ail.todo.utils

// 문자열이 제이슨 형태인지
fun String?.isJsonObject():Boolean {
    if(this?.startsWith("{") == true && this.endsWith("}")){
        return true
    } else {
        return false
    }
//    return this?.startsWith("{") == true && this.endsWith("}")
}
//fun String?.isJsonObject():Boolean = this?.startsWith("{") == true && this.endsWith("}")

// 문자열이 제이슨 배열인지
fun String?.isJsonArray() : Boolean {
    if(this?.startsWith("[") == true && this.endsWith("]")){
        return true
    } else {
        return false
    }
}