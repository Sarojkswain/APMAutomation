/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 *
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.automation.utils;

import com.wily.introscope.jdbc.IntroscopeDriver;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Class used for retrieving traces (and potentially metrics) from an APM EM database through JDBC.
 *
 * <p>Note: I plan to get this merged into the TAS codebase so this instance will be removed once
 * that happens.
 */
public class ApmJdbc implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(ApmJdbc.class);
    private static final DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private Connection connection;

    /**
     * Constructor.
     *
     * @param host EM Host.
     * @param port EM Port.
     * @param username Username for authentication.
     * @param password Password for authentication.
     * @throws SQLException if the connection fails.
     */
    public ApmJdbc(String host, int port, String username, String password) throws SQLException {
        Validate.notNull(username);
        Validate.notNull(password);
        Validate.notNull(host);
        Validate.inclusiveBetween(1, 0xffff, port);

        final String uri = MessageFormat.format("jdbc:introscope:net//{0}:{1}@{2}:{3}",
            username, password, host, String.valueOf(port));

        logger.info("Connecting to {}", uri);
        connection = new IntroscopeDriver().connect(uri, null);
    }

    /**
     * Closes the connection if still active.
     *
     * @throws SQLException See {@link Connection#close()}.
     */
    @Override
    public void close() throws SQLException {
        if (connection != null) {
            connection.close(); // no-op if already closed/failed
            connection = null;
        }
    }

    /**
     * Overload of {@link #getTraces(Date, Date, String, int, boolean)} that returns full traces.
     *
     * @see #getTraces(Date, Date, String, int, boolean)
     */
    public Collection<Document> getTraces(Date startTime, Date endTime, String query, int limit) {
        return getTraces(startTime, endTime, query, limit, false);
    }

    /**
     * Retrieves traces matching input criteria.
     *
     * @param startTime Start of time period to search in.
     * @param endTime End of time period to search in.
     * @param query Lucene query string
     * @param limit Limit on the number of returned traces. If set to {@code 0} no limit is applied.
     * @param headersOnly If {@code true} only trace headers will be retrieved.
     * @return Collection of transaction trace documents.
     */
    public Collection<Document> getTraces(Date startTime, Date endTime, String query, int limit,
        boolean headersOnly) {
        final String view = headersOnly ? "trace_headers" : "traces";
        final String limitSpec = limit > 0 ? " limit " + limit : "";


        return getTraceDocuments("select * from " + view + " where timestamp between '"
            + dateFormatter.format(startTime) + "' and '" + dateFormatter.format(endTime)
            + "' and query='" + query + "'" + limitSpec);
    }

    private Collection<Document> getTraceDocuments(String query) {
        Validate.notBlank(query);

        Collection<Document> documents = new ArrayList<>();
        logger.debug("Execuring query: {}", query);

        try (Statement statement = connection.createStatement()) {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            ResultSet results = statement.executeQuery(query);

            ResultSetMetaData metadata = results.getMetaData();
            Map<String, Integer> columns = new HashMap<>();
            for (int i = 1; i <= metadata.getColumnCount(); ++i) {
                columns.put(metadata.getColumnName(i), i);
            }

            assert columns.containsKey("Trace");

            while (results.next()) {
                // TODO: This could be improved from having the charset hardcoded to read it from
                // the XML header
                documents.add(builder.parse(new ByteArrayInputStream(
                    results.getString(columns.get("Trace")).getBytes(StandardCharsets.UTF_8))));
            }
        } catch (SQLException | ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }

        return documents;
    }
}
