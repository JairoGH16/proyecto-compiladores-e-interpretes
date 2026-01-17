package parserlexer;

import java.util.ArrayList;

/**
 * Clase Nodo
 * Representa un nodo en el árbol sintáctico
 */
public class Nodo {
    private String etiqueta;     // Nombre del nodo (ej: "programa", "declaracionGlobal")
    private String lexema;       // Valor del token (ej: "x", "int", "5")
    private String tipo;         // Tipo del token (ej: "ID", "INT", "INT_LITERAL")
    private int linea;           // Línea en el código fuente
    private int columna;         // Columna en el código fuente
    private ArrayList<Nodo> hijos;  // Lista de nodos hijos
    
    /**
     * Constructor principal: nodo con etiqueta solamente
     * Usado para nodos no terminales (ej: "programa", "expresion")
     */
    public Nodo(String etiqueta) {
        this.etiqueta = etiqueta;
        this.lexema = null;
        this.tipo = null;
        this.linea = -1;
        this.columna = -1;
        this.hijos = new ArrayList<>();
    }
    
    /**
     * Constructor para nodos terminales (tokens)
     * Usado para hojas del árbol (ej: ID, literales, palabras reservadas)
     */
    public Nodo(String lexema, String tipo) {
        this.etiqueta = lexema;
        this.lexema = lexema;
        this.tipo = tipo;
        this.linea = -1;
        this.columna = -1;
        this.hijos = new ArrayList<>();
    }
    
    /**
     * Constructor completo con línea y columna
     */
    public Nodo(String lexema, String tipo, int linea, int columna) {
        this.etiqueta = lexema;
        this.lexema = lexema;
        this.tipo = tipo;
        this.linea = linea;
        this.columna = columna;
        this.hijos = new ArrayList<>();
    }
    
    // ========== MÉTODOS PARA CONSTRUIR EL ÁRBOL ==========
    
    /**
     * Agregar un hijo al nodo
     */
    public void addHijo(Nodo hijo) {
        if (hijo != null) {
            this.hijos.add(hijo);
        }
    }
    
    /**
     * Agregar múltiples hijos
     */
    public void addHijos(ArrayList<Nodo> nuevosHijos) {
        if (nuevosHijos != null) {
            this.hijos.addAll(nuevosHijos);
        }
    }
    
    // ========== GETTERS ==========
    
    public String getEtiqueta() {
        return etiqueta;
    }
    
    public String getLexema() {
        return lexema;
    }
    
    public String getTipo() {
        return tipo;
    }
    
    public int getLinea() {
        return linea;
    }
    
    public int getColumna() {
        return columna;
    }
    
    public ArrayList<Nodo> getHijos() {
        return hijos;
    }
    
    // ========== SETTERS ==========
    
    public void setLinea(int linea) {
        this.linea = linea;
    }
    
    public void setColumna(int columna) {
        this.columna = columna;
    }
    
    // ========== MÉTODOS PARA VISUALIZACIÓN ==========
    
    /**
     * Imprimir el árbol en consola con formato jerárquico
     */
    public void arbol() {
        imprimirArbol("", true);
    }
    
    /**
     * Método auxiliar recursivo para imprimir el árbol
     */
    private void imprimirArbol(String prefijo, boolean esUltimo) {
        // Imprimir el nodo actual
        System.out.print(prefijo);
        System.out.print(esUltimo ? "└── " : "├── ");
        
        // Mostrar etiqueta y tipo si existe
        if (tipo != null && !tipo.isEmpty()) {
            System.out.println(etiqueta + " [" + tipo + "]");
        } else {
            System.out.println(etiqueta);
        }
        
        // Imprimir los hijos
        for (int i = 0; i < hijos.size(); i++) {
            boolean esUltimoHijo = (i == hijos.size() - 1);
            String nuevoPrefijo = prefijo + (esUltimo ? "    " : "│   ");
            hijos.get(i).imprimirArbol(nuevoPrefijo, esUltimoHijo);
        }
    }
    
    /**
     * Representación en String del árbol (para guardar en archivo)
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        toStringRecursivo(sb, "", true);
        return sb.toString();
    }
    
    /**
     * Método auxiliar recursivo para toString
     */
    private void toStringRecursivo(StringBuilder sb, String prefijo, boolean esUltimo) {
        sb.append(prefijo);
        sb.append(esUltimo ? "└── " : "├── ");
        
        if (tipo != null && !tipo.isEmpty()) {
            sb.append(etiqueta).append(" [").append(tipo).append("]");
        } else {
            sb.append(etiqueta);
        }
        sb.append("\n");
        
        for (int i = 0; i < hijos.size(); i++) {
            boolean esUltimoHijo = (i == hijos.size() - 1);
            String nuevoPrefijo = prefijo + (esUltimo ? "    " : "│   ");
            hijos.get(i).toStringRecursivo(sb, nuevoPrefijo, esUltimoHijo);
        }
    }
    
    /**
     * Contar el número total de nodos en el árbol
     */
    public int contarNodos() {
        int total = 1; // Contar este nodo
        for (Nodo hijo : hijos) {
            total += hijo.contarNodos();
        }
        return total;
    }
    
    /**
     * Calcular la altura del árbol
     */
    public int getAltura() {
        if (hijos.isEmpty()) {
            return 1;
        }
        
        int maxAlturaHijo = 0;
        for (Nodo hijo : hijos) {
            int alturaHijo = hijo.getAltura();
            if (alturaHijo > maxAlturaHijo) {
                maxAlturaHijo = alturaHijo;
            }
        }
        
        return 1 + maxAlturaHijo;
    }
    
    /**
     * Verificar si el nodo es una hoja (no tiene hijos)
     */
    public boolean esHoja() {
        return hijos.isEmpty();
    }
    
    /**
     * Verificar si el nodo es un terminal (tiene tipo)
     */
    public boolean esTerminal() {
        return tipo != null && !tipo.isEmpty();
    }
    
    /**
     * Obtener el número de hijos
     */
    public int getNumHijos() {
        return hijos.size();
    }
    
    /**
     * Obtener un hijo por índice
     */
    public Nodo getHijo(int indice) {
        if (indice >= 0 && indice < hijos.size()) {
            return hijos.get(indice);
        }
        return null;
    }
}