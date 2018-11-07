package org.pioneer.network;

import org.pioneer.util.IntPair;

import javax.crypto.Cipher;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

/**
 * The entity that will connect to the {@link Server}.
 *
 * @author Jacob G.
 * @since November 1, 2017
 * <p>
 * Modified for use within Pioneer Minecraft Server
 */
public class Client extends Receiver<Runnable> implements Channeled<AsynchronousSocketChannel> {
    private static final CompletionHandler<Integer, Client> PACKET_HANDLER = new CompletionHandler<>() {
        @Override
        public void completed(Integer result, Client client) {
            if (!client.channel.isOpen()) {
                client.outgoingPackets.clear();
                client.packetsToFlush.clear();
                return;
            }

            var payload = client.packetsToFlush.pollLast();

            if (payload == null) {
                client.writing.set(false);
                return;
            }

            client.channel.write(payload, client, this);
        }

        @Override
        public void failed(Throwable t, Client client) {
            t.printStackTrace();
        }
    };
    private final AtomicBoolean writing;
    private final Queue<PacketContainer> outgoingPackets;
    private final ByteBuffer buffer;
    private final Deque<ByteBuffer> packetsToFlush;
    private final Deque<IntPair<Consumer<ByteBuffer>>> stack, queue;
    private boolean prepend;
    private int size;
    private Cipher encryption, decryption;
    private ThreadPoolExecutor executor;
    private AsynchronousSocketChannel channel;

    public Client() {
        this(4096);
    }

    public Client(int bufferSize) {
        this(bufferSize, null);
    }

    public Client(int bufferSize, AsynchronousSocketChannel channel) {
        super(bufferSize);

        writing = new AtomicBoolean();
        outgoingPackets = new ArrayDeque<>();
        packetsToFlush = new ConcurrentLinkedDeque<>();
        queue = new ArrayDeque<>();
        stack = new ArrayDeque<>();
        buffer = ByteBuffer.allocateDirect(bufferSize);

        if (channel != null) {
            this.channel = channel;
        }
    }

    public final void connect(String address, int port) {
        connect(address, port, 30L, TimeUnit.SECONDS, () -> System.err.println("Couldn't connect within 30 seconds!"));
    }

