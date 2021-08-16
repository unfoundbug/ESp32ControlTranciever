package uk.co.nhickling.imriescar;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.ViewSwitcher;
import android.widget.GridLayout;

import org.json.JSONException;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements CarBTWrapper.PacketHandlerInterface {

    private AppPreferences appPreferences = new AppPreferences();
    private CarBTWrapper carBT = new CarBTWrapper();

    private Timer tmrUpdate = new Timer();
    DeviceState carState;

    TextView label_Status;
    private GaugeView g_volt, g_amp;
    private Switch sw_Pedal, sw_Gear, sw_Switch, sw_Manual, swRemoteControl;
    TextView tv_nerd_volt, tv_nerd_amp, tv_nerd_Ping, tv_nerd_Sig;
    private GridLayout grid_gauges, grid_stats, grid_controls;

    private ImageButton btn_Forward, btn_Reverse, btn_Left, btn_Right;
    private ImageButton btn_FwLe, btn_FwRi, btn_ReLe, btn_ReRi;

    TimerTask sendUpdate = new TimerTask() {
        @Override
        public void run() {
            try {
                if(swRemoteControl.isChecked()){
                    int targetDrive = 0;
                    int targetSteer = 0;
                    if(btn_Forward.isPressed())
                        targetDrive += 1;
                    if(btn_Reverse.isPressed())
                        targetDrive -= 1;
                    if(btn_Left.isPressed())
                        targetSteer += 1;
                    if(btn_Right.isPressed())
                        targetSteer -= 1;
                    if(btn_FwLe.isPressed()){
                        targetDrive += 1;
                        targetSteer += 1;
                    }
                    if(btn_FwRi.isPressed()){
                        targetDrive += 1;
                        targetSteer -= 1;
                    }
                    if(btn_ReLe.isPressed()){
                        targetDrive -= 1;
                        targetSteer += 1;
                    }
                    if(btn_ReRi.isPressed()){
                        targetDrive -= 1;
                        targetSteer -= 1;
                    }
                    carState = DeviceState.SetRemoteControl(targetDrive,targetSteer,false);
                }
                else
                {
                    carState = DeviceState                                                           .SetLocalControl();
                }
                final long tts = carBT.SendUpdate(carState);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_nerd_Ping.setText(Long.toString(tts) + "ms");
                    }
                });


            } catch (JSONException e) {
                e.printStackTrace();
            } catch (java.io.IOException ex)
            {
                ex.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        label_Status = (TextView)findViewById(R.id.label_Status);

        carBT.setEventHandler(this);

        bindWidgets();
        tmrUpdate.scheduleAtFixedRate(sendUpdate,250, 250);
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
                grid_gauges.setVisibility(View.VISIBLE);
                grid_stats.setVisibility(View.VISIBLE);
                grid_controls.setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    public void DeviceConnectionLost() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                label_Status.setText("Device connection Lost. Restarting...");
                grid_gauges.setVisibility(View.GONE);
                grid_stats.setVisibility(View.GONE);
                grid_controls.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void NewDataRecieved(CarBTWrapper.DataPacket packet) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                label_Status.setText("Data Recieved! V: " + packet.Power_Volt);
                sw_Pedal.setChecked(packet.Controls_Pedal);
                sw_Gear.setChecked(packet.Controls_Gear);
                sw_Switch.setChecked(packet.Controls_Switch);
                sw_Manual.setChecked(packet.Controls_Manual);
                tv_nerd_volt.setText("Voltage: " + packet.Power_Volt + "V");
                tv_nerd_amp.setText("Current: " + packet.Power_Amp + "A");
                g_volt.SetValue((float)packet.Power_Volt);
                g_amp.SetValue((float)packet.Power_Amp);

                label_Status.setText("Data Recieved! V: " + packet.Power_Volt);
            }
        });
    }

    private void bindWidgets()
    {
        this.grid_gauges = (GridLayout)findViewById(R.id.view_gauges);
        this.grid_stats = (GridLayout)findViewById(R.id.view_stats);
        this.grid_controls = (GridLayout)findViewById(R.id.view_controls);

        this.g_volt = (GaugeView)findViewById(R.id.gauge_Volt);
        this.g_amp = (GaugeView)findViewById(R.id.gauge_Amp);

        this.sw_Pedal = (Switch)findViewById(R.id.sw_nerd_pedal);
        this.sw_Gear = (Switch)findViewById(R.id.sw_nerd_gear);
        this.sw_Switch = (Switch)findViewById(R.id.sw_nerd_switch);
        this.sw_Manual = (Switch)findViewById(R.id.sw_nerd_manual);
        this.swRemoteControl = (Switch)findViewById(R.id.sw_RemoteControl);

        this.tv_nerd_volt = (TextView)findViewById(R.id.lbl_Volt);
        this.tv_nerd_amp = (TextView)findViewById(R.id.lbl_Amp);
        this.tv_nerd_Ping = (TextView)findViewById(R.id.lbl_Ping);
        this.tv_nerd_Sig = (TextView)findViewById(R.id.lbl_SigStr);

        this.btn_Forward = (ImageButton)findViewById(R.id.btn_Forward);
        this.btn_Reverse = (ImageButton)findViewById(R.id.btn_Reverse);
        this.btn_Left = (ImageButton)findViewById(R.id.btn_Left);
        this.btn_Right = (ImageButton)findViewById(R.id.btn_Right);

        this.btn_FwLe = (ImageButton)findViewById(R.id.btn_ForwardLeft);
        this.btn_FwRi = (ImageButton)findViewById(R.id.btn_ForwardRight);
        this.btn_ReLe = (ImageButton)findViewById(R.id.btn_ReverseLeft);
        this.btn_ReRi = (ImageButton)findViewById(R.id.btn_ReverseRight);
    }
}