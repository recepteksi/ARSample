package com.trendhive.arsample.domain.model

import platform.CoreFoundation.CFAbsoluteTimeGetCurrent

actual fun currentTimeMillis(): Long {
    return (CFAbsoluteTimeGetCurrent() * 1000).toLong()
}
