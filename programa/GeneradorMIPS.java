import java.util.*;

/**
 * Clase GeneradorMIPS
 * Objetivo: Traducir el código intermedio (Three-Address Code) a código ensamblador MIPS32.
 * Genera un archivo .asm ejecutable en simuladores. Maneja la traducción de
 * variables globales y locales, funciones, arrays, estructuras de control y expresiones.
 * Restricciones: El código intermedio debe estar bien formado y la tabla de símbolos debe contener
 * toda la información necesaria sobre tipos y alcances de variables.
 */
public class GeneradorMIPS {
    // Listas para separar código de funciones y código de inicialización global
    private ArrayList<String> codigoGlobal = new ArrayList<>();
    
    private ArrayList<String> codigoIntermedio;
    private StringBuilder mips;
    private TablaSimbolos tablaSimbolos;
    
    private String funcionActual;
    private HashMap<String, Integer> offsetsLocales;
    private int offsetActual;
    
    private ArrayList<String> parametrosEnEspera;
    private int contadorStrings = 0;
    private HashMap<String, String> mapaStrings = new HashMap<>();
    
    private HashMap<String, Integer> parametrosPorFuncion;
    private int contadorParams;
    
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
    
    public String generar() {
        generarSeccionData();
        generarSeccionText();
        return mips.toString();
    }
    
