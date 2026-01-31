import java.util.ArrayList;
import parserlexer.Nodo;

public class RecorredorAST {
    private TablaSimbolos tablaSimbolos;
    private ArrayList<String> erroresSemanticos;
    private java.util.HashSet<String> erroresReportados;
    private String funcionActual;
    private int lineaActual = -1;
    
    public RecorredorAST() {
        this.tablaSimbolos = new TablaSimbolos();
        this.erroresSemanticos = new ArrayList<>();
        this.erroresReportados = new java.util.HashSet<>();
        this.funcionActual = null;
    }
    
    public void recorrer(Nodo raiz) {
        if (raiz == null) {
            System.err.println("Error: El árbol sintáctico es nulo");
            return;
        }
        construirTabla(raiz, false);
    }
    
    public void recorrerYAnalizar(Nodo raiz) {
        if (raiz == null) {
            System.err.println("Error: El árbol sintáctico es nulo");
            return;
        }
        
        construirTabla(raiz, true);
        
        System.out.println("========== ANÁLISIS SEMÁNTICO ==========\n");
        
        if (!erroresSemanticos.isEmpty()) {
            System.out.println("❌ ERRORES SEMÁNTICOS:");
            for (String error : erroresSemanticos) {
                System.err.println("  - " + error);
            }
            System.out.println("\nTotal: " + erroresSemanticos.size() + 
                             " errores encontrados");
        } else {
            System.out.println("✓ Sin errores semánticos");
        }
    }
    
    private int obtenerLinea(Nodo nodo) {
        if (nodo == null) return -1;
        if (nodo.getLinea() > 0) return nodo.getLinea();
        
        for (Nodo hijo : nodo.getHijos()) {
            int linea = obtenerLinea(hijo);
            if (linea > 0) return linea;
        }
        return -1;
    }

    private void construirTabla(Nodo nodo, boolean analizarSemantica) {
        if (nodo == null) return;
        String etiqueta = nodo.getEtiqueta();
        
        switch (etiqueta) {
            case "programa": 
                construirPrograma(nodo, analizarSemantica); 
                break;
            case "declaraciones": 
                construirDeclaraciones(nodo, analizarSemantica); 
                break;
            case "declaracionGlobal": 
                construirDeclaracionGlobal(nodo, analizarSemantica); 
                break;
            case "navidad": 
                construirNavidad(nodo, analizarSemantica); 
                break;
            case "declaracionFuncion": 
                construirDeclaracionFuncion(nodo, analizarSemantica); 
                break;
            case "declaracionLocal": 
                construirDeclaracionLocal(nodo, analizarSemantica); 
                break;
            default:
                for (Nodo hijo : nodo.getHijos()) {
                    construirTabla(hijo, analizarSemantica);
                }
                break;
        }
    }
    
    private void analizarSemanticaNodo(Nodo nodo) {
        if (nodo == null) return;
        
        String etiqueta = nodo.getEtiqueta();
        
        switch (etiqueta) {
            case "declaracionLocal":
            case "declaracionGlobal":
                verificarAsignacion(nodo);
                break;
            case "asignacion":
                verificarAsignacionSimple(nodo);
                break;
            case "decideOf":
            case "decide":
                verificarDecideOf(nodo);
                break;
            case "caso":
                verificarCaso(nodo);
                break;
            case "loop":
                verificarLoop(nodo);
                break;
            case "acceso":
            case "accesoArray":
                verificarAccesoArray(nodo);
                break;
            case "sentencia":
                if (!nodo.getHijos().isEmpty()) {
                    Nodo primerHijo = nodo.getHijos().get(0);
                    if (primerHijo.getTipo() != null && 
                        primerHijo.getTipo().equals("RETURN")) {
                        verificarReturn(nodo);
                    }
                }
                break;
            case "primaria":
                if (esLlamadaFuncion(nodo)) {
                    verificarLlamadaFuncion(nodo);
                }
                break;
        }
        
        for (Nodo hijo : nodo.getHijos()) {
            analizarSemanticaNodo(hijo);
        }
    }
    
    private boolean esLlamadaFuncion(Nodo nodo) {
        ArrayList<Nodo> hijos = nodo.getHijos();
        if (hijos.size() < 2) return false;
        
        Nodo primero = hijos.get(0);
        Nodo segundo = hijos.get(1);
        
        return primero.getTipo() != null && primero.getTipo().equals("ID") &&
               segundo.getTipo() != null && segundo.getTipo().equals("OPEN_PAREN");
    }
    
