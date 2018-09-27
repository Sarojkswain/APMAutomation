package com.ca.apm.classes.from.appmap.plugin;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class ExternalIdDeserializer extends StdDeserializer<ExternalId> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ExternalIdDeserializer() {
        this(null);
    }
    
    public ExternalIdDeserializer(Class<ExternalId> vc) {
        super(vc);
    }

    @Override
    public ExternalId deserialize(JsonParser jparser, DeserializationContext context) throws IOException,
        JsonProcessingException {
        String externalId = jparser.getText();
        return ExternalId.fromString(externalId);
    }

}
