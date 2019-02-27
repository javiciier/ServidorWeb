/*
* REDES 2018-2019
* Práctica 1 - Iteración 1
* Javier Cancela Mato - javier.cmato@udc.es - Grupo 1.4
*/

package iteracion1;

import java.net.*;
import java.io.*;

/**
* Clase cliente multihthread encargada de realizar las peticiones HTTP al servidor web.
*/
public class PeticionHTTP extends Thread {
    // ATRIBUTOS
    public enum Estado {                    // Posibles estados al conectarse al servidor
        OK("200 OK"),                           // Conexión sin problemas
        BAD_REQUEST("400 Bad Request"),         // Servidor no entiende petición
        FORBIDDEN("403 Forbidden Request"),     // Servidor se niega a tramitar petición
        NOT_FOUND("404 Not Found");             // No existe recurso en el servidor
    
        private final String valor;
        
        // SETTER
        Estado(String valor) {
            this.valor = valor;
        }
        
        // GETTER
        public String getValor() {
            return this.valor;
        }
    }
    private final Socket cliente;                                               // Cliente que realiza la petición
    private BufferedReader entrada = null;                                      // Información a leer en la entrada
    private OutputStream salida = null;                                         // Datos de salida
    private final CabeceraHTTP cabecera;
    
    // CONSTRUCTORES
    public PeticionHTTP(Socket cliente) {
        this.cliente = cliente;
        this.cabecera = new CabeceraHTTP();
        
        try {
            // Obtenemos la entrada. API Java recomienda usar clase wrapper BufferedReader para InputStreamReader
            this.entrada = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
            this.salida = cliente.getOutputStream();                            // Muestra la información del socket (posible IOException si socket no contiene información o no existe)
        }
        catch (IOException IOexc) {
            System.out.println("Error: " + IOexc.getMessage());
            IOexc.printStackTrace();
        }
    }
    
    // MÉTODOS
        // Metodo run() para comenzar a ejecutar el thread
        // Método GEThttp()
        // Método HEADhttp()
        // Método verContenido() para conocer qué tipo de contenido tiene el fichero web
}
