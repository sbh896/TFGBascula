package tfg.sergio.bascula.Calendario;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import tfg.sergio.bascula.R;

/**
 * Created by yeyo on 28/04/2018.
 */

public class CalendarioFragment extends Fragment {

    CompactCalendarView calendar;
    private SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/mm/yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        return inflater.inflate(R.layout.fragment_calendario, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ActionBar actionbar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(false);
        actionbar.setTitle(null);
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        calendar = view.findViewById(R.id.compactcalendar_view);
        calendar.setUseThreeLetterAbbreviation(true);
        String fecha = "30/04/2018";
        Date evento = null;
        try {
            evento = df.parse(fecha);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long time = evento.getTime();
        Event ev1 = new Event(Color.RED,time,"dia de prueba");
        calendar.addEvent(ev1);
        calendar.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                Context context = getActivity().getApplicationContext();
                //if(dateClicked.toString().compareTo())
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                actionbar.setTitle(formatoFecha.format(firstDayOfNewMonth));
            }
        });

    }
}
