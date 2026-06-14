package vista;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.Scanner;

import controlador.*;
import excepciones.SVPException;
import modelo.*;
import utilidades.*;

public class UISVP {
    private static UISVP instance;
    private Scanner sc;
    private SimpleDateFormat fmtFecha;
    private SimpleDateFormat fmtHora;

    private UISVP() {
        sc = new Scanner(System.in);
        fmtFecha = new SimpleDateFormat("dd/MM/yyyy");
        fmtHora = new SimpleDateFormat("HH:mm");
    }

    public static UISVP getInstance() {
        if (instance == null) {
            instance = new UISVP();
        }
        return instance;
    }

    public void menu() {
        int opcion = 0;
        while (opcion != 14) {
            System.out.println("\n==========================================");
            System.out.println("...::: Menú principal :::...");
            System.out.println("1) Crear empresa");
            System.out.println("2) Contratar tripulante");
            System.out.println("3) Crear terminal");
            System.out.println("4) Crear cliente");
            System.out.println("5) Crear bus");
            System.out.println("6) Crear viaje");
            System.out.println("7) Vender pasajes");
            System.out.println("8) Listar ventas");
            System.out.println("9) Listar viajes");
            System.out.println("10) Listar pasajeros de viaje");
            System.out.println("11) Listar empresas");
            System.out.println("12) Listar llegadas/salidas de terminal");
            System.out.println("13) Listar ventas de empresa");
            System.out.println("14) Salir");
            System.out.println("------------------------------------------");
            System.out.print("..:: Ingrese número de opción: ");

            try {
                opcion = Integer.parseInt(sc.nextLine());
                ejecutarOpcion(opcion);
            } catch (NumberFormatException e) {
                System.out.println("*** Error: Debe ingresar un número válido. ***");
            } catch (SVPException e) {
                System.out.println("\n*** Error: " + e.getMessage() + " ***");
            } catch (Exception e) {
                System.out.println("\n*** Error de formato en los datos ingresados. Intente nuevamente. ***");
            }
        }
    }

    private void ejecutarOpcion(int opcion) throws Exception {
        switch (opcion) {
            case 1: createEmpresa(); break;
            case 2: contrataTripulante(); break;
            case 3: createTerminal(); break;
            case 4: createCliente(); break;
            case 5: createBus(); break;
            case 6: createViaje(); break;
            case 7: vendePasajes(); break;
            case 8: listVentas(); break;
            case 9: listViajes(); break;
            case 10: listPasajerosViaje(); break;
            case 11: listEmpresas(); break;
            case 12: listLlegadasSalidasTerminal(); break;
            case 13: listVentasEmpresa(); break;
            case 14: System.out.println("Saliendo del sistema..."); break;
            default: System.out.println("*** Error: Opción fuera de rango (1-14) ***");
        }
    }

    private Rut leerRut() {
        System.out.print("R.U.T : ");
        String rutStr = sc.nextLine().replace(".", "");
        return Rut.of(rutStr);
    }

    private idPersona leerIdPersona() {
        System.out.print("Rut[1] o Pasaporte[2] : ");
        int tId = Integer.parseInt(sc.nextLine());
        if (tId == 1) {
            return leerRut();
        } else {
            System.out.print("Número Pasaporte : ");
            String num = sc.nextLine();
            System.out.print("Nacionalidad : ");
            String nac = sc.nextLine();
            return Pasaporte.of(num, nac);
        }
    }

    private Nombre leerNombreCompleto() {
        Nombre nombre = new Nombre();
        System.out.print("Sr.[1] o Sra.[2] : ");
        int t = Integer.parseInt(sc.nextLine());
        nombre.setTratamiento(t == 1 ? Tratamiento.SR : Tratamiento.SRA);
        System.out.print("Nombres : ");
        nombre.setNombres(sc.nextLine());
        System.out.print("Apellido Paterno : ");
        nombre.setApellidoPaterno(sc.nextLine());
        System.out.print("Apellido Materno : ");
        nombre.setApellidoMaterno(sc.nextLine());
        return nombre;
    }

    private Direccion leerDireccion() {
        System.out.print("Calle : ");
        String calle = sc.nextLine();
        System.out.print("Numero : ");
        int num = Integer.parseInt(sc.nextLine());
        System.out.print("Comuna : ");
        String comuna = sc.nextLine();
        return new Direccion(calle, num, comuna);
    }

