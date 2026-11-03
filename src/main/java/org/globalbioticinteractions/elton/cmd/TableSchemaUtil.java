package org.globalbioticinteractions.elton.cmd;

import com.Ostermiller.util.LabeledCSVParser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eol.globi.util.CSVTSVUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TableSchemaUtil {
    public static List<String> inferColumnNames(List<String> firstTwoLines) throws IOException {
        return inferColumnNames(firstTwoLines, null);
    }

    static List<String> inferColumnNames(List<String> firstTwoLines, Character delimiter) throws IOException {
        return inferColumnNames(
                IOUtils.toInputStream(StringUtils.join(firstTwoLines, "\n"), StandardCharsets.UTF_8),
                delimiter
        );
    }

    static List<String> inferColumnNames(InputStream inputStream, Character delimiter) throws IOException {
        List<String> columnNames;
        try {
            LabeledCSVParser csvParser = new LabeledCSVParser(CSVTSVUtil.createCSVParser(inputStream));
            if (delimiter != null) {
                csvParser.changeDelimiter(delimiter);
            }
            columnNames = csvParser.getLabels().length > 1
                    ? new ArrayList<>(Arrays.asList(csvParser.getLabels()))
                    : Collections.emptyList();
        } catch (IOException ex) {
            columnNames = Collections.emptyList();
        }
        return columnNames;
    }
}
