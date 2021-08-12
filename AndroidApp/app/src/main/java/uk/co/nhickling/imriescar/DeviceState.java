package uk.co.nhickling.imriescar;

import org.json.JSONException;
import org.json.JSONObject;

public class DeviceState{
    JSONObject rootObject;
    private DeviceState(JSONObject clientObject) {
        rootObject = clientObject;
    }

    public static DeviceState SetLocalControl() throws JSONException {
        JSONObject clientObject = new JSONObject();
        JSONObject controlObject = new JSONObject();
        controlObject.put("LocalControl", true);
        clientObject.put("Control", controlObject);
        return new DeviceState(clientObject);
    }

    public static DeviceState SetRemoteControl(int driveDirection, int steerDirection, boolean lights) throws JSONException {
        JSONObject clientObject = new JSONObject();
        JSONObject controlObject = new JSONObject();
        controlObject.put("LocalControl", false);
        controlObject.put("Drive", driveDirection);
        controlObject.put("Steer", steerDirection);
        controlObject.put("Lights", lights);
        clientObject.put("Control", controlObject);
        return new DeviceState(clientObject);
    }

    public String getMessage(){
        return rootObject.toString();
    }
}