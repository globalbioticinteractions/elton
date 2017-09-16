package org.globalbioticinteractions.elton.cmd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eol.globi.service.DatasetFinder;
import org.eol.globi.service.DatasetFinderException;

import java.util.ArrayList;
import java.util.List;

class CmdUtil {
    private static final Log LOG = LogFactory.getLog(CmdUtil.class);

    static void handleNamespaces(DatasetFinder finder, NamespaceHandler handler, List<String> namespaces) throws DatasetFinderException {
        List<String> selectedNamespaces = new ArrayList<>(namespaces);
        if (selectedNamespaces.isEmpty()) {
            selectedNamespaces = new ArrayList<>(finder.findNamespaces());
        }

        for (String namespace : selectedNamespaces) {
            try {
                handler.onNamespace(namespace);
            } catch (Exception e) {
                LOG.error("failed to handle namespace [" + namespace + "]", e);
            }
        }
    }
}
