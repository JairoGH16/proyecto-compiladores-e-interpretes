import java.util.ArrayList;
import parserlexer.Nodo;

/**
 * Clase RecorredorAST
 * Objetivo: Recorrer el árbol sintáctico y llenar la tabla de símbolos
 * Detecta declaraciones de variables, funciones, parámetros y maneja alcances
 */
public class RecorredorAST {
    private TablaSimbolos tablaSimbolos;
    private ArrayList<String> errores;
    
    /**
     * Constructor
     */
    public RecorredorAST() {
        this.tablaSimbolos = new TablaSimbolos();
        this.errores = new ArrayList<>();
    }
    
    /**
     * Método principal: recorre el árbol completo
     */
    public void recorrer(Nodo raiz) {
        if (raiz == null) {
            System.err.println("Error: El árbol sintáctico es nulo");
            return;
        }
        
        System.out.println("\n========== CONSTRUYENDO TABLA DE SÍMBOLOS ==========");
        recorrerNodo(raiz);
        
        // Mostrar errores si los hay
        if (!errores.isEmpty()) {
            System.out.println("\n⚠️ ERRORES SEMÁNTICOS ENCONTRADOS:");
            for (String error : errores) {
                System.err.println("  - " + error);
            }
        } else {
            System.out.println("\n✓ Tabla de símbolos construida sin errores");
        }
    }
    
    /**
     * Recorrer un nodo recursivamente
     */
    private void recorrerNodo(Nodo nodo) {
        if (nodo == null) return;
        
        String etiqueta = nodo.getEtiqueta();
        
        // Dependiendo del tipo de nodo, realizar acciones
        switch (etiqueta) {
            case "programa":
                recorrerPrograma(nodo);
                break;
            case "declaracionGlobal":
                recorrerDeclaracionGlobal(nodo);
                break;
            case "navidad":
                recorrerNavidad(nodo);
                break;
            case "declaracionFuncion":
                recorrerDeclaracionFuncion(nodo);
                break;
            case "declaracionLocal":
                recorrerDeclaracionLocal(nodo);
                break;
            default:
                // Para otros nodos, simplemente recorrer sus hijos
                for (Nodo hijo : nodo.getHijos()) {
                    recorrerNodo(hijo);
                }
                break;
        }
    }
    
    /**
     * Recorrer el nodo programa
     */
    private void recorrerPrograma(Nodo nodo) {
        // El programa tiene: declaracionesGlobales, navidad, declaracionesFunciones
        for (Nodo hijo : nodo.getHijos()) {
            recorrerNodo(hijo);
        }
    }
    
    /**
     * Recorrer una declaración global (WORLD)
     * Formatos:
     * - WORLD ID tipo ENDL
     * - WORLD ID tipo = expresion ENDL
     * - WORLD ID array = ¡ inicializacionArray ! ENDL
     */
    private void recorrerDeclaracionGlobal(Nodo nodo) {
        ArrayList<Nodo> hijos = nodo.getHijos();
        
        // hijos[0] = "world"
        // hijos[1] = ID
        String nombreVar = hijos.get(1).getLexema();
        int linea = hijos.get(1).getLinea();
        
        // Si la línea es -1, usar 0 como valor por defecto
        if (linea == -1) linea = 0;
        
        // Determinar si es array o tipo simple
        Nodo tercerHijo = hijos.get(2);
        
        if (tercerHijo.getEtiqueta().equals("tipo")) {
            // Variable simple
            String tipo = extraerTipo(tercerHijo);
            
            boolean agregado = tablaSimbolos.agregarVariable(nombreVar, tipo, linea);
            
            if (!agregado) {
                errores.add("Variable global duplicada '" + nombreVar + "' en línea " + linea);
            }
        } else if (tercerHijo.getEtiqueta().equals("array")) {
            // Array
            String tipoArray = extraerTipoArray(tercerHijo);
            String dimensiones = extraerDimensionesArray(tercerHijo);
            
            boolean agregado = tablaSimbolos.agregarVariable(nombreVar, tipoArray, linea, dimensiones);
            
            if (!agregado) {
                errores.add("Array global duplicado '" + nombreVar + "' en línea " + linea);
            }
        }
    }
    
