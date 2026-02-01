import java.util.*;

/**
 * Generador de código MIPS a partir de código intermedio (Three-Address Code)
 */
public class GeneradorMIPS {
    private ArrayList<String> codigoIntermedio;
    private StringBuilder mips;
    private TablaSimbolos tablaSimbolos;
    
    // Control de funciones
    private String funcionActual;
    private HashMap<String, Integer> offsetsLocales;
    private int offsetActual;
    
    // Control de parámetros
    private int contadorParams;
    private ArrayList<String> parametrosEnEspera;
    
    // Mapeo de nombres de parámetros
    private HashMap<String, Integer> parametrosPorFuncion;
    
    public GeneradorMIPS(ArrayList<String> codigoIntermedio, TablaSimbolos tabla) {
        this.codigoIntermedio = codigoIntermedio;
        this.mips = new StringBuilder();
        this.tablaSimbolos = tabla;
        this.funcionActual = null;
        this.offsetsLocales = new HashMap<>();
        this.offsetActual = 0;
        this.contadorParams = 0;
        this.parametrosEnEspera = new ArrayList<>();
        this.parametrosPorFuncion = new HashMap<>();
    }
    
    /**
     * Genera el código MIPS completo
     */
    public String generar() {
        generarSeccionData();
        generarSeccionText();
        return mips.toString();
    }
    
    /**
     * Genera la sección .data con variables globales
     */
    private void generarSeccionData() {
        mips.append(".data\n");
        mips.append("    .align 2\n");
        
        // String para newline
        mips.append("    newline: .asciiz \"\\n\"\n");
        
        // Variables globales
        ArrayList<Simbolo> globales = tablaSimbolos.getSimbolosAlcance("GLOBAL");
        if (globales != null) {
            for (Simbolo s : globales) {
                if (s.getCategoria().equals("variable")) {
                    String nombre = s.getNombre();
                    String tipo = s.getTipo();
                    
                    if (s.getDimensiones() != null) {
                        // Array
                        String[] dims = s.getDimensiones().split("x");
                        int size = Integer.parseInt(dims[0]) * Integer.parseInt(dims[1]);
                        mips.append("    " + nombre + ": .word ");
                        for (int i = 0; i < size; i++) {
                            mips.append("0");
                            if (i < size - 1) mips.append(", ");
                        }
                        mips.append("\n");
                    } else {
                        // Variable simple
                        if (tipo.equals("float")) {
                            mips.append("    " + nombre + ": .float 0.0\n");
                        } else {
                            mips.append("    " + nombre + ": .word 0\n");
                        }
                    }
                }
            }
        }
        
        mips.append("\n");
    }
    
    /**
     * Genera la sección .text con el código
     */
    private void generarSeccionText() {
        mips.append(".text\n");
        mips.append("    .align 2\n");
        mips.append("    .globl main\n\n");
        
        // Punto de entrada principal
        mips.append("main:\n");
        
        // Procesar inicializaciones globales
        procesarInicializacionesGlobales();
        
        // Llamar a NAVIDAD (programa_principal)
        mips.append("    jal NAVIDAD\n");
        
        // Terminar programa
        mips.append("    li $v0, 10\n");
        mips.append("    syscall\n\n");
        
        // Generar funciones
        procesarFunciones();
    }
    
    /**
     * Procesa las inicializaciones de variables globales
     */
    private void procesarInicializacionesGlobales() {
        for (String linea : codigoIntermedio) {
            if (linea == null || linea.trim().isEmpty()) continue;
            linea = linea.trim();
            
            // Detener al encontrar primera función
            if (linea.startsWith("FUNCTION")) break;
            
            // Ignorar DECLARE (ya se procesa en .data)
            if (linea.startsWith("DECLARE")) continue;
            
            // ========== IMPORTANTE: Procesar asignaciones de arrays ==========
            if (linea.contains("[") && linea.contains("]") && linea.contains("=")) {
                procesarInstruccion(linea);
                continue;
            }
            
            // Solo procesar asignaciones simples (sin operadores)
            if (linea.contains("=") && !linea.contains("+") && 
                !linea.contains("-") && !linea.contains("*") && 
                !linea.contains("/") && !linea.contains("call")) {
                
                String[] partes = linea.split("=");
                if (partes.length == 2) {
                    String destino = partes[0].trim();
                    String valor = partes[1].trim();
                    
                    // Cargar valor
                    if (esNumero(valor)) {
                        mips.append("    li $t0, " + valor + "\n");
                    } else {
                        mips.append("    lw $t0, " + valor + "\n");
                    }
                    
                    // Guardar en global
                    mips.append("    sw $t0, " + destino + "\n");
                }
            }
        }
        
        mips.append("\n");
    }
    
