grammar Specification;
	
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
NonAlphanumeric: ~('['|']'|'('|')'|'{'|'}'|':'|'|'|'\n'|' '|[A-Za-z])+;
Token: UPPER_CASE+;
Identifier: UPPER_CASE (UPPER_CASE|LOWER_CASE|DIGIT)+;

WS: [ \r\t]+ -> skip;

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
    : (NEWLINE syntax)+
    ;

syntax
    : optional syntax?
    | closure syntax?
    | union syntax?
    | terminal syntax?
    | nonTerminal syntax?
    ;
    
optional
    : LEFT_BRACKET syntax RIGHT_BRACKET
    ;
    
closure
    : LEFT_CURLY syntax RIGHT_CURLY
    ;
    
union
    : LEFT_PARENS syntax (BAR syntax)+ RIGHT_PARENS
    ;
	
nonTerminal
	: Identifier
	| Token
	;
	
terminal
    : LowerCaseWord
    | symbol
    | Token
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