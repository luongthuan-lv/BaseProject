package com.bigsoft.clap.myphone.base.ext

import java.io.File

fun File.checkExits() {
    if (!exists()) {
        mkdirs()
    }
}