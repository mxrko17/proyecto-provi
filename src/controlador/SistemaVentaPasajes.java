package controlador;

import excepciones.SVPException;
import modelo.*;
import utilidades.*;
import java.sql.Time;
import java.util.Date;
import java.util.Optional;
import java.util.ArrayList;

public class SistemaVentaPasajes {
    private static SistemaVentaPasajes instance;

    private ArrayList<Cliente> clientes;
    private ArrayList<Pasajero> pasajeros;
    private ArrayList<Viaje> viajes;
    private ArrayList<Venta> ventas;

    private SistemaVentaPasajes() {
        clientes = new ArrayList<>();
        pasajeros = new ArrayList<>();
        viajes = new ArrayList<>();
        ventas = new ArrayList<>();
    }

    public static SistemaVentaPasajes getInstance() {
        if (instance == null) {
            instance = new SistemaVentaPasajes();
        }
        return instance;
    }

    public void createCliente(idPersona id, Nombre nom, String fono, String email) {
        if (findCliente(id).isPresent()) {
            throw new SVPException("Ya existe cliente con el id indicado");
        }
        Cliente nuevo = new Cliente(id, nom, email);
        nuevo.setTelefono(fono);
        clientes.add(nuevo);
    }

    public void createPasajero(idPersona id, Nombre nom, String fono, Nombre nomContacto, String fonoContacto) {
        if (findPasajero(id).isPresent()) {
            throw new SVPException("Ya existe pasajero con el id indicado");
        }
        Pasajero p = new Pasajero(id, nom, fono, nomContacto, fonoContacto);
        pasajeros.add(p);
    }

    public void createViaje(Date fecha, Time hora, int precio, int duracion, String patBus, idPersona[] idTripulantes, String[] nomComunas) {
        ControladorEmpresas ctrl = ControladorEmpresas.getInstance();

        if (findViaje(fecha, hora, patBus).isPresent()) {
            throw new SVPException("Ya existe viaje con fecha, hora y patente de bus indicados");
        }

        Optional<Bus> busOpt = ctrl.findBus(patBus);
        if (!busOpt.isPresent()) {
            throw new SVPException("No existe bus con la patente indicada");
        }
        Bus bus = busOpt.get();
        Rut rutEmpresa = bus.getEmpresa().getRut();

        Optional<Auxiliar> auxOpt = ctrl.findAuxiliar(idTripulantes[0], rutEmpresa);
        if (!auxOpt.isPresent()) {
            throw new SVPException("No existe auxiliar con el id indicado en la empresa con el rut indicado");
        }

        Optional<Conductor> condOpt = ctrl.findConductor(idTripulantes[1], rutEmpresa);
        if (!condOpt.isPresent()) {
            throw new SVPException("No existe conductor con el id indicado en la empresa con el rut indicado");
        }

        Optional<Terminal> termSalidaOpt = ctrl.findTerminalPorComuna(nomComunas[0]);
        if (!termSalidaOpt.isPresent()) {
            throw new SVPException("No existe terminal de salida en la comuna indicada");
        }

        Optional<Terminal> termLlegadaOpt = ctrl.findTerminalPorComuna(nomComunas[1]);
        if (!termLlegadaOpt.isPresent()) {
            throw new SVPException("No existe terminal de llegada en la comuna indicada");
        }

        Conductor[] conductores;
        if (idTripulantes.length > 2 && idTripulantes[2] != null) {
            Optional<Conductor> cond2Opt = ctrl.findConductor(idTripulantes[2], rutEmpresa);
            if (cond2Opt.isPresent()) {
                conductores = new Conductor[]{condOpt.get(), cond2Opt.get()};
            } else {
                conductores = new Conductor[]{condOpt.get()};
            }
        } else {
            conductores = new Conductor[]{condOpt.get()};
        }

        Viaje nuevoViaje = new Viaje(fecha, hora, precio, duracion, bus, auxOpt.get(), conductores, termSalidaOpt.get(), termLlegadaOpt.get());
        viajes.add(nuevoViaje);
    }

