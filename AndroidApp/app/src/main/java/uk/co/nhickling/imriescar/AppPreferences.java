package uk.co.nhickling.imriescar;

import android.content.SharedPreferences;

import java.io.InputStreamReader;

public class AppPreferences {
    SharedPreferences preferences;

    int m_iSpeedLimit;
    boolean m_bShowNerd;

    public void Initialise(SharedPreferences host){
        this.preferences = host;
        this.m_iSpeedLimit = this.preferences.getInt("iSpeedLimit", 20);
        this.m_bShowNerd = this.preferences.getBoolean("bShowNerd", false);
    }

    public int GetSpeedLimit(){
        return m_iSpeedLimit;
    }
    public void SetSpeedLimit(int newSPeed){
        m_iSpeedLimit = newSPeed;
        SharedPreferences.Editor edit = this.preferences.edit();
        edit.putInt("iSpeedLimit", newSPeed);
        edit.commit();
    }

    public boolean GetShowNerd(){
        return m_bShowNerd;
    }
    public void SetShowNerd(boolean showNerd){
        m_bShowNerd = showNerd;
        SharedPreferences.Editor edit = this.preferences.edit();
        edit.putBoolean("bShowNerd", showNerd);
        edit.commit();
    }
}
