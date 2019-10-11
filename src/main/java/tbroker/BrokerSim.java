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

public class BrokerSim extends Util implements Broker {
    int oid;
    Hashtable<String, BrokerSimOdr> odrs;
    File dir;
    List<String> positions;
    Hashtable<String, Integer> ois;
    List<String> qsym;
    Quote quote;
    // acc_pass,dir,[0:1]
    public void login(String acc_pass) {
        try {
            String[] ss = acc_pass.split(Cfg.sep);
            dir = new File(ss[1]);
            boolean brokersim = ss[2].equals("1");
            log("login,acc_pass=%s:%s,%s", acc_pass, dir, "" + brokersim);
            if (!dir.exists()) dir.mkdir();
            odrs = new Hashtable<String, BrokerSimOdr>();
            positions = new LinkedList<String>();
            ois = new Hashtable<String, Integer>();
            qsym = new LinkedList<String>();
            load();
            if (brokersim) quote = (Quote) Class.forName("tbroker.QuoteCloud").newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            log(e);
        }
        println(E, "BrokerSim.login never return");
        try {
            while (true) Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void load() {
        try {
            File fin = new File(dir, "positions.txt");
            if (!fin.exists()) return;
            BufferedReader bin =
                    new BufferedReader(new InputStreamReader(new FileInputStream(fin)));
            String s;
            positions.clear();
            while ((s = bin.readLine()) != null) {
                String[] ss = s.split(Cfg.sep);
                positions.add(ss[0]);
                ois.put(ss[0], new Integer(ss[1]));
            }
        } catch (Exception e) {
            e.printStackTrace();
            log(e);
        }
    }

    void save() {
        try {
            File fin = new File(dir, "positions.txt");
            PrintWriter out = new PrintWriter(new FileOutputStream(fin));
            for (String sym : positions) out.println(sym + Cfg.sep + getOI(sym));
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            log(e);
        }
    }

    public String toRSym(String sym) {
        return sym;
    }

    public String toSym(String rsym) {
        return rsym;
    }

    public boolean cancel(byte[] _oid) {
        String oid = new String(_oid);
        BrokerSimOdr o = odrs.get(oid);
        if (o.done || o.cancel) return false;
        else odrs.remove(oid);
        return true;
    }

    class BrokerSimOdr implements QuoteListener {
        boolean cancel;
        boolean done;
        Order odr;

        BrokerSimOdr(Order _odr) {
            odr = _odr;
        }

        public void dayOpen(Date date) {}

        public void tick(Tick tick) {
            double pri = tick.pri;
            Date date = tick.getDate();
            int vol = tick.vol;
            if (odr == null || odr.isDeal()) return;
            if (!cancel
                    && odr.vol != 0
                    && !done
                    && (date.getTime() - odr.date.getTime()) > Cfg.latencyMS
                    && (((odr.pri - pri) * odr.vol >= 0) || (odr.pri == 0))) {
                double dp = pri;
                if (odr.pri == 0) dp = (odr.vol > 0) ? dp + Cfg.wear : dp - Cfg.wear;
                new Deal(odr, pri, date);

                if (getOI(odr.sym) != 0) {
                    int newn = getOI(odr.sym) + odr.vol;
                    if (newn == 0) {
                        positions.remove(odr.sym);
                        ois.remove(odr.sym);
                    } else {
                        ois.put(odr.sym, new Integer(newn));
                    }
                } else {
                    positions.add(odr.sym);
                    ois.put(odr.sym, new Integer(odr.vol));
                }
                save();
            }
        }

        public void dayClose() {}
    }

    public Order order(
            String sym,
            int vol,
            final double pri,
            final Date date,
            final DealListener ol,
            int type,
            String tag) {
        log("order: %s %d %.2f %d", sym, vol, pri, type);
        final Order _o =
                new Order(sym, vol, pri, date, type, tag, ol, ("simroid" + oid++).getBytes());
        BrokerSimOdr o = new BrokerSimOdr(_o);
        odrs.put(new String(_o.oid), o);
        if (quote != null) {
            quote.bind(sym, o);
        } else {
            new Thread(
                            new Runnable() {
                                public void run() {
                                    try {
                                        Thread.sleep(1000);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    ol.deal(new Deal(_o, pri, date));
                                }
                            })
                    .start();
        }
        return _o;
    }

    public double getRight() {
        return 400000;
    }

    public int getOI(String sym) {
        Integer i = ois.get(sym);
        return i == null ? 0 : i.intValue();
    }

    public List<String> getPositions() {
        return positions;
    }

    public boolean isSim() {
        return false;
    }
}
