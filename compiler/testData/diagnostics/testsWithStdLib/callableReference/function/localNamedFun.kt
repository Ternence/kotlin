import kotlin.reflect.*

fun main() {
    fun foo() {}
    fun bar(<!UNUSED_PARAMETER!>x<!>: Int) {}
    fun baz() = "OK"
    
    val x = ::foo
    val y = ::bar
    val z = ::baz
    
    x : KFunction0<Unit>
    y : KFunction1<Int, Unit>
    z : KFunction0<String>
}
