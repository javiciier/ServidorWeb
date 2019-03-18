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
    private final int tiempoEspera = 60;                                        // Tiempo que espera por peticiones (en segundos)
    private PeticionHTTP peticionHTTP;                                          // Gestiona las peticiones HTTP que recibe el servidor
    public static String rutaServidor = "/home/fic/Escritorio/Redes/Practicas/p1";         // Indica el directorio donde se ubica el servidor dentro del ordenador
    public static String versionServidor = "HTTP/1.1";                          // Versión de HTTP del servidor. Iteracion 1: HTTP 1.0; Iteracion 2: HTTP 1.1
    public static String nombreServidor = "ServidorHTTP-iteracion2";            // Nombre asignado al servidor
    public static String registroAccesos = rutaServidor + "AccesosServidor";    // Ruta hacia el registro que almacena los accesos al servidor
    public static String registroErrores = rutaServidor + "ErroresServidor";    // Ruta hacia el registro que almacena los errores producidos en las peticiones al servidor
    enum ConfiguracionDefecto {
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
        public String getConfiguracion() {
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
        FileInputStream canalEntrada = null;                                        // Permite obtener los datos de entrada
        FileOutputStream canalSalida = null;                                        // Muestra los datos obtenidos
        
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
            generarConfiguracionPorDefecto(rutaConfiguracion);                  // PENDIENTE DE IMPLEMENTAR
        }
        catch (FileNotFoundException FNFexc) {
            System.err.println("Error: no existe el fichero " + nombreFichero);
            return false;
        }
        catch (IOException IOexc) {
            System.err.println("IOexc: " + IOexc.getMessage());
        }
        
        return true;
    }

    
    
    // MÉTODO MAIN
    /**
    * Se encarga de crear una instancia del servidor para que escuche las peticiones recibidas. 
    */
    public static void main(String[] args) {
        ServidorHTTP servidor = new ServidorHTTP();                             // Crea una instancia del servidor
        servidor.escucharPeticiones();                                          // Servidor comienza a ejecutarse
    }
}