    private void verificarAsignacionSimple(Nodo nodo) {
        ArrayList<Nodo> hijos = nodo.getHijos();
        if (hijos.size() < 3) return;
        
        Nodo nodoID = hijos.get(0);
        Nodo nodoExpresion = hijos.get(2);
        
        if (nodoID.getTipo() == null || !nodoID.getTipo().equals("ID")) return;
        
        String nombreVar = nodoID.getLexema();
        int linea = obtenerLinea(nodo);
        
        Simbolo simbolo = tablaSimbolos.buscar(nombreVar);
        if (simbolo == null) {
            String claveError = "var_notfound_" + nombreVar + "_" + linea;
            if (!erroresReportados.contains(claveError)) {
                erroresReportados.add(claveError);
                erroresSemanticos.add("Línea " + linea + 
                    ": variable no declarada '" + nombreVar + "'");
            }
            return;
        }
        
        String tipoVar = simbolo.getTipo();
        String tipoExpr = inferirTipo(nodoExpresion);
        
        if (!tipoVar.equals(tipoExpr) && !tipoExpr.equals("error")) {
            String claveError = "asign_simple_" + nombreVar + "_" + linea;
            if (!erroresReportados.contains(claveError)) {
                erroresReportados.add(claveError);
                erroresSemanticos.add("Línea " + linea + 
                    ": asignación incompatible - variable '" + nombreVar + 
                    "' tipo '" + tipoVar + "' pero asignado '" + tipoExpr + "'");
            }
        }
    }
    
    private void verificarDecideOf(Nodo nodo) {
        // decide of no tiene condición general
    }
    
    private void verificarCaso(Nodo nodo) {
        ArrayList<Nodo> hijos = nodo.getHijos();
        if (hijos.isEmpty()) return;
        
        Nodo expresion = hijos.get(0);
        String tipoExpr = inferirTipo(expresion);
        
        if (!tipoExpr.equals("bool") && !tipoExpr.equals("error")) {
            int linea = obtenerLinea(nodo);
            String claveError = "caso_cond_" + linea;
            if (!erroresReportados.contains(claveError)) {
                erroresReportados.add(claveError);
                erroresSemanticos.add("Línea " + linea + 
                    ": condición debe ser tipo 'bool' pero es '" + tipoExpr + "'");
            }
        }
    }
    
    private void verificarLoop(Nodo nodo) {
        buscarExitWhen(nodo);
    }
    
    private void buscarExitWhen(Nodo nodo) {
    if (nodo == null) return;
    
    String etiqueta = nodo.getEtiqueta();
    
    if (etiqueta.equals("loop")) {
        ArrayList<Nodo> hijos = nodo.getHijos();
        boolean encontreExit = false;
        
        for (int i = 0; i < hijos.size(); i++) {
            Nodo hijo = hijos.get(i);
            
            if (hijo.getTipo() != null && hijo.getTipo().equals("EXIT")) {
                encontreExit = true;
            }
            
            if (encontreExit && hijo.getTipo() != null && 
                hijo.getTipo().equals("WHEN")) {
                if (i + 1 < hijos.size()) {
                    Nodo expresion = hijos.get(i + 1);
                    if (expresion.getTipo() == null || 
                        !expresion.getTipo().equals("ENDL")) {
                        String tipoCond = inferirTipo(expresion);
                        if (!tipoCond.equals("bool") && 
                            !tipoCond.equals("error")) {
                            int linea = obtenerLinea(expresion);
                            if (linea <= 0) linea = obtenerLinea(hijo);
                            
                            String claveError = "exitwhen_" + linea;
                            if (!erroresReportados.contains(claveError)) {
                                erroresReportados.add(claveError);
                                erroresSemanticos.add("Línea " + linea + 
                                    ": exit when debe tener condición 'bool' " +
                                    "pero es '" + tipoCond + "'");
                            }
                        }
                        return;
                    }
                }
            }
        }
    }
    
    for (Nodo hijo : nodo.getHijos()) {
        buscarExitWhen(hijo);
    }
}
    
