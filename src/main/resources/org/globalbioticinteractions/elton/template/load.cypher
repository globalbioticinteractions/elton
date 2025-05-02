// verbatim taxa
CREATE CONSTRAINT taxonVerbatim IF NOT EXISTS FOR (t:TaxonVerbatim) REQUIRE (t.name, t.path, t.namespace) IS UNIQUE;
CREATE CONSTRAINT interaction IF NOT EXISTS FOR ()-[r:INTERACTS_WITH]-() REQUIRE (r.origin, r.namespace) IS UNIQUE;
CREATE CONSTRAINT dataset IF NOT EXISTS FOR (d:Dataset) REQUIRE (d.citation, d.namespace) IS UNIQUE;
CREATE CONSTRAINT study IF NOT EXISTS FOR (s:Study) REQUIRE (s.citation, s.namespace) IS UNIQUE;

LOAD CSV WITH HEADERS FROM '{{ ENDPOINT }}/indexed-interactions.csv.gz' AS row
CALL (row) {
        MERGE (sourceTaxon:TaxonVerbatim {
          name: coalesce(row.sourceTaxonName, ""),
          path: coalesce(row.sourceTaxonPath, ""),
          namespace: coalesce(row.namespace, "")
        })
        CREATE (sourceSpecimen:Specimen {
        })
        MERGE (targetTaxon:TaxonVerbatim {
          name: coalesce(row.targetTaxonName, ""),
          path: coalesce(row.targetTaxonPath, ""),
          namespace: coalesce(row.namespace, "")
        })
        CREATE (targetSpecimen:Specimen {
        })
        MERGE (study:Study {
          citation: coalesce(row.referenceCitation,""),
          doi: coalesce(row.referenceDoi,""),
          url: coalesce(row.referenceUrl,""),
          namespace: coalesce(row.namespace, "")
        })
        MERGE (dataset:Dataset {
          citation: coalesce(row.citation,""),
          lastSeenAt: row.lastSeenAt,
          archiveURI: row.archiveURI,
          namespace: coalesce(row.namespace, "")
        })
        MERGE (sourceTaxon)<-[:ORIGINALLY_DESCRIBED_AS]-(sourceSpecimen)
        MERGE (targetTaxon)<-[:ORIGINALLY_DESCRIBED_AS]-(targetSpecimen)
        MERGE (sourceSpecimen)<-[:COLLECTED]-(study)
        MERGE (targetSpecimen)<-[:COLLECTED]-(study)
        MERGE (study)-[:IN_DATASET]->(dataset)
        MERGE (sourceSpecimen)-[r:INTERACTS_WITH { id: row.interactionTypeId, name: row.interactionTypeName, origin: ("line:" + file() + "!/L" + linenumber()), namespace: row.namespace }] -> (targetSpecimen)
} IN TRANSACTIONS OF 10000 ROWS;

// resolved taxa
CREATE CONSTRAINT taxon IF NOT EXISTS FOR (t:Taxon) REQUIRE (t.id, t.catalog) IS UNIQUE;
LOAD CSV WITH HEADERS FROM '{{ ENDPOINT }}/indexed-names-resolved-col.csv.gz' AS row
CALL (row) {
        MATCH (verbatim:TaxonVerbatim {
          name: coalesce(row.providedName, ""),
          path: coalesce(row.providedCol4, ""),
          namespace: coalesce(row.providedCol6, "")
        })
        WHERE
          (row.relationName <> "NONE")
        MERGE
          (resolved:Taxon {
            catalog: row.resolvedCatalogName,
            id: row.resolvedExternalId,
            name: coalesce(row.resolvedName, ""),
            rank: coalesce(row.resolvedRank, ""),
            authorship: coalesce(row.taxonPath, ""),
            commonNames: coalesce(row.commonNames, ""),
            externalUrl: coalesce(row.resolvedExternalUrl, ""),
            path: coalesce(row.resolvedPath, ""),
            pathNames: coalesce(row.resolvedPathIds, ""),
            pathAuthorships: coalesce(row.resolvedPathAuthorships, ""),
            pathIds: coalesce(row.resolvedPathIds, "")})
        MERGE
          (verbatim)-[r:RESOLVED_TO]->(resolved)
} IN TRANSACTIONS OF 1000 ROWS;

// full text indexes
CREATE FULLTEXT INDEX taxonSearch IF NOT EXISTS FOR (n:Taxon|TaxonVerbatim) ON EACH [n.path, n.pathIds, n.commonNames, n.name, n.id, n.pathAuthorships, n.authorship];

CREATE FULLTEXT INDEX citationSearch IF NOT EXISTS FOR (n:Study|Dataset) ON EACH [n.citation];