package nl.utwente.hmi.deenigmachildtabletapp.communication;

import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

import nl.utwente.hmi.deenigmachildtabletapp.MainActivity;
import nl.utwente.hmi.middleware.MiddlewareListener;

public class CommunicationManager implements ConnectionStatusListener {


    public enum AvailableMiddlewares {
        APOLLO, ROS
    }

    public enum AvailableModes {
        CHILD, ADULT
    }

    private String IPAddress = "192.168.0.22";
    private AvailableMiddlewares selectedMiddleware = AvailableMiddlewares.ROS;
    private AvailableModes selectedMode = AvailableModes.ADULT;

    private CommunicationThread comms;
    private List<MiddlewareListener> middlewareListeners;
    private List<ConnectionStatusListener> connectionStatusListeners;

    public CommunicationManager(){
        this.middlewareListeners = new ArrayList<MiddlewareListener>();
        this.connectionStatusListeners = new ArrayList<ConnectionStatusListener>();
    }

    public void updateConnectionSettings(AvailableModes selectedMode, AvailableMiddlewares selectedMiddleware, String IPAddress){
        this.selectedMode = selectedMode;
        this.selectedMiddleware = selectedMiddleware;
        this.IPAddress = IPAddress;
    }

    private synchronized void initComms(){
        if(comms != null) {
            comms.stop();
        }

        Log.i("daniel", "Starting communication thread");
        this.comms = new CommunicationThread(selectedMode, selectedMiddleware, IPAddress);
        comms.addConnectionStatusListener(this);

        for(ConnectionStatusListener csl : connectionStatusListeners){
            Log.i("daniel", "Adding connection status listener to thread");
            comms.addConnectionStatusListener(csl);
        }

        new Thread(comms).start();
    }

    public void addConnectionStatusListener(ConnectionStatusListener csl){
        connectionStatusListeners.add(csl);
    }

    @Override
    public void statusUpdate(ConnectionStatus status, String msg) {
        if(status == ConnectionStatus.CONNECTED){
            Log.i("daniel", "Communication thread started successfully "+msg);

            for (MiddlewareListener ml : middlewareListeners) {
                Log.i("daniel", "Adding middleware listener to thread");
                comms.addMiddlewareListener(ml);
            }
        } else if(status == ConnectionStatus.ERROR){
            Log.e("daniel", "Error in communication thread: "+msg);
        }
    }

    public void start(){
        initComms();
    }

    public void restart(){
        initComms();
    }

    public void sendData(JsonNode jn){
        if(comms != null && comms.getConnectionStatus() == ConnectionStatusListener.ConnectionStatus.CONNECTED) {
            comms.sendData(jn);
        }
    }

    public ConnectionStatus getConnectionStatus(){
        return comms.getConnectionStatus();
    }

    public synchronized void addMiddlewareListener(MainActivity.CommandReceiver middlewareListener){
        //store the middlewareListener so we can automagically add it again in case of a restart
        Log.i("daniel", "Adding middleware listener to manager");
        middlewareListeners.add(middlewareListener);

        if(comms != null && comms.getConnectionStatus() == ConnectionStatus.CONNECTED) {
            Log.i("daniel", "Adding middleware listener to thread");
            comms.addMiddlewareListener(middlewareListener);
        }
    }

}
