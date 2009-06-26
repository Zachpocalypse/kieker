package kieker.tests.compileTimeWeaving.twoConcurrentMethodsExample;

import kieker.tpmon.annotation.TpmonExecutionMonitoringProbe;

/**
 * kieker.tests.compileTimeWeaving.twoConcurrentMethodsExample.Starter
 *
 * A simple test and demonstration scenario for Kieker's 
 * monitoring component tpmon.
 * 
 * @author Matthias Rohr
 * History:
 * 2009/02/20: Reduced text length
 * 2008/10/20: Initial version
 *
 */

public class Starter extends Thread{
   public static void main(String[] args) throws InterruptedException {
	for (int i = 0; i < 10000; i++) {
		new Starter().start();		
                Thread.sleep((int)(Math.random() * 49d)+1); // wait between requests
	}
	System.exit(0);
    }

    public void run() {
            waitP();
            work();
    }

    @TpmonExecutionMonitoringProbe()
    public void waitP() {
	try{Thread.sleep((int)(Math.random() * 50d)+475);} catch (Exception e){}
    }

    static boolean boolvar = true;
    @TpmonExecutionMonitoringProbe()
    private void work() {
        int a = (int)(Math.random() * 5d);
        for (int i=0; i<2500000; i++) { a += i/1000;}
        if (a % 10000 == 0 ) boolvar = false;
    }
} 
