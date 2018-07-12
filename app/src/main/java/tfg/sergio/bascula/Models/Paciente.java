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
    private String CodigoCentro;
    private Date FechaNacimiento;
    private int TipoDieta;
    private String CodigoUltimoRegistro = null;
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
        CodigoCentro = in.readString();
        TipoDieta = in.readInt();
        CodigoUltimoRegistro = in.readString();
        Sexo = in.readInt();
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

    public void setTipoDieta(int tipoDieta) {
        TipoDieta = tipoDieta;}
    public void setFechaNacimiento(Date fechaNacimiento) {this.FechaNacimiento = fechaNacimiento;}
    public void setCodigoCentro(String codigoCentro) {
        CodigoCentro = codigoCentro;
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
    public void setCodigoUltimoRegistro(String codigoUltimoRegistro) {
        CodigoUltimoRegistro = codigoUltimoRegistro;}


    public String getUrlImagen() { return UrlImagen; }
    public String getNombre() {return Nombre;}
    public String getApellidos() {return Apellidos;}
    public String getCodigoCentro() {return CodigoCentro;}
    public Date getFechaNacimiento() {return FechaNacimiento;}
    public int getTipoDieta() {return TipoDieta;}
    public String getCodigoUltimoRegistro() {return CodigoUltimoRegistro;}



    public Paciente(String n, String a, String id, String url, String codigoCentro, Date fecha, int dieta, String archivo, int sexo){
        this.Nombre=n;
        this.Apellidos = a;
        this.Id = id;
        this.UrlImagen = url;
        this.CodigoCentro = codigoCentro;
        this.FechaNacimiento = fecha;
        this.TipoDieta = dieta;
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
        parcel.writeString(CodigoCentro);
        parcel.writeInt(TipoDieta);
        parcel.writeString(CodigoUltimoRegistro);
    }


}
