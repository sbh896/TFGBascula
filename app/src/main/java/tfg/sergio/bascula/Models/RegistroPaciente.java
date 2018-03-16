package tfg.sergio.bascula.Models;

import java.util.Date;

/**
 * Created by sergio on 12/03/2018.
 */

public class RegistroPaciente {
    private String CodigoPaciente;
    private double Peso;
    private double Altura;
    private Date Fecha;

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
    }

}
