package tfg.sergio.bascula.Models;

/**
 * Created by yeyo on 04/03/2018.
 */

public class Centro {
    private String Id,Nombre;

    public String getId() {
        return Id;
    }

    public String getNombre() {
        return Nombre;
    }


    public void setId(String id) {
        this.Id = id;
    }

    public void setNombre(String nombre) {
        this.Nombre = nombre;
    }
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
