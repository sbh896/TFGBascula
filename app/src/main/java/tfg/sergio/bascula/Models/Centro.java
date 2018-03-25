package tfg.sergio.bascula.Models;

/**
 * Created by yeyo on 04/03/2018.
 */

public class Centro {
    public String Id;
    public String Nombre;

    public Centro(){

    }
    public Centro(String id, String nombre){
        this.Id = id;
        this.Nombre = nombre;
    }

    public String toString(){
        return Nombre;
    }


}
