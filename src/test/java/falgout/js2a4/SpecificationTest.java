package falgout.js2a4;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.junit.Test;

public class SpecificationTest {
    @Test
    public void parsingDoesNotLoseInformation() throws IOException, URISyntaxException {
        ClassLoader cl = getClass().getClassLoader();
        String resource = "provided_java";
        Translator t = new Translator(cl.getResourceAsStream("provided_java"));
        
        StringBuilder expected = new StringBuilder();
        for (String line : Files.readAllLines(Paths.get(cl.getResource(resource).toURI()), Charset.defaultCharset())) {
            expected.append(line.replaceAll("( |\t)+", ""));
            expected.append("\n");
        }
        
        final StringBuilder parsedInformation = new StringBuilder();
        t.translate(new SpecificationBaseListener() {
            @Override
            public void visitTerminal(TerminalNode node) {
                if (node.getSymbol().getType() != Recognizer.EOF) {
                    parsedInformation.append(node.getText());
                }
            }
        });
        
        assertEquals(expected.toString().trim(), parsedInformation.toString().trim());
    }
}
