package utils;

public class MyFunctions {

    @FunctionalInterface
    public interface ThreeFunction<T, U, V> {
        void apply(T t, U u, V v);
    }

    @FunctionalInterface
    public interface SixFunction<A, B, C, D, E, F>{
        F apply(A a, B b, C c, D d, E e);
    }
}
