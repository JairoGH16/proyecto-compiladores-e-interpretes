import java.util.*;

/**
 * Clase que maneja la tabla de símbolos completa
 * Organiza símbolos por alcance (GLOBAL, NAVIDAD, funciones, etc.)
 */
public class TablaSimbolos {
    // Mapa: alcance -> lista de símbolos
    private HashMap<String, ArrayList<Simbolo>> tablas;
    
    // Alcance actual (cambia durante el análisis)
    private String alcanceActual;
    
    // Pila de alcances (para manejar alcances anidados)
    private Stack<String> pilaAlcances;
    
    /**
     * Constructor
     */
    public TablaSimbolos() {
        this.tablas = new HashMap<>();
        this.pilaAlcances = new Stack<>();
        this.alcanceActual = "GLOBAL";
        
        // Inicializar alcance global
        this.tablas.put("GLOBAL", new ArrayList<>());
        this.pilaAlcances.push("GLOBAL");
    }
    
    /**
     * Entrar a un nuevo alcance
     * @param nombreAlcance Nombre del alcance (ej: "NAVIDAD", "sumar")
     */
    public void entrarAlcance(String nombreAlcance) {
        this.alcanceActual = nombreAlcance;
        this.pilaAlcances.push(nombreAlcance);
        
        // Crear lista para este alcance si no existe
        if (!tablas.containsKey(nombreAlcance)) {
            tablas.put(nombreAlcance, new ArrayList<>());
        }
    }
    
    /**
     * Salir del alcance actual
     */
    public void salirAlcance() {
        if (pilaAlcances.size() > 1) {
            pilaAlcances.pop();
            alcanceActual = pilaAlcances.peek();
        }
    }
    
    /**
     * Obtener el alcance actual
     */
    public String getAlcanceActual() {
        return alcanceActual;
    }
    
    /**
     * Agregar un símbolo a la tabla
     * @param simbolo El símbolo a agregar
     * @return true si se agregó exitosamente, false si ya existe
     */
    public boolean agregarSimbolo(Simbolo simbolo) {
        String alcance = simbolo.getAlcance();
        
        // Verificar si ya existe en el alcance actual
        if (existeEnAlcance(simbolo.getNombre(), alcance)) {
            return false; // Ya existe, duplicado
        }
        
        // Agregar el símbolo
        ArrayList<Simbolo> simbolosAlcance = tablas.get(alcance);
        if (simbolosAlcance == null) {
            simbolosAlcance = new ArrayList<>();
            tablas.put(alcance, simbolosAlcance);
        }
        simbolosAlcance.add(simbolo);
        
        return true;
    }
    
    /**
     * Agregar una variable al alcance actual
     */
    public boolean agregarVariable(String nombre, String tipo, int linea) {
        Simbolo simbolo = new Simbolo(nombre, tipo, linea, alcanceActual, "variable");
        return agregarSimbolo(simbolo);
    }
    
    /**
     * Agregar una variable con dimensiones (arreglo) al alcance actual
     */
    public boolean agregarVariable(String nombre, String tipo, int linea, String dimensiones) {
        Simbolo simbolo = new Simbolo(nombre, tipo, linea, alcanceActual, "variable", dimensiones);
        return agregarSimbolo(simbolo);
    }
    
    /**
     * Agregar una función al alcance GLOBAL
     */
    public boolean agregarFuncion(String nombre, String tipo, int linea) {
        Simbolo simbolo = new Simbolo(nombre, tipo, linea, "GLOBAL", "funcion");
        return agregarSimbolo(simbolo);
    }
    
    /**
     * Agregar un parámetro a la función actual
     */
    public boolean agregarParametro(String nombre, String tipo, int linea) {
        Simbolo simbolo = new Simbolo(nombre, tipo, linea, alcanceActual, "parametro");
        return agregarSimbolo(simbolo);
    }
    
