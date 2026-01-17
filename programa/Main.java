import java.io.*;
import java.util.*;
import java_cup.runtime.Symbol;

/**
 * Clase Principal (Main)
 * Objetivo: Controlar todo el flujo del programa, desde la generación de los archivos del compilador
 * hasta la ejecución del análisis léxico y la creación de reportes.
 * Restricciones: Requiere que las carpetas lib/, parserlexer/ y archivos_prueba/ existan y tengan los permisos adecuados.
 */
public class Main {
    // Escáner para leer lo que se ocupa por consola
    private static final Scanner sc = new Scanner(System.in);
    
    // Rutas de carpetas para que el programa sepa donde buscar
    private static final String RUTA_PROGRAMA = "programa/";
    private static final String RUTA_LIB = RUTA_PROGRAMA + "lib/";
    private static final String RUTA_PARSERLEXER = RUTA_PROGRAMA + "parserlexer/";
    private static final String RUTA_PRUEBAS = RUTA_PROGRAMA + "archivos_prueba/";
    private static final String RUTA_SALIDA = RUTA_PROGRAMA + "archivos_salida/";

    /**
     * Método main
     * Objetivo: Mostrar el menú interactivo y dirigir al usuario a la opción que quiera ejecutar.
     * Entrada: Argumentos de consola (no se usan en este caso).
     * Salida: Interacción por consola.
     */
    public static void main(String[] args) {
        while (true) {
            System.out.println("\n----- Menu de análisis -----");
            System.out.println("1. Generar Lexer.java, Parser.java y sym.java");
            System.out.println("2. Realizar Análisis Léxico");
            System.out.println("3. Realizar Análisis Sintáctico");
            System.out.println("0. Salir");
            
            String opcion = sc.nextLine();

            switch (opcion) {
                case "1":
                    generarArchivos();
                    break;
                case "2":
                    menuAnalisisLexico();
                    break;
                case "3":
                    menuAnalisisSintactico();
                    break;
                case "0":
                    System.out.println("Saliendo...");
                    return;
                default:
                    System.out.println("Opción no válida.");
            }
        }
    }

    private static void menuAnalisisSintactico() {
        File folder = new File(RUTA_PRUEBAS);
        File[] listaArchivos = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        
        if (listaArchivos == null || listaArchivos.length == 0) {
            System.err.println("Error: No hay archivos .txt en " + RUTA_PRUEBAS);
            return;
        }
        
        System.out.println("\n--- Lista de archivos disponibles ---");
        System.out.println("0. ANALIZAR TODOS");
        for (int i = 0; i < listaArchivos.length; i++) {
            System.out.println((i + 1) + ". " + listaArchivos[i].getName());
        }
        
        System.out.print("Escoja una opción: ");
        int eleccion;
        try {
            eleccion = Integer.parseInt(sc.nextLine());
        } catch (Exception e) {
            System.out.println("Entrada inválida");
            return;
        }
    
        if (eleccion == 0) {
            for (File f : listaArchivos) {
                ejecutarParser(f);
            }
        } else if (eleccion > 0 && eleccion <= listaArchivos.length) {
            ejecutarParser(listaArchivos[eleccion - 1]);
        } else {
            System.out.println("Opción fuera de rango");
        }
    }

    /**
     * Método generarArchivos
     * Objetivo: Limpiar los archivos viejos y regenerar el lexer y el parser usando los .jar de JFlex y CUP.
     * Entrada: Archivos Lexer.jflex y Parser.cup existentes en la carpeta parserlexer.
     * Salida: Archivos generados Lexer.java, Parser.java y sym.java.
     * Restricciones: Los .jar deben estar en la ruta correcta y se debe tener java instalado en el path.
     */
    private static void generarArchivos() {
        System.out.println("Borrando archivos antiguos...");
        String[] archivosABorrar = {"Lexer.java", "Parser.java", "sym.java"};
        for (String s : archivosABorrar) {
            File f = new File(RUTA_PARSERLEXER + s);
            if (f.exists()) f.delete();
        }
        try {
            // Sacar el sym y el parser con CUP
            System.out.println("Generando sym y Parser con CUP...");
            ProcessBuilder pb1 = new ProcessBuilder(
            "java", "-jar",
            RUTA_LIB + "java-cup-11b.jar",
            "-destdir", RUTA_PARSERLEXER,
            "-parser", "Parser",
            RUTA_PARSERLEXER + "Parser.cup"
            );
            pb1.redirectErrorStream(true);
            pb1.inheritIO();
            Process p1 = pb1.start();
            p1.waitFor();

            
            // Generar el lexer.java forzando UTF-8 para evitar problemas con simbolos raros
            System.out.println("Generando Lexer con JFlex...");
            ProcessBuilder pb2 = new ProcessBuilder(
                "java",
                "-Dfile.encoding=UTF-8",
                "-jar",
                RUTA_LIB + "jflex-full-1.9.1.jar",
                "--encoding", "UTF-8",
                RUTA_PARSERLEXER + "Lexer.jflex"
            );
            pb2.redirectErrorStream(true);
            pb2.inheritIO();
            Process p2 = pb2.start();
            p2.waitFor();

            
            System.out.println("Archivos generados");
        } catch (Exception e) {
            System.err.println("Error ejecutando JFlex/CUP: " + e.getMessage());
        }
    }

