package tfg.sergio.bascula.Models;

/**
 * Created by sergio on 27/02/2018.
 */

public class Paciente {
    private String Nombre;
    private String Apellidos;
    private String Id;
    public void setNombre(String nombre) {
        Nombre = nombre;
    }

    public void setApellidos(String apellidos) {
        Apellidos = apellidos;
    }

    public String getNombre() {
        return Nombre;
    }

    public String getApellidos() {
        return Apellidos;
    }
    public Paciente(String n, String a, String id){
        this.Nombre=n;
        this.Apellidos = a;
        this.Id = id;
    }
    public Paciente(){

    }
}
