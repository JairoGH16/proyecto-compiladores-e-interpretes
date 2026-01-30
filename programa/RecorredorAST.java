import java.util.ArrayList;
import parserlexer.Nodo;

public class RecorredorAST {
    private TablaSimbolos tablaSimbolos;
    private ArrayList<String> errores;
    private ArrayList<String> erroresSemanticos;
    private java.util.HashSet<String> erroresReportados;
    private String funcionActual;
    
    public RecorredorAST() {
        this.tablaSimbolos = new TablaSimbolos();
        this.errores = new ArrayList<>();
        this.erroresSemanticos = new ArrayList<>();
        this.erroresReportados = new java.util.HashSet<>();
        this.funcionActual = null;
    }
    
    public void recorrer(Nodo raiz) {
        if (raiz == null) {
            System.err.println("Error: El árbol sintáctico es nulo");
            return;
        }
        
        System.out.println("\n========== CONSTRUYENDO TABLA DE SÍMBOLOS ==========");
        recorrerNodo(raiz);
        
        if (!errores.isEmpty()) {
            System.out.println("\n⚠️ ERRORES EN TABLA:");
            for (String error : errores) {
                System.err.println("  - " + error);
            }
        } else {
            System.out.println("\n✓ Tabla construida sin errores");
        }
        
        System.out.println("\n========== ANÁLISIS SEMÁNTICO ==========");
        analizarSemantica(raiz);
        
        if (!erroresSemanticos.isEmpty()) {
            System.out.println("\n❌ ERRORES SEMÁNTICOS:");
            for (String error : erroresSemanticos) {
                System.err.println("  - " + error);
            }
        } else {
            System.out.println("\n✓ Sin errores semánticos");
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
                // Verificar si es return
                if (!nodo.getHijos().isEmpty()) {
                    Nodo primerHijo = nodo.getHijos().get(0);
                    if (primerHijo.getTipo() != null && primerHijo.getTipo().equals("RETURN")) {
                        verificarReturn(nodo);
                    }
                }
                break;
            case "primaria":
                // Verificar si es llamada a función
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
                    // Saltar comas
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
            erroresSemanticos.add("Línea " + linea + 
                ": función '" + nombreFunc + "' esperaba " + tiposEsperados.size() + 
                " argumentos pero recibió " + tiposReales.size());
            return;
        }
        
        for (int i = 0; i < tiposEsperados.size(); i++) {
            if (!tiposEsperados.get(i).equals(tiposReales.get(i)) && 
                !tiposReales.get(i).equals("error")) {
                erroresSemanticos.add("Línea " + linea + 
                    ": argumento " + (i+1) + " de '" + nombreFunc + 
                    "' esperaba '" + tiposEsperados.get(i) + 
                    "' pero recibió '" + tiposReales.get(i) + "'");
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
                    erroresSemanticos.add("Línea " + linea + 
                        ": return tipo '" + tipoReal + 
                        "' pero función '" + funcionActual + 
                        "' retorna '" + tipoEsperado + "'");
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
            
            if (nodo.getHijos().size() >= 2) {
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
            }
        }
        
        if (etiqueta.equals("primaria")) {
            if (!nodo.getHijos().isEmpty()) {
                Nodo hijo = nodo.getHijos().get(0);
                
                // Es llamada a función
                if (esLlamadaFuncion(nodo)) {
                    Simbolo funcion = tablaSimbolos.buscar(hijo.getLexema());
                    if (funcion != null && funcion.getCategoria().equals("funcion")) {
                        nodo.setTipoSemantico(funcion.getTipo());
                        return funcion.getTipo();
                    }
                    return "error";
                }
                
                // Es ID simple
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
                        erroresSemanticos.add("Línea " + linea + 
                            ": variable no declarada '" + hijo.getLexema() + "'");
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
    
    private void recorrerNodo(Nodo nodo) {
        if (nodo == null) return;
        String etiqueta = nodo.getEtiqueta();
        
        switch (etiqueta) {
            case "programa": recorrerPrograma(nodo); break;
            case "declaraciones": recorrerDeclaraciones(nodo); break;
            case "declaracionGlobal": recorrerDeclaracionGlobal(nodo); break;
            case "navidad": recorrerNavidad(nodo); break;
            case "declaracionFuncion": recorrerDeclaracionFuncion(nodo); break;
            case "declaracionLocal": recorrerDeclaracionLocal(nodo); break;
            default:
                for (Nodo hijo : nodo.getHijos()) {
                    recorrerNodo(hijo);
                }
                break;
        }
    }
    
    private void recorrerPrograma(Nodo nodo) {
        for (Nodo hijo : nodo.getHijos()) {
            recorrerNodo(hijo);
        }
    }
    
    private void recorrerDeclaraciones(Nodo nodo) {
        for (Nodo hijo : nodo.getHijos()) {
            recorrerNodo(hijo);
        }
    }
    
    private void recorrerDeclaracionGlobal(Nodo nodo) {
        ArrayList<Nodo> hijos = nodo.getHijos();
        String nombreVar = hijos.get(1).getLexema();
        int linea = hijos.get(1).getLinea();
        if (linea == -1) linea = 0;
        Nodo tercerHijo = hijos.get(2);
        
        if (tercerHijo.getEtiqueta().equals("tipo")) {
            String tipo = extraerTipo(tercerHijo);
            if (!tablaSimbolos.agregarVariable(nombreVar, tipo, linea)) {
                errores.add("Variable global duplicada '" + nombreVar + "' en línea " + linea);
            }
        } else if (tercerHijo.getEtiqueta().equals("array")) {
            String tipoArray = extraerTipoArray(tercerHijo);
            String dimensiones = extraerDimensionesArray(tercerHijo);
            if (!tablaSimbolos.agregarVariable(nombreVar, tipoArray, linea, dimensiones)) {
                errores.add("Array global duplicado '" + nombreVar + "' en línea " + linea);
            }
        }
    }
    
    private void recorrerNavidad(Nodo nodo) {
        tablaSimbolos.entrarAlcance("NAVIDAD");
        for (Nodo hijo : nodo.getHijos()) {
            if (hijo.getEtiqueta().equals("sentencias")) {
                recorrerNodo(hijo);
            }
        }
        tablaSimbolos.salirAlcance();
    }
    
    private void recorrerDeclaracionFuncion(Nodo nodo) {
        ArrayList<Nodo> hijos = nodo.getHijos();
        String nombreFuncion = hijos.get(1).getLexema();
        int linea = hijos.get(1).getLinea();
        if (linea == -1) linea = 0;
        
        // Buscar tipo de retorno
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
            errores.add("Función duplicada '" + nombreFuncion + "' en línea " + linea);
        }
        
        tablaSimbolos.entrarAlcance(nombreFuncion);
        
        for (Nodo hijo : hijos) {
            if (hijo != null && hijo.getEtiqueta().equals("listaParametros")) {
                recorrerParametros(hijo);
            }
        }
        
        for (Nodo hijo : hijos) {
            if (hijo != null && hijo.getEtiqueta().equals("sentencias")) {
                recorrerNodo(hijo);
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
    
    private void recorrerParametros(Nodo nodo) {
        for (Nodo hijo : nodo.getHijos()) {
            if (hijo.getEtiqueta().equals("parametro")) {
                recorrerParametro(hijo);
            }
        }
    }
    
    private void recorrerParametro(Nodo nodo) {
        ArrayList<Nodo> hijos = nodo.getHijos();
        String nombreParam = hijos.get(0).getLexema();
        int linea = hijos.get(0).getLinea();
        if (linea == -1) linea = 0;
        Nodo segundoHijo = hijos.get(1);
        
        if (segundoHijo.getEtiqueta().equals("tipo")) {
            String tipo = extraerTipo(segundoHijo);
            if (!tablaSimbolos.agregarParametro(nombreParam, tipo, linea)) {
                errores.add("Parámetro duplicado '" + nombreParam + "' en línea " + linea);
            }
        } else if (segundoHijo.getEtiqueta().equals("array")) {
            String tipoArray = extraerTipoArray(segundoHijo);
            String dimensiones = extraerDimensionesArray(segundoHijo);
            Simbolo simbolo = new Simbolo(nombreParam, tipoArray, linea, 
                                         tablaSimbolos.getAlcanceActual(), "parametro", dimensiones);
            if (!tablaSimbolos.agregarSimbolo(simbolo)) {
                errores.add("Parámetro array duplicado '" + nombreParam + "' en línea " + linea);
            }
        }
    }
    
    private void recorrerDeclaracionLocal(Nodo nodo) {
        ArrayList<Nodo> hijos = nodo.getHijos();
        String nombreVar = hijos.get(1).getLexema();
        int linea = hijos.get(1).getLinea();
        if (linea == -1) linea = 0;
        Nodo tercerHijo = hijos.get(2);
        
        if (tercerHijo.getEtiqueta().equals("tipo")) {
            String tipo = extraerTipo(tercerHijo);
            if (!tablaSimbolos.agregarVariable(nombreVar, tipo, linea)) {
                String alcance = tablaSimbolos.getAlcanceActual();
                errores.add("Variable local duplicada '" + nombreVar + "' en alcance '" + 
                           alcance + "', línea " + linea);
            }
        } else if (tercerHijo.getEtiqueta().equals("array")) {
            String tipoArray = extraerTipoArray(tercerHijo);
            String dimensiones = extraerDimensionesArray(tercerHijo);
            if (!tablaSimbolos.agregarVariable(nombreVar, tipoArray, linea, dimensiones)) {
                String alcance = tablaSimbolos.getAlcanceActual();
                errores.add("Array local duplicado '" + nombreVar + "' en alcance '" + 
                           alcance + "', línea " + linea);
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
    public ArrayList<String> getErrores() { return errores; }
    public ArrayList<String> getErroresSemanticos() { return erroresSemanticos; }
    public boolean tieneErrores() { return !errores.isEmpty() || !erroresSemanticos.isEmpty(); }
}