    private void verificarAccesoArray(Nodo nodo) {
        ArrayList<Nodo> hijos = nodo.getHijos();
        if (hijos.isEmpty()) return;
        
        Nodo nodoID = hijos.get(0);
        if (nodoID.getTipo() == null || !nodoID.getTipo().equals("ID")) return;
        
        String nombreArray = nodoID.getLexema();
        Simbolo array = tablaSimbolos.buscar(nombreArray);
        
        int linea = obtenerLinea(nodo);
        
        if (array == null) {
            String claveError = "arr_notfound_" + nombreArray + "_" + linea;
            if (!erroresReportados.contains(claveError)) {
                erroresReportados.add(claveError);
                erroresSemanticos.add("Línea " + linea + 
                    ": array no declarado '" + nombreArray + "'");
            }
            return;
        }
        
        int numIndices = 0;
        for (Nodo hijo : hijos) {
            if (hijo.getEtiqueta().startsWith("expresion") || 
                hijo.getEtiqueta().equals("literal") ||
                hijo.getEtiqueta().equals("primaria") ||
                hijo.getEtiqueta().equals("aditiva") ||
                hijo.getEtiqueta().equals("literalBooleano")) {
                String tipoIndice = inferirTipo(hijo);
                if (!tipoIndice.equals("int") && !tipoIndice.equals("error")) {
                    String claveError = "arr_idx_" + nombreArray + "_" + 
                                       linea + "_" + numIndices;
                    if (!erroresReportados.contains(claveError)) {
                        erroresReportados.add(claveError);
                        erroresSemanticos.add("Línea " + linea + 
                            ": índice de array debe ser 'int' pero es '" + 
                            tipoIndice + "'");
                    }
                }
                numIndices++;
            }
        }
        
        String dims = array.getDimensiones();
        if (dims != null && !dims.equals("?x?")) {
            int dimsEsperadas = dims.contains("x") ? 2 : 1;
            if (numIndices != dimsEsperadas) {
                String claveError = "arr_dims_" + nombreArray + "_" + linea;
                if (!erroresReportados.contains(claveError)) {
                    erroresReportados.add(claveError);
                    erroresSemanticos.add("Línea " + linea + 
                        ": array '" + nombreArray + "' tiene " + dimsEsperadas + 
                        " dimensiones pero se accede con " + numIndices);
                }
            }
        }
    }
    
    private void verificarAsignacion(Nodo nodo) {
    ArrayList<Nodo> hijos = nodo.getHijos();
    
    Nodo nodoID = null;
    Nodo nodoTipo = null;
    Nodo nodoExpresion = null;
    boolean tieneAsignacion = false;
    
    for (int i = 0; i < hijos.size(); i++) {
        Nodo hijo = hijos.get(i);
        
        if (hijo.getTipo() != null && hijo.getTipo().equals("ID") && 
            nodoID == null) {
            nodoID = hijo;
        } else if (hijo.getEtiqueta().equals("tipo") && nodoTipo == null) {
            nodoTipo = hijo;
        } else if (hijo.getTipo() != null && 
                  hijo.getTipo().equals("ASSIGN")) {
            tieneAsignacion = true;
        } else if (tieneAsignacion && nodoExpresion == null) {
            if (hijo.getTipo() == null || !hijo.getTipo().equals("ENDL")) {
                nodoExpresion = hijo;
            }
        }
    }
    
    if (nodoID == null || nodoTipo == null || nodoExpresion == null) return;
    
    int lineaOriginal = lineaActual;
    lineaActual = obtenerLinea(nodo);
    
    String tipoDeclarado = extraerTipo(nodoTipo);
    String tipoExpresion = inferirTipo(nodoExpresion);
    
    lineaActual = lineaOriginal;
    
    if (!tipoDeclarado.equals(tipoExpresion) && 
        !tipoExpresion.equals("error")) {
        int linea = obtenerLinea(nodo);
        String claveError = "asign_decl_" + nodoID.getLexema() + "_" + linea;
        if (!erroresReportados.contains(claveError)) {
            erroresReportados.add(claveError);
            erroresSemanticos.add("Línea " + linea + 
                ": variable '" + nodoID.getLexema() + "' tipo '" + 
                tipoDeclarado + "' pero asignado '" + tipoExpresion + "'");
        }
    }
}
    
