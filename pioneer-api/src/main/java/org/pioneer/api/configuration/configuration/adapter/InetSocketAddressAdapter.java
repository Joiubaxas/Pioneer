package org.pioneer.api.configuration.configuration.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.net.InetSocketAddress;

/*
 * CyConfiguration Project
 * 1.0.0 SNAPSHOT
 *
 * © 2018 Ricardo Borutta
 */
public class InetSocketAddressAdapter extends TypeAdapter<InetSocketAddress> {

    @Override
    public void write(JsonWriter out, InetSocketAddress value) throws IOException {
        out.value(value == null ? null : value.getHostName() + ":" + value.getPort());
    }

    @Override
    public InetSocketAddress read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String address = in.nextString();

        String hostname = address.substring(0, address.lastIndexOf(':'));
        String port = address.substring(address.lastIndexOf(':') + 1);

        return new InetSocketAddress(hostname, Integer.valueOf(port));
    }
}
