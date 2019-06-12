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
    private List<MiddlewareListener> listeners;
    private Middleware middleware = null;
    private boolean isInitialized = false;

    public CommunicationThread(CommunicationManager.AvailableModes selectedMode, CommunicationManager.AvailableMiddlewares selectedMiddleware, String IPAddress) {
        this.sendQueue = new LinkedBlockingQueue();
        this.selectedMode = selectedMode;
        this.selectedMiddleware = selectedMiddleware;
        this.IPAddress = IPAddress;
        listeners = new ArrayList<MiddlewareListener>();
    }

    public void addListener(MiddlewareListener listener){
        if(isInitialized && middleware != null) {
            listeners.add(listener);
        }
    }

    public void sendData(JsonNode jn){
        sendQueue.add(jn);
    }

    public void stop(){
        this.running = false;
        isInitialized = false;
    }

    /**
     * Any other threads who are dependent on this connection being initialised should call this method after creating and starting the communication thread
     * @throws InterruptedException
     */
    public synchronized void awaitInitialization() throws InterruptedException {
        if(!isInitialized){
            this.wait();
        }
    }

    public synchronized boolean isInitialized(){
        return isInitialized;
    }

    private synchronized void initializationCompleted(){
        isInitialized = true;

        //make sure to notify all waiting threads when we have succesfully initialised the connection
        this.notifyAll();
    }

    private synchronized void initializationFailed(){
        isInitialized = false;
        this.notifyAll();
    }

    @Override
    public void run() {
        isInitialized = false;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Middleware> future = executor.submit(new ConnectionInitializer());
        try {
            Log.i("daniel","Starting executor to initialise connection!");
            middleware = future.get(3, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            Log.i("daniel","Timeout");

            //TODO: better would be to actually cancel/interrupt the ROS connection attempt, but that doesn't seem possible without hacking in the code of the ROS wrapper
            //so as a workaround we just forget that this connection attempt is running in the background and dereference it...
            //future.cancel(true);

            middleware = null;
            stop();
            initializationFailed();
        } catch (InterruptedException e) {
            Log.i("daniel","Interrupted");
            e.printStackTrace();
        } catch (ExecutionException e) {
            Log.i("daniel","Exception");
            e.printStackTrace();
        }

        executor.shutdownNow();

        if(middleware != null && isInitialized){
            middleware.addListener(this);
        }

        while(this.running){
            try {
                if(isInitialized && middleware != null) {
                    JsonNode nextMessage = sendQueue.take();
                    middleware.sendData(nextMessage);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                stop();
            }
        }
    }

    @Override
    public void receiveData(JsonNode jsonNode) {
        if(running){
            for(MiddlewareListener ml : listeners){
                ml.receiveData(jsonNode);
            }
        }
    }

    private class ConnectionInitializer implements Callable<Middleware>{

        @Override
        public Middleware call() throws Exception {
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
                    initializationFailed();
                    stop();
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
                initializationCompleted();
            } else {
                Log.i("daniel","Failure!");
                initializationFailed();
            }

            return mw;
        }
    }
}
