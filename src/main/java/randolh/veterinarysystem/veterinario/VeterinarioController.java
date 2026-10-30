package randolh.veterinarysystem.veterinario;

import randolh.veterinarysystem.core.exception.ValidacionException;
import randolh.veterinarysystem.core.utils.LoggerManager;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

// Controlador de logica de negocio para profesionales veterinarios
public class VeterinarioController {

    private static final String MODULO = "VeterinarioController";
    private static final Pattern PATRON_EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PATRON_TELEFONO = Pattern.compile("^[0-9+()\\-\\s]{7,20}$");

    private final VeterinarioDAO veterinarioDAO;

    public VeterinarioController() {
        this.veterinarioDAO = new VeterinarioDAO();
    }

    public VeterinarioController(VeterinarioDAO veterinarioDAO) {
        this.veterinarioDAO = veterinarioDAO;
    }

    // Registra nuevo medico validando reglas de negocio y unicidad
    public Veterinario registrarVeterinario(Veterinario v) {
        validarVeterinario(v);

        // Validacion de unicidad de licencia profesional
        Optional<Veterinario> existenteLic = veterinarioDAO.buscarPorLicencia(v.getLicenciaProfesional());
        if (existenteLic.isPresent()) {
            throw new ValidacionException("Ya existe un medico registrado con la licencia profesional: " + v.getLicenciaProfesional());
        }

        // Validacion de unicidad de documento
        Optional<Veterinario> existenteDoc = veterinarioDAO.buscarPorDocumento(v.getNumeroDocumento());
        if (existenteDoc.isPresent()) {
            throw new ValidacionException("Ya existe un profesional registrado con el documento: " + v.getNumeroDocumento());
        }

        // Validacion de unicidad de correo electronico
        Optional<Veterinario> existenteEmail = veterinarioDAO.buscarPorEmail(v.getEmail());
        if (existenteEmail.isPresent()) {
            throw new ValidacionException("Ya existe un medico registrado con el correo: " + v.getEmail());
        }

        LoggerManager.info(MODULO, "Registrando medico veterinario: " + v.getNombreCompleto() + " (" + v.getEspecialidad() + ")");
        return veterinarioDAO.insertar(v);
    }

    // Actualiza datos de un medico veterinario validando duplicidades
    public boolean actualizarVeterinario(Veterinario v) {
        if (v.getId() == null || v.getId() <= 0) {
            throw new ValidacionException("El ID del profesional es obligatorio para actualizar.");
        }

        validarVeterinario(v);

        // Validar que la licencia no pertenezca a otro registro
        Optional<Veterinario> porLic = veterinarioDAO.buscarPorLicencia(v.getLicenciaProfesional());
        if (porLic.isPresent() && !porLic.get().getId().equals(v.getId())) {
            throw new ValidacionException("La licencia profesional '" + v.getLicenciaProfesional() + "' pertenece a otro medico.");
        }

        // Validar documento contra otros registros
        Optional<Veterinario> porDoc = veterinarioDAO.buscarPorDocumento(v.getNumeroDocumento());
        if (porDoc.isPresent() && !porDoc.get().getId().equals(v.getId())) {
            throw new ValidacionException("El documento '" + v.getNumeroDocumento() + "' pertenece a otro profesional.");
        }

        // Validar correo contra otros registros
        Optional<Veterinario> porEmail = veterinarioDAO.buscarPorEmail(v.getEmail());
        if (porEmail.isPresent() && !porEmail.get().getId().equals(v.getId())) {
            throw new ValidacionException("El correo '" + v.getEmail() + "' pertenece a otro profesional.");
        }

        LoggerManager.info(MODULO, "Actualizando veterinario ID: " + v.getId());
        return veterinarioDAO.actualizar(v);
    }

    // Desactivacion logica
    public boolean desactivarVeterinario(int id) {
        if (id <= 0) throw new ValidacionException("ID invalido para desactivacion.");
        Optional<Veterinario> opt = veterinarioDAO.buscarPorId(id);
        if (opt.isEmpty()) {
            throw new ValidacionException("No se encontro ningun profesional con ID: " + id);
        }
        LoggerManager.info(MODULO, "Desactivando profesional ID: " + id);
        return veterinarioDAO.eliminarLogico(id);
    }

