/*
 * Copyright (c) 2017 Asiel Díaz Benítez.
 * 
 * This file is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * You should have received a copy of the GNU General Public License
 * along with this file.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */

package controller.event;

import java.util.LinkedList;

public interface CopierListener {
    public void sendProgressChanged(int progress);
    public void receiveProgressChanged(int progress);
    public void filesListChanged(LinkedList<String> files);
    public void messagesListChanged(LinkedList<String> message);
    public void sendCopyFinalized(boolean finalized);
    public void receiveCopyFinalized(boolean finalized);
}
