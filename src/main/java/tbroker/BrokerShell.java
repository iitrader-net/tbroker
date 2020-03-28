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

public class BrokerShell implements Broker {

    Broker broker;
    LinkedList<Deal> deals = new LinkedList<Deal>();
    LinkedList<Order> orders = new LinkedList<Order>();

    BrokerShell(Broker broker) {
        this.broker = broker;
    }

    public void login(String acc_pass) throws Exception {
        broker.login(acc_pass);
    }

    class BrokerShellDealListener implements DealListener {
        DealListener parent;

        BrokerShellDealListener(DealListener parent) {
            this.parent = parent;
        }

        public void deal(Deal deal) {
            deals.add(deal);
            parent.deal(deal);
        }
    }

    public Order order(
            String sym, int vol, double pri, Date date, DealListener ol, int type, String tag) {
        Order o = broker.order(sym, vol, pri, date, new BrokerShellDealListener(ol), type, tag);
        orders.add(o);
        return o;
    }

    public boolean cancel(byte[] oid) {
        return broker.cancel(oid);
    }

    public List<String> getPositions() {
        return broker.getPositions();
    }

    public int getOI(String sym) {
        return broker.getOI(sym);
    }

    public double getRight() {
        return broker.getRight();
    }

    public boolean isSim() {
        return broker.isSim();
    }
}