    // Consultas
    public Optional<Veterinario> obtenerPorId(int id) {
        if (id <= 0) throw new ValidacionException("ID de consulta invalido.");
        return veterinarioDAO.buscarPorId(id);
    }

    public Optional<Veterinario> obtenerPorLicencia(String licencia) {
        if (licencia == null || licencia.trim().isEmpty()) {
            throw new ValidacionException("Debe ingresar una licencia profesional.");
        }
        return veterinarioDAO.buscarPorLicencia(licencia.trim());
    }

    public List<Veterinario> listarTodos() {
        return veterinarioDAO.listarTodos();
    }

    public List<Veterinario> listarPorEspecialidad(String especialidad) {
        if (especialidad == null || especialidad.trim().isEmpty()) {
            throw new ValidacionException("Debe ingresar una especialidad.");
        }
        return veterinarioDAO.listarPorEspecialidad(especialidad.trim());
    }

    // Validacion de atributos segun esquema de base de datos
    public void validarVeterinario(Veterinario v) {
        if (v == null) {
            throw new ValidacionException("Los datos del medico veterinario no pueden ser nulos.");
        }

        // Tipo de documento (VARCHAR 20)
        if (v.getTipoDocumento() == null || v.getTipoDocumento().trim().isEmpty()) {
            v.setTipoDocumento("CC");
        } else if (v.getTipoDocumento().trim().length() > 20) {
            throw new ValidacionException("El tipo de documento no puede exceder 20 caracteres.");
        }

        // Numero de documento (VARCHAR 20, obligatorio)
        if (v.getNumeroDocumento() == null || v.getNumeroDocumento().trim().isEmpty()) {
            throw new ValidacionException("El numero de documento es obligatorio.");
        }
        if (v.getNumeroDocumento().trim().length() > 20) {
            throw new ValidacionException("El numero de documento no puede exceder 20 caracteres.");
        }

        // Nombre completo (VARCHAR 150, obligatorio)
        if (v.getNombreCompleto() == null || v.getNombreCompleto().trim().isEmpty()) {
            throw new ValidacionException("El nombre del medico es obligatorio.");
        }
        if (v.getNombreCompleto().trim().length() > 150) {
            throw new ValidacionException("El nombre no puede exceder 150 caracteres.");
        }

        // Licencia profesional (VARCHAR 50, obligatorio)
        if (v.getLicenciaProfesional() == null || v.getLicenciaProfesional().trim().isEmpty()) {
            throw new ValidacionException("La licencia profesional o tarjeta medica es obligatoria.");
        }
        if (v.getLicenciaProfesional().trim().length() > 50) {
            throw new ValidacionException("La licencia no puede exceder 50 caracteres.");
        }

        // Especialidad (VARCHAR 100, obligatorio)
        if (v.getEspecialidad() == null || v.getEspecialidad().trim().isEmpty()) {
            throw new ValidacionException("La especialidad medica es obligatoria.");
        }
        if (v.getEspecialidad().trim().length() > 100) {
            throw new ValidacionException("La especialidad no puede exceder 100 caracteres.");
        }

        // Telefono (VARCHAR 20, obligatorio)
        if (v.getTelefono() == null || v.getTelefono().trim().isEmpty()) {
            throw new ValidacionException("El telefono es obligatorio.");
        }
        if (v.getTelefono().trim().length() > 20) {
            throw new ValidacionException("El telefono no puede exceder 20 caracteres.");
        }
        if (!PATRON_TELEFONO.matcher(v.getTelefono().trim()).matches()) {
            throw new ValidacionException("El telefono contiene caracteres no validos.");
        }

        // Email (VARCHAR 100, obligatorio, formato valido)
        if (v.getEmail() == null || v.getEmail().trim().isEmpty()) {
            throw new ValidacionException("El correo electronico es obligatorio.");
        }
        if (v.getEmail().trim().length() > 100) {
            throw new ValidacionException("El correo no puede exceder 100 caracteres.");
        }
        if (!PATRON_EMAIL.matcher(v.getEmail().trim()).matches()) {
            throw new ValidacionException("El formato del correo '" + v.getEmail() + "' es invalido.");
        }
    }
}
