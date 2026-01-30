import java.util.ArrayList;

public class Simbolo {
    private String nombre;
    private String tipo;
    private int linea;
    private String alcance;
    private String categoria;
    private String dimensiones;
    private String valorInicial;
    private ArrayList<String> tiposParametros;  // NUEVO P3 - para funciones
    
    public Simbolo(String nombre, String tipo, int linea, String alcance, String categoria) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.linea = linea;
        this.alcance = alcance;
        this.categoria = categoria;
        this.dimensiones = null;
        this.valorInicial = null;
        this.tiposParametros = new ArrayList<>();
    }
    
    public Simbolo(String nombre, String tipo, int linea, String alcance, 
                   String categoria, String dimensiones) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.linea = linea;
        this.alcance = alcance;
        this.categoria = categoria;
        this.dimensiones = dimensiones;
        this.valorInicial = null;
        this.tiposParametros = new ArrayList<>();
    }
    
    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }
    public int getLinea() { return linea; }
    public String getAlcance() { return alcance; }
    public String getCategoria() { return categoria; }
    public String getDimensiones() { return dimensiones; }
    public String getValorInicial() { return valorInicial; }
    public ArrayList<String> getTiposParametros() { return tiposParametros; }  // NUEVO P3
    
    public void setDimensiones(String dimensiones) { this.dimensiones = dimensiones; }
    public void setValorInicial(String valorInicial) { this.valorInicial = valorInicial; }
    public void agregarTipoParametro(String tipo) { this.tiposParametros.add(tipo); }  // NUEVO P3
    
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