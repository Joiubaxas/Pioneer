package org.pioneer;

import org.pioneer.network.Server;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class Pioneer {

    private static long ticks;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2); //Need to discuss this

    public static void main(String[] args) {
        var server = new Server();

        scheduler.scheduleAtFixedRate(() -> {
            ticks++;

            //TODO do something every x amount of ticks

        }, 0, 50, TimeUnit.MILLISECONDS);
    }
}