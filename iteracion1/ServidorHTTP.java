/*
* REDES 2018-2019
* Práctica 1 - Iteración 1
* Javier Cancela Mato - javier.cmato@udc.es - Grupo 1.4
*/

package iteracion1;

import java.util.Properties;
import java.io.*;
import java.net.*;

/* Servidor multithread encargado de gestionar las peticiones web.
   Así mismo, se encarga de crear registros de su actividad. */
public class ServidorHTTP {
    // ATRIBUTOS
    private final ServerSocket servidor;
    private final Socket cliente;
    private final int tiempoEspera = 30;                    // Riempo que espera por peticiones (en segundos)
    public static int puertoServidor;                       // Puerto para comunicarse con el servidor web
    FileInputStream ficheroEntrada = null;                  // Fichero de dónde se obtiene la información
    FileOutputStream ficheroSalida = null;                  // Fichero en dónde se escribe la información
    public static String registroAccesos, registroErrores;  // Nombre de registros que almacenan accesos y errores al conectarse al servdiro
    private PeticionHTTP peticionHTTP;                      // Gestiona las peticiones HTTP que recibe el servidor
    
    // CONSTRUCTORES
    public ServidorHTTP() {         // Reescritura constructor por defecto no necesita @override
        try {
            
        }
        catch () {
            
        }
    }
    
    // MÉTODOS
    private void configurarServidor(String rutaConfiguracion) {
        Properties propiedades = new Properties();
        
        try {
            if (!rutaConfiguracion.endsWith("/")) {     // Si ruta no termina en /
                rutaConfiguracion.concat("/");              // Se añade la / al final
                
            }
            else {
            }
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
        catch (IOException IOex) {
            System.out.println("Error: " + IOex.getMessage());
            IOex.printStackTrace();
        }
    }
}