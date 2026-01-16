package actividad;

import java.io.File;
import java.util.*;

public class EquiposApp 
{

	//Aqui es la ruta donde tengo metido mis XML
    // Ajusta esta ruta al directorio donde tienes los XML (carpeta con los equipos)
    final static String RUTA_XML = "src/Equiposs";

    // Aqui son los datos de mi base de datos
    // En Oracle XE, este valor debe ser el SERVICE NAME, por ejemplo: XEPDB1
    private static final String DB_NAME = "XEPDB1";
    // Recomendado: crear un usuario propio para la actividad (no usar SYS/SYSTEM en clase)
    private static final String DB_USER = "EQUIPOS";
    private static final String DB_PASS = "equipos";

    public static void main(String[] args) 
    {
        Scanner sc = new Scanner(System.in); //Leo lo que escribe la persona

        ConexionMySQL cn = new ConexionMySQL(DB_USER, DB_PASS, DB_NAME); //Creo un objeto de la conexion

        try 
        {
            cn.conectar(); //Se conecta a la base de datos al iniciar el programa

            while (true) 
            { //Aqui creo un bucle para que el usuario elija hasta que le de en la opcion "5" que vendria siendo salir
                System.out.println("\n===== MENÚ =====");
                System.out.println("1) Recorrer directorio para crear tablas");
                System.out.println("2) Rellenar datos en tablas (por equipo)");
                System.out.println("3) Mostrar equipo (desde la tabla)");
                System.out.println("4) Eliminar tablas (todas las del directorio)");
                System.out.println("5) Salir");
                System.out.print("Opción: ");

                String opcion = sc.nextLine().trim(); // Lee la opcion que eligio el usuario y elimina los espacios

                switch (opcion) 
                {
                case "1":
                    opcionCrearTablas(cn); // Este crea las tablas para todos los archivos XML
                    break;

                case "2":
                    opcionRellenarEquipo(cn, sc); //Aqui pide un equipo al usuario y lo rellena, esto cumpliria lo que pidio el profe de que pudiera rellenar mas equipos si quisiese
                    break;

                case "3":
                    opcionMostrarEquipo(cn, sc); // Aqui pido el equipo y lo muestra en pantalla :b
                    break;

                case "4":
                    opcionEliminarTablas(cn); // Elimina todas las tablas
                    break;

                case "5":
                    cn.desconectar();
                    System.out.println("¡Hasta luego!"); //Como explique antes, aqui entonces desconectaria la base de datos e imprimiria un mensaje
                    return;

                default:
                    System.out.println("Opción no válida."); // Si el usuario escribe cualquier opcion que no son las mostradas muestra este mensaje
            }
            }

        } catch (Exception e) {
            System.out.println("Ha habido un error bro " + e.getMessage()); // Si hay algun fallo en cualquier circustancia, muestra este error
        }
    }

    private static File[] listarXml() throws Exception 
    { // devuelvo un array con todo lo que hay en la ruta
        File dir = new File(RUTA_XML); //Aqui creo un objeto que representa todos los archivos xml

        if (!dir.exists() || !dir.isDirectory()) { //Aqui compruebo de que realmente la ruta exista y en caso contrario muestre un error :D
            throw new Exception("No existe el directorio XML: " + dir.getAbsolutePath());
        }

        File[] archivos = dir.listFiles();  //Devuelve todos los archivos que estan dentro del directorio
        if (archivos == null) return new File[0]; //Si este devuelve null, manda un array vacio para evitar un error

        return archivos; //Devuelve todos los archivos, asi sean XML o no
    }

   
    private static void opcionCrearTablas(ConexionMySQL cn) 
    { // Este metodo lo que hace es que recorre todos los archivos del directorio y crea una tabla para cada uno
        try {
            File[] archivos = listarXml(); // Obtengo todos los archivos 
            int contador = 0; 

            for (int i = 0; i < archivos.length; i++) { // Cuenta las tablas una a una y verifica si son archivos .xml, en el caso de que sea, este crea la tabla y pasa al siguiente
                File f = archivos[i];

                if (f.isFile() && f.getName().toLowerCase().endsWith(".xml")) 
                { 
                    String tabla = quitarExtension(f.getName()); //La tabla se llamara tal cual como se llama el arhivo pero quitandole la extension .xml
                    cn.crearTabla(tabla);
                    contador++;
                }
            }

            System.out.println("Tablas creadas/verificadas: " + contador); //Imprime el total de las tablas creadas

        } 
        catch (Exception e) 
        {
            System.out.println("Error creando tablas: " + e.getMessage()); // En el caso de cualquier error imprime en pantalla
        }
    }