    /**
     * Verificar si un símbolo existe en un alcance específico
     */
    public boolean existeEnAlcance(String nombre, String alcance) {
        ArrayList<Simbolo> simbolosAlcance = tablas.get(alcance);
        
        if (simbolosAlcance == null) {
            return false;
        }
        
        for (Simbolo s : simbolosAlcance) {
            if (s.getNombre().equals(nombre)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Buscar un símbolo por nombre en el alcance actual
     * Busca primero en el alcance actual, luego en GLOBAL
     */
    public Simbolo buscar(String nombre) {
        // Buscar en alcance actual
        ArrayList<Simbolo> simbolosActual = tablas.get(alcanceActual);
        if (simbolosActual != null) {
            for (Simbolo s : simbolosActual) {
                if (s.getNombre().equals(nombre)) {
                    return s;
                }
            }
        }
        
        // Si no está en alcance actual, buscar en GLOBAL
        if (!alcanceActual.equals("GLOBAL")) {
            ArrayList<Simbolo> simbolosGlobal = tablas.get("GLOBAL");
            if (simbolosGlobal != null) {
                for (Simbolo s : simbolosGlobal) {
                    if (s.getNombre().equals(nombre)) {
                        return s;
                    }
                }
            }
        }

        for (int i = pilaAlcances.size() - 1; i >= 0; i--) {
            String alcance = pilaAlcances.get(i);
            ArrayList<Simbolo> simbolos = tablas.get(alcance);
            if (simbolos != null) {
                for (Simbolo s : simbolos) {
                    if (s.getNombre().equals(nombre)) return s;
                }
            }
        }
        
        return null; // No encontrado
    }
    
    /**
     * Obtener todos los símbolos de un alcance
     */
    public ArrayList<Simbolo> getSimbolosAlcance(String alcance) {
        return tablas.getOrDefault(alcance, new ArrayList<>());
    }
    
    /**
     * Obtener todos los alcances
     */
    public Set<String> getAlcances() {
        return tablas.keySet();
    }
    
    /**
     * Generar reporte completo de la tabla de símbolos
     */
    public String generarReporte() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("\n");
        sb.append("╔════════════════════════════════════════════════════════════════════════╗\n");
        sb.append("║                        TABLA DE SÍMBOLOS                               ║\n");
        sb.append("╚════════════════════════════════════════════════════════════════════════╝\n");
        sb.append("\n");
        
        // Mostrar GLOBAL primero
        if (tablas.containsKey("GLOBAL")) {
            sb.append(generarReporteAlcance("GLOBAL"));
        }
        
        // Mostrar NAVIDAD si existe
        if (tablas.containsKey("NAVIDAD")) {
            sb.append(generarReporteAlcance("NAVIDAD"));
        }
        
        // Mostrar otros alcances (funciones)
        for (String alcance : tablas.keySet()) {
            if (!alcance.equals("GLOBAL") && !alcance.equals("NAVIDAD")) {
                sb.append(generarReporteAlcance(alcance));
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Generar reporte de un alcance específico
     */
    private String generarReporteAlcance(String alcance) {
        StringBuilder sb = new StringBuilder();
        ArrayList<Simbolo> simbolos = tablas.get(alcance);
        
        if (simbolos == null || simbolos.isEmpty()) {
            return ""; // No mostrar alcances vacíos
        }
        
        sb.append("┌─────────────────────────────────────────────────────────────────────┐\n");
        sb.append(String.format("│ ALCANCE: %-58s │\n", alcance));
        sb.append("├─────────────────────────────────────────────────────────────────────┤\n");
        
        // Encabezado
        sb.append(String.format("│ %-15s │ %-15s │ %-8s │ %-12s │\n", 
            "NOMBRE", "TIPO", "LÍNEA", "CATEGORÍA"));
        sb.append("├─────────────────┼─────────────────┼──────────┼──────────────┤\n");
        
        // Símbolos
        for (Simbolo s : simbolos) {
            String tipo = s.getTipo();
            if (s.getDimensiones() != null) {
                tipo += "[" + s.getDimensiones() + "]";
            }
            
            sb.append(String.format("│ %-15s │ %-15s │ %-8d │ %-12s │\n",
                s.getNombre(), tipo, s.getLinea(), s.getCategoria()));
        }
        
        sb.append("└─────────────────┴─────────────────┴──────────┴──────────────┘\n");
        sb.append("\n");
        
        return sb.toString();
    }
    
    /**
     * Imprimir tabla de símbolos (para debugging)
     */
    public void imprimir() {
        System.out.println(generarReporte());
    }
}