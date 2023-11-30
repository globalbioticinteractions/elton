package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.HashType;
import bio.guoda.preston.Hasher;
import bio.guoda.preston.RefNodeConstants;
import bio.guoda.preston.RefNodeFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Quad;
import org.eol.globi.data.NodeFactory;
import org.eol.globi.service.CacheService;
import org.eol.globi.service.ResourceService;
import org.eol.globi.tool.NullImportLogger;
import org.globalbioticinteractions.dataset.Dataset;
import org.globalbioticinteractions.dataset.DatasetProxy;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.dataset.DatasetRegistryException;
import org.globalbioticinteractions.dataset.DatasetRegistryProxy;
import org.globalbioticinteractions.elton.util.DatasetRegistryUtil;
import org.globalbioticinteractions.elton.util.NamespaceHandler;
import org.globalbioticinteractions.elton.util.NodeFactoryNull;
import org.nanopub.Run;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URI;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static bio.guoda.preston.RefNodeConstants.HAS_VERSION;

@CommandLine.Command(
        name = "cat",
        aliases = {"get"},
        description = "gets resource by hash uri"
)
public class CmdGet extends CmdDefaultParams {

    private static final Pattern PATTERN_OBJECT_NEWER = Pattern.compile(".* (" + HAS_VERSION.toString() + ") <(?<obj>[^>]*)>(.*) [.]$");

    @Override
    public void run() {
        run(getStdout());
    }

    void run(PrintStream out) {
        DatasetRegistry registry = DatasetRegistryUtil.forCacheDirOrLocalDir(
                getCacheDir(),
                getWorkDir(),
                createInputStreamFactory()
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
                        File namespaceDir = new File(getCacheDir(), namespace);
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


