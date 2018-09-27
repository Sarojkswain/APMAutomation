package com.ca.apm.systemtest.alm.data;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.alm.data.utility.ALMUtilities;
import com.ca.testing.almclient.EntityResources;
import com.ca.testing.almclient.RestWrapper;
import com.googlecode.cqengine.IndexedCollection;

/**
 * Context for single ALM site.
 */
public class ALMContext {
    static Logger log = LoggerFactory.getLogger(ALMContext.class);

    private RestWrapper wrapper;
    private IndexedCollection<ALMEntity> testFoldersCol;
    private IndexedCollection<ALMEntity> testsCol;
    private final Collection<String> unknownType = new TreeSet<>();

    public ALMContext() {
        this.testFoldersCol = ALMUtilities.freshTestFoldersCollection();
        this.testsCol = ALMUtilities.freshTestsCollection();
    }

    public ALMContext(RestWrapper wrapper) {
        this();
        this.wrapper = wrapper;
    }

    public void fetch() throws Exception {
        testFoldersCol = ALMUtilities.getAllTestFolders(wrapper);
        testsCol = ALMUtilities.getAllTests(wrapper);
    }

    public IndexedCollection<ALMEntity> getTestFoldersCol() {
        return testFoldersCol;
    }

    public IndexedCollection<ALMEntity> getTestsCol() {
        return testsCol;
    }

    public void addEntity(ALMEntity entity) {
        final String ty = entity.getType();
        switch (ty) {
            case "test":
                testsCol.add(entity);
                break;
            case "test-folder":
                testFoldersCol.add(entity);
                break;
            default:
                if (!unknownType.contains(ty)) {
                    log.warn("Unknown type of entity: {}", ty);
                    unknownType.add(ty);
                }
                break;
        }
    }

}