    /**
     * Procesa todas las funciones
     */
    private void procesarFunciones() {
        boolean dentroFuncion = false;
        ArrayList<String> instruccionesFuncion = new ArrayList<>();
        
        for (String linea : codigoIntermedio) {
            if (linea == null || linea.trim().isEmpty()) continue;
            linea = linea.trim();
            
            if (linea.startsWith("FUNCTION")) {
                dentroFuncion = true;
                instruccionesFuncion.clear();
                
                // Extraer nombre de función
                String nombreFunc = linea.substring(9, linea.length() - 1).trim();
                funcionActual = nombreFunc;
                
                // Resetear offsets y contadores
                offsetsLocales.clear();
                offsetActual = 0;
                contadorParams = 0;
                
                // Obtener parámetros de la función
                obtenerParametrosFuncion(nombreFunc);
                
                continue;
            }
            
            if (linea.equals("END_FUNCTION")) {
                // Generar código de la función
                generarFuncion(funcionActual, instruccionesFuncion);
                dentroFuncion = false;
                funcionActual = null;
                continue;
            }
            
            if (dentroFuncion) {
                instruccionesFuncion.add(linea);
            }
        }
    }
    
    /**
     * Obtiene los parámetros de una función desde la tabla de símbolos
     */
    private void obtenerParametrosFuncion(String nombreFunc) {
        // Buscar en el alcance de la función
        ArrayList<Simbolo> simbolos = tablaSimbolos.getSimbolosAlcance(nombreFunc);
        if (simbolos != null) {
            int paramIndex = 0;
            for (Simbolo s : simbolos) {
                if (s.getCategoria().equals("parametro")) {
                    // Asignar offset para parámetro (se pasan en $a0-$a3)
                    String nombre = s.getNombre();
                    offsetsLocales.put(nombre, offsetActual);
                    parametrosPorFuncion.put(nombre, paramIndex);
                    offsetActual += 4;
                    paramIndex++;
                }
            }
        }
    }
    
    /**
     * Genera el código MIPS para una función
     */
    private void generarFuncion(String nombre, ArrayList<String> instrucciones) {
        mips.append(nombre + ":\n");
        
        // Prólogo de función
        mips.append("    # Prólogo\n");
        mips.append("    addi $sp, $sp, -128    # Reservar espacio en pila\n");
        mips.append("    sw $ra, 124($sp)       # Guardar dirección de retorno\n");
        mips.append("    sw $fp, 120($sp)       # Guardar frame pointer\n");
        mips.append("    addi $fp, $sp, 128     # Establecer nuevo frame pointer\n");
        
        // Guardar parámetros recibidos en $a0-$a3
        ArrayList<Simbolo> parametros = tablaSimbolos.getSimbolosAlcance(nombre);
        if (parametros != null) {
            int paramIndex = 0;
            for (Simbolo s : parametros) {
                if (s.getCategoria().equals("parametro") && paramIndex < 4) {
                    int offset = offsetsLocales.get(s.getNombre());
                    mips.append("    sw $a" + paramIndex + ", " + offset + "($sp)    # Guardar parámetro " + s.getNombre() + "\n");
                    paramIndex++;
                }
            }
        }
        
        mips.append("\n");
        
        // Procesar instrucciones
        for (String instruccion : instrucciones) {
            procesarInstruccion(instruccion);
        }
        
        // Epílogo por defecto (si no hay return explícito)
        mips.append("    # Epílogo por defecto\n");
        mips.append("    lw $ra, 124($sp)\n");
        mips.append("    lw $fp, 120($sp)\n");
        mips.append("    addi $sp, $sp, 128\n");
        mips.append("    jr $ra\n\n");
    }
    
