package com.ca.apm.systemtest.fld.hammond.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.http.util.Args;
import org.h2.tools.Csv;

import com.ca.apm.systemtest.fld.hammond.Configuration;
import com.wily.introscope.spec.metric.AgentName;
import com.wily.introscope.spec.metric.BadlyFormedNameException;
import com.wily.introscope.spec.server.appmap.IAppMapEdge;
import com.wily.introscope.spec.server.appmap.IAppMapEdge.IAppMapEdgeOwnerType;
import com.wily.introscope.spec.server.appmap.IAppMapOwner;
import com.wily.introscope.spec.server.appmap.IAppMapProperty;
import com.wily.introscope.spec.server.appmap.IAppMapVertex;
import com.wily.introscope.spec.server.appmap.impl.AAppMapDate;
import com.wily.introscope.spec.server.appmap.impl.AAppMapEdge;
import com.wily.introscope.spec.server.appmap.impl.AAppMapOwner;
import com.wily.introscope.spec.server.appmap.impl.AAppMapVertex;
import com.wily.introscope.spec.server.appmap.impl.AAppMapVertex.OutOfTheBoxAppMapVertexAbstractionLevel;
import com.wily.introscope.spec.server.appmap.impl.AAppMapVertex.OutOfTheBoxAppMapVertexHierarchyLevel;
import com.wily.introscope.spec.server.appmap.impl.AAppMapVertex.OutOfTheBoxAppMapVertexProperty;
import com.wily.introscope.spec.server.appmap.impl.AAppMapVertex.VertexAppMapProperties;
import com.wily.introscope.spec.server.appmap.impl.AbstractAppMapElement;
import com.wily.introscope.spec.server.appmap.impl.AbstractAppMapElement.CommonAppMapProperties;
import com.wily.util.feedback.ApplicationFeedback;

public class AppmapData {

    public static final long TOPOLOGY_REPORTING_PERIOD_MILLIS = 12 * 60 * 60 * 1000;

    // private static final long INTERVAL = 6 * 60 * 60 * 1000;

    private Long slicesStartTime;
    private Long slicesEndTime;

    private ApplicationFeedback feedback;
    private Connection conn;
    private HashSet<String> agentNames;

    private Path dataFolder;

    private PreparedStatement pstmtTimestamp;

    final private static String sql =
        "SELECT "
            + "owner.id, owner.name, owner.type, head_owner.name, head_owner.id, tail_owner.name, tail_owner.id, edge.timestamp, edge.properties, head_vertex.abstraction_level, tail_vertex.abstraction_level, head_vertex.hierarchy_level, tail_vertex.hierarchy_level, head_vertex.name, tail_vertex.name, head_vertex_type.name, tail_vertex_type.name, head_agent.name, tail_agent.name, head_agent.host_name, tail_agent.host_name, head_agent.fully_qualified_host_name, tail_agent.fully_qualified_host_name, head_agent.process_name, tail_agent.process_name "
            + "FROM apm_edge AS edge "
            +

            "INNER JOIN apm_vertex AS head_vertex ON edge.head_vertex_id = head_vertex.id "
            + "LEFT JOIN apm_agent AS head_agent ON head_vertex.agent_id=head_agent.id "
            + "INNER JOIN apm_vertex_type AS head_vertex_type ON head_vertex.vertex_type_id=head_vertex_type.id "
            +

            "INNER JOIN apm_vertex AS tail_vertex ON edge.tail_vertex_id = tail_vertex.id "
            + "LEFT JOIN apm_agent as tail_agent ON tail_vertex.agent_id=tail_agent.id "
            + "INNER JOIN apm_vertex_type AS tail_vertex_type ON tail_vertex.vertex_type_id=tail_vertex_type.id "
            +

            "LEFT JOIN apm_owner AS head_owner ON edge.head_owner_id = head_owner.id "
            + "LEFT JOIN apm_owner AS tail_owner ON edge.tail_owner_id = tail_owner.id " +

