package up.startupakl2015;

import android.app.Activity;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Range;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;

import java.util.List;


public class MainActivity extends Activity {

    private Beacon beacon;
    private BeaconManager beaconManager;
    private Region region;
    private View view;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = this.getWindow().getDecorView();
        view.setBackgroundColor(0xFF00FF00);

        Intent intent = new Intent(this, RangeService.class);
        startService(intent);

    }

    @Override
    public void onDestroy() {
        stopService(new Intent(this, RangeService.class));
        super.onDestroy();
    }


}
