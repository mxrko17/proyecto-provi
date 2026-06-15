package modelo;

import java.io.Serializable;

public abstract class Pago implements Serializable{
    private int monto;

    public Pago(int monto) {
        this.monto = monto;
    }

    public int getMonto() {
        return monto;
    }
}