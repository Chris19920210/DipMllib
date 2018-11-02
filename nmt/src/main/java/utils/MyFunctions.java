package utils;

public class MyFunctions {

    @FunctionalInterface
    public interface TwoFunction<U, V> {
        void apply(U u, V v);
    }


    @FunctionalInterface
    public interface ThreeFunction<T, U, V> {
        void apply(T t, U u, V v);
    }

    @FunctionalInterface
    public interface FiveFunction<A, B, C, D, E> {
        E apply(A a, B b, C c, D d);
    }
}
