/*-****************************************************************************/
/*-                                                                           */
/*-            Copyright (c) of tbroker                                       */
/*-                                                                           */
/*-  This software is copyrighted by and is the sole property of socware.net. */
/*-  All rights, title, ownership, or other interests in the software remain  */
/*-  the property of socware.net. The source code is FREE for short-term      */
/*-  evaluation, educational or non-commercial research only. Any commercial  */
/*-  application may only be used in accordance with the corresponding license*/
/*-  agreement. Any unauthorized use, duplication, transmission, distribution,*/
/*-  or disclosure of this software is expressly forbidden.                   */
/*-                                                                           */
/*-  Knowledge of the source code may NOT be used to develop a similar product*/
/*-                                                                           */
/*-  This Copyright notice may not be removed or modified without prior       */
/*-  written consent of socware.net.                                          */
/*-                                                                           */
/*-  socware.net reserves the right to modify this software                   */
/*-  without notice.                                                          */
/*-                                                                           */
/*-  To contact socware.net:                                                  */
/*-                                                                           */
/*-             socware.help@gmail.com                                        */
/*-                                                                           */
/*-****************************************************************************/
package tbroker;

import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.*;
import java.util.*;
import org.java_websocket.client.*;
import org.java_websocket.handshake.ServerHandshake;
import org.json.*;

public class QuoteIIWS extends QuoteII implements Quote {

    String res;
    String accPass;
    IIWebSocketClient client;

    class IIWebSocketClient extends WebSocketClient {

        public IIWebSocketClient(String url) throws URISyntaxException {
            super(new URI(url));
        }

        @Override
        public void onOpen(ServerHandshake shake) {
            log("opening websocket");
            for (Iterator<String> it = shake.iterateHttpFields(); it.hasNext(); ) {
                String key = it.next();
                log(key + ":" + shake.getFieldValue(key));
            }
        }

        @Override
        public void onMessage(String msg) {
            jsnIn(msg);
        }

        @Override
        public void onClose(int paramInt, String paramString, boolean paramBoolean) {
            log("close websocket, reconnect");
            new Thread(
                            new Runnable() {
                                public void run() {
                                    try {
                                        Thread.sleep(10 * 1000);
                                        login(accPass);
                                    } catch (Exception e) {
                                        log(e);
                                    }
                                }
                            })
                    .start();
        }

        @Override
        public void onError(Exception e) {
            log(e);
        }
    }

    public void login(String acc_pass) throws Exception {
        accPass = acc_pass;
        String[] s = acc_pass.split(",");
        hostURL = s[1];
        token = "";
        for (int i = 2; i < s.length; i++) {
            if (i != 2) token += ",";
            token += s[i];
        }
        log(E, acc_pass);
        log(E, hostURL);
        log(E, token);
        client = new IIWebSocketClient(hostURL);
        client.connectBlocking();
        JSONObject jsn = new JSONObject();
        jsn.put("token", token);
        jsnOut(jsn.toString());
        synchronized (this) {
            wait();
        }
        String status = new JSONObject(res).getString("status");
        if (!status.equals("OK")) {
            throw new Exception(status);
        }
        for (String key : listeners.keySet()) {
            start(key);
        }
    }

    synchronized void jsnIn(String js) {
        JSONObject jsn = new JSONObject(js);
        log(js);
        if (jsn.has("v")) {
            tick(jsn.getString("sym"), jsn);
        } else {
            res = js;
            notify();
        }
    }

    synchronized void jsnOut(String message) {
        client.send(message);
    }

    void start(String sym) {
        log(E, "start:%s", sym);
        JSONObject jsn = new JSONObject();
        jsn.put("action", "sub");
        jsn.put("sym", sym);
        try {
            jsnOut(jsn.toString());
        } catch (Exception e) {
            new RuntimeException(e.toString());
        }
    }

    void stop(String sym) {
        log(E, "stop:%s", sym);
    }
}
