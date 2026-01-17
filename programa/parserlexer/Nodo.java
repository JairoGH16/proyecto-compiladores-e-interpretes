package parserlexer;

import java.util.ArrayList;

/**
 * Clase Nodo para construir el Árbol Sintáctico Concreto (Parse Tree)
 * Proyecto 2 - Compiladores e Intérpretes
 * Estudiantes: Rafael Odio Mendoza, Jairo González Hidalgo
 * 
 * Representa tanto terminales como no-terminales de la gramática
 * Construcción descendente: se crea desde la raíz (programa) hacia las hojas (terminales)
 */
public class Nodo {
    private String lexema;  // Texto del nodo (nombre del símbolo o valor del token)
    private String tipo;    // Tipo del nodo (nombre de la producción o terminal)
    private ArrayList<Nodo> hijos;  // Hijos del nodo
    
    /**
     * Constructor para nodos con lexema y tipo
     * @param lexema Texto del nodo
     * @param tipo Tipo del símbolo (terminal o no-terminal)
     */
    public Nodo(String lexema, String tipo) {
        this.lexema = lexema;
        this.tipo = tipo;
        this.hijos = new ArrayList<>();
    }
    
    /**
     * Constructor simplificado (solo lexema)
     * @param lexema Texto del nodo
     */
    public Nodo(String lexema) {
        this.lexema = lexema;
        this.tipo = "";
        this.hijos = new ArrayList<>();
    }
    
    // Getters y Setters
    public String getLexema() {
        return lexema;
    }
    
    public void setLexema(String lexema) {
        this.lexema = lexema;
    }
    
    public String getTipo() {
        return tipo;
    }
    
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    
    public ArrayList<Nodo> getHijos() {
        return hijos;
    }
    
    /**
     * Agrega un hijo al nodo
     * @param hijo Nodo hijo a agregar
     */
    public void addHijo(Nodo hijo) {
        if (hijo != null) {
            this.hijos.add(hijo);
        }
    }
    
    /**
     * Imprime el árbol de forma jerárquica
     * Muestra primero el padre, luego todos sus hijos recursivamente
     */
    public void arbol() {
        imprimirArbol(0);
    }
    
    /**
     * Método recursivo para imprimir el árbol con indentación
     * @param nivel Nivel de profundidad en el árbol
     */
    private void imprimirArbol(int nivel) {
        // Imprimir indentación
        String indent = "";
        for (int i = 0; i < nivel; i++) {
            indent += "  ";
        }
        
        // Imprimir el nodo actual
        if (tipo != null && !tipo.isEmpty()) {
            System.out.println(indent + "Nodo: " + lexema + " (tipo: " + tipo + ")");
        } else {
            System.out.println(indent + "Nodo: " + lexema);
        }
        
        // Imprimir hijos si existen
        if (!hijos.isEmpty()) {
            System.out.println(indent + "Hijos:");
            for (Nodo hijo : hijos) {
                hijo.imprimirArbol(nivel + 1);
            }
        }
    }
    
    public void imprimirCompacto() {
        imprimirCompactoHelper("", true);
    }
    
    private void imprimirCompactoHelper(String prefix, boolean esUltimo) {
        // Imprimir el nodo actual
        System.out.print(prefix);
        System.out.print(esUltimo ? "└── " : "├── ");
        
        if (tipo != null && !tipo.isEmpty()) {
            System.out.println(lexema + " (" + tipo + ")");
        } else {
            System.out.println(lexema);
        }
        
        // Imprimir hijos
        for (int i = 0; i < hijos.size(); i++) {
            boolean ultimo = (i == hijos.size() - 1);
            String nuevoPrefix = prefix + (esUltimo ? "    " : "│   ");
            hijos.get(i).imprimirCompactoHelper(nuevoPrefix, ultimo);
        }
    }
    
    /**
     * Genera una representación en String del árbol
     * Útil para guardar en archivos
     */
    @Override
    public String toString() {
        return toStringHelper("");
    }
    
    private String toStringHelper(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append(lexema);
        
        if (tipo != null && !tipo.isEmpty()) {
            sb.append(" (").append(tipo).append(")");
        }
        sb.append("\n");
        
        for (Nodo hijo : hijos) {
            sb.append(hijo.toStringHelper(indent + "  "));
        }
        
        return sb.toString();
    }
    
    /**
     * Cuenta el número total de nodos en el árbol
     * @return Cantidad de nodos
     */
    public int contarNodos() {
        int total = 1; // Este nodo
        for (Nodo hijo : hijos) {
            total += hijo.contarNodos();
        }
        return total;
    }
    
    /**
     * Obtiene la altura del árbol
     * @return Altura del árbol
     */
    public int getAltura() {
        if (hijos.isEmpty()) {
            return 1;
        }
        
        int maxAltura = 0;
        for (Nodo hijo : hijos) {
            maxAltura = Math.max(maxAltura, hijo.getAltura());
        }
        
        return 1 + maxAltura;
    }
}