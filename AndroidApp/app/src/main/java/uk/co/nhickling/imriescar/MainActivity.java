package uk.co.nhickling.imriescar;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.ViewSwitcher;

import java.util.Timer;

public class MainActivity extends AppCompatActivity implements CarBTWrapper.PacketHandlerInterface {

    private AppPreferences appPreferences = new AppPreferences();
    private CarBTWrapper carBT = new CarBTWrapper();

    private Timer tmrUpdate = new Timer();

    TextView label_Status;

    DeviceState carState;

    private void OnConnectionEstablished(){
    }

    private void OnConnectionLost(){
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        label_Status = (TextView)findViewById(R.id.label_Status);

        carBT.setEventHandler(this);
        carBT.Initialise();
    }

    @Override
    public void DeviceFound() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                label_Status.setText("Device has been found. Connecting...");
            }
        });
    }

    @Override
    public void DeviceConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                label_Status.setText("Device has connected. Waiting for data...");
            }
        });
    }

    @Override
    public void DeviceConnectionLost() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                label_Status.setText("Device connection Lost. Restarting...");
            }
        });
    }

    @Override
    public void NewDataRecieved(CarBTWrapper.DataPacket packet) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                label_Status.setText("Data Recieved! V: " + packet.Power_Volt);
            }
        });
    }
}