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

public class Deal extends Util {
    public Deal(String line) {
        StringTokenizer st = new StringTokenizer(line, Cfg.sep);
        sym = st.nextToken();
        date = parse(st.nextToken(), "yyyy/MM/dd HH:mm:ss");
        pri = Double.parseDouble(st.nextToken().replaceAll("\\s+", ""));
        vol = Integer.parseInt(st.nextToken().replaceAll("\\s+", ""));
        tag = st.nextToken();
        oid = st.nextToken().getBytes();
    }

    public String toString() {
        String sep = Cfg.sep;
        return sym
                + sep
                + format(date, "yyyy/MM/dd HH:mm:ss")
                + sep
                + String.format("%6.2f%s%3d%s%3s", pri, Cfg.sep, vol, Cfg.sep, tag);
    }

    public JSONObject toJsn() {
        JSONObject jsn = new JSONObject();
        jsn.put("sym", sym);
        jsn.put("vol", vol);
        jsn.put("price", pri);
        jsn.put("tag", tag);
        jsn.put("oid", new String(oid));
        jsn.put("date", formatL(date));
        return jsn;
    }

    public Deal(String _sym, double _pri, int _vol, Date _date, String _tag, byte[] _oid) {
        sym = _sym;
        pri = _pri;
        vol = _vol;
        date = _date;
        tag = _tag;
        oid = _oid;
        odr = null;
    }

    public Deal(Order _odr, double pri, Date date) {
        this(_odr.sym, pri, _odr.vol, date, _odr.tag, _odr.oid);
        odr = _odr;
        odr.deal = this;
        if (odr.dl != null) odr.dl.deal(this);
    }

    public byte[] oid;
    public String tag;
    public String sym;
    public double pri;
    public int vol;
    public Date date;
    public Order odr;

    public int compareTo(Deal d2) {
        long d = date.getTime() - d2.date.getTime();
        if (d > 0) return 1;
        else if (d < 0) return -1;
        else return 0;
    }

    public double getCost() {
        return OIFactory.newOI(this).getCost();
    }
}
