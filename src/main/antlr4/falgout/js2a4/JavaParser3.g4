parser grammar JavaParser3;

options {
    tokenVocab = JavaLexer;
}

qualifiedIdentifier
    : Identifier (DOT Identifier)*
    ;

qualifiedIdentifierList
    : qualifiedIdentifier (COMMA qualifiedIdentifier)*
    ;

compilationUnit
    : ((annotations)? PACKAGE qualifiedIdentifier SEMI)? (importDeclaration)* (typeDeclaration)*
    ;

importDeclaration
    : IMPORT (STATIC)? Identifier (DOT Identifier)* (DOT MULT)? SEMI
    ;

typeDeclaration
    : classOrInterfaceDeclaration
    | SEMI
    ;

classOrInterfaceDeclaration
    : (modifier)* (classDeclaration | interfaceDeclaration)
    ;

classDeclaration
    : normalClassDeclaration
    | enumDeclaration
    ;

interfaceDeclaration
    : normalInterfaceDeclaration
    | annotationTypeDeclaration
    ;

normalClassDeclaration
    : CLASS Identifier (typeParameters)? (EXTENDS type)? (IMPLEMENTS typeList)? classBody
    ;

enumDeclaration
    : ENUM Identifier (IMPLEMENTS typeList)? enumBody
    ;

normalInterfaceDeclaration
    : INTERFACE Identifier (typeParameters)? (EXTENDS typeList)? interfaceBody
    ;

annotationTypeDeclaration
    : AT INTERFACE Identifier annotationTypeBody
    ;

type
    : basicType
    | referenceType
    ;

basicType
    : BYTE
    | SHORT
    | CHAR
    | INT
    | LONG
    | FLOAT
    | DOUBLE
    | BOOLEAN
    ;

referenceType
    : Identifier (typeArguments)? (DOT Identifier (typeArguments)?)* (L_BRACKET R_BRACKET)*
    | basicType (L_BRACKET R_BRACKET)+
    ;

typeArguments
    : LT typeArgument (COMMA typeArgument)* GT
    ;

typeArgument
    : referenceType
    | QUES ((EXTENDS | SUPER) referenceType)?
    ;

nonWildcardTypeArguments
    : LT typeList GT
    ;

typeList
    : referenceType (COMMA referenceType)*
    ;

typeArgumentsOrDiamond
    : LT GT
    | typeArguments
    ;

nonWildcardTypeArgumentsOrDiamond
    : LT GT
    | nonWildcardTypeArguments
    ;

typeParameters
    : LT typeParameter (COMMA typeParameter)* GT
    ;

typeParameter
    : Identifier (EXTENDS bound)?
    ;

bound
    : referenceType (B_AND referenceType)*
    ;

modifier
    : annotation
    | PUBLIC
    | PROTECTED
    | PRIVATE
    | STATIC
    | ABSTRACT
    | FINAL
    | NATIVE
    | SYNCHRONIZED
    | TRANSIENT
    | VOLATILE
    | STRICTFP
    ;

annotations
    : annotation (annotation)*
    ;

annotation
    : AT qualifiedIdentifier (L_PARENS (annotationElement)? R_PARENS)?
    ;

annotationElement
    : elementValuePairs
    | elementValue
    ;

elementValuePairs
    : elementValuePair (COMMA elementValuePair)*
    ;

elementValuePair
    : Identifier ASSIGN elementValue
    ;

elementValue
    : annotation
    | expression
    | elementValueArrayInitializer
    ;

elementValueArrayInitializer
    : L_CURLY (elementValues)? (COMMA)? R_CURLY
    ;

elementValues
    : elementValue (COMMA elementValue)*
    ;

classBody
    : L_CURLY (classBodyDeclaration)* R_CURLY
    ;

classBodyDeclaration
    : SEMI
    | (modifier)* memberDecl
    | (STATIC)? block
    ;

memberDecl
    : methodOrFieldDecl
    | VOID Identifier voidMethodDeclaratorRest
    | Identifier constructorDeclaratorRest
    | genericMethodOrConstructorDecl
    | classDeclaration
    | interfaceDeclaration
    ;

methodOrFieldDecl
    : type Identifier methodOrFieldRest
    ;

methodOrFieldRest
    : fieldDeclaratorsRest SEMI
    | methodDeclaratorRest
    ;

fieldDeclaratorsRest
    : variableDeclaratorRest (COMMA variableDeclarator)*
    ;

methodDeclaratorRest
    : formalParameters (L_BRACKET R_BRACKET)* (THROWS qualifiedIdentifierList)? (block | SEMI)
    ;

voidMethodDeclaratorRest
    : formalParameters (THROWS qualifiedIdentifierList)? (block | SEMI)
    ;

constructorDeclaratorRest
    : formalParameters (THROWS qualifiedIdentifierList)? block
    ;

genericMethodOrConstructorDecl
    : typeParameters genericMethodOrConstructorRest
    ;