            "INNER JOIN apm_owner AS owner ON edge.owner_id = owner.id " +

            "WHERE ";

    public AppmapData(String dataFolderName) throws IOException {
        this.feedback = Configuration.instance().createFeedback("AppmapData");

        dataFolder = Paths.get(dataFolderName, "edges", "h2");
        try {
            Class.forName("org.h2.Driver");
            conn = DriverManager.getConnection("jdbc:h2:" + dataFolder.toAbsolutePath() + ";MVCC=FALSE"); // ;MULTI_THREADED=TRUE
        } catch (SQLException e) {
            feedback.error("cannot open database", e);
        } catch (ClassNotFoundException e) {
            feedback.error("database driver not found", e);
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    if (conn != null) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    feedback.error("cannot close database", e);
                }
            }
        });
    }

    public void save() {}

    public void load() {
        try {
            conn.setReadOnly(true);

            ResultSet rset = conn.getMetaData().getTables(null, null, "APM_EDGE", null);
            if (rset.next()) {
                pstmtTimestamp =
                    conn.prepareStatement(sql
                        + "edge.timestamp BETWEEN ? AND ? "
                        + "AND ((head_vertex.abstraction_level='P' AND head_agent.name=? AND head_agent.host_name=? AND head_agent.process_name=?) "
                        + "OR (tail_vertex.abstraction_level='P' AND tail_agent.name=? AND tail_agent.host_name=? AND tail_agent.process_name=?))");
            }

        } catch (SQLException e) {
            feedback.error("cannot set database readonly", e);
        }
    }

    public void importCsv(Path csvFolder) {
        Args.check(Files.isDirectory(csvFolder), "csv folder");

        Statement stmt;
        PreparedStatement pstmt;
        ResultSet rs;
        String sql = null;

        try {
            stmt = conn.createStatement();

            sql = "DROP TABLE IF EXISTS apm_agent";
            stmt.executeUpdate(sql);
            sql =
                "CREATE TABLE apm_agent (" + "id bigint PRIMARY KEY,"
                    + "name character varying(200) NOT NULL," + "host_name character varying(200),"
                    + "process_name character varying(200),"
                    + "fully_qualified_host_name character varying(200),"
                    + "creation_date timestamp NOT NULL," + "update_date timestamp NOT NULL,"
                    + "user_name character varying(200)" + ") " + "AS SELECT * FROM CSVREAD('"
                    + Paths.get(csvFolder.toString(), "apm_agent.csv").toString() + "');";
            stmt.executeUpdate(sql);

            sql = "DROP TABLE IF EXISTS apm_owner";
            stmt.executeUpdate(sql);
            sql =
                "CREATE TABLE apm_owner ("
                    + "id bigint PRIMARY KEY,"
                    + "name character varying(200) NOT NULL,"
                    + "transaction_id numeric,"
                    + "type character varying(30) NOT NULL,"
                    + "creation_date timestamp NOT NULL,"
                    + "update_date timestamp NOT NULL,"
                    + "user_name character varying(200) NOT NULL,"
                    + "CONSTRAINT apm_owner_type_check CHECK ((((type)::text = 'App'::text) OR ((type)::text"
                    + "= 'BTC'::text)))" + ") " + "AS SELECT * FROM CSVREAD('"
                    + Paths.get(csvFolder.toString(), "apm_owner.csv").toString() + "');";
            stmt.executeUpdate(sql);

            sql = "DROP TABLE IF EXISTS apm_vertex_type";
            stmt.executeUpdate(sql);
            sql =
                "CREATE TABLE apm_vertex_type (" + "id integer PRIMARY KEY,"
                    + "name character varying(200) NOT NULL," + "creation_date timestamp NOT NULL,"
                    + "update_date timestamp NOT NULL,"
                    + "user_name character varying(200) NOT NULL" + ") "
                    + "AS SELECT * FROM CSVREAD('"
                    + Paths.get(csvFolder.toString(), "apm_vertex_type.csv").toString() + "');";
            stmt.executeUpdate(sql);

            sql = "DROP TABLE IF EXISTS apm_vertex";
            stmt.executeUpdate(sql);
            sql =
                "CREATE TABLE apm_vertex (" + "id bigint PRIMARY KEY,"
                    + "name character varying(2000) NOT NULL,"
                    + "abstraction_level character(1) NOT NULL,"
                    + "hierarchy_level character varying(200) NOT NULL," + "properties bytea,"
                    + "creation_date bigint NOT NULL," + "update_date bigint NOT NULL,"
                    + "user_name character varying(200) NOT NULL," + "parent_id bigint,"
                    + "agent_id bigint," + "vertex_type_id integer NOT NULL,"
                    + "FOREIGN KEY(agent_id) REFERENCES apm_agent(ID),"
                    + "FOREIGN KEY(vertex_type_id) REFERENCES apm_vertex_type(ID),"
                    + "CONSTRAINT apm_vertex_abstraction_level_check CHECK (((abstraction_level ="
                    + "'P') OR (abstraction_level = 'L'))));";
            stmt.executeUpdate(sql);

            rs =
                new Csv().read(Paths.get(csvFolder.toString(), "apm_vertex.csv").toString(), null,
                    null);
            sql = "INSERT INTO apm_vertex VALUES(?,?,?,?,?,?,?,?,?,?,?)";
            pstmt = conn.prepareStatement(sql);
            while (rs.next()) {
                pstmt.setLong(1, rs.getLong(1));
                pstmt.setString(2, rs.getString(2));
                pstmt.setString(3, rs.getString(3));
                pstmt.setString(4, rs.getString(4));
                pstmt.setBytes(5, rs.getBytes(5));
                pstmt.setLong(6, Timestamp.valueOf(rs.getString(6)).getTime());
                pstmt.setLong(7, Timestamp.valueOf(rs.getString(7)).getTime());
                pstmt.setString(8, rs.getString(8));
                pstmt.setLong(9, rs.getLong(9));
                pstmt.setLong(10, rs.getLong(10));
                pstmt.setInt(11, rs.getInt(11));

                pstmt.executeUpdate();
            }
            pstmt.close();

            sql = "DROP TABLE IF EXISTS apm_edge";
            stmt.executeUpdate(sql);
            sql = "DROP INDEX IF EXISTS PUBLIC.IDX_EDGE_CREATION_DATE";
            stmt.executeUpdate(sql);
            sql = "DROP INDEX IF EXISTS PUBLIC.IDX_EDGE_UPDATE_DATE";
            stmt.executeUpdate(sql);
            sql = "DROP INDEX IF EXISTS PUBLIC.IDX_EDGE_TIMESTAMP";
            stmt.executeUpdate(sql);
            sql =
                "CREATE TABLE apm_edge ("
                    + "id bigint auto_increment PRIMARY KEY,"
                    + "head_vertex_id bigint NOT NULL,"
                    + "tail_vertex_id bigint NOT NULL,"
                    + "owner_id bigint NOT NULL,"
                    + "head_owner_id bigint,"
                    + "tail_owner_id bigint,"
                    + "type character(2) DEFAULT 'RR' NOT NULL,"
                    + "properties bytea,"
                    + "creation_date bigint NOT NULL,"
                    + "update_date bigint NOT NULL,"
                    + "user_name character varying(200) NOT NULL,"
                    + "timestamp bigint NOT NULL,"
                    + "FOREIGN KEY(head_vertex_id) REFERENCES apm_vertex(ID),"
                    + "FOREIGN KEY(tail_vertex_id) REFERENCES apm_vertex(ID),"
                    + "FOREIGN KEY(head_owner_id) REFERENCES apm_owner(ID),"
                    + "FOREIGN KEY(tail_owner_id) REFERENCES apm_owner(ID),"
                    + "CONSTRAINT apm_edge_type_check CHECK ((((type = 'RR') OR (type = 'RQ')) OR (type = 'RS')))"
                    + ");";
            stmt.executeUpdate(sql);

            rs =
                new Csv().read(Paths.get(csvFolder.toString(), "apm_edge.csv").toString(), null,
                    null);

            sql = "INSERT INTO apm_edge VALUES(default,?,?,?,?,?,?,?,?,?,?,?)";
            pstmt = conn.prepareStatement(sql);

            while (rs.next()) {
                long creation_date = Timestamp.valueOf(rs.getString(9)).getTime();
                long update_date = Timestamp.valueOf(rs.getString(10)).getTime();

                for (long timestamp : calculateTimestamp(creation_date, update_date)) {
                    pstmt.setLong(1, rs.getLong(2));
                    pstmt.setLong(2, rs.getLong(3));
                    pstmt.setLong(3, rs.getLong(4));
                    if (rs.getLong(5) == 0) {
                        pstmt.setNull(4, Types.BIGINT);
                    } else {
                        pstmt.setLong(4, rs.getLong(5));
                    }
                    if (rs.getLong(6) == 0) {
                        pstmt.setNull(5, Types.BIGINT);
                    } else {
                        pstmt.setLong(5, rs.getLong(6));
                    }
                    pstmt.setString(6, rs.getString(7));
                    pstmt.setBytes(7, rs.getBytes(8));
                    pstmt.setLong(8, creation_date);
                    pstmt.setLong(9, update_date);
                    pstmt.setString(10, rs.getString(11));
                    pstmt.setLong(11, timestamp);
                    pstmt.executeUpdate();
                }
            }
            pstmt.close();
            rs.close();

            sql = "CREATE INDEX PUBLIC.IDX_AGENT ON PUBLIC.APM_AGENT(NAME,HOST_NAME,PROCESS_NAME)";
            stmt.executeUpdate(sql);
            sql = "CREATE INDEX PUBLIC.IDX_EDGE_TIMESTAMP ON PUBLIC.APM_EDGE(TIMESTAMP)";
            stmt.executeUpdate(sql);

            sql = "SHUTDOWN DEFRAG";
            stmt.executeUpdate(sql);

            stmt.close();

            conn.close();
            conn = null;
        } catch (SQLException e) {
            feedback.error("database error", e);
        }
    }

    static final long INTERVAL = 6 * 60 * 60 * 1000;

    private List<Long> calculateTimestamp(long creation, long update) {
        ArrayList<Long> result = new ArrayList<Long>();
        if (checkTimeRange(creation, update)) {
            if (creation == update) {
                result.add(creation);
            } else {
                int repetitions = (int) Math.ceil((double) (update - creation) / INTERVAL);
                if (repetitions <= 1) {
                    if (creation > slicesStartTime) {
                        result.add(creation);
                    }
                    if (update < slicesEndTime) {
                        result.add(update);
                    }
                } else {
                    double increment = (double) (update - creation) / repetitions;
                    for (int i = 0; i <= repetitions; i++) {
                        long ts = creation + (long) (increment * i);
                        if (ts > slicesStartTime && ts < slicesEndTime) {
                            result.add(ts);
                        }
                    }
                }
            }
        }

        return result;
    }

    public boolean checkTimeRange(long creationTime, long updateTime) {
        if (slicesStartTime != null && updateTime < slicesStartTime || slicesEndTime != null
            && creationTime > slicesEndTime) {
            return false;
        }
        return true;
    }

    public boolean checkAgentName(IAppMapEdge edge) {
        try {
            if (agentNames == null) {
                return true;
            } else {
                Map<IAppMapProperty, Object> prop = edge.getHeadNode().getProperties();
                String headAgentName =
                    AgentName.getAgentName(
                        com.wily.introscope.domain.KDomainConstants.kSuperDomainName,
                        (String) prop.get(OutOfTheBoxAppMapVertexProperty.HostName),
                        (String) prop.get(OutOfTheBoxAppMapVertexProperty.ProcessName),
                        (String) prop.get(OutOfTheBoxAppMapVertexProperty.AgentName))
                        .getProcessURLWithoutDomain();
                prop = edge.getTailNode().getProperties();
                String tailAgentName =
                    AgentName.getAgentName(
                        com.wily.introscope.domain.KDomainConstants.kSuperDomainName,
                        (String) prop.get(OutOfTheBoxAppMapVertexProperty.HostName),
                        (String) prop.get(OutOfTheBoxAppMapVertexProperty.ProcessName),
                        (String) prop.get(OutOfTheBoxAppMapVertexProperty.AgentName))
                        .getProcessURLWithoutDomain();

                return agentNames.contains(headAgentName) || agentNames.contains(tailAgentName);
            }
        } catch (BadlyFormedNameException e) {
            return false;
        }
    }

    public void setSlicesRange(Long startTime, Long endTime) {
        if (startTime != null && endTime != null && startTime < endTime) {
            this.slicesStartTime = startTime;
            this.slicesEndTime = endTime;
        }
    }

    public void setAgentNames(List<AgentName> list) {
        agentNames = new HashSet<>();
        for (AgentName an : list) {
            agentNames.add(an.getProcessURLWithoutDomain());
        }
    }

    public List<IAppMapEdge> getEdgesInInterval(long start, long duration, long offset,
        AgentName agentName) {

        ArrayList<IAppMapEdge> result = new ArrayList<>();

        if (pstmtTimestamp != null) {
            try {
                synchronized (conn) {
                    executeEdgesInIntervalQuery(start, duration, offset, agentName, result,
                        pstmtTimestamp);
                }
            } catch (SQLException e) {
                feedback.error(String.format(
                    "Cannot read edge from H2 database. Requesting slice %d for agent %s", start
                        - offset, agentName.toString()), e);
            }
        }
        return result;
    }

    private void executeEdgesInIntervalQuery(long start, long duration, long offset,
        AgentName agentName, ArrayList<IAppMapEdge> result, PreparedStatement pstmt)
        throws SQLException {

        pstmt.setLong(1, start - offset);
        pstmt.setLong(2, start + duration - offset);
        pstmt.setString(3, agentName.getAgentName());
        pstmt.setString(4, agentName.getHost());
        pstmt.setString(5, agentName.getProcess());
        pstmt.setString(6, agentName.getAgentName());
        pstmt.setString(7, agentName.getHost());
        pstmt.setString(8, agentName.getProcess());

        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            result.add(createEdge(rs, offset));
        }
        rs.close();
    }

    private IAppMapEdge createEdge(ResultSet rs, long offset) throws SQLException {
        IAppMapVertex head = null;
        IAppMapVertex tail = null;
        IAppMapOwner owner = null;
        IAppMapEdgeOwnerType ownerType = null;
        IAppMapOwner headOwner = null;
        IAppMapOwner tailOwner = null;

        owner = AAppMapOwner.getInstance(rs.getLong(1), rs.getString(2));
        switch (rs.getString(3)) {
            case "App":
                ownerType = AAppMapEdge.OutOfTheBoxAppMapOwnerType.Application;
                break;
            case "BTC":
                ownerType = AAppMapEdge.OutOfTheBoxAppMapOwnerType.BusinessTransactionComponent;
                break;
        }

        String headOwnerName = rs.getString(4);
        long headOwnerId = rs.getLong(5);
        if (rs.wasNull()) {
            headOwner = AbstractAppMapElement.NOINFO_EDGE_OWNER;
        } else {
            headOwner = AAppMapOwner.getInstance(headOwnerId, headOwnerName);
        }

        String tailOwnerName = rs.getString(6);
        long tailOwnerId = rs.getLong(7);
        if (rs.wasNull()) {
            tailOwner = AbstractAppMapElement.NOINFO_EDGE_OWNER;
        } else {
            tailOwner = AAppMapOwner.getInstance(tailOwnerId, tailOwnerName);
        }

        head = createVertex(rs, offset, 0, headOwner);

        tail = createVertex(rs, offset, 1, tailOwner);

        if (head == null || tail == null || owner == null || ownerType == null || headOwner == null
            || tailOwner == null) {
            return null;
        }

        AAppMapEdge retval = new AAppMapEdge(head, tail, owner, ownerType, headOwner, tailOwner);
        AAppMapDate timestamp = new AAppMapDate(rs.getLong(8) + offset);
        retval.getProperties().put(CommonAppMapProperties.CreationDate, timestamp);
        retval.getProperties().put(CommonAppMapProperties.UpdateDate, timestamp);

        Properties userProps = deserialize(rs.getBytes(9));
        if (userProps != null) {
            for (Object propKey : userProps.keySet()) {
                Object propValue = userProps.get(propKey);
                retval.getUserDefinedProperties().put(propKey.toString(), propValue.toString());
            }
        }

        return retval;
    }

    private IAppMapVertex createVertex(ResultSet rs, long offset, int prefix, IAppMapOwner owner)
        throws SQLException {
        AAppMapVertex vertex = new AAppMapVertex();
        Map<IAppMapProperty, Object> props = vertex.getProperties();

        if ("L".equals(rs.getString(prefix + 10))) {
            props.put(VertexAppMapProperties.AbstractionLevel,
                OutOfTheBoxAppMapVertexAbstractionLevel.Logical);
        } else {
            props.put(VertexAppMapProperties.AbstractionLevel,
                OutOfTheBoxAppMapVertexAbstractionLevel.Physical);
        }
        props.put(VertexAppMapProperties.HierarchyLevel, OutOfTheBoxAppMapVertexHierarchyLevel
            .valueOf(OutOfTheBoxAppMapVertexHierarchyLevel.class, rs.getString(prefix + 12)));

        props.put(CommonAppMapProperties.Label, rs.getString(prefix + 14));
        vertex.setType(rs.getString(prefix + 16));
        AAppMapDate timestamp = new AAppMapDate(rs.getLong(prefix + 8) + offset);
        props.put(CommonAppMapProperties.CreationDate, timestamp);
        props.put(CommonAppMapProperties.LastReportedDate, timestamp);

        String agentName = rs.getString(prefix + 18);
        if (!rs.wasNull()) {
            props.put(OutOfTheBoxAppMapVertexProperty.AgentName, agentName);
            String hostName = rs.getString(prefix + 20);
            if (!rs.wasNull()) {
                props.put(OutOfTheBoxAppMapVertexProperty.HostName, hostName);
            }
            // String fullHostName = rs.getString(prefix + 22);
            // if (!rs.wasNull()) {
            // props.put(OutOfTheBoxAppMapVertexProperty.FullyQualifiedHostName, fullHostName);
            // }
            String processName = rs.getString(prefix + 24);
            if (!rs.wasNull()) {
                props.put(OutOfTheBoxAppMapVertexProperty.ProcessName, processName);
            } else {
                feedback.warn("Missing process name for " + agentName);
            }
        }
        if (owner != null) {
            props.put(OutOfTheBoxAppMapVertexProperty.agentSideOwner, owner);
        }

        return vertex;
    }

    private static <T> T deserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        try {
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
            @SuppressWarnings("unchecked")
            T props = (T) in.readObject();
            return props;
        } catch (Exception e) {
            return null;
        }
    }

    public static void main(String[] args) throws IOException {
        Path tmp = Paths.get("C:", "share");
        AppmapData ad = new AppmapData(tmp.toString());
        ad.slicesStartTime = 1446121020000L;
        ad.slicesEndTime = 1446135600000L;
        ad.importCsv(tmp);
    }
}
