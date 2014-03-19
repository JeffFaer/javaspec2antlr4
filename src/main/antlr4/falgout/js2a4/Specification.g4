grammar Specification;

BAR: '|';
L_CURLY: '{';
R_CURLY: '}';
L_BRACKET: '[';
R_BRACKET: ']';
UNDERSCORE: '_';

Token
    : (LETTER | DIGIT | UNDERSCORE)+
    ;
    
fragment
LETTER: [a-zA-Z];

fragment
DIGIT: [0-9];

WS: [ ] -> skip;

rhs
    : syntax (BAR syntax)* EOF
    ;
    
syntax
    : Token+ syntax?
    | optional syntax?
    | closure syntax?
    ;
    
optional
    : L_BRACKET syntax R_BRACKET
    ;
    
closure
    : L_CURLY syntax R_CURLY
    ;