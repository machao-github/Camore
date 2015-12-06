package com.machao.camore.app.application.activities;

import android.app.ActivityManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupMenu;

import com.machao.camore.R;
import com.machao.camore.app.content_provider.camera.Preview;
import com.machao.camore.app.content_provider.file.DirPicker;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;

    private PopupMenu srcSelectorMenu;

    private FrameLayout frameLayout;
    private Button okButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        okButton = (Button)findViewById(R.id.main_act_ok_button);
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        frameLayout = (FrameLayout)findViewById(R.id.main_act_frame_layout);
        setSupportActionBar(toolbar);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleOkButtonClick();
            }
        });
        okButton.setVisibility(View.INVISIBLE);

        validateMemorySize();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.data_src_selector:

                showPopupMenu(findViewById(R.id.data_src_selector));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRestart(){
        super.onRestart();
        //onNavigateBack();
    }

    @Override
    public void onPause(){
        super.onPause();
        if (frameContProvider != null){
            frameContProvider.stop();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        onNavigateBack();
    }

    private void showPopupMenu(View view){

        if (!isPopupInited){
            InitPopupMenu(view);
            isPopupInited = true;
        }
        srcSelectorMenu.show();
    }


    private boolean isPopupInited = false;

    private void InitPopupMenu(View view){
        srcSelectorMenu = new PopupMenu(this,view);
        srcSelectorMenu.inflate(R.menu.menu_data_source);

        srcSelectorMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                item.setChecked(true);
                okButton.setVisibility(View.VISIBLE);
                onDataSourceSelected(id);
                return false;
            }
        });
    }

    public interface MainActFram {
        void start()throws IOException;
        void stop();
    }

    private MainActFram frameContProvider;
    private void handleOkButtonClick(){

        try{
            okButton.setVisibility(View.INVISIBLE);
            frameContProvider.start();
        }
        catch (IOException e){

            popupWarning("failed when handling files",e.getMessage(),null);
            okButton.setVisibility(View.VISIBLE);
        }

    }

    private void validateMemorySize(){
        ActivityManager manager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
        int memlarge = manager.getLargeMemoryClass();
        if (memlarge < 250){
            popupWarning(" memory allocation failure ", "Not sufficient" +
                            " size of memory could be acquired,the size of required memory might " +
                            "be reduced in future version." +
                            "\nSorry for the inconvienence",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
        }
    }

    private void popupWarning(String title,String msg, final DialogInterface.OnClickListener listener){
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("ok", listener)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void inflateFileDirSelect(){

        frameContProvider = new DirPicker(this,frameLayout);
    }

    private void inflateCamera(){
        frameContProvider = new Preview(this,frameLayout);

    }

    private int curSourceItemId = -1;
    private void onDataSourceSelected(int newItemId){
        if (newItemId != curSourceItemId ){

            if (curSourceItemId != -1){
                frameContProvider.stop();
            }

            inflate(newItemId);
        }

        curSourceItemId = newItemId;
    }

    private void inflate(int newItemId){
        if (newItemId == R.id.data_src_camera){
            inflateCamera();
        }else if (newItemId == R.id.data_src_file){
            inflateFileDirSelect();
        }
    }

    private void onNavigateBack(){
        if (curSourceItemId == -1){
            okButton.setVisibility(View.INVISIBLE);
            return;
        }
        okButton.setVisibility(View.VISIBLE);
        inflate(curSourceItemId);
    }

}
