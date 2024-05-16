grammar BindExpression;
@header {
package io.github.sheikah45.fx2j.parser.antlr;
}

expression
    :    base=expression '.' property=Identifier                    # propertyRead
    |    base=expression '.' method=Identifier
             '('
                 (args+=expression (',' args+=expression)*)?
             ')'                                                    # methodCall
    |    collection=expression '[' accessor=expression ']'          # collectionAccess
    |    '!' base=expression                                        # invert
    |    '-' base=expression                                        # negate
    |    left=expression '*' right=expression                       # multiply
    |    left=expression '/' right=expression                       # divide
    |    left=expression '%' right=expression                       # remainder
    |    left=expression '+' right=expression                       # add
    |    left=expression '-' right=expression                       # subtract
    |    left=expression '>' right=expression                       # greaterThan
    |    left=expression '>=' right=expression                      # greaterThanEqual
    |    left=expression '<' right=expression                       # lessThan
    |    left=expression '<=' right=expression                      # lessThanEqual
    |    left=expression '==' right=expression                      # equality
    |    left=expression '!=' right=expression                      # inequality
    |    left=expression '&&' right=expression                      # and
    |    left=expression '||' right=expression                      # or
    |    '(' inside=expression ')'                                  # enclosed
    |    Identifier                                                 # variable
    |    String                                                     # stringLiteral
    |    Decimal                                                    # decimalLiteral
    |    Fractional                                                 # fractionalLiteral
    |    'null'                                                     # nullLiteral
    |    'true'                                                     # trueLiteral
    |    'false'                                                    # falseLiteral
    ;

String
    :	 ('"' StringCharacters? '"')
    |    ('\'' StringCharacters? '\'')
    ;

Decimal
    :	[0-9]+
    ;

Fractional
	:	Decimal '.' Decimal? ExponentPart?
	|	'.' Decimal ExponentPart?
	|	Decimal ExponentPart
	|	Decimal
	;

Identifier
    :	[a-zA-Z][a-zA-Z0-9]*
    ;

fragment
StringCharacters
    :   ~["'\\]+
    ;

fragment
ExponentPart
	:	[eE][+-]?Decimal
	;

Whitespace: [ \n\t\r]+ -> skip;