    public void iniciaVenta(String idDoc, TipoDocumento tipo, Date fechaViaje, String comSalida, String comLlegada, idPersona idCliente, int nroPasajes) {
        if (findVenta(idDoc, tipo).isPresent()) {
            throw new SVPException("Ya existe venta con el id y tipo de documento indicados");
        }
        Optional<Cliente> cliente = findCliente(idCliente);
        if (!cliente.isPresent()) {
            throw new SVPException("No existe cliente con id indicado");
        }

        String[][] horarios = getHorariosDisponibles(fechaViaje, comSalida, comLlegada, nroPasajes);
        if (horarios.length == 0) {
            throw new SVPException("No existen viajes disponibles en la fecha y con terminales en las comunas de salida y llegada indicados");
        }

        Venta nueva = new Venta(idDoc, tipo, new Date(), cliente.get());
        ventas.add(nueva);
    }

    public String[][] getHorariosDisponibles(Date fechaViaje, String comunaSalida, String comunaLlegada, int nroPasajes) {
        ArrayList<String[]> disponibles = new ArrayList<>();
        java.text.SimpleDateFormat dfHora = new java.text.SimpleDateFormat("HH:mm");

        for (Viaje v : viajes) {
            boolean matchFecha = v.getFecha().equals(fechaViaje);
            boolean matchSalida = v.getTerminalSalida() != null &&
                    v.getTerminalSalida().getDireccion().getComuna().equalsIgnoreCase(comunaSalida);
            boolean matchLlegada = v.getTerminalLlegada() != null &&
                    v.getTerminalLlegada().getDireccion().getComuna().equalsIgnoreCase(comunaLlegada);

            if (matchFecha && matchSalida && matchLlegada && v.existeDisponibilidad(nroPasajes)) {
                disponibles.add(new String[]{
                        v.getBus().getPatente(), dfHora.format(v.getHora()),
                        String.valueOf(v.getPrecio()), String.valueOf(v.getNroAsientosDisponibles())
                });
            }
        }
        return disponibles.toArray(new String[0][0]);
    }

    public String[] listAsientosDeViaje(Date fecha, Time hora, String patBus) {
        Optional<Viaje> v = findViaje(fecha, hora, patBus);
        return v.isPresent() ? v.get().getAsientos() : new String[0];
    }

    public Optional<String> getNombrePasajero(idPersona idPasajero) {
        Optional<Pasajero> p = findPasajero(idPasajero);
        return p.map(pasajero -> pasajero.getNombreCompleto().toString());
    }

    public Optional<Integer> getMontoVenta(String idDocumento, TipoDocumento tipo) {
        Optional<Venta> v = findVenta(idDocumento, tipo);
        return v.map(Venta::getMonto);
    }

    public void vendePasaje(String idDoc, TipoDocumento tipo, Date fechaViaje, Time hora, String patBus, int asiento, idPersona idPasajero) {
        Optional<Venta> venta = findVenta(idDoc, tipo);
        if (!venta.isPresent()) {
            throw new SVPException("No existe venta con el id y tipo de documento indicados");
        }
        Optional<Pasajero> pasajero = findPasajero(idPasajero);
        if (!pasajero.isPresent()) {
            throw new SVPException("No existe pasajero con el id indicado");
        }
        Optional<Viaje> viaje = findViaje(fechaViaje, hora, patBus);
        if (!viaje.isPresent()) {
            throw new SVPException("No existe viaje con la fecha, hora y patente de bus indicados");
        }
        venta.get().createPasaje(asiento, viaje.get(), pasajero.get());
    }

    public void pagaVenta(String idDocumento, TipoDocumento tipo) {
        Optional<Venta> venta = findVenta(idDocumento, tipo);
        if (!venta.isPresent()) throw new SVPException("No existe venta con el id y tipo de documento indicados");
        if (!venta.get().pagaMonto()) throw new SVPException("La venta ya fue pagada");
    }

