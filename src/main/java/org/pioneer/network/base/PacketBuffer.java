package org.pioneer.network.base;

import java.nio.ByteBuffer;

import static java.nio.ByteBuffer.*;

public class PacketBuffer {

    private ByteBuffer buffer;

    public PacketBuffer(int cap, boolean direct) {
        if (direct)
            this.buffer = allocateDirect(cap);
        else
            this.buffer = allocate(cap);
    }

    public PacketBuffer(byte[] array) {
        this.buffer = wrap(array);
    }

    public PacketBuffer writeInt(int i) { buffer.putInt(i); return this; }

    public PacketBuffer writeInts(int... i) { for (int x : i) buffer.putInt(x); return this; }

    public PacketBuffer writeByte(int l) { buffer.put((byte) l); return this; }

    private PacketBuffer writeBytes(int... bytes) { for (int b : bytes) buffer.put((byte) b); return this; }

    //TODO add get methods and varInt and stuff u know
}
