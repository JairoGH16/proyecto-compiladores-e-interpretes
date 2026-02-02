import java.util.ArrayList;

/**
 * Generador de Código Intermedio (Three-Address Code) - VERSIÓN CORREGIDA
 * Genera código intermedio en formato de tres direcciones
 */
public class GeneradorCodigoIntermedio {
    private ArrayList<String> codigo;
    private int contadorTemporales;
    private int contadorEtiquetas;
    private TablaSimbolos tablaSimbolos;
    
    public GeneradorCodigoIntermedio(TablaSimbolos tablaSimbolos) {
        this.codigo = new ArrayList<>();
        this.contadorTemporales = 0;
        this.contadorEtiquetas = 0;
        this.tablaSimbolos = tablaSimbolos;
    }
    
    // ==================== GENERADORES DE TEMPORALES Y ETIQUETAS ====================
    
    public String nuevoTemporal() {
        return "t" + (contadorTemporales++);
    }
    
    public String nuevaEtiqueta() {
        return "L" + (contadorEtiquetas++);
    }
    
    public void agregarInstruccion(String instruccion) {
        codigo.add(instruccion);
    }
    
    public void agregarEtiqueta(String etiqueta) {
        codigo.add(etiqueta + ":");
    }
    
    // ==================== GENERACIÓN DE CÓDIGO ====================
    
    /**
     * Generar código para una asignación simple: x = expresion
     */
    public void generarAsignacion(String variable, String temporal) {
        agregarInstruccion(variable + " = " + temporal);
    }
    
    /**
     * Generar código para una operación binaria: t = a op b
     * CORRECCIÓN: Ahora maneja correctamente '//' (división entera)
     */
    public String generarOperacionBinaria(String operando1, String operador, String operando2) {
        String temp = nuevoTemporal();
        
        // CORRECCIÓN: Convertir '//' a 'DIV_ENTERA' para que MIPS lo reconozca
        // Dentro de generarOperacionBinaria
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
     * Generar código para una operación unaria: t = op a
     */
    public String generarOperacionUnaria(String operador, String operando) {
        String temp = nuevoTemporal();
        agregarInstruccion(temp + " = " + operador + " " + operando);
        return temp;
    }
    
    /**
     * Generar código para acceso a array: t = arr[i]
     */
    public String generarAccesoArray(String array, String indice) {
        String temp = nuevoTemporal();
        agregarInstruccion(temp + " = " + array + "[" + indice + "]");
        return temp;
    }
    
    /**
     * Generar código para acceso a array 2D: t = arr[i][j]
     */
    public String generarAccesoArray2D(String array, String indice1, String indice2) {
        String temp = nuevoTemporal();
        agregarInstruccion(temp + " = " + array + "[" + indice1 + "][" + indice2 + "]");
        return temp;
    }
    
    /**
     * Generar código para asignación a array: arr[i] = valor
     */
    public void generarAsignacionArray(String array, String indice, String valor) {
        agregarInstruccion(array + "[" + indice + "] = " + valor);
    }
    
    /**
     * Generar código para asignación a array 2D: arr[i][j] = valor
     */
    public void generarAsignacionArray2D(String array, String indice1, String indice2, String valor) {
        agregarInstruccion(array + "[" + indice1 + "][" + indice2 + "] = " + valor);
    }
    
    /**
     * Generar código para salto incondicional: goto L
     */
    public void generarGoto(String etiqueta) {
        agregarInstruccion("goto " + etiqueta);
    }
    
    /**
     * Generar código para salto condicional: if condicion goto L
     */
    public void generarIfGoto(String condicion, String etiqueta) {
        agregarInstruccion("if " + condicion + " goto " + etiqueta);
    }
    
    /**
     * Generar código para salto condicional negado: ifFalse condicion goto L
     */
    public void generarIfFalseGoto(String condicion, String etiqueta) {
        agregarInstruccion("ifFalse " + condicion + " goto " + etiqueta);
    }
    
    /**
     * Generar código para llamada a función: t = call func, n
     */
    public String generarLlamadaFuncion(String nombreFuncion, int numParams) {
        String temp = nuevoTemporal();
        agregarInstruccion(temp + " = call " + nombreFuncion + ", " + numParams);
        return temp;
    }
    
    /**
     * Generar código para parámetro de función: param x
     */
    public void generarParam(String valor) {
        agregarInstruccion("param " + valor);
    }
    
    /**
     * Generar código para return: return x
     */
    public void generarReturn(String valor) {
        if (valor != null && !valor.isEmpty()) {
            agregarInstruccion("return " + valor);
        } else {
            agregarInstruccion("return");
        }
    }
    
    /**
     * Generar código para inicio de función
     */
    public void generarInicioFuncion(String nombreFuncion) {
        agregarInstruccion("");
        agregarInstruccion("FUNCTION " + nombreFuncion + ":");
    }
    
    /**
     * Generar código para fin de función
     */
    public void generarFinFuncion() {
        agregarInstruccion("END_FUNCTION");
        agregarInstruccion("");
    }
    
    /**
     * Generar código para declaración de variable
     */
    public void generarDeclaracion(String variable, String tipo) {
        agregarInstruccion("DECLARE " + variable + " : " + tipo);
    }
    
    /**
     * Generar código para declaración de array
     */
    public void generarDeclaracionArray(String variable, String tipo, String dimensiones) {
        agregarInstruccion("DECLARE " + variable + " : " + tipo + "[" + dimensiones + "]");
    }
    
    // ==================== OBTENER RESULTADOS ====================
    
    public ArrayList<String> getCodigo() {
        return codigo;
    }
    
    public String getCodigoCompleto() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n    ========== INICIO CÓDIGO INTERMEDIO ==========\n\n");
        
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
    
    public void imprimir() {
        System.out.println(getCodigoCompleto());
    }
    
    public int getNumInstrucciones() {
        return codigo.size();
    }
}