    /**
     * Método menuAnalisisLexico
     * Objetivo: Buscar los txt disponibles y dejar escoger al usuario cuál analizar.
     * Entrada: Selección del usuario por consola.
     * Salida: Llamada al método ejecutarLexer con el archivo seleccionado.
     * Restricciones: Debe haber archivos .txt en la carpeta archivos_prueba/.
     */
    private static void menuAnalisisLexico() {
        File folder = new File(RUTA_PRUEBAS);
        File[] listaArchivos = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        
        // Si no hay archivos en la carpeta tirar error y regresar
        if (listaArchivos == null || listaArchivos.length == 0) {
            System.err.println("Error: No hay archivos .txt en " + RUTA_PRUEBAS);
            return;
        }
        
        System.out.println("\n--- Lista de archivos disponibles ---");
        System.out.println("0. ANALIZAR TODOS");
        for (int i = 0; i < listaArchivos.length; i++) {
            System.out.println((i + 1) + ". " + listaArchivos[i].getName());
        }
        
        System.out.print("Escoja una opción: ");
        int eleccion;
        try {
            eleccion = Integer.parseInt(sc.nextLine());
        } catch (Exception e) {
            System.out.println("Entrada inválida");
            return;
        }

        if (eleccion == 0) {
            for (File f : listaArchivos) {
                ejecutarLexer(f);
            }
        } else if (eleccion > 0 && eleccion <= listaArchivos.length) {
            ejecutarLexer(listaArchivos[eleccion - 1]);
        } else {
            System.out.println("Opción fuera de rango");
        }
    }

    /**
     * Método ejecutarLexer
     * Objetivo: Analizar el archivo txt token por token usando reflexión y crear el reporte final.
     * Entrada: Objeto File que representa el archivo fuente a analizar.
     * Salida: Archivo de texto en archivos_salida/ con la tabla de tokens y lista de errores.
     * Restricciones: El archivo Lexer.java debe haber sido generado previamente (opción 1).
     */
    private static void ejecutarLexer(File archivoFuente) {
        String nombreSinExt = archivoFuente.getName().replace(".txt", "");
        String rutaReporte = RUTA_SALIDA + "reporte_" + nombreSinExt + ".txt";
        
        // Lista para ir guardando los errores para ponerlos al final del txt
        List<String> listaErrores = new ArrayList<>();

        try {
            System.out.println("Analizando: " + archivoFuente.getName() + "...");
            
            // Leer el archivo con UTF-8 para que no se caiga con simbolos raros
            Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(archivoFuente), "UTF-8"));
            
