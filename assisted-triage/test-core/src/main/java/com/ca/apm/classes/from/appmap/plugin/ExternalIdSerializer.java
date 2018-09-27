package com.ca.apm.classes.from.appmap.plugin;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class ExternalIdSerializer extends StdSerializer<ExternalId>{

    protected ExternalIdSerializer(Class<ExternalId> t) {
        super(t);
    }
    
    public ExternalIdSerializer() {
        this(null);
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    public void serialize(ExternalId value, JsonGenerator jgen, SerializerProvider provider)
        throws IOException {
        jgen.writeString(value.toString());
    }

}
