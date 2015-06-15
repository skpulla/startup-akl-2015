package up.startupakl2015;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by justintan on 14/06/15.
 */
public class RangeService extends Service {

    private BeaconManager beaconManager;
    private NotificationManager notificationManager;
    private int NOTIFICATION_ID;
    private static Region region;
    private boolean enteredBack;
    private double[] array;
    private int counter;
    private boolean exitTriggered;
    private View view;
    private long exitTimeStamp;
    private long currentTimeStamp;
    private long enterTimeStamp;
    private int prev;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        enteredBack = true;
        NOTIFICATION_ID = 321;
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        region = new Region("regionid", "b9407f30-f5f8-466e-aff9-25556b57fe6d", 63429, 2793);
        array = new double[10];
        counter = 0;
        exitTriggered = false;
        exitTimeStamp = 0;
        currentTimeStamp = 0;
        enterTimeStamp = 0;
        prev = 5;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startMonitoring();
        return START_STICKY;
    }



    private void startMonitoring() {
        if (beaconManager == null) {
            beaconManager = new BeaconManager(this);

            /**
             * Scanning
             */
            beaconManager.setBackgroundScanPeriod(TimeUnit.SECONDS.toMillis(1), 1);

            beaconManager.setRangingListener(new BeaconManager.RangingListener() {

                @Override
                public void onBeaconsDiscovered(Region paramRegion, List<Beacon> paramList) {
                    Beacon foundBeacon = null;
                    for (Beacon beacon : paramList) {
                        if (beacon.getMajor() == 63429) {
                            foundBeacon = beacon;
                            updateDistance(foundBeacon);
                        }
                    }
                }

            });

            beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
                @Override
                public void onServiceReady() {
                    try {
                        beaconManager.startRanging(region);
                        System.out.println("Service: started ranging");

                    } catch (RemoteException e) {
                        System.out.println(e);
                    }
                }
            });
        }
    }

    private void updateDistance(Beacon foundBeacon) {
        double calcDistance = Utils.computeAccuracy(foundBeacon);
        if (!exitTriggered) {
            if (calcDistance > 15) {
                calcDistance = getAvg(array) + 5;
            }
        } else {
            if (calcDistance > 5) {
                calcDistance = 5;
            }
        }
        array[counter % 10] = calcDistance;
        counter++;

        double distance  = getAvg(array);

        System.out.println("Service: calcDistance= "+calcDistance+" | distance= " + distance+" | counter= "+counter);
        currentTimeStamp = System.currentTimeMillis();
        if (distance > 4 && ((currentTimeStamp - exitTimeStamp) > 120000)) {
            postNotification("Your baby is waiting for you.", 0);
            exitTimeStamp = System.currentTimeMillis();
            array = new double[10];
            for (int i = 0; i < array.length; i++) {
                array[i] = 5;
            }
        }
//        } else {
//            if (enteredBack == false && (distance < 4)) {
//                postNotification("Took you awhile, but your back now.", 1);
//                enteredBack = true;
//                enterTimeStamp = System.currentTimeMillis();
//            }
//        }
    }

    private double getAvg(double[] array) {
        double result = 0;
        for (Double value : array) {
            result += value;
        }
        return result/array.length;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void postNotification(String msg, int id) {
            Intent notifyIntent = new Intent(RangeService.this, RangeService.class);
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivities(
                    RangeService.this,
                    0,
                    new Intent[]{notifyIntent},
                    PendingIntent.FLAG_UPDATE_CURRENT);
            Notification notification;
            if (id == 0) {
                notification = new Notification.Builder(RangeService.this)
                        .setColor(0xFFFF0000)
                        .setSmallIcon(R.drawable.alarm)
                        .setContentTitle("Precious Sense")
                        .setContentText(msg)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .build();
            } else {
                notification = new Notification.Builder(RangeService.this)
                        .setColor(0xFF00FF00)
                        .setSmallIcon(R.drawable.baby_ok)
                        .setContentTitle("Precious Sense")
                        .setContentText(msg)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .build();

            }
            notification.defaults |= Notification.DEFAULT_SOUND;
            notification.defaults |= Notification.DEFAULT_LIGHTS;
            notificationManager.notify(NOTIFICATION_ID, notification);

    }
}
