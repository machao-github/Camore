package com.machao.camore.app.content_provider.file;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import java.io.File;

import com.machao.camore.app.application.activities.CompuStatusActivity;
import com.machao.camore.app.application.activities.MainActivity;
import com.machao.camore.R;
import com.machao.camore.app.Configs.Configs;

public class DirPicker implements MainActivity.MainActFram {

    private TextView curDirAbsTextView;
    private ListView itemsListView;
    private ArrayAdapter<String> listAdapter;
    private FileDirItems items = new FileDirItems();
    private String workingDir;
    private FrameLayout frameLayout;

    Activity activity;
    public DirPicker(Activity activity,FrameLayout frameLayout){
        this.activity = activity;
        this.frameLayout = frameLayout;
        LayoutInflater inflater = activity.getLayoutInflater();
        inflater.inflate(R.layout.dir_picker,frameLayout);

        curDirAbsTextView = (TextView)frameLayout.findViewById(R.id.file_select_abs_dir);
        itemsListView = (ListView)frameLayout.findViewById(R.id.file_select_list);
        initListView();
    }

    private void initListView(){

        itemsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                handleItemClick(position);
            }
        });
        workingDir = Configs.getWorkingDir();
        File dir = new File(workingDir);
        if (!dir.exists()){
            workingDir = Configs.getExternStorageDir();
        }
        updateViews();
    }

    public void start(){
        Configs.setWorkingDir(workingDir);
        startActivityCompute();
    }

    public void stop(){
        frameLayout.removeAllViews();
    }

    private void startActivityCompute(){
        Intent intent = new Intent(activity, CompuStatusActivity.class);
        activity.startActivity(intent);
    }

    private void handleItemClick(int position){
        FileDirItems.Item item = items.getItem(position);
        if(item.isFile() ||
                !item.hasParent()){
            return;
        }

        workingDir = item.getAbsPath();
        updateViews();
    }

    private void updateViews(){
        curDirAbsTextView.setText(workingDir);

        listAdapter = new ArrayAdapter<>(activity,R.layout.file_select_item);
        items.fillAdapter(workingDir, listAdapter);
        itemsListView.setAdapter(listAdapter);
    }
}
