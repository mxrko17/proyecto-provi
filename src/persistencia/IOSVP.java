package persistencia;

import excepciones.SVPException;
import modelo.*;
import utilidades.Direccion;
import utilidades.Nombre;
import utilidades.Rut;
import utilidades.Tratamiento;
import utilidades.idPersona;

import java.io.*;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Predicate;

public class IOSVP implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private static IOSVP instance;

    private IOSVP() {
    }

    public static IOSVP getInstance() {
        if (instance == null) {
            instance = new IOSVP();
        }
        return instance;
    }

    public Object[] readDatosIniciales() {
        File archivo = new File("SVPDatosIniciales.txt");
        if (!archivo.exists() || !archivo.canRead()) {
            throw new SVPException("No existe o no se puede abrir el archivo SVPDatosIniciales.txt");
        }

        List<Object> objetosCreados = new ArrayList<>();
        List<Empresa> empresasTemp = new ArrayList<>();
        List<Terminal> terminalesTemp = new ArrayList<>();
        List<Bus> busesTemp = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            int seccionActual = 0;

            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;

                if (linea.equals("+")) {
                    seccionActual++;
                    continue;
                }

                String[] datos = linea.split(";");
                for (int i = 0; i < datos.length; i++) {
                    datos[i] = datos[i].trim();
                }

                switch (seccionActual) {
                    case 0: // --- Clientes y/o Pasajeros ---
                        String[] primeraCelda = datos[0].split("[: ]");
                        String tipo = primeraCelda[0].trim();
                        // CORRECCIÓN: Limpiamos los puntos antes de pasarlo a Rut.of
                        Rut rut = Rut.of(primeraCelda[1].trim().replace(".", ""));

                        Nombre nom = new Nombre();
                        nom.setTratamiento(Tratamiento.valueOf(datos[1]));
                        nom.setNombres(datos[2]);
                        nom.setApellidoPaterno(datos[3]);
                        nom.setApellidoMaterno(datos[4]);
                        String fono = datos[5];

                        if (tipo.equals("C") || tipo.equals("CP")) {
                            String email = datos[6];
                            Cliente c = new Cliente(rut, nom, email);
                            c.setTelefono(fono);
                            objetosCreados.add(c);
                        }

                        if (tipo.equals("P") || tipo.equals("CP")) {
                            int offset = tipo.equals("CP") ? 7 : 6;
                            Nombre nomContacto = new Nombre();
                            nomContacto.setTratamiento(Tratamiento.valueOf(datos[offset]));
                            nomContacto.setNombres(datos[offset + 1]);
                            nomContacto.setApellidoPaterno(datos[offset + 2]);
                            nomContacto.setApellidoMaterno(datos[offset + 3]);
                            String fonoContacto = datos[offset + 4];

                            Pasajero p = new Pasajero(rut, nom, fono, nomContacto, fonoContacto);
                            objetosCreados.add(p);
                        }
                        break;

                    case 1: // --- Empresas ---
                        // CORRECCIÓN: Limpiamos los puntos
                        Empresa emp = new Empresa(Rut.of(datos[0].replace(".", "")), datos[1]);
                        emp.setUrl(datos[2]);
                        empresasTemp.add(emp);
                        objetosCreados.add(emp);
                        break;

                    case 2: // --- Tripulantes ---
                        String[] celdaTrip = datos[0].split("[: ]");
                        String tipoTrip = celdaTrip[0].trim();
                        // CORRECCIÓN: Limpiamos los puntos
                        Rut rutTrip = Rut.of(celdaTrip[1].trim().replace(".", ""));

                        Nombre nomTrip = new Nombre();
                        nomTrip.setTratamiento(Tratamiento.valueOf(datos[1]));
                        nomTrip.setNombres(datos[2]);
                        nomTrip.setApellidoPaterno(datos[3]);
                        nomTrip.setApellidoMaterno(datos[4]);

                        Direccion dirTrip = new Direccion(datos[5], Integer.parseInt(datos[6]), datos[7]);
                        // CORRECCIÓN: Limpiamos los puntos
                        Rut rutEmpresaTrip = Rut.of(datos[8].replace(".", ""));

                        findEmpresa(empresasTemp, e -> e.getRut().equals(rutEmpresaTrip)).ifPresent(e -> {
                            if (tipoTrip.equals("A")) e.addAuxiliar(rutTrip, nomTrip, dirTrip);
                            if (tipoTrip.equals("C")) e.addConductor(rutTrip, nomTrip, dirTrip);
                        });
                        break;

                    case 3: // --- Terminales ---
                        Terminal term = new Terminal(datos[0], new Direccion(datos[1], Integer.parseInt(datos[2]), datos[3]));
                        terminalesTemp.add(term);
                        objetosCreados.add(term);
                        break;

                    case 4: // --- Buses ---
                        // CORRECCIÓN: Limpiamos los puntos
                        Rut rutEmpBus = Rut.of(datos[4].replace(".", ""));
                        findEmpresa(empresasTemp, e -> e.getRut().equals(rutEmpBus)).ifPresent(e -> {
                            Bus bus = new Bus(datos[0], Integer.parseInt(datos[3]), e);
                            bus.setMarca(datos[1]);
                            bus.setModelo(datos[2]);
                            busesTemp.add(bus);
                            objetosCreados.add(bus);
                        });
                        break;

                    case 5: // --- Viajes ---
                        SimpleDateFormat sdfFec = new SimpleDateFormat("dd-MM-yyyy");
                        SimpleDateFormat sdfHor = new SimpleDateFormat("HH:mm");

                        Date fecha = sdfFec.parse(datos[0]);
                        Time hora = new Time(sdfHor.parse(datos[1]).getTime());
                        int precio = Integer.parseInt(datos[2]);
                        int duracion = Integer.parseInt(datos[3]);
                        String patente = datos[4];
                        // CORRECCIÓN: Limpiamos los puntos
                        Rut rutAux = Rut.of(datos[5].replace(".", ""));
                        String[] rutsConductores = datos[6].split(" ");

                        Optional<Bus> busViaje = findBus(busesTemp, b -> b.getPatente().equalsIgnoreCase(patente));
                        Optional<Terminal> termSalida = findTerminal(terminalesTemp, t -> t.getNombre().equalsIgnoreCase(datos[7]));
                        Optional<Terminal> termLlega = findTerminal(terminalesTemp, t -> t.getNombre().equalsIgnoreCase(datos[8]));

                        if (busViaje.isPresent() && termSalida.isPresent() && termLlega.isPresent()) {
                            Empresa empresaViaje = busViaje.get().getEmpresa();
                            Optional<Tripulante> aux = findTripulante(empresaViaje, rutAux, "Auxiliar");

                            List<Conductor> condsLista = new ArrayList<>();
                            for (String rc : rutsConductores) {
                                // CORRECCIÓN: Limpiamos los puntos también en el bucle de los choferes
                                findTripulante(empresaViaje, Rut.of(rc.replace(".", "")), "Conductor")
                                        .ifPresent(c -> condsLista.add((Conductor) c));
                            }

                            if (aux.isPresent() && !condsLista.isEmpty()) {
                                Viaje v = new Viaje(fecha, hora, precio, duracion, busViaje.get(),
                                        (Auxiliar) aux.get(), condsLista.toArray(new Conductor[0]),
                                        termSalida.get(), termLlega.get());
                                objetosCreados.add(v);
                            }
                        }
                        break;
                }
            }
        } catch (Exception e) {
            throw new SVPException("No existe o no se puede abrir el archivo SVPDatosIniciales.txt: " + e.getMessage());
        }

        return objetosCreados.toArray();
    }

    public void saveControladores(Object[] controladores) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("SVPObjetos.obj"))) {
            oos.writeObject(controladores);
        } catch (FileNotFoundException e) {
            throw new SVPException("No se puede abrir o crear el archivo SVPObjetos.obj");
        } catch (IOException e) {
            throw new SVPException("No se puede grabar en el archivo SVPObjetos.obj");
        }
    }

    public Object[] readControladores() {
        File file = new File("SVPObjetos.obj");
        if (!file.exists() || !file.canRead()) {
            throw new SVPException("No existe o no se puede abrir el archivo SVPObjetos.obj");
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Object[]) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new SVPException("No se puede leer el archivo SVPObjetos.obj");
        }
    }

    public void savePasajesDeVenta(Pasaje[] pasajes, String nombreArchivo) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(nombreArchivo))) {
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat hf = new SimpleDateFormat("HH:mm");

            for (Pasaje p : pasajes) {
                writer.println("---------------- PASAJE ELECTRÓNICO ----------------");
                writer.println("Nombre Empresa                  Número de pasaje");
                writer.printf("%-31s %d\n", p.getViaje().getBus().getEmpresa().getNombre().toUpperCase(), p.getNumero());
                writer.println("Nombre Pasajero                 RUT/Pasaporte");
                writer.printf("%-31s %s\n", p.getPasajero().getNombreCompleto().toString().toUpperCase(), p.getPasajero().getIdPersona().toString());
                writer.println("Patente bus    Asiento          Valor Pagado");
                writer.printf("%-14s %-16d %d\n", p.getViaje().getBus().getPatente(), p.getAsiento(), p.getViaje().getPrecio());
                writer.println("Terminal origen      Terminal destino    Fecha       Hora");
                writer.printf("%-20s %-19s %-11s %s\n",
                        p.getViaje().getTerminalSalida().getNombre().toUpperCase(),
                        p.getViaje().getTerminalLlegada().getNombre().toUpperCase(),
                        df.format(p.getViaje().getFecha()),
                        hf.format(p.getViaje().getHora()));
                writer.println("----------------------------------------------------\n");
            }
        } catch (IOException e) {
            throw new SVPException("No se puede abrir o crear el archivo " + nombreArchivo);
        }
    }

    public Optional<Empresa> findEmpresa(List<Empresa> empresas, Predicate<Empresa> criterio) {
        return empresas.stream().filter(criterio).findFirst();
    }

    public Optional<Bus> findBus(List<Bus> buses, Predicate<Bus> criterio) {
        return buses.stream().filter(criterio).findFirst();
    }

    public Optional<Terminal> findTerminal(List<Terminal> terminales, Predicate<Terminal> criterio) {
        return terminales.stream().filter(criterio).findFirst();
    }

    public Optional<Tripulante> findTripulante(Empresa empresa, idPersona id, String rol) {
        return java.util.Arrays.stream(empresa.getTripulantes())
                .filter(t -> t.getIdPersona().equals(id) && t.getClass().getSimpleName().equalsIgnoreCase(rol))
                .findFirst();
    }
}