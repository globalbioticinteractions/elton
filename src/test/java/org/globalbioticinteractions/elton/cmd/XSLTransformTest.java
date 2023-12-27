package org.globalbioticinteractions.elton.cmd;

import org.apache.commons.io.FileUtils;
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

public class XSLTransformTest {

    @Test
    public void transformEMLWithStylesheet() throws TransformerException, URISyntaxException, IOException {
        transformAndAssert("eml-drucker.xml");
    }

    @Test
    public void transformEMLWithStylesheet2() throws TransformerException, URISyntaxException, IOException {
        transformAndAssert("eml-carvalheiro.xml");
    }

    private void transformAndAssert(String resourceName) throws URISyntaxException, TransformerException, IOException {
        URL resource = getClass().getResource("eml-2/emlroot.xsl");

        StreamSource xslSource =
                new StreamSource(new File(resource.toURI()));
        // Create a stylesheet from the system id that was found
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer(xslSource);


        // Run the transform engine
        StreamSource ss = new StreamSource(getClass().getResourceAsStream(resourceName));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        StreamResult sr = new StreamResult(outputStream);
        transformer.transform(ss, sr);

        String htmlRendered = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);

        FileUtils.writeStringToFile(new File("/tmp/bla.html"), htmlRendered, StandardCharsets.UTF_8);

        assertThat(htmlRendered, startsWith("<!DOCTYPE html"));
    }

}