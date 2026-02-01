import java.io.*;
import java.util.*;
import java_cup.runtime.Symbol;
import parserlexer.*;

/**
 * Clase Principal (Main)
 * Objetivo: Controlar todo el flujo del compilador: generación, análisis léxico, 
 * sintáctico, semántico, código intermedio y ensamblador MIPS.
 */
public class Main {
    private static final Scanner sc = new Scanner(System.in);
    
    private static final String RUTA_PROGRAMA = "programa/";
    private static final String RUTA_LIB = RUTA_PROGRAMA + "lib/";
    private static final String RUTA_PARSERLEXER = RUTA_PROGRAMA + "parserlexer/";
    private static final String RUTA_PRUEBAS = RUTA_PROGRAMA + "archivos_prueba/";
    private static final String RUTA_SALIDA = RUTA_PROGRAMA + "archivos_salida/";

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n----- Menu de compilación -----");
            System.out.println("1. Generar Lexer.java, Parser.java y sym.java");
            System.out.println("2. Realizar Análisis Léxico");
            System.out.println("3. Realizar Análisis Sintáctico");
            System.out.println("4. Realizar Análisis Semántico");
            System.out.println("5. Realizar Análisis Total (Léxico+Sintáctico+Semántico)");
            System.out.println("6. Generar Código Intermedio (TAC)"); 
            System.out.println("7. Generar Código Ensamblador MIPS (.s)");
            System.out.println("0. Salir");
            
            System.out.print("Seleccione una opción: ");
            String opcion = sc.nextLine();

