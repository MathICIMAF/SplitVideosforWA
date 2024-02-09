package com.amg.splitvideosforwa;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
public class OutputsActivity extends AppCompatActivity implements AlertDialogHelper.AlertDialogListener{

    private static File folder;
    SharedPreferences.Editor edit;
    List<VideoFile> files = new ArrayList();
    List<VideoFile> selected_files = new ArrayList();
    private RecyclerView.LayoutManager lManager;
    private RecyclerView list;
    SharedPreferences preference;
    int views;

    AlertDialogHelper alertDialogHelper;

    VideosAdapter adapter;
    boolean isMultiSelect = false;

    ActionMode mActionMode;
    Menu context_menu;
    public static void launch(Activity activity, File folder2) {
        activity.startActivityForResult(getLaunchIntent(activity), 1);
        folder = folder2;
    }

    public static Intent getLaunchIntent(Context context) {
        return new Intent(context, OutputsActivity.class);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outputs);

        alertDialogHelper =new AlertDialogHelper(this);
        SharedPreferences preferences = getPreferences(0);
        this.preference = preferences;
        this.edit = preferences.edit();
        for (int i = 0; i < folder.listFiles().length; i++) {
            try {
                this.files.add(new VideoFile(folder.listFiles()[i]));
            } catch (Exception unused) {
            }
        }
        setTitle(getString(R.string.open_path));
        ((TextView) findViewById(R.id.output)).setText(folder.getPath());
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list);
        this.list = recyclerView;
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        this.lManager = linearLayoutManager;
        this.list.setLayoutManager(linearLayoutManager);
        TextView textView = (TextView) findViewById(R.id.empty);
        if (this.files.size() == 0) {
            textView.setVisibility(View.VISIBLE);
        }
        adapter = new VideosAdapter(this.files,selected_files, this);
        this.list.setAdapter(adapter);

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (isMultiSelect)
                    multi_select(position);
                //else
                    //Toast.makeText(getApplicationContext(), "Details Page", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(View view, int position) {
                if (!isMultiSelect) {
                    selected_files = new ArrayList<VideoFile>();
                    isMultiSelect = true;

                    if (mActionMode == null) {
                        mActionMode = startActionMode(mActionModeCallback);
                    }
                }

                multi_select(position);

            }
        }));
    }

    public void multi_select(int position) {
        if (mActionMode != null) {
            if (selected_files.contains(files.get(position)))
                selected_files.remove(files.get(position));
            else
                selected_files.add(files.get(position));

            if (selected_files.size() > 0)
                mActionMode.setTitle("" + selected_files.size());
            else
                mActionMode.setTitle("");

            refreshAdapter();

        }
    }

    public void multi_select_all() {
        if (mActionMode != null) {

            if (selected_files.size() > 0)
                mActionMode.setTitle("" + selected_files.size());
            else
                mActionMode.setTitle("");

            refreshAdapter();
        }
    }
    public void refreshAdapter()
    {
        adapter.selected_files=selected_files;
        adapter.files=files;
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_common_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_select:
                if (!isMultiSelect) {
                    selected_files = new ArrayList<VideoFile>();
                    selected_files.addAll(files);
                    isMultiSelect = true;

                    if (mActionMode == null) {
                        mActionMode = startActionMode(mActionModeCallback);
                    }
                }
                multi_select_all();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_multi_select, menu);
            context_menu = menu;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    alertDialogHelper.showAlertDialog("",getString(R.string.delete_video),getString(R.string.pos_delete),getString(R.string.neg_delete),1,false);
                    return true;
                case R.id.action_share:
                    shareSelecteds();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            isMultiSelect = false;
            selected_files = new ArrayList<>();
            refreshAdapter();
        }
    };

    private void shareSelecteds() {
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("video/");
        ArrayList<Uri> urisList = new ArrayList<Uri>();
        for (VideoFile file:selected_files){
            Context applicationContext = getApplicationContext();
            Uri uri = FileProvider.getUriForFile(applicationContext, getPackageName() + ".provider", file.getFile());
            urisList.add(uri);
        }
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, urisList);
        startActivity(intent);

    }

    private void deleteSelecteds(){
        for (VideoFile file:selected_files){
            files.remove(file);
            file.getFile().delete();
        }
        refreshAdapter();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPositiveClick(int from) {
        if(from==1)
        {
            if(selected_files.size()>0)
            {
                deleteSelecteds();
                if (mActionMode != null) {
                    mActionMode.finish();
                }
                Toast.makeText(getApplicationContext(),  getString(R.string.msj_delete), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onNegativeClick(int from) {

    }

    @Override
    public void onNeutralClick(int from) {

    }
}
