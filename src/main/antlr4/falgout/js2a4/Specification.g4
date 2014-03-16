grammar Specification;

@members{
    boolean bar = true;
    boolean bracket = true;
}

L_CURLY: '{';
R_CURLY: '}';
L_PARENS: '(';
R_PARENS: ')';
L_BRACKET: '[';
R_BRACKET: ']';
COLON: ':';
BAR: '|';
NEWLINE: '\n';
EMPTY_CURLY: '{}';
EMPTY_PARENS: '()';
EMPTY_BRACKET: '[]';

Identifier
    : UPPER_CASE (UPPER_CASE | LOWER_CASE | DIGIT)*
    ;
    
Keyword
    : LOWER_CASE+
    ;

WS: [ \t\r]+ -> skip;

Symbol
    : ~[ A-Za-z0-9]
    ;
    
fragment
UPPER_CASE: [A-Z];

fragment
LOWER_CASE: [a-z];

fragment
DIGIT: [0-9];

specification
    : production (NEWLINE NEWLINE+ production)* NEWLINE* EOF
    ;
    
production
    : (lhs COLON NEWLINE)+ rhs (NEWLINE rhs)*
    ;
    
lhs
    : nonTerminal
    ;
    
rhs
    : syntax+
    ;
    
syntax
    : closure
    | optional
    | parenthetical
    | nonTerminal
    | terminal
    ;
    
closure
    : L_CURLY syntax+ R_CURLY
    ;
    
optional
    : L_BRACKET {bracket = false;} syntax+ R_BRACKET {bracket = true;}
    ;
    
parenthetical
    : union
    | L_PARENS syntax* R_PARENS
    ;
    
union
    : L_PARENS {bar = false;} syntax+ (BAR syntax+)+ R_PARENS {bar = true;}
    ;
    
nonTerminal
    : Identifier
    ;
    
terminal
    : Keyword
    | symbols
    | {bracket}? L_BRACKET
    ;
    
symbols
    : symbol+
    ;
    
 symbol
    : Symbol
    | COLON
    | BAR BAR
    | EMPTY_PARENS
    | EMPTY_BRACKET
    | EMPTY_CURLY
    | {bar}? BAR
    | {bracket}? R_BRACKET
    ;