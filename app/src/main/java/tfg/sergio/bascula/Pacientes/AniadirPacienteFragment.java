package tfg.sergio.bascula.Pacientes;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;

import android.text.format.Time;

import tfg.sergio.bascula.Models.Centro;
import tfg.sergio.bascula.Models.Paciente;
import tfg.sergio.bascula.R;

/**
 * Created by sergio on 27/02/2018.
 */

public class AniadirPacienteFragment extends Fragment {
    private EditText inputNombre,inputApellido;
    private ImageButton foto;
    private Uri uriImagenAltaCalidad = null;
    private static final int CAMERA_REQUEST_CODE = 1;
    //almacenamiento firebase
    private StorageReference mStorage;
    DatabaseReference mDatabase;
    private DatabaseReference mDatabaseCentros;

    private Spinner mSpinner;
    private ArrayList<Centro>centros = new ArrayList<>();

    private ProgressDialog progreso;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        return inflater.inflate(R.layout.fragment_aniadir_paciente, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference("pacientes");
        mDatabaseCentros = FirebaseDatabase.getInstance().getReference("centros");
        mSpinner = (Spinner) view.findViewById(R.id.sp_centros);

        inputNombre = (EditText) view.findViewById(R.id.nombre);
        inputApellido = (EditText) view.findViewById(R.id.apellidos);
        foto = (ImageButton) view.findViewById(R.id.imagen_paciente);

        progreso = new ProgressDialog(getActivity());

        view.findViewById(R.id.btn_guardar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nombre = inputNombre.getText().toString();
                String apellidos = inputApellido.getText().toString();

                if (TextUtils.isEmpty(nombre)) {
                    Toast.makeText(getActivity(), "Introduzca un nombre.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(apellidos)) {
                    Toast.makeText(getActivity(), "Introduzca los apellidos.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(uriImagenAltaCalidad == null) {
                    Toast.makeText(getActivity(), "Introduzca una imagen de usuario.", Toast.LENGTH_SHORT).show();

                }
                if(mSpinner.getSelectedItemPosition() == 0){
                    Toast.makeText(getActivity(), "Seleccione un centro.", Toast.LENGTH_SHORT).show();
                }

                guardarPaciente(nombre,apellidos);
            }
        });

        foto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uriImagenAltaCalidad = generateTimeStampPhotoFileUri();

                Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                camera_intent.putExtra(MediaStore.EXTRA_OUTPUT,uriImagenAltaCalidad);
                startActivityForResult(camera_intent,CAMERA_REQUEST_CODE);
            }
        });

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                Centro c = (Centro)mSpinner.getSelectedItem();
                Toast.makeText(getActivity(), c.getId(), Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        this.obtenerCentros();

    }

    private void obtenerCentros(){
        ArrayAdapter<Centro> arrayAdapter = new ArrayAdapter<Centro>(getActivity(),android.R.layout.simple_spinner_dropdown_item,centros){
            @Override
            public boolean isEnabled(int position) {
                if(position == 0)
                {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return false;
                }
                else
                {
                    return true;
                }
            }

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;

            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }


        };
        centros.add(new Centro("-1","Seleccionar centro..."));
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpinner.setAdapter(arrayAdapter);
        mDatabaseCentros.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String value = dataSnapshot.getValue(String.class);
                String key = dataSnapshot.getKey();
                centros.add(new Centro(key,value));

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void  guardarPaciente(final String nombre,final String apellidos){
        progreso.setMessage("Guardando paciente ...");
        progreso.show();
        StorageReference path = mStorage.child("Fotos_pacientes").child(uriImagenAltaCalidad.getLastPathSegment());
        path.putFile(uriImagenAltaCalidad).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUri = taskSnapshot.getDownloadUrl();
                Centro c = (Centro)mSpinner.getSelectedItem();

                String id = mDatabase.push().getKey();
                Paciente paciente = new Paciente(nombre,apellidos,id,downloadUri.toString(),c.getId());
                mDatabase.child(id).setValue(paciente);
                progreso.dismiss();

            }
        });

        FragmentManager fm = getFragmentManager();
        fm.popBackStackImmediate();

    }

    private Uri generateTimeStampPhotoFileUri() {

        Uri photoFileUri = null;
        File outputDir = getPhotoDirectory();
        if (outputDir != null) {
            Time t = new Time();
            t.setToNow();
            File photoFile = new File(outputDir, System.currentTimeMillis() + ".jpg");
            photoFileUri = Uri.fromFile(photoFile);
        }
        return photoFileUri;
    }

    private File getPhotoDirectory() {
        File outputDir = null;
        String externalStorageStagte = Environment.getExternalStorageState();
        if (externalStorageStagte.equals(Environment.MEDIA_MOUNTED)) {
            File photoDir = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            outputDir = new File(photoDir, getString(R.string.app_name));
            if (!outputDir.exists())
                if (!outputDir.mkdirs()) {
                    Toast.makeText(getActivity(),"Error al crear directorio "+ outputDir.getAbsolutePath(),Toast.LENGTH_SHORT).show();
                    outputDir = null;
                }
        }
        return outputDir;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK){

            //imagen alta calidad
            foto.setImageURI(uriImagenAltaCalidad);


        }
    }
}
