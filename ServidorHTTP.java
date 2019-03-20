/*
* REDES 2018-2019
* Práctica 1 - Iteración 2
* Javier Cancela Mato - javier.cmato@udc.es - Grupo 1.4
*/

import java.io.*;
import java.net.*;
import java.util.Properties;

/**
* Servidor multithread encargado de gestionar peticiones web HTTP.
* A partir de la iteracion2 podrá realizar tareas como:
* * Crear ficheros con registros de su actividad (peticiones recibidas, errores)-
* * Autoconfiguración a partir de un fichero de configuración.
*/
public class ServidorHTTP {
    // ATRIBUTOS
    private ServerSocket servidor;
    private Socket cliente;
    private int puerto = 5000;                                                  // Puerto que ocupará el servidor
    private final int tiempoEspera = 300;                                        // Tiempo que espera por peticiones (en segundos)
    private PeticionHTTP peticionHTTP;                                          // Gestiona las peticiones HTTP que recibe el servidor
    public static String rutaServidor = "/home/fic/Escritorio/Redes/Practicas/p1";         // Indica el directorio donde se ubica el servidor dentro del ordenador
    public static String versionServidor = "HTTP/1.1";                          // Versión de HTTP del servidor. Iteracion 1: HTTP 1.0; Iteracion 2: HTTP 1.1
    public static String nombreServidor = "Servidor HTTP de Javier";            // Nombre asignado al servidor
    public static String registroAccesos = rutaServidor + "AccesosServidor";    // Ruta hacia el registro que almacena los accesos al servidor
    public static String registroErrores = rutaServidor + "ErroresServidor";    // Ruta hacia el registro que almacena los errores producidos en las peticiones al servidor
    public static String recursoPorDefecto;                                     // Fichero que devuelve el servidor por defecto
    public boolean permiso;
    enum ConfiguracionDefecto {
        /* Todos los campos almacenados serán Strings porque serán los valores
        *  que almacena la clase Properties de Java dentro del fichero.
        */
        PORT("5000"),                                                           // Puerto por defecto que abre el servidor
        DIRECTORY_INDEX("index.html"),                                          // Fichero por defecto que muestra el servidor
        DIRECTORY("ServidorHTTP"),                                              // Nombre del directorio donde se almacena el servidor
        ALLOW("false");                                                         // INdica si se recibe una petición a un archivo (false) o a un directorio (true) (false por defecto
        
        private final String argumento;
        
        // CONSTRUCTORES
        ConfiguracionDefecto(String argumento) {
            this.argumento = argumento;
        }
        
        // GETTERS
        public String getValor() {
            return this.argumento;
        }
    }
    
    // CONSTRUCTORES
    /**
    * Constructor sin argumentos que crea un socket para el servidor en el puerto indicado.
    */
    public ServidorHTTP() {
        try {
            this.servidor = new ServerSocket(this.puerto);                      // Crea el servidor en el puerto indicado (genera IOException si puerto está ocupado)
            System.out.println("Servidor abierto en puerto " + this.puerto);
            this.servidor.setSoTimeout(tiempoEspera*1000);                      // Establece tiempo de espera indicado (x1000 ms)
            System.out.println("Tiempo de espera establecido: " + this.tiempoEspera + " segundos.");
        }
        catch (IOException IOexc) {
            System.err.println("Runtime Error: puerto " + this.puerto + " está ocupado.");
            System.err.println("Error: " + IOexc.getMessage());
        }
    }
    
    /**
    * Crea el servidor y lo configura a partir de un fichero de configuración recibido como parámetro.
    * @param rutaConfiguracion        Ruta hacia el fichero que contiene la configuración por defecto
    */
    public ServidorHTTP(String rutaConfiguracion) {
        try {
            configurarServidor(rutaConfiguracion);
            this.servidor = new ServerSocket(this.puerto);
            this.servidor.setSoTimeout(this.tiempoEspera);
            System.out.println("Servidor configurado a partir del fichero de configuración " + new File(rutaConfiguracion).getName());
        }
        catch (IOException IOexc) {
            System.err.println("Error: no se pudo configurar servidor a partir del fichero.");
            System.err.println(IOexc.getMessage());
        }
    }
    
    // MÉTODOS    
    public void escucharPeticiones() {
        try {
            while (true) {          // Bucle infinito para que servidor siempre escuche peticiones
                this.cliente = this.servidor.accept();                          // Servidor acepta conexión al recibirla (puede generar IOException)
                //System.out.println("Conexión aceptada.");
                this.peticionHTTP = new PeticionHTTP(this.cliente);             // Crea una nueva petición HTTP que gestiona la conexión recibida
                this.peticionHTTP.start();                                      // Comienza a ejecutar el nuevo thread creado anteriormente
            }
        }
        catch (SocketTimeoutException TOexc) {
            System.err.println("Tiempo agotado. " + tiempoEspera + " segundos sin recibir peticiones");
        }
        catch (IOException IOexc) {
            System.out.println("Error E/S: " + IOexc.getMessage());
        }
        finally {                                                               // Si no recibe ninguna petición, cierra el puerto y captura posibles excepciones
            try {
                this.servidor.close();
            }
            catch (IOException IOexc) {
                System.err.println("Error: no se pueden liberar recursos.");
                throw new RuntimeException(IOexc);
            }
        }
    }
    