            switch (opcion) {
                case "1": generarArchivos(); break;
                case "2": menuArchivos("LEXICO"); break;
                case "3": menuArchivos("SINTACTICO"); break;
                case "4": menuArchivos("SEMANTICO"); break;
                case "5": menuArchivos("TOTAL"); break;
                case "6": menuArchivos("INTERMEDIO"); break;
                case "7": menuArchivos("MIPS"); break;
                case "0":
                    System.out.println("\n¡Hasta luego!");
                    return;
                default:
                    System.out.println("Opción no válida.");
            }
        }
    }

    /**
     * Menú genérico para selección de archivos para evitar código duplicado.
     */
    private static void menuArchivos(String tipoAnalisis) {
        File folder = new File(RUTA_PRUEBAS);
        File[] listaArchivos = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        
        if (listaArchivos == null || listaArchivos.length == 0) {
            System.err.println("Error: No hay archivos .txt en " + RUTA_PRUEBAS);
            return;
        }
        
        System.out.println("\n--- Lista de archivos para " + tipoAnalisis + " ---");
        System.out.println("0. ANALIZAR TODOS");
        for (int i = 0; i < listaArchivos.length; i++) {
            System.out.println((i + 1) + ". " + listaArchivos[i].getName());
        }
        
        System.out.print("Escoja una opción: ");
        try {
            int eleccion = Integer.parseInt(sc.nextLine());
            if (eleccion == 0) {
                for (File f : listaArchivos) ejecutarFlujo(f, tipoAnalisis);
            } else if (eleccion > 0 && eleccion <= listaArchivos.length) {
                ejecutarFlujo(listaArchivos[eleccion - 1], tipoAnalisis);
            } else {
                System.out.println("Opción fuera de rango");
            }
        } catch (Exception e) {
            System.out.println("Entrada inválida");
        }
    }

    /**
     * Centraliza la ejecución de los análisis según la opción seleccionada.
     */
    private static void ejecutarFlujo(File archivo, String tipo) {
        switch (tipo) {
            case "LEXICO": ejecutarLexer(archivo); break;
            case "SINTACTICO": ejecutarParser(archivo); break;
            case "SEMANTICO": ejecutarAnalisisSemantico(archivo, false); break;
            case "TOTAL": {
                boolean ok = validarCodigoCompleto(archivo);
                System.out.println(ok ? "ARCHIVO VALIDO" : "CONTIENE ERRORES");
                break;
            }
            case "INTERMEDIO": ejecutarGeneracion(archivo, "TAC"); break;
            case "MIPS": ejecutarGeneracion(archivo, "MIPS"); break;
        }
    }

    private static void ejecutarGeneracion(File archivoFuente, String modo) {
        try {
            System.out.println("\nAnalizando para generación: " + archivoFuente.getName());
            Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(archivoFuente), "UTF-8"));
            
            Lexer lexer = new Lexer(reader);
            Parser parser = new Parser(lexer);
            parser.parse();
            
            if (!Parser.getErrores().isEmpty()) {
                System.err.println("Errores sintácticos detectados. Abortando.");
                return;
            }

            Nodo raiz = (Nodo) Parser.arbolSintactico;
            RecorredorAST recorredor = new RecorredorAST();
            recorredor.recorrerYAnalizar(raiz);

            if (recorredor.tieneErrores()) {
                System.err.println("Errores semánticos detectados. El código viola el tipado fuerte.");
                return;
            }

            GeneradorCodigoIntermedio tacGen = recorredor.generarCodigoIntermedio(raiz);
            String nombreBase = archivoFuente.getName().replace(".txt", "");

            if (modo.equals("TAC")) {
                String salida = tacGen.getCodigoCompleto();
                System.out.println(salida);
                guardarArchivo(RUTA_SALIDA + "tac_" + nombreBase + ".txt", salida);
            } else {
                GeneradorMIPS mipsGen = new GeneradorMIPS(tacGen.getCodigo(), recorredor.getTablaSimbolos());
                String assembly = mipsGen.generar();
                System.out.println("\n--- CÓDIGO MIPS GENERADO ---\n" + assembly);
                guardarArchivo(RUTA_SALIDA + "mips_" + nombreBase + ".s", assembly);
            }
        } catch (Exception e) {
            System.err.println("Error en generación: " + e.getMessage());
        }
    }

    private static void guardarArchivo(String ruta, String contenido) throws IOException {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(ruta), "UTF-8"))) {
            writer.print(contenido);
            System.out.println("✓ Guardado en: " + ruta);
        }
    }

    // --- MÉTODOS DE SOPORTE EXISTENTES (LIMPIADOS) ---

    private static void ejecutarLexer(File archivoFuente) {
        String rutaReporte = RUTA_SALIDA + "lexer_" + archivoFuente.getName();
        try {
            Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(archivoFuente), "UTF-8"));
            Lexer lexer = new Lexer(reader);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(rutaReporte), "UTF-8"));
            
            writer.println("REPORTE LEXICO: " + archivoFuente.getName() + "\n");
            while (true) {
                Symbol s = lexer.next_token();
                if (s.sym == sym.EOF) break;
                writer.printf("Token: %-15s | Lexema: %-15s | Linea: %d%n", obtenerNombreToken(s.sym), s.value, s.left);
            }
            writer.close();
            System.out.println("✓ Reporte léxico generado: " + rutaReporte);
        } catch (Exception e) { System.err.println("Error: " + e.getMessage()); }
    }

    private static void ejecutarParser(File archivoFuente) {
        String nombreSinExt = archivoFuente.getName().replace(".txt", "");
        String rutaReporteArbol = RUTA_SALIDA + "arbol_tabla_" + nombreSinExt + ".txt";
        
        try {
            System.out.println("\n========== ANÁLISIS SINTÁCTICO ==========");
            System.out.println("Analizando: " + archivoFuente.getName() + "...");
            
            Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(archivoFuente), "UTF-8"));
            
            // USAR DefaultSymbolFactory para evitar el ClassCastException
            java_cup.runtime.SymbolFactory sf = new java_cup.runtime.DefaultSymbolFactory();
            Lexer lexer = new Lexer(reader);
            Parser parser = new Parser(lexer, sf); // Constructor NO DEPRECATED
            
            Parser.limpiarErrores();
            parser.parse();
            
            ArrayList<String> erroresSintacticos = Parser.getErrores();
            Nodo arbol = (Nodo) Parser.arbolSintactico;
            
            // ... resto del código (impresión de árbol y tabla) ...
            if (erroresSintacticos.isEmpty()) {
                System.out.println("✓ Archivo sintácticamente correcto.");
                if (arbol != null) arbol.arbol(); 
            } else {
                System.err.println("✗ Se encontraron " + erroresSintacticos.size() + " error(es) sintáctico(s):");
                erroresSintacticos.forEach(System.err::println);
            }

            if (arbol != null) {
                RecorredorAST recorredor = new RecorredorAST();
                recorredor.recorrer(arbol); 
                TablaSimbolos tabla = recorredor.getTablaSimbolos();
                System.out.println(tabla.generarReporte());
                
                // Generación de archivo (try-with-resources)
                try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(rutaReporteArbol), "UTF-8"))) {
                    writer.println("REPORTE SINTÁCTICO Y TABLA DE SÍMBOLOS: " + archivoFuente.getName());
                    writer.println("======================================================================");
                    writer.println("ÁRBOL SINTÁCTICO:");
                    writer.println(arbol.toString());
                    writer.println("\n======================================================================");
                    writer.println(tabla.generarReporte());
                    writer.println("======================================================================");
                }
                System.out.println("✓ Reporte completo generado en: " + rutaReporteArbol);
            }
            
        } catch (Exception e) {
            System.err.println("Error en análisis sintáctico: " + e.getMessage());
        }
    }

    private static void ejecutarAnalisisSemantico(File archivoFuente, boolean silencioso) {
        try {
            Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(archivoFuente), "UTF-8"));
            Lexer lexer = new Lexer(reader);
            Parser parser = new Parser(lexer);
            Parser.limpiarErrores();
            parser.parse();
            
            if (!Parser.getErrores().isEmpty()) return;

            RecorredorAST recorredor = new RecorredorAST();
            recorredor.recorrerYAnalizar((Nodo)Parser.arbolSintactico);
            
            if (!recorredor.tieneErrores() && !silencioso) {
                System.out.println("Analisis semántico exitoso.");
            }
        } catch (Exception e) { System.err.println("Error: " + e.getMessage()); }
    }

    private static boolean validarCodigoCompleto(File archivoFuente) {
        try {
            Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(archivoFuente), "UTF-8"));
            Lexer lexer = new Lexer(reader);
            Parser parser = new Parser(lexer);
            Parser.limpiarErrores();
            parser.parse();
            if (!Parser.getErrores().isEmpty()) return false;

            RecorredorAST recorredor = new RecorredorAST();
            recorredor.recorrerYAnalizar((Nodo)Parser.arbolSintactico);
            return !recorredor.tieneErrores();
        } catch (Exception e) { return false; }
    }

    private static void generarArchivos() {
        try {
            System.out.println("Borrando archivos antiguos...");
            String[] archivos = {"Lexer.java", "Parser.java", "sym.java", "Lexer.java~"};
            for (String nombre : archivos) {
                File f = new File(RUTA_PARSERLEXER + nombre);
                if (f.exists()) {
                    if (f.delete()) {
                        System.out.println("  - Eliminado: " + nombre);
                    }
                }
            }

            System.out.println("Generando archivos con CUP...");
            Process p1 = new ProcessBuilder(
                "java", "-jar", RUTA_LIB + "java-cup-11b.jar", 
                "-destdir", RUTA_PARSERLEXER, 
                "-parser", "Parser", 
                RUTA_PARSERLEXER + "Parser.cup"
            ).inheritIO().start();
            p1.waitFor();

            System.out.println("Generando archivos con JFlex...");
            Process p2 = new ProcessBuilder(
                "java", "-jar", RUTA_LIB + "jflex-full-1.9.1.jar", 
                "--encoding", "UTF-8",
                "--nobak", // <--- ESTO ELIMINA EL ARCHIVO CON ~
                "-d", RUTA_PARSERLEXER,
                RUTA_PARSERLEXER + "Lexer.jflex"
            ).inheritIO().start();
            p2.waitFor();
            
            System.out.println("✓ Archivos generados correctamente.");
        } catch (Exception e) { 
            System.err.println("Error: " + e.getMessage()); 
        }
    }

    private static String obtenerNombreToken(int id) {
        try {
            java.lang.reflect.Field[] fields = sym.class.getFields();
            for (java.lang.reflect.Field field : fields) {
                if (field.getInt(null) == id) return field.getName();
            }
        } catch (Exception e) { }
        return "UNKNOWN";
    }
}