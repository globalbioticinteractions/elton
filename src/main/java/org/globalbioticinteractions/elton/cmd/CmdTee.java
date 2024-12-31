package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.HashType;
import bio.guoda.preston.store.BlobStoreAppendOnly;
import bio.guoda.preston.store.KeyTo3LevelPath;
import bio.guoda.preston.store.KeyValueStoreLocalFileSystem;
import bio.guoda.preston.store.ValidatingKeyValueStreamContentAddressedFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.lang.StringUtils;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CommandLine.Command(
        name = "tee",
        aliases = {"cp", "carbon-copy"},
        description = CmdTee.DESCRIPTION
)
public class CmdTee extends CmdDefaultParams {

    public static final String DESCRIPTION = "Copy referenced resources into specified destination directory. Compatible with Preston defaults.";

    @Override
    public void doRun() {
        run(getStdout());
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @CommandLine.Option(names = {"--dest-dir"},
            description = "Destination directory for copied (tee-ied) resources (default: ${DEFAULT-VALUE})"
    )
    private File destDir = new File("./data");

    void run(PrintStream out) {


        BlobStoreAppendOnly blobStore = new BlobStoreAppendOnly(
                new KeyValueStoreLocalFileSystem(
                        new File(getWorkDir()),
                        new KeyTo3LevelPath(destDir.toURI()),
                        new ValidatingKeyValueStreamContentAddressedFactory()
                ),
                true,
                HashType.sha256
        );

        Consumer<String> contentCopier = new ContentCopier(blobStore, getDataDir(), this.getStderr());

        InputStream stdin = getStdin();
        contentDetector(out, contentCopier, stdin);

    }

    public static void contentDetector(PrintStream out, Consumer<String> contentCopier, InputStream stdin) {
        InputStream proxy = new TeeInputStream(stdin, out);

        BufferedReader reader = IOUtils.buffer(new InputStreamReader(proxy, StandardCharsets.UTF_8));
        try {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] hashCandidates = StringUtils.splitByWholeSeparatorPreserveAllTokens(line, "hash://");
                Pattern iriPattern = HashType.sha256.getIRIPattern();
                for (String hashCandidate : hashCandidates) {
                    String candidate = StringUtils.substring("hash://" + hashCandidate, 0, HashType.sha256.getIriStringLength());
                    Matcher matcher = iriPattern.matcher(candidate);

                    if (matcher.matches()) {
                        String filename = matcher.group("contentId");
                        contentCopier.accept(filename);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("failed to read from stdin", e);
        }
    }

    public void setDestDir(File destDir) {
        this.destDir = destDir;
    }

}


