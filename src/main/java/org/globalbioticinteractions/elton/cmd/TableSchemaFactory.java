package org.globalbioticinteractions.elton.cmd;

import java.io.IOException;
import java.util.List;

import static org.globalbioticinteractions.elton.cmd.TableSchemaUtil.inferColumnNames;
import static org.globalbioticinteractions.elton.cmd.CmdInit.inferColumnNamesTSV;

public class TableSchemaFactory {
    public TableSchema create(String urlString, String citation, List<String> firstTwoLines) throws IOException {

        TableSchema tableSchema = new TableSchema();
        tableSchema.setUrlString(urlString);
        tableSchema.setCitation(citation);
        tableSchema.setFirstTwoLines(firstTwoLines);

        List<String> columnNames = inferColumnNames(firstTwoLines);
        tableSchema.setColumnNames(columnNames);
        if (!columnNames.isEmpty()) {
            tableSchema.setDelimiter(",");
            tableSchema.setColumnNames(columnNames);
        } else {
            columnNames = inferColumnNamesTSV(firstTwoLines);
            if (!columnNames.isEmpty()) {
                tableSchema.setColumnNames(columnNames);
                tableSchema.setDelimiter("\t");
            } else {
                columnNames = inferColumnNames(firstTwoLines, ';');
                tableSchema.setColumnNames(columnNames);
                tableSchema.setDelimiter(";");
            }

        }


        return tableSchema;
    }
}
