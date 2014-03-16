javaspec2antlr4
===============

Translates the grammar given at the end of the [Java Language Specification](http://docs.oracle.com/javase/specs/jls/se7/html/jls-18.html ) to [ANTLR 4](http://www.antlr.org/index.html).

There are several files that are "important" to this project:
* [provided_java](provided_java) just a copy and paste job from the JLS.
* [JavaLexer.g4](src/main/antlr4/falgout/js2a4/JavaLexer.g4) provides the lexing rules for the Java language.
* [JavaParser.g4](src/main/antlr4/falgout/js2a4/JavaParser.g4) is the direct translation from the JLS.
* [JavaParser2.g4](src/main/antlr4/falgout/js2a4/JavaParser2.g4) updates JavaParser.g4 so that it can compile all of the files in the JDK (albeit, very slowly in some cases)
  * Empty for statements: `for(;;)`. (Update to `JavaParser.forControl`)
  * Primitive arrays: `short[] shortArray = new short[5];` for all primitive types. (Update to `JavaParser.creator`)
  * Compound assignment: `x = y = z;`. (Update to `JavaParser.expression`)
  * Arrays in generics: `List<ReferenceType[]>`. (Update to `JavaParser.type`, `JavaParser.referenceType`)
  * Compound logic expressions: `boolean b = expression && variable instanceof Type && anotherExpression`. Before, `instanceof` checks had to be made at the end of a compound expression. (Update to `JavaParser.expression2Rest`)
  * Operators: `>>` and `>>>` are not tokens (it's possible that there is a clever way to allow them to be, but I don't see it) because they can interfere with nested generics: `List<List<String>>` (the `>>` would become a single token, not the two separate `GT` tokens which are expected). Update how the `JavaParser.infixOps` finds `>`, `>>`, and `>>>`.
* [JavaParser3.g4](src/main/antlr4/falgout/js2a4/JavaParser3.g4) optimized JavaParser2.g4
  * Streamlined `forControl`
  * Combined the various expression rules and added `<assoc='right'>` options to the assignment operators.
  * Updated `primary` to work with the new `expression` and to remove the epsilon option.
