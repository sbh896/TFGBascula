package tfg.sergio.bascula.Centros;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
    private Context ctx = getActivity();
    private FragmentManager fragmentManager;
    private Bitmap imageBitmap;


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
        if(savedInstanceState != null){
            byte[] byteArray = savedInstanceState.getByteArray("imageBitmap");
            if(byteArray != null){
                imageBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                uriImagenAltaCalidad = Uri.parse(savedInstanceState.getString("imageUri"));
            }
        }
        if(imageBitmap!=null) {
            inputFoto.setScaleType(ImageView.ScaleType.FIT_XY);
            inputFoto.setImageBitmap(imageBitmap);
        }
        else if(centroOriginal.UrlImagen != null){
            inputFoto.setScaleType(ImageView.ScaleType.FIT_XY);
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
                fragmentManager = getActivity().getSupportFragmentManager();
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

            //imagen alta calidad
            inputFoto.setScaleType(ImageView.ScaleType.FIT_XY);

            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            InputStream imageStream = null;
            try {
                imageStream = getActivity().getContentResolver().openInputStream(uri);
                Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);
                img = rotateImageIfRequired(getActivity(), img, uri);
                inputFoto.setImageBitmap(img);
                imageBitmap = img;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            uriImagenAltaCalidad = uri;
        }
    }


    private static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }
    //endregion


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        if(imageBitmap != null){
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            imageBitmap.recycle();
            savedInstanceState.putByteArray("imageBitmap", byteArray);
            savedInstanceState.putString("imageUri", uriImagenAltaCalidad.toString());
        }
        super.onSaveInstanceState(savedInstanceState);
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
        centroOriginal.Nombre = inputCentro.getText().toString();
        centroOriginal.Direccion = inputDireccion.getText().toString();

        if(uriImagenAltaCalidad != null){
            progreso.show();
            if(centroOriginal.ArchivoFoto != null){

            StorageReference fotoEliminar = mStorage.child("Fotos_centros/"+ centroOriginal.ArchivoFoto);
                // Delete the file
                fotoEliminar.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                    }
                });
            }

            StorageReference path = mStorage.child("Fotos_centros").child(uriImagenAltaCalidad.getLastPathSegment());

            Bitmap bmp = null;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos);
            byte[] data = baos.toByteArray();
            //uploading the image
            UploadTask uploadTask2 = path.putBytes(data);
            uploadTask2.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Uri downloadUri = taskSnapshot.getDownloadUrl();
                    centroOriginal.UrlImagen = downloadUri.toString();
                    centroOriginal.ArchivoFoto = uriImagenAltaCalidad.getLastPathSegment();
                    mDatabaseCentros.child(centroOriginal.Id).setValue(centroOriginal);
                    progreso.dismiss();

                    fragmentManager.popBackStackImmediate();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progreso.dismiss();
                }
            });


        }else{
            mDatabaseCentros.child(centroOriginal.Id).setValue(centroOriginal);
            fragmentManager.popBackStackImmediate();

        }
        progreso.dismiss();

        fragmentManager.popBackStackImmediate();
    }
    //endregion
}