    /**
     * Procesa una instrucción individual
     */
    /**
     * Procesa una instrucción individual del código intermedio
     */
    private void procesarInstruccion(String instr) {
        if (instr == null || instr.trim().isEmpty()) return;
        instr = instr.trim();
        
        // ========== ETIQUETAS ==========
        if (instr.endsWith(":")) {
            mips.append(instr + "\n");
            return;
        }
        
            // ========== DECLARE ==========
        if (instr.startsWith("DECLARE")) {
            procesarDeclare(instr);
            return;
        }
        
        // ========== SHOW (imprimir) ==========
        if (instr.startsWith("show ")) {
            String valor = instr.substring(5).trim();
            
            // Cargar el valor a imprimir en $a0
            cargarValor(valor, "$a0");
            
            // Syscall para imprimir entero
            mips.append("    li $v0, 1          # Syscall print_int\n");
            mips.append("    syscall\n");
            
            // Imprimir newline
            mips.append("    la $a0, newline    # Imprimir salto de línea\n");
            mips.append("    li $v0, 4          # Syscall print_string\n");
            mips.append("    syscall\n");
            
            return;
        }
        
        // ========== SALTOS CONDICIONALES ==========
        // if condicion goto etiqueta
        if (instr.startsWith("if ")) {
            String[] partes = instr.split("\\s+");
            if (partes.length >= 4) {
                String condicion = partes[1];
                String etiqueta = partes[3];
                
                cargarValor(condicion, "$t0");
                mips.append("    bnez $t0, " + etiqueta + "    # Saltar si " + condicion + " != 0\n");
            }
            return;
        }
        
        // ifFalse condicion goto etiqueta
        if (instr.startsWith("ifFalse ")) {
            String[] partes = instr.split("\\s+");
            if (partes.length >= 4) {
                String condicion = partes[1];
                String etiqueta = partes[3];
                
                cargarValor(condicion, "$t0");
                mips.append("    beqz $t0, " + etiqueta + "    # Saltar si " + condicion + " == 0\n");
            }
            return;
        }
        
        // ========== SALTOS INCONDICIONALES ==========
        // goto etiqueta
        if (instr.startsWith("goto ")) {
            String etiqueta = instr.substring(5).trim();
            mips.append("    j " + etiqueta + "\n");
            return;
        }
        
        // ========== PARÁMETROS ==========
        // param valor
        if (instr.startsWith("param ")) {
            String valor = instr.substring(6).trim();
            parametrosEnEspera.add(valor);
            return;
        }
        
        // ========== LLAMADAS A FUNCIÓN ==========
        // resultado = call funcion, numParams
        if (instr.contains("= call ")) {
            String[] partes = instr.split("=");
            String destino = partes[0].trim();
            String llamada = partes[1].trim();
            
            String[] partesLlamada = llamada.split("\\s+");
            String nombreFunc = partesLlamada[1].replace(",", "");
            
            // Cargar parámetros en $a0-$a3
            for (int i = 0; i < parametrosEnEspera.size() && i < 4; i++) {
                cargarValor(parametrosEnEspera.get(i), "$a" + i);
            }
            
            // Llamar función
            mips.append("    jal " + nombreFunc + "    # Llamar " + nombreFunc + "\n");
            
            // Guardar resultado
            guardarValor("$v0", destino);
            
            // Limpiar parámetros
            parametrosEnEspera.clear();
            return;
        }
        
        // ========== RETURN ==========
        // return valor
        if (instr.startsWith("return")) {
            String valor = instr.substring(6).trim();
            
            // Cargar valor de retorno en $v0 (si no está vacío)
            if (!valor.isEmpty() && !valor.startsWith("\"")) {
                cargarValor(valor, "$v0");
            }
            
            // Epílogo de función
            mips.append("    # Return\n");
            mips.append("    lw $ra, 124($sp)\n");
            mips.append("    lw $fp, 120($sp)\n");
            mips.append("    addi $sp, $sp, 128\n");
            mips.append("    jr $ra\n");
            return;
        }
        
        // ========== ACCESO A ARRAYS ==========
        // destino = array[indice1][indice2]
        if (instr.contains("=") && instr.contains("[") && instr.contains("]")) {
            String[] partes = instr.split("=");
            String destino = partes[0].trim();
            String expresion = partes[1].trim();
            
            // Detectar si es acceso a array
            if (expresion.contains("[")) {
                procesarAccesoArray(destino, expresion);
                return;
            }
        }
        
        // array[indice1][indice2] = valor
        if (instr.contains("[") && instr.contains("]") && instr.contains("=")) {
            String[] partes = instr.split("=");
            if (partes[0].contains("[")) {
                procesarAsignacionArray(instr);
                return;
            }
        }
        
        // ========== ASIGNACIONES Y OPERACIONES ==========
        // destino = operando1 op operando2
        // destino = operando
        if (instr.contains("=")) {
            procesarAsignacion(instr);
            return;
        }
    }

