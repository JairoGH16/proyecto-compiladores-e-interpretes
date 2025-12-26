package parserlexer;
import java_cup.runtime.*;
/* Analizador Léxico para lenguaje de configuración de chips */

%%

%class Lexer
%public
%unicode
%cup
%line
%column

%{
  StringBuffer string = new StringBuffer();

  private Symbol symbol(int type) {
    return new Symbol(type, yyline + 1, yycolumn + 1);
  }
  private Symbol symbol(int type, Object value) {
    return new Symbol(type, yyline + 1, yycolumn + 1, value);
  }
%}

/* expresiones regulares básicas */
LineTerminator = \r|\n|\r\n
WhiteSpace     = {LineTerminator} | [ \t\f]

/* comentarios */
LineComment    = "|" [^\r\n]*
MultiLineComment = "є" ~"э"

/* identificadores */
Identifier = [a-zA-Z_][a-zA-Z0-9_]*

/* literales Numéricos */
IntegerLiteral = [0-9]+
FloatLiteral   = [0-9]+ \. [0-9]+

/* estado para capturar cadenas de texto */
%state STRING

/* estado para capturar errores en una linea (modo pánico) */
%state PANIC_MODE

/* estado para capturar comentarios multilinea */
%state MULTICOMMENT

%%

<YYINITIAL> {
/* ----- Palabras reservadas ------------------------- */
/* Variables */
    "world"      { return symbol(sym.WORLD); }
    "local"      { return symbol(sym.LOCAL); }
/* Tipos de datos */
    "int"        { return symbol(sym.INT); }
    "float"      { return symbol(sym.FLOAT); }
    "bool"       { return symbol(sym.BOOL); }
    "char"       { return symbol(sym.CHAR); }
    "string"     { return symbol(sym.STRING); }
 /* Main y funciones */
    "navidad"    { return symbol(sym.NAVIDAD); }
    "coal"       { return symbol(sym.COAL); }
    "gift"       { return symbol(sym.GIFT); }
/* Estructuras de control */
    "decide"     { return symbol(sym.DECIDE); }
    "of"         { return symbol(sym.OF); }
    "else"       { return symbol(sym.ELSE); }
    "end"        { return symbol(sym.END); }
    "loop"       { return symbol(sym.LOOP); }
    "exit"       { return symbol(sym.EXIT); }
    "when"       { return symbol(sym.WHEN); }
    "for"        { return symbol(sym.FOR); }
/* Control de flujo */
    "return"     { return symbol(sym.RETURN); }
    "break"      { return symbol(sym.BREAK); }
    /* Entrada/Salida */
    "show"       { return symbol(sym.SHOW); }
    "get"        { return symbol(sym.GET); }
/* Delimitador de fin de instrucción */
    "endl"       { return symbol(sym.ENDL); }
/* Literales booleanos */
    "true"       { return symbol(sym.TRUE); }
    "false"      { return symbol(sym.FALSE); }

/* ----- Operadores ------------------------------ */
/* Bloques (reemplazan llaves) */
    "¡"          { return symbol(sym.OPEN_BLOCK); }
    "!"          { return symbol(sym.CLOSE_BLOCK); }
/* Paréntesis (reemplazan paréntesis tradicionales) */
    "¿"          { return symbol(sym.OPEN_PAREN); }
    "?"          { return symbol(sym.CLOSE_PAREN); }
/* Corchetes para arreglos */
    "["          { return symbol(sym.OPEN_BRACKET); }
    "]"          { return symbol(sym.CLOSE_BRACKET); }
/* Operadores aritméticos */
    "++"         { return symbol(sym.INCREMENT); }
    "--"         { return symbol(sym.DECREMENT); }
    "+"          { return symbol(sym.PLUS); }
    "-"          { return symbol(sym.MINUS); }
    "*"          { return symbol(sym.MULT); }
    "//"         { return symbol(sym.INT_DIV); }
    "/"          { return symbol(sym.DIV); }
    "%"          { return symbol(sym.MOD); }
    "^"          { return symbol(sym.POWER); }
/* Operadores relacionales */
    "<="         { return symbol(sym.LTEQ); }
    ">="         { return symbol(sym.GTEQ); }
    "=="         { return symbol(sym.EQEQ); }
    "Σ="         { return symbol(sym.NEQ); }
    "<"          { return symbol(sym.LT); }
    ">"          { return symbol(sym.GT); }
/* Operadores lógicos */
    "@"          { return symbol(sym.AND); }
    "~"          { return symbol(sym.OR); }
    "Σ"          { return symbol(sym.NOT); }
/* Asignación */
    "="          { return symbol(sym.ASSIGN); }
/* Flecha (para decide of) */
    "->"         { return symbol(sym.ARROW); }
/* Separadores */
    ","          { return symbol(sym.COMMA); }
    ";"          { return symbol(sym.SEMICOLON); }

/* ----- Literales ------------------------------ */
/* Números */
    {FloatLiteral}    { return symbol(sym.FLOAT_LITERAL, 
                               Float.parseFloat(yytext())); }
    {IntegerLiteral}  { return symbol(sym.INT_LITERAL, 
                               Integer.parseInt(yytext())); } 
/* Caracteres individuales */
    \'[^\']\'         { return symbol(sym.CHAR_LITERAL, 
                               yytext().charAt(1)); }
/* Cadenas de texto */
    \"                { string.setLength(0); string.append('\"'); yybegin(STRING); }
/* Identificadores */
    {Identifier}      { return symbol(sym.ID, yytext()); }

/* ----- Comentarios y espacios ------------------------ */
    {WhiteSpace}      { /* Ignorar */ }
    {LineComment}     { /* Ignorar */ }
    "є"               { yybegin(MULTICOMMENT); }
}

<STRING> {
    \"                             { string.append('\"'); yybegin(YYINITIAL); 
                                   return symbol(sym.STRING_LITERAL, 
                                   string.toString()); }
    [^\n\r\"\\]+                   { string.append( yytext() ); }
    \\t                            { string.append('\t'); }
    \\n                            { string.append('\n'); }
    \\r                            { string.append('\r'); }
    \\\"                           { string.append('\"'); }
    \\                             { string.append('\\'); }
}

/* Comentarios multilinea */
<MULTICOMMENT> {
    "э"               { yybegin(YYINITIAL); }
    [^\r\nэ]+         { /* ignorar */ }
    {LineTerminator}  { /* ignorar */ }
    <<EOF>>           { yybegin(YYINITIAL); return symbol(sym.ERROR, "Comentario multilinea no cerrado"); }
}

/* error fallback, maneja errores lexicos causados por elementos desconocidos y continua en la linea siguiente (modo pánico) */
<YYINITIAL> [^] { 
    String elementoDesconocido = yytext();
    System.err.println("Error: Elemento desconocido <" + elementoDesconocido + "> en la línea " + (yyline + 1));
    yybegin(PANIC_MODE); 
    return symbol(sym.ERROR, elementoDesconocido);
}
/* En el estado PANIC_MODE se consumen todos los caracteres hasta el final de la linea */
<PANIC_MODE> [^\r\n]+ { /* ignorar */ }
<PANIC_MODE> {LineTerminator} { yybegin(YYINITIAL); }