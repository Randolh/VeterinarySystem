package randolh.veterinarysystem.cita;

import randolh.veterinarysystem.core.exception.ValidacionException;

import java.time.LocalDateTime;

// Interfaz del patron State para el ciclo de vida de una cita
public interface EstadoCitaState {

    void confirmar(Cita cita);

    void iniciarAtencion(Cita cita);

    void finalizar(Cita cita);

    void cancelar(Cita cita, String motivo);

    void marcarNoAsistio(Cita cita);

    EstadoCitaEnum getEstadoEnum();

    // Fabrica para instanciar el estado correspondiente
    static EstadoCitaState desdeEnum(EstadoCitaEnum estado) {
        if (estado == null) return new EstadoProgramadaState();
        return switch (estado) {
            case PROGRAMADA -> new EstadoProgramadaState();
            case CONFIRMADA -> new EstadoConfirmadaState();
            case EN_CURSO -> new EstadoEnCursoState();
            case FINALIZADA -> new EstadoFinalizadaState();
            case CANCELADA -> new EstadoCanceladaState();
            case NO_ASISTIO -> new EstadoNoAsistioState();
        };
    }
}

// Estado: Programada
class EstadoProgramadaState implements EstadoCitaState {

    @Override
    public void confirmar(Cita cita) {
        cita.setEstadoState(new EstadoConfirmadaState());
    }

    @Override
    public void iniciarAtencion(Cita cita) {
        cita.setEstadoState(new EstadoEnCursoState());
    }

    @Override
    public void finalizar(Cita cita) {
        throw new ValidacionException("No es posible finalizar una cita sin haber iniciado la atencion clinica.");
    }

    @Override
    public void cancelar(Cita cita, String motivo) {
        cita.setMotivoCancelacion(motivo);
        cita.setFechaCancelacion(LocalDateTime.now());
        cita.setEstadoState(new EstadoCanceladaState());
    }

    @Override
    public void marcarNoAsistio(Cita cita) {
        cita.setEstadoState(new EstadoNoAsistioState());
    }

    @Override
    public EstadoCitaEnum getEstadoEnum() {
        return EstadoCitaEnum.PROGRAMADA;
    }
}

// Estado: Confirmada
class EstadoConfirmadaState implements EstadoCitaState {

    @Override
    public void confirmar(Cita cita) {
        throw new ValidacionException("La cita ya se encuentra en estado confirmada.");
    }

    @Override
    public void iniciarAtencion(Cita cita) {
        cita.setEstadoState(new EstadoEnCursoState());
    }

    @Override
    public void finalizar(Cita cita) {
        throw new ValidacionException("Debe iniciar la atencion antes de poder finalizar la cita.");
    }

    @Override
    public void cancelar(Cita cita, String motivo) {
        cita.setMotivoCancelacion(motivo);
        cita.setFechaCancelacion(LocalDateTime.now());
        cita.setEstadoState(new EstadoCanceladaState());
    }

    @Override
    public void marcarNoAsistio(Cita cita) {
        cita.setEstadoState(new EstadoNoAsistioState());
    }

    @Override
    public EstadoCitaEnum getEstadoEnum() {
        return EstadoCitaEnum.CONFIRMADA;
    }
}

// Estado: En Curso
class EstadoEnCursoState implements EstadoCitaState {

    @Override
    public void confirmar(Cita cita) {
        throw new ValidacionException("La atencion ya se encuentra en curso.");
    }

    @Override
    public void iniciarAtencion(Cita cita) {
        throw new ValidacionException("La cita ya esta siendo atendida actualmente.");
    }

    @Override
    public void finalizar(Cita cita) {
        cita.setEstadoState(new EstadoFinalizadaState());
    }

    @Override
    public void cancelar(Cita cita, String motivo) {
        throw new ValidacionException("No se puede cancelar una consulta que ya se encuentra en curso.");
    }

    @Override
    public void marcarNoAsistio(Cita cita) {
        throw new ValidacionException("No se puede marcar inasistencia en una cita que ya esta en curso.");
    }

    @Override
    public EstadoCitaEnum getEstadoEnum() {
        return EstadoCitaEnum.EN_CURSO;
    }
}

// Estado: Finalizada
class EstadoFinalizadaState implements EstadoCitaState {

    @Override
    public void confirmar(Cita cita) {
        throw new ValidacionException("Una cita finalizada no puede ser modificada.");
    }

    @Override
    public void iniciarAtencion(Cita cita) {
        throw new ValidacionException("La cita medica ya ha finalizado.");
    }

    @Override
    public void finalizar(Cita cita) {
        throw new ValidacionException("La cita ya se encuentra finalizada.");
    }

    @Override
    public void cancelar(Cita cita, String motivo) {
        throw new ValidacionException("No es posible cancelar una cita medica que ya ha finalizado.");
    }

    @Override
    public void marcarNoAsistio(Cita cita) {
        throw new ValidacionException("No se puede marcar inasistencia sobre una cita ya finalizada.");
    }

    @Override
    public EstadoCitaEnum getEstadoEnum() {
        return EstadoCitaEnum.FINALIZADA;
    }
}

// Estado: Cancelada
class EstadoCanceladaState implements EstadoCitaState {

    @Override
    public void confirmar(Cita cita) {
        throw new ValidacionException("No se puede confirmar una cita cancelada.");
    }

    @Override
    public void iniciarAtencion(Cita cita) {
        throw new ValidacionException("No se puede iniciar atencion en una cita cancelada.");
    }

    @Override
    public void finalizar(Cita cita) {
        throw new ValidacionException("No se puede finalizar una cita cancelada.");
    }

    @Override
    public void cancelar(Cita cita, String motivo) {
        throw new ValidacionException("La cita ya se encuentra cancelada.");
    }

    @Override
    public void marcarNoAsistio(Cita cita) {
        throw new ValidacionException("La cita ya esta cancelada.");
    }

    @Override
    public EstadoCitaEnum getEstadoEnum() {
        return EstadoCitaEnum.CANCELADA;
    }
}

// Estado: No Asistio
class EstadoNoAsistioState implements EstadoCitaState {

    @Override
    public void confirmar(Cita cita) {
        throw new ValidacionException("El cliente no asistio a la cita.");
    }

    @Override
    public void iniciarAtencion(Cita cita) {
        throw new ValidacionException("No se puede atender una cita marcada como no asistida.");
    }

    @Override
    public void finalizar(Cita cita) {
        throw new ValidacionException("No se puede finalizar una cita con inasistencia.");
    }

    @Override
    public void cancelar(Cita cita, String motivo) {
        throw new ValidacionException("La cita ya fue registrada como no asistida.");
    }

    @Override
    public void marcarNoAsistio(Cita cita) {
        throw new ValidacionException("La cita ya esta marcada como no asistida.");
    }

    @Override
    public EstadoCitaEnum getEstadoEnum() {
        return EstadoCitaEnum.NO_ASISTIO;
    }
}
