package falgout.js2a4;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.antlr.runtime.RecognitionException;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.ParserInterpreter;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.LexerGrammar;
import org.junit.Test;

public class TranslatorTest {
    @Test
    public void translationCanParseJDK() throws IOException, URISyntaxException, RecognitionException {
        ClassLoader cl = getClass().getClassLoader();
        Path lexFile = Paths.get(cl.getResource("JavaLexer.g4").toURI());
        
        Translator t = new Translator(cl.getResourceAsStream("provided_java"));
        String translation = t.translate();
        
        LexerGrammar lex = new LexerGrammar(new String(Files.readAllBytes(lexFile), Charset.defaultCharset()));
        Grammar grammar = new Grammar(translation, lex);
        
        try (ZipInputStream src = new ZipInputStream(cl.getResourceAsStream("src.zip"))) {
            ZipEntry e;
            while ((e = src.getNextEntry()) != null) {
                if (e.getName().endsWith(".java")) {
                    Lexer l = lex.createLexerInterpreter(new ANTLRInputStream(src));
                    ParserInterpreter p = grammar.createParserInterpreter(new CommonTokenStream(l));
                    p.setErrorHandler(new BailErrorStrategy());
                    p.parse(grammar.getRule("compilationUnit").index);
                }
            }
        }
    }
}
