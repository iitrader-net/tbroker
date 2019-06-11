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

public class QuoteII extends RPCClient implements Quote {
    static final long PERIOD = 30 * 1000L;

    static final int POOL = 3;

    Hashtable<String, LinkedList<QuoteListener>> listeners;
    Hashtable<String, Runnable> executers;

    ScheduledThreadPoolExecutor scheduler;

    public void login(String acc_pass) throws Exception {
        // quote tbroker.QuoteII acc_pass,http://i2trader.com:5691,apikey_...
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
        if (!support("SPY")) throw new Exception("login fails");
    }
    String toRSym(String sym) {
        if(sym.startsWith("tx")){
            return "TX"+sym.substring(6, 8) + ".TW";
        }
        return sym;
    }
    public boolean support(String sym) {
        sym = toRSym(sym);
        try {
            JSONObject ret = get("/quote/" + sym);
            if (ret.getString("ret").equals("OK")) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    public QuoteII() {
        listeners = new Hashtable<String, LinkedList<QuoteListener>>();
        executers = new Hashtable<String, Runnable>();
        scheduler = new ScheduledThreadPoolExecutor(POOL);
    }

    public void bind(String sym, QuoteListener nl) {
        sym = toRSym(sym);
        LinkedList<QuoteListener> ls = listeners.get(sym);
        if (ls == null) {
            ls = new LinkedList<QuoteListener>();
            listeners.put(sym, ls);
            start(sym);
        }
        ls.add(nl);
    }

    public void unbind(String sym, QuoteListener l) {
        sym = toRSym(sym);
        LinkedList<QuoteListener> ls = listeners.get(sym);
        if (ls == null) {
            throw new RuntimeException("unexpected unbind");
        }
        ls.remove(l);
        if (ls.size() == 0) {
            stop(sym);
        }
    }

    class P implements Runnable {
        String sym;

        P(String _sym) {
            sym = _sym;
        }

        public void run() {
            try {
                JSONObject ret = get("/quote/" + sym);
                if (ret.getString("ret").equals("OK")) {
                    double pri = ret.getDouble("v");
                    long ts = ret.getLong("ts");
                    LinkedList<QuoteListener> ql = listeners.get(sym);
                    for (QuoteListener l : ql) {
                        Tick tick = new Tick(new Date(ts * 1000), 1, pri);
                        l.tick(tick);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void start(String sym) {
        log(E, "start:%s", sym);
        Runnable exe = new P(sym);
        executers.put(sym, exe);
        scheduler.scheduleAtFixedRate(exe, 1000, PERIOD, TimeUnit.MILLISECONDS);
    }

    void stop(String sym) {
        log(E, "stop:%s", sym);
        Runnable exe = executers.get(sym);
        executers.remove(exe);
        scheduler.remove(exe);
    }
}