    private void verificarLlamadaFuncion(Nodo nodo) {
        ArrayList<Nodo> hijos = nodo.getHijos();
        if (hijos.isEmpty()) return;
        
        Nodo nodoID = hijos.get(0);
        String nombreFunc = nodoID.getLexema();
        int linea = obtenerLinea(nodo);
        
        Simbolo funcion = tablaSimbolos.buscar(nombreFunc);
        
        if (funcion == null || !funcion.getCategoria().equals("funcion")) {
            String claveError = "func_notfound_" + nombreFunc + "_" + linea;
            if (!erroresReportados.contains(claveError)) {
                erroresReportados.add(claveError);
                erroresSemanticos.add("Línea " + linea + 
                    ": función '" + nombreFunc + "' no declarada");
            }
            return;
        }
        
        ArrayList<String> tiposEsperados = funcion.getTiposParametros();
        ArrayList<String> tiposReales = new ArrayList<>();
        
        for (Nodo hijo : hijos) {
            if (hijo.getEtiqueta().equals("listaExpresiones")) {
                for (Nodo expr : hijo.getHijos()) {
                    String etiq = expr.getEtiqueta();
                    String tipo = expr.getTipo();
                    boolean esComa = (etiq != null && etiq.equals(",")) || 
                                    (tipo != null && tipo.equals("COMMA"));
                    if (!esComa) {
                        String tipoArg = inferirTipo(expr);
                        tiposReales.add(tipoArg);
                    }
                }
            }
        }
        
        if (tiposEsperados.size() != tiposReales.size()) {
            String claveError = "args_count_" + nombreFunc + "_" + linea;
            if (!erroresReportados.contains(claveError)) {
                erroresReportados.add(claveError);
                erroresSemanticos.add("Línea " + linea + 
                    ": función '" + nombreFunc + "' esperaba " + 
                    tiposEsperados.size() + " argumentos pero recibió " + 
                    tiposReales.size());
            }
            return;
        }
        
        for (int i = 0; i < tiposEsperados.size(); i++) {
            if (!tiposEsperados.get(i).equals(tiposReales.get(i)) && 
                !tiposReales.get(i).equals("error")) {
                String claveError = "args_type_" + nombreFunc + "_" + 
                                   i + "_" + linea;
                if (!erroresReportados.contains(claveError)) {
                    erroresReportados.add(claveError);
                    erroresSemanticos.add("Línea " + linea + 
                        ": argumento " + (i+1) + " de '" + nombreFunc + 
                        "' esperaba '" + tiposEsperados.get(i) + 
                        "' pero recibió '" + tiposReales.get(i) + "'");
                }
            }
        }
    }
    
    private void verificarReturn(Nodo nodo) {
        if (funcionActual == null) {
            int linea = obtenerLinea(nodo);
            String claveError = "return_nofunction_" + linea;
            if (!erroresReportados.contains(claveError)) {
                erroresReportados.add(claveError);
                erroresSemanticos.add("Línea " + linea + 
                    ": return fuera de función");
            }
            return;
        }
        
        Simbolo funcion = tablaSimbolos.buscar(funcionActual);
        if (funcion == null) return;
        
        String tipoEsperado = funcion.getTipo();
        
        for (Nodo hijo : nodo.getHijos()) {
            if (hijo.getTipo() == null || 
                (!hijo.getTipo().equals("RETURN") && 
                 !hijo.getTipo().equals("ENDL"))) {
                String tipoReal = inferirTipo(hijo);
                
                if (!tipoEsperado.equals(tipoReal) && 
                    !tipoReal.equals("error")) {
                    int linea = obtenerLinea(nodo);
                    String claveError = "return_type_" + funcionActual + 
                                       "_" + linea;
                    if (!erroresReportados.contains(claveError)) {
                        erroresReportados.add(claveError);
                        erroresSemanticos.add("Línea " + linea + 
                            ": return tipo '" + tipoReal + "' pero función '" + 
                            funcionActual + "' retorna '" + tipoEsperado + "'");
                    }
                }
                break;
            }
        }
    }
    
