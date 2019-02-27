/*
* REDES 2018-2019
* Práctica 1 - Iteración 1
* Javier Cancela Mato - javier.cmato@udc.es - Grupo 1.4
*/

import java.net.*;
import java.io.*;

/**
* Clase cliente multihthread encargada de realizar las peticiones HTTP al servidor web.
*/
public class PeticionHTTP extends Thread {
    // ATRIBUTOS
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
        }
    }
    
    // MÉTODOS
        // Metodo run() para comenzar a ejecutar el thread
        // Método GEThttp()
        // Método HEADhttp()
        // Método verContenido() para conocer qué tipo de contenido tiene el fichero web
}
