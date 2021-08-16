package uk.co.nhickling.imriescar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public class CarBTWrapper {

    final UUID SERIAL_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //UUID for serial connection

    private BluetoothDevice m_device;
    private BluetoothSocket m_socket;
    private InputStream m_inputStream;
    private OutputStream m_outputStream;

    private InputStreamReader m_streamReader;
    private OutputStreamWriter m_streamWriter;
    private BufferedReader m_streamBufferRead;

    private boolean initialised = false;

    private Thread m_handleThread;

    public CarBTWrapper(){
        Reset();
    }

    private Runnable deviceManagement = new Runnable() {

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
            BluetoothAdapter m_deviceAdapter = BluetoothAdapter.getDefaultAdapter();
            for(;;) {
                try {
                    Reset();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(m_deviceAdapter.isEnabled())
                {
                    Set<BluetoothDevice> devices = m_deviceAdapter.getBondedDevices();
                    m_device = null;
                    for(BluetoothDevice device : devices)
                    {
                        String deviceName = device.getName();
                        if(deviceName.equals("Imrie_Car")){
                            m_device = device;
                        }
                    }
                    if(m_device == null){
                        continue;
                    }

                    eventHandler.DeviceFound();

                    try {
                        m_socket = m_device.createRfcommSocketToServiceRecord(SERIAL_UUID);
                    } catch (IOException e) {
                        continue;
                    }


                    try {
                        m_socket.connect();
                        m_outputStream = m_socket.getOutputStream();
                        m_inputStream = m_socket.getInputStream();
                        m_streamReader = new InputStreamReader(m_inputStream);
                        m_streamWriter = new OutputStreamWriter(m_outputStream);
                        m_streamBufferRead = new BufferedReader(m_streamReader);
                        eventHandler.DeviceConnected();
                        for(;;)
                        {
                            if(m_socket.isConnected()){
                                String readLine = null;
                                readLine = m_streamBufferRead.readLine();//ReadLine(m_streamReader);

                                if(readLine.startsWith("{")) {
                                    DataPacket dp = null;
                                    try {
                                        dp = new DataPacket(readLine);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    eventHandler.NewDataRecieved(dp);
                                }
                                else{
                                    replyRecieved.signal();
                                }
                            }
                        }

                    } catch (IOException e) {
                        eventHandler.DeviceConnectionLost();
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException interruptedException) {
                            interruptedException.printStackTrace();
                        }
                        continue;
                    }
                }
            }
        }
    };

    public class ThreadEvent {

        private Object lock = new Object();

        public void signal() {
            synchronized (lock) {
                lock.notify();
            }
        }

        public void reset(){
            lock = new Object();
        }

        public void await() throws InterruptedException {
            synchronized (lock) {
                lock.wait(500);
            }
        }
    }

    final ThreadEvent replyRecieved = new ThreadEvent();

    public long SendUpdate(DeviceState state) throws IOException {
        if(m_streamWriter != null)
        if(m_socket.isConnected()){
            replyRecieved.reset();
            try {
                m_streamWriter.write(state.getMessage() + '\n');
            } catch (NullPointerException npe){
                return -2;
            }
            Instant before = Instant.now();
            m_streamWriter.flush();
            try {
                replyRecieved.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Duration timeSpan = Duration.between(before, Instant.now());
            return timeSpan.toMillis();
        }
        return -1;
    }

    public void Initialise(){
        if(initialised)
        {
            return;
        }
        initialised = true;

        m_handleThread = new Thread(deviceManagement);
        m_handleThread.start();
    }

    public void Reset(){
        m_streamBufferRead = null;
        m_socket = null;
        m_streamReader = null;
        m_streamWriter = null;
        m_inputStream = null;
        m_outputStream = null;
        m_device = null;
    }


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

        public void DeviceFound();

        public void DeviceConnected();

        public void DeviceConnectionLost();

        public void NewDataRecieved(DataPacket packet);
    }
    private PacketHandlerInterface eventHandler;

    public void setEventHandler(PacketHandlerInterface packetInterface){
        this.eventHandler = packetInterface;
    }

}