genericMethodOrConstructorRest
    : (type | VOID) Identifier methodDeclaratorRest
    | Identifier constructorDeclaratorRest
    ;

interfaceBody
    : L_CURLY (interfaceBodyDeclaration)* R_CURLY
    ;

interfaceBodyDeclaration
    : SEMI
    | (modifier)* interfaceMemberDecl
    ;

interfaceMemberDecl
    : interfaceMethodOrFieldDecl
    | VOID Identifier voidInterfaceMethodDeclaratorRest
    | interfaceGenericMethodDecl
    | classDeclaration
    | interfaceDeclaration
    ;

interfaceMethodOrFieldDecl
    : type Identifier interfaceMethodOrFieldRest
    ;

interfaceMethodOrFieldRest
    : constantDeclaratorsRest SEMI
    | interfaceMethodDeclaratorRest
    ;

constantDeclaratorsRest
    : constantDeclaratorRest (COMMA constantDeclarator)*
    ;

constantDeclaratorRest
    : (L_BRACKET R_BRACKET)* ASSIGN variableInitializer
    ;

constantDeclarator
    : Identifier constantDeclaratorRest
    ;

interfaceMethodDeclaratorRest
    : formalParameters (L_BRACKET R_BRACKET)* (THROWS qualifiedIdentifierList)? SEMI
    ;

voidInterfaceMethodDeclaratorRest
    : formalParameters (THROWS qualifiedIdentifierList)? SEMI
    ;

interfaceGenericMethodDecl
    : typeParameters (type | VOID) Identifier interfaceMethodDeclaratorRest
    ;

formalParameters
    : L_PARENS (formalParameterDecls)? R_PARENS
    ;

formalParameterDecls
    : (variableModifier)* type formalParameterDeclsRest
    ;

variableModifier
    : FINAL
    | annotation
    ;

formalParameterDeclsRest
    : variableDeclaratorId (COMMA formalParameterDecls)?
    | ELLIPSES variableDeclaratorId
    ;

variableDeclaratorId
    : Identifier (L_BRACKET R_BRACKET)*
    ;

variableDeclarators
    : variableDeclarator (COMMA variableDeclarator)*
    ;

variableDeclarator
    : Identifier variableDeclaratorRest
    ;

variableDeclaratorRest
    : (L_BRACKET R_BRACKET)* (ASSIGN variableInitializer)?
    ;

variableInitializer
    : arrayInitializer
    | expression
    ;

arrayInitializer
    : L_CURLY (variableInitializer (COMMA variableInitializer)* (COMMA)?)? R_CURLY
    ;

block
    : L_CURLY blockStatements R_CURLY
    ;

blockStatements
    : (blockStatement)*
    ;

blockStatement
    : localVariableDeclarationStatement
    | classOrInterfaceDeclaration
    | (Identifier COLON)? statement
    ;
    
localVariableDeclarationStatement
    : localVariableDeclaration SEMI
    ;
    
localVariableDeclaration
    : (variableModifier)* type variableDeclarators
    ;

statement
    : block
    | SEMI
    | Identifier COLON statement
    | expression SEMI
    | IF parExpression statement (ELSE statement)?
    | ASSERT expression (COLON expression)? SEMI
    | SWITCH parExpression L_CURLY switchBlockStatementGroups R_CURLY
    | WHILE parExpression statement
    | DO statement WHILE parExpression SEMI
    | FOR L_PARENS forControl R_PARENS statement
    | BREAK (Identifier)? SEMI
    | CONTINUE (Identifier)? SEMI
    | RETURN (expression)? SEMI
    | THROW expression SEMI
    | SYNCHRONIZED parExpression block
    | TRY block (catches | (catches)? finall)
    | TRY resourceSpecification block (catches)? (finall)?
    ;

catches
    : catchClause (catchClause)*
    ;

catchClause
    : CATCH L_PARENS (variableModifier)* catchType Identifier R_PARENS block
    ;

catchType
    : qualifiedIdentifier (B_OR qualifiedIdentifier)*
    ;

finall
    : FINALLY block
    ;

resourceSpecification
    : L_PARENS resources (SEMI)? R_PARENS
    ;

resources
    : resource (SEMI resource)*
    ;

resource
    : (variableModifier)* referenceType variableDeclaratorId ASSIGN expression
    ;

switchBlockStatementGroups
    : (switchBlockStatementGroup)*
    ;

switchBlockStatementGroup
    : switchLabels blockStatements
    ;

switchLabels
    : switchLabel (switchLabel)*
    ;

switchLabel
    : CASE expression COLON
    | CASE enumConstantName COLON
    | DEFAULT COLON
    ;

enumConstantName
    : Identifier
    ;

forControl
    : forVarControl
    | (forInit)? SEMI (expression)? SEMI (forUpdate)?
    ;

forVarControl
    : (variableModifier)* type variableDeclaratorId COLON expression
    ;

forInit
    : localVariableDeclaration
    | expressionList
    ;

forUpdate
    : expressionList
    ;
    
