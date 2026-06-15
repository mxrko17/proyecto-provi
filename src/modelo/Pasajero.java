package modelo;

import utilidades.Nombre;
import utilidades.idPersona;
import java.io.Serializable;

public class Pasajero extends Persona implements Serializable{
    private Nombre nomContacto;
    private String fonoContacto;

    public Pasajero(idPersona idPersona, Nombre nombreCompleto, String telefono, Nombre nomContacto, String fonoContacto) {
        super(idPersona, nombreCompleto);
        this.setTelefono(telefono);
        this.nomContacto = nomContacto;
        this.fonoContacto = fonoContacto;
    }

    public Nombre getNomContacto() {
        return nomContacto;
    }

    public void setNomContacto(Nombre nom) {
        this.nomContacto = nom;
    }

    public String getFonoContacto() {
        return fonoContacto;
    }

    public void setFonoContacto(String fono) {
        this.fonoContacto = fono;
    }
}