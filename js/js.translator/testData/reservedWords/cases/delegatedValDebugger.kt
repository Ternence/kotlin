package foo

// NOTE THIS FILE IS AUTO-GENERATED by the generateTestDataForReservedWords.kt. DO NOT EDIT!

trait Trait {
    val debugger: Int
}

class TraitImpl : Trait {
    override val debugger: Int = 0
}

class TestDelegate : Trait by TraitImpl() {
    fun test() {
        testNotRenamed("debugger", { debugger })
    }
}

fun box(): String {
    TestDelegate().test()

    return "OK"
}