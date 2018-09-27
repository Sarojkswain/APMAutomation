package com.ca.apm.systemtest.alm.data.utility;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.alm.data.ALMEntity;
import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.testing.almclient.EntityResources;
import com.ca.testing.almclient.RestWrapper;
import com.ca.testing.almclient.entities.alm.Entity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterators;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.index.navigable.NavigableIndex;

/**
 * Created by haiva01 on 17.4.2015.
 */
public class ALMUtilities {
    private static Logger log = LoggerFactory.getLogger(ALMUtilities.class);

    public static Map<String, String> extractFieldsAndValues(Entity entity) {
        Map<String, String> map = new TreeMap<>();
        Entity.Fields fields = entity.getFields();
        List<Entity.Fields.Field> fieldList = fields.getField();
        for (Entity.Fields.Field field : fieldList) {
            String key = field.getName();
            List<Entity.Fields.Field.Value> valueList = field.getValue();
            if (!valueList.isEmpty()) {
                map.put(key, valueList.get(0).getValue());
                if (valueList.size() > 1) {
                    log.warn("{} has multiple values ({}) for field {}", entity.getType(),
                        valueList.size(), key);
                }
            }
        }
        return map;
    }


    public static IndexedCollection<ALMEntity> freshTestFoldersCollection() {
        IndexedCollection<ALMEntity> almEntities = new ConcurrentIndexedCollection<>();
        almEntities.addIndex(NavigableIndex.onAttribute(ALMEntity.ID));
        almEntities.addIndex(NavigableIndex.onAttribute(ALMEntity.PARENT_ID));
        almEntities.addIndex(NavigableIndex.onAttribute(ALMEntity.NAME));
        return almEntities;
    }


    public static IndexedCollection<ALMEntity> getAllTestFolders(
        RestWrapper wrapper) throws Exception {
        IndexedCollection<ALMEntity> almEntities = freshTestFoldersCollection();
        List<Entity> entityList = wrapper.queryAlmEntityAndGetList(EntityResources.TEST_FOLDERS,
            "{}");
        Iterators.addAll(almEntities, Collections2.transform(entityList,
            new EntityALMEntityFunction()).iterator());
        return almEntities;
    }

    public static IndexedCollection<ALMEntity> freshTestsCollection() {
        IndexedCollection<ALMEntity> almEntities = new ConcurrentIndexedCollection<>();
        almEntities.addIndex(NavigableIndex.onAttribute(ALMEntity.ID));
        almEntities.addIndex(NavigableIndex.onAttribute(ALMEntity.PARENT_ID));
        almEntities.addIndex(NavigableIndex.onAttribute(ALMEntity.NAME));
        return almEntities;
    }

    public static IndexedCollection<ALMEntity> getAllTests(RestWrapper wrapper) throws Exception {
        IndexedCollection<ALMEntity> almEntities = freshTestsCollection();
        List<Entity> entityList = wrapper
            .queryAlmEntityAndGetList(EntityResources.TESTS, "{}");
        Iterators.addAll(almEntities, Collections2.transform(entityList,
            new EntityALMEntityFunction()).iterator());
        return almEntities;
    }

    public static class EntityALMEntityFunction implements Function<Entity, ALMEntity> {
        @Override
        public ALMEntity apply(Entity input) {
            if (input == null) {
                return null;
            }
            return new ALMEntity(input);
        }
    }

//    public static void writeEntitiesIntoXmlFile(String file, Iterable<ALMEntity> entities)
//        throws IOException, JAXBException {
//        JAXBContext jc = JAXBContext.newInstance(Entity.class);
//        Marshaller marshaller = jc.createMarshaller();
//        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
//        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
//            for (ALMEntity entity : entities) {
//                marshaller.marshal(entity.getEntity(), os);
//            }
//        }
//    }

    public static void writeEntitiesIntoJsonFile(String file, Iterable<ALMEntity> entities)
        throws IOException, JAXBException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        AnnotationIntrospector introspector = new JaxbAnnotationIntrospector(
            TypeFactory.defaultInstance());
        // if using BOTH JAXB annotations AND Jackson annotations:
        AnnotationIntrospector secondary = new JacksonAnnotationIntrospector();
        mapper.setAnnotationIntrospector(AnnotationIntrospector.pair(introspector, secondary));

        try (
            OutputStream fs = new FileOutputStream(file);
            OutputStream os = new BufferedOutputStream(fs)) {
            mapper.writeValue(os, entities);
            os.flush();
        } catch (Exception ex) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, ex,
                "Got exception while writing to {1}. Exception: {0}", file);
        }
    }

    public static Collection<ALMEntity> readEntitiesFromJsonFile(String file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<ArrayList<ALMEntity>> ty = new TypeReference<ArrayList<ALMEntity>>() {};
        try (InputStream inputStream = FileUtils.openInputStream(new File(file))) {
            return mapper.<List<ALMEntity>>readValue(inputStream, ty);
        }
    }
}
