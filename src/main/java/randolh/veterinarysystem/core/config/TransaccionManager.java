package randolh.veterinarysystem.core.config;

import randolh.veterinarysystem.core.exception.BaseDatosException;
import randolh.veterinarysystem.core.utils.LoggerManager;

import java.sql.Connection;
import java.sql.SQLException;

// Gestor funcional de transacciones ACID
public final class TransaccionManager {

    private static final String MODULO = "TransaccionManager";

    private TransaccionManager() {
    }

    // Operacion transaccional con valor de retorno
    @FunctionalInterface
    public interface OperacionTransaccional<T> {
        T ejecutar(Connection conn) throws Exception;
    }

    // Operacion transaccional sin valor de retorno
    @FunctionalInterface
    public interface AccionTransaccional {
        void ejecutar(Connection conn) throws Exception;
    }

    // Ejecuta un bloque en una transaccion con commit y rollback automatico
    public static <T> T ejecutarEnTransaccion(OperacionTransaccional<T> operacion) {
        Connection conn = ConexionBD.getInstancia().getConexion();
        boolean autoCommitOriginal = true;

        try {
            autoCommitOriginal = conn.getAutoCommit();
            conn.setAutoCommit(false);

            T resultado = operacion.ejecutar(conn);

            conn.commit();
            LoggerManager.info(MODULO, "Transaccion confirmada (commit) exitosamente.");
            return resultado;

        } catch (Exception e) {
            try {
                conn.rollback();
                LoggerManager.error(MODULO, "Error en transaccion. Rollback aplicado exitosamente.", e);
            } catch (SQLException rollbackEx) {
                LoggerManager.error(MODULO, "Fallo critico al intentar aplicar rollback.", rollbackEx);
            }

            if (e instanceof BaseDatosException) {
                throw (BaseDatosException) e;
            }
            throw new BaseDatosException("Fallo en la ejecucion de la transaccion: " + e.getMessage(), e);

        } finally {
            try {
                conn.setAutoCommit(autoCommitOriginal);
            } catch (SQLException autoCommitEx) {
                LoggerManager.warning(MODULO, "No se pudo restaurar el estado original de autoCommit: " + autoCommitEx.getMessage());
            }
        }
    }

    // Sobrecarga para acciones sin valor de retorno
    public static void ejecutarAccionEnTransaccion(AccionTransaccional accion) {
        ejecutarEnTransaccion(conn -> {
            accion.ejecutar(conn);
            return null;
        });
    }
}
