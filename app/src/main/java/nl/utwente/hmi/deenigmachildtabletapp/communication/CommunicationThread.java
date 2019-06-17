package nl.utwente.hmi.deenigmachildtabletapp.communication;

import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import nl.utwente.hmi.middleware.Middleware;
import nl.utwente.hmi.middleware.MiddlewareListener;
import nl.utwente.hmi.middleware.ros.ROSMiddleware;
import nl.utwente.hmi.middleware.stomp.STOMPMiddleware;
import pk.aamir.stompj.StompJRuntimeException;

public class CommunicationThread implements Runnable, MiddlewareListener {

    private CommunicationManager.AvailableModes selectedMode;
    private String IPAddress;
    private CommunicationManager.AvailableMiddlewares selectedMiddleware;
    protected BlockingQueue<JsonNode> sendQueue = null;
    private boolean running = true;
    private List<MiddlewareListener> middlewareListeners;
    private List<ConnectionStatusListener> connectionStatusListeners;
    private Middleware middleware = null;
    private ConnectionStatusListener.ConnectionStatus currentStatus;

    public CommunicationThread(CommunicationManager.AvailableModes selectedMode, CommunicationManager.AvailableMiddlewares selectedMiddleware, String IPAddress) {
        this.sendQueue = new LinkedBlockingQueue();
        this.selectedMode = selectedMode;
        this.selectedMiddleware = selectedMiddleware;
        this.IPAddress = IPAddress;
        middlewareListeners = new ArrayList<MiddlewareListener>();
        connectionStatusListeners = new ArrayList<ConnectionStatusListener>();
        setStatus(ConnectionStatusListener.ConnectionStatus.UNDEFINED, "");
    }

    public void addMiddlewareListener(MiddlewareListener middlewareListener){
        Log.i("daniel", "Adding middleware listener in communication thread");
        middlewareListeners.add(middlewareListener);
    }

    public void sendData(JsonNode jn){
        sendQueue.add(jn);
    }

    public void stop(){
        this.running = false;
        middlewareListeners.clear();
        connectionStatusListeners.clear();
    }

    public void addConnectionStatusListener(ConnectionStatusListener csl){
        Log.i("daniel", "Adding connection status listener in communication thread");
        connectionStatusListeners.add(csl);
    }

    private void setStatus(ConnectionStatusListener.ConnectionStatus status, String msg){
        Log.i("daniel", "Setting status "+status+" message: "+msg);
        this.currentStatus = status;
        for(ConnectionStatusListener csl : connectionStatusListeners){
            csl.statusUpdate(status, msg);
        }
    }

    public ConnectionStatusListener.ConnectionStatus getConnectionStatus(){
        return currentStatus;
    }

    @Override
    public void run() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Middleware> future = executor.submit(new ConnectionInitializer());
        try {
            Log.i("daniel","Starting executor to initialise connection");
            middleware = future.get(3, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            Log.i("daniel","Timeout");

            //TODO: better would be to actually cancel/interrupt the ROS connection attempt, but that doesn't seem possible without hacking in the code of the ROS wrapper
            //so as a workaround we just forget that this connection attempt is running in the background and dereference it...
            //future.cancel(true);

            setStatus(ConnectionStatusListener.ConnectionStatus.ERROR, "Timeout while connecting");
            middleware = null;
            stop();
        } catch (InterruptedException e) {
            Log.i("daniel","Interrupted");
            e.printStackTrace();
        } catch (ExecutionException e) {
            Log.i("daniel","Exception");
            e.printStackTrace();
        }

        executor.shutdownNow();

        while(this.running){
            try{
                if(currentStatus == ConnectionStatusListener.ConnectionStatus.CONNECTED && middleware != null) {
                    JsonNode nextMessage = sendQueue.take();
                    middleware.sendData(nextMessage);
                } else {
                    //wait for the connection to be made
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                stop();
            }
        }
    }

    @Override
    public void receiveData(JsonNode jsonNode) {
        Log.d("daniel", "Got message 1: "+jsonNode.toString());
        if(running){
            for(MiddlewareListener ml : middlewareListeners){
                Log.d("daniel", "Forwarding to thread listener");
                ml.receiveData(jsonNode);
            }
        }
    }

    private class ConnectionInitializer implements Callable<Middleware>{

        @Override
        public Middleware call() throws Exception {
            setStatus(ConnectionStatusListener.ConnectionStatus.CONNECTING, "Attempting to establish connection");

            Middleware mw = null;
            if(selectedMiddleware == CommunicationManager.AvailableMiddlewares.APOLLO) {
                try {
                    if (selectedMode == CommunicationManager.AvailableModes.ADULT) {
                        Log.i("daniel","Starting "+selectedMode+" on "+selectedMiddleware);
                        mw = new STOMPMiddleware(IPAddress, 61613, "/topic/adult_tablet.command", "/topic/adult_tablet.feedback");
                    } else if (selectedMode == CommunicationManager.AvailableModes.CHILD) {
                        Log.i("daniel","Starting "+selectedMode+" on "+selectedMiddleware);
                        mw = new STOMPMiddleware(IPAddress, 61613, "/topic/child_tablet.command", "/topic/child_tablet.feedback");
                    }
                }catch(StompJRuntimeException e){
                    Log.e("daniel","Got error while connecting to APOLLO: "+e.getMessage());
                    setStatus(ConnectionStatusListener.ConnectionStatus.ERROR, "Error while connecting: "+e.getMessage());
                    stop();
                    return null;
                }
            } else if(selectedMiddleware == CommunicationManager.AvailableMiddlewares.ROS){
                if(selectedMode == CommunicationManager.AvailableModes.ADULT) {
                    Log.i("daniel","Starting "+selectedMode+" on "+selectedMiddleware);
                    mw = new ROSMiddleware("ws://"+IPAddress+":9090","/adult_tablet_feedback","/adult_tablet_command");
                }else if(selectedMode == CommunicationManager.AvailableModes.CHILD) {
                    Log.i("daniel","Starting "+selectedMode+" on "+selectedMiddleware);
                    mw = new ROSMiddleware("ws://"+IPAddress+":9090","/child_tablet_feedback","/child_tablet_command");
                }
            }

            if(mw != null){
                Log.i("daniel","Success!");
                Thread.sleep(2000);
                Log.i("daniel","Registering this thread as listener to middleware");
                mw.addListener(CommunicationThread.this);
                setStatus(ConnectionStatusListener.ConnectionStatus.CONNECTED, "Connected successfully");
            } else {
                Log.i("daniel","Failure!");
                setStatus(ConnectionStatusListener.ConnectionStatus.ERROR, "Unable to initiate middleware");
            }

            return mw;
        }
    }
}
