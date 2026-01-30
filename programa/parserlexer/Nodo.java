package parserlexer;

import java.util.ArrayList;

public class Nodo {
    private String etiqueta;
    private String lexema;
    private String tipo;
    private int linea;
    private int columna;
    private ArrayList<Nodo> hijos;
    private String tipoSemantico;  // NUEVO P3
    
    public Nodo(String etiqueta) {
        this.etiqueta = etiqueta;
        this.lexema = null;
        this.tipo = null;
        this.linea = -1;
        this.columna = -1;
        this.hijos = new ArrayList<>();
        this.tipoSemantico = null;
    }
    
    public Nodo(String lexema, String tipo) {
        this.etiqueta = lexema;
        this.lexema = lexema;
        this.tipo = tipo;
        this.linea = -1;
        this.columna = -1;
        this.hijos = new ArrayList<>();
        this.tipoSemantico = null;
    }
    
    public Nodo(String lexema, String tipo, int linea, int columna) {
        this.etiqueta = lexema;
        this.lexema = lexema;
        this.tipo = tipo;
        this.linea = linea;
        this.columna = columna;
        this.hijos = new ArrayList<>();
        this.tipoSemantico = null;
    }
    
    public void addHijo(Nodo hijo) {
        if (hijo != null) {
            this.hijos.add(hijo);
        }
    }
    
    public void addHijos(ArrayList<Nodo> nuevosHijos) {
        if (nuevosHijos != null) {
            this.hijos.addAll(nuevosHijos);
        }
    }
    
    public String getEtiqueta() { return etiqueta; }
    public String getLexema() { return lexema; }
    public String getTipo() { return tipo; }
    public int getLinea() { return linea; }
    public int getColumna() { return columna; }
    public ArrayList<Nodo> getHijos() { return hijos; }
    
    public String getTipoSemantico() { return tipoSemantico; }  // NUEVO P3
    public void setTipoSemantico(String t) { this.tipoSemantico = t; }  // NUEVO P3
    
    public void setLinea(int linea) { this.linea = linea; }
    public void setColumna(int columna) { this.columna = columna; }
    
    public void arbol() {
        imprimirArbol("", true);
    }
    
    private void imprimirArbol(String prefijo, boolean esUltimo) {
        System.out.print(prefijo);
        System.out.print(esUltimo ? "└── " : "├── ");
        
        if (tipo != null && !tipo.isEmpty()) {
            System.out.print(etiqueta + " [" + tipo + "]");
        } else {
            System.out.print(etiqueta);
        }
        
        if (tipoSemantico != null && !tipoSemantico.isEmpty()) {
            System.out.print(" {" + tipoSemantico + "}");
        }
        
        System.out.println();
        
        for (int i = 0; i < hijos.size(); i++) {
            boolean esUltimoHijo = (i == hijos.size() - 1);
            String nuevoPrefijo = prefijo + (esUltimo ? "    " : "│   ");
            hijos.get(i).imprimirArbol(nuevoPrefijo, esUltimoHijo);
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        toStringRecursivo(sb, "", true);
        return sb.toString();
    }
    
    private void toStringRecursivo(StringBuilder sb, String prefijo, boolean esUltimo) {
        sb.append(prefijo);
        sb.append(esUltimo ? "└── " : "├── ");
        
        if (tipo != null && !tipo.isEmpty()) {
            sb.append(etiqueta).append(" [").append(tipo).append("]");
        } else {
            sb.append(etiqueta);
        }
        
        if (tipoSemantico != null && !tipoSemantico.isEmpty()) {
            sb.append(" {").append(tipoSemantico).append("}");
        }
        
        sb.append("\n");
        
        for (int i = 0; i < hijos.size(); i++) {
            boolean esUltimoHijo = (i == hijos.size() - 1);
            String nuevoPrefijo = prefijo + (esUltimo ? "    " : "│   ");
            hijos.get(i).toStringRecursivo(sb, nuevoPrefijo, esUltimoHijo);
        }
    }
    
    public int contarNodos() {
        int total = 1;
        for (Nodo hijo : hijos) {
            total += hijo.contarNodos();
        }
        return total;
    }
    
    public int getAltura() {
        if (hijos.isEmpty()) return 1;
        int maxAlturaHijo = 0;
        for (Nodo hijo : hijos) {
            int alturaHijo = hijo.getAltura();
            if (alturaHijo > maxAlturaHijo) {
                maxAlturaHijo = alturaHijo;
            }
        }
        return 1 + maxAlturaHijo;
    }
    
    public boolean esHoja() { return hijos.isEmpty(); }
    public boolean esTerminal() { return tipo != null && !tipo.isEmpty(); }
    public int getNumHijos() { return hijos.size(); }
    
    public Nodo getHijo(int indice) {
        if (indice >= 0 && indice < hijos.size()) {
            return hijos.get(indice);
        }
        return null;
    }
}