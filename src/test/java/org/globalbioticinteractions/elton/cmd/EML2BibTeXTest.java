package org.globalbioticinteractions.elton.cmd;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.eol.globi.taxon.XmlUtil;
import org.eol.globi.util.DateUtil;
import org.hamcrest.core.Is;
import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.BibTeXFormatter;
import org.jbibtex.BibTeXParser;
import org.jbibtex.Key;
import org.jbibtex.KeyValue;
import org.jbibtex.StringValue;
import org.joda.time.DateTime;
import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;

public class EML2BibTeXTest {

    public static final Key ABSTRACT = new Key("abstract");
    public static final Key KEYWORDS = new Key("keywords");

    @Test
    public void transformDruckerToBibTex() throws TransformerException, URISyntaxException, IOException, XPathExpressionException, SAXException, ParserConfigurationException {
        InputStream is = getClass().getResourceAsStream("eml-drucker.xml");
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_MISC, new Key("carvalheiro2023"));

        populateDatasetCitation(is, entry);

        assertThat(entry.getField(BibTeXEntry.KEY_AUTHOR).toUserString(), Is.is("Debora Drucker"));
        assertThat(entry.getField(BibTeXEntry.KEY_TITLE).toUserString(), Is.is("Abundância e Distribuição de Ervas Terrestres em Parcelas Ripárias na Reserva Ducke: Variação Lateral"));
        assertThat(entry.getField(BibTeXEntry.KEY_PUBLISHER).toUserString(), Is.is("Instituto Nacional de Pesquisas da Amazônia – INPA"));
        assertThat(entry.getField(ABSTRACT).toUserString(), Is.is("Os dados aqui disponibilizados são produto do trabalho realizado por Debora Drucker durante seu curso de mestrado. O objetivo central foi investigar a abundância e distribuição espacial de ervas terrestres (apenas as espécies que germinam e passam todo o seu ciclo de vida no solo, sensu Poulsen (1996)) em 20 Parcelas ripárias paralelas aos igarapés na Reserva Florestal Adolpho Ducke. Referência: Poulsen, A. D. 1996. Species richness and density of ground herbs within a plot of lowland rainforest in north-west Borneo. Journal of Tropical Ecology 12: 177-190."));
        assertThat(entry.getField(KEYWORDS).toUserString(), Is.is("Ervas, Parcelas Ripárias, Reserva Ducke, Floresta de Terra Firme, PELD-PPBio"));
    }

    @Test
    public void transformUCSBIZCToBibTex() throws IOException, XPathExpressionException, SAXException, ParserConfigurationException {
        InputStream is = getClass().getResourceAsStream("eml-ucsb-izc.xml");
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_MISC, new Key("zip:hash://sha256/f5d8f67c1eca34cbba1abac12f353585c78bb053bc8ce7ee7e7a78846e1bfc4a!/eml.xml"));

        populateDatasetCitation(is, entry);

        assertThat(entry.getField(BibTeXEntry.KEY_AUTHOR).toUserString(), Is.is("Katja Seltmann"));
        assertThat(entry.getField(BibTeXEntry.KEY_TITLE).toUserString(), Is.is("University of California Santa Barbara Invertebrate Zoology Collection"));
        assertThat(entry.getField(BibTeXEntry.KEY_YEAR).toUserString(), Is.is("2023"));
        assertThat(entry.getField(BibTeXEntry.KEY_PUBLISHER).toUserString(), Is.is("Ecdysis Portal"));
        assertThat(entry.getField(ABSTRACT).toUserString(), Is.is("University of California Santa Barbara Invertebrate Zoology Collection, Cheadle Center for Biodiversity and Ecological Restoration. Contributions to data in this collection come from Elaine Tan (https://orcid.org/0000-0002-0504-4067), Rachel Behm (https://orcid.org/0000-0001-7264-3492) and Zach Brown. The data is archived at https://doi.org/10.5281/zenodo.5660088."));
        assertThat(entry.getField(BibTeXEntry.KEY_URL).toUserString(), Is.is("https://ecdysis.org/collections/misc/collprofiles.php?collid=38"));


        BibTeXDatabase database = new BibTeXDatabase();
        database.addObject(entry);

        assertThat(toBibTeXString(database), Is.is("@misc{zip:hash://sha256/f5d8f67c1eca34cbba1abac12f353585c78bb053bc8ce7ee7e7a78846e1bfc4a!/eml.xml,\n" +
                "\tauthor = {Katja Seltmann},\n" +
                "\tpublisher = {Ecdysis Portal},\n" +
                "\ttitle = {University of California Santa Barbara Invertebrate Zoology Collection},\n" +
                "\turl = {https://ecdysis.org/collections/misc/collprofiles.php?collid=38},\n" +
                "\tabstract = {University of California Santa Barbara Invertebrate Zoology Collection, Cheadle Center for Biodiversity and Ecological Restoration. Contributions to data in this collection come from Elaine Tan (https://orcid.org/0000-0002-0504-4067), Rachel Behm (https://orcid.org/0000-0001-7264-3492) and Zach Brown. The data is archived at https://doi.org/10.5281/zenodo.5660088.},\n" +
                "\tyear = 2023\n" +
                "}"));

    }

    @Test
    public void transformCarvalheiroToBibTex() throws IOException, XPathExpressionException, SAXException, ParserConfigurationException {
        InputStream is = getClass().getResourceAsStream("eml-carvalheiro.xml");
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_MISC, new Key("carvalheiro2023"));

        populateDatasetCitation(is, entry);

        assertThat(entry.getField(BibTeXEntry.KEY_AUTHOR).toUserString(), Is.is("Luisa Carvalheiro and José A. Salim"));
        assertThat(entry.getField(BibTeXEntry.KEY_TITLE).toUserString(), Is.is("WorldFAIR pilot data from: VisitationData_Luisa_Carvalheiro"));
        assertThat(entry.getField(BibTeXEntry.KEY_PUBLISHER).toUserString(), Is.is("University of Goias and University of Sao Paulo"));
        assertThat(entry.getField(ABSTRACT).toUserString(), Is.is("Note: This is an example of the data included in a large database of data on plant-flower visitor interactions, where datasets were only inlcuded when quantitative information on visitation rate is available at species level, and inofmration on flower anundance and sampling effort is available for each flowering species present in the study site."));
        assertThat(entry.getField(KEYWORDS).toUserString(), Is.is("plant-pollinator interactions, visitation data, worldfair"));
    }

    private void populateDatasetCitation(InputStream is, BibTeXEntry entry) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {
        Node dataset = (Node) XmlUtil.applyXPath(is, "//dataset", XPathConstants.NODE);
        addCreators(entry, dataset);
        addPublisher(entry, dataset);
        addTitle(entry, dataset);
        NodeList keywords = (NodeList) XmlUtil.applyXPath(dataset, "keywordSet/keyword", XPathConstants.NODESET);
        List<String> keywordList = new ArrayList<>();
        for (int i = 0; i < keywords.getLength(); i++) {
            Node item = keywords.item(i);
            String keywordString = StringUtils.trim(item.getTextContent());
            if (!keywordList.contains(keywordString)) {
                keywordList.add(keywordString);
            }
        }
        if (keywordList.size() > 0) {
            entry.addField(KEYWORDS, new StringValue(StringUtils.join(keywordList, ", "), StringValue.Style.BRACED));
        }

        addURL(entry, dataset);
        addAbstract(entry, dataset);
        addPubDate(entry, dataset);
    }

    private void addURL(BibTeXEntry entry, Node dataset) throws XPathExpressionException {
        String urlStringCandidate = (String) XmlUtil.applyXPath(dataset, "alternateIdentifier", XPathConstants.STRING);
        if (StringUtils.startsWith(urlStringCandidate, "http")) {
            try {
                new URI(urlStringCandidate);
                entry.addField(BibTeXEntry.KEY_URL, new StringValue(StringUtils.trim(urlStringCandidate), StringValue.Style.BRACED));
            } catch (URISyntaxException e) {
                //
            }
        }
    }

    private void addCreators(BibTeXEntry entry, Node dataset) throws XPathExpressionException {
        NodeList creators = (NodeList) XmlUtil.applyXPath(dataset, "creator", XPathConstants.NODESET);
        ArrayList<String> authorStrings = new ArrayList<>();
        appendAuthors(creators, authorStrings);
        NodeList associated = (NodeList) XmlUtil.applyXPath(dataset, "associatedParty", XPathConstants.NODESET);
        appendAuthors(associated, authorStrings);

        entry.addField(BibTeXEntry.KEY_AUTHOR, new StringValue(StringUtils.join(authorStrings, " and "), StringValue.Style.BRACED));
    }

    private void addPublisher(BibTeXEntry entry, Node dataset) throws XPathExpressionException {
        NodeList organizations = (NodeList) XmlUtil.applyXPath(dataset, "creator/organizationName", XPathConstants.NODESET);
        List<String> orgStrings = new ArrayList<>();
        for (int j = 0; j < organizations.getLength(); j++) {
            String organization = organizations.item(j).getTextContent();
            if (!orgStrings.contains(organization)) {
                orgStrings.add(organization);
            }

        }

        entry.addField(BibTeXEntry.KEY_PUBLISHER, new StringValue(StringUtils.join(orgStrings, " and "), StringValue.Style.BRACED));
    }

    private void addTitle(BibTeXEntry entry, Node dataset) throws XPathExpressionException {
        String title = (String) XmlUtil.applyXPath(dataset, "title", XPathConstants.STRING);
        entry.addField(BibTeXEntry.KEY_TITLE, new StringValue(StringUtils.trim(title), StringValue.Style.BRACED));
    }

    private void addAbstract(BibTeXEntry entry, Node dataset) throws XPathExpressionException {
        String title = (String) XmlUtil.applyXPath(dataset, "abstract//*", XPathConstants.STRING);
        String abstractString = StringUtils.replace(StringUtils.trim(title), "\n", " ");
        entry.addField(ABSTRACT, new StringValue(RegExUtils.replaceAll(abstractString, "[ ]+", " "), StringValue.Style.BRACED));
    }

    private void addPubDate(BibTeXEntry entry, Node dataset) throws XPathExpressionException {
        String pubDate = (String) XmlUtil.applyXPath(dataset, "pubDate", XPathConstants.STRING);
        try {
            DateTime dateTime = DateUtil.parseDateUTC(pubDate);
            if (dateTime != null) {
                entry.addField(BibTeXEntry.KEY_YEAR, new KeyValue(Integer.toString(dateTime.getYear())));
            }
        } catch (IllegalArgumentException ex) {
            // ignore
        }
    }

    private void appendAuthors(NodeList creators, List<String> authorStrings) throws XPathExpressionException {
        for (int j = 0; j < creators.getLength(); j++) {
            Node item = creators.item(j);

            NodeList firstNames = (NodeList) XmlUtil.applyXPath(item, "individualName/givenName", XPathConstants.NODESET);
            NodeList lastName = (NodeList) XmlUtil.applyXPath(item, "individualName/surName", XPathConstants.NODESET);

            List<String> creatorNames = new ArrayList<>();
            append(firstNames, creatorNames);
            append(lastName, creatorNames);
            if (creatorNames.size() > 0) {
                String authorString = StringUtils.join(creatorNames, " ");
                if (!authorStrings.contains(authorString)) {
                    authorStrings.add(authorString);
                }
            }
        }
    }

    private void append(NodeList firstNames, List<String> firstNameStrings) {
        for (int i = 0; i < firstNames.getLength(); i++) {
            String textContent = firstNames.item(i).getTextContent();
            if (StringUtils.isNotBlank(textContent)) {
                firstNameStrings.add(textContent);
            }
        }
    }

    @Test
    public void createBibTex() throws Throwable {

        String bibTex = "@article{CitekeyArticle,\n" +
                "\tauthor = \"P. J. Cohen\",\n" +
                "\ttitle = \"The independence of the continuum hypothesis\",\n" +
                "\tjournal = \"Proceedings of the National Academy of Sciences\",\n" +
                "\tyear = 1963,\n" +
                "\tvolume = \"50\",\n" +
                "\tnumber = \"6\",\n" +
                "\tpages = \"1143--1148\"\n" +
                "}";
        InputStream stream = IOUtils.toInputStream(bibTex, StandardCharsets.UTF_8);
        BibTeXParser parser = new BibTeXParser();

        BibTeXDatabase database = parser.parse(new InputStreamReader(stream, StandardCharsets.UTF_8));

        Map<Key, BibTeXEntry> entries = database.getEntries();
        assertThat(entries.size(), Is.is(1));

        BibTeXEntry bibTeXEntry = entries.values().stream().findFirst().orElseThrow(new Supplier<Throwable>() {
            @Override
            public Throwable get() {
                return new RuntimeException("kaboom");
            }
        });

        assertThat(bibTeXEntry.getField(BibTeXEntry.KEY_JOURNAL).toUserString(), Is.is("Proceedings of the National Academy of Sciences"));

        String bibTeX = toBibTeXString(database);
        assertThat(bibTeX, Is.is(bibTex));
    }

    private static String toBibTeXString(BibTeXDatabase database) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        BibTeXFormatter bibTeXFormatter = new BibTeXFormatter();
        bibTeXFormatter.format(database, new OutputStreamWriter(os, StandardCharsets.UTF_8));
        return new String(os.toByteArray(), StandardCharsets.UTF_8);
    }

}