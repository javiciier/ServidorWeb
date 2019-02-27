/*
* REDES 2018-2019
* Práctica 1 - Iteración 1
* Javier Cancela Mato - javier.cmato@udc.es - Grupo 1.4
*/

package iteracion1;

import java.util.Properties;
import java.io.*;
import java.net.*;

/**
* Servidor multithread encargado de gestionar peticiones web HTTP.
* Así mismo, se encarga de crear registros de su actividad.
* El servidor también puede configurarse a partir de un fichero de configuración.
*/
public class ServidorHTTP {
    // ATRIBUTOS
    private final ServerSocket servidor;
    private Socket cliente;
    private final int tiempoEspera = 60;                                        // Tiempo que espera por peticiones (en segundos)
    public static int puertoServidor;                                           // Puerto para comunicarse con el servidor web
    public static String registroAccesos, registroErrores;                      // Nombre de registros que almacenan accesos y errores al conectarse al servdiro
    private PeticionHTTP peticionHTTP;                                          // Gestiona las peticiones HTTP que recibe el servidor
    
    
    // CONSTRUCTORES
    public ServidorHTTP() {         // Reescritura constructor por defecto no necesita @override
        try {
            
        }
        catch () {
            
        }
    }
    
    // MÉTODOS
    /**
    * Configura servidor a partir de un fichero de configuración.
    * @param rutaConfiguracion Ruta hacia el directorio que contiene fichero de configuración del servidor
    */
    private void configurarServidor(String rutaConfiguracion) {
        Properties propiedades = new Properties();
        File ficheroConfig;
        FileInputStream canalEntrada = null;                                    // Fichero de dónde se obtiene la información del servidor
        FileOutputStream canalSalida = null;                                    // Fichero en dónde se escribe la información
        
        try {
            if (!rutaConfiguracion.endsWith("/")) {                             // Si la ruta no termina en /
                rutaConfiguracion.concat("/");                                  // Se añade la / al final
            ficheroConfig = new File(rutaConfiguracion + "FicheroConfiguracion");   // Nombra el fichero de configuracion
            ficheroConfig.createNewFile();                                      // Crea un fichero con el nombre asociado. (IOException si ya existe)
            canalEntrada = new FileInputStream(ficheroConfig);                  // Obtiene información del fichero de configuración
            }
            else {
            }
        }
        catch (IOException IOexc) {
            System.out.println("Error " + IOexc.getMessage());
        }
    }
    
    public void escucharPeticiones() {
        try {
            while (true) {          // Bucle infinito para que servidor permanezca siempre escuchando peticiones
                this.cliente = this.servidor.accept();                          // Servidor acepta petición al recibirla (puede generar IOException)
                this.peticionHTTP = new PeticionHTTP(this.cliente);             // Crea un nuevo cliente que gestiona la petición web (crea nuevo thread)
                this.peticionHTTP.start();                                      // Comienza a ejecutar el nuevo thread creado anteriormente
            }
        }
        catch (IOException IOexc) {
            System.out.println("Error: " + IOexc.getMessage());
            IOexc.printStackTrace();
        }
    }
}