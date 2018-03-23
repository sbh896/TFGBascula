package tfg.sergio.bascula.Models;

import java.util.Calendar;
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

    public int monthsBetweenDates() {
        int year = this.FechaNacimiento.getYear();
        int month = this.FechaNacimiento.getMonth();
        int day = this.FechaNacimiento.getDay();

        Calendar dob = Calendar.getInstance();
        dob.set(year, month, day);

        Calendar today = Calendar.getInstance();

        int monthsBetween = 0;
        int dateDiff = today.get(Calendar.DAY_OF_MONTH) - dob.get(Calendar.DAY_OF_MONTH);

        if (dateDiff < 0) {
            int borrrow = today.getActualMaximum(Calendar.DAY_OF_MONTH);
            dateDiff = (today.get(Calendar.DAY_OF_MONTH) + borrrow) - dob.get(Calendar.DAY_OF_MONTH);
            monthsBetween--;

            if (dateDiff > 0) {
                monthsBetween++;
            }
        } else {
            monthsBetween++;
        }
        monthsBetween += today.get(Calendar.MONTH) - dob.get(Calendar.MONTH);
        monthsBetween += (today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)) * 12;
        return monthsBetween;
    }

}