    /**
     * Recorrer el bloque NAVIDAD
     */
    private void recorrerNavidad(Nodo nodo) {
        // Entrar al alcance NAVIDAD
        tablaSimbolos.entrarAlcance("NAVIDAD");
        
        // Recorrer las sentencias dentro de NAVIDAD
        for (Nodo hijo : nodo.getHijos()) {
            if (hijo.getEtiqueta().equals("sentencias")) {
                recorrerNodo(hijo);
            }
        }
        
        // Salir del alcance NAVIDAD
        tablaSimbolos.salirAlcance();
    }
    
    /**
     * Recorrer una declaración de función (GIFT)
     */
    private void recorrerDeclaracionFuncion(Nodo nodo) {
        ArrayList<Nodo> hijos = nodo.getHijos();
        
        // hijos[0] = "gift"
        // hijos[1] = ID (nombre función)
        // hijos[2] = "¿"
        // hijos[3] = parámetros (puede ser null)
        // hijos[4] = "?"
        // hijos[5] = tipo retorno
        
        String nombreFuncion = hijos.get(1).getLexema();
        int linea = hijos.get(1).getLinea();
        
        // Si la línea es -1, usar 0 como valor por defecto
        if (linea == -1) linea = 0;
        
        String tipoRetorno = extraerTipo(hijos.get(5));
        
        // Agregar la función al alcance GLOBAL
        boolean agregado = tablaSimbolos.agregarFuncion(nombreFuncion, tipoRetorno, linea);
        
        if (!agregado) {
            errores.add("Función duplicada '" + nombreFuncion + "' en línea " + linea);
        }
        
        // Entrar al alcance de la función
        tablaSimbolos.entrarAlcance(nombreFuncion);
        
        // Procesar parámetros si existen
        for (Nodo hijo : hijos) {
            if (hijo != null && hijo.getEtiqueta().equals("listaParametros")) {
                recorrerParametros(hijo);
            }
        }
        
        // Procesar el cuerpo de la función (sentencias)
        for (Nodo hijo : hijos) {
            if (hijo != null && hijo.getEtiqueta().equals("sentencias")) {
                recorrerNodo(hijo);
            }
        }
        
        // Salir del alcance de la función
        tablaSimbolos.salirAlcance();
    }
    
    /**
     * Recorrer los parámetros de una función
     */
    private void recorrerParametros(Nodo nodo) {
        for (Nodo hijo : nodo.getHijos()) {
            if (hijo.getEtiqueta().equals("parametro")) {
                recorrerParametro(hijo);
            }
        }
    }
    
    /**
     * Recorrer un parámetro individual
     * Formato: ID tipo | ID array
     */
    private void recorrerParametro(Nodo nodo) {
        ArrayList<Nodo> hijos = nodo.getHijos();
        
        // hijos[0] = ID
        String nombreParam = hijos.get(0).getLexema();
        int linea = hijos.get(0).getLinea();
        
        // Si la línea es -1, usar 0 como valor por defecto
        if (linea == -1) linea = 0;
        
        // hijos[1] = tipo o array
        Nodo segundoHijo = hijos.get(1);
        
        if (segundoHijo.getEtiqueta().equals("tipo")) {
            // Parámetro simple
            String tipo = extraerTipo(segundoHijo);
            
            boolean agregado = tablaSimbolos.agregarParametro(nombreParam, tipo, linea);
            
            if (!agregado) {
                errores.add("Parámetro duplicado '" + nombreParam + "' en línea " + linea);
            }
        } else if (segundoHijo.getEtiqueta().equals("array")) {
            // Parámetro array
            String tipoArray = extraerTipoArray(segundoHijo);
            String dimensiones = extraerDimensionesArray(segundoHijo);
            
            Simbolo simbolo = new Simbolo(nombreParam, tipoArray, linea, 
                                         tablaSimbolos.getAlcanceActual(), "parametro", dimensiones);
            
            boolean agregado = tablaSimbolos.agregarSimbolo(simbolo);
            
            if (!agregado) {
                errores.add("Parámetro array duplicado '" + nombreParam + "' en línea " + linea);
            }
        }
    }
    
