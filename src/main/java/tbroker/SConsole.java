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
import java.net.*;
import java.util.*;

public class SConsole extends Thread {

    int port;
    Shell shell;

    public SConsole(int _port, Shell _shell) {
        port = _port;
        shell = _shell;
    }

    public void run() {
        try {
            ServerSocket ss = new ServerSocket(port);
            while (true) {
                try {
                    Socket s = ss.accept();
                    InetSocketAddress ia = (InetSocketAddress) s.getRemoteSocketAddress();
                    if (!ia.toString().contains("127.0.0.1")) s.close();

                    BufferedReader cin =
                            new BufferedReader(new InputStreamReader(s.getInputStream()));
                    PrintStream out = new PrintStream(s.getOutputStream());
                    while (true) {
                        out.print(">");
                        out.flush();
                        String line = cin.readLine();
                        while (line.startsWith("\n") || line.startsWith("\r"))
                            line = cin.readLine();
                        try {
                            if (!shell.exe(line, out)) {
                                out.println("no such [" + line + "]");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
