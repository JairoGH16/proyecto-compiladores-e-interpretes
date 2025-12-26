import java.io.*;
import java.util.*;
import java_cup.runtime.Symbol;

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    
    // Rutas de carpetas para que el programa sepa donde buscar
    private static final String RUTA_PROGRAMA = "programa/";
    private static final String RUTA_LIB = RUTA_PROGRAMA + "lib/";
    private static final String RUTA_PARSERLEXER = RUTA_PROGRAMA + "parserlexer/";
    private static final String RUTA_PRUEBAS = RUTA_PROGRAMA + "archivos_prueba/";
    private static final String RUTA_SALIDA = RUTA_PROGRAMA + "archivos_salida/";

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n----- Menu de análisis léxico -----");
            System.out.println("1. Generar Lexer.java, Parser.java y sym.java");
            System.out.println("2. Realizar Análisis Léxico");
            System.out.println("0. Salir");
            System.out.print("Seleccione una opción: ");
            
            String opcion = sc.nextLine();

            switch (opcion) {
                case "1":
                    generarArchivos();
                    break;
                case "2":
                    menuAnalisisLexico();
                    break;
                case "0":
                    System.out.println("Saliendo...");
                    return;
                default:
                    System.out.println("Opción no válida.");
            }
        }
    }

    // Limpiar los archivos viejos y genera el lexer y sym nuevos con los jar
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
            Process p1 = Runtime.getRuntime().exec("java -jar " + RUTA_LIB + "java-cup-11b.jar -destdir " + RUTA_PARSERLEXER + " -parser Parser " + RUTA_PARSERLEXER + "Parser.cup");
            p1.waitFor();
            
            // Generar el lexer.java forzando UTF-8 para evitar problemas con simbolos raros
            System.out.println("Generando Lexer con JFlex...");
            Process p2 = Runtime.getRuntime().exec("java -Dfile.encoding=UTF-8 -jar " + RUTA_LIB + "jflex-full-1.9.1.jar --encoding UTF-8 " + RUTA_PARSERLEXER + "Lexer.jflex");
            p2.waitFor();
            
            System.out.println("Archivos generados");
        } catch (Exception e) {
            System.err.println("Error ejecutando JFlex/CUP: " + e.getMessage());
        }
    }

    // Buscar los txt disponibles y deja escoger cual analizar
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

    // Analizar el archivo txt y crear el reporte
    private static void ejecutarLexer(File archivoFuente) {
        String nombreSinExt = archivoFuente.getName().replace(".txt", "");
        String rutaReporte = RUTA_SALIDA + "reporte_" + nombreSinExt + ".txt";

        try {
            System.out.println("Analizando: " + archivoFuente.getName() + "...");
            
            // Leer el archivo con UTF-8 para que no se caiga con simbolos raros
            Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(archivoFuente), "UTF-8"));
            
            // Cargar las clases dinamicamente para que el programa compile bien
            Class<?> lexerClass = Class.forName("parserlexer.Lexer");
            Object scanner = lexerClass.getConstructor(Reader.class).newInstance(reader);
            
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(rutaReporte), "UTF-8"));

            writer.println("REPORTE DE TOKENS: " + archivoFuente.getName());
            writer.println("======================================================================");
            writer.printf("%-25s | %-25s | %-10s | %-10s%n", "IDENTIFICADOR (TIPO)", "LEXEMA", "LINEA", "COLUMNA");
            writer.println("----------------------------------------------------------------------");

            // Sacar el valor de EOF de sym.java
            Class<?> symClass = Class.forName("parserlexer.sym");
            int EOF_VAL = symClass.getField("EOF").getInt(null);

            // Sacar tokens uno por uno hasta llegar al final del archivo
            while (true) {
                Symbol s = (Symbol) lexerClass.getMethod("next_token").invoke(scanner);
                
                if (s.sym == EOF_VAL) break;

                // Sacar el lexema del scanner o del symbol directamente
                String lexema = (s.value != null) ? s.value.toString() : (String) lexerClass.getMethod("yytext").invoke(scanner);
                String nombreToken = obtenerNombreToken(s.sym);
                String idConTipo = s.sym + " (" + nombreToken + ")";

                // Escribir la linea en el reporte
                writer.printf("%-25s | %-25s | %-10d | %-10d%n", 
                               idConTipo, lexema, s.left, s.right);
            }

            writer.println("----------------------------------------------------------------------");
            writer.println("FIN DEL ANALISIS");
            writer.close();
            System.out.println("Reporte generado: " + rutaReporte);

        } catch (ClassNotFoundException e) {
            System.err.println("Error: No estan los archivos del Lexer. Use la opcion 1 primero.");
        } catch (Exception e) {
            System.err.println("Error analizando " + archivoFuente.getName() + ": " + e.getMessage());
        }
    }

    // Buscar el nombre del token en sym.java
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