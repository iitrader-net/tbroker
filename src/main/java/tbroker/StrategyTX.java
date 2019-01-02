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

public abstract class StrategyTX extends Strategy {

    private class TaskCover extends TimerTask {
        public void run() {
            Date now = new Date();
            log(E, "dcover: %s %.2f", format(now, "HH:mm:ss"), ltick.pri);
            tick(ltick);
        }
    }

    private boolean isEnd, isClr;
    Date dtail, dhead;

    public void init(
            String _sym,
            String _ptag,
            int _mhead,
            int _mtail,
            DealListener _dealListener,
            Broker _broker) {
        super.init(_sym, _ptag, _dealListener);
        mhead = _mhead;
        mtail = _mtail;
        broker = _broker;
        isDay = true;
        if (!broker.isSim()) {
            Date now = new Date();
            Date d = addMonth(now, 2);
            d.setDate(1);
            if (_sym.equals("tx" + format(d, "yyyyMM")) && getCloseDay().getTime() >= now.getTime())
                forceND = true;

            Date tri = parseL(format(now) + " 13:29:50");
            long delta = tri.getTime() - now.getTime();
            if (delta > 0) {
                log(E, "dcover: " + formatL(tri));
                new Timer().schedule(new TaskCover(), delta);
            } else {
                log(E, "dcover: ignore");
            }
        }
    }

    public String toString() {
        String ts = (ltick == null) ? "__:__:__" : format(ltick.getDate(), "HH:mm:ss");
        double net = 0;
        double pri = 0;
        int oin = 0;
        if (ltick != null) {
            pri = ltick.pri;
            net = oi.getGain(ltick.pri);
            oin = oi.vol();
        }
        return String.format(
                "%3d %6s %6s %6.0f %6.0f %6.0f %6.0f %6.0f %8s",
                oin, ptag, sym, open, pri, stp, upp, net, ts);
    }

    public void dayOpen(Date date) {
        String head, tail;
        head = "8:45:30";
        tail = "13:20:00";
        Date pivot = parse(sym.substring(2) + "01");
        mh = addDay(pivot, mhead);
        mt = addDay(pivot, mtail);
        stp = 0;
        upp = 0;
        if (!inRange(date)) return;

        dtail = parseL(format(date) + " " + tail);
        dhead = parseL(format(date) + " " + head);

        exes = new LinkedList<OrderExe>();

        stopLossN = 0;
        isEnd = false;
        isClr = false;

        log(E, "dayOpen:" + sym + Cfg.sep + formatL(date));
    }

    public void tick(Tick tick) {
        if (!inRange(tick.getDate())) return;

        for (OrderExe exe : exes) exe.tick(tick);
        Date date = tick.getDate();
        long ts = tick.getDate().getTime();
        long tl = dtail.getTime();
        ltick = tick;

        if (open == 0) open = tick.pri;
        if (ts > tl) {
            if (isDay && inPos() && !isEnd) {
                isEnd = true;
                order(-oi.vol(), tick.pri, date, tag("end"));
            }
        } else if (ts > tl - 60 * 1000) {
            if (isDay && !isClr) {
                for (OrderExe exe : exes) exe.clear(tick.getDate());
                isClr = true;
            }
        } else if (date.getTime() > dhead.getTime()) _tick(tick);
    }

    public void dayClose() {
        if (ltick == null || !inRange(ltick.getDate())) return;
        log(E, "dayClose:" + sym + Cfg.sep + formatL(ltick.getDate()));
        if (inPos() && isDay) {
            double dp = (oi.vol() > 0) ? ltick.pri - Cfg.wear : ltick.pri + Cfg.wear;
            Order odr =
                    new Order(
                            sym,
                            -oi.vol(),
                            dp,
                            ltick.getDate(),
                            0,
                            "close",
                            this,
                            "dummp".getBytes());
            Deal deal = new Deal(odr, dp, ltick.getDate());
        }
    }

    Broker broker;

    // latest tick
    Tick ltick;

    /// this strategy is intra-day trade
    boolean isDay = true;

    /// true: the intra-day trade type is not available on the market
    boolean forceND = false;

    double open;

    /// stop loss price if used
    double stp;

    /// up scale price if used
    double upp;

    /// the time to activate the Strategy
    Date mh;

    /// the time to diactivate the Stragegy
    Date mt;

    int mhead, mtail;

    Date getCloseDay() {
        Date d = new Date();
        int[] i = new int[] {18, 17, 16, 15, 21, 20, 19};
        d.setDate(1);
        d.setDate(i[d.getDay()] + 1);
        return d;
    }

    boolean inRange(Date date) {
        return inRange(date, mh, mt);
    }

    OrderExe newOrderExe(double pri, boolean buy, Date date, String otag) {
        boolean id = isDay;
        if (forceND) id = false;
        int type = id ? Broker.DAY : 0;
        return new OrderExe(broker, this, sym, buy, pri, type, tag(otag), date);
    }

    abstract void _tick(Tick tick);
}
