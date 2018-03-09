package tfg.sergio.bascula.Pacientes;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import tfg.sergio.bascula.Models.Paciente;
import tfg.sergio.bascula.R;

/**
 * Created by yeyo on 05/03/2018.
 */

public class DetallePacienteFragment extends Fragment {
    private String key = "";
    private TextView out_nombre;
    private ImageView out_perfil;
    DatabaseReference dbpacientes;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_detalle_paciente, null);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        key = bundle.getString("key");
        dbpacientes = FirebaseDatabase.getInstance().getReference("pacientes");
        out_nombre = view.findViewById(R.id.txt_nombre);
        out_perfil = view.findViewById(R.id.foto_perfil);

        dbpacientes.child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Paciente paciente = (Paciente) dataSnapshot.getValue(Paciente.class);
                out_nombre.setText(paciente.getNombre() +" "+paciente.getApellidos());
                //cargar Imagen
                Picasso.with(getActivity()).load(paciente.getUrlImagen()).into(out_perfil);



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        super.onViewCreated(view, savedInstanceState);
    }
}

