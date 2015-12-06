package com.machao.camore.app.computation_core.im_data;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.renderscript.Allocation;

import com.machao.camore.app.Configs.Configs;
import com.machao.camore.app.computation_core.rs_invokor.RsInvokorBase;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;

public class Data {
    private static final int SUPER_RES_SCALE = 2;
    
    //second derevative is required during calculation meaning that the magin of the generated image
    //is not reliable 
    public static final int MARGIN_TO_DUMP_ORI = 3;
    public static final int MARGIN_TO_DUMP_SUPER = MARGIN_TO_DUMP_ORI*SUPER_RES_SCALE;
    
    
    private int oriWidth;
    private int oriHeight;
    private int supImWidth;
    private int supImHeight;

    private int oriValidWidth;
    private int oriValidHeight;

    private int supValidWidth;
    private int supValidHeight;

    private int pixsInOri;
    private int pixsInSup;

    private Rect oriImRegion;
    private Rect supImRegion;
    private Rect oriValidRegion;
    private Rect supValidRegion;

    private Bitmap baseIm;
    private Bitmap curIm;
    private Bitmap superResIm;




    public Allocation getAllocSuper() {
        return RsInvokorBase.allocSuperRes;
    }


    public static class Blocks{
        public int numBlocksX;
        public int numBlocksY;
        public int totalNumBlocks;
        public int curIndX;
        public int curIndY;
        public Block[][] blocks;

        public void alloc(){
            blocks = new Block[numBlocksX][numBlocksY];
            for (int x = 0 ;x<numBlocksX; x++){
                for (int y=0;y<numBlocksY;y++){
                    blocks[x][y] = new Block();
                }
            }
        }


        public Block getCurBlock(){
            return blocks[curIndX][curIndY];
        }

        public LinkedList<Block> getCurNeighbours(){
            return getFourNeighbours(curIndX,curIndY);
        }

        public Block getCurNeighbour(){
            if (curIndX == 0 && curIndY == 0 ){
                return null;
            }

            int x = curIndX;
            int y = curIndY;
            if (curIndX >= 1){
                x = curIndX -1;
            }else{
                y = curIndY -1;
            }

            return blocks[x][y];
        }

        public Block getBlock(int x,int y){
            return blocks[x][y];
        }

        public LinkedList<Block> getFourNeighbours(int x ,int y){
            LinkedList<Block> neighbours = new LinkedList<Block>();

            if (x >= 1){
                neighbours.add(getBlock(x-1,y));
            }
            if (y >= 1){
                neighbours.add(getBlock(x,y-1));
            }

            if(x < (numBlocksX-1)){
                neighbours.add(getBlock(x+1,y));
            }

            if (y < (numBlocksY -1)){
                neighbours.add(getBlock(x,y+1));
            }
            return neighbours;
        }
    }

    public static class Block{
        public int xTranslation;
        public int yTranslation;
        public float subPixMotionX;
        public float subPixMotionY;
        public Rect region = new Rect();

        public Rect getCorrespBlockSuper(){
            Rect rect = new Rect();

            rect.left = 2*region.left;
            rect.right = rect.left + 2*(region.width()+1);
            rect.top = 2*region.top;
            rect.bottom = rect.top + 2*(region.height()+1);
            return rect;
        }

    }

    public Blocks blocks = new Blocks();

    public ImViewProperty imViewSizes;
    public static class ImViewProperty{
        public int curImHeight;
        public int curImWidth;
        public int superResImWidth;
        public int superResImHeight;
        public int max_width;
        public int max_height;
    }

    public Data(){

    }

    private boolean isInited = false;
    public boolean isInited(){
        return isInited;
    }

    public void recycleCurIm(){
        curIm.recycle();
        curIm = null;
    }

    public void copyAllocSuperToBitmap(){
        recycleCurIm();
        RsInvokorBase.allocSuperRes.copyTo(superResIm);

    }

    public Bitmap getSuperResImToShow(){

        int width = supImWidth;
        int height = supImHeight;
        int x = 0;int y = 0;
        if (supImWidth > imViewSizes.max_width){
            width = imViewSizes.max_width;
            x = (supImWidth - imViewSizes.max_width)/2;
        }

        if (supImHeight > imViewSizes.max_height){
            height = imViewSizes.max_height;
            y = (supImHeight - imViewSizes.max_height)/2;
        }

        Bitmap newBitmap =  Bitmap.createBitmap(superResIm,x,y,width,height);
        superResIm.recycle();
        superResIm = newBitmap;

        return superResIm;
    }

    public void init(Bitmap base){
        baseIm = base;
        initAlloc(base.getWidth(), base.getHeight());
        isInited = true;
    }

