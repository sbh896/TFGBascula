package tfg.sergio.bascula.Models;

import java.util.Date;

/**
 * Created by sergio on 27/02/2018.
 */

public class Paciente {
    private String Nombre;
    private String Apellidos;
    private String Id;
    private String UrlImagen;
    private String Centro;
    private Date FechaNacimiento;
    private boolean EsDietaHipocalorica;
    private String UltimoRegistro = null;


    public void setEsDietaHipocalorica(boolean esDietaHipocalorica) {EsDietaHipocalorica = esDietaHipocalorica;}
    public void setFechaNacimiento(Date fechaNacimiento) {this.FechaNacimiento = fechaNacimiento;}
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
    public void setUltimoRegistro(String ultimoRegistro) {UltimoRegistro = ultimoRegistro;}


    public String getUrlImagen() { return UrlImagen; }
    public String getNombre() {return Nombre;}
    public String getApellidos() {return Apellidos;}
    public String getCentro() {return Centro;}
    public Date getFechaNacimiento() {return FechaNacimiento;}
    public boolean getEsDietaHipocalorica() {return EsDietaHipocalorica;}
    public String getUltimoRegistro() {return UltimoRegistro;}



    public Paciente(String n, String a, String id, String url, String centro, Date fecha, boolean dieta){
        this.Nombre=n;
        this.Apellidos = a;
        this.Id = id;
        this.UrlImagen = url;
        this.Centro = centro;
        this.FechaNacimiento = fecha;
        this.EsDietaHipocalorica = dieta;
    }
    public Paciente(){

    }

}
