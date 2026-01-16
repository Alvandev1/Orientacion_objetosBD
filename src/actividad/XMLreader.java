package actividad;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class XMLreader {

    public static List<Jugador> leerJugadores(File xmlFile) throws Exception { // Esto es un metodo estatico, lo puedo llamar sin crear un objeto. Devuelve la lista jugador recibe el xml como file

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); //Configuro y creo el DOM parser
        DocumentBuilder db = dbf.newDocumentBuilder(); //Este es el objeto encargado de leer el XML

        Document doc = db.parse(xmlFile); // Aqui lee el archivo y lo convierte en un documento
        doc.getDocumentElement().normalize(); // Aqui evitamos problemas con textos separados

        NodeList jugadores = doc.getElementsByTagName("jugador"); //Aqui busca en todo el documento las etiquetas donde salga jugador y los guarda en una lista
        List<Jugador> lista = new ArrayList<>(); // Crea una lista vacía donde iremos guardando los jugadores ya convertidos a objetos Jugador.

        for (int i = 0; i < jugadores.getLength(); i++) { //Este bucle lo que jace es recorrer todos los jugadores que ha encontrado
            Element elemento = (Element) jugadores.item(i); //Obtiene el jugador y lo convierte en element para poder acceder a sus etiquetas de cada uno

            String nombre = texto(elemento, "nombre"); // Llama al metodo texto para sacar su contenido <nombre>
            int dorsal = Integer.parseInt(texto(elemento, "dorsal")); // Llama al metodo texto para sacar su contenido <dorsal>
            String demarcacion = texto(elemento, "demarcacion"); // Llama al metodo texto para sacar su contenido <demarcacion>
            String nacimiento = texto(elemento, "nacimiento"); // Llama al metodo texto para sacar su contenido <nacimiento>

            lista.add(new Jugador(nombre, dorsal, demarcacion, nacimiento)); // Crea un objeto Jugador con esos datos y lo mete dentro de la lista.
        }

        return lista; //devuelve la lista de todos los jugadores leidos con sus respectivos xd
    }

    private static String texto(Element padre, String etiqueta) {
        return padre.getElementsByTagName(etiqueta).item(0).getTextContent().trim();
        // padre.getElementsByTagName(etiqueta) -> busca esa etiqueta dentro del padre (por ejemplo "nombre")
        // item(0) -> coge la primera coincidencia (se asume que existe una sola)
        // getTextContent() -> saca el texto que hay dentro de la etiqueta
        // trim() -> elimina espacios al principio y al final
    }
}