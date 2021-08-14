package de.crafttogether.craftbahn.util;

public interface Callback<E extends Throwable, V> {
    void call(E exception, V result);
}
