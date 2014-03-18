javaspec2antlr4
===============

Translates the grammar given at the end of the [Java Language Specification](http://docs.oracle.com/javase/specs/jls/se8/html/jls-19.html ) to [ANTLR 4](http://www.antlr.org/index.html).

There are several files that are "important" to this project:
* [provided_java](provided_java) just a copy and paste job from the JLS.
* [JavaLexer.g4](src/main/antlr4/falgout/js2a4/JavaLexer.g4) provides the lexing rules for the Java language.
* [JavaParser.g4](src/main/antlr4/falgout/js2a4/JavaParser.g4) is the direct translation from the JLS.
* [JavaParser2.g4](src/main/antlr4/falgout/js2a4/JavaParser2.g4)
* [JavaParser3.g4](src/main/antlr4/falgout/js2a4/JavaParser3.g4) optimized JavaParser2.g4
