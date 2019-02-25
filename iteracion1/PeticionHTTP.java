/*
* REDES 2018-2019
* Práctica 1 - Iteración 1
* Javier Cancela Mato - javier.cmato@udc.es - Grupo 1.4
*/

package iteracion1;

import java.net.*;
import java.io.*;

/* Clase cliente encargada de realizar las peticiones HTTP al servidor web. */
public class PeticionHTTP {
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
    private final Socket cliente = null;           // Cliente que realiza la petición
    private final BufferedReader entrada = null;   // Canal de entrada
    
    // CONSTRUCTORES
    public SolicitudHTTP(Socket cliente) {
        this.cliente = cliente;
        this.argumentos = new CabeceraHTTP();
    }
    
    // MÉTODOS
    
}
