package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.RefNodeFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.rdf.api.IRI;

import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.dataset.DatasetRegistryException;
import org.globalbioticinteractions.elton.util.DatasetRegistryUtil;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static bio.guoda.preston.RefNodeConstants.HAS_VERSION;

@CommandLine.Command(
        name = "cat",
        aliases = {"get"},
        description = CmdGet.DESCRIPTION
)
public class CmdGet extends CmdDefaultParams {

    private static final Pattern PATTERN_OBJECT_NEWER = Pattern.compile(".* (" + HAS_VERSION.toString() + ") <(?<obj>[^>]*)>(.*) [.]$");
    static final String DESCRIPTION = "gets resource by hash uri";

    @Override
    public void doRun() {
        run(getStdout());
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    void run(PrintStream out) {
        DatasetRegistry registry = DatasetRegistryUtil.forCacheOrLocalDir(
                getDataDir(),
                getProvDir(),
                getWorkDir(),
                createInputStreamFactory(),
                getContentPathFactory(),
                getProvenancePathFactory(),
                getActivityListener(DatasetRegistryUtil.NAMESPACE_LOCAL)
        );

        final List<String> actualNamespaces = new ArrayList<>();
        try {
            CmdUtil.handleNamespaces(registry, actualNamespaces::add, getNamespaces());
        } catch (DatasetRegistryException e) {
            throw new RuntimeException("failed to handle namespaces", e);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(getStdin()));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                Matcher matcher = PATTERN_OBJECT_NEWER.matcher(line);
                if (matcher.matches()) {
                    IRI contentId = RefNodeFactory.toIRI(matcher.group("obj"));
                    File file = null;
                    for (String namespace : actualNamespaces) {
                        File namespaceDir = new File(getDataDir(), namespace);
                        File fileCandidate = new File(namespaceDir, StringUtils.substring(contentId.getIRIString(), "hash://sha256/".length()));
                        if (fileCandidate.exists()) {
                            file = fileCandidate;
                            break;
                        }
                    }
                    if (file == null) {
                        throw new RuntimeException("cannot resolve [" + contentId + "]");
                    }
                    IOUtils.copy(new FileInputStream(file), out);
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("failed to read from stdin", ex);
        }
    }

}


