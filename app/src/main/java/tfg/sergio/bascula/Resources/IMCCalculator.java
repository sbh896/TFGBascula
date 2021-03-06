package tfg.sergio.bascula.Resources;

import java.util.Arrays;

/**
 * Created by Sergio Barrado on 18/03/2018.
 */

public class IMCCalculator {
    private static int[] meses = {60,66,72,78,84,90,96,102,108,114,120,126,132,138,144,150,156,162,168,174,180,186,192,198,204,210,216};
    private static double[] femObs = { 18.9 ,19.0 ,19.2 ,19.5 ,19.8 ,20.1 ,20.6 ,21.0 ,21.5 ,22.0 ,22.6 ,23.1 ,23.7 ,24.3 ,25.0 ,25.6 ,26.2 ,26.8 ,27.3 ,27.8 ,28.2 ,28.6 ,28.9 ,29.1 ,29.3 ,29.4 ,29.5};
    private static double[] femSob = { 16.9 ,16.9 ,17.0 ,17.1 ,17.3 ,17.5 ,17.7 ,18.0 ,18.3 ,18.7 ,19.0 ,19.4 ,19.9 ,20.3 ,20.8 ,21.3 ,21.8 ,22.3 ,22.7 ,23.1 ,23.5 ,23.8 ,24.1 ,24.3 ,24.5 ,24.6 ,24.8};
    private static double[] femNor = { 13.9 ,13.9 ,13.9 ,13.9 ,13.9 ,14.0 ,14.1 ,14.3 ,14.4 ,14.6 ,14.8 ,15.1 ,15.3 ,15.6 ,16.0 ,16.3 ,16.6 ,16.9 ,17.2 ,17.5 ,17.8 ,18.0 ,18.2 ,18.3 ,18.4 ,18.5 ,18.6};
    private static double[] femDes = { 12.7 ,12.7 ,12.7 ,12.7 ,12.7 ,12.8 ,12.9 ,13.0 ,13.1 ,13.3 ,13.5 ,13.7 ,13.9 ,14.1 ,14.4 ,14.7 ,14.9 ,15.2 ,15.4 ,15.7 ,15.9 ,16.0 ,16.2 ,16.3 ,16.4 ,16.4 ,16.4};
    private static double[] femDesMod = { 11.8 ,11.7 ,11.7 ,11.7 ,11.8 ,11.8 ,11.9 ,12.0 ,12.1 ,12.2 ,12.4 ,12.5 ,12.7 ,12.9 ,13.2 ,13.4 ,13.6 ,13.8 ,14.0 ,14.2 ,14.4 ,14.5 ,14.6 ,14.7 ,14.7 ,14.7 ,14.7};
    private static double[] femDesSev = { 11.8 ,11.7 ,11.7 ,11.7 ,11.8 ,11.8 ,11.9 ,12.0 ,12.1 ,12.2 ,12.4 ,12.5 ,12.7 ,12.9 ,13.2 ,13.4 ,13.6 ,13.8 ,14.0 ,14.2 ,14.4 ,14.5 ,14.6 ,14.7 ,14.7 ,14.7 ,14.7};

    private static double[] mascObs = { 18.3 ,18.4 ,18.5 ,18.7 ,19.0 ,19.3 ,19.7 ,20.1 ,20.5 ,20.9 ,21.4 ,21.9 ,22.5 ,23.0 ,23.6 ,24.2 ,24.8 ,25.3 ,25.9 ,26.5 ,27.0 ,27.4 ,27.9 ,28.3 ,28.6 ,29.0 ,29.2};
    private static double[] mascSob = { 16.6 ,16.7 ,16.8 ,16.9 ,17.0 ,17.2 ,17.4 ,17.7 ,17.9 ,18.2 ,18.5 ,18.8 ,19.2 ,19.5 ,19.9 ,20.4 ,20.8 ,21.3 ,21.8 ,22.2 ,22.7 ,23.1 ,23.5 ,23.9 ,24.3 ,24.6 ,24.9};
    private static double[] mascNor = { 14.1, 14.1, 14.1, 14.1, 14.2, 14.3, 14.4, 14.5, 14.6, 14.8 ,14.9, 15.1, 15.3, 15.5, 15.8, 16.1, 16.4, 16.7, 17.0, 17.3, 17.6, 18.0, 18.2, 18.5, 18.8, 19.0, 19.2};
    private static double[] mascDes = { 13.0 ,13.0 ,13.0 ,13.1 ,13.1 ,13.2 ,13.3 ,13.4 ,13.5 ,13.6 ,13.7 ,13.9 ,14.1 ,14.2 ,14.5 ,14.7 ,14.9 ,15.2 ,15.5 ,15.7 ,16.0 ,16.3 ,16.5 ,16.7 ,16.9 ,17.1 ,17.3};
    private static double[] mascDesMod = { 12.1 ,12.1 ,12.1 ,12.2 ,12.3 ,12.3 ,12.4 ,12.5 ,12.6 ,12.7 ,12.8 ,12.9 ,13.1 ,13.2 ,13.4 ,13.6 ,13.8 ,14.0 ,14.3 ,14.5 ,14.7 ,14.9 ,15.1 ,15.3 ,15.4 ,15.6 ,15.7};
    private static double[] mascDesSev = { 12.1 ,12.1 ,12.1 ,12.2 ,12.3 ,12.3 ,12.4 ,12.5 ,12.6 ,12.7 ,12.8 ,12.9 ,13.1 ,13.2 ,13.4 ,13.6 ,13.8 ,14.0 ,14.3 ,14.5 ,14.7 ,14.9 ,15.1 ,15.3 ,15.4 ,15.6 ,15.7};
    public IMCCalculator(){
    }

    public static int Calcular(int m, float IMC, int sexo){
        int i=0;
        if(sexo == 2){
            for(int mes : meses){
                if(m <= mes || i == meses.length -1){
                    if(IMC >= femObs[i]){
                        return 0;
                    }else if(IMC >= femSob[i]){
                        return 1;
                    }else if(IMC >= femNor[i]){
                        return 2;
                    }else if(IMC >= femDes[i]){
                        return 3;
                    }else if(IMC >= femDesMod[i]){
                        return 4;
                    }else if(IMC < femDesSev[i]){
                        return 5;
                    }
                }
                i++;
            }
        }
        else if(sexo == 1){
            for(int mes : meses){
                if(m <= mes || i == meses.length -1){
                    if(IMC >= mascObs[i]){
                        return 0;
                    }else if(IMC >= mascSob[i]){
                        return 1;
                    }else if(IMC >= mascNor[i]){
                        return 2;
                    }else if(IMC >= mascDes[i]){
                        return 3;
                    }else if(IMC >= mascDesMod[i]){
                        return 4;
                    }else if(IMC < mascDesSev[i]){
                        return 5;
                    }
                }
                i++;
            }
        }
        return -1;
    }

    public static double CalcularIMC(Double peso, Double altura){
        if(altura == 0){
            return 0;
        }
        return (peso/Math.pow(altura,2));
    }

}
