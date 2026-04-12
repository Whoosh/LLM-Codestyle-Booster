package test;
public class MyInterfaceTest {
    void testDefault() {
        MyInterface x = new MyInterface() {
            public void abstractMethod() {}
        };
        x.defaultMethod();
    }
}
