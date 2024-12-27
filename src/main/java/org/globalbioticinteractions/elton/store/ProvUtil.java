package org.globalbioticinteractions.elton.store;

import bio.guoda.preston.HashType;
import bio.guoda.preston.RefNodeConstants;
import bio.guoda.preston.RefNodeFactory;
import bio.guoda.preston.process.StatementsEmitter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static bio.guoda.preston.RefNodeConstants.GENERATED_AT_TIME;
import static bio.guoda.preston.RefNodeConstants.HAS_VERSION;
import static bio.guoda.preston.RefNodeConstants.IS_A;
import static bio.guoda.preston.RefNodeConstants.WAS_GENERATED_BY;
import static bio.guoda.preston.RefNodeFactory.toIRI;
import static bio.guoda.preston.RefNodeFactory.toStatement;

public class ProvUtil {
    public static void emitDataGenerationActivity(
            List<IRI> dependencies,
            IRI versionSource,
            BlankNodeOrIRI newVersion,
            StatementsEmitter emitter,
            Optional<BlankNodeOrIRI> sourceActivity
    ) {
        Literal nowLiteral = RefNodeFactory.nowDateTimeLiteral();

        IRI downloadActivity = toIRI(UUID.randomUUID());
        emitter.emit(toStatement(
                downloadActivity,
                newVersion,
                WAS_GENERATED_BY,
                downloadActivity));
        emitter.emit(toStatement(
                downloadActivity,
                newVersion,
                RefNodeConstants.QUALIFIED_GENERATION,
                downloadActivity));
        emitter.emit(toStatement(
                downloadActivity,
                downloadActivity,
                GENERATED_AT_TIME,
                nowLiteral));
        emitter.emit(toStatement(
                downloadActivity,
                downloadActivity,
                IS_A,
                RefNodeConstants.GENERATION));
        sourceActivity.ifPresent(blankNodeOrIRI -> emitter.emit(toStatement(
                downloadActivity,
                downloadActivity,
                RefNodeConstants.WAS_INFORMED_BY,
                blankNodeOrIRI)));
        for (IRI dependency : dependencies) {
            emitter.emit(toStatement(
                    downloadActivity,
                    downloadActivity,
                    RefNodeConstants.USED,
                    dependency)
            );
        }
        emitter.emit(toStatement(downloadActivity, versionSource, HAS_VERSION, newVersion));
        for (IRI dependency : dependencies) {
            if (!dependency.equals(newVersion)) {
                emitter.emit(toStatement(
                        downloadActivity,
                        newVersion,
                        RefNodeConstants.WAS_DERIVED_FROM,
                        dependency));
            }
        }
    }

    public static void saveGeneratedContentIfNeeded(File tmpSourceFile, IRI iri, String dataDir) throws IOException {
        File destFile = new File(dataDir, StringUtils.replace(iri.getIRIString(), HashType.sha256.getPrefix(), ""));

        if (destFile.exists()) {
            FileUtils.delete(tmpSourceFile);
        } else {
            FileUtils.moveFile(
                    tmpSourceFile,
                    destFile
            );
        }
    }
}
