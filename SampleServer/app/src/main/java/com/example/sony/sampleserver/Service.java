package com.example.sony.sampleserver;

import org.alljoyn.bus.BusAttachment;
import org.alljoyn.bus.BusListener;
import org.alljoyn.bus.BusObject;
import org.alljoyn.bus.Mutable;
import org.alljoyn.bus.SessionOpts;
import org.alljoyn.bus.SessionPortListener;
import org.alljoyn.bus.Status;
import org.alljoyn.bus.annotation.BusSignalHandler;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class Service extends Activity {
    /* Load the native alljoyn_java library. */
    static {
        System.loadLibrary("alljoyn_java");
    }

    private static final String TAG = "SessionlessService";







    /* The AllJoyn object that is our service. */
    private SimpleService mSimpleService;

    /* Handler used to make calls to AllJoyn methods. See onCreate(). */
    private Handler mBusHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        /* Make all AllJoyn calls through a separate handler thread to prevent blocking the UI. */
        HandlerThread busThread = new HandlerThread("BusHandler");
        busThread.start();
        mBusHandler = new BusHandler(busThread.getLooper());

        /* Start our service. */
        mSimpleService = new SimpleService();
        mBusHandler.sendEmptyMessage(BusHandler.ADDMATCH);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection

            return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        /* Disconnect to prevent any resource leaks. */
        mBusHandler.sendEmptyMessage(BusHandler.DISCONNECT);
    }

    /* The class that is our AllJoyn service.  It implements the SimpleInterface. */
    class SimpleService implements SimpleInterface, BusObject {

        /*
         * This is the Signal Handler code which has the interface name and the name of the signal
         * which is sent by the client. It prints out the string it receives as parameter in the
         * signal on the UI
         *
         * This code also prints the string it received from the user and the string it is
         * returning to the user to the screen.
         */

        @BusSignalHandler(iface = "org.alljoyn.bus.test.sessions", signal = "coords")
        public void coords(int x, int y) {

            Log.i(TAG, "x  : "+ x + " y :" + y);


        }


    }



    /* This class will handle all AllJoyn calls. See onCreate(). */
    class BusHandler extends Handler {
        /*
         * Name used as the well-known name and the advertised name.  This name must be a unique name
         * both to the bus and to the network as a whole.  The name uses reverse URL style of naming.
         */
        private static final String SERVICE_NAME = "org.alljoyn.bus.test.sessions";
        private static final short CONTACT_PORT=42;

        private BusAttachment mBus;

        /* These are the messages sent to the BusHandler from the UI. */
        public static final int ADDMATCH = 1;
        public static final int DISCONNECT = 2;


        public BusHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            /* Connect to the bus and start our service. */
            case ADDMATCH: {
                org.alljoyn.bus.alljoyn.DaemonInit.PrepareDaemon(getApplicationContext());
                /*
                 * All communication through AllJoyn begins with a BusAttachment.
                 *
                 * A BusAttachment needs a name. The actual name is unimportant except for internal
                 * security. As a default we use the class name as the name.
                 *
                 * By default AllJoyn does not allow communication between devices (i.e. bus to bus
                 * communication).  The second argument must be set to Receive to allow
                 * communication between devices.
                 */
                mBus = new BusAttachment(getPackageName(), BusAttachment.RemoteMessage.Receive);

                /*
                 * Create a bus listener class
                 */
                mBus.registerBusListener(new BusListener());

                /*
                 * To make a service available to other AllJoyn peers, first register a BusObject with
                 * the BusAttachment at a specific path.
                 *
                 * Our service is the SimpleService BusObject at the "/SimpleService" path.
                 */
                Status status = mBus.registerBusObject(mSimpleService, "/SimpleService");
                logStatus("BusAttachment.registerBusObject()", status);
                if (status != Status.OK) {
                    finish();
                    return;
                }

                /*
                 * The next step in making a service available to other AllJoyn peers is to connect the
                 * BusAttachment to the bus with a well-known name.
                 */
                /*
                 * connect the BusAttachement to the bus
                 */
                status = mBus.connect();
                logStatus("BusAttachment.connect()", status);
                if (status != Status.OK) {
                    finish();
                    return;
                }


                /*
                 *  We register our signal handler which is implemented inside the SimpleService
                 */
                status = mBus.registerSignalHandlers(mSimpleService);
                if (status != Status.OK) {
                    Log.i(TAG, "Problem while registering signal handler");
                    return;
                }

                status = mBus.addMatch("sessionless='t'");
                if (status == Status.OK) {
                    Log.i(TAG,"AddMatch was called successfully");
                }

                break;
            }

            /* Release all resources acquired in connect. */
            case DISCONNECT: {
                /*
                 * It is important to unregister the BusObject before disconnecting from the bus.
                 * Failing to do so could result in a resource leak.
                 */
                mBus.unregisterBusObject(mSimpleService);
                mBus.disconnect();
                mBusHandler.getLooper().quit();
                break;
            }

            default:
                break;
            }
        }
    }

    private void logStatus(String msg, Status status) {
        String log = String.format("%s: %s", msg, status);
        if (status == Status.OK) {
            Log.i(TAG, log);
        } else {
            Log.e(TAG, log);
        }
    }
}
