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
import org.json.*;

public class StrategyTXNDMA extends StrategyTXND {
    static final int MA_SZ = 20;
    Avg ma = new Avg(MA_SZ);

    public void init(String sym, DealListener dealListener, Broker broker, Quote quote)
            throws Exception {
        super.init(sym, "p", -50, 12, dealListener, broker);
        quote.bind(sym, this);
    }

    public void save(File dir) throws Exception {
        JSONObject jsn = new JSONObject();
        jsn.put("oi", new Integer(oi == null ? 0 : oi.vol()));
        JSONArray vs = new JSONArray();
        int i = 0;
        for (Double d : ma.w) {
            vs.put(i++, d);
        }
        jsn.put("ma", vs);
        saveJson(jsn, getJsonFile(dir, sym));
    }

    public void load(File dir) throws Exception {
        JSONObject jsn = loadJson(getJsonFile(dir, sym));
        oi.vol = jsn.getInt("oi");
        JSONArray vs = jsn.getJSONArray("ma");
        ma = new Avg(MA_SZ);
        for (int i = 0; i < vs.length(); i++) {
            ma.push(vs.getDouble(i));
        }
    }

    public void _tick(Tick tick) {
        ma.push(tick.pri, 1);
        if (ma.sz() < MA_SZ) {
            return;
        }
        double avg = ma.avg();
        Date date = tick.getDate();
        if (tick.pri > avg + 20 && !inPos() && !inOdr()) {
            order(scale, 0, date, "go");
        } else if (tick.pri < avg) {
            cover(date, 0, "stop");
        }
    }

    public void _deal(Deal deal) {
        log(E, "_deal:" + deal.toString());
    }
}
