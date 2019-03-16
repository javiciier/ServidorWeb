/*
* REDES 2018-2019
* Práctica 1 - Iteración 2
* Javier Cancela Mato - javier.cmato@udc.es - Grupo 1.4
*/

import java.util.Date;

/**
* Clase auxiliar que gestiona la información de las cabeceras HTTP.
* Es empleada en clase PeticionHTTP.
*/
public class CabeceraHTTP {
    // ATRIBUTOS
    private Date fecha;                                                         // Almacena la información temporal
    private String dispositivo;                                                 // Información sobre el dispositivo
    
    // GETTERS
    public Date getFecha() {
        return this.fecha;
    }
    
    public String getDispositivo() {
        return this.dispositivo;
    }
    
    // SETTERS
    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }
    
    public void setDispositivo(String dispositivo) {
        this.dispositivo = dispositivo;
    }
}
