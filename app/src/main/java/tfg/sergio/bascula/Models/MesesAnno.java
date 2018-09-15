package tfg.sergio.bascula.Models;

import java.util.ArrayList;

/**
 * Created by Sergio Barrado on 13/04/2018.
 */

public class MesesAnno {
    public ArrayList<Mes>mesesAnio = new ArrayList();

    public MesesAnno(){
        String[] meses = {"Enero","Febrero","Marzo","Abril","Mayo","Junio","Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"};
        int i =1;
        for (String mes:meses) {
            mesesAnio.add(new Mes(mes,String.format("%02d",i)));
            i++;
        }
    }

}
