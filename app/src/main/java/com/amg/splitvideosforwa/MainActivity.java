package com.amg.splitvideosforwa;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.Statistics;
import com.arthenica.mobileffmpeg.StatisticsCallback;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    public static boolean SETTINGS_ACT = false;
    private String outputFolder;
    Operation current;
    private SharedPreferences preferences;
    private int seconds;

    ProgressBar progressBar;

    TextView percentText;
    private int parts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        date.setTime(3000);
        date.setTime(date.getTime()+4000);
        String s = format.format(date);
        Log.d("OUT",s);
         */
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        this.preferences = defaultSharedPreferences;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.READ_MEDIA_VIDEO") != 0 && ContextCompat.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE") != 0 && ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
                ActivityCompat.requestPermissions(this, new String[]{"android.permission.READ_MEDIA_VIDEO", "android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"}, 1234);
            } else {

                initializeOutput();

                initializeViews();
            }
        }
        else {
            if ( ContextCompat.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE") != 0 && ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
                ActivityCompat.requestPermissions(this, new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"}, 1234);
            } else {

                initializeOutput();

                initializeViews();
            }
        }

    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onResume() {
        super.onResume();
        if (SETTINGS_ACT) {
            initializeOutput();

            initializeViews();
            SETTINGS_ACT = false;
        }
    }

    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, android.app.Activity
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1234) {
            try {
                if (grantResults.length == 0 || grantResults[0] != 0) {
                    return;
                }
                initializeOutput();

                initializeViews();
            } catch (Exception unused) {
            }
        }
    }

    private void initializeOutput() {
        if (Build.VERSION.SDK_INT > 29) {
            this.outputFolder = getExternalFilesDir(null).getAbsolutePath() + "/Splitter/";
        } else {
            this.outputFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Splitter/";
        }
        File file = new File(this.outputFolder);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    private void initializeViews() {

        String s = preferences.getString("seconds", "30");
        try {
            seconds = Integer.parseInt(s);
        }
        catch (Exception e){
            seconds = 30;
        }
        this.percentText = findViewById(R.id.percent);
        progressBar = findViewById(R.id.progress_circular);

        findViewById(R.id.split_fast_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                current = Operation.SplitFast;
                selectVideo();
            }
        });

        /*findViewById(R.id.split_quality_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                current = Operation.SplitHighQuality;
                selectVideo();
            }
        });

         */

        findViewById(R.id.output_layout).setOnClickListener(new View.OnClickListener() { // from class: com.amg.compressaudio.MainActivity.7
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                OutputsActivity.launch(MainActivity.this, new File(MainActivity.this.outputFolder));
            }
        });
    }

    private ActivityResultLauncher<Intent> selectVideoAct = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Uri selectedVideoUri = data.getData();
                        String selectedFilePath = RealPathUtil.getRealPath(MainActivity.this,selectedVideoUri);

                        if (selectedFilePath != null) {
                            String command = "";
                            String fileName = "";
                            String output = "";
                            parts = 0;
                            percentText.setText(0 + "%");
                            String input = selectedFilePath.replace(" ","\' \'");
                            progressBar.setVisibility(View.VISIBLE);
                            switch (current){
                                case SplitFast:
                                    //progressDialog.show();
                                    fileName = RealPathUtil.getFileName(input);
                                    //command = "-y -i "+ input+" -acodec copy -f segment -segment_time " +seconds
                                    //        +" -vcodec copy -reset_timestamps 1 -map 0 "
                                    //        + output;
                                    //command = "-y -i "+ input+" -c:v libx264 -c:a aac -strict experimental -b:a 192k -force_key_frames \"expr:gte(t,n_forced*" +seconds
                                    //        +")\" -f segment -segment_time "+seconds
                                    //        +" -reset_timestamps 1 -map 0 "
                                    //        + output;
                                    int duration = MediaPlayer.create(MainActivity.this, selectedVideoUri).getDuration();
                                    long start = 0, end = seconds*1000;
                                    if (end > duration){
                                        Toast.makeText(MainActivity.this,getString(R.string.msj),Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                    int c = 1;
                                    int time_mili = seconds*1000;
                                    int total = (duration%time_mili > 0)?(duration/time_mili+1):duration/time_mili;
                                    while (start < duration){
                                        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
                                        Date date = new Date();
                                        date.setTime(start);
                                        String time_start = format.format(date);
                                        date.setTime(end);
                                        String time_end = format.format(end);
                                        output = RealPathUtil.newFilePath("_"+c,fileName,outputFolder);
                                        command = "-y -i "+ input+" -ss "+time_start+ " -to "+time_end
                                                +" -c:v copy -c:a copy "+output;
                                        executeCommand(command,selectedVideoUri,total);
                                        c++;
                                        start+=(seconds*1000);
                                        end+=(seconds*1000);
                                        if (end > duration)
                                            end = duration;
                                    }

                                    break;

                            }
                        }
                    }
                }
            }
    );

    void selectVideo(){

        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        selectVideoAct.launch(Intent.createChooser(intent,"Select Video"));
    }

    private void executeCommand(String command,Uri audioUri, int total ) {


        final int duration = MediaPlayer.create(MainActivity.this, audioUri).getDuration();
        if (current == Operation.SplitHighQuality) {
            Config.enableStatisticsCallback(new StatisticsCallback() {
                public void apply(Statistics newStatistics) {
                    float parseFloat = (Float.parseFloat(String.valueOf(newStatistics.getTime())) / duration) * 100.0f;
                    float f = parseFloat < 99.0f ? parseFloat : 100.0f;
                    percentText.setText(((int) f) + "%");
                }
            });
        }
        else {
            Config.enableStatisticsCallback(new StatisticsCallback() {
                public void apply(Statistics newStatistics) {

                }
            });
        }

        long executionId = FFmpeg.executeAsync(command, new ExecuteCallback() {

            @Override
            public void apply(final long executionId, final int returnCode) {
                //progressDialog.dismiss();
                parts++;
                if (returnCode == RETURN_CODE_SUCCESS) {
                    //success++;
                    //successText.setText(MainActivity.this.getString(R.string.success)+success);
                    //errorText.setText(MainActivity.this.getString(R.string.error) + error);
                } else if (returnCode == RETURN_CODE_CANCEL) {
                    Log.i(Config.TAG, "Async command execution cancelled by user.");
                } else {
                    //error++;
                    //successText.setText(MainActivity.this.getString(R.string.success)+success);
                    //errorText.setText(MainActivity.this.getString(R.string.error) + error);
                    Log.i(Config.TAG, String.format("Async command execution failed with returnCode=%d.", returnCode));
                }
                float f = parts;
                f /= total; f*=100;
                percentText.setText(((int) f) + "%");
                if (parts == total) {
                    progressBar.setVisibility(View.GONE);
                    showFinishDialog();
                }

            }
        });

    }

    void showFinishDialog(){
        AlertDialog.Builder title = new AlertDialog.Builder(MainActivity.this).setTitle(MainActivity.this.getString(R.string.completed));
        title.setMessage(MainActivity.this.getString(R.string.folder) + "\n" + MainActivity.this.outputFolder).setPositiveButton("OK", new DialogInterface.OnClickListener() { // from class: com.amg.compressaudio.MainActivity.CompressAudios.1
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

            }
        }).show();
    }

    private void deleteTempFile() {
        final File[] files = getCacheDir().listFiles();
        if (files != null) {
            for (final File file : files) {
                //if (file.getName().contains(TEMP_FILE)) {
                file.delete();
                Log.d("DELETE",file.getPath());
                //}
            }
        }
    }

    @Override // android.app.Activity
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override // android.app.Activity
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return false;
    }

}