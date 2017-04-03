package controller.event;

import java.util.LinkedList;

public class CopierAdapter implements CopierListener {
    public void sendProgressChanged(int progress) {};
    public void receiveProgressChanged(int progress) {};
    public void filesListChanged(LinkedList<String> files) {};
    public void messagesListChanged(LinkedList<String> message) {};
    public void sendCopyFinalized(boolean finalized) {};
    public void receiveCopyFinalized(boolean finalized) {};

}
