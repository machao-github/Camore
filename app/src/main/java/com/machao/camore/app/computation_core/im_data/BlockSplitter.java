package com.machao.camore.app.computation_core.im_data;

import android.graphics.Rect;
import android.util.Log;

import java.util.Iterator;

public class BlockSplitter implements Iterable<Rect>{
    public static final String LOG_TAG = " Image splitter ";
    public static final int BLOCK_WIDTH = 256;
    public static final int BLOCK_HEIGHT = BLOCK_WIDTH;

    //the number of  blocks the image will be split in rows and columns
    private int blockRowNum;
    private int blockColNum;
    private int numBlocks;
    private int blockRowIndMax;
    private int blockColIndMax;

    //current index of block in the image
    private int blockRowIndex;
    private int blockColIndex = -1;

    private Rect curBlock = new Rect();

    //private Data data;
    private Rect rect = new Rect();
    public BlockSplitter(Rect rect){
        this.rect.set(rect);

        blockColNum =  (rect.width() + 1)% BLOCK_WIDTH > Data.MARGIN_TO_DUMP_ORI*2 ?
                     1 +  (rect.width() + 1)/ BLOCK_WIDTH: (rect.width() + 1)/ BLOCK_WIDTH;
        blockColIndMax = blockColNum -1;
        blockRowNum = (rect.height() + 1)% BLOCK_HEIGHT > Data.MARGIN_TO_DUMP_ORI*2?
                      1 + (rect.height() + 1)/BLOCK_HEIGHT:  (rect.height() + 1)/ BLOCK_HEIGHT;
        blockRowIndMax = blockRowNum -1;

        numBlocks = blockColNum*blockRowNum;
    }

    public int getCurIndexX(){
        return blockColIndex;
    }

    public int getCurIndexY(){
        return blockRowIndex;
    }

    public static int getBlockRowNum(int num){
        return 1 + num/ BLOCK_HEIGHT;
    }

    public static int getBlockColNum(int num){
        return 1 + num/BLOCK_WIDTH;
    }

    public int getBlockNum(){
        return numBlocks;
    }

    @Override
    public Iterator<Rect> iterator(){
        Iterator<Rect> it = new Iterator<Rect>() {

            @Override
            public boolean hasNext() {
                updateIndex();
                if (blockRowIndex > blockRowIndMax){
                    return false;
                }

                return true;
            }

            @Override
            public Rect next() {

                Rect rect = GetBlock();
                return rect;
            }

            private Rect GetBlock(){


                curBlock.top =  Data.MARGIN_TO_DUMP_ORI + blockRowIndex*BLOCK_HEIGHT;
                if (blockRowIndex == blockRowIndMax){
                    //curBlock.top = rect.bottom - BLOCK_HEIGHT +1;
                    curBlock.bottom = rect.bottom - Data.MARGIN_TO_DUMP_ORI;
                }else {
                    curBlock.bottom = curBlock.top + BLOCK_HEIGHT -1 ;
                }

                curBlock.left = Data.MARGIN_TO_DUMP_ORI + blockColIndex*BLOCK_WIDTH;
                if (blockColIndex == blockColIndMax){
                    //curBlock.left = rect.right - BLOCK_WIDTH +1;
                    curBlock.right = rect.right - Data.MARGIN_TO_DUMP_ORI;
                }else {

                    curBlock.right = curBlock.left + BLOCK_WIDTH - 1;
                }

                return  curBlock;
            }

            private void updateIndex(){

                if (blockColIndex == blockColIndMax){
                    blockColIndex = 0;
                    blockRowIndex ++;
                }else {
                    blockColIndex ++;
                }
            }

            @Override
            public void remove() {

            }
        };

        return it;
    }

    public static void main(String[] args){
        Log.d("test splitter ","------------splitter--------------");

        for(int width = 1000;width<10000;width++){
            for (int height = 500;height<10000;height++){

                Rect rect= new Rect(0,0,width,height);
                BlockSplitter blockSplitter = new BlockSplitter(rect);
                for (Rect newrect: blockSplitter){
                    if (newrect.left < 0 || newrect.left > rect.right
                            ||newrect.right < 0 || newrect.right >rect.right
                            ||newrect.top < 0 || newrect.top >rect.bottom
                            ||newrect.bottom <0 || newrect.bottom > rect.bottom
                            ||newrect.top >= newrect.bottom
                            ||newrect.left >= newrect.right
                            || blockSplitter.getCurIndexX()<0
                            || blockSplitter.getCurIndexX()> blockSplitter.blockColIndMax
                            || blockSplitter.getCurIndexY()<0
                            || blockSplitter.getCurIndexY()> blockSplitter.blockRowIndMax){
                        Log.e("error rect splitter" ," error ");
                        Log.e("index block splitter","block index x,y-> " + Integer.toString(blockSplitter
                                .getCurIndexX()) + "," + Integer.toString(blockSplitter.getCurIndexY()) + " "+
                                PrimFieldToStr.toString(newrect));
                    }
                }
            }
        }

        Log.d("test splitter ","---------------end-of-splitter----------------");
    }

}