            // Cargar las clases dinamicamente para que el programa compile bien aunque no exista Lexer.java aun
            Class<?> lexerClass = Class.forName("parserlexer.Lexer");
            Object scanner = lexerClass.getConstructor(Reader.class).newInstance(reader);
            
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(rutaReporte), "UTF-8"));

            writer.println("REPORTE DE TOKENS: " + archivoFuente.getName());
            writer.println("======================================================================");
            writer.printf("%-25s | %-25s | %-10s | %-10s%n", "IDENTIFICADOR (TIPO)", "LEXEMA", "LINEA", "COLUMNA");
            writer.println("----------------------------------------------------------------------");

            // Sacar valores de sym.java por reflexion
            Class<?> symClass = Class.forName("parserlexer.sym");
            int EOF_VAL = symClass.getField("EOF").getInt(null);
            int ERROR_VAL = symClass.getField("ERROR").getInt(null);

            // Sacar tokens uno por uno hasta llegar al final del archivo
            while (true) {
                Symbol s = (Symbol) lexerClass.getMethod("next_token").invoke(scanner);
                
                if (s.sym == EOF_VAL) break;

                // Sacar el lexema del scanner o del symbol directamente
                String lexema = (s.value != null) ? s.value.toString() : (String) lexerClass.getMethod("yytext").invoke(scanner);
                String nombreToken = obtenerNombreToken(s.sym);
                String idConTipo = s.sym + " (" + nombreToken + ")";

                // Si es un error lo mete a la lista para el final
                if (s.sym == ERROR_VAL) {
                    listaErrores.add("Caracter ilegal <" + lexema + "> en línea " + s.left);
                }

                // Escribir la linea en el reporte
                writer.printf("%-25s | %-25s | %-10d | %-10d%n", 
                               idConTipo, lexema, s.left, s.right);
            }

            // Escribe el resumen de errores antes de cerrar
            writer.println("----------------------------------------------------------------------");
            if (!listaErrores.isEmpty()) {
                writer.println("ERRORES ENCONTRADOS:");
                for (String err : listaErrores) {
                    writer.println("- " + err);
                }
            } else {
                writer.println("No se encontraron errores léxicos.");
            }
            
            writer.println("----------------------------------------------------------------------");
            writer.println("FIN DEL ANALISIS");
            writer.close();
            System.out.println("Reporte generado: " + rutaReporte);

        } catch (ClassNotFoundException e) {
            System.err.println("Error: No están los archivos del Lexer. Use la opcion 1 primero.");
        } catch (Exception e) {
            System.err.println("Error analizando " + archivoFuente.getName() + ": " + e.getMessage());
        }
    }

    private static void ejecutarParser(File archivoFuente) {
        String nombreSinExt = archivoFuente.getName().replace(".txt", "");
        String rutaReporteArbol = RUTA_SALIDA + "arbol_" + nombreSinExt + ".txt";
        
        try {
            System.out.println("\n========== ANÁLISIS SINTÁCTICO ==========");
            System.out.println("Analizando: " + archivoFuente.getName() + "...");
            
            // Leer el archivo con UTF-8
            Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(archivoFuente), "UTF-8"));
            
            // Cargar las clases dinámicamente
            Class<?> lexerClass = Class.forName("parserlexer.Lexer");
            Object scanner = lexerClass.getConstructor(Reader.class).newInstance(reader);
            
            Class<?> parserClass = Class.forName("parserlexer.Parser");
            Object parser = parserClass.getConstructor(java_cup.runtime.Scanner.class).newInstance(scanner);
            
            // Ejecutar el parser
            parserClass.getMethod("parse").invoke(parser);
            
            // Obtener el árbol sintáctico
            java.lang.reflect.Field campoArbol = parserClass.getField("arbolSintactico");
            Object arbol = campoArbol.get(null);
            
            if (arbol != null) {
                System.out.println("\n========== ÁRBOL SINTÁCTICO ==========");
                
                // Imprimir en consola
                arbol.getClass().getMethod("arbol").invoke(arbol);
                
                // Guardar en archivo
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(rutaReporteArbol), "UTF-8"));
                writer.println("ÁRBOL SINTÁCTICO: " + archivoFuente.getName());
                writer.println("======================================================================");
                writer.println(arbol.toString());
                writer.println("======================================================================");
                
                // Estadísticas del árbol
                int numNodos = (int) arbol.getClass().getMethod("contarNodos").invoke(arbol);
                int altura = (int) arbol.getClass().getMethod("getAltura").invoke(arbol);
                
                writer.println("\nESTADÍSTICAS:");
                writer.println("- Número total de nodos: " + numNodos);
                writer.println("- Altura del árbol: " + altura);
                
                writer.close();
                
                System.out.println("\n✓ Árbol sintáctico generado: " + rutaReporteArbol);
            } else {
                System.err.println("✗ No se pudo construir el árbol sintáctico");
            }
            
        } catch (Exception e) {
            System.err.println("Error en análisis sintáctico: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Método obtenerNombreToken
     * Objetivo: Buscar el nombre legible del token (string) a partir de su ID numérico en sym.java.
     * Entrada: Entero que representa el ID del token.
     * Salida: String con el nombre del token (ej: "INT_LITERAL") o "UNKNOWN" si falla.
     * Restricciones: sym.java debe haber sido generado y compilado.
     */
    private static String obtenerNombreToken(int id) {
        try {
            Class<?> symClass = Class.forName("parserlexer.sym");
            java.lang.reflect.Field[] fields = symClass.getFields();
            for (java.lang.reflect.Field field : fields) {
                if (field.getInt(null) == id) return field.getName();
            }
        } catch (Exception e) { }
        return "UNKNOWN";
    }
}