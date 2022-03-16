package violentapplications.asterixobelixsoundboard;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.facebook.messenger.MessengerThreadParams;
import com.facebook.messenger.MessengerUtils;
import com.facebook.messenger.ShareToMessengerParams;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SHARE_TO_MESSENGER = 1;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private MessengerThreadParams mThreadParams;
    private boolean mPicking;
    private MediaPlayer mp;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) recyclerView.getLayoutParams();

        Adapter myAdapter = new Adapter(this);
        myAdapter.setSoundList(getSoundList());
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(myAdapter);

        //Intent for Send via Messenger
        Intent intent = getIntent();
        if (Intent.ACTION_PICK.equals(intent.getAction())) {
            mThreadParams = MessengerUtils.getMessengerThreadParamsForIntent(intent);
            mPicking = true;
        }

        //Store two functionalities in adapter
        myAdapter.setOnItemClickListener(new Adapter.OnItemClickListener() {
            //Play the sound on button click
            @Override
            public void onItemClick(Sound item) {
                try {
                    onPause();
                    mp = MediaPlayer.create(getApplicationContext(), item.getUri());
                    mp.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //Send via Messenger
            @Override
            public void onSendMessage(Sound item) {
                ShareToMessengerParams shareToMessengerParams =
                        ShareToMessengerParams.newBuilder(item.getUri(), "audio/mpeg")
                                .build();

                if (mPicking) {
                    MessengerUtils.finishShareToMessenger(MainActivity.this, shareToMessengerParams);
                } else {
                    MessengerUtils.shareToMessenger(
                            MainActivity.this,
                            REQUEST_CODE_SHARE_TO_MESSENGER,
                            shareToMessengerParams);
                }
            }
        });

        //On long button press - show alert dialog and allow user to set sound as notification/alarm sound
        myAdapter.setOnItemLongClickListener(new Adapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(final Sound item) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.setAs);
                builder.setCancelable(true);
                //Set as Notification sound
                builder.setNeutralButton(R.string.option1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /*
                        In versions >= 23 of Android,
                        system requires MANAGE_WRITE_SETTINGS permission in order to
                        make this function work.
                        */
                        if (Build.VERSION.SDK_INT >= 23) {
                            if (checkPermission()) {
                                //If user has permission, allow him to set it
                                if (Settings.System.canWrite(MainActivity.this)) {
                                    RingtoneManager.setActualDefaultRingtoneUri(
                                            MainActivity.this,
                                            RingtoneManager.TYPE_NOTIFICATION, item.getUri());
                                    Toast.makeText(getApplicationContext(), R.string.notificationSetSuccessful, Toast.LENGTH_LONG).show();
                                } else {
                                    //Refer user to settings if insufficient permission
                                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS)
                                            .setData(Uri.parse("package:" + getPackageName()))
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    Toast.makeText(getApplicationContext(), R.string.notificationTryAgain, Toast.LENGTH_LONG).show();
                                }
                                Log.e("value", "already granted");
                            } else {
                                requestPermission();
                            }
                        } else {
                            //Versions <23 of Android don't require permission
                            RingtoneManager.setActualDefaultRingtoneUri(
                                    MainActivity.this,
                                    RingtoneManager.TYPE_NOTIFICATION, item.getUri());
                            Toast.makeText(getApplicationContext(), R.string.notificationSetSuccessful, Toast.LENGTH_LONG).show();
                            Log.e("value", "already granted");
                        }
                    }

                });
                //Set as alarm sound
                builder.setPositiveButton(R.string.option2, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /*
                        In versions >= 23 of Android,
                        system requires MANAGE_WRITE_SETTINGS permission in order to
                        make this function work.
                        */
                        if (Build.VERSION.SDK_INT >= 23) {
                            if (checkPermission()) {
                                if (Settings.System.canWrite(MainActivity.this)) {
                                    RingtoneManager.setActualDefaultRingtoneUri(
                                            MainActivity.this,
                                            RingtoneManager.TYPE_RINGTONE, item.getUri());
                                    Toast.makeText(getApplicationContext(), R.string.alarmSetSuccessful, Toast.LENGTH_LONG).show();
                                } else {
                                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS)
                                            .setData(Uri.parse("package:" + getPackageName()))
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    Toast.makeText(getApplicationContext(), R.string.notificationTryAgain, Toast.LENGTH_LONG).show();
                                }
                                Log.e("value", "already granted");
                            } else {
                                requestPermission();
                            }
                        } else {
                            //Versions <23 of Android don't require permission
                            RingtoneManager.setActualDefaultRingtoneUri(
                                    MainActivity.this,
                                    RingtoneManager.TYPE_RINGTONE, item.getUri());
                            Toast.makeText(getApplicationContext(), R.string.alarmSetSuccessful, Toast.LENGTH_LONG).show();
                            Log.e("value", "already granted");
                        }
                    }

                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
        //end of onCreate
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(MainActivity.this, R.string.permissionInfo, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), R.string.notificationTryAgain, Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mp != null) {
            mp.stop();
            mp.release();
            mp = null;
        }
    }

    private List<Sound> getSoundList() {
        Field[] soundRaws = R.raw.class.getFields();
        String[] stringArray = getResources().getStringArray(R.array.strings);
        List<Sound> soundList = new ArrayList<>();
        for (int i = 0; i < stringArray.length; i++) {
            soundList.add(new Sound(stringArray[i],
                    Uri.parse("android.resource://" + getPackageName() + "/" + getResources().getIdentifier(soundRaws[i].getName(), "raw", getPackageName()))));
        }
        return soundList;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
