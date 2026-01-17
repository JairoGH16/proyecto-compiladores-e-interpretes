/**
 * Clase que representa un símbolo en la tabla de símbolos
 * Cada símbolo contiene información sobre variables, funciones, etc.
 */
public class Simbolo {
    private String nombre;           // Nombre del símbolo (ej: "x", "sumar")
    private String tipo;             // Tipo (ej: "int", "char")
    private int linea;               // Línea donde se declaró
    private String alcance;          // Alcance (ej: "GLOBAL", "NAVIDAD", "sumar")
    private String categoria;        // Categoría: "variable", "funcion", "parametro"
    private String dimensiones;      // Para arreglos: "3x3", "5x2", etc.
    private String valorInicial;     // Valor inicial si existe
    
    /**
     * Constructor completo
     */
    public Simbolo(String nombre, String tipo, int linea, String alcance, String categoria) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.linea = linea;
        this.alcance = alcance;
        this.categoria = categoria;
        this.dimensiones = null;
        this.valorInicial = null;
    }
    
    /**
     * Constructor con dimensiones (para arreglos)
     */
    public Simbolo(String nombre, String tipo, int linea, String alcance, 
                   String categoria, String dimensiones) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.linea = linea;
        this.alcance = alcance;
        this.categoria = categoria;
        this.dimensiones = dimensiones;
        this.valorInicial = null;
    }
    
    // ========== GETTERS ==========
    
    public String getNombre() {
        return nombre;
    }
    
    public String getTipo() {
        return tipo;
    }
    
    public int getLinea() {
        return linea;
    }
    
    public String getAlcance() {
        return alcance;
    }
    
    public String getCategoria() {
        return categoria;
    }
    
    public String getDimensiones() {
        return dimensiones;
    }
    
    public String getValorInicial() {
        return valorInicial;
    }
    
    // ========== SETTERS ==========
    
    public void setDimensiones(String dimensiones) {
        this.dimensiones = dimensiones;
    }
    
    public void setValorInicial(String valorInicial) {
        this.valorInicial = valorInicial;
    }
    
    /**
     * Representación en String del símbolo
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(nombre).append(" | ");
        sb.append(tipo);
        if (dimensiones != null) {
            sb.append("[").append(dimensiones).append("]");
        }
        sb.append(" | línea ").append(linea);
        sb.append(" | ").append(categoria);
        if (valorInicial != null) {
            sb.append(" = ").append(valorInicial);
        }
        return sb.toString();
    }
}