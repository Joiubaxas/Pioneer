package org.pioneer.network;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.function.Consumer;

/**
 * A {@link PacketContainer} that will be sent from a {@link Client} to the {@link Server} or vice versa.
 */
public final class PacketContainer {
    private boolean prepend;
    private int size;
    private final Deque<Consumer<ByteBuffer>> queue;

    public PacketContainer() {
        this.queue = new ArrayDeque<>();
    }

    public PacketContainer putByte(int b) {
        size += Byte.BYTES;
        if (prepend) {
            queue.offerFirst(buffer -> buffer.put((byte) b));
        } else {
            queue.offerLast(buffer -> buffer.put((byte) b));
        }
        return this;
    }

    public PacketContainer putBytes(byte... src) {
        size += src.length * Byte.BYTES;
        if (prepend) {
            queue.offerFirst(buffer -> buffer.put(src));
        } else {
            queue.offerLast(buffer -> buffer.put(src));
        }
        return this;
    }

    public PacketContainer putChar(char c) {
        size += Character.BYTES;
        if (prepend) {
            queue.offerFirst(buffer -> buffer.putChar(c));
        } else {
            queue.offerLast(buffer -> buffer.putChar(c));
        }
        return this;
    }

    public PacketContainer putDouble(double d) {
        size += Double.BYTES;
        if (prepend) {
            queue.offerFirst(buffer -> buffer.putDouble(d));
        } else {
            queue.offerLast(buffer -> buffer.putDouble(d));
        }
        return this;
    }

    public PacketContainer putFloat(float f) {
        size += Float.BYTES;
        if (prepend) {
            queue.offerFirst(buffer -> buffer.putFloat(f));
        } else {
            queue.offerLast(buffer -> buffer.putFloat(f));
        }
        return this;
    }

    public PacketContainer putInt(int i) {
        size += Integer.BYTES;
        if (prepend) {
            queue.offerFirst(buffer -> buffer.putInt(i));
        } else {
            queue.offerLast(buffer -> buffer.putInt(i));
        }
        return this;
    }

    public PacketContainer putLong(long l) {
        size += Long.BYTES;
        if (prepend) {
            queue.offerFirst(buffer -> buffer.putLong(l));
        } else {
            queue.offerLast(buffer -> buffer.putLong(l));
        }
        return this;
    }

    public PacketContainer putShort(int s) {
        size += Short.BYTES;
        if (prepend) {
            queue.offerFirst(buffer -> buffer.putShort((short) s));
        } else {
            queue.offerLast(buffer -> buffer.putShort((short) s));
        }
        return this;
    }

    public PacketContainer putString(String s) {
        putShort(s.length());
        putBytes(s.getBytes());
        return this;
    }

    public PacketContainer prepend(Runnable runnable) {
        prepend = true;
        runnable.run();
        prepend = false;
        return this;
    }

    @SafeVarargs
    public final <T extends Client> void write(T... clients) {
        if (clients.length == 0) {
            throw new IllegalArgumentException("You must send this packet to at least one client!");
        }

        for (Client client : clients) {
            if (size > client.getBufferSize()) {
                System.err.println("PacketContainer is too large (Size: " + size + ") for client buffer size (Limit: " + client.getBufferSize() + ")");
                continue;
            }

            client.getOutgoingPackets().offer(this);
        }
    }

    public final void write(Collection<? extends Client> clients) {
        if (clients.isEmpty()) {
            throw new IllegalArgumentException("You must send this packet to at least one client!");
        }

        clients.forEach(client -> {
            if (size > client.getBufferSize()) {
                System.err.println("PacketContainer is too large (Size: " + size + ") for client buffer size (Limit: " + client.getBufferSize() + ")");
                return;
            }

            client.getOutgoingPackets().offer(PacketContainer.this);
        });
    }

    @SafeVarargs
    public final <T extends Client> void writeAndFlush(T... clients) {
        if (clients.length == 0) {
            throw new IllegalArgumentException("You must send this packet to at least one client!");
        }

        for (Client client : clients) {
            if (size > client.getBufferSize()) {
                System.err.println("PacketContainer is too large (Size: " + size + ") for client buffer size (Limit: " + client.getBufferSize() + ")");
                continue;
            }

            client.getOutgoingPackets().offer(this);
            client.flush();
        }
    }

    public final void writeAndFlush(Collection<? extends Client> clients) {
        if (clients.isEmpty()) {
            throw new IllegalArgumentException("You must send this packet to at least one client!");
        }

        clients.forEach(client -> {
            if (size > client.getBufferSize()) {
                throw new IllegalStateException("PacketContainer is too large (Size: " + size + ") for client buffer size (Limit: " + client.getBufferSize() + ")");
            }

            client.getOutgoingPackets().offer(PacketContainer.this);
            client.flush();
        });
    }

    public int getSize() {
        return size;
    }

    public Deque<Consumer<ByteBuffer>> getQueue() {
        return queue;
    }

}
