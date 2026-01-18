package parserlexer;
import java_cup.runtime.*;

/*
 * Analizador Léxico para lenguaje de configuración de chips
 * Objetivo: Definir las reglas léxicas (expresiones regulares) que JFlex usará para generar la clase Lexer.java.
 *           Este archivo se encarga de reconocer todos los tokens válidos del lenguaje.
 * Entrada: Archivo de texto con código fuente en el lenguaje personalizado.
 * Salida: Tokens de tipo Symbol (definidos en sym.java) para cada lexema encontrado.
 * Restricciones: Requiere codificación UTF-8 para reconocer caracteres especiales como 'Σ', '¡', '¿', 'є', 'э'.
 */

%%

%class Lexer
%public
%unicode
%cup
%line
%column

%{
  // Buffer para ir construyendo strings literales caracter por caracter cuando estamos en el estado STRING
  StringBuffer string = new StringBuffer();

  /*
   * Método symbol (sin valor)
   * Objetivo: Crear un objeto Symbol para tokens simples (palabras reservadas, operadores).
   * Entrada: type (ID numérico del token).
   * Salida: Objeto Symbol con la línea y columna ajustadas (base 1 para lectura humana).
   */
  private Symbol symbol(int type) {
    return new Symbol(type, yyline + 1, yycolumn + 1);
  }
  
  /*
   * Método symbol (con valor)
   * Objetivo: Crear un objeto Symbol para tokens complejos que llevan datos asociados (literales, IDs).
   * Entrada: type (ID numérico del token), value (valor del lexema parseado).
   * Salida: Objeto Symbol con tipo, valor, línea y columna.
   */
  private Symbol symbol(int type, Object value) {
    return new Symbol(type, yyline + 1, yycolumn + 1, value);
  }
%}

/* ----- Definiciones de Expresiones Regulares Básicas ----- */
LineTerminator = \r|\n|\r\n
WhiteSpace     = {LineTerminator} | [ \t\f]

/* Comentarios: de una línea (|) y multilínea (empieza con є y termina con э) */
LineComment    = "|" [^\r\n]*

/* Identificadores: empiezan con letra/guion bajo, siguen letras/numeros/guion bajo */
Identifier = [a-zA-Z_][a-zA-Z0-9_]*

/* Literales numéricos: Enteros y Flotantes (con punto decimal obligatorio) */
FloatLiteral = ([0-9]+ "." [0-9]+)

IntegerLiteral = 0 | [1-9][0-9]*


/*
 * Estados léxicos:
 * - YYINITIAL: Estado por defecto.
 * - STRING: Para procesar cadenas de texto y sus escapes.
 * - PANIC_MODE: Para ignorar el resto de la línea tras un error.
 * - MULTICOMMENT: Para procesar bloques de comentarios multilínea.
 */
%state STRING
%state PANIC_MODE
%state MULTICOMMENT

%%

<YYINITIAL> {
/* 
 * ----- Palabras Reservadas ----- 
 * Objetivo: Identificar keywords del lenguaje.
 * Se definen antes que los identificadores para tener prioridad.
 */

/* Variables y alcance */
    "world"      { return symbol(sym.WORLD); }    // Globales
    "local"      { return symbol(sym.LOCAL); }    // Locales

/* Tipos de datos */
    "int"        { return symbol(sym.INT); }
    "float"      { return symbol(sym.FLOAT); }
    "bool"       { return symbol(sym.BOOL); }
    "char"       { return symbol(sym.CHAR); }
    "string"     { return symbol(sym.STRING); }

/* Estructura del programa y funciones */
    "navidad"    { return symbol(sym.NAVIDAD); }  // main
    "coal"       { return symbol(sym.COAL); }     // void
    "gift"       { return symbol(sym.GIFT); }     // function

/* Control de flujo */
    "decide"     { return symbol(sym.DECIDE); }   // if
    "of"         { return symbol(sym.OF); }       // parte del if
    "else"       { return symbol(sym.ELSE); }
    "end"        { return symbol(sym.END); }      // cierre genérico
    "loop"       { return symbol(sym.LOOP); }     // bucle infinito
    "exit"       { return symbol(sym.EXIT); }     // break condicional del loop
    "when"       { return symbol(sym.WHEN); }     // condición del exit
    "for"        { return symbol(sym.FOR); }      // bucle for

/* Saltos */
    "return"     { return symbol(sym.RETURN); }
    "break"      { return symbol(sym.BREAK); }

/* Entrada / Salida */
    "show"       { return symbol(sym.SHOW); }     // print
    "get"        { return symbol(sym.GET); }      // read

/* Fin de instrucción (reemplaza al punto y coma a veces en este lenguaje) */
    "endl"       { return symbol(sym.ENDL); }

/* Literales booleanos */
    "true"       { return symbol(sym.TRUE); }
    "false"      { return symbol(sym.FALSE); }

/* 
 * ----- Operadores y Delimitadores -----
 * Caracteres especiales definidos en el enunciado (UTF-8).
 */

/* Bloques de código (reemplazan { y }) */
    "¡"          { return symbol(sym.OPEN_BLOCK); }
    "!"          { return symbol(sym.CLOSE_BLOCK); }

/* Paréntesis de agrupación (reemplazan ( y )) */
    "¿"          { return symbol(sym.OPEN_PAREN); }
    "?"          { return symbol(sym.CLOSE_PAREN); }

/* Corchetes de arreglos */
    "["          { return symbol(sym.OPEN_BRACKET); }
    "]"          { return symbol(sym.CLOSE_BRACKET); }

/* Aritmética */
    "++"         { return symbol(sym.INCREMENT); }
    "--"         { return symbol(sym.DECREMENT); }
    "+"          { return symbol(sym.PLUS); }
    "-"          { return symbol(sym.MINUS); }
    "*"          { return symbol(sym.MULT); }
    "//"         { return symbol(sym.INT_DIV); }  // División Entera
    "/"          { return symbol(sym.DIV); }      // División Decimal
    "%"          { return symbol(sym.MOD); }
    "^"          { return symbol(sym.POWER); }

/* Relacionales */
    "<="         { return symbol(sym.LTEQ); }
    ">="         { return symbol(sym.GTEQ); }
    "=="         { return symbol(sym.EQEQ); }
    "Σ="         { return symbol(sym.NEQ); }      // Diferente de
    "<"          { return symbol(sym.LT); }
    ">"          { return symbol(sym.GT); }

/* Lógicos */
    "@"          { return symbol(sym.AND); }      // Conjunción
    "~"          { return symbol(sym.OR); }       // Disyunción
    "Σ"          { return symbol(sym.NOT); }      // Negación

/* Asignación y otros símbolos */
    "="          { return symbol(sym.ASSIGN); }
    "->"         { return symbol(sym.ARROW); }    // Para el decide of
    ","          { return symbol(sym.COMMA); }
    ";"          { return symbol(sym.SEMICOLON); }

/* 
 * ----- Literales y Valores -----
 */

/* Flotantes */
{FloatLiteral} {
    return symbol(sym.FLOAT_LITERAL, Double.parseDouble(yytext()));
}

/* Enteros */
{IntegerLiteral} {
    return symbol(sym.INT_LITERAL, Integer.parseInt(yytext()));
}

/* Char literal: captura un solo caracter entre comillas simples */
    \'[^\']\'         { return symbol(sym.CHAR_LITERAL, 
                               yytext().charAt(1)); }

