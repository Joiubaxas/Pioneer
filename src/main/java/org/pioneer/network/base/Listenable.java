package org.pioneer.network.base;

import java.util.function.Consumer;

public class Listenable<T extends Listenable> {

    private Consumer<Throwable> exceptionHandler;

}
