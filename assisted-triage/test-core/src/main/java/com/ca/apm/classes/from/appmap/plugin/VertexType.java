package com.ca.apm.classes.from.appmap.plugin;

import java.util.Collection;
import java.util.EnumSet;

/**
 * Vertex Type Entity
 * 
 * @author lokra02
 *
 */
public class VertexType {
        
    
    public static enum Type{
        EXTERNAL, BUSINESSTRANSACTION, EJB, EJBCLIENT, DATABASE, DATABASE_SOCKET, SOCKET, WEBSERVICE, WEBSERVICE_SERVER, JMSSERVER, SERVLET, TRANSACTION_PROCESSOR, GENERICFRONTEND, GENERICBACKEND, DEFAULT, AUTOMATICENTRYPOINT, APPLICATION_ENTRYPOINT, INFERRED_DATABASE, INFERRED_SOCKET, INFERRED_WEBSERVICE, INFERRED_GENERICBACKEND,
        ENTERPRISE_MANAGER, EM_MASTER, EM_PROVIDER, EM_MOM, EM_COLLECTOR, AGENT, EM_DATABASE, DATABASE_SERVER, 
        AGENT_CONNECTION, EM_CONNECTION, EMDB_CONNECTION;
                
        public static final EnumSet<Type> TRANSACTION_TYPES = EnumSet.of( BUSINESSTRANSACTION, APPLICATION_ENTRYPOINT );
        private static final Type[] TRANSACTION_TYPES_AS_ARRAY = TRANSACTION_TYPES.toArray(new Type[TRANSACTION_TYPES.size()]); 
        
        public static boolean isTransaction(Type type) {
            return TRANSACTION_TYPES.contains(type);
        }
        
        public static boolean isTransaction(String type) {
            for (Type trType : TRANSACTION_TYPES_AS_ARRAY) {
                if (trType.name().equalsIgnoreCase(type)) {
                    return true;
                }
            }
            return false;
        }
        
        public static boolean containsTransactionType(Collection<String> types) {
            if (types == null) {
                return false;
            }
            
            for (Type type : TRANSACTION_TYPES_AS_ARRAY) {
                if (types.contains(type.name())) {
                    return true;
                }
            }
            return false;
        }
    }


}
