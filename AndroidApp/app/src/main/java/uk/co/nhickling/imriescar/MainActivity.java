package uk.co.nhickling.imriescar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void btnclick_ToggleNerdStats(View view){
        TableLayout layout = (TableLayout)findViewById(R.id.block_NerdStatsDetail);
        layout.setVisibility(layout.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
    }
}