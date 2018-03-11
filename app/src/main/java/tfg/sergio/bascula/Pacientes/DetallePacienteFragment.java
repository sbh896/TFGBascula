package tfg.sergio.bascula.Pacientes;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import tfg.sergio.bascula.Models.Paciente;
import tfg.sergio.bascula.R;

/**
 * Created by yeyo on 05/03/2018.
 */

public class DetallePacienteFragment extends Fragment {
    private String key = "";
    private TextView out_nombre;
    private ImageView out_perfil;
    DatabaseReference mDatabase;
    AlertDialog.Builder builder;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_detalle_paciente, null);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_update:
                return true;
            case R.id.action_delete:
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        key = bundle.getString("key");
        mDatabase = FirebaseDatabase.getInstance().getReference("pacientes");
        out_nombre = view.findViewById(R.id.txt_nombre);
        out_perfil = view.findViewById(R.id.foto_perfil);

        mDatabase.child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Paciente paciente = (Paciente) dataSnapshot.getValue(Paciente.class);
                if(paciente == null){
                    return;
                }
                out_nombre.setText(paciente.getNombre() +" "+paciente.getApellidos());
                //cargar Imagen
                Picasso.with(getActivity()).load(paciente.getUrlImagen()).into(out_perfil);



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Eliminar paciente");
        builder.setMessage("¿Está seguro de que desea eliminar al paciente seleccionado?");
        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mDatabase.child(key).removeValue();

                dialogInterface.dismiss();
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                PacientesFragment fragment = new PacientesFragment();
                ft.replace(R.id.pacientes_screen,fragment);
                ft.commit();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {


                dialogInterface.dismiss();
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }

}

