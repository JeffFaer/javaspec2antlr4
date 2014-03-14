package falgout.js2a4;

import static org.junit.Assert.assertEquals;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.junit.Test;

public class SpecificationTest {
    @Test
    public void parsingDoesNotLoseInformation() throws IOException {
        String file = TranslateSpecificationToANTLR4.getFileName();
        
        StringBuilder expected = new StringBuilder();
        for (String line : Files.readAllLines(Paths.get(file), Charset.defaultCharset())) {
            expected.append(line.replaceAll("( |\t)+", ""));
            expected.append("\n");
        }
        
        Reader r = new FileReader(file);
        SpecificationParser parser = TranslateSpecificationToANTLR4.getParser(r);
        
        final StringBuilder parsedInformation = new StringBuilder();
        parser.addParseListener(new SpecificationBaseListener() {
            @Override
            public void visitTerminal(TerminalNode node) {
                if (node.getSymbol().getType() != Recognizer.EOF) {
                    parsedInformation.append(node.getText());
                }
            }
        });
        parser.specification();
        
        assertEquals(expected.toString().trim(), parsedInformation.toString().trim());
    }
}
