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

public class OrderExe extends Util implements DealListener {
    private Broker broker;
    private DealListener strategy;
    private String tag;
    private int type;
    private int level;
    private long timeOut;
    private Date simDealDate;
    private boolean done, cancel;
    Order odr;

    public OrderExe(
            Broker _broker,
            DealListener _strategy,
            String sym,
            boolean buy,
            double pri,
            int _type,
            String _tag,
            Date date) {
        broker = _broker;
        strategy = _strategy;
        tag = _tag;
        type = _type;
        level = 0;
        timeOut = 0;
        int vol = buy ? 1 : -1;
        log("%s,%d,%5.2f,%d,%s,%s", sym, vol, pri, type, tag, formatL(date));
        odr = broker.order(sym, vol, pri, date, this, type, tag);
        done = false;
        cancel = false;
    }

    void tick(Tick tick) {
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
            if (broker.isSim()) {
                new Deal(odr, pri, date);
            } else {
                log(E, "SIM:%s %.2f", odr.toString(), pri);
                simDealDate = date;
            }
            done = true;
        }

        if (odr == null || odr.isDeal()) return;
        if (timeOut != 0 && date.getTime() - odr.date.getTime() > timeOut && level == 0) {

            mod(0, date, tag + "-F");
            level = 1;
        }
        if (odr.pri != 0
                && simDealDate != null
                && (date.getTime() - simDealDate.getTime()) > 8 * 1000) {

            simDealDate = null;
            mod(0, date, tag + "-simTR");
        }
    }

    public void setForce(long _timeOut) {
        timeOut = _timeOut;
    }

    public boolean cancel(Date date) {
        log("cancel:%s,%s", tag, formatL(date));
        return cancel = broker.cancel(odr.oid);
    }

    public void mod(double pri, Date date, String _tag) {
        log("mod:%s,%5.2f,%s", tag, pri, formatL(date));
        if (cancel(date)) {
            log("%s,%d,%5.2f,%d,%s,%s", odr.sym, odr.vol, pri, type, tag, formatL(date));
            odr = broker.order(odr.sym, odr.vol, pri, date, this, type, tag = _tag);
            cancel = false;
        }
    }

    public void clear(Date date) {
        if (!odr.isDeal()) cancel(date);
    }

    public void deal(Deal d) {
        strategy.deal(d);
    }

    public boolean isDeal() {
        return odr.isDeal();
    }

    public boolean isCancel() {
        return cancel;
    }
}