    /**
     * Recorrer una declaración local (LOCAL)
     * Formatos:
     * - LOCAL ID tipo ENDL
     * - LOCAL ID tipo = expresion ENDL
     * - LOCAL ID array = ¡ inicializacionArray ! ENDL
     */
    private void recorrerDeclaracionLocal(Nodo nodo) {
        ArrayList<Nodo> hijos = nodo.getHijos();
        
        // hijos[0] = "local"
        // hijos[1] = ID
        String nombreVar = hijos.get(1).getLexema();
        int linea = hijos.get(1).getLinea();
        
        // Si la línea es -1, usar 0 como valor por defecto
        if (linea == -1) linea = 0;
        
        // Determinar si es array o tipo simple
        Nodo tercerHijo = hijos.get(2);
        
        if (tercerHijo.getEtiqueta().equals("tipo")) {
            // Variable simple
            String tipo = extraerTipo(tercerHijo);
            
            boolean agregado = tablaSimbolos.agregarVariable(nombreVar, tipo, linea);
            
            if (!agregado) {
                String alcance = tablaSimbolos.getAlcanceActual();
                errores.add("Variable local duplicada '" + nombreVar + "' en alcance '" + 
                           alcance + "', línea " + linea);
            }
        } else if (tercerHijo.getEtiqueta().equals("array")) {
            // Array
            String tipoArray = extraerTipoArray(tercerHijo);
            String dimensiones = extraerDimensionesArray(tercerHijo);
            
            boolean agregado = tablaSimbolos.agregarVariable(nombreVar, tipoArray, linea, dimensiones);
            
            if (!agregado) {
                String alcance = tablaSimbolos.getAlcanceActual();
                errores.add("Array local duplicado '" + nombreVar + "' en alcance '" + 
                           alcance + "', línea " + linea);
            }
        }
    }
    
    /**
     * Extraer el tipo de un nodo "tipo"
     * El tipo está en el primer hijo
     */
    private String extraerTipo(Nodo nodoTipo) {
        if (nodoTipo == null || nodoTipo.getHijos().isEmpty()) {
            return "unknown";
        }
        
        Nodo primerHijo = nodoTipo.getHijos().get(0);
        return primerHijo.getLexema();
    }
    
    /**
     * Extraer el tipo base de un array (int o char)
     */
    private String extraerTipoArray(Nodo nodoArray) {
        if (nodoArray == null || nodoArray.getHijos().isEmpty()) {
            return "unknown";
        }
        
        // El primer hijo es el tipo (int o char)
        Nodo primerHijo = nodoArray.getHijos().get(0);
        return primerHijo.getLexema();
    }
    
    /**
     * Extraer dimensiones de un array
     * Formato: tipo [ expr , expr ]
     */
    private String extraerDimensionesArray(Nodo nodoArray) {
        ArrayList<Nodo> hijos = nodoArray.getHijos();
        
        // hijos[0] = tipo (int/char)
        // hijos[1] = "["
        // hijos[2] = expresion (dim1)
        // hijos[3] = ","
        // hijos[4] = expresion (dim2)
        // hijos[5] = "]"
        
        if (hijos.size() < 6) {
            return "?x?";
        }
        
        String dim1 = extraerValorExpresion(hijos.get(2));
        String dim2 = extraerValorExpresion(hijos.get(4));
        
        return dim1 + "x" + dim2;
    }
    
    /**
     * Intenta extraer el valor literal de una expresión
     * Si no es un literal, retorna "?"
     */
    private String extraerValorExpresion(Nodo expr) {
        if (expr == null) return "?";
        
        // Navegar por la jerarquía hasta encontrar un literal
        Nodo actual = expr;
        
        while (actual != null) {
            String etiqueta = actual.getEtiqueta();
            
            // Si llegamos a un literal, extraer su valor
            if (etiqueta.equals("literal")) {
                ArrayList<Nodo> hijos = actual.getHijos();
                if (!hijos.isEmpty()) {
                    return hijos.get(0).getLexema();
                }
            }
            
            // Si llegamos a una primaria con ID o literal
            if (etiqueta.equals("primaria")) {
                ArrayList<Nodo> hijos = actual.getHijos();
                if (!hijos.isEmpty()) {
                    Nodo hijo = hijos.get(0);
                    if (hijo.getTipo() != null && !hijo.getTipo().isEmpty()) {
                        return hijo.getLexema();
                    }
                }
            }
            
            // Descender al primer hijo
            if (actual.getHijos().isEmpty()) {
                break;
            }
            actual = actual.getHijos().get(0);
        }
        
        return "?";
    }
    
    /**
     * Obtener la tabla de símbolos
     */
    public TablaSimbolos getTablaSimbolos() {
        return tablaSimbolos;
    }
    
    /**
     * Obtener lista de errores
     */
    public ArrayList<String> getErrores() {
        return errores;
    }
    
    /**
     * Verificar si hubo errores
     */
    public boolean tieneErrores() {
        return !errores.isEmpty();
    }
}