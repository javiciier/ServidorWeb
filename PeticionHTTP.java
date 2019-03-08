/*
* REDES 2018-2019
* Práctica 1 - Iteración 1
* Javier Cancela Mato - javier.cmato@udc.es - Grupo 1.4
*/

import java.io.*;
import java.net.*;
import java.util.Date;


/**
* Clase encargada de realizar las peticiones HTTP al servidor web.
* Soporta ejecución multihilo para poder realizar varias peticiones simultaneas.
*/
public class PeticionHTTP extends Thread {
    // ATRIBUTOS
    private final Socket cliente;                                               // Cliente que realiza la petición
    private BufferedReader entrada;                                      // Información a leer en la entrada
    private OutputStream salida;                                         // Datos de salida
    private final CabeceraHTTP cabecera;
    private String lineaComandos;                                          // Comando que se solicita realizar
    enum EstadoHTTP {							// Posibles estados a recibir cuando se realiza una solicitud HTTP
    	OK("200 OK"),								// Recurso se recibió sin errores
    	NOT_FOUND("404 Not Found"),						// Recurso no se encontró o no existe
        BAD_REQUEST("400 Bad Request");
    	// recordar eliminar ; al añadir nuevos estados

    	private final String estado;

    	// CONSTRUCTORES
    	EstadoHTTP(String estado) {
    		this.estado = estado;
    	}

    	// GETTERS
    	public String getEstadoHTTP() {
    		return this.estado;
    	}
    }
    
    // CONSTRUCTORES
    /**
    * Constructor con parámetros que recibe un Socket y se lo asigna a la clase.
    @param cliente Socket que asignamos a la petición.
    */
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
     * Formato: <peticion> <recurso> <versionHTTP>
     */
    private void HEADhttp() {
    	String[] ruta = this.lineaComandos.split(" ");				// Obtiene el recurso solicitado
    	File recurso = new File("/home/fic/Escritorio/Redes/Practicas/p1/ficheros" + ruta[1]);		// Busca el recurso en el mismo directorio o subdirectorios en donde está ubicado el servidor
    	
    	if ( recurso.exists() )                                                // Si el recurso buscado existe en algún directorio
            mostrarRespuesta(EstadoHTTP.OK, recurso, false);                           // Petición correcta. Muestra la información del recurso recibido como argumento
    	else
            mostrarRespuesta(EstadoHTTP.NOT_FOUND, null, false);			// Muestra ERROR 404 porque no existe archivo. Al no existir, no le pasa como argumento ningún recurso
    }

    /**
     * Método que obtiene el cuerpo (contenido) de una peticion HTTP.
     */
    private void GEThttp() {
        String[] comandos = this.lineaComandos.split(" ");                      // Separa los elementos de la línea de comandos. Formato <COMANDO> <RUTA> <VERSION_HTTP>
        File recurso = new File("/home/fic/Escritorio/Redes/Practicas/p1/ficheros" + comandos[1]);       // Obtiene el recurso buscado a partir de la ruta del servidor y la ruta especificada.
        //PrintWriter canalSalida = new PrintWriter(this.salida, true);         // Permite mostrar información por pantalla
        
        if ( recurso.exists() ) {						// Si el recurso EXISTE, lo muestra
            mostrarRespuesta(EstadoHTTP.OK, recurso, true);
        } // fin if
        else {									// Recurso no existe -> ERROR 404											// El recurso no existe
            mostrarRespuesta(EstadoHTTP.NOT_FOUND, null, true);
        }  // fin else
    }
    
    /**
     * Método encargado de mostrar por pantalla la información asociada a la petición realizada.
     * @param estado 	Estado que debería devolver la solicitud según su situación (archivo encontrado, nombre incorrecto, etc)
     * @param recurso 	Fichero del cual obtiene la información solicitada
     * @param mostrarCuerpo     Booleano que indica si el método debe devolver el contenido del fichero
     */
    private void mostrarRespuesta(EstadoHTTP estado, File recurso, boolean mostrarCuerpo) {
    	PrintWriter canalSalida = new PrintWriter(this.salida, true);		// Establece un canal de salida para mostrar información por pantalla
    	String textoEstado;							// Texto que se mostrará según el estado de la petición
        Date fechaRespuesta;                                                    // Almacena la fecha en la que se generó la petición
    
        // LÍNEA DE ESTADO
    	switch (estado) {							// Muestra distinta información por el canal de salida según el estado de la petición recibida
            case OK:
                textoEstado = ( this.lineaComandos.split(" ")[2] + " " + estado.getEstadoHTTP() );
                canalSalida.println(textoEstado);
            break;
            case NOT_FOUND:
                textoEstado = ( this.lineaComandos.split(" ")[2] + " " + estado.getEstadoHTTP() );
                canalSalida.println(textoEstado);
                //recurso = new File("/home/fic/Escritorio/Redes/Practicas/p1/ficheros/notFound.html");
            break;  // fin NOT_FOUND
            case BAD_REQUEST:
            	textoEstado = ( this.lineaComandos.split(" ")[2] + " " + estado.getEstadoHTTP() );
                canalSalida.println(textoEstado);
                //recurso = new File("/home/fic/Escritorio/Redes/Practicas/p1/ficheros/BAD_REQUEST.html");
                /* canalSalida.println();
                mostrarCuerpoGET(recurso);                                      // Muestra recurso BAD REQUEST */
            break;
            default:
                canalSalida.println("Error: respuesta no válida");
    	} // fin switch
        
        // LÍNEAS DE CABECERA
        fechaRespuesta = new Date();
        canalSalida.println("Date: " + fechaRespuesta);
        canalSalida.println("Server: " + ServidorHTTP.nombreServidor);
        if ( recurso != null ) {                                        // Si recibe un recurso muestra su información
            Date ultimaModificacion = new Date(recurso.lastModified());
            canalSalida.println("Last-Modified: " + ultimaModificacion);
            canalSalida.println("Content-Length: " + recurso.length());
            canalSalida.println("Content-Type: " + getTipoContenido(recurso.getName()));
            canalSalida.println();
        }
        
        // CUERPO DE LA PETICIÓN
        if ( mostrarCuerpo && (recurso != null) ) {
            mostrarCuerpoGET(recurso);
        }
    }
    
