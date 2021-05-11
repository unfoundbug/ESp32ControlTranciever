package uk.co.nhickling.imriescar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.UUID;

public class CarBTWrapper {
    private BluetoothAdapter m_deviceAdapter;
    private BluetoothDevice m_device;
    private BluetoothSocket m_socket;
    private InputStream m_inputStream;
    private OutputStream m_outputStream;

    private InputStreamReader m_streamReader;
    private OutputStreamWriter m_streamWriter;
    private BufferedReader m_streamBufferRead;
    private Thread m_handleThread;

    public class DataPacket{
        private String source;

        public boolean Controls_Pedal;
        public boolean Controls_Gear;
        public boolean Controls_Switch;
        public boolean Controls_Manual;
        public boolean Controls_Remote;

        public int Drive_Target;
        public boolean Drive_Lights;
        public String Drive_Mode;

        public double Power_Volt;
        public double Power_Amp;

        public DataPacket(String source) throws JSONException {
            if (source == null) {
                throw new JSONException("No source to load");
            }
            this.source = source;
            JSONObject jObject = new JSONObject(this.source);
            JSONObject controlObject = jObject.getJSONObject("Controls");
            Controls_Pedal = controlObject.getBoolean("Pedal");
            Controls_Gear = controlObject.getBoolean("Gear");
            Controls_Switch = controlObject.getBoolean("Switch");
            Controls_Remote = controlObject.getBoolean("ToggleRemote");
            Controls_Manual = !Controls_Remote;

            JSONObject driveObject = jObject.getJSONObject("Drive");
            Drive_Target = driveObject.getInt("Target");
            Drive_Lights = driveObject.getBoolean("Lights");
            Drive_Mode = driveObject.getString("Mode");

            JSONObject powerObject = jObject.getJSONObject("Power");
            try {
                Power_Volt = powerObject.getDouble("Volt");
                Power_Amp = powerObject.getDouble("Amp");
            } catch (JSONException ex){

            }

        }
    }
    public interface PacketHandlerInterface{
        public void NewDataRecieved(DataPacket packet);
    }
    private PacketHandlerInterface eventHandler;
    public void setEventHandler(PacketHandlerInterface packetInterface){
            this.eventHandler = packetInterface;
    }

    Runnable handleCommunications = new Runnable() {

        private String ReadLine(InputStreamReader sr) throws IOException {
            StringBuilder sb = new StringBuilder();
            int readByte = sr.read();
            while (readByte>-1 && readByte!= '\n')
            {
                sb.append((char) readByte);
                readByte = sr.read();
            }
            return sb.length()==0?null:sb.toString();
        }

        @Override
        public void run() {
            for(;;){
                if(m_streamBufferRead != null){
                    if(m_socket.isConnected()){
                        String readLine = null;
                        try {
                            readLine = m_streamBufferRead.readLine();//ReadLine(m_streamReader);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        DataPacket dp = null;
                        try {
                            dp = new DataPacket(readLine);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        eventHandler.NewDataRecieved(dp);
                    }
                }
            }
        }
    };

    public static class DeviceState{
        JSONObject rootObject;
        public DeviceState() throws JSONException {
            rootObject = new JSONObject();
        }

        public void SetLocalControl() throws JSONException {
            rootObject = new JSONObject();
            JSONObject controlObject = new JSONObject();
            controlObject.put("LocalControl", true);
            rootObject.put("Control", controlObject);
        }

        public void SetRemoteControl(int driveDirection, int steerDirection, boolean lights) throws JSONException {
            rootObject = new JSONObject();
            JSONObject controlObject = new JSONObject();
            controlObject.put("LocalControl", false);
            controlObject.put("Drive", driveDirection);
            controlObject.put("Steer", steerDirection);
            controlObject.put("Lights", lights);
            rootObject.put("Control", controlObject);
        }
        public String getMessage(){
            return rootObject.toString();
        }
    }

    public void SendUpdate(DeviceState state) throws IOException {
        if(m_streamWriter != null)
        if(m_socket.isConnected()){
            m_streamWriter.write(state.getMessage() + '\n');
            m_streamWriter.flush();
        }
    }

    public boolean Initialise(){
        Reset();

        m_deviceAdapter = BluetoothAdapter.getDefaultAdapter();
        if(m_deviceAdapter.isEnabled()){
            final UUID SERIAL_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //UUID for serial connection
            m_device = m_deviceAdapter.getRemoteDevice("F0:08:D1:D3:90:A6");
            try {
                m_socket = m_device.createRfcommSocketToServiceRecord(SERIAL_UUID);
            } catch (IOException e) { return false;}

            try {
                m_socket.connect();
                m_outputStream = m_socket.getOutputStream();
                m_inputStream = m_socket.getInputStream();
                m_streamReader = new InputStreamReader(m_inputStream);
                m_streamWriter = new OutputStreamWriter(m_outputStream);
                m_streamBufferRead = new BufferedReader(m_streamReader);
                m_handleThread = new Thread(this.handleCommunications);
                m_handleThread.start();
                return true;
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }

    public void Reset(){
        m_streamBufferRead = null;
        m_socket = null;
        m_streamReader = null;
        m_streamWriter = null;
        m_inputStream = null;
        m_outputStream = null;
        m_device = null;
        m_deviceAdapter = null;
    }
}
