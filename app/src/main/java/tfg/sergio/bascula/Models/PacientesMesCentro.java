package tfg.sergio.bascula.Models;

/**
 * Created by Sergio Barrado on 25/03/2018.
 */

public class PacientesMesCentro {
    public String id;
    public int NumObesidad ;
    public int NumSobrepeso;
    public int NumNormal;
    public int NumDesnutricion;
    public int NumDesnutricionMod;
    public int NumDesnutricionSev;

    public  PacientesMesCentro(){

    }
    public PacientesMesCentro(String ident){
            id = ident;
            NumObesidad = 0;
            NumSobrepeso = 0;
            NumNormal = 0;
            NumDesnutricion = 0;
            NumDesnutricionMod = 0;
            NumDesnutricionSev = 0;
        }

    public int obtenerNumPacientes(int tipoEstado){
        switch (tipoEstado){
            case 0:
                return NumObesidad;
            case 1:
                return NumSobrepeso;
            case 2:
                return NumNormal;
            case 3:
                return NumDesnutricion;
            case 4:
                return NumDesnutricionMod;
            case 5:
                return NumDesnutricionSev;
        }
        return -1;
    }

    public void ActualizarNumPacientes(int tipoEstado, int tipo){
        switch (tipoEstado){
            case 0:
                this.NumObesidad += tipo == 1 ? 1 : (-1);
                break;
            case 1:
                this.NumSobrepeso += tipo == 1 ? 1 : (-1);
                break;
            case 2:
                this.NumNormal += tipo == 1 ? 1 : (-1);
                break;
            case 3:
                this.NumDesnutricion += tipo == 1 ? 1 : (-1);
                break;
            case 4:
                this.NumDesnutricionMod += tipo == 1 ? 1 : (-1);
                break;
            case 5:
                this.NumDesnutricionSev+= tipo == 1 ? 1 : (-1);
                break;
        }
    }
}
