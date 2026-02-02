import java.util.ArrayList;

/**
 * Clase GeneradorCodigoIntermedio
 * Objetivo: Generar código intermedio (Three-Address Code) a partir del árbol sintáctico.
 * Produce instrucciones en formato de tres direcciones que sirven como representación intermedia
 * entre el código fuente y el código ensamblador MIPS.
 * Restricciones: Requiere una tabla de símbolos válida para resolver tipos y alcances.
 */
public class GeneradorCodigoIntermedio {
    // Lista que almacena todas las instrucciones de código intermedio generadas
    private ArrayList<String> codigo;
    
    // Contador para generar nombres únicos de variables temporales (t0, t1, t2, ...)
    private int contadorTemporales;
    
    // Contador para generar nombres únicos de etiquetas (L0, L1, L2, ...)
    private int contadorEtiquetas;
    
    // Referencia a la tabla de símbolos para consultar información de variables y funciones
    private TablaSimbolos tablaSimbolos;
    
    /**
     * Constructor de GeneradorCodigoIntermedio
     * Objetivo: Inicializar el generador con una tabla de símbolos y preparar las estructuras necesarias.
     * Entrada: TablaSimbolos - tabla de símbolos construida durante el análisis semántico.
     * Salida: Instancia del generador lista para generar código intermedio.
     */
    public GeneradorCodigoIntermedio(TablaSimbolos tablaSimbolos) {
        this.codigo = new ArrayList<>();
        this.contadorTemporales = 0;
        this.contadorEtiquetas = 0;
        this.tablaSimbolos = tablaSimbolos;
    }
    
    // ==================== GENERADORES DE TEMPORALES Y ETIQUETAS ====================
    
    /**
     * Método nuevoTemporal
     * Objetivo: Generar un nombre único para una variable temporal.
     * Entrada: Ninguna.
     * Salida: String con el nombre del temporal (ej: "t0", "t1", "t2").
     */
    public String nuevoTemporal() {
        return "t" + (contadorTemporales++);
    }
    
    /**
     * Método nuevaEtiqueta
     * Objetivo: Generar un nombre único para una etiqueta de salto.
     * Entrada: Ninguna.
     * Salida: String con el nombre de la etiqueta (ej: "L0", "L1", "L2").
     */
    public String nuevaEtiqueta() {
        return "L" + (contadorEtiquetas++);
    }
    
    /**
     * Método agregarInstruccion
     * Objetivo: Agregar una instrucción de código intermedio a la lista.
     * Entrada: String - la instrucción a agregar.
     * Salida: Ninguna (modifica la lista interna de código).
     */
    public void agregarInstruccion(String instruccion) {
        codigo.add(instruccion);
    }
    
    /**
     * Método agregarEtiqueta
     * Objetivo: Agregar una etiqueta de salto al código intermedio.
     * Entrada: String - nombre de la etiqueta.
     * Salida: Ninguna (agrega "etiqueta:" a la lista de código).
     */
    public void agregarEtiqueta(String etiqueta) {
        codigo.add(etiqueta + ":");
    }
    
    // ==================== GENERACIÓN DE CÓDIGO ====================
    
    /**
     * Método generarAsignacion
     * Objetivo: Generar una instrucción de asignación simple (variable = valor).
     * Entrada: String variable - nombre de la variable destino.
     *          String temporal - valor o temporal a asignar.
     * Salida: Ninguna (agrega instrucción "variable = temporal").
     */
    public void generarAsignacion(String variable, String temporal) {
        agregarInstruccion(variable + " = " + temporal);
    }
    
    /**
     * Método generarOperacionBinaria
     * Objetivo: Generar una instrucción para operación binaria (suma, resta, multiplicación, etc.).
     * Entrada: String operando1 - primer operando.
     *          String operador - operador (+, -, *, /, //, %, etc.).
     *          String operando2 - segundo operando.
     * Salida: String - nombre del temporal que contiene el resultado.
     * Restricciones: Convierte '//' a 'DIV_ENTERA' y '/' a 'DIV_DECIMAL' para MIPS.
     */
    public String generarOperacionBinaria(String operando1, String operador, String operando2) {
        String temp = nuevoTemporal();
        
        // Convertir operador de división para que MIPS lo reconozca correctamente
        if (operador.equals("//")) {
            agregarInstruccion(temp + " = " + operando1 + " DIV_ENTERA " + operando2);
        } else if (operador.equals("/")) {
            agregarInstruccion(temp + " = " + operando1 + " DIV_DECIMAL " + operando2);
        } else {
            agregarInstruccion(temp + " = " + operando1 + " " + operador + " " + operando2);
        }
        
        return temp;
    }
    
