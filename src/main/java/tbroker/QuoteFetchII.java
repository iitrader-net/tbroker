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

import java.util.*;
import java.util.concurrent.*;
import org.json.*;

public class QuoteFetchII extends RPCClient implements QuoteFetch {

    public void login(String acc_pass) throws Exception {
        String[] s = acc_pass.split(",");
        hostURL = s[0];
        token = s[1];
        log(E, hostURL);
        log(E, token);
    }

    public LinkedList<Candle> fetch(String sym, Date ts1, Date ts2) {
        LinkedList<Candle> ret = new LinkedList<Candle>();
        try {
            Tick t = fetch(sym);
            Candle c = new Candle(t.pri, t.pri, t.pri, t.pri, ts1.getTime() / 1000);
            ret.add(c);
        } catch (Exception e) {
            log(e);
        }
        return ret;
        // throw new RuntimeException("fetch is not supported:"+formatL(ts1)+","+formatL(ts2));
    }

    public Tick fetch(String sym) throws Exception {
        JSONObject jsn = get("/quote/" + sym);
        if (jsn.getString("ret").equals("OK")) {
            double pri = jsn.getDouble("v");
            long ts = jsn.getLong("ts");
            return new Tick(new Date(ts * 1000), 1, pri);
        }
        return null;
    }
}
