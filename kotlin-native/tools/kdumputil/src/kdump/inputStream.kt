/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kdump

import base.Endianness
import io.readByteInt
import io.readCString
import java.io.IOException
import java.io.InputStream

fun InputStream.readEndianness(): Endianness {
    val byte = this.readByteInt()
    return if (byte.and(1) != 0) Endianness.LITTLE else Endianness.BIG
}

fun InputStream.readIdSize(): IdSize {
    return when (val size = this.readByteInt()) {
        1 -> IdSize.BITS_8
        2 -> IdSize.BITS_16
        4 -> IdSize.BITS_32
        8 -> IdSize.BITS_64
        else -> throw IOException("Invalid id size: $size.")
    }
}

private const val HEADER = "Kotlin/Native dump 1.0.8"
private const val COMPRESSED_REFS_HEADER = "Kotlin/Native dump 1.0.9"

fun InputStream.readDump(): MemoryDump {
    val headerString = readCString().also {
        if (it != HEADER && it != COMPRESSED_REFS_HEADER) {
            throw IOException("invalid header \"$it\", expected \"$HEADER\" or \"$COMPRESSED_REFS_HEADER\"")
        }
    }
    val endianness = readEndianness()
    val idSize = readIdSize()
    val reader = Reader(this, endianness, idSize)
    val refEncoding = if (headerString == COMPRESSED_REFS_HEADER) reader.readRefEncoding() else null
    val items = reader.readList { readItem() }
    return MemoryDump(
            headerString = headerString,
            endianness = endianness,
            idSize = idSize,
            refEncoding = refEncoding,
            items = items,
    )
}
