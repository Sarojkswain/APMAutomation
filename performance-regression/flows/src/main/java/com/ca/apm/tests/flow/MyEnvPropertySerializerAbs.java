package com.ca.apm.tests.flow;

import com.ca.tas.property.AbstractEnvPropertySerializer;
import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
public abstract class MyEnvPropertySerializerAbs<T> extends AbstractEnvPropertySerializer<T> {

    public MyEnvPropertySerializerAbs(Class<? extends AbstractEnvPropertySerializer<T>> serializerClass) {
        super(serializerClass);
    }

    protected <E> List<E> deserializeList(Map deserializedMap, String key, Class<E> type) {
        return (List<E>) deserializeObject(deserializedMap, key);
    }

    protected <E> List<E> deserializeListNotNull(Map deserializedMap, String key, Class<E> type) {
        List<E> returnObject = deserializeList(deserializedMap, key, type);
        if (returnObject == null) {
            throw new IllegalArgumentException("Insufficient arguments in env property file: " + key + " is missing.");
        }
        return returnObject;
    }

    protected String serializeList(List list) {
        return serializeObject(list);
    }

    protected <E> Map<String, E> deserializeMap(Map deserializedMap, String key, Class<E> valueType) {
        return (Map<String, E>) deserializeObject(deserializedMap, key);
    }

    protected String serializeMap(Map map) {
        return serializeObject(map);
    }

    protected <E> E[] deserializeArray(Map deserializedMap, String key, Class<E> type) {
        return (E[]) deserializeObject(deserializedMap, key);
    }

    protected <E> String serializeAray(E[] array) {
        return serializeObject(array);
    }

    protected Object deserializeObject(Map deserializedMap, String key) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(Base64.decodeBase64(((String) deserializedMap.get(key)).getBytes()));
             ObjectInputStream ois = new ObjectInputStream(in)) {
            return ois.readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace(); // todo
        }
        return null;
    }

    protected String serializeObject(Object object) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(out)) {
            oos.writeObject(object);
            return new String(Base64.encodeBase64(out.toByteArray()));
        } catch (IOException e) {
            e.printStackTrace(); // todo
        }
        return null;
    }
}
