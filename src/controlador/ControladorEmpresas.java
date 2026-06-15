package controlador;

import excepciones.SVPException;
import modelo.*;
import utilidades.*;
import java.util.Date;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Arrays;

public class ControladorEmpresas implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private static ControladorEmpresas instance;
    private ArrayList<Empresa> empresas;
    private ArrayList<Terminal> terminales;

    private ControladorEmpresas() {
        empresas = new ArrayList<>();
        terminales = new ArrayList<>();
    }

    public static ControladorEmpresas getInstance() {
        if (instance == null) {
            instance = new ControladorEmpresas();
        }
        return instance;
    }

    public static void setInstanciaPersistente(ControladorEmpresas nuevaInstancia) {
        instance = nuevaInstancia;
    }

    public void setDatosIniciales(Object[] objetos) {
        this.empresas.clear();
        this.terminales.clear();

        Arrays.stream(objetos)
                .filter(Empresa.class::isInstance)
                .map(Empresa.class::cast)
                .forEach(empresas::add);

        Arrays.stream(objetos)
                .filter(Terminal.class::isInstance)
                .map(Terminal.class::cast)
                .forEach(terminales::add);
    }

    public void createEmpresa(Rut rut, String nombre, String url) {
        if (findEmpresa(rut).isPresent()) {
            throw new SVPException("Ya existe empresa con el rut indicado");
        }
        Empresa e = new Empresa(rut, nombre);
        e.setUrl(url);
        empresas.add(e);
    }

    public void createBus(String pat, String marca, String modelo, int nroAsientos, Rut rutEmp) {
        Optional<Empresa> emp = findEmpresa(rutEmp);
        if (!emp.isPresent()) {
            throw new SVPException("No existe empresa con el rut indicado");
        }
        if (findBus(pat).isPresent()) {
            throw new SVPException("Ya existe bus con la patente indicada");
        }
        Bus b = new Bus(pat, nroAsientos, emp.get());
        b.setMarca(marca);
        b.setModelo(modelo);
    }

    public void createTerminal(String nombre, Direccion direccion) {
        if (findTerminal(nombre).isPresent()) {
            throw new SVPException("Ya existe terminal con el nombre indicado");
        }
        if (findTerminalPorComuna(direccion.getComuna()).isPresent()) {
            throw new SVPException("Ya existe terminal en la comuna indicada");
        }
        terminales.add(new Terminal(nombre, direccion));
    }

    public void hireConductorForEmpresa(Rut rutEmp, idPersona id, Nombre nom, Direccion dir) {
        Optional<Empresa> emp = findEmpresa(rutEmp);
        if (!emp.isPresent()) {
            throw new SVPException("No existe empresa con el rut indicado");
        }
        if (!emp.get().addConductor(id, nom, dir)) {
            throw new SVPException("Ya está contratado conductor/auxiliar con el id dado en la empresa señalada");
        }
    }

    public void hireAuxiliarForEmpresa(Rut rutEmp, idPersona id, Nombre nom, Direccion dir) {
        Optional<Empresa> emp = findEmpresa(rutEmp);
        if (!emp.isPresent()) {
            throw new SVPException("No existe empresa con el rut indicado");
        }
        if (!emp.get().addAuxiliar(id, nom, dir)) {
            throw new SVPException("Ya está contratado auxiliar/conductor con el id dado en la empresa señalada");
        }
    }

    public String[][] listEmpresas() {
        String[][] list = new String[empresas.size()][6];
        for (int i = 0; i < empresas.size(); i++) {
            Empresa e = empresas.get(i);
            String rutFormateado = String.format("%,d", e.getRut().getNumero()).replace(',', '.') + "-" + e.getRut().getDv();
            list[i][0] = rutFormateado;
            list[i][1] = e.getNombre();
            list[i][2] = e.getUrl();
            list[i][3] = String.valueOf(e.getTripulantes().length);
            list[i][4] = String.valueOf(e.getBuses().length);
            list[i][5] = String.valueOf(e.getVentas().length);
        }
        return list;
    }

    public String[][] listLlegadasSalidasTerminal(String nombre, Date fecha) {
        Optional<Terminal> t = findTerminal(nombre);
        if (!t.isPresent()) {
            throw new SVPException("No existe terminal con el nombre indicado");
        }

        ArrayList<String[]> resultado = new ArrayList<>();
        java.text.SimpleDateFormat fmtHoraSalida = new java.text.SimpleDateFormat("HH:mm");
        java.time.format.DateTimeFormatter fmtHoraLlegada = java.time.format.DateTimeFormatter.ofPattern("HH:mm");

        Arrays.stream(t.get().getSalidas())
                .filter(v -> v.getFecha().equals(fecha))
                .forEach(v -> resultado.add(new String[]{
                        "Salida", fmtHoraSalida.format(v.getHora()), v.getBus().getPatente(),
                        v.getBus().getEmpresa().getNombre(), String.valueOf(v.getBus().getNroAsientos() - v.getNroAsientosDisponibles())
                }));

        Arrays.stream(t.get().getLlegadas())
                .filter(v -> v.getFecha().equals(fecha))
                .forEach(v -> resultado.add(new String[]{
                        "Llegada", v.getFechaHoraTermino().toLocalTime().format(fmtHoraLlegada), v.getBus().getPatente(),
                        v.getBus().getEmpresa().getNombre(), String.valueOf(v.getBus().getNroAsientos() - v.getNroAsientosDisponibles())
                }));

        return resultado.toArray(new String[0][0]);
    }

    public String[][] listVentasEmpresa(Rut rut) {
        Optional<Empresa> emp = findEmpresa(rut);
        if (!emp.isPresent()) {
            throw new SVPException("No existe empresa con el rut indicado");
        }

        Venta[] ventas = emp.get().getVentas();
        String[][] result = new String[ventas.length][4];
        java.text.SimpleDateFormat fmtFecha = new java.text.SimpleDateFormat("dd/MM/yyyy");

        for (int i = 0; i < ventas.length; i++) {
            result[i][0] = fmtFecha.format(ventas[i].getFecha());
            result[i][1] = ventas[i].getTipo().toString().toLowerCase();
            result[i][2] = String.format("%,d", ventas[i].getMontoPagado()).replace(',', '.');
            result[i][3] = ventas[i].getTipoPago() != null ? ventas[i].getTipoPago() : "Pendiente";
        }
        return result;
    }

    protected Optional<Empresa> findEmpresa(Rut rut) {
        return empresas.stream().filter(e -> e.getRut().equals(rut)).findFirst();
    }

    protected Optional<Terminal> findTerminal(String nombre) {
        return terminales.stream().filter(t -> t.getNombre().equalsIgnoreCase(nombre)).findFirst();
    }

    protected Optional<Terminal> findTerminalPorComuna(String comuna) {
        return terminales.stream().filter(t -> t.getDireccion().getComuna().equalsIgnoreCase(comuna)).findFirst();
    }

    protected Optional<Bus> findBus(String patente) {
        return empresas.stream()
                .flatMap(e -> Arrays.stream(e.getBuses()))
                .filter(b -> b.getPatente().equalsIgnoreCase(patente))
                .findFirst();
    }

    protected Optional<Conductor> findConductor(idPersona id, Rut rutEmpresa) {
        return findEmpresa(rutEmpresa)
                .map(Empresa::getTripulantes)
                .stream()
                .flatMap(Arrays::stream)
                .filter(t -> t instanceof Conductor && t.getIdPersona().equals(id))
                .map(Conductor.class::cast)
                .findFirst();
    }

    protected Optional<Auxiliar> findAuxiliar(idPersona id, Rut rutEmpresa) {
        return findEmpresa(rutEmpresa)
                .map(Empresa::getTripulantes)
                .stream()
                .flatMap(Arrays::stream)
                .filter(t -> t instanceof Auxiliar && t.getIdPersona().equals(id))
                .map(Auxiliar.class::cast)
                .findFirst();
    }
}