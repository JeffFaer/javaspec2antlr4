package falgout.js2a4;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.gui.TreeViewer;

import falgout.js2a4.ANTLRv4Parser.GrammarSpecContext;
import falgout.js2a4.ANTLRv4Parser.LexerRuleContext;
import falgout.js2a4.ANTLRv4Parser.RuleSpecContext;
import falgout.js2a4.SpecificationParser.ClosureContext;
import falgout.js2a4.SpecificationParser.LhsContext;
import falgout.js2a4.SpecificationParser.NonTerminalContext;
import falgout.js2a4.SpecificationParser.OptionalContext;
import falgout.js2a4.SpecificationParser.ProductionContext;
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
                    // done walking the tree
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
                b.append("parser grammar JavaParser;\n");
                b.append("\n");
                b.append("options {\n");
                b.append(TAB + "tokenVocab = JavaLexer;\n");
                b.append("}\n");
                b.append("\n");
                
                b.append(join(ctx.production(), "\n\n"));
                return b.toString();
            }
            
            @Override
            public String visitProduction(ProductionContext ctx) {
                StringBuilder b = new StringBuilder();
                String rhs = join(ctx.rhs(), "\n" + TAB + "| ");
                
                Iterator<LhsContext> i = ctx.lhs().iterator();
                while (i.hasNext()) {
                    b.append(visitLhs(i.next()));
                    b.append("\n" + TAB + ": ");
                    b.append(rhs);
                    b.append("\n" + TAB + ";");
                    
                    if (i.hasNext()) {
                        b.append("\n");
                        b.append("\n");
                    }
                }
                
                return b.toString();
            }
            
            private String getTokenText(String text) {
                String tokenText = tokens.get(text);
                if (tokenText == null && text.length() > 1) {
                    // let's see if each individual character is a token
                    StringBuilder b = new StringBuilder();
                    for (int i = 0; i < text.length(); i++) {
                        if (i > 0) {
                            b.append(" ");
                        }
                        b.append(tokens.get(text.subSequence(i, i + 1)));
                    }
                    tokenText = b.toString();
                }
                
                return tokenText;
            }
            
            @Override
            public String visitNonTerminal(NonTerminalContext ctx) {
                return nonTerminals.get(ctx.getText());
            }
            
            @Override
            public String visitTerminal(TerminalContext ctx) {
                return getTokenText(ctx.getText());
            }
            
            @Override
            public String visitTerminal(TerminalNode node) {
                return tokens.get(node.getText());
            }
            
            @Override
            public String visitClosure(ClosureContext ctx) {
                ClosureContext ccx = getParent(ctx.parent, ClosureContext.class);
                if (ccx == null) {
                    for (String lhs : getLhsNames(ctx)) {
                        switch (lhs) {
                        case "block":
                        case "interfaceBody":
                        case "annotationTypeBody":
                        case "classBody":
                        case "enumBody":
                        case "elementValueArrayInitializer":
                        case "arrayInitializer":
                        case "statement":
                            StringBuilder b = new StringBuilder();
                            b.append(tokens.get("{")).append(" ");
                            b.append(join(ctx.syntax(), " "));
                            b.append(" ").append(tokens.get("}"));
                            return b.toString();
                        }
                    }
                }
                
                return "(" + join(ctx.syntax(), " ") + ")*";
            }
            
            private <R extends RuleContext> R getParent(RuleContext ctx, Class<R> clazz) {
                while (ctx != null && !clazz.isInstance(ctx)) {
                    ctx = ctx.parent;
                }
                return ctx == null ? null : clazz.cast(ctx);
            }
            
            private List<String> getLhsNames(RuleContext ctx) {
                List<String> names = new ArrayList<>();
                for (LhsContext l : getParent(ctx, ProductionContext.class).lhs()) {
                    names.add(l.accept(this));
                }
                
                return names;
            }
            
            @Override
            public String visitOptional(OptionalContext ctx) {
                for (String lhs : getLhsNames(ctx)) {
                    switch (lhs) {
                    case "selector":
                        if (ctx.syntax().size() == 1) {
                            SyntaxContext scx = ctx.syntax(0);
                            if (scx.nonTerminal() == null || !"expression".equals(scx.nonTerminal().accept(this))) {
                                break;
                            }
                        } else {
                            break;
                        }
                        //$FALL-THROUGH$
                    case "arrayCreatorRest":
                        StringBuilder b = new StringBuilder();
                        b.append(tokens.get("[")).append(" ");
                        b.append(join(ctx.syntax(), " "));
                        b.append(" ").append(tokens.get("]"));
                        
                        return b.toString();
                    }
                }
                return "(" + join(ctx.syntax(), " ") + ")?";
            }
            
            @Override
            public String visitUnion(UnionContext ctx) {
                StringBuilder b = new StringBuilder("(");
                for (int i = 1; i < ctx.getChildCount() - 1; i++) {
                    if (i > 1) {
                        b.append(" ");
                    }
                    
                    ParseTree p = ctx.getChild(i);
                    if (p instanceof TerminalNode) {
                        b.append("|");
                    } else {
                        b.append(ctx.getChild(i).accept(this));
                    }
                }
                return b.append(")").toString();
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
                    
                    ANTLRv4Lexer lex = new ANTLRv4Lexer(new ANTLRFileStream(
                            "src/main/antlr4/falgout/js2a4/JavaLexer.g4"));
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
        String f = new Translator(args).translate();
        System.out.println(f);
        Files.write(Paths.get("src/main/antlr4/falgout/js2a4/JavaParser.g4"), f.getBytes());
    }
}
