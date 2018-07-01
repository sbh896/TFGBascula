package tfg.sergio.bascula.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by sergio on 27/02/2018.
 */

public class Paciente implements Parcelable {

    public String getArchivoFoto() {
        return ArchivoFoto;
    }

    public void setArchivoFoto(String archivoFoto) {
        ArchivoFoto = archivoFoto;
    }


    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    private String Nombre;
    private String Apellidos;
    private String ArchivoFoto;
    private String Id;
    private String UrlImagen;
    private String Centro;
    private Date FechaNacimiento;
    private boolean EsDietaHipocalorica;
    private String UltimoRegistro = null;
    private String CodigoSilla = null;
    private int Sexo; //1 hombre, 2 mujer

    public int getSexo() {
        return Sexo;
    }

    public void setSexo(int sexo) {
        Sexo = sexo;
    }

    public String getCodigoSilla() {
        return CodigoSilla;
    }

    public void setCodigoSilla(String codigoSilla) {
        CodigoSilla = codigoSilla;
    }


    protected Paciente(Parcel in) {
        Nombre = in.readString();
        Apellidos = in.readString();
        Id = in.readString();
        UrlImagen = in.readString();
        Centro = in.readString();
        EsDietaHipocalorica = in.readByte() != 0;
        UltimoRegistro = in.readString();
    }

    public static final Creator<Paciente> CREATOR = new Creator<Paciente>() {
        @Override
        public Paciente createFromParcel(Parcel in) {
            return new Paciente(in);
        }

        @Override
        public Paciente[] newArray(int size) {
            return new Paciente[size];
        }
    };

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



    public Paciente(String n, String a, String id, String url, String centro, Date fecha, boolean dieta, String archivo, int sexo){
        this.Nombre=n;
        this.Apellidos = a;
        this.Id = id;
        this.UrlImagen = url;
        this.Centro = centro;
        this.FechaNacimiento = fecha;
        this.EsDietaHipocalorica = dieta;
        this.ArchivoFoto = archivo;
        this.Sexo = sexo;
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

    @Override
    public int describeContents() {
        return 0;
    }

    
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(Nombre);
        parcel.writeString(Apellidos);
        parcel.writeString(Id);
        parcel.writeString(UrlImagen);
        parcel.writeString(Centro);
        parcel.writeByte((byte) (EsDietaHipocalorica ? 1 : 0));
        parcel.writeString(UltimoRegistro);
    }


}
