package actividad;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * Clase para la conexión con una base de datos MySQL
 *
 * @author Francisco Jesús Delgado Almirón
 */
public class ConexionMySQL {

    // Base de datos a la que nos conectamos
    private String BD;
    // Usuario de la base de datos
    private String USUARIO;
    // Contraseña del usuario de la base de datos
    private String PASS;
    // Objeto donde se almacenará nuestra conexión
    private Connection connection;
    // Indica que está en localhost
    private String HOST;
    // Zona horaria
    private TimeZone zonahoraria;

    /**
     * Constructor de la clase
     *
     * @param usuario Usuario de la base de datos
     * @param pass Contraseña del usuario
     * @param bd Base de datos a la que nos conectamos
     */
    public ConexionMySQL(String usuario, String pass, String bd) {
        HOST = "localhost:1521";
        USUARIO = usuario;
        PASS = pass;
        BD = bd;
        connection = null;
    }

    /**
     * Comprueba que el driver de MySQL esté correctamente integrado
     *
     * @throws SQLException Se lanzará cuando haya un fallo con la base de datos
     */
    private void registrarDriver() throws SQLException {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Error al conectar con Oracle D: : " + e.getMessage());
        }
    }

    /**
     * Conecta con la base de datos
     *
     * @throws SQLException Se lanzará cuando haya un fallo con la base de datos
     */
    public void conectar() throws SQLException {
        if (connection == null || connection.isClosed()) {
            registrarDriver();
            connection = (Connection) DriverManager.getConnection("jdbc:oracle:thin:@//" + HOST + "/" + BD, USUARIO,PASS);
            
        }
    }

    /**
     * Cierra la conexión con la base de datos
     *
     * @throws SQLException Se lanzará cuando haya un fallo con la base de datos
     */
    public void desconectar() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    /**
     * Ejecuta una consulta SELECT
     *
     * @param consulta Consulta SELECT a ejecutar
     * @return Resultado de la consulta
     * @throws SQLException Se lanzará cuando haya un fallo con la base de datos
     */
    public ResultSet ejecutarSelect(String consulta) throws SQLException {
        Statement stmt = connection.createStatement();
        ResultSet rset = stmt.executeQuery(consulta);

        return rset;
    }

    /**
     * Ejecuta una consulta INSERT, DELETE o UPDATE
     *
     * @param consulta Consulta INSERT, DELETE o UPDATE a ejecutar
     * @return Cantidad de filas afectadas
     * @throws SQLException Se lanzará cuando haya un fallo con la base de datos
     */
    public int ejecutarInsertDeleteUpdate(String consulta) throws SQLException {
        Statement stmt = connection.createStatement();
        int fila = stmt.executeUpdate(consulta);

        return fila;
    }

    /* =====================================================================
     *  AMPLIACION PARA ORACLE XE (segun enunciado del PDF)
     * ===================================================================== */

    /**
     * Normaliza el nombre de tabla a un identificador valido de Oracle.
     * - Solo letras/numeros/_
     * - Debe empezar por letra (si no, se antepone T_)
     * - Longitud maxima recomendada: 30
     */
    private String normalizarNombreTabla(String nombreArchivoSinExtension) {
        String t = nombreArchivoSinExtension == null ? "" : nombreArchivoSinExtension.trim();
        t = t.replaceAll("[^A-Za-z0-9_]", "_");
        if (t.isEmpty()) t = "EQUIPO";
        if (!Character.isLetter(t.charAt(0))) t = "T_" + t;
        if (t.length() > 30) t = t.substring(0, 30);
        return t.toUpperCase();
    }

    /**
     * Crea el tipo de dato de Oracle para Jugador (si no existe).
     */
    public void crearTipoJugador() throws SQLException {
        // ORA-00955: name is already used by an existing object
        final String sql = "CREATE TYPE JUGADOR_T AS OBJECT (" +
                "nombre VARCHAR2(100), " +
                "dorsal NUMBER, " +
                "demarcacion VARCHAR2(50), " +
                "nacimiento VARCHAR2(20)" +
                ")";
        try {
            ejecutarInsertDeleteUpdate(sql);
        } catch (SQLException e) {
            if (e.getErrorCode() != 955) throw e;
        }
    }

    /**
     * Elimina el tipo de dato Jugador (si existe).
     * Nota: FORCE permite borrar aunque haya dependencias (si tu Oracle lo soporta).
     */
    public void eliminarTipoJugador() throws SQLException {
        try {
            ejecutarInsertDeleteUpdate("DROP TYPE JUGADOR_T FORCE");
        } catch (SQLException e) {
            // ORA-04043: object does not exist
            if (e.getErrorCode() != 4043) throw e;
        }
    }

    /**
     * Crea la tabla de un equipo usando el nombre del archivo XML como nombre.
     * La tabla se crea como "object table" del tipo JUGADOR_T.
     */
    public void crearTabla(String nombreEquipo) throws SQLException {
        crearTipoJugador();
        String tabla = normalizarNombreTabla(nombreEquipo);
        final String sql = "CREATE TABLE " + tabla + " OF JUGADOR_T";
        try {
            ejecutarInsertDeleteUpdate(sql);
        } catch (SQLException e) {
            // ORA-00955: name is already used by an existing object
            if (e.getErrorCode() != 955) throw e;
        }
    }

    /**
     * Elimina una tabla.
     */
    public void eliminarTabla(String nombreEquipo) throws SQLException {
        String tabla = normalizarNombreTabla(nombreEquipo);
        try {
            ejecutarInsertDeleteUpdate("DROP TABLE " + tabla + " PURGE");
        } catch (SQLException e) {
            // ORA-00942: table or view does not exist
            if (e.getErrorCode() != 942) throw e;
        }
    }

    /**
     * Vacía una tabla.
     */
    public void vaciarTabla(String nombreEquipo) throws SQLException {
        String tabla = normalizarNombreTabla(nombreEquipo);
        try {
            ejecutarInsertDeleteUpdate("TRUNCATE TABLE " + tabla);
        } catch (SQLException e) {
            // ORA-00942: table or view does not exist
            if (e.getErrorCode() != 942) throw e;
        }
    }

    /**
     * Inserta un jugador en la tabla del equipo.
     */
    public void insertarJugador(String nombreEquipo, Jugador j) throws SQLException {
        String tabla = normalizarNombreTabla(nombreEquipo);
        // Object table: INSERT ... VALUES (JUGADOR_T(...))
        final String sql = "INSERT INTO " + tabla + " VALUES (JUGADOR_T(?, ?, ?, ?))";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, j.getNombre());
            ps.setInt(2, j.getDorsal());
            ps.setString(3, j.getDemarcacion());
            ps.setString(4, j.getNacimiento());
            ps.executeUpdate();
        }
    }

    /**
     * Devuelve los jugadores de un equipo desde la tabla.
     */
    public List<Jugador> mostrarEquipo(String nombreEquipo) throws SQLException {
        String tabla = normalizarNombreTabla(nombreEquipo);
        List<Jugador> jugadores = new ArrayList<>();
        final String sql = "SELECT nombre, dorsal, demarcacion, nacimiento FROM " + tabla + " ORDER BY dorsal";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                jugadores.add(new Jugador(
                        rs.getString(1),
                        rs.getInt(2),
                        rs.getString(3),
                        rs.getString(4)
                ));
            }
        } catch (SQLException e) {
            // ORA-00942: table or view does not exist
            if (e.getErrorCode() == 942) return jugadores;
            throw e;
        }
        return jugadores;
    }
}
