import java.util.ArrayList;
import parserlexer.Nodo;

public class RecorredorAST {
    private TablaSimbolos tablaSimbolos;
    private ArrayList<String> erroresSemanticos;
    private java.util.HashSet<String> erroresReportados;
    private String funcionActual;
    
    public RecorredorAST() {
        this.tablaSimbolos = new TablaSimbolos();
        this.erroresSemanticos = new ArrayList<>();
        this.erroresReportados = new java.util.HashSet<>();
        this.funcionActual = null;
    }
    
    // OPCIÓN 3: Solo construir tabla (SIN análisis semántico)
    public void recorrer(Nodo raiz) {
        if (raiz == null) {
            System.err.println("Error: El árbol sintáctico es nulo");
            return;
        }
        construirTabla(raiz);
    }
    
    // OPCIÓN 4: Construir tabla + Análisis semántico
    public void recorrerYAnalizar(Nodo raiz) {
        if (raiz == null) {
            System.err.println("Error: El árbol sintáctico es nulo");
            return;
        }
        
        construirTabla(raiz);
        analizarSemantica(raiz);
        
        System.out.println("========== ANÁLISIS SEMÁNTICO ==========\n");
        
        if (!erroresSemanticos.isEmpty()) {
            System.out.println("❌ ERRORES SEMÁNTICOS:");
            for (String error : erroresSemanticos) {
                System.err.println("  - " + error);
            }
            System.out.println("\nTotal: " + erroresSemanticos.size() + " errores encontrados");
        } else {
            System.out.println("✓ Sin errores semánticos");
        }
    }
    
    private void construirTabla(Nodo nodo) {
        if (nodo == null) return;
        String etiqueta = nodo.getEtiqueta();
        
        switch (etiqueta) {
            case "programa": construirPrograma(nodo); break;
            case "declaraciones": construirDeclaraciones(nodo); break;
            case "declaracionGlobal": construirDeclaracionGlobal(nodo); break;
            case "navidad": construirNavidad(nodo); break;
            case "declaracionFuncion": construirDeclaracionFuncion(nodo); break;
            case "declaracionLocal": construirDeclaracionLocal(nodo); break;
            default:
                for (Nodo hijo : nodo.getHijos()) {
                    construirTabla(hijo);
                }
                break;
        }
    }
    
