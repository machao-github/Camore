package com.machao.camore.app.content_provider.file;

import android.widget.ArrayAdapter;

import com.machao.camore.app.Configs.Configs;

import java.io.File;
import java.util.LinkedList;


public class FileDirItems {
    public static class Item{
        public static final String PARENT_DIR = "..";
        private String relatPath;
        private String absPath;
        private String parentPath;

        public Item(String relatPath,String absPath,String parentPath){
            this.relatPath = relatPath;
            this.absPath = absPath;
            this.parentPath = parentPath;
        }

        public boolean isFile(){
            File file = new File(absPath);
            return !file.isDirectory();
        }

        public boolean hasParent(){
            File parent = new File(parentPath);
            return parent != null;
        }

        public String getAbsPath(){
            return absPath;
        }

        public String getParentAbs(){
            return parentPath;
        }

        public String toString(){
            return getNameToShow();
        }

        public String getNameToShow(){
            if (!isFile() && absPath.equals(parentPath) ){
                return PARENT_DIR;
            }else if (!isFile()){
                return relatPath + " ->";
            }

            return relatPath;
        }

    }

    private LinkedList<Item> items = new LinkedList<Item>();

    public Item getItem(int position){
        return items.get(position);
    }

    public void fillAdapter(String absPath, ArrayAdapter<String> adapter){
        items.clear();

        File curDir = new File(absPath);
        if (!curDir.exists()){
            curDir = new File(Configs.getExternStorageDir());
        }

        File[] files = curDir.listFiles();

        Item item;
        String parentDir = curDir.getParent();
        item = new Item(Item.PARENT_DIR,parentDir,parentDir);
        items.add(item);adapter.add(item.getNameToShow());


        for (File file:files){
            if (file.isDirectory()){
                item = fileToItem(file);
                items.add(item);
                adapter.add(item.getNameToShow());
            }

        }

    }

    private Item fileToItem(File file){
        return new Item(file.getName(),file.getAbsolutePath(),file.getParent());
    }
}
