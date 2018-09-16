package tfg.sergio.bascula.Centros;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import tfg.sergio.bascula.Adapters.AdapterCentro;
import tfg.sergio.bascula.Models.Centro;
import tfg.sergio.bascula.R;

/**
 * Created by Sergio Barrado on 25/02/2018.
 */

public class CentrosFragment extends Fragment {
    private RecyclerView listaCentros;
    private EditText buscador;
    private ArrayList<Centro> centros = new ArrayList<>();
    private DatabaseReference mDatabaseCentros;
    private ImageButton btnadd;

    RecyclerView.Adapter adapter;
    List<Centro> elementos = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_centros, null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDatabaseCentros = FirebaseDatabase.getInstance().getReference("centros");
        listaCentros = (RecyclerView) view.findViewById(R.id.lista_centros);

        btnadd = view.findViewById(R.id.btn_nuevo);
        btnadd.bringToFront();


        //listaCentros.setHasFixedSize(true);
        listaCentros.setLayoutManager(new GridLayoutManager(this.getActivity(),3));

        this.FireBaseCentrosSearch("");

        buscador = (EditText) view.findViewById(R.id.buscador);
        buscador.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                FireBaseCentrosSearch(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        view.findViewById(R.id.btn_nuevo).setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.addToBackStack("centros");
                AniadirCentroFragment fragment = new AniadirCentroFragment();
                ft.replace(R.id.centros_screen,fragment);
                ft.commit();

            }
        });
    }

    //region centros
    private void FireBaseCentrosSearch(String search) {

        elementos.clear();
        final Date[] fechaUltimoRegistro = {null};


        Query firebaseSearchQuery = mDatabaseCentros.orderByChild("Nombre").startAt(search).endAt(search + "\uf8ff");

        adapter = new AdapterCentro(elementos, getActivity(), getFragmentManager());

        listaCentros.setAdapter(adapter);

        firebaseSearchQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final Centro centro = dataSnapshot.getValue(Centro.class);
                final String centro_key = dataSnapshot.getKey();
                elementos.add(centro);
                adapter.notifyDataSetChanged();
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
}
