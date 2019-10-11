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

public class IdxImplCB extends Util implements IdxImpl, PData {

    private static Hashtable<String, IdxImplCB> instance;

    static {
        instance = new Hashtable<String, IdxImplCB>();
    }

    public static void init(String sym, Quote quote) throws Exception {
        IdxImplCB i = instance.get(sym);
        if (i == null) {
            i = new IdxImplCB(sym);
            Idx idx = Idx.getInstance(sym);
            idx.bind(i, "COW_BEAR");
            idx.bind(i, "CBA");
            idx.bind(i, "CBN");
            idx.bind(i, "RSI10");
            idx.bind(i, "SED");
            idx.bind(i, "OPEN");
            idx.bind(i, "HIGH");
            idx.bind(i, "LOW_");
            quote.bind(sym, i);
            instance.put(sym, i);
            if (Cfg.loadIdx) i.load(new File(Cfg.loadPath));
        }
    }

    public double get(String name) {
        switch (name.charAt(2)) {
            case 'W':
                return avg / avgn;
            case 'A':
                return avg;
            case 'N':
                return avgn;
            case 'I':
                return rsi10c;
            case 'D':
                return sed;
            case 'E':
                return op;
            case 'G':
                return hi;
            case '_':
                return lw;
        }
        return 0;
    }

    private Date mh, mt;

    private int mhead = -50;

    private int mtail = 20;

    private double avg = 0;
    private int avgn;

    private String sym;
    private double op, hi, lw, ed, sed;
    private Date ld;

    private double ped;
    private int rsi10c, rsi10p;
    private LinkedList<Integer> rsi10;

    private IdxImplCB(String _sym) {
        sym = _sym;
        rsi10 = new LinkedList<Integer>();
    }

    File getJsonFile(File dir, String sym) {
        Class c = this.getClass();
        return new File(dir, c.getName() + "." + sym + ".json");
    }

    public void save(File dir) throws Exception {
        JSONObject jsn = new JSONObject();
        jsn.put("avg", avg);
        jsn.put("avgn", avgn);
        jsn.put("sym", sym);
        jsn.put("op", op);
        jsn.put("hi", hi);
        jsn.put("lw", lw);
        jsn.put("ed", ed);
        if (ld == null) ld = parseL("19700101 00:00:00");
        jsn.put("ld", formatL(ld));
        jsn.put("ped", ped);
        jsn.put("rsi10c", rsi10c);
        jsn.put("rsi10p", rsi10p);
        JSONArray jrsi = new JSONArray(rsi10);
        for (int i = 0; i < rsi10.size(); i++) {
            JSONObject o = new JSONObject();
            o.put("v", rsi10.get(i).intValue());
            jrsi.put(i, o);
        }
        jsn.put("rsi", jrsi);
        saveJson(jsn, getJsonFile(dir, sym));
    }

    public void load(File dir) throws Exception {
        String s = null;
        File lf = getJsonFile(dir, sym);
        if (!lf.exists()) return;
        println(Y, "load from:" + lf.getPath());
        JSONObject js = loadJson(lf);
        avg = js.getDouble("avg");
        avgn = js.getInt("avgn");
        sym = js.getString("sym");
        op = js.getDouble("op");
        hi = js.getDouble("hi");
        lw = js.getDouble("lw");
        ed = js.getDouble("ed");
        ld = parseL(js.getString("ld"));
        ped = js.getDouble("ped");
        rsi10c = js.getInt("rsi10c");
        rsi10p = js.getInt("rsi10p");
        JSONArray rsi = js.getJSONArray("rsi");
        rsi10 = new LinkedList<Integer>();
        for (int i = 0; i < rsi.length(); i++) {
            JSONObject o = rsi.getJSONObject(i);
            rsi10.add(o.getInt("v"));
        }
        println(
                Y,
                String.format(
                        "avg = %.2f\n"
                                + "avgn= %d \n"
                                + "sym = %s\n"
                                + "op  = %.2f \n"
                                + "hi  = %.2f \n"
                                + "lw  = %.2f \n"
                                + "ed  = %.2f \n"
                                + "ld  = %s \n"
                                + "ped = %.2f\n"
                                + "rsi10c=%d \n"
                                + "rsi10p=%d \n",
                        avg,
                        avgn,
                        sym,
                        op,
                        hi,
                        lw,
                        ed,
                        ld == null ? "null" : formatL(ld),
                        ped,
                        rsi10c,
                        rsi10p));
        StringBuffer sb = new StringBuffer();
        sb.append("{ ");
        for (int i = 0; i < rsi10.size(); i++) sb.append(rsi10.get(i) + ",");
        println(Y, sb.toString());
    }

    public void dayOpen(Date date) {
        Date pivot = parse(sym.substring(2) + "01");
        mh = addDay(pivot, mhead);
        mt = addDay(pivot, mtail);
        if (!inRange(date, mh, mt)) return;
        this.sym = sym;
        op = hi = lw = 0;
        sed = ed;
    }

    public void tick(Tick tick) {
        Date date = tick.getDate();
        if (!inRange(date, mh, mt)) return;
        if (op == 0) {
            op = hi = lw = tick.pri;
        }
        ld = date;
        if (tick.pri > hi) hi = tick.pri;
        if (tick.pri < lw) lw = tick.pri;
        ed = tick.pri;

        avg += tick.vol * tick.pri;
        avgn += tick.vol;
    }

    public void day(String sym, Date d, double op, double hi, double lw, double ed) {
        if (ped == 0) {
            ped = ed;
            rsi10c = 0;
            rsi10p = 0;
            rsi10 = new LinkedList<Integer>();
            return;
        }
        int rsi = ed > ped ? 1 : -1;
        rsi10p = rsi10c;

        rsi10c += rsi;
        rsi10.addLast(rsi);

        if (rsi10.size() > 10) {
            rsi10c -= rsi10.getFirst();
            rsi10.remove();
        }
    }

    public void dayClose() {
        if (!inRange(ld, mh, mt)) return;
        day(sym, ld, op, hi, lw, ed);
    }
}
