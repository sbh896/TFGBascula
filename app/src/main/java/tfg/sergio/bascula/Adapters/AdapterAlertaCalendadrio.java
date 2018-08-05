package tfg.sergio.bascula.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import tfg.sergio.bascula.Models.ElementoListadoAlerta;
import tfg.sergio.bascula.R;

/**
 * Created by yeyo on 24/03/2018.
 */

public class AdapterAlertaCalendadrio extends RecyclerView.Adapter<AdapterAlertaCalendadrio.PacientesViewHolder>{

    List<ElementoListadoAlerta> alertas;
    Context ctx;

    public AdapterAlertaCalendadrio(List<ElementoListadoAlerta> listado, Context c){
        this.alertas = listado;
        this.ctx = c;
    }

    @Override
    public PacientesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.calendario_list_layout, parent, false);
        PacientesViewHolder holder = new PacientesViewHolder(v,parent.getContext());
        return holder;
    }

    @Override
    public void onBindViewHolder(PacientesViewHolder holder, int position) {
        final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");

        final ElementoListadoAlerta alerta = alertas.get(position);
        holder.paciente_nombre.setText(alerta.paciente.getNombre() + " " + alerta.paciente.getApellidos());
        //holder.paciente_fecha.setText(alerta.ultimoRegistro == null ? "-":formatter.format(alerta.ultimoRegistro.getFecha()));
        holder.alerta_fecha.setText(formatter.format(alerta.alerta.fechaInicio));

    }

    @Override
    public int getItemCount() {
        return alertas.size();
    }

    public static class PacientesViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        Context context;
        TextView paciente_nombre, paciente_fecha, alerta_fecha, peso, fechaRegistro;

        public PacientesViewHolder(View itemView, Context ctx) {
            super(itemView);
            mView = itemView;
            paciente_nombre =  (TextView) mView.findViewById(R.id.nombre);
            alerta_fecha = mView.findViewById(R.id.txt_tipo_alerta);

            context = ctx;

        }

        public void setDetails(Context ctx, ElementoListadoAlerta alerta){
            final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
            paciente_nombre =  (TextView) mView.findViewById(R.id.nombre);
            alerta_fecha = mView.findViewById(R.id.txt_tipo_alerta);



            paciente_nombre.setText(alerta.paciente.getNombre() + " " + alerta.paciente.getApellidos());

            paciente_fecha.setText(alerta.ultimoRegistro== null ? "-":formatter.format(alerta.ultimoRegistro.getFecha()));

        }
    }
}
