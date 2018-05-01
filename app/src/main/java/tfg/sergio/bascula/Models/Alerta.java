package tfg.sergio.bascula.Models;

import java.util.Date;

/**
 * Created by yeyo on 01/05/2018.
 */

public class Alerta {
    public Date fechaInicio;
    public int periodica;     //0 => unica, 1 => semanal, 2 => mensual
    public int mail;         // 0 => no, 1 => si
    public String comentario;
}
