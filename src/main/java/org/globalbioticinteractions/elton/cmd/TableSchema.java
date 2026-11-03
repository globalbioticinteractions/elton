package org.globalbioticinteractions.elton.cmd;

import java.util.List;

public class TableSchema {
    private String urlString;
    private String citation;
    private List<String> firstTwoLines;
    private List<String> columnNames;
    private String delimiter;

    public void setUrlString(String urlString) {
        this.urlString = urlString;
    }

    public String getUrlString() {
        return urlString;
    }

    public void setCitation(String citation) {
        this.citation = citation;
    }

    public String getCitation() {
        return citation;
    }

    public void setFirstTwoLines(List<String> firstTwoLines) {
        this.firstTwoLines = firstTwoLines;
    }

    public List<String> getFirstTwoLines() {
        return firstTwoLines;
    }

    public void setColumnNames(List<String> columnNames) {
        
        this.columnNames = columnNames;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public String getDelimiter() {
        return delimiter;
    }
}
