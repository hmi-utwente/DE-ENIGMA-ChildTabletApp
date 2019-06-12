package nl.utwente.hmi.deenigmachildtabletapp.communication;

import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

import nl.utwente.hmi.deenigmachildtabletapp.MainActivity;
import nl.utwente.hmi.middleware.MiddlewareListener;

public class CommunicationManager {

    private String IPAddress = "192.168.0.22";
    private AvailableMiddlewares selectedMiddleware = AvailableMiddlewares.APOLLO;
    private AvailableModes selectedMode = AvailableModes.ADULT;

    public enum AvailableMiddlewares {
        APOLLO, ROS
    }

    public enum AvailableModes {
        CHILD, ADULT
    }

    private CommunicationThread comms;
    private List<MiddlewareListener> listeners;

    public CommunicationManager(){
        this.listeners = new ArrayList<MiddlewareListener>();
    }

    public void updateConnectionSettings(AvailableModes selectedMode, AvailableMiddlewares selectedMiddleware, String IPAddress){
        this.selectedMode = selectedMode;
        this.selectedMiddleware = selectedMiddleware;
        this.IPAddress = IPAddress;
    }

    private void initComms(){
        if(comms != null) {
            comms.stop();
        }

        Log.i("daniel", "Starting communication thread");
        this.comms = new CommunicationThread(selectedMode, selectedMiddleware, IPAddress);
        new Thread(comms).start();

        //now wait for the connection to be started
        try {
            comms.awaitInitialization();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.e("daniel", "Error when starting communication thread");
        }

        if(comms.isInitialized()) {
            Log.i("daniel", "Communication thread started successfully");

            for (MiddlewareListener l : listeners) {
                comms.addListener(l);
            }
        } else {
            Log.i("daniel", "Communication thread was unable to start");
        }
    }

    public boolean isInitialized(){
        return comms.isInitialized();
    }

    public void start(){
        initComms();
    }

    public void restart(){
        initComms();
    }

    public void sendData(JsonNode jn){
        if(comms != null && comms.isInitialized()) {
            comms.sendData(jn);
        }
    }

    public void addListener(MainActivity.CommandReceiver listener){
        //store the listener so we can automagically add it again in case of a restart
        listeners.add(listener);

        if(comms != null && comms.isInitialized()) {
            comms.addListener(listener);
        }
    }

}