    private String inferirTipo(Nodo nodo) {
        if (nodo == null) return "error";
        
        String etiqueta = nodo.getEtiqueta();
        
        if (etiqueta.equals("literal")) {
            if (nodo.getHijos().isEmpty()) return "error";
            Nodo hijo = nodo.getHijos().get(0);
            String tipo = hijo.getTipo();
            
            if (tipo == null) return "error";
            
            String tipoInferido = mapearTipoLiteral(tipo);
            hijo.setTipoSemantico(tipoInferido);
            nodo.setTipoSemantico(tipoInferido);
            return tipoInferido;
        }
        
        if (etiqueta.equals("literalBooleano")) {
            if (nodo.getHijos().isEmpty()) return "error";
            Nodo hijo = nodo.getHijos().get(0);
            hijo.setTipoSemantico("bool");
            nodo.setTipoSemantico("bool");
            return "bool";
        }
        
        if (etiqueta.equals("aditiva") || etiqueta.equals("multiplicativa") || 
            etiqueta.equals("suma") || etiqueta.equals("resta") || 
            etiqueta.equals("multiplicacion") || etiqueta.equals("division")) {
            
            if (nodo.getHijos().size() >= 3) {
                String tipo1 = inferirTipo(nodo.getHijos().get(0));
                String tipo2 = inferirTipo(nodo.getHijos().get(2));
                
                if (tipo1.equals("int") && tipo2.equals("int")) {
                    nodo.setTipoSemantico("int");
                    return "int";
                }
                if (tipo1.equals("float") && tipo2.equals("float")) {
                    nodo.setTipoSemantico("float");
                    return "float";
                }
                if ((tipo1.equals("int") || tipo1.equals("float")) &&
                    (tipo2.equals("int") || tipo2.equals("float"))) {
                    nodo.setTipoSemantico("float");
                    return "float";
                }
                
                if (!tipo1.equals("error") && !tipo2.equals("error")) {
                    int linea = obtenerLinea(nodo);
                    String claveError = "arit_" + linea + "_" + tipo1 + "_" + tipo2;
                    if (!erroresReportados.contains(claveError)) {
                        erroresReportados.add(claveError);
                        erroresSemanticos.add("Línea " + linea + 
                            ": operación aritmética inválida entre '" + tipo1 + 
                            "' y '" + tipo2 + "'");
                    }
                }
                return "error";
            }
        }
        
        if (etiqueta.equals("relacional") || etiqueta.equals("comparacion") ||
            etiqueta.equals("mayor") || etiqueta.equals("menor") ||
            etiqueta.equals("mayorIgual") || etiqueta.equals("menorIgual") ||
            etiqueta.equals("igual") || etiqueta.equals("diferente")) {
            
            if (nodo.getHijos().size() >= 3) {
                String tipo1 = inferirTipo(nodo.getHijos().get(0));
                String tipo2 = inferirTipo(nodo.getHijos().get(2));
                
                if ((tipo1.equals("int") || tipo1.equals("float")) &&
                    (tipo2.equals("int") || tipo2.equals("float"))) {
                    nodo.setTipoSemantico("bool");
                    return "bool";
                }
                
                if (tipo1.equals(tipo2) && !tipo1.equals("error")) {
                    nodo.setTipoSemantico("bool");
                    return "bool";
                }
                
                if (!tipo1.equals("error") && !tipo2.equals("error")) {
                    int linea = obtenerLinea(nodo);
                    String claveError = "comp_" + linea + "_" + tipo1 + "_" + tipo2;
                    if (!erroresReportados.contains(claveError)) {
                        erroresReportados.add(claveError);
                        erroresSemanticos.add("Línea " + linea + 
                            ": comparación entre tipos incompatibles: '" + 
                            tipo1 + "' y '" + tipo2 + "'");
                    }
                }
            }
            nodo.setTipoSemantico("bool");
            return "bool";
        }
        
        if (etiqueta.equals("logica") || etiqueta.equals("conjuncion") || 
            etiqueta.equals("disyuncion") || etiqueta.equals("negacion")) {
            
            if (etiqueta.equals("negacion")) {
                if (nodo.getHijos().size() >= 2) {
                    String tipoOperando = inferirTipo(nodo.getHijos().get(1));
                    if (!tipoOperando.equals("bool") && 
                        !tipoOperando.equals("error")) {
                        int linea = obtenerLinea(nodo);
                        String claveError = "not_" + linea + "_" + tipoOperando;
                        if (!erroresReportados.contains(claveError)) {
                            erroresReportados.add(claveError);
                            erroresSemanticos.add("Línea " + linea + 
                                ": operador NOT requiere operando 'bool' pero es '" + 
                                tipoOperando + "'");
                        }
                    }
                }
            } else {
                if (nodo.getHijos().size() >= 3) {
                    String tipo1 = inferirTipo(nodo.getHijos().get(0));
                    String tipo2 = inferirTipo(nodo.getHijos().get(2));
                    
                    if (!tipo1.equals("bool") && !tipo1.equals("error")) {
                        int linea = obtenerLinea(nodo);
                        String claveError = "log_left_" + linea + "_" + tipo1;
                        if (!erroresReportados.contains(claveError)) {
                            erroresReportados.add(claveError);
                            erroresSemanticos.add("Línea " + linea + 
                                ": operador lógico requiere 'bool' pero " +
                                "lado izquierdo es '" + tipo1 + "'");
                        }
                    }
                    if (!tipo2.equals("bool") && !tipo2.equals("error")) {
                        int linea = obtenerLinea(nodo);
                        String claveError = "log_right_" + linea + "_" + tipo2;
                        if (!erroresReportados.contains(claveError)) {
                            erroresReportados.add(claveError);
                            erroresSemanticos.add("Línea " + linea + 
                                ": operador lógico requiere 'bool' pero " +
                                "lado derecho es '" + tipo2 + "'");
                        }
                    }
                }
            }
            nodo.setTipoSemantico("bool");
            return "bool";
        }
        
        if (etiqueta.equals("acceso") || etiqueta.equals("accesoArray")) {
            ArrayList<Nodo> hijos = nodo.getHijos();
            if (!hijos.isEmpty()) {
                Nodo nodoID = hijos.get(0);
                if (nodoID.getTipo() != null && nodoID.getTipo().equals("ID")) {
                    Simbolo array = tablaSimbolos.buscar(nodoID.getLexema());
                    if (array != null) {
                        String tipo = array.getTipo();
                        nodo.setTipoSemantico(tipo);
                        return tipo;
                    }
                }
            }
            return "error";
        }
        
        if (etiqueta.equals("primaria")) {
            if (!nodo.getHijos().isEmpty()) {
                Nodo hijo = nodo.getHijos().get(0);
                
                if (esLlamadaFuncion(nodo)) {
                    Simbolo funcion = tablaSimbolos.buscar(hijo.getLexema());
                    if (funcion != null && 
                        funcion.getCategoria().equals("funcion")) {
                        nodo.setTipoSemantico(funcion.getTipo());
                        return funcion.getTipo();
                    }
                    return "error";
                }
                
                if (hijo.getTipo() != null && hijo.getTipo().equals("ID")) {
                    Simbolo simbolo = tablaSimbolos.buscar(hijo.getLexema());
                    if (simbolo != null) {
                        String tipo = simbolo.getTipo();
                        hijo.setTipoSemantico(tipo);
                        nodo.setTipoSemantico(tipo);
                        return tipo;
                    }
                    int linea = (lineaActual > 0) ? lineaActual : obtenerLinea(nodo);
                    if (linea <= 0) linea = 1;
                    String claveError = "var_notfound_id_" + 
                                       hijo.getLexema() + "_" + linea;
                    if (!erroresReportados.contains(claveError)) {
                        erroresReportados.add(claveError);
                        erroresSemanticos.add("Línea " + linea + 
                            ": variable no declarada '" + hijo.getLexema() + "'");
                    }
                    return "error";
                }
                
                if (hijo.getTipo() != null && 
                    hijo.getTipo().equals("OPEN_PAREN")) {
                    if (nodo.getHijos().size() >= 2) {
                        Nodo expresion = nodo.getHijos().get(1);
                        return inferirTipo(expresion);
                    }
                }
                
                return inferirTipo(hijo);
            }
        }
        
        for (Nodo hijo : nodo.getHijos()) {
            String tipoHijo = inferirTipo(hijo);
            if (!tipoHijo.equals("error")) {
                nodo.setTipoSemantico(tipoHijo);
                return tipoHijo;
            }
        }
        
        return "error";
    }
    
