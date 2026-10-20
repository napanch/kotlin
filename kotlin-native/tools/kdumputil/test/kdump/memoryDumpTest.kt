/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kdump

import kdump.hprof.newConverter
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class MemoryDumpTest {
    @Test
    fun compressedReferences() {
        val bytes = ByteArrayOutputStream().apply {
            fun u8(value: Int) = write(value)
            fun u32(value: Int) = repeat(4) { write(value ushr (it * 8)) }
            fun id(value: Long) = repeat(8) { write((value ushr (it * 8)).toInt()) }
            fun str(value: String) = write(value.toByteArray() + 0)

            str("Kotlin/Native dump 1.0.9")
            u8(1) // little endian
            u8(8) // id size
            id(0x100000000) // ref base
            u8(3) // ref shift

            u8(1)
            id(0x10)
            u8(0)
            id(0)
            str("")
            str("Node")
            u32(8)
            u32(1)
            u32(0)

            u8(2)
            id(0x100000040)
            id(0x10)
            u32(8)
            u32(0x10)
            u32(0)
        }.toByteArray()

        val dump = bytes.inputStream().readDump()
        assertEquals(RefEncoding(Id(0x100000000), 3), dump.refEncoding)

        val converter = dump.newConverter()
        val type = dump.items[0] as Type
        val obj = dump.items[1] as ObjectItem
        assertEquals(Id(0x100000080), converter.getId(obj.byteArray, 0))
        assertEquals(
                listOf(Field(0, RuntimeType.OBJECT, "object_0"), Field(4, RuntimeType.INT_32, "data_0")),
                converter.syntheticFields(type.id, type.body as Type.Body.Object),
        )
    }
}