    /**
     * Procesa DECLARE para arrays locales
     */
    private void procesarDeclare(String instr) {
        // DECLARE nombre : tipo[dims]
        String[] partes = instr.split(":");
        if (partes.length < 2) return;
        
        String nombre = partes[0].replace("DECLARE", "").trim();
        String tipoYDims = partes[1].trim();
        
        // Verificar si tiene dimensiones (es array)
        if (tipoYDims.contains("[")) {
            // Extraer dimensiones
            int bracketPos = tipoYDims.indexOf('[');
            String dims = tipoYDims.substring(bracketPos + 1, tipoYDims.indexOf(']'));
            
            String[] dimensiones = dims.split("x");
            int dim1 = Integer.parseInt(dimensiones[0]);
            int dim2 = Integer.parseInt(dimensiones[1]);
            int totalElementos = dim1 * dim2;
            
            // Reservar espacio en la pila para el array
            int offsetInicio = offsetActual;
            offsetsLocales.put(nombre, offsetInicio);
            
            mips.append("    # DECLARE " + nombre + "[" + dim1 + "][" + dim2 + "]\n");
            mips.append("    # Reservar " + (totalElementos * 4) + " bytes en pila\n");
            
            // NO adelantar offsetActual aquí, se hará al acceder
            // Guardar info del array para usarla después
            offsetsLocales.put(nombre + "_dim1", dim1);
            offsetsLocales.put(nombre + "_dim2", dim2);
            offsetsLocales.put(nombre + "_base", offsetActual);
            
            offsetActual += (totalElementos * 4);
        }
    }

    /**
     * Procesa acceso a array CORREGIDO para arrays locales
     */
    private void procesarAccesoArray(String destino, String expresion) {
        // Extraer nombre del array y los índices
        int bracketPos = expresion.indexOf('[');
        String nombreArray = expresion.substring(0, bracketPos).trim();
        
        // Extraer índices
        String indices = expresion.substring(bracketPos);
        String[] partes = indices.split("\\]\\[");
        
        if (partes.length == 2) {
            // Array 2D: array[i][j]
            String indice1 = partes[0].replace("[", "").trim();
            String indice2 = partes[1].replace("]", "").trim();
            
            // Cargar índices
            cargarValor(indice1, "$t0");  // i
            cargarValor(indice2, "$t1");  // j
            
            // Verificar si es array local o global
            if (offsetsLocales.containsKey(nombreArray + "_base")) {
                // Array LOCAL en pila
                int baseOffset = offsetsLocales.get(nombreArray + "_base");
                int numCols = offsetsLocales.get(nombreArray + "_dim2");
                
                mips.append("    # Acceso a array local " + nombreArray + "[" + indice1 + "][" + indice2 + "]\n");
                mips.append("    li $t2, " + numCols + "    # Número de columnas\n");
                mips.append("    mul $t3, $t0, $t2       # i * numCols\n");
                mips.append("    add $t3, $t3, $t1       # i * numCols + j\n");
                mips.append("    sll $t3, $t3, 2         # Multiplicar por 4\n");
                mips.append("    addi $t3, $t3, " + baseOffset + "  # Agregar offset base\n");
                mips.append("    add $t4, $sp, $t3       # Dirección = $sp + offset\n");
                mips.append("    lw $t5, 0($t4)          # Cargar elemento\n");
                
                // Guardar en destino
                guardarValor("$t5", destino);
            } else {
                // Array GLOBAL
                Simbolo arraySymbol = tablaSimbolos.buscar(nombreArray);
                if (arraySymbol != null && arraySymbol.getDimensiones() != null) {
                    String[] dims = arraySymbol.getDimensiones().split("x");
                    int numCols = Integer.parseInt(dims[1]);
                    
                    mips.append("    # Acceso a array global " + nombreArray + "[" + indice1 + "][" + indice2 + "]\n");
                    mips.append("    li $t2, " + numCols + "    # Número de columnas\n");
                    mips.append("    mul $t3, $t0, $t2       # i * numCols\n");
                    mips.append("    add $t3, $t3, $t1       # i * numCols + j\n");
                    mips.append("    sll $t3, $t3, 2         # Multiplicar por 4\n");
                    
                    // Cargar dirección base del array global
                    mips.append("    la $t4, " + nombreArray + "    # Dirección base del array\n");
                    mips.append("    add $t4, $t4, $t3       # Dirección del elemento\n");
                    mips.append("    lw $t5, 0($t4)          # Cargar elemento\n");
                    
                    // Guardar en destino
                    guardarValor("$t5", destino);
                }
            }
        } else if (partes.length == 1) {
            // Array 1D: array[i]
            String indice = partes[0].replace("[", "").replace("]", "").trim();
            
            cargarValor(indice, "$t0");
            
            // Verificar si es local o global
            if (offsetsLocales.containsKey(nombreArray + "_base")) {
                // Array local
                int baseOffset = offsetsLocales.get(nombreArray + "_base");
                
                mips.append("    sll $t1, $t0, 2         # i * 4\n");
                mips.append("    addi $t1, $t1, " + baseOffset + "\n");
                mips.append("    add $t2, $sp, $t1\n");
                mips.append("    lw $t3, 0($t2)\n");
                
                guardarValor("$t3", destino);
            } else {
                // Array global
                mips.append("    sll $t1, $t0, 2         # i * 4\n");
                mips.append("    la $t2, " + nombreArray + "    # Dirección base\n");
                mips.append("    add $t2, $t2, $t1       # Dirección del elemento\n");
                mips.append("    lw $t3, 0($t2)          # Cargar elemento\n");
                
                guardarValor("$t3", destino);
            }
        }
    }

