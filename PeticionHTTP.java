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
    private String comando;														// Comando que se solicita realizar
    public enum EstadoHTTP {													// Posibles estados a recibir cuando se realiza una solicitud HTTP
    	OK("200 OK"),															// Recurso se recibió sin errores
    	NOT_FOUND("404 Not Found");												// Recurso no se encontró o no existe
    	// recordar eliminar ; al añadir nuevos estados

    	private final String estado;

    	// CONSTRUCTOR DEL ENUMERADO
    	EstadoHTTP(String estado) {
    		this.estado = estado;
    	}

    	// GETTER DEL ENUMERADO
    	public String getEstadoHTTP() {
    		return this.estado;
    	}
    }

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
    /**
     * Método que obtiene la cabecera de una petición HTTP según formato RFC 1945.
     * Formato: "peticion" "recurso" "versionHTTP"
     */
    private void HEADhttp() {
    	String ruta = this.comando.split(" ")[1];								// Obtiene el recurso solicitado
    	File recurso = new File(ServidorHTTP.rutaServidor + ruta);				// Busca el recurso en el mismo directorio o subdirectorios en donde está ubicado el servidor
    	
    	if (recurso.exists()) {													// Si el recurso buscado existe en algún directorio
    		mostrarRespuesta(EstadoHTTP.OK, recurso);							// Petición correcta. Muestra la información del recurso recibido como argumento
    	else
    		mostrarRespuesta(EstadoHTTP.NOT_FOUND, null);						// Muestra ERROR 404 porque no existe archivo. Al no existir, no le pasa como argumento ningún recurso
    	}
    }

    private void GEThttp() {

    }

    /**
     * Método encargado de mostrar por pantalla la información asociada a la petición realizada.
     * @param estado 	Estado que debería devolver la solicitud según su situación (archivo encontrado, nombre incorrecto, etc)
     * @param recurso 	Fichero del cual obtiene la información solicitada
     */
    private void mostrarRespuesta(EstadoHTTP estado, File recurso) {
    	PrintWriter canalSalida = new PrintWriter(this.salida, true);			// Establece un canal de salida para mostrar información por pantalla
    	String versionHTTP:														// Indica la versión HTTP empleada (HTTP 1.0 o HTTP 1.1)
    
    	switch (estado) {														// Muestra distinta información por el canal de salida según el estado de la petición recibida
    		case OK:
    			// Implementar OK
    			break;		// fin OK
    		case NOT_FOUND:
    			// Implementar NOT_FOUND
    			break;		// fin NOT_FOUND
    	}	// fin switch
    }

        // Método GEThttp()
    	// Método mostrarRespuesta() que devuelve la información solicitada en la petición
        // Método verContenido() para conocer contenido (texto, gif, imágenes, etc)
    	// Metodo run() para comenzar a ejecutar el thread
}
