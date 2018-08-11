package tfg.sergio.bascula;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class BajaUsuario extends AppCompatActivity {
    private EditText inputMail, inputPass;
    private FirebaseAuth auth;
    private ProgressBar progressBar;
    private Button btnBaja;
    private static final String TAG = "EmailPassword";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        auth = FirebaseAuth.getInstance();

        // Obtenemos elementos UI

        setContentView(R.layout.activity_baja_usuario);

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        //setSupportActionBar(toolbar);

        inputMail = (EditText) findViewById(R.id.email);
        inputPass = (EditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnBaja = (Button) findViewById(R.id.btn_baja);


        btnBaja.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = inputMail.getText().toString();
                final String pass = inputPass.getText().toString();
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Introduzca un email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(pass)) {
                    Toast.makeText(getApplicationContext(), "Introduzca una contraseña", Toast.LENGTH_SHORT).show();
                    return;
                }

                //Realizamos la autenticación empleando firebase
                auth.signInWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(BajaUsuario.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    FirebaseUser user = auth.getCurrentUser();
                                    user.delete()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(getApplicationContext(), "Cuenta de usuario borrada.", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                    finish();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(BajaUsuario.this, "Usuario o contraseña incorrectos",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

}