    /**
     * Procesa asignación a array CORREGIDO
     */
    private void procesarAsignacionArray(String instr) {
        String[] partes = instr.split("=");
        String arrayPart = partes[0].trim();
        String valor = partes[1].trim();
        
        int bracketPos = arrayPart.indexOf('[');
        String nombreArray = arrayPart.substring(0, bracketPos).trim();
        
        String indices = arrayPart.substring(bracketPos);
        String[] indicesParts = indices.split("\\]\\[");
        
        if (indicesParts.length == 2) {
            // Array 2D
            String indice1 = indicesParts[0].replace("[", "").trim();
            String indice2 = indicesParts[1].replace("]", "").trim();
            
            // Verificar si es local o global
            if (offsetsLocales.containsKey(nombreArray + "_base")) {
                // Array LOCAL - código existente
                // ... (mantener como está)
            } else {
                // Array GLOBAL
                Simbolo arraySymbol = tablaSimbolos.buscar(nombreArray);
                if (arraySymbol != null && arraySymbol.getDimensiones() != null) {
                    String[] dims = arraySymbol.getDimensiones().split("x");
                    int numCols = Integer.parseInt(dims[1]);
                    
                    mips.append("    # Asignar a array global " + nombreArray + "[" + indice1 + "][" + indice2 + "] = " + valor + "\n");
                    
                    // Si AMBOS índices son literales, optimizar
                    if (esNumero(indice1) && esNumero(indice2)) {
                        int i = Integer.parseInt(indice1);
                        int j = Integer.parseInt(indice2);
                        int offset = (i * numCols + j) * 4;
                        
                        cargarValor(valor, "$t0");
                        mips.append("    sw $t0, " + nombreArray + " + " + offset + "\n");
                    } else {
                        // Índices variables
                        cargarValor(indice1, "$t0");
                        cargarValor(indice2, "$t1");
                        
                        mips.append("    li $t2, " + numCols + "\n");
                        mips.append("    mul $t3, $t0, $t2\n");
                        mips.append("    add $t3, $t3, $t1\n");
                        mips.append("    sll $t3, $t3, 2\n");
                        mips.append("    la $t4, " + nombreArray + "\n");
                        mips.append("    add $t4, $t4, $t3\n");
                        
                        cargarValor(valor, "$t5");
                        mips.append("    sw $t5, 0($t4)\n");
                    }
                }
            }
        }
    }
    
