package falgout.js2a4;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import falgout.js2a4.ANTLRv4Parser.GrammarSpecContext;
import falgout.js2a4.ANTLRv4Parser.LexerRuleContext;
import falgout.js2a4.ANTLRv4Parser.RuleSpecContext;
import falgout.js2a4.SpecificationParser.ClosureContext;
import falgout.js2a4.SpecificationParser.LhsContext;
import falgout.js2a4.SpecificationParser.NonTerminalContext;
import falgout.js2a4.SpecificationParser.OptionalContext;
import falgout.js2a4.SpecificationParser.ProductionContext;
import falgout.js2a4.SpecificationParser.RhsContext;
import falgout.js2a4.SpecificationParser.SpecificationContext;
import falgout.js2a4.SpecificationParser.SyntaxContext;
import falgout.js2a4.SpecificationParser.TerminalContext;
import falgout.js2a4.SpecificationParser.UnionContext;

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
        
        final Map<String, String> tokens = getTokens();
        final Map<String, String> nonTerminals = new LinkedHashMap<>();
        
        SpecificationContext spec = parser.specification();
        
        final List<NonTerminalContext> remove = new ArrayList<>();
        ParseTreeWalker w = new ParseTreeWalker();
        w.walk(new SpecificationBaseListener() {
            @Override
            public void enterNonTerminal(NonTerminalContext ctx) {
                String text = ctx.getText();
                if (tokens.containsKey(text)) {
                    nonTerminals.put(text, tokens.get(text));
                    
                    // This nonTerminal in the Java Spec is actually handled
                    // as a token in the JavaLexer.g4, remove it when we're
                    // done wlaking the tree
                    if (ctx.parent instanceof LhsContext) {
                        remove.add(ctx);
                    }
                } else if (!nonTerminals.containsKey(text)) {
                    String antlrId = Character.toLowerCase(text.charAt(0)) + text.substring(1);
                    while (tokens.containsKey(antlrId)) {
                        antlrId = antlrId.substring(0, antlrId.length() - 1);
                    }
                    nonTerminals.put(text, antlrId);
                }
            }
        }, spec);
        
        // remove rules which are handled as tokens
        for (RuleContext ctx : remove) {
            while (!spec.children.remove(ctx)) {
                ctx = ctx.parent;
            }
        }
        
        SpecificationVisitor<String> toString = new SpecificationBaseVisitor<String>() {
            private static final String TAB = "    ";
            
            @Override
            public String visitSpecification(SpecificationContext ctx) {
                StringBuilder b = new StringBuilder();
                b.append("parser grammar Java;\n");
                b.append("\n");
                b.append("options {\n");
                b.append(TAB + "tokenVocab=JavaLexer;\n");
                b.append("}\n");
                b.append("\n");
                
                b.append(join(ctx.production(), "\n\n"));
                return b.toString();
            }
            
            @Override
            public String visitProduction(ProductionContext ctx) {
                StringBuilder b = new StringBuilder();
                String rhs = visitRhs(ctx.rhs());
                
                Iterator<LhsContext> i = ctx.lhs().iterator();
                while (i.hasNext()) {
                    b.append(visitLhs(i.next()));
                    b.append("\n" + TAB + ": ");
                    b.append(rhs);
                    
                    if (i.hasNext()) {
                        b.append("\n");
                        b.append("\n");
                    }
                }
                
                return b.toString();
            }
            
            @Override
            public String visitRhs(RhsContext ctx) {
                return join(ctx.syntax(), "\n" + TAB + "| ") + "\n" + TAB + ";";
            }
            
            @Override
            public String visitNonTerminal(NonTerminalContext ctx) {
                return nonTerminals.get(ctx.getText());
            }
            
            @Override
            public String visitTerminal(TerminalContext ctx) {
                String text = ctx.getText();
                String ret = tokens.get(text);
                if (ret == null && text.length() > 1) {
                    // welp, that didn't work. let's try each character
                    // individually
                    StringBuilder b = new StringBuilder();
                    for (int i = 0; i < text.length(); i++) {
                        if (i > 0) {
                            b.append(" ");
                        }
                        
                        b.append(tokens.get(text.substring(i, i + 1)));
                    }
                    
                    ret = b.toString();
                }
                return ret;
            }
            
            @Override
            public String visitClosure(ClosureContext ctx) {
                return visitSyntaxElement(ctx.syntax(), "*");
            }
            
            @Override
            public String visitOptional(OptionalContext ctx) {
                return visitSyntaxElement(ctx.syntax(), "?");
            }
            
            private String visitSyntaxElement(SyntaxContext ctx, String suffix) {
                String child = visitSyntax(ctx);
                StringBuilder b = new StringBuilder("(");
                b.append(child).append(")").append(suffix);
                return b.toString();
            }
            
            @Override
            public String visitUnion(UnionContext ctx) {
                StringBuilder b = new StringBuilder("(");
                b.append(join(ctx.syntax(), " | ")).append(")");
                return b.toString();
            }
            
            @Override
            protected String aggregateResult(String aggregate, String nextResult) {
                if (aggregate == null) {
                    return nextResult;
                } else {
                    return aggregate + " " + nextResult;
                }
            }
            
            private String join(List<? extends ParseTree> ctx, String delim) {
                StringBuilder b = new StringBuilder();
                Iterator<? extends ParseTree> i = ctx.iterator();
                while (i.hasNext()) {
                    b.append(i.next().accept(this));
                    
                    if (i.hasNext()) {
                        b.append(delim);
                    }
                }
                
                return b.toString();
            }
        };
        return toString.visit(spec);
    }
    
    private Map<String, String> tokens;
    
    // pull tokens from JavaLexer.g4
    // constant tokens are also put in as a reverse mapping (rule -> token name)
    private Map<String, String> getTokens() throws IOException {
        if (tokens == null) {
            synchronized (this) {
                if (tokens == null) {
                    tokens = new LinkedHashMap<>();
                    
                    ANTLRv4Lexer lex = new ANTLRv4Lexer(new ANTLRInputStream(getClass().getClassLoader()
                            .getResourceAsStream("JavaLexer.g4")));
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
                                right = right.substring(1, right.length() - 1);
                                tokens.put(right, left);
                            }
                        }
                    }
                }
            }
        }
        return tokens;
    }
    
    public static void main(String[] args) throws IOException {
        System.out.println(new Translator(args).translate());
    }
}
