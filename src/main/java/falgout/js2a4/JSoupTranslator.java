package falgout.js2a4;

import static java.util.stream.Collectors.joining;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

import falgout.js2a4.ANTLRv4Parser.GrammarSpecContext;
import falgout.js2a4.ANTLRv4Parser.LexerRuleContext;
import falgout.js2a4.ANTLRv4Parser.RuleSpecContext;
import falgout.js2a4.SpecificationParser.ClosureContext;
import falgout.js2a4.SpecificationParser.OptionalContext;
import falgout.js2a4.SpecificationParser.RhsContext;

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
        b.append("parser grammar JavaParser;\n");
        b.append("\n");
        b.append("options {\n");
        b.append(TAB).append("tokenVocab = JavaLexer;\n");
        b.append("}\n");
        b.append("\n");

        Document doc = Jsoup.parse(new File(file), Charset.defaultCharset().name());
        List<Element> productions = doc.getElementsByClass("production");

        for (Element production : productions) {
            Element lhs = production.getElementsByClass("lhs").get(0);
            Element rhs = production.getElementsByClass("rhs").get(0);

            String left = parseLhs(lhs);
            if (left != null) {
                b.append(left).append("\n");
                b.append(TAB).append(": ").append(parseRhs(left, rhs)).append("\n");
                b.append(TAB).append(";").append("\n");
                b.append("\n");
            }
        }

        return b.toString().trim();
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
            String ruleName = Character.toLowerCase(text.charAt(0)) + text.substring(1);
            while (tokens.containsKey(ruleName)) {
                ruleName = ruleName.substring(0, ruleName.length() - 1);
            }
            return ruleName;
        }
    }

    private String getTokenName(String text) {
        String token = tokens.get(text);
        if (token == null) {
            token = text.chars().mapToObj(i -> String.valueOf((char) i)).map(s -> tokens.get(s)).collect(joining(" "));
        }

        return token;
    }
    
    private String parseRhs(String lhs, Element rhs) {
        String newline = "\n" + TAB + "| ";
        switch (lhs) {
        case "integralType":
        case "floatingPointType":
        case "assignmentOperator":
            return rhs.getElementsByTag("code").stream().map(e -> getTokenName(e.text())).collect(joining(newline));
        default:
            for (Element e : rhs.getElementsByTag("a")) {
                e.unwrap();
            }
            for (TextNode n : rhs.textNodes()) {
                n.text(getRuleName(n.text()));
            }
            for (Element e : rhs.getElementsByTag("br")) {
                e.replaceWith(new TextNode("|", null));
            }
            for (Element e : rhs.getElementsByTag("code")) {
                e.replaceWith(new TextNode(getTokenName(e.text()), null));
            }

            SpecificationLexer lex = new SpecificationLexer(new ANTLRInputStream(rhs.text()));
            SpecificationParser parser = new SpecificationParser(new CommonTokenStream(lex));
            
            SpecificationVisitor<String> toString = new SpecificationBaseVisitor<String>() {
                @Override
                public String visitClosure(ClosureContext ctx) {
                    return "(" + ctx.syntax().accept(this) + ")*";
                }

                @Override
                public String visitOptional(OptionalContext ctx) {
                    return "(" + ctx.syntax().accept(this) + ")?";
                }

                @Override
                public String visitTerminal(TerminalNode node) {
                    return getRuleName(node.getText());
                }

                @Override
                protected String aggregateResult(String aggregate, String nextResult) {
                    if (aggregate == null) {
                        return nextResult;
                    } else {
                        return aggregate + " " + nextResult;
                    }
                }
            };
            RhsContext parsed = parser.rhs();
            Iterator<ParseTree> i = parsed.children.iterator();
            while (i.hasNext()) {
                ParseTree t = i.next();
                if (t instanceof TerminalNode) {
                    if (((TerminalNode) t).getSymbol().getType() == SpecificationParser.BAR) {
                        if (i.next().getText().length() == 0) {
                            i.remove();
                        }
                    }
                }
            }
            return parsed.syntax().stream().map(ctx -> ctx.accept(toString)).collect(joining(newline));
        }
    }

    public static void main(String[] args) throws IOException {
        String f = new JSoupTranslator("provided_java").translate();
        System.out.println(f);
        Files.write(Paths.get("src/main/antlr4/falgout/js2a4/JavaParser.g4"), f.getBytes());
    }
}
