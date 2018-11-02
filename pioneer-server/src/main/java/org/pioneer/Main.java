package org.pioneer;

/*
 * Pioneer Project
 * 1.0.0 SNAPSHOT
 *
 * © 2018 Ricardo Borutta
 */
public class Main {

    public static void main(String[] args) {

        System.out.println("Loading configuration...");

        PioneerServerConfiguration configuration = new PioneerServerConfiguration();
        configuration.load();

        PioneerServer server = new PioneerServer(configuration);

        try {
            System.out.println("Starting server...");
            server.start();
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            System.out.println("Adding shutdown hook...");
            Runtime runtime = Runtime.getRuntime();
            runtime.addShutdownHook(new Thread(server::stop, "Pioneer server shutdown"));
        }

    }

}
