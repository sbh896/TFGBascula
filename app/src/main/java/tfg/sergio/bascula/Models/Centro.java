package tfg.sergio.bascula.Models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Sergio Barrado on 04/03/2018.
 */

public class Centro implements Parcelable{
    public String Id;
    public String Nombre;
    public String Direccion;
    public String UrlImagen;
    public String ArchivoFoto;

    public Centro(){

    }
    public Centro(String id, String nombre){
        this.Id = id;
        this.Nombre = nombre;
    }

    public String toString(){
        return Nombre;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(Id);
        parcel.writeString(Nombre);
        parcel.writeString(Direccion);
        parcel.writeString(UrlImagen);
    }
}
