package tfg.sergio.bascula.Models;

import android.support.annotation.NonNull;

/**
 * Created by yeyo on 13/04/2018.
 */

public class Mes {
    public String Nombre;
    public String Id;
    public Mes(String n, String i){
        this.Id = i;
        this.Nombre = n;
    }
    public String toString(){
        return Nombre;
    }
}
