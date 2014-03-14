grammar Specification;
	
WS: [ \r\t]+ -> skip;

COLON: ':';
LEFT_BRACKET: '[';
RIGHT_BRACKET: ']';
LEFT_CURLY: '{';
RIGHT_CURLY: '}';
LEFT_PARENS: '(';
RIGHT_PARENS: ')';
BAR: '|';
NEWLINE: '\n';

fragment
UPPER_CASE: [A-Z];
fragment
LOWER_CASE: [a-z];
fragment
DIGIT: [0-9];

LowerCaseWord: (LOWER_CASE)+;
NonAlphanumeric: ~('['|']'|'('|')'|'{'|'}'|':'|'|'|'\n'|[A-Za-z])+;
Identifier: UPPER_CASE (UPPER_CASE|LOWER_CASE|DIGIT)+;

specification
    : production (NEWLINE NEWLINE production)* EOF
    ;

production
    : (NEWLINE? lhs COLON)+ rhs
    ;

lhs
    : nonTerminal
    ;
	
rhs
    : (NEWLINE syntax+)+
    ;

syntax
    : LEFT_BRACKET syntax+ RIGHT_BRACKET
    | LEFT_CURLY syntax+ RIGHT_CURLY
    | LEFT_PARENS syntax+ BAR syntax+ RIGHT_PARENS
    | nonTerminal
    | terminal
    ;
	
nonTerminal
	: Identifier
	;
	
terminal
    : keyword
    | symbol
    ;

keyword
    : LowerCaseWord
    ;

symbol
    : NonAlphanumeric
    | LEFT_BRACKET? RIGHT_BRACKET
    | LEFT_CURLY RIGHT_CURLY
    | LEFT_PARENS
    | RIGHT_PARENS
    | COLON
    | BAR
    ;