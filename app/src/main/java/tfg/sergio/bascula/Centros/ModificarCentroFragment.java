package tfg.sergio.bascula.Centros;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import tfg.sergio.bascula.Models.Centro;
import tfg.sergio.bascula.Models.PacientesMesCentro;
import tfg.sergio.bascula.R;

/**
 * Created by yeyo on 24/03/2018.
 */

public class ModificarCentroFragment extends Fragment {

    private EditText inputCentro, inputDireccion;
    private ImageButton inputFoto;
    private Uri uriImagenAltaCalidad = null;
    private static final int CAMERA_REQUEST_CODE = 1;
    //almacenamiento firebase
    private StorageReference mStorage;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseCentros, mDatabaseDatosMes;
    private ProgressDialog progreso;
    private Centro centroOriginal;
    AlertDialog.Builder alert;

    private int PICK_IMAGE_REQUEST = 1;
    // Permiso de almacenamiento
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        return inflater.inflate(R.layout.fragment_aniadir_centro, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        centroOriginal = bundle.getParcelable("centro");

        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference("pacientes");
        mDatabaseCentros = FirebaseDatabase.getInstance().getReference("centros");
        mDatabaseDatosMes = FirebaseDatabase.getInstance().getReference("pacientesMes");
        inputCentro = (EditText) view.findViewById(R.id.nombre);
        inputDireccion = view.findViewById(R.id.direccion);
        inputFoto = (ImageButton) view.findViewById(R.id.imagen_paciente);
        progreso = new ProgressDialog(getActivity());

        inputCentro.setText(centroOriginal.Nombre);
        inputDireccion.setText(centroOriginal.Direccion);

        if(centroOriginal.UrlImagen != null){
            Picasso.with(getActivity()).load(centroOriginal.UrlImagen).resize(200,200).into(inputFoto);
        }

        alert = new AlertDialog.Builder(getActivity());
        alert.setTitle("Añadir centro");
        alert.setMessage("¿Desea añadir el centro sin dirección?");
        alert.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                guardarCentro();
            }
        });
        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();
            }
        });
        //Guardado
        view.findViewById(R.id.btn_guardar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(inputCentro.getText().toString())) {
                    Toast.makeText(getActivity(), "Introduzca un nombre.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(inputDireccion.getText().toString())){
                    AlertDialog alertar = alert.create();
                    alertar.show();
                }
                else{
                    guardarCentro();
                }
            }
        });

        //Botón foto
        inputFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                // Show only images, no videos or anything else
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });



    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                // Log.d(TAG, String.valueOf(bitmap));
                inputFoto.setImageBitmap(bitmap);
                uriImagenAltaCalidad = uri;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Verificador de permisos de la app para usar almacenamiento interno
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    //region Firebase
    private void  guardarCentro(){
        final String nombre = inputCentro.getText().toString();
        final String direccion = inputDireccion.getText().toString();


        Bitmap bmp = null;
        try {
            bmp = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uriImagenAltaCalidad);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos);
            byte[] data = baos.toByteArray();
            //uploading the image
            StorageReference path = mStorage.child("Fotos_centros").child(uriImagenAltaCalidad.getLastPathSegment());
            UploadTask uploadTask2 = path.putBytes(data);
            uploadTask2.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    DateFormat dateFormat = new SimpleDateFormat("YYYYMM");
                    Date date = new Date();

                    //Guardado del centro en Firebase
                    progreso.setMessage("Guardando centro ...");
                    progreso.show();

                    String id = mDatabaseCentros.push().getKey();
                    String id2 = mDatabaseDatosMes.push().getKey();
                    Centro centro = new Centro(id,nombre);
                    centro.Direccion = direccion;
                    centro.UrlImagen = taskSnapshot.getDownloadUrl().toString();
                    String ident = id + dateFormat.format(date);
                    PacientesMesCentro pmc= new PacientesMesCentro(ident);

                    mDatabaseDatosMes.child(id2).setValue(pmc);
                    mDatabaseCentros.child(id).setValue(centro);
                    progreso.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progreso.dismiss();
                }
            });


        } catch (IOException e) {
            e.printStackTrace();
        }




        progreso.dismiss();


        FragmentManager fm = getFragmentManager();
        fm.popBackStackImmediate();
    }
    //endregion
}
