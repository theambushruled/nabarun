package com.example.sony.sampleclient;

import org.alljoyn.bus.BusAttachment;
import org.alljoyn.bus.BusException;
import org.alljoyn.bus.BusListener;
import org.alljoyn.bus.BusObject;
import org.alljoyn.bus.Mutable;
import org.alljoyn.bus.ProxyBusObject;
import org.alljoyn.bus.SessionListener;
import org.alljoyn.bus.SessionOpts;
import org.alljoyn.bus.SignalEmitter;
import org.alljoyn.bus.Status;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class Client extends Activity {
    /* Load the native alljoyn_java library. */
    static {
        System.loadLibrary("alljoyn_java");
    }



    private static final String TAG = "SessionlessClient";

    private EditText mEditText;
    private ArrayAdapter<String> mListViewArrayAdapter;
    private ListView mListView;
    private Menu menu;

    /* Handler used to make calls to AllJoyn methods. See onCreate(). */
    private BusHandler mBusHandler;

    private ProgressDialog mDialog;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        /* Make all AllJoyn calls through a separate handler thread to prevent blocking the UI. */
        HandlerThread busThread = new HandlerThread("BusHandler");
        busThread.start();
        mBusHandler = new BusHandler(busThread.getLooper());

        /* Connect to an AllJoyn object. */
        mBusHandler.sendEmptyMessage(BusHandler.CONNECT);
        //mHandler.sendEmptyMessage(MESSAGE_START_PROGRESS_DIALOG);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        /* Disconnect to prevent resource leaks. */
        mBusHandler.sendEmptyMessage(BusHandler.DISCONNECT);
    }

    class SignalService implements SimpleInterface, BusObject {
        /**
         * Intentionally empty implementation of Ping.  Since this
         * method is only used as a signal emitter, it will never be called
         * directly.
         */
        public void coords(int x, int y) throws BusException {
        }
    }



    /* This class will handle all AllJoyn calls. See onCreate(). */
    class BusHandler extends Handler {
        /*
         * Name used as the well-known name and the advertised name of the service this client is
         * interested in.  This name must be a unique name both to the bus and to the network as a
         * whole.
         *
         * The name uses reverse URL style of naming, and matches the name used by the service.
         */
        private static final String SERVICE_NAME = "org.alljoyn.bus.samples.simple";
        private static final short CONTACT_PORT=42;

        private BusAttachment mBus;
        private ProxyBusObject mProxyObj;
        private SimpleInterface mSimpleInterface;
        SignalEmitter emitter;

        private int     mSessionId;
        private boolean mIsInASession;
        private boolean mIsConnected;
        private boolean mIsStoppingDiscovery;

        /* These are the messages sent to the BusHandler from the UI. */
        public static final int CONNECT = 1;
        public static final int DISCONNECT = 3;
        public static final int PING = 4;


        private SignalService mySignalService = new SignalService();
        private SimpleInterface mSignalSimpleInterface = null;

        public BusHandler(Looper looper) {
            super(looper);

            mIsInASession = false;
            mIsConnected = false;
            mIsStoppingDiscovery = false;
        }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
            /* Connect to a remote instance of an object implementing the SimpleInterface. */
            case CONNECT: {
                org.alljoyn.bus.alljoyn.DaemonInit.PrepareDaemon(getApplicationContext());
                /*
                 * All communication through AllJoyn begins with a BusAttachment.
                 *
                 * A BusAttachment needs a name. The actual name is unimportant except for internal
                 * security. As a default we use the class name as the name.
                 *
                 * By default AllJoyn does not allow communication between devices (i.e. bus to bus
                 * communication). The second argument must be set to Receive to allow communication
                 * between devices.
                 */
                mBus = new BusAttachment(getPackageName(), BusAttachment.RemoteMessage.Receive);

                /*
                 * Create a bus listener class
                 */
                mBus.registerBusListener(new BusListener() {
                    @Override
                    public void foundAdvertisedName(String name, short transport, String namePrefix) {
                        logInfo(String.format("MyBusListener.foundAdvertisedName(%s, 0x%04x, %s)", name, transport, namePrefix));
                    }
                });

                Status status = mBus.registerBusObject(mySignalService, "/SignalService");
                if (Status.OK != status) {
                    logStatus("BusAttachment.registerBusObject()", status);
                    return;
                }

                /* To communicate with AllJoyn objects, we must connect the BusAttachment to the bus. */
                status = mBus.connect();
                logStatus("BusAttachment.connect()", status);
                if (Status.OK != status) {
                    finish();
                    return;
                }
                mBusHandler.sendEmptyMessage(BusHandler.PING);

                break;
            }

            /* Release all resources acquired in the connect. */
            case DISCONNECT: {
                mIsStoppingDiscovery = true;
                if (mIsConnected) {
                    Status status = mBus.leaveSession(mSessionId);
                    logStatus("BusAttachment.leaveSession()", status);
                }
                mBus.disconnect();
                getLooper().quit();
                break;
            }

            /*
             * Call the service's Ping method through the ProxyBusObject.
             *
             * This will also print the String that was sent to the service and the String that was
             * received from the service to the user interface.
             */
            case PING: {
                try {
                    if(emitter == null){
                        emitter = new SignalEmitter(mySignalService, 0, SignalEmitter.GlobalBroadcast.Off);
                        emitter.setSessionlessFlag(true);
                        mSignalSimpleInterface = emitter.getInterface(SimpleInterface.class);
                    }
                    if (mSignalSimpleInterface != null) {

                        mSignalSimpleInterface.coords(2,3);
                    }
                } catch (BusException ex) {
                    logException("SimpleInterface.Ping()", ex);
                }
                break;
            }
            default:
                break;
            }
        }

        /* Helper function to send a message to the UI thread. */

    }

    private void logStatus(String msg, Status status) {
        String log = String.format("%s: %s", msg, status);
        if (status == Status.OK) {
            Log.i(TAG, log);
        } else {
            Log.e(TAG, log);
        }
    }

    private void logException(String msg, BusException ex) {
        String log = String.format("%s: %s", msg, ex);
        Log.e(TAG, log, ex);
    }

    /*
     * print the status or result to the Android log. If the result is the expected
     * result only print it to the log.  Otherwise print it to the error log and
     * Sent a Toast to the users screen.
     */
    private void logInfo(String msg) {
        Log.i(TAG, msg);
    }
}
