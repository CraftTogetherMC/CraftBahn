package de.crafttogether.craftbahn.util;

public interface Callback<E extends Throwable, V extends Object> {
    void call(E exception, V result);
}
