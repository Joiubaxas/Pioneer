package org.pioneer.network;

import java.io.IOException;
import java.nio.channels.AsynchronousChannel;
import java.util.function.Consumer;

/**
 *
 * @author Jacob G.
 * @since November 1, 2017
 *
 * Modified for use within Pioneer Minecraft Server
 * @param <T> The channel for this Channeled entity
 */
public interface Channeled<T extends AsynchronousChannel> {
    T getChannel();

    default void close(Consumer<IOException> consumer){
        try {
            getChannel().close();
        } catch (IOException exception) {
            consumer.accept(exception);
        }
    }

    default void close(){
        close($ -> {});
    }
}
