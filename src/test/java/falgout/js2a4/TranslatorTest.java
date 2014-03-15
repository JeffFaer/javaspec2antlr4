package falgout.js2a4;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.antlr.runtime.RecognitionException;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;

public class TranslatorTest {
    @Test
    public void translationCanParseJDK() throws IOException, URISyntaxException, RecognitionException {
        ClassLoader cl = getClass().getClassLoader();
        
        try (ZipInputStream src = new ZipInputStream(cl.getResourceAsStream("src.zip"))) {
            ZipEntry e;
            while ((e = src.getNextEntry()) != null) {
                if (e.getName().endsWith(".java")) {
                    ByteArrayOutputStream sink = new ByteArrayOutputStream((int) e.getSize());
                    byte[] buf = new byte[1024 * 8];
                    int read;
                    while ((read = src.read(buf)) > 0) {
                        sink.write(buf, 0, read);
                    }
                    String file = new String(sink.toByteArray());
                    
                    JavaLexer lex = new JavaLexer(new ANTLRInputStream(file));
                    JavaParser parse = new JavaParser(new CommonTokenStream(lex));
                    parse.setErrorHandler(new BailErrorStrategy());
                    try {
                        parse.compilationUnit();
                    } catch (Throwable t) {
                        System.out.println(file);
                        throw t;
                    }
                }
            }
        }
    }
}
