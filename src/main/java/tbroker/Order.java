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
import org.json.*;

public class Order extends Util {
    public String sym;
    public int vol;
    public double pri;
    public Date date;
    public int type;
    public String tag;
    public byte[] oid;
    DealListener dl;
    Deal deal;
    Object priv;

    public Order(
            String _sym,
            int _vol,
            double _pri,
            Date _date,
            int _type,
            String _tag,
            DealListener _dl,
            byte[] _oid) {
        sym = _sym;
        vol = _vol;
        pri = _pri;
        date = _date;
        type = _type;
        tag = _tag;
        dl = _dl;
        oid = _oid;
        deal = null;
        priv = null;
    }

    public boolean isDeal() {
        return deal != null;
    }

    public Object clone() {
        Order o = new Order(sym, vol, pri, date, type, tag, dl, oid);
        o.deal = deal;
        return o;
    }

    public String toString() {
        return String.format(
                "%s,%d,%.2f,%s,%d,%s,%s", sym, vol, pri, formatL(date), type, tag, new String(oid));
    }

    public JSONObject toJsn() {
        JSONObject ret = new JSONObject();
        ret.put("sym", sym);
        ret.put("vol", vol);
        ret.put("pri", pri);
        ret.put("date", formatL(date));
        ret.put("type", type);
        ret.put("tag", tag);
        ret.put("oid", new String(oid));
        return ret;
    }
}