    private void createEmpresa() {
        System.out.println("\n:::: Creando una nueva Empresa ::::");
        Rut rut = leerRut();
        System.out.print("Nombre: ");
        String nombre = sc.nextLine();
        System.out.print("url: ");
        String url = sc.nextLine();

        ControladorEmpresas.getInstance().createEmpresa(rut, nombre, url);
        System.out.println(":::: Empresa guardada exitosamente ::::");
    }

    private void contrataTripulante() {
        System.out.println("\n...:::: Contratando un nuevo Tripulante ::::....");
        System.out.println(":::: Dato de la Empresa");
        Rut rutEmpresa = leerRut();

        System.out.println("\n:::: Datos tripulante");
        System.out.print("Auxiliar[1] o Conductor[2] : ");
        int tipo = Integer.parseInt(sc.nextLine());

        idPersona id = leerIdPersona();
        Nombre nom = leerNombreCompleto();
        Direccion dir = leerDireccion();

        if (tipo == 1) {
            ControladorEmpresas.getInstance().hireAuxiliarForEmpresa(rutEmpresa, id, nom, dir);
            System.out.println("...:::: Auxiliar contratado exitosamente ::::....");
        } else {
            ControladorEmpresas.getInstance().hireConductorForEmpresa(rutEmpresa, id, nom, dir);
            System.out.println("...:::: Conductor contratado exitosamente ::::....");
        }
    }

    private void createTerminal() {
        System.out.println("\n..:::: Creando un nuevo Terminal ::::....");
        System.out.print("Nombre : ");
        String nombre = sc.nextLine();
        Direccion dir = leerDireccion();

        ControladorEmpresas.getInstance().createTerminal(nombre, dir);
        System.out.println("...:::: Terminal guardado exitosamente ::::....");
    }

    private void createCliente() {
        System.out.println("\n...:::: Crear un nuevo Cliente ::::....");
        idPersona id = leerIdPersona();
        Nombre nombre = leerNombreCompleto();
        System.out.print("Telefono movil : ");
        String fono = sc.nextLine();
        System.out.print("Email : ");
        String email = sc.nextLine();

        SistemaVentaPasajes.getInstance().createCliente(id, nombre, fono, email);
        System.out.println("...:::: Cliente guardado exitosamente ::::...");
    }

    private void createBus() {
        System.out.println("\n..:::: Creando un nuevo Bus ::::....");
        System.out.print("Patente : ");
        String pat = sc.nextLine();
        System.out.print("Marca : ");
        String marca = sc.nextLine();
        System.out.print("Modelo : ");
        String modelo = sc.nextLine();
        System.out.print("Número de asientos : ");
        int asientos = Integer.parseInt(sc.nextLine());

        System.out.println(":::: Dato de la empresa");
        Rut rut = leerRut();

        ControladorEmpresas.getInstance().createBus(pat, marca, modelo, asientos, rut);
        System.out.println("...:::: Bus guardado exitosamente ::::....");
    }

    private void createViaje() throws ParseException {
        System.out.println("\n...:::: Creando un nuevo Viaje ::::....");
        System.out.print("Fecha[dd/mm/yyyy] : ");
        Date fecha = fmtFecha.parse(sc.nextLine());
        System.out.print("Hora[hh:mm] : ");
        Time hora = new Time(fmtHora.parse(sc.nextLine()).getTime());
        System.out.print("Precio : ");
        int precio = Integer.parseInt(sc.nextLine());
        System.out.print("Duración (minutos) : ");
        int duracion = Integer.parseInt(sc.nextLine());
        System.out.print("Patente Bus : ");
        String patente = sc.nextLine();

        System.out.print("Nro. de conductores : ");
        int nroCond = Integer.parseInt(sc.nextLine());

        idPersona[] tripulantes = new idPersona[3];
        System.out.println(":: Id Auxiliar ::");
        tripulantes[0] = leerIdPersona();
        System.out.println(":: Id Conductor ::");
        tripulantes[1] = leerIdPersona();

        if (nroCond > 1) {
            System.out.println(":: Id Conductor 2 ::");
            tripulantes[2] = leerIdPersona();
        }

        String[] comunas = new String[2];
        System.out.print("Nombre comuna salida : ");
        comunas[0] = sc.nextLine();
        System.out.print("Nombre comuna llegada : ");
        comunas[1] = sc.nextLine();

        SistemaVentaPasajes.getInstance().createViaje(fecha, hora, precio, duracion, patente, tripulantes, comunas);
        System.out.println("...:::: Viaje guardado exitosamente ::::....");
    }