    private String mapearTipoLiteral(String tipoToken) {
        switch (tipoToken) {
            case "INT_LITERAL": return "int";
            case "FLOAT_LITERAL": return "float";
            case "STRING_LITERAL": return "string";
            case "CHAR_LITERAL": return "char";
            case "TRUE":
            case "FALSE": return "bool";
            default: return "error";
        }
    }
    
    private void construirPrograma(Nodo nodo, boolean analizarSemantica) {
        for (Nodo hijo : nodo.getHijos()) {
            construirTabla(hijo, analizarSemantica);
        }
    }
    
    private void construirDeclaraciones(Nodo nodo, boolean analizarSemantica) {
        for (Nodo hijo : nodo.getHijos()) {
            construirTabla(hijo, analizarSemantica);
        }
    }
    
    private void construirDeclaracionGlobal(Nodo nodo, 
                                           boolean analizarSemantica) {
        ArrayList<Nodo> hijos = nodo.getHijos();
        String nombreVar = hijos.get(1).getLexema();
        int linea = hijos.get(1).getLinea();
        if (linea == -1) linea = 0;
        Nodo tercerHijo = hijos.get(2);
        
        if (tercerHijo.getEtiqueta().equals("tipo")) {
            String tipo = extraerTipo(tercerHijo);
            if (!tablaSimbolos.agregarVariable(nombreVar, tipo, linea)) {
                erroresSemanticos.add("Línea " + linea + 
                    ": Variable global duplicada '" + nombreVar + "'");
            }
        } else if (tercerHijo.getEtiqueta().equals("array")) {
            String tipoArray = extraerTipoArray(tercerHijo);
            String dimensiones = extraerDimensionesArray(tercerHijo);
            if (!tablaSimbolos.agregarVariable(nombreVar, tipoArray, linea, 
                                              dimensiones)) {
                erroresSemanticos.add("Línea " + linea + 
                    ": Array global duplicado '" + nombreVar + "'");
            }
        }
        
        if (analizarSemantica) {
            analizarSemanticaNodo(nodo);
        }
    }
    
