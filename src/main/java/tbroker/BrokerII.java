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
import java.util.*;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.json.*;

public class BrokerII extends RPCClient implements Broker {

    PositionCache pos;

    public void login(String acc_pass) throws Exception {
        // broker tbroker.BrokerII acc_pass,http://i2trader.com:5691,apikey_...
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
        syncPosition();
    }

    void syncPosition() throws Exception {
        if (pos != null && !pos.isExpired()) return;
        JSONObject jo = get("/position");
        if (!jo.getString("ret").equals("OK")) throw new Exception(jo.getString("ret"));
        JSONArray sa = jo.getJSONArray("sym");
        JSONArray va = jo.getJSONArray("vol");
        String ps = "";
        for (int i = 0; i < sa.length(); i++) {
            ps += sa.getString(i) + "," + va.getInt(i) + ",";
        }
        pos = new PositionCache(ps);
    }

    static String convertTX(String sym) {
        if (!sym.startsWith("tx")) return sym;
        String mon = sym.substring(6, 8);
        return "TX" + mon + ".TW";
    }

    public Order order(
            String sym, int vol, double pri, Date date, DealListener ol, int type, String tag) {
        //if (ol != null) throw new RuntimeException("DealListener is not supported yet");
        sym = convertTX(sym);
        JSONObject jsn = new JSONObject();
        jsn.put("sym", sym);
        jsn.put("vol", "" + vol);
        jsn.put("pri", "" + pri);
        jsn.put("type", "" + type);
        jsn.put("tag", "" + tag);
        jsn.put("callback", "");

        try {
            JSONObject ret = post("/order", jsn);
            System.out.println(ret.toString());
            if (!ret.getString("ret").equals("OK")) {
                throw new RuntimeException(ret.getString("ret"));
            }
            String oid = ret.getString("roid");
            return new Order(sym, vol, pri, date, type, tag, ol, oid.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean cancel(byte[] oid) {
        try {
            String path = "/cancel?roid=" + new String(oid);
            JSONObject jo = delete(path);
            if (jo.getString("ret").equals("OK")) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<String> getPositions() {
        try {
            syncPosition();
            return pos.getList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getOI(String sym) {
        try {
            syncPosition();
            return pos.getOI(sym);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public double getRight() {
        try {
            JSONObject jo = get("/right");
            return jo.getDouble("right");
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public boolean isSim() {
        return false;
    }
}
