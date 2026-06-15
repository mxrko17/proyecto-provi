package modelo;

import utilidades.Direccion;
import utilidades.Nombre;
import utilidades.idPersona;
import java.io.Serializable;

public class Conductor extends Tripulante implements Serializable{

    public Conductor(idPersona id, Nombre nom, Direccion dir) {
        super(id, nom, dir);
    }

    @Override
    public void addViaje(Viaje viaje) {
        super.addViaje(viaje);
    }

    @Override
    public int getNroViajes() {
        return super.getNroViajes();
    }
}