    private static void opcionRellenarEquipo(ConexionMySQL cn, Scanner sc) { //Esto lo que hace es rellenar las tablas segun el equipo que elija
        try 
        {
            System.out.print("Dime el nombre del equipo (ejemplo: Barcelona, Madrid, etc.): ");
            String equipo = sc.nextLine().trim(); //Guardo el equipo y lo leo en una variable y le quito los espacios

            File xmlFile = buscarXmlPorEquipo(equipo); // Aqui busca el archivo xml que el nombre coincida con lo que introdijo el usuario
            if (xmlFile == null) { //En el caso de que no lo encuentre le avisa al user
                System.out.println("No existe XML para el equipo: " + equipo);
                return;
            }

            String tabla = quitarExtension(xmlFile.getName()); 

            // Asegura la tabla y evita duplicados si vuelvo a rellenar
            cn.crearTabla(tabla);
            cn.vaciarTabla(tabla);

            // 
            List<Jugador> jugadores = XMLreader.leerJugadores(xmlFile); //Aqui lo que hace es que lee el XML y crea una lista del jugador con la informacion del xml

            int insertados = 0; //cuenta los datos insertados
            for (int i = 0; i < jugadores.size(); i++) //recorre la lista de jugadores y lo inserta en la tabla
            {
                cn.insertarJugador(tabla, jugadores.get(i));
                insertados++;
            }

            System.out.println("Insertados " + insertados + " jugadores en la tabla: " + tabla); //Aqui muestra cuantos jugadores insertó

        } catch (Exception e) 
        {
            System.out.println("Error rellenando equipo: " + e.getMessage()); //Aqui si hay cualquier error
        }
    }

     
    private static void opcionMostrarEquipo(ConexionMySQL cn, Scanner sc) { //Pido el nombre del equipo y lo muestro en la consola
        try 
        {
            System.out.print("Nombre del equipo/tabla: "); //Aqui pido el nombre del equipo
            String equipo = sc.nextLine().trim(); //Lo leo y quito los espacios y lo guardo en la variable equipo

            List<Jugador> jugadores = cn.mostrarEquipo(equipo); // Hace el select en la tabla y lo muestra

            for (int i = 0; i < jugadores.size(); i++) //imrpime cada jugador usando el toString
            {
                System.out.println(jugadores.get(i));
            }

            if (jugadores.isEmpty()) System.out.println("(Tabla vacía o no existe)"); // Si no hay nada lo dice

        } 
        catch (Exception e) 
        {
            System.out.println("Error mostrando equipo: " + e.getMessage()); //Si hay un error
        }
    }

    
    private static void opcionEliminarTablas(ConexionMySQL cn) //Esto elimina todas las tablas
    {
        try 
        {
            File[] archivos = listarXml();
            int borradas = 0;

            for (int i = 0; i < archivos.length; i++) //Aqui lista todo lo que hay en el directorio que termine en .xml y lo borra hasta que borre todas
            {
                File f = archivos[i];

                if (f.isFile() && f.getName().toLowerCase().endsWith(".xml")) 
                {
                    String tabla = quitarExtension(f.getName());
                    cn.eliminarTabla(tabla);
                    borradas++;
                }
            }

            System.out.println("Tablas eliminadas: " + borradas);

            // (Opcional) eliminar el TYPE una vez no queden tablas que dependan de el.
            // Si quieres conservar el tipo para reutilizarlo, comenta esta linea.
            cn.eliminarTipoJugador();

        } 
        catch (Exception e) 
        {
            System.out.println("Error eliminando tablas: " + e.getMessage()); //Si pasa cualquier cosa muestra un error
        }
    }

    // Busca el XML por nombre de equipo (sin .xml), ignorando mayúsculas
    private static File buscarXmlPorEquipo(String equipo) throws Exception 
    {
        File[] archivos = listarXml(); //Busca dentro del directorio un XML cuyo nombre coincida sin importar las mayusculas

        for (int i = 0; i < archivos.length; i++) 
        { 
            File f = archivos[i];
            if (!f.isFile()) continue; //Si el archivo no coincide pasa al que esta despues

            if (!f.getName().toLowerCase().endsWith(".xml")) continue;

            if (f.getName().equalsIgnoreCase(equipo.trim() + ".xml")) return f; //Si coincide lo devuelve sino devuelve null
        }

        return null;
    }

    private static String quitarExtension(String s) 
    {
        return s.replaceFirst("\\.xml$", "");
    }
}

