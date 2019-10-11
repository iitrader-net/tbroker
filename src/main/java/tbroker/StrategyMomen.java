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

public class StrategyMomen extends StrategyTX {

    // threshold parameter
    double th;
    // stoploss parameter
    double st;

    public void init(String _sym, DealListener _dealListener, Broker _broker, Quote quote)
            throws Exception {
        super.init(_sym, "p", -50, 12, _dealListener, _broker);
        th = 5;
        st = -10;
        maxsln = 1;
        IdxImplCB.init(sym, quote);
        quote.bind(sym, this);
    }

    boolean err1 = false;

    public void _tick(Tick tick) {
        Date date = tick.getDate();
        Date oint = combine(date, "10:16:00");
        if (date.getTime() < oint.getTime()) return;
        double cb = tick.pri - idx(sym, "COW_BEAR");
        double rsi10c = idx(sym, "RSI10");
        if (!inPos() && !inOdr() && tick.pri > open + th && stopLossN < maxsln) {
            double cba = idx(sym, "CBA");
            double cbn = idx(sym, "CBN");
            if (cb > -8 && rsi10c <= 6) {
                order(scale, tick.pri - 1, date, "go");
                log(
                        E,
                        "%s price=%.2f, cb=%.2f %.2f %.2f, rsi10c=%.2f",
                        sym,
                        tick.pri,
                        cb,
                        cba,
                        cbn,
                        rsi10c);
            } else if (!err1) {
                log(
                        E,
                        "SKIP %s price=%.2f, cb=%.2f %.2f %.2f, rsi10c=%.2f",
                        sym,
                        tick.pri,
                        cb,
                        cba,
                        cbn,
                        rsi10c);
                err1 = true;
            }
        } else if (inPos() && tick.pri < stp && stopLossN == 0) {
            cover(date, stp, "st");
        }
    }

    public void _deal(Deal deal) {
        if (oi.vol != 0) {
            stp = deal.pri + st;
            log(E, "%s st=%.2f, stp=%.2f", sym, st, stp);
        }
    }
}
