package tfg.sergio.bascula.Resources;

/**
 * Created by yeyo on 24/03/2018.
 */

public enum EnumIMCFirebase {
    OBESIDAD("NumObesidad", 0),
    SOBREPESO("NumSobrepeso", 1),
    NORMAL("NumNormal", 2),
    DESN("NumDesnutrición", 3),
    DESNMOD("NumDesnutricionMod", 4),
    DESNSEV("NumDesnutricionSev", 5);

    private String stringValue;
    private int intValue;

    private EnumIMCFirebase(String toString, int value) {
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
