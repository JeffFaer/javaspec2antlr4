package falgout.js2a4;

import static java.util.stream.Collectors.joining;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import falgout.js2a4.ANTLRv4Parser.GrammarSpecContext;
import falgout.js2a4.ANTLRv4Parser.LexerRuleContext;
import falgout.js2a4.ANTLRv4Parser.RuleSpecContext;

public class JSoupTranslator {
    private static final String TAB = "    ";
    
    private final Map<String, String> tokens;
    private final String file;
    
    public JSoupTranslator(String file) throws IOException {
        this.file = file;
        tokens = new LinkedHashMap<>();

        ANTLRv4Lexer lex = new ANTLRv4Lexer(new ANTLRFileStream("src/main/antlr4/falgout/js2a4/JavaLexer.g4"));
        ANTLRv4Parser parse = new ANTLRv4Parser(new CommonTokenStream(lex));
        GrammarSpecContext g = parse.grammarSpec();

        for (RuleSpecContext r : g.rules().ruleSpec()) {
            LexerRuleContext l = r.lexerRule();
            String left = l.TOKEN_REF().getText();
            tokens.put(left, left);
            if (left.toUpperCase().equals(left)) {
                String right = l.lexerRuleBlock().accept(new ANTLRv4ParserBaseVisitor<String>() {
                    @Override
                    public String visitTerminal(ANTLRv4Parser.TerminalContext ctx) {
                        return ctx.getText();
                    }
                });
                if (right != null) {
                    // remove quotes
                    right = right.substring(1, right.length() - 1);
                    tokens.put(right, left);
                }
            }
        }
    }
    
    public String translate() throws IOException {
        StringBuilder b = new StringBuilder();
        
        Document doc = Jsoup.parse(new File(file), Charset.defaultCharset().name());
        List<Element> productions = doc.getElementsByClass("production");
        
        for (Element production : productions) {
            Element lhs = production.getElementsByClass("lhs").get(0);
            Element rhs = production.getElementsByClass("rhs").get(0);
            
            String left = parseLhs(lhs);
            if (left != null) {
                b.append(left).append("\n");
                b.append(TAB).append(":").append(parseRhs(rhs)).append("\n");
                b.append(TAB).append(";").append("\n");
                b.append("\n");
            }
        }
        
        return b.toString();
    }
    
    private String parseLhs(Element lhs) {
        String id = lhs.text();
        id = id.substring(0, id.length() - 1);
        
        return tokens.containsKey(id) ? null : getRuleName(id);
    }
    
    private String getRuleName(String text) {
        if (tokens.containsKey(text)) {
            return text;
        } else {
            return Character.toLowerCase(text.charAt(0)) + text.substring(1);
        }
    }
    
    private String getTokenName(String text) {
        String token = tokens.get(text);
        if (token == null) {
            token = text.chars().mapToObj(i -> String.valueOf((char) i)).map(s -> tokens.get(s)).collect(joining(" "));
        }
        
        return token;
    }

    private String parseRhs(Element rhs) {
        StringBuilder b = new StringBuilder();
        for (Element e : rhs.getAllElements()) {
            switch (e.tagName()) {
            case "div":
                break;
            case "br":
                b.append("\n").append(TAB).append("|");
                break;
            case "code":
                b.append(getTokenName(e.text()));
                break;
            default:
                b.append(getRuleName(e.text()));
                break;
            }
            b.append(" ");
        }
        return b.toString();
    }
    
    public static void main(String[] args) throws IOException {
        String f = new JSoupTranslator("provided_java").translate();
        System.out.println(f);
        Files.write(Paths.get("src/main/antlr4/falgout/js2a4/JavaParser.g4"), f.getBytes());
    }
}
