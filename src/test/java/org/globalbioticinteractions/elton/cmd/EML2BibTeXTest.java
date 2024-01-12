package org.globalbioticinteractions.elton.cmd;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.eol.globi.taxon.XmlUtil;
import org.hamcrest.core.Is;
import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.BibTeXFormatter;
import org.jbibtex.BibTeXParser;
import org.jbibtex.Key;
import org.jbibtex.KeyValue;
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
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;

public class EML2BibTeXTest {

    public static final Key ABSTRACT = new Key("abstract");

    @Test
    public void transformDruckerToBibTex() throws TransformerException, URISyntaxException, IOException, XPathExpressionException, SAXException, ParserConfigurationException {
        InputStream is = getClass().getResourceAsStream("eml-drucker.xml");
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_MISC, new Key("carvalheiro2023"));

        populateDatasetCitation(is, entry);

        assertThat(entry.getField(BibTeXEntry.KEY_AUTHOR).toUserString(), Is.is("Debora Drucker"));
        assertThat(entry.getField(BibTeXEntry.KEY_TITLE).toUserString(), Is.is("Abundância e Distribuição de Ervas Terrestres em Parcelas Ripárias na Reserva Ducke: Variação Lateral"));
        assertThat(entry.getField(ABSTRACT).toUserString(), Is.is("Os dados aqui disponibilizados são produto do trabalho realizado por Debora Drucker durante seu curso de mestrado. O objetivo central foi investigar a abundância e distribuição espacial de ervas terrestres (apenas as espécies que germinam e passam todo o seu ciclo de vida no solo, sensu Poulsen (1996)) em 20 Parcelas ripárias paralelas aos igarapés na Reserva Florestal Adolpho Ducke. Referência: Poulsen, A. D. 1996. Species richness and density of ground herbs within a plot of lowland rainforest in north-west Borneo. Journal of Tropical Ecology 12: 177-190."));
    }

    @Test
    public void transformCarvalheiroToBibTex() throws IOException, XPathExpressionException, SAXException, ParserConfigurationException {
        InputStream is = getClass().getResourceAsStream("eml-carvalheiro.xml");
        BibTeXEntry entry = new BibTeXEntry(BibTeXEntry.TYPE_MISC, new Key("carvalheiro2023"));

        populateDatasetCitation(is, entry);

        assertThat(entry.getField(BibTeXEntry.KEY_AUTHOR).toUserString(), Is.is("Luisa Carvalheiro and José A. Salim"));
        assertThat(entry.getField(BibTeXEntry.KEY_TITLE).toUserString(), Is.is("WorldFAIR pilot data from: VisitationData_Luisa_Carvalheiro"));
        assertThat(entry.getField(ABSTRACT).toUserString(), Is.is("Note: This is an example of the data included in a large database of data on plant-flower visitor interactions, where datasets were only inlcuded when quantitative information on visitation rate is available at species level, and inofmration on flower anundance and sampling effort is available for each flowering species present in the study site."));
    }

    private void populateDatasetCitation(InputStream is, BibTeXEntry entry) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {
        Node dataset = (Node) XmlUtil.applyXPath(is, "//dataset", XPathConstants.NODE);
        addCreators(entry, dataset);
        addTitle(entry, dataset);
        addAbstract(entry, dataset);
    }

    private void addCreators(BibTeXEntry entry, Node dataset) throws XPathExpressionException {
        NodeList creators = (NodeList) XmlUtil.applyXPath(dataset, "creator", XPathConstants.NODESET);
        String author = parseAuthor(creators);
        entry.addField(BibTeXEntry.KEY_AUTHOR, new KeyValue(author));
    }

    private void addTitle(BibTeXEntry entry, Node dataset) throws XPathExpressionException {
        String title = (String) XmlUtil.applyXPath(dataset, "title", XPathConstants.STRING);
        entry.addField(BibTeXEntry.KEY_TITLE, new KeyValue(StringUtils.trim(title)));
    }

    private void addAbstract(BibTeXEntry entry, Node dataset) throws XPathExpressionException {
        String title = (String) XmlUtil.applyXPath(dataset, "abstract//*", XPathConstants.STRING);
        String abstractString = StringUtils.replace(StringUtils.trim(title), "\n", " ");
        entry.addField(ABSTRACT, new KeyValue(RegExUtils.replaceAll(abstractString, "[ ]+", " ")));
    }

    private String parseAuthor(NodeList creators) throws XPathExpressionException {
        List<String> authorStrings = new ArrayList<>();
        for (int j = 0; j < creators.getLength(); j++) {
            Node item = creators.item(j);

            NodeList firstNames = (NodeList) XmlUtil.applyXPath(item, "individualName/givenName", XPathConstants.NODESET);
            NodeList lastName = (NodeList) XmlUtil.applyXPath(item, "individualName/surName", XPathConstants.NODESET);

            List<String> creatorNames = new ArrayList<>();
            append(firstNames, creatorNames);
            append(lastName, creatorNames);
            authorStrings.add(StringUtils.join(creatorNames, " "));
        }

        return StringUtils.join(authorStrings, " and ");
    }

    private void append(NodeList firstNames, List<String> firstNameStrings) {
        for (int i = 0; i < firstNames.getLength(); i++) {
            firstNameStrings.add(firstNames.item(i).getTextContent());
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

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        BibTeXFormatter bibTeXFormatter = new BibTeXFormatter();
        bibTeXFormatter.format(database, new OutputStreamWriter(os, StandardCharsets.UTF_8));

        assertThat(new String(os.toByteArray(), StandardCharsets.UTF_8), Is.is(bibTex));
    }


}