    /**
    * Método que autoconfigura el servidor a partir de un fichero con las configuraciones por defecto.
    * Mediante un boolean se puede comprobar si se ha configurado correctamente.
    * @param rutaConfiguracion    Ruta hacia el directorio en donde está el fichero con las propiedades del servidor.
    * @return                     TRUE si se puede configurar el servidor correctamente; FALSE en caso contrario.
    */
    private boolean configurarServidor(String rutaConfiguracion) {
        Properties propiedades = new Properties();                              // Permite manejar las propiedades del servidor
        File ficheroConfiguracion = null;                                       // Fichero con la configuración del servidor
        String nombreFichero = "servidor.properties";                           // Nombre del fichero que almacena la configuración
        FileInputStream canalEntrada = null;                                    // Permite obtener los datos de entrada
        FileOutputStream canalSalida = null;                                    // Muestra los datos obtenidos
        // Variables auxiliares temporales
        String auxPuerto;
        String auxIndice;
        String auxDirectorio;
        String auxAllow;
        
        try {
            if ( !rutaConfiguracion.endsWith("/") )                             // Si la ruta de configuración no es un directorio
                rutaConfiguracion += "/";                                       // Se añade / al final para que sea directorio
            ficheroConfiguracion = new File(rutaConfiguracion + nombreFichero); // Obtiene el nombre del fichero que contiene la configuración
            ficheroConfiguracion.createNewFile();                               // En caso de que dicho fichero no exista, lo crea
            canalEntrada = new FileInputStream(ficheroConfiguracion);           // Los datos se obtendrán a partir del fichero de configuración
            propiedades.load(canalEntrada);                                     // Obtiene las propiedades almacenadas en el fichero de configuración del servidor
            
            /* En caso de que el fichero de configuración fuese creado de cero,
            no tendrá ninguna información almacenada, por lo que debemos escribir
            en el fichero las propiedades y configuración por defecto del servidor. */
            while ( (auxPuerto = propiedades.getProperty(ConfiguracionDefecto.PORT.name())) == null ) {             // Si no está guardado el nº de puerto en el fichero
                propiedades.setProperty(ConfiguracionDefecto.PORT.name(), ConfiguracionDefecto.PORT.getValor());    // Se asocia un valor al nº de puerto dentro del fichero
            }
            this.puerto = Integer.parseInt(auxPuerto);                          // Asigna al servidor el puerto obtenido desde el fichero (convertido de String a Integer)
            
            while ( (auxIndice = propiedades.getProperty(ConfiguracionDefecto.DIRECTORY_INDEX.name())) == null ) {                          // Si no está guardado el fichero por defecto
                propiedades.setProperty(ConfiguracionDefecto.DIRECTORY_INDEX.name(), ConfiguracionDefecto.DIRECTORY_INDEX.getValor());      // Escribe cual es el fichero a obtener (generalmente suele ser index.html)
            }
            if ( auxIndice.startsWith("/") ) {                                    // Si el recurso a obtener comienza por "/" (es directorio)
                auxIndice = auxIndice.substring(1);                             // Se elimina la "/" inicial
            }
            
            while ( (auxDirectorio = propiedades.getProperty(ConfiguracionDefecto.DIRECTORY.name())) == null ) {
                propiedades.setProperty(ConfiguracionDefecto.DIRECTORY.name(), ConfiguracionDefecto.DIRECTORY.getValor());
            }
            if ( auxDirectorio.endsWith("/") ) {                                                      // Si el directorio recibido finaliza en "/"
                auxDirectorio = auxDirectorio.substring(0, auxDirectorio.lastIndexOf("/") - 1);     // Elimina la "/" al final
            }
            
            while ( (auxAllow = propiedades.getProperty(ConfiguracionDefecto.ALLOW.name())) == null ) {
                propiedades.setProperty(ConfiguracionDefecto.ALLOW.name(), ConfiguracionDefecto.ALLOW.getValor());
            }
            this.permiso = Boolean.parseBoolean(auxAllow);
            
            canalSalida = new FileOutputStream(ficheroConfiguracion);           // Las propiedades se escribirán en el fichero de configuración
            propiedades.store(canalSalida, "Configuración por defecto del servidor:");      // Escribe las propiedades en el fichero y añade un comentario            
        } // fin try
        catch (FileNotFoundException FNFexc) {
            System.err.println("Error: no existe el fichero " + nombreFichero);
            return false;
        }
        catch (IOException IOexc) {
            System.err.println("IOexc: " + IOexc.getMessage());
        }
        finally {                                                               // Cerramos los canales de entrada y salida y capturamos sus posibles excepciones
            try {
                canalEntrada.close();
                canalSalida.close();
            }
            catch (IOException IOexc) {
                System.err.println("Error: no se pueden liberar recursos del servidor.");
                throw new RuntimeException(IOexc);
            }
        } // fin finally
        
        return true;
    }
    
    
    // MÉTODO MAIN
    /**
    * Se encarga de crear una instancia del servidor para que escuche las peticiones recibidas. 
    */
    public static void main(String[] args) {
        ServidorHTTP servidor;                                                  // Declara el servidor a usar
        
        switch( args.length ) {                                                 // Según el nº de parámetros recibidos
            case 0:         // NO recibe ningún argumento
                servidor = new ServidorHTTP();                                  // Crea el servidor por defecto
            break;
            case 1:         // Recibe la ruta al fichero de configuracion
                File config = new File(args[0]);                                // Obtiene el fichero
                if ( config.exists() ) {
                    servidor = new ServidorHTTP(args[0]);                       // Llama al constructor con parámetros
                }
            break;
            default:        // Recibe más de 1 argumento
                System.err.println("FORMATO: ServidorHTTP <ficheroConfiguracion>");
                System.exit(-1);
            break;
        }
        servidor.escucharPeticiones();
    }
}
