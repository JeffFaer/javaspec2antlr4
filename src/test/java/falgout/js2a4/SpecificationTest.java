package falgout.js2a4;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.junit.Test;

import falgout.js2a4.SpecificationParser.SpecificationContext;

public class SpecificationTest {
    @Test
    public void parsingDoesNotLoseInformation() throws IOException, URISyntaxException {
        String resource = "provided_java";
        
        StringBuilder expected = new StringBuilder();
        for (String line : Files.readAllLines(Paths.get(resource), Charset.defaultCharset())) {
            expected.append(line.replaceAll("( |\t)+", ""));
            expected.append("\n");
        }
        
        SpecificationLexer lex = new SpecificationLexer(new ANTLRFileStream(resource));
        SpecificationParser parser = new SpecificationParser(new CommonTokenStream(lex));
        SpecificationContext spec = parser.specification();
        
        final StringBuilder parsedInformation = new StringBuilder();
        ParseTreeWalker w = new ParseTreeWalker();
        w.walk(new SpecificationBaseListener() {
            @Override
            public void visitTerminal(TerminalNode node) {
                if (node.getSymbol().getType() != Recognizer.EOF) {
                    parsedInformation.append(node.getText());
                }
            }
        }, spec);
        
        assertEquals(expected.toString().trim(), parsedInformation.toString().trim());
    }
}
