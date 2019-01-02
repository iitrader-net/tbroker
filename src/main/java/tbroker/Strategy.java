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

public abstract class Strategy extends Util implements QuoteListener, DealListener {

    // number of the maximum stop loss
    int maxsln;

    String sym;

    // tag to identify the Strategy
    String ptag;

    // the basic unit to order
    int scale;

    // number of the stopLoss that have been made
    int stopLossN;

    DealListener dealListener;

    LinkedList<OrderExe> exes;

    // Open Interest
    OI oi;

    public void initScale(int _scale) {
        scale = _scale;
    }

    public void init(String _sym, String _ptag, DealListener _dealListener) {
        maxsln = 1;
        sym = _sym;
        ptag = _ptag;
        dealListener = _dealListener;
        oi = OIFactory.newOI(sym);
        exes = new LinkedList<OrderExe>();
    }

    public double idx(String sym, String i) {
        return Idx.getInstance(sym).getIdx(i);
    }

    public List<OrderExe> getOdrExes() {
        return exes;
    }

    public OI getOI() {
        return oi;
    }

    public void deal(Deal deal) {
        oi.add(deal);
        if (oi.vol() != 0) log("%s: OI: %d", ptag, oi.vol());
        else log("%s: empty", ptag);
        _deal(deal);
        dealListener.deal(deal);
    }

    public abstract void init(String _sym, DealListener _dealListener, Broker _broker, Quote quote)
            throws Exception;

    public abstract void dayOpen(Date date);

    public abstract void tick(Tick tick);

    public abstract void dayClose();

    String tag(String otag) {
        return ptag + "-" + otag;
    }

    OrderExe order(double pri, boolean buy, Date d, String otag) {
        OrderExe exe = newOrderExe(pri, buy, d, otag);
        exes.add(exe);
        return exe;
    }

    void order(int vol, double pri, Date date, String otag) {
        int n = Math.abs(vol);
        for (int i = 0; i < n; i++) {
            OrderExe exe = newOrderExe(pri, vol > 0, date, otag);
            exes.add(exe);
        }
    }

    void orderV(int vol, double pri, Date date, String otag, long timeOut) {
        int n = Math.abs(vol);
        for (int i = 0; i < n; i++) {
            OrderExe exe = newOrderExe(pri, vol > 0, date, otag);
            exe.setForce(timeOut);
            exes.add(exe);
        }
    }

    boolean inOdr() {
        for (OrderExe exe : exes) if (!exe.isDeal() && !exe.isCancel()) return true;
        return false;
    }

    void cover(Date date, double pri, String tag) {
        if (!inPos()) return;
        stopLossN++;
        order(-oi.vol(), pri, date, tag);
    }

    boolean inPos() {
        return oi.vol() != 0;
    }

    abstract OrderExe newOrderExe(double pri, boolean buy, Date date, String otag);

    abstract void _deal(Deal deal);
}