    private void construirNavidad(Nodo nodo, boolean analizarSemantica) {
        tablaSimbolos.entrarAlcance("NAVIDAD");
        for (Nodo hijo : nodo.getHijos()) {
            if (hijo.getEtiqueta().equals("sentencias")) {
                construirTabla(hijo, analizarSemantica);
                if (analizarSemantica) {
                    analizarSemanticaNodo(hijo);
                }
            }
        }
        tablaSimbolos.salirAlcance();
    }
    
    private void construirDeclaracionFuncion(Nodo nodo, 
                                            boolean analizarSemantica) {
        ArrayList<Nodo> hijos = nodo.getHijos();
        String nombreFuncion = hijos.get(1).getLexema();
        int linea = hijos.get(1).getLinea();
        if (linea == -1) linea = 0;
        
        String tipoRetorno = "unknown";
        for (Nodo hijo : hijos) {
            if (hijo.getEtiqueta().equals("tipo")) {
                tipoRetorno = extraerTipo(hijo);
                break;
            }
        }
        
        Simbolo funcion = new Simbolo(nombreFuncion, tipoRetorno, linea, 
                                      "GLOBAL", "funcion");
        
        for (Nodo hijo : hijos) {
            if (hijo != null && hijo.getEtiqueta().equals("listaParametros")) {
                extraerTiposParametros(hijo, funcion);
            }
        }
        
        if (!tablaSimbolos.agregarSimbolo(funcion)) {
            erroresSemanticos.add("Línea " + linea + 
                ": Función duplicada '" + nombreFuncion + "'");
        }
        
        tablaSimbolos.entrarAlcance(nombreFuncion);
        funcionActual = nombreFuncion;
        
        for (Nodo hijo : hijos) {
            if (hijo != null && hijo.getEtiqueta().equals("listaParametros")) {
                construirParametros(hijo, analizarSemantica);
            }
        }
        
        for (Nodo hijo : hijos) {
            if (hijo != null && hijo.getEtiqueta().equals("sentencias")) {
                construirTabla(hijo, analizarSemantica);
                if (analizarSemantica) {
                    analizarSemanticaNodo(hijo);
                }
            }
        }
        
        tablaSimbolos.salirAlcance();
        funcionActual = null;
    }
    
    private void extraerTiposParametros(Nodo listaParams, Simbolo funcion) {
        for (Nodo hijo : listaParams.getHijos()) {
            if (hijo.getEtiqueta().equals("parametro")) {
                ArrayList<Nodo> hijosParam = hijo.getHijos();
                if (hijosParam.size() >= 2) {
                    Nodo nodoTipo = hijosParam.get(1);
                    if (nodoTipo.getEtiqueta().equals("tipo")) {
                        String tipo = extraerTipo(nodoTipo);
                        funcion.agregarTipoParametro(tipo);
                    } else if (nodoTipo.getEtiqueta().equals("array")) {
                        String tipoArray = extraerTipoArray(nodoTipo);
                        funcion.agregarTipoParametro(tipoArray);
                    }
                }
            }
        }
    }
    
    private void construirParametros(Nodo nodo, boolean analizarSemantica) {
        for (Nodo hijo : nodo.getHijos()) {
            if (hijo.getEtiqueta().equals("parametro")) {
                construirParametro(hijo, analizarSemantica);
            }
        }
    }
    
