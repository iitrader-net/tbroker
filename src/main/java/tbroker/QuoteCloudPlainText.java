package tbroker;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;

public class QuoteCloudPlainText extends QuoteCloud {
    public boolean support(String sym) {
        String res = handShake("sup", sym);
        return res.equals("t");
    }

    String getBindID(String sym) {
        return handShake("bind", sym);
    }
}
