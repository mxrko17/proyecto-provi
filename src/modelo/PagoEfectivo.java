package modelo;

import java.io.Serializable;

public class PagoEfectivo extends Pago implements Serializable{
    public PagoEfectivo(int monto) {
        super(monto);
    }
}