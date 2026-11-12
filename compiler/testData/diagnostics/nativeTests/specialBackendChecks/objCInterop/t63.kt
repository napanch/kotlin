// RUN_PIPELINE_TILL: CODEGEN
// WITH_PLATFORM_LIBS
// KT-86474
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

import kotlinx.cinterop.*
import platform.Foundation.NSLog
import platform.Foundation.NSString
import platform.posix.printf

fun foo(detail: String, nullableDetail: String?) {
    NSLog("%@", <!STRING_AS_VARIADIC_C_ARGUMENT!>"literal"<!>)
    NSLog("%@", <!STRING_AS_VARIADIC_C_ARGUMENT!>detail<!>)
    NSLog("%@", <!STRING_AS_VARIADIC_C_ARGUMENT!>nullableDetail<!>)
    NSLog("%@", *arrayOf(<!STRING_AS_VARIADIC_C_ARGUMENT!>detail<!>))
    NSLog("%@", *arrayOf(*arrayOf(<!STRING_AS_VARIADIC_C_ARGUMENT!>detail<!>)))
    printf("%s", <!STRING_AS_VARIADIC_C_ARGUMENT!>detail<!>)
    printf("%s", *arrayOf(<!STRING_AS_VARIADIC_C_ARGUMENT!>detail<!>))
}

fun bar(detail: String, nsString: NSString) {
    NSLog("%@", nsString)
    NSLog("%@", detail <!CAST_NEVER_SUCCEEDS!>as<!> NSString)
    NSLog("%s", detail.cstr)
    printf("%s", detail.cstr)
}

fun kotlinVararg(format: String, vararg args: Any?) {}

fun kotlinCall(detail: String) {
    kotlinVararg("%@", detail)
}
