package nl.utwente.hmi.deenigmatabletapp.communication;

public interface ConnectionStatusListener {

    public enum ConnectionStatus{
        UNDEFINED,
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        ERROR
    }

    public void statusUpdate(ConnectionStatus status, String msg);

}