    /**
     * Procesa asignaciones y operaciones
     */
    private void procesarAsignacion(String instr) {
        String[] partes = instr.split("=", 2);
        String destino = partes[0].trim();
        String expresion = partes[1].trim();
        
        String[] tokens = expresion.split("\\s+");
        
        // Operación binaria: op1 operador op2
        if (tokens.length == 3) {
            String op1 = tokens[0];
            String operador = tokens[1];
            String op2 = tokens[2];
            
            cargarValor(op1, "$t0");
            cargarValor(op2, "$t1");
            
            switch (operador) {
                case "+":
                    mips.append("    add $t2, $t0, $t1    # " + op1 + " + " + op2 + "\n");
                    break;
                case "-":
                    mips.append("    sub $t2, $t0, $t1    # " + op1 + " - " + op2 + "\n");
                    break;
                case "*":
                    mips.append("    mul $t2, $t0, $t1    # " + op1 + " * " + op2 + "\n");
                    break;
                case "/":
                    mips.append("    div $t0, $t1         # " + op1 + " / " + op2 + "\n");
                    mips.append("    mflo $t2\n");
                    break;
                case "%":
                    mips.append("    div $t0, $t1         # " + op1 + " % " + op2 + "\n");
                    mips.append("    mfhi $t2\n");
                    break;
                case "==":
                    mips.append("    seq $t2, $t0, $t1    # " + op1 + " == " + op2 + "\n");
                    break;
                case "!=":
                    mips.append("    sne $t2, $t0, $t1    # " + op1 + " != " + op2 + "\n");
                    break;
                case "<":
                    mips.append("    slt $t2, $t0, $t1    # " + op1 + " < " + op2 + "\n");
                    break;
                case ">":
                    mips.append("    sgt $t2, $t0, $t1    # " + op1 + " > " + op2 + "\n");
                    break;
                case "<=":
                    mips.append("    sle $t2, $t0, $t1    # " + op1 + " <= " + op2 + "\n");
                    break;
                case ">=":
                    mips.append("    sge $t2, $t0, $t1    # " + op1 + " >= " + op2 + "\n");
                    break;
                case "&&":
                    mips.append("    and $t2, $t0, $t1    # " + op1 + " && " + op2 + "\n");
                    break;
                case "||":
                    mips.append("    or $t2, $t0, $t1     # " + op1 + " || " + op2 + "\n");
                    break;
                default:
                    mips.append("    # Operador desconocido: " + operador + "\n");
                    return;
            }
            
            guardarValor("$t2", destino);
        }
        // Operación unaria o asignación simple
        else if (tokens.length == 1) {
            cargarValor(expresion, "$t0");
            guardarValor("$t0", destino);
        }
        // Operación unaria: ! operando
        else if (tokens.length == 2 && tokens[0].equals("!")) {
            cargarValor(tokens[1], "$t0");
            mips.append("    seq $t2, $t0, $zero    # !" + tokens[1] + "\n");
            guardarValor("$t2", destino);
        }
    }
    
    /**
     * Carga un valor en un registro
     */
    private void cargarValor(String valor, String registro) {
        valor = valor.trim();
        
        if (valor.isEmpty()) {
            mips.append("    li " + registro + ", 0\n");
            return;
        }
        
        // Número literal
        if (esNumero(valor)) {
            mips.append("    li " + registro + ", " + valor + "\n");
            return;
        }
        
        // String literal (ignorar por ahora)
        if (valor.startsWith("\"")) {
            return;
        }
        
        // Variable local o parámetro
        if (offsetsLocales.containsKey(valor)) {
            int offset = offsetsLocales.get(valor);
            mips.append("    lw " + registro + ", " + offset + "($sp)\n");
            return;
        }
        
        // Variable global
        Simbolo s = tablaSimbolos.buscar(valor);
        if (s != null && s.getAlcance().equals("GLOBAL")) {
            mips.append("    lw " + registro + ", " + valor + "\n");
            return;
        }
        
        // Variable temporal nueva (asignar offset)
        if (valor.startsWith("t") || valor.startsWith("i") || valor.startsWith("suma") || 
            valor.startsWith("total") || valor.startsWith("residuo")) {
            offsetsLocales.put(valor, offsetActual);
            int offset = offsetActual;
            offsetActual += 4;
            mips.append("    lw " + registro + ", " + offset + "($sp)\n");
        }
    }
    
    /**
     * Guarda un valor desde un registro
     */
    private void guardarValor(String registro, String destino) {
        destino = destino.trim();
        
        // Variable local o parámetro
        if (offsetsLocales.containsKey(destino)) {
            int offset = offsetsLocales.get(destino);
            mips.append("    sw " + registro + ", " + offset + "($sp)\n");
            return;
        }
        
        // Variable global
        Simbolo s = tablaSimbolos.buscar(destino);
        if (s != null && s.getAlcance().equals("GLOBAL")) {
            mips.append("    sw " + registro + ", " + destino + "\n");
            return;
        }
        
        // Variable temporal nueva (asignar offset)
        offsetsLocales.put(destino, offsetActual);
        int offset = offsetActual;
        offsetActual += 4;
        mips.append("    sw " + registro + ", " + offset + "($sp)\n");
    }
    
    /**
     * Verifica si un string es un número
     */
    private boolean esNumero(String s) {
        if (s == null || s.isEmpty()) return false;
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e1) {
            try {
                Float.parseFloat(s);
                return true;
            } catch (NumberFormatException e2) {
                return false;
            }
        }
    }
}