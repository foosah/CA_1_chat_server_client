/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package echoclient;

import java.util.ArrayList;

/**
 *
 * @author Seb
 */
public interface EchoListener {
    public void messageArrived(String data);
    public void messageArrived(ArrayList<String> onlineUserList);
}
