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

public abstract class StrategyTXND extends StrategyTX implements PData {

    public void init(String _sym, DealListener _dealListener, Broker _broker, Quote quote)
            throws Exception {
        super.init(sym, ptag, mhead, mtail, dealListener, broker);
        isDay = false;
    }

    File getJsonFile(File dir, String sym) {
        return new File(dir, this.getClass().getName() + "." + sym + ".json");
    }

    public void save(File dir) throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("oi", new Integer(oi == null ? 0 : oi.vol()));
        saveJson(obj, getJsonFile(dir, sym));
    }

    public void load(File dir) throws Exception {
        JSONObject jobj = loadJson(getJsonFile(dir, sym));
        oi.vol = jobj.getInt("oi");
    }

    boolean day1stTick;
    Date day;

    public void dayOpen(Date _day) {
        super.dayOpen(_day);
        day1stTick = false;
        day = _day;
    }
}
