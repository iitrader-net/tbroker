package tbroker;

import java.util.*;

public class QuoteHelper extends Util {
    Hashtable<String, LinkedList<QuoteListener>> listeners;

    private class TaskOpen extends TimerTask {
        public void run() {
            for (String key : listeners.keySet()) {
                LinkedList<QuoteListener> ll = listeners.get(key);
                for (QuoteListener l : ll) l.dayOpen(new Date());
            }
        }
    }

    private class TaskClose extends TimerTask {
        public void run() {
            for (String key : listeners.keySet()) {
                LinkedList<QuoteListener> ll = listeners.get(key);
                for (QuoteListener l : ll) l.dayClose();
            }
        }
    }

    void book(String tick, TimerTask task) {
        Date now = new Date();
        Date bookTick = parseL(format(now) + " " + tick);
        long delta = bookTick.getTime() - now.getTime();
        if (delta > 0) {
            new Timer().schedule(task, delta);
        } else {
            log(E, "ignore " + tick);
        }
    }

    public QuoteHelper(String openTick, String closeTick) {
        listeners = new Hashtable<String, LinkedList<QuoteListener>>();
        if (openTick != null) {
            book(openTick, new TaskOpen());
        }
        if (closeTick != null) {
            book(closeTick, new TaskClose());
        }
    }

    public void bind(String sym, QuoteListener l) {
        LinkedList<QuoteListener> ll = listeners.get(sym);
        if (ll == null) {
            ll = new LinkedList<QuoteListener>();
            listeners.put(sym, ll);
        }
        ll.add(l);
    }
}
