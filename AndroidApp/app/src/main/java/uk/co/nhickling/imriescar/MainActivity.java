package uk.co.nhickling.imriescar;

import androidx.appcompat.app.AppCompatActivity;

import android.opengl.Visibility;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;

import org.json.JSONException;

import java.util.Timer;
import java.util.TimerTask;

import uk.co.nhickling.imriescar.CarBTWrapper.DeviceState;

public class MainActivity extends AppCompatActivity implements CarBTWrapper.PacketHandlerInterface {

    private AppPreferences appPreferences = new AppPreferences();
    private CarBTWrapper carBT = new CarBTWrapper();

    private SeekBar sbSpeedLimit = null;
    private TextView tvSpeedLimit = null;

    private GaugeView g_volt, g_amp;

    private Switch sw_Pedal, sw_Gear, sw_Switch, sw_Manual, swRemoteControl;
    TextView tv_nerd_volt, tv_nerd_amp, tv_nerd_Ping, tv_nerd_Sig;

    private LinearLayout nerdStats = null;
    private TableLayout nerdStatDetail = null;

    private LinearLayout blockHeader, blockControls, blockFooter;

    private ImageButton btn_Forward, btn_Reverse, btn_Left, btn_Right;

    private ImageButton btn_FwLe, btn_FwRi, btn_ReLe, btn_ReRi;

    private Timer tmrUpdate = new Timer();

    DeviceState carState;

    private void OnConnectionEstablished(){
        nerdStats.setVisibility(View.VISIBLE);
        blockControls.setVisibility(View.VISIBLE);
        blockFooter.setVisibility(View.VISIBLE);
        blockHeader.setVisibility(View.GONE);

    }

    private void OnConnectionLost(){

        blockHeader.setVisibility(View.VISIBLE);
        nerdStats.setVisibility(View.GONE);
        blockControls.setVisibility(View.GONE);
        blockFooter.setVisibility(View.GONE);
    }

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
                    carState.SetRemoteControl(targetDrive,targetSteer,false);
                }
                else
                {
                    carState.SetLocalControl();
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

        blockHeader = (LinearLayout)findViewById(R.id.block_Header);
        nerdStats = (LinearLayout)findViewById(R.id.block_NerdStats);
        blockControls = (LinearLayout)findViewById(R.id.block_Controls);
        blockFooter = (LinearLayout)findViewById(R.id.block_Footer);
        nerdStatDetail = (TableLayout)findViewById(R.id.block_NerdStatsDetail);

        //sbSpeedLimit = (SeekBar)findViewById(R.id.sb_SpeedLimit);
        //tvSpeedLimit = (TextView)findViewById(R.id.lbl_Speed);

        this.g_volt = (GaugeView)findViewById(R.id.gauge_Volt);
        this.g_amp = (GaugeView)findViewById(R.id.gauge_Amp);


        this.appPreferences.Initialise(getSharedPreferences("AppSettings", MODE_PRIVATE));
        this.nerdStatDetail.setVisibility(this.appPreferences.GetShowNerd() ? View.VISIBLE : View.GONE);
        /*this.sbSpeedLimit.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                appPreferences.SetSpeedLimit(seekBar.getProgress());
                tvSpeedLimit.setText("Speed Limit:" + appPreferences.GetSpeedLimit() + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        this.sbSpeedLimit.setProgress(this.appPreferences.m_iSpeedLimit);
        */
        this.carBT.setEventHandler(this);

        sw_Pedal = (Switch)findViewById(R.id.sw_nerd_pedal);
        sw_Gear = (Switch)findViewById(R.id.sw_nerd_gear);
        sw_Switch = (Switch)findViewById(R.id.sw_nerd_switch);
        sw_Manual = (Switch)findViewById(R.id.sw_nerd_manual);
        swRemoteControl = (Switch)findViewById(R.id.sw_RemoteControl);

        tv_nerd_volt = (TextView)findViewById(R.id.lbl_Volt);
        tv_nerd_amp = (TextView)findViewById(R.id.lbl_Amp);
        tv_nerd_Ping = (TextView)findViewById(R.id.lbl_Ping);
        tv_nerd_Sig = (TextView)findViewById(R.id.lbl_SigStr);



        btn_Forward = (ImageButton)findViewById(R.id.btn_Forward);
        btn_Reverse = (ImageButton)findViewById(R.id.btn_Reverse);
        btn_Left = (ImageButton)findViewById(R.id.btn_Left);
        btn_Right = (ImageButton)findViewById(R.id.btn_Right);

        btn_FwLe = (ImageButton)findViewById(R.id.btn_ForwardLeft);
        btn_FwRi = (ImageButton)findViewById(R.id.btn_ForwardRight);
        btn_ReLe = (ImageButton)findViewById(R.id.btn_ReverseLeft);
        btn_ReRi = (ImageButton)findViewById(R.id.btn_ReverseRight);

        carState = null;

        tmrUpdate.scheduleAtFixedRate(sendUpdate,250, 250);
    }



    public void btnclick_StartConnection(View view){
        if(this.carBT.Initialise()){
            OnConnectionEstablished();
        }
        else{
            OnConnectionLost();
        }
    }
    public void btnclick_ToggleNerdStats(View view){
        nerdStatDetail.setVisibility(nerdStatDetail.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
        this.appPreferences.SetShowNerd(nerdStatDetail.getVisibility() == View.VISIBLE);
    }
    public void btnclick_Reset(View view){
        this.OnConnectionLost();
    }

    @Override
    public void NewDataRecieved(CarBTWrapper.DataPacket packet) {
        final CarBTWrapper.DataPacket renderPacket = packet;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sw_Pedal.setChecked(renderPacket.Controls_Pedal);
                sw_Gear.setChecked(renderPacket.Controls_Gear);
                sw_Switch.setChecked(renderPacket.Controls_Switch);
                sw_Manual.setChecked(renderPacket.Controls_Manual);
                tv_nerd_volt.setText("Voltage: " + renderPacket.Power_Volt + "V");
                tv_nerd_amp.setText("Current: " + renderPacket.Power_Amp + "A");
                g_volt.SetValue((float)renderPacket.Power_Volt);
                g_amp.SetValue((float)renderPacket.Power_Amp);
            }
        });

    }
}