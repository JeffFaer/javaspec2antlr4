package falgout.js2a4;

import java.io.IOException;
import java.io.InputStream;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

public class Translator {
    private String file;
    private InputStream in;
    
    public Translator(String... args) {
        file = args.length == 1 ? args[0] : "provided_java";
    }
    
    public Translator(InputStream in) {
        this.in = in;
    }
    
    private ANTLRInputStream getInputStream() throws IOException {
        if (in != null) {
            return new ANTLRInputStream(in);
        } else {
            return new ANTLRFileStream(file);
        }
    }
    
    public String translate(SpecificationListener... listeners) throws IOException {
        SpecificationLexer lexer = new SpecificationLexer(getInputStream());
        SpecificationParser parser = new SpecificationParser(new CommonTokenStream(lexer));
        for (SpecificationListener l : listeners) {
            parser.addParseListener(l);
        }
        return parser.specification().toStringTree(parser);
    }
    
    public static void main(String[] args) throws IOException {
        System.out.println(new Translator(args).translate());
    }
}