    private void generarSeccionData() {
        mips.append(".data\n");
        mips.append("    .align 2\n");
        mips.append("    newline: .asciiz \"\\n\"\n");
        
        // Extraer todos los strings del código intermedio
        for (String linea : codigoIntermedio) {
            if (linea.contains("\"")) {
                int inicio = linea.indexOf("\"");
                int fin = linea.lastIndexOf("\"");
                if (inicio != -1 && fin != -1 && inicio < fin) {
                    String contenido = linea.substring(inicio, fin + 1);
                    if (!mapaStrings.containsKey(contenido)) {
                        String etiqueta = "str_" + (contadorStrings++);
                        mapaStrings.put(contenido, etiqueta);
                        mips.append("    " + etiqueta + ": .asciiz " + contenido + "\n");
                    }
                }
            }
        }
        
        // Declarar variables globales
        ArrayList<Simbolo> globales = tablaSimbolos.getSimbolosAlcance("GLOBAL");
        if (globales != null) {
            for (Simbolo s : globales) {
                if (s.getCategoria().equals("variable")) {
                    String nombre = s.getNombre();
                    String tipo = s.getTipo();
                    
                    if (s.getDimensiones() != null) {
                        String[] dims = s.getDimensiones().split("x");
                        int size = Integer.parseInt(dims[0]) * Integer.parseInt(dims[1]);
                        mips.append("    " + nombre + ": .word ");
                        for (int i = 0; i < size; i++) {
                            mips.append("0");
                            if (i < size - 1) mips.append(", ");
                        }
                        mips.append("\n");
                    } else {
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
    
    private void generarSeccionText() {
        mips.append(".text\n");
        mips.append("    .globl main\n");
        mips.append("main:\n");
        
        // CRÍTICO: Procesar primero el código global (inicializaciones de matrices, vars globales)
        // Esto asegura que las matrices globales no estén llenas de ceros.
        mips.append("    # --- Inicialización Global ---\n");
        
        // Llenamos la lista codigoGlobal separándola de las funciones
        separarCodigoGlobalYFunciones();
        
        for (String instr : codigoGlobal) {
            procesarInstruccion(instr);
        }
        mips.append("    # -----------------------------\n");
        
        mips.append("    jal NAVIDAD\n");
        mips.append("    li $v0, 10\n");
        mips.append("    syscall\n\n");
        
        // Ahora sí, generamos el código de las funciones
        generarCodigoFunciones();
    }
    
    private void separarCodigoGlobalYFunciones() {
        codigoGlobal.clear();
        boolean dentroFuncion = false;
        
        for (String linea : codigoIntermedio) {
            if (linea == null || linea.trim().isEmpty()) continue;
            linea = linea.trim();
            
            if (linea.startsWith("FUNCTION")) {
                dentroFuncion = true;
            }
            
            if (!dentroFuncion && !linea.startsWith("FUNCTION") && !linea.startsWith("END_FUNCTION")) {
                // Si no es una declaración (DECLARE), es una instrucción ejecutable global
                // Las declaraciones (DECLARE) globales ya se manejaron en .data
                if (!linea.startsWith("DECLARE")) {
                    codigoGlobal.add(linea);
                }
            }
            
            if (linea.equals("END_FUNCTION")) {
                dentroFuncion = false;
            }
        }
    }

    private void generarCodigoFunciones() {
        boolean dentroFuncion = false;
        ArrayList<String> instruccionesFuncion = new ArrayList<>();
        
        for (String linea : codigoIntermedio) {
            if (linea == null || linea.trim().isEmpty()) continue;
            linea = linea.trim();
            
            if (linea.startsWith("FUNCTION")) {
                dentroFuncion = true;
                instruccionesFuncion.clear();
                
                String nombreFunc = linea.substring(9, linea.length() - 1).trim();
                funcionActual = nombreFunc;
                
                offsetsLocales.clear();
                offsetActual = 0;
                contadorParams = 0;
                
                obtenerParametrosFuncion(nombreFunc);
                continue;
            }
            
            if (linea.equals("END_FUNCTION")) {
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

    private void obtenerParametrosFuncion(String nombreFunc) {
        ArrayList<Simbolo> simbolos = tablaSimbolos.getSimbolosAlcance(nombreFunc);
        if (simbolos != null) {
            int paramIndex = 0;
            for (Simbolo s : simbolos) {
                if (s.getCategoria().equals("parametro")) {
                    String nombre = s.getNombre();
                    offsetsLocales.put(nombre, offsetActual);
                    parametrosPorFuncion.put(nombre, paramIndex);
                    offsetActual += 4;
                    paramIndex++;
                }
            }
        }
    }
    
    private void generarFuncion(String nombre, ArrayList<String> instrucciones) {
        mips.append(nombre + ":\n");
        mips.append("    # Prólogo de " + nombre + "\n");
        mips.append("    addi $sp, $sp, -256\n");
        mips.append("    sw $ra, 252($sp)\n");
        mips.append("    sw $fp, 248($sp)\n");
        mips.append("    addi $fp, $sp, 256\n");
        
        // Guardar parámetros
        ArrayList<Simbolo> parametros = tablaSimbolos.getSimbolosAlcance(nombre);
        if (parametros != null) {
            int paramIndex = 0;
            for (Simbolo s : parametros) {
                if (s.getCategoria().equals("parametro") && paramIndex < 4) {
                    int offset = offsetsLocales.get(s.getNombre());
                    mips.append("    sw $a" + paramIndex + ", " + offset + 
                              "($sp)    # Guardar parámetro " + s.getNombre() + "\n");
                    paramIndex++;
                }
            }
        }
        
        mips.append("\n");
        
        // Procesar instrucciones
        for (String instruccion : instrucciones) {
            procesarInstruccion(instruccion);
        }
        
        // Epílogo por defecto
        mips.append("    # Epílogo por defecto\n");
        mips.append("    lw $ra, 252($sp)\n");
        mips.append("    lw $fp, 248($sp)\n");
        mips.append("    addi $sp, $sp, 256\n");
        mips.append("    jr $ra\n\n");
    }
private void procesarInstruccion(String instr) {
        if (instr == null || instr.trim().isEmpty()) return;
        instr = instr.trim();

        // 1. MANEJO DE ETIQUETAS
        if (instr.endsWith(":")) {
            mips.append(instr + "\n");
            return;
        }

        // 2. DECLARACIONES (Reserva de espacio en stack)
        if (instr.startsWith("DECLARE")) {
            procesarDeclare(instr);
            return;
        }

        // 3. ENTRADA (GET)
        if (instr.startsWith("get ")) {
            String variable = instr.substring(4).trim();
            mips.append("    li $v0, 5          # Leer entero\n");
            mips.append("    syscall\n");
            guardarValor("$v0", variable);
            return;
        }

        // 4. SALIDA (SHOW) - CORREGIDO PARA FLOATS
        if (instr.startsWith("show ")) {
            String valor = instr.substring(5).trim();
            
            if (valor.startsWith("\"")) {
                // String
                cargarValor(valor, "$a0");
                mips.append("    li $v0, 4          # Imprimir String\n");
                mips.append("    syscall\n");
            } else {
                // Detectar si es float (literal o variable)
                boolean esFloat = false;
                if (valor.contains(".")) {
                    esFloat = true;
                } else {
                    Simbolo s = tablaSimbolos.buscar(valor);
                    if (s != null && s.getTipo().equals("float")) esFloat = true;
                }
                
                if (esFloat) {
                    // === LÓGICA CORRECTA PARA FLOATS ===
                    if (valor.contains(".")) {
                        // Literal: Crear etiqueta, cargarla con l.s
                        String etiqueta = "float_const_" + (contadorStrings++);
                        mips.insert(mips.indexOf(".text"), 
                            "    " + etiqueta + ": .float " + valor + "\n");
                        mips.append("    l.s $f12, " + etiqueta + "\n");
                    } else {
                        // Variable: Cargar con lwc1 (local) o l.s (global)
                        if (offsetsLocales.containsKey(valor)) {
                            int offset = offsetsLocales.get(valor);
                            mips.append("    lwc1 $f12, " + offset + "($sp)  # Cargar float local\n");
                        } else {
                            mips.append("    l.s $f12, " + valor + "     # Cargar float global\n");
                        }
                    }
                    mips.append("    li $v0, 2          # Syscall 2 = Float\n");
                    mips.append("    syscall\n");
                } else {
                    // Entero / Char
                    cargarValor(valor, "$a0");
                    mips.append("    li $v0, 1          # Syscall 1 = Int\n");
                    mips.append("    syscall\n");
                }
            }
            return;
        }

        // 5. CONTROL DE FLUJO
        if (instr.startsWith("if ")) {
            String[] partes = instr.split("\\s+");
            cargarValor(partes[1], "$t0");
            mips.append("    bnez $t0, " + partes[3] + "\n");
            return;
        }

        if (instr.startsWith("ifFalse ")) {
            String[] partes = instr.split("\\s+");
            cargarValor(partes[1], "$t0");
            mips.append("    beqz $t0, " + partes[3] + "\n");
            return;
        }

        if (instr.startsWith("goto ")) {
            mips.append("    j " + instr.substring(5).trim() + "\n");
            return;
        }

        // 6. FUNCIONES Y RETORNO
        if (instr.startsWith("param ")) {
            parametrosEnEspera.add(instr.substring(6).trim());
            return;
        }

        if (instr.contains("= call ")) {
            String[] partes = instr.split("=");
            String destino = partes[0].trim();
            String llamada = partes[1].trim();
            String nombreFunc = llamada.split("\\s+")[1].replace(",", "");
            
            for (int i = 0; i < parametrosEnEspera.size() && i < 4; i++) {
                cargarValor(parametrosEnEspera.get(i), "$a" + i);
            }
            mips.append("    jal " + nombreFunc + "\n");
            guardarValor("$v0", destino);
            parametrosEnEspera.clear();
            return;
        }

        if (instr.startsWith("return")) {
            String valor = instr.length() > 6 ? instr.substring(6).trim() : "";
            if (!valor.isEmpty()) cargarValor(valor, "$v0");
            
            mips.append("    # Epílogo de función\n");
            mips.append("    lw $ra, 252($sp)\n");
            mips.append("    lw $fp, 248($sp)\n");
            mips.append("    addi $sp, $sp, 256\n");
            
            if (funcionActual != null && funcionActual.equalsIgnoreCase("NAVIDAD")) {
                mips.append("    li $v0, 10\n");
                mips.append("    syscall\n");
            } else {
                mips.append("    jr $ra\n");
            }
            return;
        }

        // 7. OPERACIONES ARITMÉTICAS Y LÓGICAS
        if (instr.contains("=") && !instr.contains("[")) {
            String[] partes = instr.split("=", 2);
            String destino = partes[0].trim();
            String expresion = partes[1].trim();
            String[] tokens = expresion.split("\\s+");

            if (tokens.length == 3) {
                cargarValor(tokens[0], "$t0");
                cargarValor(tokens[2], "$t1");
                String op = tokens[1];

                switch (op) {
                    case "+": mips.append("    addu $t2, $t0, $t1\n"); break;
                    case "-": mips.append("    subu $t2, $t0, $t1\n"); break;
                    case "*": mips.append("    mul $t2, $t0, $t1\n"); break;
                    case "/": case "DIV_ENTERA":
                        mips.append("    div $t0, $t1\n");
                        mips.append("    mflo $t2\n");
                        break;
                    case "%":
                        mips.append("    div $t0, $t1\n");
                        mips.append("    mfhi $t2\n");
                        break;
                    case "==": mips.append("    seq $t2, $t0, $t1\n"); break;
                    case "!=": mips.append("    sne $t2, $t0, $t1\n"); break;
                    case "<":  mips.append("    slt $t2, $t0, $t1\n"); break;
                    case ">":  mips.append("    sgt $t2, $t0, $t1\n"); break;
                    case "<=": mips.append("    sle $t2, $t0, $t1\n"); break;
                    case ">=": mips.append("    sge $t2, $t0, $t1\n"); break;
                    case "&&": mips.append("    and $t2, $t0, $t1\n"); break;
                    case "||": mips.append("    or $t2, $t0, $t1\n"); break;
                }
                guardarValor("$t2", destino);
                return;
            }
        }

        // 8. ASIGNACIONES DE ARRAYS
        if (instr.contains("[") && instr.contains("]")) {
            if (instr.indexOf("[") < instr.indexOf("=")) {
                procesarAsignacionArray(instr);
            } else {
                String[] partes = instr.split("=");
                procesarAccesoArray(partes[0].trim(), partes[1].trim());
            }
            return;
        }

        // 9. ASIGNACIÓN SIMPLE
        if (instr.contains("=")) {
            String[] partes = instr.split("=");
            cargarValor(partes[1].trim(), "$t0");
            guardarValor("$t0", partes[0].trim());
        }
    }

    private void procesarDeclare(String instr) {
        String[] partes = instr.split(":");
        if (partes.length < 2) return;
        
        String nombre = partes[0].replace("DECLARE", "").trim();
        String tipoYDims = partes[1].trim();
        
        if (tipoYDims.contains("[")) {
            // Array
            int bracketPos = tipoYDims.indexOf('[');
            String dims = tipoYDims.substring(bracketPos + 1, tipoYDims.indexOf(']'));
            
            String[] dimensiones = dims.split("x");
            int dim1 = Integer.parseInt(dimensiones[0]);
            int dim2 = Integer.parseInt(dimensiones[1]);
            int totalElementos = dim1 * dim2;
            
            mips.append("    # DECLARE " + nombre + "[" + dim1 + "][" + dim2 + "]\n");
            
            int offsetInicio = offsetActual;
            offsetsLocales.put(nombre, offsetInicio);
            offsetsLocales.put(nombre + "_dim1", dim1);
            offsetsLocales.put(nombre + "_dim2", dim2);
            offsetsLocales.put(nombre + "_base", offsetActual);
            
            offsetActual += (totalElementos * 4);
        } else {
            // Variable simple (solo si no es global ya declarada en .data)
            if (!offsetsLocales.containsKey(nombre)) {
                // Verificar si no es global antes de crear local
                Simbolo s = tablaSimbolos.buscar(nombre);
                if (s == null || !s.getAlcance().equals("GLOBAL")) {
                     cargarValorSimple("0", "$t0");
                     guardarValor("$t0", nombre);
                }
            }
        }
    }

    private void procesarAccesoArray(String destino, String expresion) {
        int bracketPos = expresion.indexOf('[');
        String nombreArray = expresion.substring(0, bracketPos).trim();
        String indices = expresion.substring(bracketPos);
        String[] partes = indices.split("\\]\\[");
        
        if (partes.length == 2) {
            String indice1 = partes[0].replace("[", "").trim();
            String indice2 = partes[1].replace("]", "").trim();
            
            cargarValor(indice1, "$t0");
            cargarValor(indice2, "$t1");
            
            if (offsetsLocales.containsKey(nombreArray + "_base")) {
                int baseOffset = offsetsLocales.get(nombreArray + "_base");
                int numCols = offsetsLocales.get(nombreArray + "_dim2");
                
                mips.append("    li $t2, " + numCols + "\n");
                mips.append("    mul $t3, $t0, $t2\n");
                mips.append("    add $t3, $t3, $t1\n");
                mips.append("    sll $t3, $t3, 2\n");
                mips.append("    addi $t3, $t3, " + baseOffset + "\n");
                mips.append("    add $t4, $sp, $t3\n");
                mips.append("    lw $t5, 0($t4)\n");
                guardarValor("$t5", destino);
            } else {
                Simbolo arraySymbol = tablaSimbolos.buscar(nombreArray);
                if (arraySymbol != null && arraySymbol.getDimensiones() != null) {
                    String[] dims = arraySymbol.getDimensiones().split("x");
                    int numCols = Integer.parseInt(dims[1]);
                    
                    mips.append("    li $t2, " + numCols + "\n");
                    mips.append("    mul $t3, $t0, $t2\n");
                    mips.append("    add $t3, $t3, $t1\n");
                    mips.append("    sll $t3, $t3, 2\n");
                    mips.append("    la $t4, " + nombreArray + "\n");
                    mips.append("    add $t4, $t4, $t3\n");
                    mips.append("    lw $t5, 0($t4)\n");
                    guardarValor("$t5", destino);
                }
            }
        }
    }

    private void procesarAsignacionArray(String instr) {
        String[] partes = instr.split("=");
        String arrayPart = partes[0].trim();
        String valor = partes[1].trim();
        
        int bracketPos = arrayPart.indexOf('[');
        String nombreArray = arrayPart.substring(0, bracketPos).trim();
        String indices = arrayPart.substring(bracketPos);
        String[] indicesParts = indices.split("\\]\\[");
        
        if (indicesParts.length == 2) {
            String indice1 = indicesParts[0].replace("[", "").trim();
            String indice2 = indicesParts[1].replace("]", "").trim();
            
            cargarValor(indice1, "$t0");
            cargarValor(indice2, "$t1");
            cargarValor(valor, "$t5"); // Valor a guardar
            
            if (offsetsLocales.containsKey(nombreArray + "_base")) {
                int baseOffset = offsetsLocales.get(nombreArray + "_base");
                int numCols = offsetsLocales.get(nombreArray + "_dim2");
                
                mips.append("    li $t2, " + numCols + "\n");
                mips.append("    mul $t3, $t0, $t2\n");
                mips.append("    add $t3, $t3, $t1\n");
                mips.append("    sll $t3, $t3, 2\n");
                mips.append("    addi $t3, $t3, " + baseOffset + "\n");
                mips.append("    add $t4, $sp, $t3\n");
                mips.append("    sw $t5, 0($t4)\n");
            } else {
                Simbolo arraySymbol = tablaSimbolos.buscar(nombreArray);
                if (arraySymbol != null && arraySymbol.getDimensiones() != null) {
                    String[] dims = arraySymbol.getDimensiones().split("x");
                    int numCols = Integer.parseInt(dims[1]);
                    
                    mips.append("    li $t2, " + numCols + "\n");
                    mips.append("    mul $t3, $t0, $t2\n");
                    mips.append("    add $t3, $t3, $t1\n");
                    mips.append("    sll $t3, $t3, 2\n");
                    mips.append("    la $t4, " + nombreArray + "\n");
                    mips.append("    add $t4, $t4, $t3\n");
                    mips.append("    sw $t5, 0($t4)\n");
                }
            }
        }
    }
    
    private void cargarValor(String valor, String registro) {
    valor = valor.trim();
    
    if (valor.isEmpty()) {
        mips.append("    li " + registro + ", 0\n");
        return;
    }
    
    // 1. Manejo de Strings Literales
    if (valor.startsWith("\"")) {
        String etiqueta = mapaStrings.get(valor);
        if (etiqueta != null) {
            mips.append("    la " + registro + ", " + etiqueta + "\n");
        }
        return;
    }
    
    // 2. Manejo de Números Literales (Int/Float)
    if (esNumero(valor)) {
        cargarValorSimple(valor, registro);
        return;
    }

    // 3. Manejo de Caracteres Literales
    if (valor.startsWith("'") && valor.endsWith("'")) {
        String charContent = valor.replaceAll("'", "");
        int ascii = charContent.isEmpty() ? 0 : (int)charContent.charAt(0);
        mips.append("    li " + registro + ", " + ascii + "\n");
        return;
    }
    
    // 4. BUSCAR EN TABLA DE OFFSETS (Variables Locales y Temporales ya existentes)
    // Esto es vital para que ++ y -- funcionen: siempre lee de la memoria.
    if (offsetsLocales.containsKey(valor)) {
        int offset = offsetsLocales.get(valor);
        mips.append("    lw " + registro + ", " + offset + "($sp)    # Cargar local/temp: " + valor + "\n");
        return;
    }
    
    // 5. BUSCAR EN VARIABLES GLOBALES
    Simbolo s = tablaSimbolos.buscar(valor);
    if (s != null && s.getAlcance().equals("GLOBAL")) {
        if (s.getTipo().equals("float")) {
            mips.append("    l.s $f0, " + valor + "\n");
            mips.append("    mfc1 " + registro + ", $f0\n");
        } else {
            mips.append("    lw " + registro + ", " + valor + "            # Cargar global: " + valor + "\n");
        }
        return;
    }
    
    // 6. CORRECCIÓN PARA VARIABLES TEMPORALES NUEVAS (t0, t1, etc.)
    // Si llegamos aquí, es un temporal que aparece por primera vez en una expresión.
    // Reservamos espacio pero NO intentamos un 'lw' porque todavía no tiene valor.
    // Simplemente inicializamos el registro en 0 o lo preparamos.
    int nuevoOffset = offsetActual;
    offsetsLocales.put(valor, nuevoOffset);
    offsetActual += 4;
    
    // Si es un temporal nuevo en el lado derecho de una op, su valor por defecto es 0
    mips.append("    li " + registro + ", 0              # Nuevo temporal: " + valor + "\n");
}
    
    private void cargarValorSimple(String valor, String registro) {
        if (valor.contains(".")) {
            // Float literal
            String etiquetaFloat = "float_const_" + (contadorStrings++);
            mips.insert(mips.indexOf(".text"), "    " + etiquetaFloat + ": .float " + valor + "\n");
            mips.append("    l.s $f0, " + etiquetaFloat + "\n");
            mips.append("    mfc1 " + registro + ", $f0\n");
        } else {
            try {
                long num = Long.parseLong(valor);
                if (num >= -32768 && num <= 32767) {
                    mips.append("    li " + registro + ", " + valor + "\n");
                } else {
                    mips.append("    li " + registro + ", " + valor + "\n");
                }
            } catch (NumberFormatException e) {
                mips.append("    li " + registro + ", 0\n");
            }
        }
    }
    
    private void guardarValor(String registro, String destino) {
    destino = destino.trim();
    
    // Si ya existe en la pila, sobrescribir esa posición
    if (offsetsLocales.containsKey(destino)) {
        int offset = offsetsLocales.get(destino);
        mips.append("    sw " + registro + ", " + offset + "($sp)    # Actualizar memoria\n");
        return;
    }
    
    // Si es global
    Simbolo s = tablaSimbolos.buscar(destino);
    if (s != null && s.getAlcance().equals("GLOBAL")) {
        mips.append("    sw " + registro + ", " + destino + "\n");
        return;
    }
    
    // Si es nuevo
    offsetsLocales.put(destino, offsetActual);
    mips.append("    sw " + registro + ", " + offsetActual + "($sp)\n");
    offsetActual += 4;
}
    
    private boolean esNumero(String str) {
        if (str == null || str.trim().isEmpty()) return false;
        try { Double.parseDouble(str); return true; } catch (NumberFormatException e) { return false; }
    }
}