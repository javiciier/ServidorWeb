/*
* REDES 2018-2019
* Práctica 1 - Iteración 1
* Javier Cancela Mato - javier.cmato@udc.es - Grupo 1.4
*/

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
    private ServerSocket servidor;
    private Socket cliente;
    private int puerto = 5000;                                                  // Puerto que ocupará el servidor
    private final int tiempoEspera = 60;                                        // Tiempo que espera por peticiones (en segundos)
    public static int puertoServidor;                                           // Puerto para comunicarse con el servidor web
    public static String registroAccesos, registroErrores;                      // Nombre de registros que almacenan accesos y errores al conectarse al servdiro
    private PeticionHTTP peticionHTTP;                                          // Gestiona las peticiones HTTP que recibe el servidor
    
    
    // CONSTRUCTORES
    /**
    * Constructor sin argumentos que crea un socket para el servidor en el puerto indicado.
    */
    public ServidorHTTP() {
        try {
            this.servidor = new ServerSocket(puerto);                           // Crea el servidor en el puerto indicado
            this.servidor.setSoTimeout(tiempoEspera*1000);                      // Establece tiempo de espera indicado (x1000 ms)
        }
        catch (IOException IOexc) {
            System.out.println("Error: " + IOexc.getMessage());
        }
    }
    
    // MÉTODOS    
    public void escucharPeticiones() {
        try {
            while (true) {          // Bucle infinito para que servidor siempre escuche peticiones
                this.cliente = this.servidor.accept();                          // Servidor acepta petición al recibirla (puede generar IOException)
                this.peticionHTTP = new PeticionHTTP(this.cliente);             // Crea un nuevo cliente que gestiona la petición web (crea nuevo thread)
                this.peticionHTTP.start();                                      // Comienza a ejecutar el nuevo thread creado anteriormente
            }
        }
        catch (SocketTimeoutException TOexc) {
            System.err.println("Tiempo agotado." + this.tiempoEspera + "segundos sin recibir peticiones");
        }
        catch (IOException IOexc) {
            System.out.println("Error: " + IOexc.getMessage());
        }
        finally {                                                               // Si no recibe ninguna petición, cierra el puerto y captura posibles excepciones
            try {
                this.servidor.close();
            }
            catch (IOException IOexc) {
                System.err.println("Error: " + IOexc.getMessage());
            }
        }
    }
}