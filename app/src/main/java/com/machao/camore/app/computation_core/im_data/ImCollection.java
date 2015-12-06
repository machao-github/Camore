package com.machao.camore.app.computation_core.im_data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.machao.camore.app.Configs.Configs;

import java.io.File;
import java.util.Iterator;


public class ImCollection implements Iterable<Bitmap>{
    public static final String LOG_TAG = ImCollection.class.getName();
    private File imFolder;
    private File[] files;
    public ImCollection(){
        imFolder = new File(Configs.getWorkingDir());
        files =  imFolder.listFiles();
    }
    private int index;

    public int getNum(){
        return files.length;
    }

    @Override
    public Iterator<Bitmap> iterator(){

        Iterator<Bitmap> it = new Iterator<Bitmap>() {

            @Override
            public boolean hasNext() {
                if (index >= files.length){
                    return false;
                }

                return true;
            }

            @Override
            public Bitmap next() {
                Bitmap bitmap = null;
                BitmapFactory.Options opt = new BitmapFactory.Options();

                while (index < files.length){
                    File file = files[index];
                    index++;
                    if (!file.isFile()){
                        continue;
                    }

                    bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    if (bitmap == null){
                        continue;
                    }else {
                        Log.d(LOG_TAG,file.getName());
                        break;
                    }

                }

                return bitmap;
            }


            @Override
            public void remove() {

            }
        };

        return it;
    }

}
