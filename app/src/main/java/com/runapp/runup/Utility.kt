package com.runapp.runup

import android.graphics.Color
import java.util.regex.Pattern

fun calcColorMap(speed: Float): Int {
    val gain = Constant.MAX_COLORMAP_SPEED - Constant.MIN_COLORMAP_SPEED
    val green = (255 * (Constant.MAX_COLORMAP_SPEED - speed) / gain).toInt()
    val red = (255 * (speed - Constant.MIN_COLORMAP_SPEED) / gain).toInt()

    return Color.rgb(red, green, 0)
}


fun getFuncname(): String {
    val trace = Thread.currentThread().stackTrace[4]
    val pattern = Pattern.compile("[\\.]+")
    val splitedStr = pattern.split(trace.className)
    val simpleClass = splitedStr[splitedStr.size - 1]

    return simpleClass + "#" + trace.methodName + ":" + trace.lineNumber.toString()
}