expressionList
    : expression (COMMA expression)*
    ;
    
expression
    : primary
    | expression selector
    | expression arguments
    | L_PARENS type R_PARENS expression
    | expression postfixOp
    | prefixOp expression
    | expression infixOp expression
    | expression INSTANCEOF type
    | expression QUES expression COLON expression
    | expression
      (   ASSIGN<assoc=right>
      |   PLUS_ASSIGN<assoc=right>
      |   MINUS_ASSIGN<assoc=right>
      |   MULT_ASSIGN<assoc=right>
      |   DIV_ASSIGN<assoc=right>
      |   AND_ASSIGN<assoc=right>
      |   OR_ASSIGN<assoc=right>
      |   XOR_ASSIGN<assoc=right>
      |   L_SHIFT_ASSIGN<assoc=right>
      |   R_SHIFT_ASSIGN<assoc=right>
      |   UR_SHIFT_ASSIGN<assoc=right>
      |   MOD_ASSIGN<assoc=right>
      )
      expression
    ;

infixOp
    : L_OR
    | L_AND
    | B_OR
    | XOR
    | B_AND
    | EQ
    | NEQ
    | LT
    | LE
    | GE
    | L_SHIFT
    | GT GT? GT?
    | PLUS
    | MINUS
    | MULT
    | DIV
    | MOD
    ;

prefixOp
    : INCREMENT
    | DECREMENT
    | BANG
    | TILD
    | PLUS
    | MINUS
    ;

postfixOp
    : INCREMENT
    | DECREMENT
    ;

primary
    : literal
    | parExpression
    | THIS (arguments)?
    | SUPER superSuffix
    | NEW creator
    | nonWildcardTypeArguments (explicitGenericInvocationSuffix | THIS arguments)
    | Identifier
    | type DOT CLASS
    | VOID DOT CLASS
    ;

literal
    : IntegerLiteral
    | FloatingPointLiteral
    | CharacterLiteral
    | StringLiteral
    | BooleanLiteral
    | NullLiteral
    ;

parExpression
    : L_PARENS expression R_PARENS
    ;

arguments
    : L_PARENS (expression (COMMA expression)*)? R_PARENS
    ;

superSuffix
    : arguments
    | DOT Identifier (arguments)?
    ;

explicitGenericInvocationSuffix
    : SUPER superSuffix
    | Identifier arguments
    ;

creator
    : nonWildcardTypeArguments createdName classCreatorRest
    | createdName (classCreatorRest | arrayCreatorRest)
    | basicType arrayCreatorRest
    ;

createdName
    : Identifier (typeArgumentsOrDiamond)? (DOT Identifier (typeArgumentsOrDiamond)?)*
    ;

classCreatorRest
    : arguments (classBody)?
    ;

arrayCreatorRest
    : L_BRACKET (R_BRACKET (L_BRACKET R_BRACKET)* arrayInitializer | expression R_BRACKET (L_BRACKET expression R_BRACKET)* (L_BRACKET R_BRACKET)*)
    ;

identifierSuffix
    : (((L_BRACKET R_BRACKET)* DOT CLASS | expression))?
    | arguments
    | DOT (CLASS | explicitGenericInvocation | THIS | SUPER arguments | NEW (nonWildcardTypeArguments)? innerCreator)
    ;

explicitGenericInvocation
    : nonWildcardTypeArguments explicitGenericInvocationSuffix
    ;

innerCreator
    : Identifier (nonWildcardTypeArgumentsOrDiamond)? classCreatorRest
    ;

selector
    : DOT Identifier (arguments)?
    | DOT explicitGenericInvocation
    | DOT THIS
    | DOT SUPER superSuffix
    | DOT NEW (nonWildcardTypeArguments)? innerCreator
    | L_BRACKET expression R_BRACKET
    ;

enumBody
    : L_CURLY (enumConstants)? (COMMA)? (enumBodyDeclarations)? R_CURLY
    ;

enumConstants
    : enumConstant
    | enumConstants COMMA enumConstant
    ;

enumConstant
    : (annotations)? Identifier (arguments)? (classBody)?
    ;

enumBodyDeclarations
    : SEMI (classBodyDeclaration)*
    ;

annotationTypeBody
    : L_CURLY (annotationTypeElementDeclarations)? R_CURLY
    ;

annotationTypeElementDeclarations
    : annotationTypeElementDeclaration
    | annotationTypeElementDeclarations annotationTypeElementDeclaration
    ;

annotationTypeElementDeclaration
    : (modifier)* annotationTypeElementRest
    ;

annotationTypeElementRest
    : type Identifier annotationMethodOrConstantRest SEMI
    | classDeclaration
    | interfaceDeclaration
    | enumDeclaration
    | annotationTypeDeclaration
    ;

annotationMethodOrConstantRest
    : annotationMethodRest
    | constantDeclaratorsRest
    ;

annotationMethodRest
    : L_PARENS R_PARENS (L_BRACKET R_BRACKET)? (DEFAULT elementValue)?
    ;