    /**
     * Método generarOperacionUnaria
     * Objetivo: Generar una instrucción para operación unaria (negación, not lógico, etc.).
     * Entrada: String operador - operador unario (-, !, etc.).
     *          String operando - operando sobre el que se aplica el operador.
     * Salida: String - nombre del temporal que contiene el resultado.
     */
    public String generarOperacionUnaria(String operador, String operando) {
        String temp = nuevoTemporal();
        agregarInstruccion(temp + " = " + operador + " " + operando);
        return temp;
    }
    
    /**
     * Método generarAccesoArray
     * Objetivo: Generar instrucción para acceder a un elemento de array unidimensional.
     * Entrada: String array - nombre del array.
     *          String indice - índice del elemento a acceder.
     * Salida: String - nombre del temporal que contiene el valor leído.
     */
    public String generarAccesoArray(String array, String indice) {
        String temp = nuevoTemporal();
        agregarInstruccion(temp + " = " + array + "[" + indice + "]");
        return temp;
    }
    
    /**
     * Método generarAccesoArray2D
     * Objetivo: Generar instrucción para acceder a un elemento de array bidimensional.
     * Entrada: String array - nombre del array.
     *          String indice1 - índice de la fila.
     *          String indice2 - índice de la columna.
     * Salida: String - nombre del temporal que contiene el valor leído.
     */
    public String generarAccesoArray2D(String array, String indice1, String indice2) {
        String temp = nuevoTemporal();
        agregarInstruccion(temp + " = " + array + "[" + indice1 + "][" + indice2 + "]");
        return temp;
    }
    
    /**
     * Método generarAsignacionArray
     * Objetivo: Generar instrucción para asignar un valor a un elemento de array unidimensional.
     * Entrada: String array - nombre del array.
     *          String indice - índice del elemento.
     *          String valor - valor a asignar.
     * Salida: Ninguna (agrega instrucción "array[indice] = valor").
     */
    public void generarAsignacionArray(String array, String indice, String valor) {
        agregarInstruccion(array + "[" + indice + "] = " + valor);
    }
    
    /**
     * Método generarAsignacionArray2D
     * Objetivo: Generar instrucción para asignar un valor a un elemento de array bidimensional.
     * Entrada: String array - nombre del array.
     *          String indice1 - índice de la fila.
     *          String indice2 - índice de la columna.
     *          String valor - valor a asignar.
     * Salida: Ninguna (agrega instrucción "array[indice1][indice2] = valor").
     */
    public void generarAsignacionArray2D(String array, String indice1, String indice2, String valor) {
        agregarInstruccion(array + "[" + indice1 + "][" + indice2 + "] = " + valor);
    }
    
    /**
     * Método generarGoto
     * Objetivo: Generar una instrucción de salto incondicional.
     * Entrada: String etiqueta - etiqueta destino del salto.
     * Salida: Ninguna (agrega instrucción "goto etiqueta").
     */
    public void generarGoto(String etiqueta) {
        agregarInstruccion("goto " + etiqueta);
    }
    
    /**
     * Método generarIfGoto
     * Objetivo: Generar una instrucción de salto condicional (si condición es verdadera, salta).
     * Entrada: String condicion - expresión booleana a evaluar.
     *          String etiqueta - etiqueta destino si la condición es verdadera.
     * Salida: Ninguna (agrega instrucción "if condicion goto etiqueta").
     */
    public void generarIfGoto(String condicion, String etiqueta) {
        agregarInstruccion("if " + condicion + " goto " + etiqueta);
    }
    
    /**
     * Método generarIfFalseGoto
     * Objetivo: Generar una instrucción de salto condicional (si condición es falsa, salta).
     * Entrada: String condicion - expresión booleana a evaluar.
     *          String etiqueta - etiqueta destino si la condición es falsa.
     * Salida: Ninguna (agrega instrucción "ifFalse condicion goto etiqueta").
     */
    public void generarIfFalseGoto(String condicion, String etiqueta) {
        agregarInstruccion("ifFalse " + condicion + " goto " + etiqueta);
    }
    
    /**
     * Método generarLlamadaFuncion
     * Objetivo: Generar una instrucción de llamada a función.
     * Entrada: String nombreFuncion - nombre de la función a llamar.
     *          int numParams - número de parámetros de la función.
     * Salida: String - nombre del temporal que contendrá el valor de retorno.
     */
    public String generarLlamadaFuncion(String nombreFuncion, int numParams) {
        String temp = nuevoTemporal();
        agregarInstruccion(temp + " = call " + nombreFuncion + ", " + numParams);
        return temp;
    }
    