    public void pagaVenta(String idDocumento, TipoDocumento tipo, long nroTarjeta) {
        Optional<Venta> venta = findVenta(idDocumento, tipo);
        if (!venta.isPresent()) throw new SVPException("No existe venta con el id y tipo de documento indicados");
        if (!venta.get().pagaMonto(nroTarjeta)) throw new SVPException("La venta ya fue pagada");
    }

    public String[][] listVentas() {
        String[][] datos = new String[ventas.size()][4];
        java.text.SimpleDateFormat dfFecha = new java.text.SimpleDateFormat("dd/MM/yyyy");
        for (int i = 0; i < ventas.size(); i++) {
            Venta v = ventas.get(i);
            datos[i][0] = v.getIdDocumento();
            datos[i][1] = v.getTipo().toString().toLowerCase();
            datos[i][2] = dfFecha.format(v.getFecha());
            datos[i][3] = String.format("%,d", v.getMontoPagado()).replace(',', '.');
        }
        return datos;
    }

    public String[][] listViajes() {
        String[][] datos = new String[viajes.size()][8];
        java.text.SimpleDateFormat dfFecha = new java.text.SimpleDateFormat("dd/MM/yyyy");
        java.text.SimpleDateFormat dfHora = new java.text.SimpleDateFormat("HH:mm");

        for (int i = 0; i < viajes.size(); i++) {
            Viaje v = viajes.get(i);
            datos[i][0] = (v.getFecha() != null) ? dfFecha.format(v.getFecha()) : "N/A";
            datos[i][1] = (v.getHora() != null) ? dfHora.format(v.getHora()) : "N/A";

            if (v.getFechaHoraTermino() != null) {
                datos[i][2] = v.getFechaHoraTermino().toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            } else {
                datos[i][2] = "N/A";
            }

            datos[i][3] = String.format("$%,d", v.getPrecio()).replace(',', '.');
            datos[i][4] = String.valueOf(v.getNroAsientosDisponibles());
            datos[i][5] = (v.getBus() != null) ? v.getBus().getPatente() : "N/A";
            datos[i][6] = (v.getTerminalSalida() != null) ? v.getTerminalSalida().getDireccion().getComuna().toUpperCase() : "N/A";
            datos[i][7] = (v.getTerminalLlegada() != null) ? v.getTerminalLlegada().getDireccion().getComuna().toUpperCase() : "N/A";
        }
        return datos;
    }

    public String[][] listPasajerosViaje(Date fecha, Time hora, String patenteBus) {
        Optional<Viaje> v = findViaje(fecha, hora, patenteBus);
        if (!v.isPresent()) {
            throw new SVPException("No existe viaje con la fecha, hora y patente de bus indicados");
        }
        return v.get().getListaPasajeros();
    }

    private Optional<Cliente> findCliente(idPersona id) {
        for (Cliente c : clientes) {
            if (c.getIdPersona().equals(id)) return Optional.of(c);
        }
        return Optional.empty();
    }

    private Optional<Venta> findVenta(String idDocumento, TipoDocumento tipoDocumento) {
        for (Venta v : ventas) {
            if (v.getIdDocumento().equals(idDocumento) && v.getTipo().equals(tipoDocumento)) return Optional.of(v);
        }
        return Optional.empty();
    }

    private Optional<Viaje> findViaje(Date fecha, Time hora, String patenteBus) {
        for (Viaje v : viajes) {
            if (v.getFecha().equals(fecha) && v.getHora().equals(hora) && v.getBus().getPatente().equals(patenteBus)) return Optional.of(v);
        }
        return Optional.empty();
    }

    private Optional<Pasajero> findPasajero(idPersona idPersona) {
        for (Pasajero p : pasajeros) {
            if (p.getIdPersona().equals(idPersona)) return Optional.of(p);
        }
        return Optional.empty();
    }
}