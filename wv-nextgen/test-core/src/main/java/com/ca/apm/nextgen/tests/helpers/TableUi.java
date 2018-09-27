package com.ca.apm.nextgen.tests.helpers;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author haiva01
 */
public class TableUi implements Iterable<TableUi.TableRowRecord> {
    private static final Logger log = LoggerFactory.getLogger(TableUi.class);

    private WebViewUi ui;

    private List<WebElement> headerElems;
    private Map<String, Integer> headerTextToIndex;

    private List<WebElement> rowsElements;
    private List<List<String>> values;
    private List<List<WebElement>> valuesElements;

    /**
     * @param ui            {@link WebViewUi} instance
     * @param tableSelector e.g., By.id("webview-consolelens-table-grid")
     */
    public TableUi(WebViewUi ui, By tableSelector) {
        this.ui = ui;
        reparse(null, tableSelector);
    }

    /**
     * @param ui                 {@link WebViewUi} instance
     * @param tableSearchContext table root search context
     * @param tableSelector      e.g., By.id("webview-consolelens-table-grid")
     */
    public TableUi(WebViewUi ui, SearchContext tableSearchContext, By tableSelector) {
        this.ui = ui;
        reparse(tableSearchContext, tableSelector);
    }

    /**
     * 
     * Constructor which parses the table header and values independently from the provided 
     * elements.
     * 
     * @param ui              {@link WebViewUi} instance
     * @param headerTable     header element
     * @param valuesTable     values element
     */
    public TableUi(WebViewUi ui, WebElement headerTable, WebElement valuesTable) {
        this.ui = ui;
        parse(headerTable, valuesTable);
    }
    
    public void reparse(SearchContext tableSearchContext, By tableSelector) {
        headerElems = null;
        headerTextToIndex = null;
        rowsElements = null;
        values = null;
        valuesElements = null;

        WebElement tableRoot = tableSearchContext != null ? 
            ui.getWebElement(tableSearchContext, tableSelector) : ui.getWebElement(tableSelector);

        parse(tableRoot);
    }

    private void parse(WebElement tableRoot) {
        // Get present table elements. We assume that there are exactly two <table> elements. One
        // contains headers of the table. The other contains table values.
        List<WebElement> tables = ui.getWebElements(tableRoot, By.xpath(".//table"));
        Assert.assertEquals(tables.size(), 2);

        final WebElement headerTable = tables.get(0);
        final WebElement valuesTable = tables.get(1);
        parse(headerTable, valuesTable);
    }

    private void parse(WebElement headerTable, WebElement valuesTable) {
        // Parse header and create mappings for header elements.
        headerElems
            = ui.getWebElements(headerTable, By.cssSelector("div.webview-Common-Column-header"));
        headerTextToIndex = new LinkedHashMap<>(headerElems.size());
        int index = 0;
        for (final WebElement element : headerElems) {
            final String headerText = element.getText();
            log.debug("Header '{}' at position {}.", headerText, index);
            if (headerTextToIndex.containsKey(headerText)) {
                log.warn("Duplicate header '{}'. Keeping previous header mapping.", headerText);
            } else {
                headerTextToIndex.put(headerText, index);
            }
            ++index;
        }

        // Get all rows of the values table.

        // We use position()=2 here because there is some sort of dummy tbody before the tbody
        // that has all the values.
        WebElement tbody = ui.getWebElement(valuesTable, By.xpath("./tbody[position()=2]"));
        rowsElements = ui.getWebElements(tbody, By.xpath("./tr"));
        log.debug("Found {} rows in table.", rowsElements.size());
        values = new ArrayList<>(rowsElements.size());
        valuesElements = new ArrayList<>(rowsElements.size());
        for (WebElement rowElement : rowsElements) {
            List<WebElement> rowValuesElements
                = Collections.unmodifiableList(ui.getWebElements(rowElement, By.xpath("./td")));
            valuesElements.add(rowValuesElements);

            List<String> valuesList = new ArrayList<>(headerElems.size());
            for (WebElement valueElement : rowValuesElements) {
                String elemText = valueElement.getText();
                log.trace("element value: >{}<", elemText);
                valuesList.add(elemText);
            }
            valuesList = Collections.unmodifiableList(valuesList);
            values.add(valuesList);
        }

        // Wrap in unmodifiable list.
        values = Collections.unmodifiableList(values);
        valuesElements = Collections.unmodifiableList(valuesElements);
    
    }
    
    public WebElement getCellElement(int row, int column) {
        return valuesElements.get(row).get(column);
    }

    public String getCellValue(int row, int column) {
        return values.get(row).get(column);
    }

    public List<String> getRowValues(int row) {
        return values.get(row);
    }

    public WebElement getRowElement(int row) {
        return rowsElements.get(row);
    }

    public List<WebElement> getRowValuesElements(int row) {
        return valuesElements.get(row);
    }

    public int rowCount() {
        return values.size();
    }

    public WebViewUi getUi() {
        return ui;
    }

    public Map<String, Integer> getHeaderTextToIndexMap() {
        return Collections.unmodifiableMap(headerTextToIndex);
    }

    public Map<Integer, String> getIndexToHeaderTextMap() {
        Map<Integer, String> indexToHeaderTextMap = new HashMap<Integer, String>(
            headerTextToIndex.size());
        for (Entry<String, Integer> entry : headerTextToIndex.entrySet()) {
            indexToHeaderTextMap.put(entry.getValue(), entry.getKey());
        }
        return Collections.unmodifiableMap(indexToHeaderTextMap);
    }

    /**
     * Returns an iterator over a set of elements of type TableRowRecord.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<TableRowRecord> iterator() {
        return new TableUiIterator();
    }

    /**
     * This class is type of value of {@link TableUi}'s iterator.
     */
    public static class TableRowRecord {
        /**
         * row {@link WebElement}
         */
        protected WebElement rowElement;
        /**
         * row values as strings
         */
        protected List<String> values;
        /**
         * row values as {@link WebElement}
         */
        protected List<WebElement> valuesElements;

        protected TableRowRecord(WebElement rowElement, List<String> values,
            List<WebElement> valuesElements) {
            this.rowElement = rowElement;
            this.values = values;
            this.valuesElements = valuesElements;
        }

        public WebElement getRowElement() {
            return rowElement;
        }

        public List<String> getValues() {
            return values;
        }

        public List<WebElement> getValuesElements() {
            return valuesElements;
        }
    }

    /**
     * This class is what {@link TableUi#iterator()} returns.
     */
    protected class TableUiIterator implements Iterator<TableRowRecord> {
        protected int index = 0;

        /**
         * Returns {@code true} if the iteration has more elements.
         * (In other words, returns {@code true} if {@link #next} would
         * return an element rather than throwing an exception.)
         *
         * @return {@code true} if the iteration has more elements
         */
        @Override
        public boolean hasNext() {
            final int rows = rowCount();
            return index != rows;
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration
         * @throws java.util.NoSuchElementException if the iteration has no more elements
         */
        @Override
        public TableRowRecord next() throws java.util.NoSuchElementException {
            if (!hasNext()) {
                throw new java.util.NoSuchElementException("No more values in this table.");
            }

            TableRowRecord record = new TableRowRecord(getRowElement(index), getRowValues(index),
                getRowValuesElements(index));
            ++index;
            return record;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException(
                "Removing elements is not supported.");
        }
    }
}
