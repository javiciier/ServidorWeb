/*
* REDES 2018-2019
* Práctica 1 - Iteración 1
* Javier Cancela Mato - javier.cmato@udc.es - Grupo 1.4
*/

import java.io.*;
import java.net.*;

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
    public static String versionServidor = "HTTP/1.0";                          // Versión de HTTP del servidor. Iteracion 1: HTTP 1.0; Iteracion 2: HTTP 1.1
    public static String nombreServidor = "ServidorHTTP-iteracion1";            // Nombre asignado al servidor
    //public static String recursoPorDefecto = "index.html";                      // Recurso que el servidor entregará por defecto cuando reciba una petición
    
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
    
    // MÉTODO MAIN
    /**
    * Se encarga de crear una instancia del servidor para que escuche las peticiones recibidas. 
    */
    public static void main(String[] args) {
        ServidorHTTP servidor = new ServidorHTTP();                             // Crea una instancia del servidor
        servidor.escucharPeticiones();                                          // Servidor comienza a ejecutarse
    }
}
