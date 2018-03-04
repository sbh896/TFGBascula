package tfg.sergio.bascula.Models;

/**
 * Created by sergio on 27/02/2018.
 */

public class Paciente {
    private String Nombre;
    private String Apellidos;
    private String Id;
    private String UrlImagen;
    private String Centro;

    public void setCentro(String centro) {
        Centro = centro;
    }

    public void setUrlImagen(String urlImagen) {
        UrlImagen = urlImagen;
    }

    public void setNombre(String nombre) {
        Nombre = nombre;
    }

    public void setApellidos(String apellidos) {
        Apellidos = apellidos;
    }

    public String getUrlImagen() { return UrlImagen; }
    public String getNombre() {return Nombre;}
    public String getApellidos() {return Apellidos;}
    public String getCentro() {return Centro;}

    public Paciente(String n, String a, String id, String url, String centro){
        this.Nombre=n;
        this.Apellidos = a;
        this.Id = id;
        this.UrlImagen = url;
        this.Centro = centro;
    }
    public Paciente(){

    }
}
