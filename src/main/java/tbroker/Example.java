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

public class Example {
    public static void main(String[] args) {
        try {
            Cfg.init();
            Shell s = new Shell(System.in, System.out);
            s.exe("quote tbroker.QuoteCloud tbroker.net,ibanezchen@gmail.com,zinnia");
            s.exe("broker tbroker.BrokerSim acc_pass,dummy");
            s.exe("right");
            s.exe("sconsole 5690");
            s.exe("tbroker 1 tx,StrategyMomen:1");
            s.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
