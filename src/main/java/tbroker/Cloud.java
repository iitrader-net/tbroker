package tbroker;

import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.*;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public abstract class Cloud extends Util {
    static final int PING = 10 * 1000;
    String ip;
    int port;
    int seq;
    Hashtable<String, Ticket> msgMap;
    Socket socket;
    InputStream in;
    OutputStream out;
    String accPass;
    Sec sec;
    CloudTime tTime;
    Timer timer;

    class CloudIn extends Thread {
        public void run() {
            try {
                while (true) {
                    byte[] lb = new byte[2];
                    in.read(lb);
                    int len = (lb[0] << 8) | lb[1];
                    byte[] v = new byte[len];
                    int n = 0;
                    if ((n = in.read(v)) != len) {
                        log("len :" + len + "," + n);
                        throw new IOException();
                    }
                    cloudIn(v);
                }
            } catch (IOException e) {
                log(e);
                log("ChoudIn Exception , ignore");
                //	log("reset");
                //	reset();
            } catch (Exception e) {
                log(e);
            }
        }
    }

    class CloudTime extends Thread {
        Date update = new Date();

        public void run() {
            while (true) {
                try {
                    Thread.sleep(PING);
                } catch (Exception e) {
                    log(e);
                    return;
                }
                try {
                    _handShake("echo" + Cfg.sep + new Date().getTime());
                    update = new Date();
                } catch (Exception e) {
                    log(e);
                    return;
                }
            }
        }
    }

    class CloudCheck extends TimerTask {
        public void run() {
            long delta = new Date().getTime() - tTime.update.getTime();
            // log("delta = "+delta);
            if (delta > 3 * PING) {
                try {
                    log(E, "timeout");
                    reset();
                    if (accPass != null) login(accPass);
                    ready();
                } catch (Exception e) {
                    log(e);
                }
            }
        }
    }

    void cloudIn(byte[] v) throws Exception {
        StringTokenizer sk = new StringTokenizer(new String(v), Cfg.sep);
        String id = sk.nextToken();
        String res = sk.nextToken();
        Ticket t = msgMap.get(id);
        if (t != null) {
            synchronized (t) {
                t.res = res;
                t.ack = true;
                t.notify();
            }
        }
    }

    class Ticket {
        String id;
        byte[] bs;
        boolean ack;
        String res;

        Ticket(String _id) {
            id = _id;
            bs = null;
            ack = false;
            res = null;
        }

        Ticket set(String content) {
            bs = (id + Cfg.sep + content).getBytes();
            return this;
        }
    }

    Ticket newTicket() {
        Ticket t = new Ticket("" + seq++);
        return t;
    }

    synchronized void sendReq(Ticket t) throws Exception {
        msgMap.put(t.id, t);
        byte[] bs = t.bs;
        long len = bs.length;
        byte[] lh = new byte[2];
        lh[0] = (byte) ((len >> 8) & 0xff);
        lh[1] = (byte) ((len) & 0xff);
        out.write(lh);
        out.write(bs);
        dbg("out:" + new String(bs));
    }

    synchronized void connect() throws Exception {
        msgMap = new Hashtable<String, Ticket>();
        socket = new Socket();
        log("connect " + ip + ":" + port);
        InetSocketAddress isa = new InetSocketAddress(ip, port);
        socket.connect(isa, 10000);
        in = socket.getInputStream();
        out = socket.getOutputStream();
        new CloudIn().start();
        tTime = new CloudTime();
        tTime.start();
        if (timer == null) {
            timer = new Timer();
            timer.schedule(new CloudCheck(), PING, PING);
        }
    }

    private synchronized void reset() {
        try {
            if (socket != null) socket.close();
            socket = null;
            tTime.update = new Date();
        } catch (Exception e) {
            log(e);
        }
    }

    String getRes(Ticket t) throws Exception {
        String res;
        synchronized (t) {
            while (!t.ack) t.wait();
            res = t.res;
        }
        msgMap.remove(t.id);
        return res;
    }

    String _handShake(String load) throws Exception {
        Ticket ticket = newTicket();
        sendReq(ticket.set(load));
        String res = getRes(ticket);
        return res;
    }

    String handShake(String name, String value) {
        return handShake(name + Cfg.sep + value);
    }

    String handShake(String load) {
        while (true) {
            try {
                synchronized (this) {
                    while (socket == null) wait();
                }
                return _handShake(load);
            } catch (Exception e) {
                e.printStackTrace();
                reset();
            }
        }
    }

    void initSec(String key) throws Exception {
        dbg("initSec:" + key);
        sec = new Sec(key);
    }

    String handShakeSec(String name, String value) {
        return handShakeSec(name + Cfg.sep + value + Cfg.sep);
    }

    String handShakeSec(String load) {
        try {
            String msg = "enc" + Cfg.sep + sec.en(load);
            return sec.de(handShake(msg));
        } catch (Exception e) {
            log(e);
            return "";
        }
    }

    boolean isReady() {
        return socket != null;
    }

    private synchronized void ready() {
        notify();
    }

    public abstract void login(String ip) throws Exception;

    public void login(String _ip, int _port) throws Exception {
        ip = _ip;
        port = _port;
        seq = dbg ? 0 : (int) (new Date().getTime() & 0xfff);
        connect();
    }
}
