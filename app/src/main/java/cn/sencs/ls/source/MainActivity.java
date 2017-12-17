package cn.sencs.ls.source;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import cn.sencs.nfcsocket.NfcClientSocket;


public class MainActivity extends AppCompatActivity{
    private Camera mCamera;
    public MyCameraView mPreview;
    public TextView serverStatus;
    public static String SERVERIP = "localhost";
    public static final int SERVERPORT = 9192;
    private Handler handler = new Handler();
    private Thread cThread;

    private Button nfcbtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nfcbtn = (Button)findViewById(R.id.nfcButton);
        serverStatus = (TextView) findViewById(R.id.textView);
        SERVERIP = getLocalIpAddress();

        nfcbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!NfcClientSocket.getInstance(getApplicationContext())
                        .isConnected()) {
                    int i = NfcClientSocket
                            .getInstance(getApplicationContext()).connect();
                    Log.d("BTR", i + "");
                    if (i == NfcClientSocket.CONNECT_SUCCESS) {
                        String tmp = SERVERIP;
                        byte[] response = NfcClientSocket.getInstance(
                                getApplicationContext()).send(
                                tmp.getBytes());
                    }
                } else {
                    String tmp = SERVERIP;
                    byte[] response = NfcClientSocket.getInstance(
                            getApplicationContext()).send(tmp.getBytes());
                }
            }
        });

        serverStatus.setText("Listening on IP: " + SERVERIP);


        mCamera = getCameraInstance();
        mPreview = new MyCameraView(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        cThread = new Thread(new MyServerThread(this,SERVERIP,SERVERPORT,handler));
        cThread.start();
    }






    /**
     * Get local ip address of the phone
     * @return ipAddress
     */
    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()&& inetAddress instanceof Inet4Address) { return inetAddress.getHostAddress().toString(); }
                }
            }
        } catch (SocketException ex) {
            Log.e("ServerActivity", ex.toString());
        }
        return null;
    }

    /**
     * Get camera instance
     * @return
     */
    public static Camera getCameraInstance()
    {
        Camera c=null;
        try{
            c=Camera.open();
        }catch(Exception e){
            e.printStackTrace();
        }
        return c;
    }

    private NfcClientSocket.NfcClientSocketListener clientListener
            = new NfcClientSocket.NfcClientSocketListener() {

        @Override
        public void onDiscoveryTag() {
            Log.d("BTR", "tag!");
        }

        @Override
        public Activity getCurrentActivity() {
            return MainActivity.this;
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        NfcClientSocket.getInstance(getApplicationContext()).unregister(
                clientListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        NfcClientSocket.getInstance(getApplicationContext()).register(
                clientListener);
    }
}