    /**
     * Método generarParam
     * Objetivo: Generar una instrucción para pasar un parámetro a una función.
     * Entrada: String valor - valor del parámetro a pasar.
     * Salida: Ninguna (agrega instrucción "param valor").
     */
    public void generarParam(String valor) {
        agregarInstruccion("param " + valor);
    }
    
    /**
     * Método generarReturn
     * Objetivo: Generar una instrucción de retorno de función.
     * Entrada: String valor - valor a retornar (puede ser null para return sin valor).
     * Salida: Ninguna (agrega instrucción "return" o "return valor").
     */
    public void generarReturn(String valor) {
        if (valor != null && !valor.isEmpty()) {
            agregarInstruccion("return " + valor);
        } else {
            agregarInstruccion("return");
        }
    }
    
    /**
     * Método generarInicioFuncion
     * Objetivo: Generar el encabezado de una función en el código intermedio.
     * Entrada: String nombreFuncion - nombre de la función.
     * Salida: Ninguna (agrega "FUNCTION nombreFuncion:").
     */
    public void generarInicioFuncion(String nombreFuncion) {
        agregarInstruccion("");
        agregarInstruccion("FUNCTION " + nombreFuncion + ":");
    }
    
    /**
     * Método generarFinFuncion
     * Objetivo: Generar el cierre de una función en el código intermedio.
     * Entrada: Ninguna.
     * Salida: Ninguna (agrega "END_FUNCTION").
     */
    public void generarFinFuncion() {
        agregarInstruccion("END_FUNCTION");
        agregarInstruccion("");
    }
    
    /**
     * Método generarDeclaracion
     * Objetivo: Generar una instrucción de declaración de variable.
     * Entrada: String variable - nombre de la variable.
     *          String tipo - tipo de la variable (int, float, bool, char, string).
     * Salida: Ninguna (agrega "DECLARE variable : tipo").
     */
    public void generarDeclaracion(String variable, String tipo) {
        agregarInstruccion("DECLARE " + variable + " : " + tipo);
    }
    
    /**
     * Método generarDeclaracionArray
     * Objetivo: Generar una instrucción de declaración de array.
     * Entrada: String variable - nombre del array.
     *          String tipo - tipo base de los elementos del array.
     *          String dimensiones - dimensiones del array (ej: "10" o "5x5").
     * Salida: Ninguna (agrega "DECLARE variable : tipo[dimensiones]").
     */
    public void generarDeclaracionArray(String variable, String tipo, String dimensiones) {
        agregarInstruccion("DECLARE " + variable + " : " + tipo + "[" + dimensiones + "]");
    }
    
    // ==================== OBTENER RESULTADOS ====================
    
    /**
     * Método getCodigo
     * Objetivo: Obtener la lista completa de instrucciones generadas.
     * Entrada: Ninguna.
     * Salida: ArrayList<String> - lista de instrucciones de código intermedio.
     */
    public ArrayList<String> getCodigo() {
        return codigo;
    }
    
    /**
     * Método getCodigoCompleto
     * Objetivo: Obtener el código intermedio formateado como String para visualización.
     * Entrada: Ninguna.
     * Salida: String - código intermedio formateado con indentación y separadores.
     */
    public String getCodigoCompleto() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n    ========== INICIO CÓDIGO INTERMEDIO ==========\n\n");
        
        // Formatear cada instrucción según su tipo
        for (String instruccion : codigo) {
            if (instruccion.isEmpty()) {
                sb.append("\n");
            } else if (instruccion.startsWith("FUNCTION") || instruccion.startsWith("END_FUNCTION")) {
                sb.append(instruccion).append("\n");
            } else if (instruccion.endsWith(":")) {
                sb.append(instruccion).append("\n");
            } else {
                sb.append("    ").append(instruccion).append("\n");
            }
        }
        
        sb.append("\n    ========== FIN CÓDIGO INTERMEDIO ==========\n");
        return sb.toString();
    }
    
    /**
     * Método imprimir
     * Objetivo: Imprimir el código intermedio en la consola.
     * Entrada: Ninguna.
     * Salida: Ninguna (imprime en System.out).
     */
    public void imprimir() {
        System.out.println(getCodigoCompleto());
    }
    
    /**
     * Método getNumInstrucciones
     * Objetivo: Obtener el número total de instrucciones generadas.
     * Entrada: Ninguna.
     * Salida: int - cantidad de instrucciones en el código intermedio.
     */
    public int getNumInstrucciones() {
        return codigo.size();
    }
}