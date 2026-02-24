package Def;

interface C {}

interface D {}

class A implements C {}

class B extends A implements D {}

public class TestA {
    public static void main(String[] args) {
        B itemB = new B();

        System.out.println(itemB instanceof C); // true
        System.out.println(itemB instanceof D); // true
        System.out.println(itemB instanceof A); // true
        System.out.println(itemB instanceof B); // true
    }
}