    private void analizarSemantica(Nodo nodo) {
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
            case "decide":
                verificarCondicion(nodo);
                break;
            case "loop":
                verificarLoop(nodo);
                break;
            case "acceso":
                verificarAccesoArray(nodo);
                break;
            case "declaracionFuncion":
                String nombreFunc = nodo.getHijos().get(1).getLexema();
                String funcionAnterior = funcionActual;
                funcionActual = nombreFunc;
                for (Nodo hijo : nodo.getHijos()) {
                    analizarSemantica(hijo);
                }
                funcionActual = funcionAnterior;
                return;
            case "sentencia":
                if (!nodo.getHijos().isEmpty()) {
                    Nodo primerHijo = nodo.getHijos().get(0);
                    if (primerHijo.getTipo() != null && primerHijo.getTipo().equals("RETURN")) {
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
            analizarSemantica(hijo);
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
        int linea = nodoID.getLinea();
        if (linea == -1) linea = 0;
        
        Simbolo simbolo = tablaSimbolos.buscar(nombreVar);
        if (simbolo == null) {
            String claveError = nombreVar + "_" + linea;
            if (!erroresReportados.contains(claveError)) {
                erroresReportados.add(claveError);
                erroresSemanticos.add("Línea " + linea + ": variable no declarada '" + nombreVar + "'");
            }
            return;
        }
        
        String tipoVar = simbolo.getTipo();
        String tipoExpr = inferirTipo(nodoExpresion);
        
        if (!tipoVar.equals(tipoExpr) && !tipoExpr.equals("error")) {
            erroresSemanticos.add("Línea " + linea + 
                ": asignación incompatible - variable '" + nombreVar + 
                "' tipo '" + tipoVar + "' pero asignado '" + tipoExpr + "'");
        }
    }
    
    private void verificarCondicion(Nodo nodo) {
        for (Nodo hijo : nodo.getHijos()) {
            if (hijo.getEtiqueta().startsWith("relacional") || 
                hijo.getEtiqueta().startsWith("logica") ||
                hijo.getEtiqueta().equals("expresion") ||
                hijo.getEtiqueta().equals("comparacion") ||
                hijo.getEtiqueta().equals("aditiva") ||
                hijo.getEtiqueta().equals("multiplicativa")) {
                
                String tipoCondicion = inferirTipo(hijo);
                if (!tipoCondicion.equals("bool") && !tipoCondicion.equals("error")) {
                    erroresSemanticos.add("Condición debe ser tipo 'bool' pero es '" + tipoCondicion + "'");
                }
                break;
            }
        }
    }
    
    private void verificarLoop(Nodo nodo) {
        buscarExitWhen(nodo);
    }
    
    private void buscarExitWhen(Nodo nodo) {
        if (nodo == null) return;
        
        if (nodo.getEtiqueta().equals("exitWhen")) {
            for (Nodo hijo : nodo.getHijos()) {
                String tipoHijo = hijo.getTipo();
                if (tipoHijo == null || (!tipoHijo.equals("EXIT") && !tipoHijo.equals("WHEN") && 
                    !tipoHijo.equals("ENDL"))) {
                    String tipoCond = inferirTipo(hijo);
                    if (!tipoCond.equals("bool") && !tipoCond.equals("error")) {
                        erroresSemanticos.add("Exit when debe tener condición 'bool' pero es '" + tipoCond + "'");
                    }
                    return;
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
        
        if (array == null) {
            int linea = nodoID.getLinea();
            if (linea == -1) linea = 0;
            erroresSemanticos.add("Línea " + linea + ": array no declarado '" + nombreArray + "'");
            return;
        }
        
        int numIndices = 0;
        for (Nodo hijo : hijos) {
            if (hijo.getEtiqueta().startsWith("expresion") || 
                hijo.getEtiqueta().equals("literal") ||
                hijo.getEtiqueta().equals("primaria") ||
                hijo.getEtiqueta().equals("aditiva")) {
                String tipoIndice = inferirTipo(hijo);
                if (!tipoIndice.equals("int") && !tipoIndice.equals("error")) {
                    erroresSemanticos.add("Índice de array debe ser 'int' pero es '" + tipoIndice + "'");
                }
                numIndices++;
            }
        }
        
        String dims = array.getDimensiones();
        if (dims != null && !dims.equals("?x?")) {
            int dimsEsperadas = dims.contains("x") ? 2 : 1;
            if (numIndices != dimsEsperadas) {
                erroresSemanticos.add("Array '" + nombreArray + 
                    "' tiene " + dimsEsperadas + " dimensiones pero se accede con " + numIndices);
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
            
            if (hijo.getTipo() != null && hijo.getTipo().equals("ID") && nodoID == null) {
                nodoID = hijo;
            } else if (hijo.getEtiqueta().equals("tipo") && nodoTipo == null) {
                nodoTipo = hijo;
            } else if (hijo.getTipo() != null && hijo.getTipo().equals("ASSIGN")) {
                tieneAsignacion = true;
            } else if (tieneAsignacion && nodoExpresion == null) {
                if (hijo.getTipo() == null || !hijo.getTipo().equals("ENDL")) {
                    nodoExpresion = hijo;
                }
            }
        }
        
        if (nodoID == null || nodoTipo == null || nodoExpresion == null) return;
        
        String tipoDeclarado = extraerTipo(nodoTipo);
        String tipoExpresion = inferirTipo(nodoExpresion);
        
        if (!tipoDeclarado.equals(tipoExpresion) && !tipoExpresion.equals("error")) {
            int linea = nodoID.getLinea();
            if (linea == -1) linea = 0;
            erroresSemanticos.add("Línea " + linea + 
                ": variable '" + nodoID.getLexema() + 
                "' tipo '" + tipoDeclarado + "' pero asignado '" + tipoExpresion + "'");
        }
    }
    
    private void verificarLlamadaFuncion(Nodo nodo) {
        ArrayList<Nodo> hijos = nodo.getHijos();
        if (hijos.isEmpty()) return;
        
        Nodo nodoID = hijos.get(0);
        String nombreFunc = nodoID.getLexema();
        int linea = nodoID.getLinea();
        if (linea == -1) linea = 0;
        
        Simbolo funcion = tablaSimbolos.buscar(nombreFunc);
        
        if (funcion == null || !funcion.getCategoria().equals("funcion")) {
            String claveError = "func_" + nombreFunc + "_" + linea;
            if (!erroresReportados.contains(claveError)) {
                erroresReportados.add(claveError);
                erroresSemanticos.add("Línea " + linea + ": función '" + nombreFunc + "' no declarada");
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
            erroresSemanticos.add("Línea " + linea + ": función '" + nombreFunc + "' esperaba " + 
                tiposEsperados.size() + " argumentos pero recibió " + tiposReales.size());
            return;
        }
        
        for (int i = 0; i < tiposEsperados.size(); i++) {
            if (!tiposEsperados.get(i).equals(tiposReales.get(i)) && 
                !tiposReales.get(i).equals("error")) {
                erroresSemanticos.add("Línea " + linea + ": argumento " + (i+1) + " de '" + nombreFunc + 
                    "' esperaba '" + tiposEsperados.get(i) + "' pero recibió '" + tiposReales.get(i) + "'");
            }
        }
    }
    
    private void verificarReturn(Nodo nodo) {
        if (funcionActual == null) {
            erroresSemanticos.add("Return fuera de función");
            return;
        }
        
        Simbolo funcion = tablaSimbolos.buscar(funcionActual);
        if (funcion == null) return;
        
        String tipoEsperado = funcion.getTipo();
        
        for (Nodo hijo : nodo.getHijos()) {
            if (hijo.getTipo() == null || (!hijo.getTipo().equals("RETURN") && !hijo.getTipo().equals("ENDL"))) {
                String tipoReal = inferirTipo(hijo);
                
                if (!tipoEsperado.equals(tipoReal) && !tipoReal.equals("error")) {
                    int linea = nodo.getHijos().get(0).getLinea();
                    if (linea == -1) linea = 0;
                    erroresSemanticos.add("Línea " + linea + ": return tipo '" + tipoReal + 
                        "' pero función '" + funcionActual + "' retorna '" + tipoEsperado + "'");
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
                    erroresSemanticos.add("Operación aritmética inválida entre '" + tipo1 + "' y '" + tipo2 + "'");
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
                
                if (tipo1.equals(tipo2)) {
                    nodo.setTipoSemantico("bool");
                    return "bool";
                }
                
                if (!tipo1.equals("error") && !tipo2.equals("error")) {
                    erroresSemanticos.add("Comparación entre tipos incompatibles: '" + tipo1 + "' y '" + tipo2 + "'");
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
                    if (!tipoOperando.equals("bool") && !tipoOperando.equals("error")) {
                        erroresSemanticos.add("Operador NOT requiere operando 'bool' pero es '" + tipoOperando + "'");
                    }
                }
            } else {
                if (nodo.getHijos().size() >= 3) {
                    String tipo1 = inferirTipo(nodo.getHijos().get(0));
                    String tipo2 = inferirTipo(nodo.getHijos().get(2));
                    
                    if (!tipo1.equals("bool") && !tipo1.equals("error")) {
                        erroresSemanticos.add("Operador lógico requiere 'bool' pero lado izquierdo es '" + tipo1 + "'");
                    }
                    if (!tipo2.equals("bool") && !tipo2.equals("error")) {
                        erroresSemanticos.add("Operador lógico requiere 'bool' pero lado derecho es '" + tipo2 + "'");
                    }
                }
            }
            nodo.setTipoSemantico("bool");
            return "bool";
        }
        
        if (etiqueta.equals("acceso")) {
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
                    if (funcion != null && funcion.getCategoria().equals("funcion")) {
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
                    int linea = hijo.getLinea();
                    if (linea == -1) linea = 0;
                    String claveError = hijo.getLexema() + "_" + linea;
                    if (!erroresReportados.contains(claveError)) {
                        erroresReportados.add(claveError);
                        erroresSemanticos.add("Línea " + linea + ": variable no declarada '" + hijo.getLexema() + "'");
                    }
                    return "error";
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
    
    private void construirPrograma(Nodo nodo) {
        for (Nodo hijo : nodo.getHijos()) {
            construirTabla(hijo);
        }
    }
    
    private void construirDeclaraciones(Nodo nodo) {
        for (Nodo hijo : nodo.getHijos()) {
            construirTabla(hijo);
        }
    }
    
    private void construirDeclaracionGlobal(Nodo nodo) {
        ArrayList<Nodo> hijos = nodo.getHijos();
        String nombreVar = hijos.get(1).getLexema();
        int linea = hijos.get(1).getLinea();
        if (linea == -1) linea = 0;
        Nodo tercerHijo = hijos.get(2);
        
        if (tercerHijo.getEtiqueta().equals("tipo")) {
            String tipo = extraerTipo(tercerHijo);
            if (!tablaSimbolos.agregarVariable(nombreVar, tipo, linea)) {
                erroresSemanticos.add("Línea " + linea + ": Variable global duplicada '" + nombreVar + "'");
            }
        } else if (tercerHijo.getEtiqueta().equals("array")) {
            String tipoArray = extraerTipoArray(tercerHijo);
            String dimensiones = extraerDimensionesArray(tercerHijo);
            if (!tablaSimbolos.agregarVariable(nombreVar, tipoArray, linea, dimensiones)) {
                erroresSemanticos.add("Línea " + linea + ": Array global duplicado '" + nombreVar + "'");
            }
        }
    }
    
    private void construirNavidad(Nodo nodo) {
        tablaSimbolos.entrarAlcance("NAVIDAD");
        for (Nodo hijo : nodo.getHijos()) {
            if (hijo.getEtiqueta().equals("sentencias")) {
                construirTabla(hijo);
            }
        }
        tablaSimbolos.salirAlcance();
    }
    
    private void construirDeclaracionFuncion(Nodo nodo) {
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
        
        Simbolo funcion = new Simbolo(nombreFuncion, tipoRetorno, linea, "GLOBAL", "funcion");
        
        for (Nodo hijo : hijos) {
            if (hijo != null && hijo.getEtiqueta().equals("listaParametros")) {
                extraerTiposParametros(hijo, funcion);
            }
        }
        
        if (!tablaSimbolos.agregarSimbolo(funcion)) {
            erroresSemanticos.add("Línea " + linea + ": Función duplicada '" + nombreFuncion + "'");
        }
        
        tablaSimbolos.entrarAlcance(nombreFuncion);
        
        for (Nodo hijo : hijos) {
            if (hijo != null && hijo.getEtiqueta().equals("listaParametros")) {
                construirParametros(hijo);
            }
        }
        
        for (Nodo hijo : hijos) {
            if (hijo != null && hijo.getEtiqueta().equals("sentencias")) {
                construirTabla(hijo);
            }
        }
        
        tablaSimbolos.salirAlcance();
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
    
    private void construirParametros(Nodo nodo) {
        for (Nodo hijo : nodo.getHijos()) {
            if (hijo.getEtiqueta().equals("parametro")) {
                construirParametro(hijo);
            }
        }
    }
    
    private void construirParametro(Nodo nodo) {
        ArrayList<Nodo> hijos = nodo.getHijos();
        String nombreParam = hijos.get(0).getLexema();
        int linea = hijos.get(0).getLinea();
        if (linea == -1) linea = 0;
        Nodo segundoHijo = hijos.get(1);
        
        if (segundoHijo.getEtiqueta().equals("tipo")) {
            String tipo = extraerTipo(segundoHijo);
            if (!tablaSimbolos.agregarParametro(nombreParam, tipo, linea)) {
                erroresSemanticos.add("Línea " + linea + ": Parámetro duplicado '" + nombreParam + "'");
            }
        } else if (segundoHijo.getEtiqueta().equals("array")) {
            String tipoArray = extraerTipoArray(segundoHijo);
            String dimensiones = extraerDimensionesArray(segundoHijo);
            Simbolo simbolo = new Simbolo(nombreParam, tipoArray, linea, 
                                         tablaSimbolos.getAlcanceActual(), "parametro", dimensiones);
            if (!tablaSimbolos.agregarSimbolo(simbolo)) {
                erroresSemanticos.add("Línea " + linea + ": Parámetro array duplicado '" + nombreParam + "'");
            }
        }
    }
    
    private void construirDeclaracionLocal(Nodo nodo) {
        ArrayList<Nodo> hijos = nodo.getHijos();
        String nombreVar = hijos.get(1).getLexema();
        int linea = hijos.get(1).getLinea();
        if (linea == -1) linea = 0;
        Nodo tercerHijo = hijos.get(2);
        
        if (tercerHijo.getEtiqueta().equals("tipo")) {
            String tipo = extraerTipo(tercerHijo);
            if (!tablaSimbolos.agregarVariable(nombreVar, tipo, linea)) {
                String alcance = tablaSimbolos.getAlcanceActual();
                erroresSemanticos.add("Línea " + linea + ": Variable local duplicada '" + nombreVar + 
                           "' en alcance '" + alcance + "'");
            }
        } else if (tercerHijo.getEtiqueta().equals("array")) {
            String tipoArray = extraerTipoArray(tercerHijo);
            String dimensiones = extraerDimensionesArray(tercerHijo);
            if (!tablaSimbolos.agregarVariable(nombreVar, tipoArray, linea, dimensiones)) {
                String alcance = tablaSimbolos.getAlcanceActual();
                erroresSemanticos.add("Línea " + linea + ": Array local duplicado '" + nombreVar + 
                           "' en alcance '" + alcance + "'");
            }
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