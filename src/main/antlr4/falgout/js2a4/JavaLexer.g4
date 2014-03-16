lexer grammar JavaLexer;

/* 3.9 */
ABSTRACT: 'abstract';
ASSERT: 'assert';
BOOLEAN: 'boolean';
BREAK: 'break';
BYTE: 'byte';
CASE: 'case';
CATCH: 'catch';
CHAR: 'char';
CLASS: 'class';
CONST: 'const';
CONTINUE: 'continue';
DEFAULT: 'default';
DO: 'do';
DOUBLE: 'double';
ELSE: 'else';
ENUM: 'enum';
EXTENDS: 'extends';
FINAL: 'final';
FINALLY: 'finally';
FLOAT: 'float';
FOR: 'for';
IF: 'if';
GOTO: 'goto';
IMPLEMENTS: 'implements';
IMPORT: 'import';
INSTANCEOF: 'instanceof';
INT: 'int';
INTERFACE: 'interface';
LONG: 'long';
NATIVE: 'native';
NEW: 'new';
PACKAGE: 'package';
PRIVATE: 'private';
PROTECTED: 'protected';
PUBLIC: 'public';
RETURN: 'return';
SHORT: 'short';
STATIC: 'static';
STRICTFP: 'strictfp';
SUPER: 'super';
SWITCH: 'switch';
SYNCHRONIZED: 'synchronized';
THIS: 'this';
THROW: 'throw';
THROWS: 'throws';
TRANSIENT: 'transient';
TRY: 'try';
VOID: 'void';
VOLATILE: 'volatile';
WHILE: 'while';

/* 3.10.1 */
IntegerLiteral
    : DecimalIntegerLiteral
    | HexIntegerLiteral
    | OctalIntegerLiteral
    | BinaryIntegerLiteral
    ;
    
fragment
DecimalIntegerLiteral
    : DecimalNumeral IntegerTypeSuffix?
    ;
    
fragment
HexIntegerLiteral
    : HexNumeral IntegerTypeSuffix?
    ;
    
fragment
OctalIntegerLiteral
    : OctalNumeral IntegerTypeSuffix?
    ;
    
fragment
BinaryIntegerLiteral
    : BinaryNumeral IntegerTypeSuffix?
    ;
    
fragment
IntegerTypeSuffix
    : 'l'
    | 'L'
    ;

fragment
DecimalNumeral
    : '0'
    | NonZeroDigit Digits?
    ;
    
fragment
Digits
    : Digit
    | Digit (Digit | '_')* Digit
    ;
    
fragment
Digit
    : '0'
    | NonZeroDigit
    ;
  
fragment
NonZeroDigit
    : [1-9]
    ;
    
fragment
HexNumeral
    : '0' ('x'|'X') HexDigits
    ;
    
fragment
HexDigits
    : HexDigit
    | HexDigit (HexDigit | '_')* HexDigit
    ;
    
fragment
HexDigit
    : [0-9a-fA-F]
    ;
    
fragment
OctalNumeral
    : '0' OctalDigits
    ;
    
fragment
OctalDigits
    : OctalDigit
    | OctalDigit (OctalDigit | '_')* OctalDigit
    ;
    
fragment
OctalDigit
    : [0-7]
    ;
    
fragment
BinaryNumeral
    : '0' [bB] BinaryDigits
    ;
    
fragment
BinaryDigits
    : BinaryDigit
    | BinaryDigit (BinaryDigit | '_')* BinaryDigit
    ;
    
fragment
BinaryDigit
    : [0-1]
    ;
    
 /* 3.10.2 */
 FloatingPointLiteral
    : DecimalFloatingPointLiteral
    | HexadecimalFloatingPointLiteral
    ;
    
