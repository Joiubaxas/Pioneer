package org.pioneer.api.configuration.configuration;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.pioneer.api.configuration.configuration.adapter.FileAdapter;
import org.pioneer.api.configuration.configuration.adapter.InetSocketAddressAdapter;

import java.io.*;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;

/*
 * CyConfiguration Project
 * 1.0.0 SNAPSHOT
 *
 * © 2018 Ricardo Borutta
 */
public abstract class AbstractConfiguration {

    private Gson gson;

    public AbstractConfiguration() {
        this.gson = new GsonBuilder().setPrettyPrinting()
                .registerTypeAdapter(InetSocketAddress.class, new InetSocketAddressAdapter())
                .registerTypeAdapter(File.class, new FileAdapter())
                .create();
    }

    public AbstractConfiguration(Gson gson) {
        this.gson = gson;
    }

    /**
     * Loads the configuration file
     */
    public void load() {
        ConfigurationInfo info = getClass().getAnnotation(ConfigurationInfo.class);

        if (info == null) {
            throw new NullPointerException("The configuration info is missing.");
        }

        File file = new File(info.filename());
        if (!file.isFile()) {
            saveDefaults(file);
            return;
        }

        try {
            JsonObject object = gson.fromJson(new FileReader(file), JsonObject.class);

            boolean saveAfterLoading = loadObjectIntoJsonObject(this, object);

            if (saveAfterLoading) {
                save();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * Loads the fields with {@link ConfigurationProperty} and {@link ConfigurationProperties} into the json object.
     *
     * @param object     the object with the fields
     * @param jsonObject the object where the fields load into.
     * @return true if the json object contains all fields.
     */
    private boolean loadObjectIntoJsonObject(Object object, JsonObject jsonObject) {
        if (!object.getClass().isAnnotationPresent(ConfigurationInfo.class) &&
                !object.getClass().isAnnotationPresent(ConfigurationProperties.class)) {
            throw new IllegalArgumentException();
        }

        Field[] fields = object.getClass().getDeclaredFields();

        boolean saveAfterLoading = false;

        for (Field field : fields) {
            field.setAccessible(true);

            if (!field.isAnnotationPresent(ConfigurationProperty.class)) continue;
            ConfigurationProperty property = field.getAnnotation(ConfigurationProperty.class);

            try {
                if (!property.isDefault() && field.get(object) != null) continue;

                String path = property.path();
                JsonObject rootEntry = jsonObject;
                rootEntry = getJsonObject(path, rootEntry);

                JsonElement element = rootEntry.get(property.name());

                Object o = field.get(object);

                if ((element == null || element.isJsonNull()) && o != null) {
                    saveAfterLoading = true;
                    continue;
                }

                if (o != null && o.getClass().isAnnotationPresent(ConfigurationProperties.class)) {
                    JsonObject childObject = element.getAsJsonObject();
                    boolean result = loadObjectIntoJsonObject(o, childObject);
                    if (result) {
                        saveAfterLoading = true;
                    }
                    rootEntry.add(property.name(), childObject);
                    continue;
                }

                field.set(object, gson.fromJson(element, field.getType()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return saveAfterLoading;
    }

    /**
     * Puts the field's values with {@link ConfigurationProperty} and {@link ConfigurationProperties} into the json object.
     *
     * @param object     the object where are the fields inside
     * @param jsonObject where the field values puts into.
     */
    private void saveObjectIntoJsonObject(Object object, JsonObject jsonObject) {
        if (!object.getClass().isAnnotationPresent(ConfigurationInfo.class) &&
                !object.getClass().isAnnotationPresent(ConfigurationProperties.class)) {
            throw new IllegalArgumentException();
        }

        Field[] fields = object.getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);

            if (!field.isAnnotationPresent(ConfigurationProperty.class)) continue;
            ConfigurationProperty property = field.getAnnotation(ConfigurationProperty.class);

            try {
                if (!property.isDefault() && field.get(object) != null) continue;

                String path = property.path();
                JsonObject rootEntry = jsonObject;
                rootEntry = getJsonObject(path, rootEntry);

                Object value = field.get(object);
                if (value != null && value.getClass().isAnnotationPresent(ConfigurationProperties.class)) {
                    JsonObject childObject = new JsonObject();

                    saveObjectIntoJsonObject(value, childObject);
                    rootEntry.add(property.name(), childObject);
                    continue;
                }

                if (!jsonObject.has(property.name())) {
                    rootEntry.add(property.name(), gson.toJsonTree(value));
                }

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Creates or gets the path in the root entry.
     *
     * @param path      the path split by '.'
     * @param rootEntry the json object where the path creates.
     * @return {@link JsonObject} the last object by path.
     */
    private JsonObject getJsonObject(String path, JsonObject rootEntry) {
        JsonObject object = rootEntry;
        if (!path.isEmpty()) {
            String[] root = path.split("\\.");

            for (String entry : root) {

                if (object.has(entry)) {
                    object = object.getAsJsonObject(entry);
                    continue;
                }

                JsonObject newEntry = new JsonObject();
                object.add(entry, newEntry);
                object = newEntry;
            }
        }
        return object;
    }

    private void saveDefaults(File file) {
        try {
            Files.createParentDirs(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        JsonObject object = new JsonObject();

        saveObjectIntoJsonObject(this, object);

        try {
            PrintWriter writer = new PrintWriter(file);
            gson.toJson(object, writer);
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves the configuration file. Force = false
     * Overloaded method.
     */
    public void save() {
        save(false);
    }

    /**
     * Saves the configuration file.
     */
    public void save(boolean force) {

        ConfigurationInfo info = getClass().getAnnotation(ConfigurationInfo.class);

        if (info == null) {
            throw new NullPointerException("The configuration info is missing.");
        }

        File file = new File(info.filename());
        if (!file.isFile()) {
            saveDefaults(file);
            return;
        }

        try {
            JsonObject object = new JsonObject();

            if (!force) {
                gson.fromJson(new FileReader(file), JsonObject.class);
            }

            saveObjectIntoJsonObject(this, object);

            PrintWriter writer = new PrintWriter(file);
            gson.toJson(object, writer);
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
