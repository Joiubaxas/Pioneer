package org.pioneer;

import org.pioneer.network.PioneerServer;

public class Pioneer {
    private static PioneerServer server;

    public static void main(String[] args){
        startServer();
    }

    private static void startServer(){
        server = new PioneerServer();
    }
}