fragment
DecimalFloatingPointLiteral
    : Digits '.' Digits? ExponentPart? FloatTypeSuffix?
    | '.' Digits ExponentPart? FloatTypeSuffix?
    | Digits ExponentPart FloatTypeSuffix?
    | Digits ExponentPart FloatTypeSuffix?
    | Digits FloatTypeSuffix
    ;
    
fragment
ExponentPart
    : ExponentIndicator SignedInteger
    ;
    
fragment
ExponentIndicator
    : [eE]
    ;
    
fragment
SignedInteger
    : Sign? Digits
    ;
    
fragment
Sign
    : [-+]
    ;
    
fragment
FloatTypeSuffix
    : [fFdD]
    ;
    
fragment
HexadecimalFloatingPointLiteral
    : HexSignificand BinaryExponent FloatTypeSuffix?
    ;
    
fragment
HexSignificand
    : HexNumeral '.'?
    | '0' [xX] HexDigits? '.' HexDigits
    ;
    
fragment
BinaryExponent
    : BinaryExponentIndicator SignedInteger
    ;
    
fragment
BinaryExponentIndicator
    : [pP]
    ;
    
/* 3.10.3 */
BooleanLiteral
    : 'true'
    | 'false'
    ;
    
/* 3.10.4 */
CharacterLiteral
    : '\'' (SingleCharacter | EscapeSequence) '\''
    ;
    
fragment
SingleCharacter
    : ~['\\]
    ;
    
/* 3.10.5 */
StringLiteral
    : '"' StringCharacter* '"'
    ;
    
fragment
StringCharacter
    : ~["\\]
    | EscapeSequence
    ;
    
/* 3.10.6 */
fragment
EscapeSequence
    : '\\' [btnfr"'\\]
    | OctalEscape
    | UnicodeEscape
    ;
    
fragment
OctalEscape
    : '\\' OctalDigit
    | '\\' OctalDigit OctalDigit
    | '\\' [0-3] OctalDigit OctalDigit
    ;
    
fragment
UnicodeEscape
    : '\\' 'u'+ HexDigit HexDigit HexDigit HexDigit
    ;
    
/** 3.10.7 */
NullLiteral
    : 'null'
    ;
    
/* 3.11 */
L_PARENS: '(';
R_PARENS: ')';
L_CURLY: '{';
R_CURLY: '}';
L_BRACKET: '[';
R_BRACKET: ']';
SEMI: ';';
COMMA: ',';
DOT: '.';

/* 3.12 */
PLUS_ASSIGN: '+=';
MINUS_ASSIGN: '-=';
MULT_ASSIGN: '*=';
DIV_ASSIGN: '/=';
AND_ASSIGN: '&=';
OR_ASSIGN: '|=';
XOR_ASSIGN: '^=';
MOD_ASSIGN: '%=';
L_SHIFT_ASSIGN: '<<=';
R_SHIFT_ASSIGN: '>>=';
UR_SHIFT_ASSIGN: '>>>=';

EQ: '==';
LE: '<=';
GE: '>=';
NEQ: '!=';
L_AND: '&&';
L_OR: '||';
INCREMENT: '++';
DECREMENT: '--';
L_SHIFT: '<<';

ASSIGN: '=';
LT: '<';
GT: '>';
BANG: '!';
TILD: '~';
QUES: '?';
COLON: ':';
PLUS: '+';
MINUS: '-';
MULT: '*';
DIV: '/';
B_AND: '&';
B_OR: '|';
XOR: '^';
MOD: '%';

// and some bonuses
AT: '@';
ELLIPSES: '...';

/* 3.8 
 * TODO Consider full Unicode
 */
Identifier
    : JavaLetter (JavaLetter | JavaDigit)*
    ;
    
fragment
JavaLetter
    : [a-zA-Z$_]
    ;
    
fragment
JavaDigit
    : [0-9]
    ;
    
WS: [ \t\n\r\f] -> channel(WS);
COMMENT
    : ('/*' .*? '*/' | '//' ~[\r\n]*)-> channel(COMMENT)
    ;