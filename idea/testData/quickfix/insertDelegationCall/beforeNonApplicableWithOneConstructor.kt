// "Insert 'this()' call" "false"
// ACTION: Insert 'super()' call
// ERROR: There is no applicable constructor for call without arguments in superclass

open class B(val x: Int)

class A : B {
    constructor(x: String)<caret>
}
