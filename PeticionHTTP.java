/*
* REDES 2018-2019
* Práctica 1 - Iteración 2
* Javier Cancela Mato - javier.cmato@udc.es - Grupo 1.4
*/

import java.io.*;
import java.net.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
* Clase encargada de realizar las peticiones HTTP al servidor web.
* Soporta ejecución multihilo para poder realizar varias peticiones simultaneas.
*/
public class PeticionHTTP extends Thread {
    // ATRIBUTOS
    private final Socket cliente;                                               // Cliente que realiza la petición
    private BufferedReader entrada;                                             // Información a leer en la entrada
    private OutputStream salida;                                                // Datos de salida
    private final CabeceraHTTP cabecera;
    private String lineaComandos;                                               // Comando que se solicita realizar
    public final int tiempoEspera = 60;
    enum EstadoHTTP {				// Posibles estados a recibir cuando se realiza una solicitud HTTP
    	OK("200 OK"),								// Recurso se recibió sin errores
    	NOT_FOUND("404 Not Found"),						// Recurso no se encontró o no existe
        BAD_REQUEST("400 Bad Request"),
        FORBIDDEN("403 Forbidden"),
        NOT_MODIFIED("304 Not Modified");
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
            this.cliente.setSoTimeout(this.tiempoEspera * 1000);                // Establece tiempo de espera del socket a 1 minuto
            // Obtenemos la entrada. API Java recomienda usar clase wrapper BufferedReader para InputStreamReader
            this.entrada = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
            this.salida = cliente.getOutputStream();                            // Muestra la información del socket (posible IOException si socket no contiene información o no existe)
        }
        catch (SocketException Sexc) {
            System.err.println("Socket Error: " + Sexc.getMessage());
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
        Date fechaModServidor = new Date(recurso.lastModified());
        Date fechaModCliente;
        
        if ( recurso.exists() ) {						// Si el recurso EXISTE, lo procesa
            if ( recurso.isDirectory() ) {                                      // SI el recurso solicitado es un directorio
                if ( !comandos[1].endsWith("/") ) {                             // SI el direcotorio no acaba en "/", se le añade
                    comandos[1] += "/";
                }
                // Si recibe un directorio, muestra el archivo por defecto a mostrar (index.html)
                File indice = new File("/home/fic/Escritorio/Redes/Practicas/p1/ficheros" + comandos[1] + ServidorHTTP.ConfiguracionDefecto.DIRECTORY_INDEX.getValor());
                if ( indice.exists() ) {                                        // Si el fichero por defecto existe, lo muestra
                    mostrarRespuesta(EstadoHTTP.OK, indice, true);
                }
                else {                                                          // Si no existe dicho fichero, muestra una lista con todos lso ficheros que existen y enlaces para acceder a ellos
                    if ( ServidorHTTP.permiso ) {                               // Comprobamos si el servidor da permiso para mostrar todos los ficheros que contiene
                        String enlace = "http://" + this.cabecera.getDispositivo() + comandos[1];
                        mostrarRecursosDirectorio(recurso.list(), enlace);
                    }
                    else {
                        mostrarRespuesta(EstadoHTTP.FORBIDDEN, null, true);
                    }
                }
            } // fin if
            else {
                fechaModCliente = this.cabecera.getFecha();
                if (fechaModCliente == null || fechaModServidor.after(fechaModCliente)) {
                    mostrarRespuesta(EstadoHTTP.OK, recurso, true);
                }
                else {
                    mostrarRespuesta(EstadoHTTP.NOT_MODIFIED, null, false);
                }
            }
        } // fin if
        else {									// Recurso no existe -> ERROR 404											// El recurso no existe
            mostrarRespuesta(EstadoHTTP.NOT_FOUND, null, false);
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
    	String textoEstado = "";                                              // Texto que se mostrará según el estado de la petición
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
                recurso = new File("/home/fic/Escritorio/Redes/Practicas/p1/ficheros/NOT_FOUND.html");
            break;
            case BAD_REQUEST:
            	textoEstado = ( this.lineaComandos.split(" ")[2] + " " + estado.getEstadoHTTP() );
                canalSalida.println(textoEstado);
                recurso = new File("/home/fic/Escritorio/Redes/Practicas/p1/ficheros/BAD_REQUEST.html");
                /* canalSalida.println();
                mostrarCuerpoGET(recurso);                                      // Muestra recurso BAD REQUEST */
            break;
            case FORBIDDEN:
                textoEstado = ( this.lineaComandos.split(" ")[2] + " " + estado.getEstadoHTTP() );
                canalSalida.println(textoEstado);
                recurso = new File("/home/fic/Escritorio/Redes/Practicas/p1/ficheros/FORBIDDEN.html");
            break;
            case NOT_MODIFIED:
                textoEstado = ( this.lineaComandos.split(" ")[2] + " " + estado.getEstadoHTTP() );
                canalSalida.println(textoEstado);
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
        if ( mostrarCuerpo ) {
            mostrarCuerpoGET(recurso);
        }
        registrarActividad(textoEstado, recurso);
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
            this.salida.write('\n');
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
     * Método que escribe en un fichero de texto la actividad realizada por el servidor.
     * Si el fichero no existe, se genera uno nuevo y se escribe la actividad.
     * En caso de generarse algún error, genera un nuevo fichero para registrar explícitamente los errores.
     * @param actividad     Texto a guardar en el registro
     * @param recurso       Recurso solicitado
     */
    private void registrarActividad(String actividad, File recurso) {
        File registro = null;                                                          // Fichero en el que escribir la actividad
        PrintWriter canalSalida;
        String[] respuesta = actividad.split(" ");                              // Array que almacena la respuesta enviada por el servidor
        String codigoRespuesta = respuesta[1];                                  // Cadena que contiene el código de respuesta de la petición HTTP
        FileOutputStream ficheroRegistro;
        
        try {
            switch ( codigoRespuesta.charAt(0) ) {
                case '2':
                case '3':                                                       // Las peticiones fueron correctas
                    registro = new File(ServidorHTTP.registroAccesos);          // Obtiene el fichero de registro
                    canalSalida = new PrintWriter(new FileOutputStream(registro, true), true);   // Establece el registro de accesos como canal de salida (en dónde escribir la información)
                    // Escribe la información de la solicitud en el registro de accesos
                    canalSalida.println("Peticion recibida: " + this.lineaComandos);
                    canalSalida.println("IP cliente: " + this.cliente.getInetAddress().toString().substring(1));
                    canalSalida.println("Fecha y hora de petición: " + new Date());
                    canalSalida.println("Código de estado: " + codigoRespuesta);
                    if ( recurso != null )                                      // Si recibe algún recurso, muestra su tamaño
                        canalSalida.println("Tamaño: " + recurso.length() + " (Bytes)");
                    canalSalida.println("---------------------------------------------\n");    // Separa un registro de otro
                break;
                case '4':                                                       // Se generó un error gestionando la petición
                    registro = new File(ServidorHTTP.registroErrores);          // Obtiene el fichero donde almacena los errores
                    canalSalida = new PrintWriter(new FileOutputStream(registro, true), true);      // Establece el registro de errores como fichero en el que escribir
                    // Escribe la información en el registro de errores
                    canalSalida.println("Petición errónea: " + this.lineaComandos);
                    canalSalida.println("IP cliente: " + this.cliente.getInetAddress().toString().substring(1));
                    canalSalida.println("Fecha y hora del error: " + new Date());
                    canalSalida.println("Mensaje de error: " + actividad);
                    canalSalida.println("---------------------------------------------\n");    // Separa un registro de otro
                break;
            } // fin switch
        }
        catch (FileNotFoundException FNFexc) {
            System.err.println("Error: no existe el registro " + registro.getName());
        }
    }
    
    /**
    * Método que lee todas las cabeceras de la respuesta del servidor.
    * Su finalidad principal es detectar si existe la cabecera "IF-MODIFIED-SINCE".
    * @param arrayCabeceras     Array que contiene las cabeceras de la respuesta generada
    */
    private void reconocerCabeceras(ArrayList<String> arrayCabeceras) {
        SimpleDateFormat fecha;                                                 // Fecha de IF-MODIFIED-SINCE (si existiese)
        String[] camposCabecera;                                                // Array que almacena el nombre y el contenido de cada cabecera
        
        for(String cabecera: arrayCabeceras) {                                  // Recorre todas las líneas de cabecera de la respuesta
            camposCabecera = cabecera.split(": ");
            switch (camposCabecera[0]) {                                  // Según el nombre de la cabecera
                case "If-Modified-Since":
                    try {
                        fecha = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", new Locale("english"));
                        this.cabecera.setFecha(fecha.parse(camposCabecera[1]));
                    }
                    catch (ParseException Pexc) {
                        System.err.println("Error: " + Pexc.getMessage());
                    }
                break;
                case "Host":
                    this.cabecera.setDispositivo(camposCabecera[1]);
                break;
            }
        } // fin for
    }
    
    /**
    * Muestra los ficheros dentro de un directorio en caso de recibir como recurso un directorio.
    * @param ficheros       Lista de ficheros dentro del directorio
    * @param directorio     Nombre del directorio
    */
    private void mostrarRecursosDirectorio(String[] ficheros, String enlace) {
        String links = "";
        links += "<html>"
            + "<head>Ficheros en directorio</head>"
                + "<body>"
                + "<ul>";
        // Enlaza a cada fichero existente
        for (String fichero: ficheros) {
            links += "<li>"
                + "<a href=\"" + enlace + fichero + "\">" + fichero + "</a>"
                + "</li>";
        }
        links += "</ul>"
                + "</body>"
                + "</html>";
        
        try {
            mostrarRespuesta(EstadoHTTP.OK, null, false);
            this.salida.write(links.getBytes());
        }
        catch (IOException IOexc) {
            System.err.println("Error: " + IOexc.getLocalizedMessage());
        }
    }
    /**
     * Método encargado de ejecutar cada thread creado por el servidor.
     * Sobreescribe al método run() de la clase Thread para adaptar su comportamiento a nuestro servidor.
     * Este método está destinado a ejecutarse desde el método main() del ServidorHTTP
     */
    @Override
    public void run() {
        boolean mantenerConexion = true;                                        // Indica si el socket puede realizar varias peticiónes en caso de protocolo HTTP/1.1
        boolean responder = true;                                               // Indica si se debe enviar un mensaje de vuelta o se produjo algún error (por defecto el servidor siempre responderá)
        ArrayList<String> cabeceras = new ArrayList<>();                        // ArrayList que almacenará las lineas de las cabeceras de la respuesta
        
        try {
            this.cliente.setSoTimeout(this.tiempoEspera*1000);                  // Establece el tiempo de espera del socket
            while (mantenerConexion) {
                String[] peticion = null;                                              // Array que almacena las partes de la petición recibida
                this.lineaComandos = this.entrada.readLine();                   // Obtiene la petición realizada por el cliente
                if (this.lineaComandos != null) {
                    peticion = this.lineaComandos.split(" ");                   // Obtiene cada parte de la petición recibida
                }    
                /* else
                    continue*/
                if (peticion.length != 3) {                                     // Si recibe una petición con campos de más o de menos, muestra error
                    mostrarRespuesta(EstadoHTTP.BAD_REQUEST, null, true);
                }
                else {                                                          // La petición ha sido correcta
                    String lineaCabecera;
                    do {                                                        // Lee las cabeceras de las respuestas y las almacena en un arraylist
                        lineaCabecera = this.entrada.readLine();
                        cabeceras.add(lineaCabecera);
                        System.out.println(lineaCabecera);
                    } while ( !lineaCabecera.isEmpty() );
                    reconocerCabeceras(cabeceras);
                    
                    System.out.println(this.lineaComandos);                     // Muestra la petición recibida
                    // MUESTRA LAS CABECERAS
                    for (String cabecera : cabeceras) {
                        System.out.println(cabecera);
                    }
                    // LÍNEA EN BLANCO
                    System.out.println();
                    
                    switch(peticion[2]) {                                       // Comprueba el protocolo HTTP empleado
                        case "HTTP/1.0":                                        // Conexión no es persistente
                            mantenerConexion = false;
                        break;
                        case "HTTP/1.1":                                        // Conexión debe mantenerse
                            mantenerConexion = true;
                        break;
                        default:                                                // Formato inválido
                            System.err.println("Formato " + peticion[2] + " no válido");
                            mostrarRespuesta(EstadoHTTP.BAD_REQUEST, null, true);
                            mantenerConexion = false;
                            responder = false;
                        break;
                    }
                    
                    if (responder) {                                            // Si el servidor debe enviar una respuesta
                        switch(peticion[0]) {                                   // Selecciona la respuesta que debe devolver
                            case "HEAD":
                                HEADhttp();
                            break;
                            case "GET":
                                GEThttp();
                            break;
                            case "TRACE": case "POST": case "DELETE": case "PUT":   // Estos comandos no están implementadas en este servidor
                                System.err.println("FUNCIÓN " + peticion[0] + " NO IMPLEMENTADA");
                                mostrarRespuesta(EstadoHTTP.BAD_REQUEST, null, false);
                            break;
                            default:                                            // Recibe un comando extraño
                                mostrarRespuesta(EstadoHTTP.BAD_REQUEST, null, false);
                            break;
                        }
                    } // fin if(responder)                   
                } // fin else 
            } // fin while
        }
        catch ( SocketException Sexc ) {
            System.err.println("FALLO DE SOCKET: 60 segundos sin recibir peticiones");
        }
        catch ( IOException IOexc ) {
            System.err.println("RUNTIME ERROR: " + IOexc.getMessage());
        }
        finally {                                                               // Cerramos los canales de entrada y salida y capturamos sus posibles excepciones
            try {
                this.entrada.close();
                this.salida.close();
                this.cliente.close();
                cabeceras.clear();
            }
            catch (IOException IOexc) {
                System.err.println("Error: no se pueden liberar recursos.");
                throw new RuntimeException(IOexc);
            }
        } // fin finally
    }
}
