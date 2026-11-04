package randolh.veterinarysystem.dueno;

import randolh.veterinarysystem.core.exception.ValidacionException;
import randolh.veterinarysystem.core.utils.LoggerManager;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

// Controlador de logica de negocio para duenos y clientes
public class DuenoController {

    private static final String MODULO = "DuenoController";
    private static final Pattern PATRON_EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PATRON_TELEFONO = Pattern.compile("^[0-9+()\\-\\s]{7,20}$");

    private final DuenoDAO duenoDAO;

    public DuenoController() {
        this.duenoDAO = new DuenoDAO();
    }

    public DuenoController(DuenoDAO duenoDAO) {
        this.duenoDAO = duenoDAO;
    }

    // Registra un nuevo dueno validando reglas de negocio y unicidad
    public Dueno registrarDueno(Dueno dueno) {
        validarDueno(dueno);

        // Validacion de unicidad de documento
        Optional<Dueno> existenteDoc = duenoDAO.buscarPorDocumento(dueno.getNumeroDocumento());
        if (existenteDoc.isPresent()) {
            throw new ValidacionException("Ya existe un dueno registrado con el numero de documento: " + dueno.getNumeroDocumento());
        }

        // Validacion de unicidad de correo electronico
        Optional<Dueno> existenteEmail = duenoDAO.buscarPorEmail(dueno.getEmail());
        if (existenteEmail.isPresent()) {
            throw new ValidacionException("Ya existe un dueno registrado con el correo electronico: " + dueno.getEmail());
        }

        LoggerManager.info(MODULO, "Registrando dueno: " + dueno.getNombreCompleto());
        return duenoDAO.insertar(dueno);
    }

    // Actualiza los datos de un dueno validando unicidad contra otros registros
    public boolean actualizarDueno(Dueno dueno) {
        if (dueno.getId() == null || dueno.getId() <= 0) {
            throw new ValidacionException("El identificador del dueno es obligatorio para actualizar.");
        }

        validarDueno(dueno);

        // Verificar que no choque con otro dueno por documento
        Optional<Dueno> porDoc = duenoDAO.buscarPorDocumento(dueno.getNumeroDocumento());
        if (porDoc.isPresent() && !porDoc.get().getId().equals(dueno.getId())) {
            throw new ValidacionException("El documento '" + dueno.getNumeroDocumento() + "' ya pertenece a otro cliente registrado.");
        }

        // Verificar que no choque con otro dueno por email
        Optional<Dueno> porEmail = duenoDAO.buscarPorEmail(dueno.getEmail());
        if (porEmail.isPresent() && !porEmail.get().getId().equals(dueno.getId())) {
            throw new ValidacionException("El correo '" + dueno.getEmail() + "' ya pertenece a otro cliente registrado.");
        }

        LoggerManager.info(MODULO, "Actualizando dueno ID: " + dueno.getId());
        return duenoDAO.actualizar(dueno);
    }

    // Desactiva un dueno de forma logica
    public boolean desactivarDueno(int id) {
        if (id <= 0) {
            throw new ValidacionException("ID invalido para desactivacion.");
        }
        Optional<Dueno> existente = duenoDAO.buscarPorId(id);
        if (existente.isEmpty()) {
            throw new ValidacionException("No se encontro ningun dueno con el ID: " + id);
        }
        LoggerManager.info(MODULO, "Desactivando dueno ID: " + id);
        return duenoDAO.eliminarLogico(id);
    }

    // Busca dueno por ID
    public Optional<Dueno> obtenerPorId(int id) {
        if (id <= 0) {
            throw new ValidacionException("ID de consulta invalido.");
        }
        return duenoDAO.buscarPorId(id);
    }

    // Busca dueno por documento
    public Optional<Dueno> obtenerPorDocumento(String documento) {
        if (documento == null || documento.trim().isEmpty()) {
            throw new ValidacionException("Debe ingresar un numero de documento para la busqueda.");
        }
        return duenoDAO.buscarPorDocumento(documento.trim());
    }

    // Busca duenos por nombre o apellido
    public List<Dueno> buscarPorNombre(String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            throw new ValidacionException("Debe ingresar un termino de busqueda.");
        }
        return duenoDAO.buscarPorNombre(termino.trim());
    }

    // Lista todos los duenos
    public List<Dueno> listarTodos() {
        return duenoDAO.listarTodos();
    }

    // Valida integridad y restricciones de columnas DDL
    public void validarDueno(Dueno d) {
        if (d == null) {
            throw new ValidacionException("Los datos del dueno no pueden ser nulos.");
        }

        // Tipo de documento (VARCHAR 20)
        if (d.getTipoDocumento() == null || d.getTipoDocumento().trim().isEmpty()) {
            d.setTipoDocumento("CC");
        } else if (d.getTipoDocumento().length() > 20) {
            throw new ValidacionException("El tipo de documento no puede exceder 20 caracteres.");
        }

        // Numero de documento (VARCHAR 20, obligatorio)
        if (d.getNumeroDocumento() == null || d.getNumeroDocumento().trim().isEmpty()) {
            throw new ValidacionException("El numero de documento es obligatorio.");
        }
        if (d.getNumeroDocumento().trim().length() > 20) {
            throw new ValidacionException("El numero de documento no puede exceder 20 caracteres.");
        }

        // Nombre completo (VARCHAR 150, obligatorio)
        if (d.getNombreCompleto() == null || d.getNombreCompleto().trim().isEmpty()) {
            throw new ValidacionException("El nombre completo del dueno es obligatorio.");
        }
        if (d.getNombreCompleto().trim().length() > 150) {
            throw new ValidacionException("El nombre completo no puede exceder 150 caracteres.");
        }

        // Telefono (VARCHAR 20, obligatorio)
        if (d.getTelefono() == null || d.getTelefono().trim().isEmpty()) {
            throw new ValidacionException("El numero de telefono es obligatorio.");
        }
        if (d.getTelefono().trim().length() > 20) {
            throw new ValidacionException("El telefono no puede exceder 20 caracteres.");
        }
        if (!PATRON_TELEFONO.matcher(d.getTelefono().trim()).matches()) {
            throw new ValidacionException("El telefono contiene caracteres no validos. Solo se permiten digitos, espacios, guiones y '+'.");
        }

        // Email (VARCHAR 100, obligatorio, formato valido)
        if (d.getEmail() == null || d.getEmail().trim().isEmpty()) {
            throw new ValidacionException("El correo electronico es obligatorio.");
        }
        if (d.getEmail().trim().length() > 100) {
            throw new ValidacionException("El correo electronico no puede exceder 100 caracteres.");
        }
        if (!PATRON_EMAIL.matcher(d.getEmail().trim()).matches()) {
            throw new ValidacionException("El formato del correo electronico '" + d.getEmail() + "' es invalido (ejemplo: usuario@dominio.com).");
        }

        // Direccion (VARCHAR 255, opcional)
        if (d.getDireccion() != null && d.getDireccion().trim().length() > 255) {
            throw new ValidacionException("La direccion no puede exceder 255 caracteres.");
        }

        // Contacto de emergencia (VARCHAR 100, opcional)
        if (d.getContactoEmergencia() != null && d.getContactoEmergencia().trim().length() > 100) {
            throw new ValidacionException("El contacto de emergencia no puede exceder 100 caracteres.");
        }
    }
}
