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
    private ServerSocket servidor = null;
    private Socket cliente = null;
    private final int tiempoEspera = 30;                    // Riempo que espera por peticiones (en segundos)
    public static int puertoServidor;                       // Puerto para comunicarse con el servidor web
    FileInputStream ficheroEntrada = null;                  // Fichero de dónde se obtiene la información
    FileOutputStream ficheroSalida = null;                  // Fichero en dónde se escribe la información
    public static String registroAccesos, registroErrores;  // Nombre de registros que almacenan accesos y errores al conectarse al servdiro
    
    
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
}