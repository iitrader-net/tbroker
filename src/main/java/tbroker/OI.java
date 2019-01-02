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

public class OI {

    String sym;

    int vol;

    // TX: taxRate = 0.00002
    private double taxRate, commit, gra;

    private double net;

    LinkedList<Deal> deals;

    OI(String _sym, double _taxRate, double _commit, double _gra) {
        sym = _sym;
        taxRate = _taxRate;
        commit = _commit;
        gra = _gra;
        vol = 0;
        net = 0;
        deals = new LinkedList<Deal>();
    }

    public OI add(Deal d) {
        if (sym != null && !d.sym.equals(sym))
            throw new RuntimeException("OI:" + sym + d.toString());
        deals.addLast(d);
        net -= ((d.vol * d.pri));
        vol += d.vol;
        return this;
    }

    public double getCost() {
        double tax = 0;
        for (Deal d : deals) tax += Math.abs(d.vol) * d.pri * taxRate * gra;
        return tax + deals.size() * commit;
    }

    public double getGain() {
        if (vol != 0) throw new RuntimeException("OI:" + sym + ",vol:" + vol);
        return net * gra;
    }

    public double getGain(double last) {
        if (vol == 0) return getGain();
        else return (net + vol * last) * gra;
    }

    public double getBE() {
        return Math.abs(net / vol);
    }

    public int vol() {
        return vol;
    }
}