/* 
 * String literal: detecta comilla doble inicial.
 * Acción: Limpia el buffer, añade la comilla inicial y cambia al estado STRING.
 */
    \"                { string.setLength(0); 
                        string.append('\"'); 
                        yybegin(STRING); }

/* Identificadores de usuario */
    {Identifier}      { return symbol(sym.ID, yytext()); }

/* 
 * ----- Espacios y Comentarios -----
 * Acción: No retornar nada, simplemente consumir la entrada.
 */
    {WhiteSpace}      { /* Ignorar */ }
    {LineComment}     { /* Ignorar */ }
    
/* Inicio de comentario multilínea: cambia al estado MULTICOMMENT */
    "є"               { yybegin(MULTICOMMENT); }
}

/*
 * Estado STRING
 * Objetivo: Capturar el contenido de una cadena de texto, manejando secuencias de escape.
 * Entrada: Caracteres dentro de las comillas.
 * Salida: Token STRING_LITERAL una vez cerrada la comilla.
 */
<STRING> {
    /* Cierre de comillas: terminar string y volver a YYINITIAL */
    \"                             { string.append('\"'); 
                                     yybegin(YYINITIAL); 
                                     return symbol(sym.STRING_LITERAL, 
                                                   string.toString()); }
    
    /* Caracteres normales: agregar al buffer */
    [^\n\r\"\\]+                   { string.append(yytext()); }
    
    /* Secuencias de escape: convertir y agregar al buffer */
    \\t                            { string.append('\t'); }
    \\n                            { string.append('\n'); }
    \\r                            { string.append('\r'); }
    \\\"                           { string.append('\"'); }
    \\                             { string.append('\\'); }
}

/*
 * Estado MULTICOMMENT
 * Objetivo: Consumir todo el texto hasta encontrar el símbolo de cierre 'э'.
 * Restricción: Debe reportar error si se acaba el archivo (EOF) sin cerrar.
 */
<MULTICOMMENT> {
    "э"               { yybegin(YYINITIAL); }
    [^э\r\n]+         { /* ignorar */ }
    {LineTerminator}  { /* ignorar */ }

    <<EOF>> {
        yybegin(YYINITIAL);
        return symbol(sym.ERROR, "Comentario multilinea no cerrado");
    }
}

/*
 * Manejo de Errores (Recuperación en Modo Pánico)
 * Regla [^]: Captura cualquier caracter que no haya hecho match con las reglas anteriores.
 * Objetivo: Reportar el error léxico y saltar al final de la línea actual para intentar 
 *           recuperarse en la siguiente línea.
 */
<YYINITIAL> [^] { 
    String elementoDesconocido = yytext();
    System.err.println("Error: Elemento desconocido <" + elementoDesconocido + 
                       "> en la línea " + (yyline + 1));
    yybegin(PANIC_MODE); 
    return symbol(sym.ERROR, elementoDesconocido);
}

/*
 * Estado PANIC_MODE
 * Objetivo: Consumir el resto de la línea donde ocurrió el error.
 */
<PANIC_MODE> [^\r\n]+ { /* consumir basura de la línea actual */ }
<PANIC_MODE> {LineTerminator} { yybegin(YYINITIAL); /* al cambiar de línea, reiniciar */ }