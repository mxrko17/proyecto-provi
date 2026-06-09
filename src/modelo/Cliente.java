package modelo;
import utilidades.Nombre;
import utilidades.idPersona;
import java.util.ArrayList;

public class Cliente extends Persona {
    private String email;
    private ArrayList<Venta> ventas;

    public Cliente(idPersona id, Nombre nom, String email) {
        super(id, nom); //persona
        this.email = email;
        this.ventas = new ArrayList<>();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void addVenta(Venta venta) {
        if (!ventas.contains(venta)) {
            ventas.add(venta);
        }
    }

    public Venta[] getVentas() {
        return ventas.toArray(new Venta[0]);
    }
}