    public String saveSuperResImToExternStorage()throws IOException{
        String dir = Configs.getSuperResImagsDir();
        File file = new File(dir);
        if (!file.exists()){
            file.mkdirs();
        }

        int count = 0;
        String superResImFilePath;
        File fileImage;
        while (true){
            superResImFilePath= dir + Integer.toString(count) + ".jpg";
            fileImage = new File(superResImFilePath);
            if (!fileImage.exists()){
                break;
            }
            count++;
        }

        fileImage.createNewFile();
        OutputStream os = null;
        try{
            os = new BufferedOutputStream(new FileOutputStream(fileImage));
            superResIm.compress(Bitmap.CompressFormat.JPEG,100,os);
        }finally {
            if (os != null){
                os.close();
            }
        }
        return superResImFilePath;
    }

    private void initAlloc(int width, int height){
        initValues(width, height);
        allocateData();
    }



    private void initValues(int width, int height){
        oriHeight = height;
        oriWidth = width;
        oriValidWidth = oriWidth - 2*MARGIN_TO_DUMP_ORI;
        oriValidHeight = oriHeight - 2*MARGIN_TO_DUMP_ORI;


        supImWidth = 2*width;
        supImHeight = 2*height;
        supValidWidth = supImWidth - 2*MARGIN_TO_DUMP_SUPER;
        supValidHeight= supImHeight - 2*MARGIN_TO_DUMP_SUPER;

        pixsInOri = height*width;
        pixsInSup = SUPER_RES_SCALE*SUPER_RES_SCALE*pixsInOri;
        
        oriImRegion = new Rect(0,0,width -1 ,height - 1);
        supImRegion = new Rect(0,0,supImWidth -1 ,supImHeight -1);

        oriValidRegion = new Rect(oriImRegion);
        supValidRegion = new Rect(supImRegion);

        oriValidRegion.inset(MARGIN_TO_DUMP_ORI,MARGIN_TO_DUMP_ORI);
        supValidRegion.inset(MARGIN_TO_DUMP_SUPER,MARGIN_TO_DUMP_SUPER);
        
    }

    private void allocateData(){
        initBlocks();

        superResIm = Bitmap.createScaledBitmap(baseIm,supImWidth,supImHeight,false);
    }

    private void initBlocks(){
        blocks.numBlocksX = BlockSplitter.getBlockColNum(oriValidWidth);
        blocks.numBlocksY = BlockSplitter.getBlockRowNum(oriValidHeight);
        blocks.totalNumBlocks = blocks.numBlocksX*blocks.numBlocksY;

        blocks.alloc();
    }

    public void setCurBlockRegion(Rect curBlockRegion ,int xIndex,int yIndex) {
        blocks.curIndX = xIndex;
        blocks.curIndY = yIndex;
        Block block = blocks.getCurBlock();
        block.region.set(curBlockRegion);

    }

    public void recycleBitmaps(){
        if (curIm != null){
            curIm.recycle();
            curIm = null;
        }

        if (baseIm != null){
            baseIm.recycle();
            baseIm = null;
        }

        if (superResIm != null){
            superResIm.recycle();
            superResIm = null;
        }
    }




    public int getPixsInOri() {
        return pixsInOri;
    }

    public int getSupImHeight() {
        return supImHeight;
    }

    public int getSupImWidth() {
        return supImWidth;
    }

    public int getOriHeight() {
        return oriHeight;
    }

    public int getOriWidth() {
        return oriWidth;
    }

    public int getPixsInSup() {
        return pixsInSup;
    }

    public Rect getOriImRegion() {
        return oriImRegion;
    }

    public Rect getSupImRegion() {
        return supImRegion;
    }

    public Rect getOriValidRegion() {
        return oriValidRegion;
    }

    public Rect getSupValidRegion() {
        return supValidRegion;
    }

    public Bitmap getSuperResIm() {
        return superResIm;
    }

    public Bitmap getBaseIm() {
        return baseIm;
    }

    public void setBaseIm(Bitmap baseIm) {
        this.baseIm = baseIm;
    }

    public Bitmap getCurIm() {
        return curIm;
    }

    public void setCurIm(Bitmap curIm) {
        this.curIm = curIm;
    }


    public int getOriValidWidth() {
        return oriValidWidth;
    }

    public int getOriValidHeight() {
        return oriValidHeight;
    }

    public int getSupValidWidth() {
        return supValidWidth;
    }

    public int getSupValidHeight() {
        return supValidHeight;
    }

    public int getNumTotalBlocks() {
        return blocks.totalNumBlocks;
    }

}
