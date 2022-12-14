parser grammar XMLParser;
options { tokenVocab=XMLLexer; }

document    :   prolog? elements;

elements    :(misc* element misc*)+;

prolog      :   XMLDeclOpen attribute* SPECIAL_CLOSE ;

content     :   chardata? ((element|reference | CDATA | PI | COMMENT) chardata?)*
            ;

element     :   '<' Name attribute* '>' content '<' '/' Name '>'
            |   '<' Name attribute* '/>'
            ;

reference   :   EntityRef | CharRef ;

attribute   :   Name '=' STRING ; // Our STRING is AttValue in spec
/** ``All text that is not markup constitutes the character data of
 *  the document.''
 */
chardata    :   TEXT #Use
            | SEA_WS #UNUse
            ;

misc        :   COMMENT | PI | SEA_WS ;

