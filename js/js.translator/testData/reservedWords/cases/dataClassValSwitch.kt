package foo

// NOTE THIS FILE IS AUTO-GENERATED by the generateTestDataForReservedWords.kt. DO NOT EDIT!

data class DataClass(val switch: String) {
    {
        testNotRenamed("switch", { switch })
    }
}

fun box(): String {
    DataClass("123")

    return "OK"
}