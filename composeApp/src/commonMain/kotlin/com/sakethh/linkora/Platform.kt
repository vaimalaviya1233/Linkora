package com.sakethh.linkora

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform