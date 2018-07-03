package tfg.sergio.bascula.Resources;

/**
 * Created by sergio on 21/03/2018.
 */

public enum EnumIMC {
    OBESIDAD("Obesidad", 0),
    SOBREPESO("Sobrepeso", 1),
    NORMAL("Normal", 2),
    DESN("Desnutrición", 3),
    DESNMOD("Desnutrición moderada", 4),
    DESNSEV("Desnutrición severa", 5),
    TODOS("IMC",-1);

    private String stringValue;
    private int intValue;

    private EnumIMC(String toString, int value) {
        stringValue = toString;
        intValue = value;
    }

    @Override
    public String toString() {
        return stringValue;
    }

    public int getId(){
        return intValue;
    }

}
