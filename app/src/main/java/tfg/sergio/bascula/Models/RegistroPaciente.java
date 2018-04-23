package tfg.sergio.bascula.Models;

import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sergio on 12/03/2018.
 */

public class RegistroPaciente implements Comparable<RegistroPaciente>{
    public String getCodigoRegistro() {
        return codigoRegistro;
    }

    public void setCodigoRegistro(String codigoRegistro) {
        this.codigoRegistro = codigoRegistro;
    }

    private String codigoRegistro;
    private String CodigoPaciente;
    private double Peso=0;
    private double Altura=0;
    private Date Fecha;
    private String StrFecha;

    public void setStrFecha(String strFecha) {
        StrFecha = strFecha;
    }

    public String getStrFecha() {
        return StrFecha;
    }

    public void setCodigoPaciente(String codigoPaciente) {
        CodigoPaciente = codigoPaciente;
    }

    public void setPeso(double peso) {
        Peso = peso;
    }

    public void setAltura(double altura) {
        Altura = altura;
    }

    public void setFecha(Date fecha) {
        Fecha = fecha;
    }

    public String getCodigoPaciente() {
        return CodigoPaciente;
    }

    public double getPeso() {
        return Peso;
    }

    public double getAltura() {
        return Altura;
    }

    public Date getFecha() {
        return Fecha;
    }


    public RegistroPaciente(){

    }
    public RegistroPaciente(String codigoPaciente, double peso, double altura, Date fecha){
        this.CodigoPaciente = codigoPaciente;
        this.Peso = peso;
        this.Altura = altura;
        this.Fecha = fecha;
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
        this.StrFecha = formatter.format(fecha);
    }

    @Override
    public int compareTo(@NonNull RegistroPaciente registroPaciente) {
        if(this.Fecha == null || registroPaciente.getFecha() == null){
            return 0;
        }
        return this.Fecha.compareTo(registroPaciente.getFecha());
    }

    public float getIMC(){
        if (this.Altura == 0 ){
            return 0;
        }
        return (float) (this.Peso/Math.pow(this.Altura,2));
    }
}
