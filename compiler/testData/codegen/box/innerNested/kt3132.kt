class Test {
    trait Foo { }

    class FooImplNested: Foo { }
    
    inner class FooImplInner: Foo { }
}

fun box(): String {
    Test().FooImplInner()
    Test.FooImplNested()
    return "OK"
}
