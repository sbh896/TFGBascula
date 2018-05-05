package tfg.sergio.bascula.Models;

/**
 * Created by sergio on 02/05/2018.
 */

public class ElementoListadoAlerta {
    public Alerta alerta;
    public Paciente paciente;
    public RegistroPaciente ultimoRegistro;
    public String key;

    @Override
    public boolean equals(Object obj) {
        if (this.key.equals(((ElementoListadoAlerta)obj).key)) {
            return true;
        }

        return false;
    }
}