    /**
    * Método que muestra por pantalla el contenido de un fichero.
    * Este método se emplea en GEThttp() para mostrar el contenido de un fichero HTML.
    * @param recurso    Fichero a partir del cual se obtiene el contenido a devolver.
    */
    private void mostrarCuerpoGET(File recurso) {
        FileInputStream origen = null;                                          // Fuente de donde se obtienen los datos
        byte[] contenido = new byte[1024];                                      // Array en donde se guarda el contenido leido (byte porque puede ser de cualquier tipo)
        int tamanho;
        
        try {
            origen = new FileInputStream(recurso);                              // Abre el fichero origen (si no lo encuentra genera FileNotFoundException)
            while ( (tamanho = origen.read(contenido)) != -1 ) {
                this.salida.write(contenido, 0, tamanho);
            }
            //System.out.println();
        }
        catch (FileNotFoundException FNFexc) {
            System.err.println("Error: no se encuentra fichero " +
                    this.lineaComandos.split(" ")[1].substring(1));
            //System.err.println("Ruta a fichero recibida: " + recurso.getPath());
        }
        catch (IOException IOexc) {
            System.err.println("Error: " + IOexc.getMessage());
        }
        finally {
            if (origen != null) {
                try {
                    origen.close();
                }
                catch (IOException IOexc) {
                    System.err.println("Error: no se pueden liberar recursos.");
                }
            } // fin if
        }
        
    }
    
    /**
     * Método que, a partir del nombre de un fichero, devuelve que tipo de fichero es (html, imagen, texto plano, etc).
     * @param fichero   Nombre del fichero a comprobar
     * @return          Texto indicando tipo del fichero
     */
    private String getTipoContenido(String fichero) {
        int puntoExtension = fichero.lastIndexOf(".");                          // Calcula la posición, en el nombre del recurso, en donde empieza el . que indica la extensión del fichero
        String extension = fichero.substring(puntoExtension + 1);                // Devuelve la posición en donde empieza la extensión de fichero, sin el punto (html, txt, .gif, png)
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
    
    /**
     * Método encargado de ejecutar cada thread creado por el servidor.
     * Sobreescribe al método run() de la clase Thread para adaptar su comportamiento a nuestro servidor.
     * Este método está destinado a ejecutarse desde el método main() del ServidorHTTP
     */
    @Override
    public void run() {
        String peticion;
        try {
            this.lineaComandos = this.entrada.readLine();                       // Obtiene la petición realizada y la almacena (posible IOException)
            if ( this.lineaComandos != null ) {                                   // Si recibió la petición correctamente
                System.out.println(this.lineaComandos);                         // Muestra la petición solicitada por pantalla
                peticion = this.lineaComandos.split(" ")[0];                    // Obtiene qué petición realizar (GET, HEAD, etc)
                switch (peticion) {
                    case "HEAD":
                        HEADhttp();
                    break;
                    case "GET":
                        GEThttp();
                    break;
                    default:                                                    // Recibe una petición que no se comprende (mal escrita, no existe, etc)
                        mostrarRespuesta(EstadoHTTP.BAD_REQUEST, null, false);
                } // fin switch
            } // fin if
        } // fin try
        catch (SocketException Sexc) {
            System.err.println("Socket error: " + Sexc.getMessage());
        }
        catch (IOException IOexc) {
            System.err.println("Runtime error: " + IOexc.getMessage());
        }
        finally {                                                               // Cerramos los canales de entrada y salida y capturamos sus posibles excepciones
            try {
                this.entrada.close();
                this.salida.close();
                this.cliente.close();
            }
            catch (IOException IOexc) {
                System.err.println("Error: no se pueden liberar recursos.");
                throw new RuntimeException(IOexc);
            }
        } // fin finalñy
    }
}
