package org.globalbioticinteractions.elton.cmd;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.hamcrest.core.Is;
import org.junit.Test;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;

public class XSLTransformTest {

    @Test
    public void transformEMLWithStylesheetDrucker() throws TransformerException, URISyntaxException, IOException {
        transformAndAssert("eml-drucker.xml", "eml-drucker.html");
    }

    @Test
    public void transformEMLWithStylesheetCarvalheiro() throws TransformerException, URISyntaxException, IOException {
        transformAndAssert("eml-carvalheiro.xml", "eml-carvalheiro.html");
    }

    private void transformAndAssert(String resourceName, String expectedResourceTransformed) throws URISyntaxException, TransformerException, IOException {
        URL resource = getClass().getResource("eml-2/emlroot.xsl");

        StreamSource xslSource =
                new StreamSource(new File(resource.toURI()));
        TransformerFactory tFactory = TransformerFactory.newInstance("org.apache.xalan.processor.TransformerFactoryImpl",null);
        Transformer transformer = tFactory.newTransformer(xslSource);


        // Run the transform engine
        StreamSource ss = new StreamSource(getClass().getResourceAsStream(resourceName));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        StreamResult sr = new StreamResult(outputStream);
        transformer.transform(ss, sr);

        String htmlRendered = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
        String htmlExpected = IOUtils.toString(getClass().getResourceAsStream(expectedResourceTransformed), StandardCharsets.UTF_8);
        
        assertThat(htmlRendered, Is.is(htmlExpected));
    }

}