    private void vendePasajes() throws ParseException {
        System.out.println("\n..:::: Venta de pasajes ::::..");

        System.out.println(":::: Datos de la Venta");
        System.out.print("ID Documento: ");
        String idDoc = sc.nextLine();
        System.out.print("Tipo documento: [1] Boleta [2] Factura : ");
        TipoDocumento tipo = Integer.parseInt(sc.nextLine()) == 1 ? TipoDocumento.BOLETA : TipoDocumento.FACTURA;
        System.out.print("Fecha de viaje[dd/mm/yyyy] : ");
        Date fechaViaje = fmtFecha.parse(sc.nextLine());
        System.out.print("Origen (comuna): ");
        String comSalida = sc.nextLine();
        System.out.print("Destino (comuna): ");
        String comLlegada = sc.nextLine();

        System.out.println("\n:::: Datos del cliente");
        idPersona idCliente = leerIdPersona();

        System.out.println("\n:::: Pasajes a vender");
        System.out.print("Cantidad de pasajes: ");
        int cantidad = Integer.parseInt(sc.nextLine());

        SistemaVentaPasajes.getInstance().iniciaVenta(idDoc, tipo, fechaViaje, comSalida, comLlegada, idCliente, cantidad);

        String[][] horarios = SistemaVentaPasajes.getInstance().getHorariosDisponibles(fechaViaje, comSalida, comLlegada, cantidad);
        System.out.println("\n:::: Listado de horarios disponibles");
        System.out.println("*------------*----------*----------*----------*");
        System.out.println("| BUS        | SALIDA   | VALOR    | ASIENTOS |");
        System.out.println("*------------*----------*----------*----------*");
        for (int i = 0; i < horarios.length; i++) {
            System.out.printf("%d | %-10s | %-8s | $%-7s | %-8s |\n", (i+1), horarios[i][0], horarios[i][1], horarios[i][2], horarios[i][3]);
        }
        System.out.println("*------------*----------*----------*----------*");
        System.out.print("Seleccione viaje [1.." + horarios.length + "] : ");
        int sel = Integer.parseInt(sc.nextLine()) - 1;

        String patBus = horarios[sel][0];
        Time hora = new Time(fmtHora.parse(horarios[sel][1]).getTime());

        System.out.println("\n:::: Asientos disponibles para el viaje seleccionado");
        String[] asientos = SistemaVentaPasajes.getInstance().listAsientosDeViaje(fechaViaje, hora, patBus);
        for (int i = 0; i < asientos.length; i++) {
            System.out.printf("| %-2s ", asientos[i]);
            if ((i + 1) % 4 == 0) System.out.println("|");
        }
        System.out.print("\nSeleccione sus asientos [separe por ,] : ");
        String[] asientosSelect = sc.nextLine().split(",");

        for (int i = 0; i < cantidad; i++) {
            System.out.println("\n:::: Datos pasajeros " + (i + 1));
            idPersona idPas = leerIdPersona();

            Optional<String> nomP = SistemaVentaPasajes.getInstance().getNombrePasajero(idPas);
            if (!nomP.isPresent()) {
                System.out.println("Pasajero nuevo, ingrese datos:");
                Nombre nP = leerNombreCompleto();
                System.out.print("Telefono: "); String fono = sc.nextLine();

                System.out.println("- Contacto Emergencia -");
                Nombre nC = leerNombreCompleto();
                System.out.print("Telefono Contacto: "); String fonoC = sc.nextLine();

                SistemaVentaPasajes.getInstance().createPasajero(idPas, nP, fono, nC, fonoC);
            }

            SistemaVentaPasajes.getInstance().vendePasaje(idDoc, tipo, fechaViaje, hora, patBus, Integer.parseInt(asientosSelect[i].trim()), idPas);
            System.out.println(":::: Pasaje agregado exitosamente");
        }

        int total = SistemaVentaPasajes.getInstance().getMontoVenta(idDoc, tipo).orElse(0);
        System.out.println("\n:::: Monto total de la venta: $" + total);
        System.out.println(":::: Pago de la venta");
        System.out.print("Efectivo[1] o Tarjeta[2] : ");
        int formaPago = Integer.parseInt(sc.nextLine());

        if (formaPago == 1) {
            SistemaVentaPasajes.getInstance().pagaVenta(idDoc, tipo);
        } else {
            System.out.print("Ingrese número de tarjeta: ");
            long nroTarj = Long.parseLong(sc.nextLine());
            SistemaVentaPasajes.getInstance().pagaVenta(idDoc, tipo, nroTarj);
        }

        System.out.println("...:::: Venta realizada exitosamente ::::...");
    }

