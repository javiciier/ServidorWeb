/*
* REDES 2018-2019
* Práctica 1 - Iteración 1
* Javier Cancela Mato - javier.cmato@udc.es - Grupo 1.4
*/

import java.io.*;
import java.net.*;
import java.util.Date;

/**
* Clase cliente multihthread encargada de realizar las peticiones HTTP al servidor web.
*/
public class PeticionHTTP extends Thread {
    // ATRIBUTOS
    private final Socket cliente;                                               // Cliente que realiza la petición
    private BufferedReader entrada = null;                                      // Información a leer en la entrada
    private OutputStream salida = null;                                         // Datos de salida
    private final CabeceraHTTP cabecera;
    private String lineaComandos;                                               // Comando que se solicita realizar
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
    	String ruta = this.lineaComandos.split(" ")[1];				// Obtiene el recurso solicitado
    	File recurso = new File(ServidorHTTP.rutaServidor + ruta);		// Busca el recurso en el mismo directorio o subdirectorios en donde está ubicado el servidor
    	
    	if (recurso.exists())							// Si el recurso buscado existe en algún directorio
    		mostrarRespuesta(EstadoHTTP.OK, recurso);			// Petición correcta. Muestra la información del recurso recibido como argumento
    	else
    		mostrarRespuesta(EstadoHTTP.NOT_FOUND, null);			// Muestra ERROR 404 porque no existe archivo. Al no existir, no le pasa como argumento ningún recurso
    }

    /**
     * Método que obtiene el cuerpo (contenido) de una peticion HTTP.
     */
    private void GEThttp() {
        String[] comandos = this.lineaComandos.split(" ");                      // Separa los elementos de la línea de comandos. Formato <COMANDO> <RUTA> <VERSION_HTTP>
        File recurso = new File(ServidorHTTP.rutaServidor + comandos[1]);       // Obtiene el recurso buscado a partir de la ruta del servidor y la ruta especificada.
        Date fechaModifServidor, fechaModifCliente;
        String index;                                                           // Ruta hacia el recurso principal. Por defecto es el fichero index.html
        
        if (!recurso.exists()) {                                                // Si el recurso no existe
            mostrarRespuesta(EstadoHTTP.NOT_FOUND, null);
        }
        else {                                                                  // El recurso SÍ existe
            try {
                if ( !recurso.isDirectory() ) {                                   // El recurso no es un directorio
                    fechaModifCliente = this.cabecera.getFecha();               // Obtiene la fecha de modificación del recurso solicitado
                    fechaModifServidor = new Date(recurso.lastModified());      // Obtiene la fecha de modificación del recurso obtenido por el servidor
                    if (fechaModifServidor.compareTo(fechaModifCliente) > 0 )   // Comprueba si la fecha de modificación del recurso en el servidor es posterior a la fecha del recurso
                        mostrarRespuesta(EstadoHTTP.OK, recurso);
                    else                                                        // Recurso ha sido modificado después del almacenado en el servidor, por lo tanto no puede encontrar el recurso exacto
                        mostrarRespuesta(EstadoHTTP.NOT_FOUND, null);
                }  // fin if
                else {                                                          // Recurso recibido ES un directorio
                    if ( !comandos[1].endsWith("/") )                           // Si el directorio no acaba con "/", se le añade
                        comandos[1] += "/";
                    index = ( ServidorHTTP.rutaServidor + comandos[1] + ServidorHTTP.recursoPorDefecto);  // Obtiene el nombre del recurso principal que entrega el servidor
                    File recursoPrincipal = new File(index);                    // Carga el recurso principal
                    if (recursoPrincipal.exists())                              // Si recurso principal existe, lo muestra
                        mostrarRespuesta(EstadoHTTP.OK, recursoPrincipal);
                }  // fin else
            }
            catch (NullPointerException NPex) {
                System.err.println("Error: " + NPex.getMessage());
            }
        }  // fin else
    }
    
    
    /**
     * Método encargado de mostrar por pantalla la información asociada a la petición realizada.
     * @param estado 	Estado que debería devolver la solicitud según su situación (archivo encontrado, nombre incorrecto, etc)
     * @param recurso 	Fichero del cual obtiene la información solicitada
     */
    private void mostrarRespuesta(EstadoHTTP estado, File recurso) {
    	PrintWriter canalSalida = new PrintWriter(this.salida, true);		// Establece un canal de salida para mostrar información por pantalla
    	String textoEstado;							// Texto que se mostrará según el estado de la petición
        Date fechaRespuesta;                                                    // Almacena la fecha en la que se generó la petición
        Date ultimaModificacion;
    
    	switch (estado) {							// Muestra distinta información por el canal de salida según el estado de la petición recibida
            case OK:
                // LÍNEA DE ESTADO
                textoEstado = ( ServidorHTTP.versionServidor + " " + estado.OK.getEstadoHTTP() );
                canalSalida.println(textoEstado);
                // LÍNEAS DE CABECERA
                fechaRespuesta = new Date();
                canalSalida.println("Date: " + fechaRespuesta);
                canalSalida.println("Server: " + ServidorHTTP.nombreServidor);
                if ( recurso != null ) {                                        // Si recibe un recurso muestra su información
                    ultimaModificacion = new Date(recurso.lastModified());
                    canalSalida.println("Last-Modified: " + ultimaModificacion);
                    canalSalida.println("Content-Length: " + recurso.length());
                    canalSalida.println("Content-Type: " + getTipoContenido(recurso.getName()));
                }
            break;  // fin OK
            case NOT_FOUND:
                // LÍNEA DE ESTADO
                textoEstado = ( ServidorHTTP.versionServidor + " " + estado.NOT_FOUND.getEstadoHTTP() );
                canalSalida.println(textoEstado);
                // LÍNEAS DE CABECERA
                
            break;  // fin NOT_FOUND
    	}// fin switch
    }
    
    /**
     * Método que, a partir del nombre de un fichero, devuelve que tipo de fichero es (html, imagen, texto plano, etc).
     * @param fichero   Nombre del fichero a comprobar
     * @return          Texto indicando tipo del fichero
     */
    private String getTipoContenido(String fichero) {
        int puntoExtension = fichero.lastIndexOf(".");                          // Calcula la posición, en el nombre del recurso, en donde empieza el . que indica la extensión del fichero
        String extension = fichero.substring(puntoExtension);                   // Devuelve la posición en donde empieza la extensión de fichero (.html, .txt, .gif, .png)
        String tipo;                                                            // Tipo de fichero. (Variable necesaria porque return impide ejecutar break dentro de switch)
        
        switch (extension) {
            case "html":                                                        // Fichero es tipo HTML
                tipo = "text/html";
            break;
            case "txt":                                                         // Fichero de texto
                tipo = "text/plain";
            break;
            case "gif":
                tipo = "image/gif";
            break;
            case "png":
                tipo = "image/png";
            break;
            default:
                tipo = "application/octet-stream";
            break;
        }
        
        return tipo;
    }

        // Método verContenido() para conocer contenido (texto, gif, imágenes, etc)
    
    /**
     * Método encargado de ejecutar cada thread creado por el servidor.
     * Sobreescribe al método run() de la clase Thread para adaptar su comportamiento a nuestro servidor.
     */
    @Override
    public void run() {
        
    }
}