    public final void connect(String address, int port, long timeout, TimeUnit unit, Runnable onTimeout) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("The port must be between 0 and 65535!");
        }

        executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), runnable -> {
            Thread thread = new Thread(runnable);
            thread.setDaemon(false);
            return thread;
        });

        executor.prestartAllCoreThreads();

        try {
            this.channel = AsynchronousSocketChannel.open(AsynchronousChannelGroup.withThreadPool(executor));
            this.channel.setOption(StandardSocketOptions.SO_RCVBUF, bufferSize);
            this.channel.setOption(StandardSocketOptions.SO_SNDBUF, bufferSize);
            this.channel.setOption(StandardSocketOptions.SO_KEEPALIVE, false);
            this.channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to open the channel!");
        }

        try {
            channel.connect(new InetSocketAddress(address, port)).get(timeout, unit);
        } catch (AlreadyConnectedException e) {
            throw new IllegalStateException("This client is already connected to a server!");
        } catch (ExecutionException e) {
            throw new IllegalStateException("An ExecutionException has occurred:", e);
        } catch (Exception e) {
            onTimeout.run();
            close();
            return;
        }

        connectListeners.forEach(Runnable::run);

        try {
            channel.read(buffer, this, Listener.INSTANCE);
        } catch (ShutdownChannelGroupException e) {
            // This exception is caught whenever a client closes their
            // connection to the server. In that case, do nothing.
        }
    }

    @Override
    public void close() {
        preDisconnectListeners.forEach(Runnable::run);

        flush();

        while (writing.get()) {
            Thread.onSpinWait();
        }

        close($ -> System.err.println("There was an error closing the Client channel!"));

        if (executor != null) {
            executor.shutdownNow();
        }

        while (channel.isOpen()) {
            Thread.onSpinWait();
        }

        postDisconnectListeners.forEach(Runnable::run);
    }

    public void preDisconnect(Runnable listener) {
        preDisconnectListeners.add(listener);
    }

    public void postDisconnect(Runnable listener) {
        postDisconnectListeners.add(listener);
    }

    public final void read(int n, Consumer<ByteBuffer> consumer) {
        if (size >= n) {
            size -= n;
            consumer.accept(buffer);
            return;
        }

        if (prepend) {
            stack.addFirst(new IntPair<>(n, consumer));
        } else {
            queue.offer(new IntPair<>(n, consumer));
        }
    }

    public final void readAlways(int n, Consumer<ByteBuffer> consumer) {
        read(n, new Consumer<>() {
            @Override
            public void accept(ByteBuffer buffer) {
                consumer.accept(buffer);
                read(n, this);
            }
        });
    }

    private <T> T read(CompletableFuture<T> future) {
        return future.join();
    }

    public final byte readByte() {
        var future = new CompletableFuture<Byte>();
        readByte(future::complete);
        return read(future);
    }

    public final void readByte(Consumer<Byte> consumer) {
        read(Byte.BYTES, buffer -> consumer.accept(buffer.get()));
    }

    public final void readByteAlways(Consumer<Byte> consumer) {
        readAlways(Byte.BYTES, buffer -> consumer.accept(buffer.get()));
    }

    public final byte[] readBytes(int n) {
        var future = new CompletableFuture<byte[]>();
        readBytes(n, future::complete);
        return read(future);
    }

    public final void readBytes(int n, Consumer<byte[]> consumer) {
        read(n, buffer -> {
            byte[] b = new byte[n];
            buffer.get(b);
            consumer.accept(b);
        });
    }

    public final void readBytesAlways(int n, Consumer<byte[]> consumer) {
        readAlways(n, buffer -> {
            byte[] b = new byte[n];
            buffer.get(b);
            consumer.accept(b);
        });
    }

    public final char readChar() {
        var future = new CompletableFuture<Character>();
        readChar(future::complete);
        return read(future);
    }

    public final void readChar(Consumer<Character> consumer) {
        read(Character.BYTES, buffer -> consumer.accept(buffer.getChar()));
    }

    public final void readCharAlways(Consumer<Character> consumer) {
        readAlways(Character.BYTES, buffer -> consumer.accept(buffer.getChar()));
    }

    public final double readDouble() {
        var future = new CompletableFuture<Double>();
        readDouble(future::complete);
        return read(future);
    }

    public final void readDouble(DoubleConsumer consumer) {
        read(Double.BYTES, buffer -> consumer.accept(buffer.getDouble()));
    }

    public final void readDoubleAlways(DoubleConsumer consumer) {
        readAlways(Double.BYTES, buffer -> consumer.accept(buffer.getDouble()));
    }

    public final float readFloat() {
        var future = new CompletableFuture<Float>();
        readFloat(future::complete);
        return read(future);
    }

    public final void readFloat(Consumer<Float> consumer) {
        read(Float.BYTES, buffer -> consumer.accept(buffer.getFloat()));
    }

    public final void readFloatAlways(Consumer<Float> consumer) {
        readAlways(Float.BYTES, buffer -> consumer.accept(buffer.getFloat()));
    }

    public final int readInt() {
        var future = new CompletableFuture<Integer>();
        readInt(future::complete);
        return read(future);
    }

    public final void readInt(IntConsumer consumer) {
        read(Integer.BYTES, buffer -> consumer.accept(buffer.getInt()));
    }

    public final void readIntAlways(IntConsumer consumer) {
        readAlways(Integer.BYTES, buffer -> consumer.accept(buffer.getInt()));
    }

    public final long readLong() {
        var future = new CompletableFuture<Long>();
        readLong(future::complete);
        return read(future);
    }

    public final void readLong(LongConsumer consumer) {
        read(Long.BYTES, buffer -> consumer.accept(buffer.getLong()));
    }

    public final void readLongAlways(LongConsumer consumer) {
        readAlways(Long.BYTES, buffer -> consumer.accept(buffer.getLong()));
    }

    public final short readShort() {
        var future = new CompletableFuture<Short>();
        readShort(future::complete);
        return read(future);
    }

    public final void readShort(Consumer<Short> consumer) {
        read(Short.BYTES, buffer -> consumer.accept(buffer.getShort()));
    }

    public final void readShortAlways(Consumer<Short> consumer) {
        readAlways(Short.BYTES, buffer -> consumer.accept(buffer.getShort()));
    }

    public final String readString() {
        var future = new CompletableFuture<String>();
        readString(future::complete);
        return read(future);
    }

    public final void readString(Consumer<String> consumer) {
        readShort(s -> read(s, buffer -> {
            var b = new byte[s & 0xFFFF];
            buffer.get(b);
            consumer.accept(new String(b));
        }));
    }

    public final void readStringAlways(Consumer<String> consumer) {
        readShortAlways(s -> read(s, buffer -> {
            var b = new byte[s & 0xFFFF];
            buffer.get(b);
            consumer.accept(new String(b));
        }));
    }

    public final void flush() {
        int totalBytes = 0;
        int amountToPoll = outgoingPackets.size();

        PacketContainer packet;

        var queue = new ArrayDeque<Consumer<ByteBuffer>>();

        while (amountToPoll-- > 0 && (packet = outgoingPackets.poll()) != null) {
            int currentBytes = totalBytes;

            boolean tooBig = (totalBytes += packet.getSize()) >= bufferSize;
            boolean empty = outgoingPackets.isEmpty();

            if (!tooBig || empty) {
                queue.addAll(packet.getQueue());
            }

            // If we've buffered all of the packets that we can, send them off.
            if (tooBig || empty) {
                var raw = ByteBuffer.allocateDirect(empty ? totalBytes : currentBytes);

                Consumer<ByteBuffer> consumer;

                while ((consumer = queue.pollFirst()) != null) {
                    consumer.accept(raw);
                }

                queue.addAll(packet.getQueue());

                raw.flip();

                if (encryption != null) {
                    try {
                        encryption.update(raw, raw.duplicate());
                        raw.flip();
                    } catch (Exception e) {
                        throw new IllegalStateException("Exception occurred when encrypting:", e);
                    }
                }

                if (!writing.getAndSet(true)) {
                    channel.write(raw, this, PACKET_HANDLER);
                } else {
                    packetsToFlush.offerFirst(raw);
                }

                totalBytes = packet.getSize();
            }
        }
    }

    public Queue<PacketContainer> getOutgoingPackets() {
        return outgoingPackets;
    }

    @Override
    public final AsynchronousSocketChannel getChannel() {
        return channel;
    }

    ByteBuffer getBuffer() {
        return buffer;
    }

    public void setEncryption(Cipher encryption) {
        if (!encryption.getAlgorithm().endsWith("NoPadding")) {
            throw new IllegalArgumentException("The cipher cannot have any padding!");
        }

        this.encryption = encryption;
    }

    public void setDecryption(Cipher decryption) {
        if (!decryption.getAlgorithm().endsWith("NoPadding")) {
            throw new IllegalArgumentException("The cipher cannot have any padding!");
        }

        this.decryption = decryption;
    }

    static class Listener implements CompletionHandler<Integer, Client> {
        private static final Listener INSTANCE = new Listener();

        @Override
        public void completed(Integer result, Client client) {
            // A result of -1 normally means that the end-of-stream has been
            // reached. In that case, close the client's connection.
            if (result == -1) {
                client.close();
                return;
            }

            client.size += result;

            var buffer = client.buffer.flip();
            var queue = client.queue;

            IntPair<Consumer<ByteBuffer>> peek;

            if ((peek = queue.pollLast()) == null) {
                client.channel.read(buffer.flip().limit(buffer.capacity()), client, this);
                return;
            }

            client.prepend = true;

            boolean decrypt = client.decryption != null;

            var stack = client.stack;

            int key;

            while (client.size >= (key = peek.getKey())) {
                if (decrypt) {
                    try {
                        int position = buffer.position();
                        buffer.limit(buffer.position() + key);
                        client.decryption.update(buffer, buffer.duplicate());
                        buffer.flip().position(position);
                    } catch (Exception e) {
                        throw new IllegalStateException("Exception occurred when decrypting:", e);
                    }
                }

                client.size -= key;

                peek.getValue().accept(buffer);

                while (!stack.isEmpty()) {
                    queue.offer(stack.poll());
                }

                if ((peek = queue.pollLast()) == null) {
                    break;
                }
            }

            client.prepend = false;

            if (peek != null) {
                queue.addLast(peek);
            }

            if (client.size > 0) {
                buffer.compact();
            } else {
                buffer.flip();
            }

            client.channel.read(buffer.limit(buffer.capacity()), client, this);
        }

        @Override
        public void failed(Throwable t, Client client) {
            client.close();
        }
    }
}