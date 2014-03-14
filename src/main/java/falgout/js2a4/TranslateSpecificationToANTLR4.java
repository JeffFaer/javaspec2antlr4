package falgout.js2a4;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

public class TranslateSpecificationToANTLR4 {
    public static void main(String[] args) throws IOException {
        System.out.println(convert(new FileReader(getFileName(args))));
    }
    
    public static String getFileName(String... args) {
        return args.length == 1 ? args[0] : "provided_java";
    }
    
    public static SpecificationParser getParser(Reader file) throws IOException {
        ANTLRInputStream in = new ANTLRInputStream(file);
        SpecificationLexer lexer = new SpecificationLexer(in);
        
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        return new SpecificationParser(tokens);
    }
    
    public static String convert(Reader file) throws IOException {
        SpecificationParser parser = getParser(file);
        return parser.specification().toStringTree(parser);
    }
}