    private void listVentas() {
        System.out.println("\n...:::: Listado de ventas generales ::::....");
        String[][] ventas = SistemaVentaPasajes.getInstance().listVentas();
        for (String[] v : ventas) {
            System.out.printf("Doc: %s | Tipo: %s | Fecha: %s | Monto Pagado: $%s\n", v[0], v[1], v[2], v[3]);
        }
    }

    private void listViajes() {
        System.out.println("\n...... Listado de viajes .......");
        String[][] viajes = SistemaVentaPasajes.getInstance().listViajes();

        if (viajes.length == 0) {
            System.out.println("No existen viajes registrados.");
            return;
        }

        System.out.println("*------------*-----------*------------*--------*---------------*----------*----------*---------*");
        System.out.println("| FECHA      | HORA SALE | HORA LLEGA | PRECIO | ASIENTOS DISP | PATENTE  | ORIGEN   | DESTINO |");
        System.out.println("*------------*-----------*------------*--------*---------------*----------*----------*---------*");
        for (String[] v : viajes) {
            System.out.printf("| %-10s | %-9s | %-10s | %-6s | %-13s | %-8s | %-8s | %-7s |\n",
                    v[0], v[1], v[2], v[3], v[4], v[5], v[6], v[7]);
        }
        System.out.println("*------------*-----------*------------*--------*---------------*----------*----------*---------*");
    }

    private void listPasajerosViaje() throws ParseException {
        System.out.println("\n...:::: Listado de pasajeros de un viaje ::::....");
        System.out.print("Fecha del viaje[dd/mm/yyyy] : ");
        Date fecha = fmtFecha.parse(sc.nextLine());
        System.out.print("Hora del viaje[hh:mm] : ");
        Time hora = new Time(fmtHora.parse(sc.nextLine()).getTime());
        System.out.print("Patente bus : ");
        String patente = sc.nextLine();

        String[][] pasajeros = SistemaVentaPasajes.getInstance().listPasajerosViaje(fecha, hora, patente);
        for (String[] p : pasajeros) {
            System.out.printf("ID: %s | Nombre: %s | Contacto: %s | Fono: %s\n", p[0], p[1], p[2], p[3]);
        }
    }

    private void listEmpresas() {
        System.out.println("\n...... Listado de empresas .......");
        String[][] emp = ControladorEmpresas.getInstance().listEmpresas();
        System.out.println("*----------------*----------------------*-------------------------*---------------------------------*");
        System.out.println("| RUT EMPRESA    | NOMBRE               | URL                     | ESTADO GENERAL                  |");
        System.out.println("*----------------*----------------------*-------------------------*---------------------------------*");
        for (String[] e : emp) {
            String estado = "Trips: " + e[3] + " | Buses: " + e[4] + " | Ventas: " + e[5];
            System.out.printf("| %-14s | %-20s | %-23s | %-31s |\n", e[0], e[1], e[2], estado);
        }
        System.out.println("*----------------*----------------------*-------------------------*---------------------------------*");
    }

    private void listLlegadasSalidasTerminal() throws ParseException {
        System.out.println("\n...:::: Listado de llegadas y salidas de un terminal ::::....");
        System.out.print("Nombre terminal : ");
        String term = sc.nextLine();
        System.out.print("Fecha[dd/mm/yyyy] : ");
        Date f = fmtFecha.parse(sc.nextLine());

        String[][] movs = ControladorEmpresas.getInstance().listLlegadasSalidasTerminal(term, f);
        System.out.println("*----------------*--------*-------------*--------------------*----------------*");
        System.out.println("| LLEGADA/SALIDA | HORA   | PATENTE BUS | NOMBRE EMPRESA     | NRO. PASAJEROS |");
        System.out.println("*----------------*--------*-------------*--------------------*----------------*");
        for (String[] m : movs) {
            System.out.printf("| %-14s | %-6s | %-11s | %-18s | %-14s |\n", m[0], m[1], m[2], m[3], m[4]);
        }
        System.out.println("*----------------*--------*-------------*--------------------*----------------*");
    }

    private void listVentasEmpresa() {
        System.out.println("\n...:::: Listado de ventas de una empresa ::::....");
        Rut rut = leerRut();

        String[][] ventas = ControladorEmpresas.getInstance().listVentasEmpresa(rut);
        System.out.println("*------------*----------*--------------*---------------*");
        System.out.println("| FECHA      | TIPO     | MONTO PAGADO | TIPO PAGO     |");
        System.out.println("*------------*----------*--------------*---------------*");
        for (String[] v : ventas) {
            System.out.printf("| %-10s | %-8s | $%-11s | %-13s |\n", v[0], v[1], v[2], v[3]);
        }
        System.out.println("*------------*----------*--------------*---------------*");
    }
}