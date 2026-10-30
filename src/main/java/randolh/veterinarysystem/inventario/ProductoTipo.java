package randolh.veterinarysystem.inventario;

// Representa una categoria o tipo de producto farmaceutico
public class ProductoTipo {

    private int id;
    private String nombre;
    private String descripcion;

    public ProductoTipo() {}

    public ProductoTipo(int id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    @Override
    public String toString() {
        return String.format("[%d] %s", id, nombre);
    }
}
