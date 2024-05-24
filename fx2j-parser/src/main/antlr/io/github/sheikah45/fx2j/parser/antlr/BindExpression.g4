grammar BindExpression;
@header {
package io.github.sheikah45.fx2j.parser.antlr;
}

expression
    :    'null'                                                                                     # nullLiteral
    |    value=('true'|'false')                                                                     # booleanLiteral
    |    String                                                                                     # stringLiteral
    |    Integer                                                                                    # wholeLiteral
    |    Fraction                                                                                   # fractionLiteral
    |    Identifier                                                                                 # variable
    |    '(' inside=expression ')'                                                                  # enclosed
    |    base=expression '.' property=Identifier                                                    # propertyRead
    |    base=expression '.' method=Identifier '('(args+=expression (',' args+=expression)*)?')'    # methodCall
    |    collection=expression '[' accessor=expression ']'                                          # collectionAccess
    |    '-' base=expression                                                                        # negate
    |    left=expression operator=('*'|'/'|'%') right=expression                                    # multiplicative
    |    left=expression operator=('+'|'-') right=expression                                        # additive
    |    left=expression operator=('>'|'>='|'<'|'<='|'=='|'!=') right=expression                    # comparative
    |    '!' base=expression                                                                        # invert
    |    left=expression operator=('&&'|'||') right=expression                                      # logical
    ;


Integer: [0-9]+;
Fraction
    : Integer '.' Integer? ExponentPart?
    | '.' Integer ExponentPart?
    | Integer ExponentPart
    ;
String
    : '"' StringCharacters? '"'
    | '\'' StringCharacters? '\''
    | '"' String? '"'
    | '\'' String? '\''
    ;
Identifier: [a-zA-Z$_][a-zA-Z0-9$_]*;
ExponentPart: [eE][+-]?Integer;
Whitespace: [ \n\t\r]+ -> skip;

fragment
StringCharacters: ~["'\\]+;