    private void construirParametro(Nodo nodo, boolean analizarSemantica) {
        ArrayList<Nodo> hijos = nodo.getHijos();
        String nombreParam = hijos.get(0).getLexema();
        int linea = hijos.get(0).getLinea();
        if (linea == -1) linea = 0;
        Nodo segundoHijo = hijos.get(1);
        
        if (segundoHijo.getEtiqueta().equals("tipo")) {
            String tipo = extraerTipo(segundoHijo);
            if (!tablaSimbolos.agregarParametro(nombreParam, tipo, linea)) {
                erroresSemanticos.add("Línea " + linea + 
                    ": Parámetro duplicado '" + nombreParam + "'");
            }
        } else if (segundoHijo.getEtiqueta().equals("array")) {
            String tipoArray = extraerTipoArray(segundoHijo);
            String dimensiones = extraerDimensionesArray(segundoHijo);
            Simbolo simbolo = new Simbolo(nombreParam, tipoArray, linea, 
                                         tablaSimbolos.getAlcanceActual(), 
                                         "parametro", dimensiones);
            if (!tablaSimbolos.agregarSimbolo(simbolo)) {
                erroresSemanticos.add("Línea " + linea + 
                    ": Parámetro array duplicado '" + nombreParam + "'");
            }
        }
    }
    
    private void construirDeclaracionLocal(Nodo nodo, 
                                          boolean analizarSemantica) {
        ArrayList<Nodo> hijos = nodo.getHijos();
        String nombreVar = hijos.get(1).getLexema();
        int linea = hijos.get(1).getLinea();
        if (linea == -1) linea = 0;
        Nodo tercerHijo = hijos.get(2);
        
        if (tercerHijo.getEtiqueta().equals("tipo")) {
            String tipo = extraerTipo(tercerHijo);
            if (!tablaSimbolos.agregarVariable(nombreVar, tipo, linea)) {
                String alcance = tablaSimbolos.getAlcanceActual();
                erroresSemanticos.add("Línea " + linea + 
                    ": Variable local duplicada '" + nombreVar + 
                    "' en alcance '" + alcance + "'");
            }
        } else if (tercerHijo.getEtiqueta().equals("array")) {
            String tipoArray = extraerTipoArray(tercerHijo);
            String dimensiones = extraerDimensionesArray(tercerHijo);
            if (!tablaSimbolos.agregarVariable(nombreVar, tipoArray, linea, 
                                              dimensiones)) {
                String alcance = tablaSimbolos.getAlcanceActual();
                erroresSemanticos.add("Línea " + linea + 
                    ": Array local duplicado '" + nombreVar + 
                    "' en alcance '" + alcance + "'");
            }
        }
        
        if (analizarSemantica) {
            analizarSemanticaNodo(nodo);
        }
    }
    
    private String extraerTipo(Nodo nodoTipo) {
        if (nodoTipo == null || nodoTipo.getHijos().isEmpty()) return "unknown";
        return nodoTipo.getHijos().get(0).getLexema();
    }
    
    private String extraerTipoArray(Nodo nodoArray) {
        if (nodoArray == null || nodoArray.getHijos().isEmpty()) return "unknown";
        return nodoArray.getHijos().get(0).getLexema();
    }
    
    private String extraerDimensionesArray(Nodo nodoArray) {
        ArrayList<Nodo> hijos = nodoArray.getHijos();
        if (hijos.size() < 6) return "?x?";
        String dim1 = extraerValorExpresion(hijos.get(2));
        String dim2 = extraerValorExpresion(hijos.get(4));
        return dim1 + "x" + dim2;
    }
    
    private String extraerValorExpresion(Nodo expr) {
        if (expr == null) return "?";
        Nodo actual = expr;
        
        while (actual != null) {
            String etiqueta = actual.getEtiqueta();
            
            if (etiqueta.equals("literal")) {
                ArrayList<Nodo> hijos = actual.getHijos();
                if (!hijos.isEmpty()) {
                    return hijos.get(0).getLexema();
                }
            }
            
            if (etiqueta.equals("primaria")) {
                ArrayList<Nodo> hijos = actual.getHijos();
                if (!hijos.isEmpty()) {
                    Nodo hijo = hijos.get(0);
                    if (hijo.getTipo() != null && !hijo.getTipo().isEmpty()) {
                        return hijo.getLexema();
                    }
                }
            }
            
            if (actual.getHijos().isEmpty()) break;
            actual = actual.getHijos().get(0);
        }
        
        return "?";
    }
    
    public TablaSimbolos getTablaSimbolos() { return tablaSimbolos; }
    public ArrayList<String> getErrores() { return new ArrayList<>(); }
    public ArrayList<String> getErroresSemanticos() { return erroresSemanticos; }
    public boolean tieneErrores() { return !erroresSemanticos.isEmpty(); }
}