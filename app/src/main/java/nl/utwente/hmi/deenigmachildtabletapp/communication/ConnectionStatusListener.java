package nl.utwente.hmi.deenigmachildtabletapp.communication;

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
