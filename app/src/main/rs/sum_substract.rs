#pragma version(1)
#pragma rs java_package_name(com.machao.camore.app.computation_core)
#pragma rs_fp_relaxed

rs_allocation g_imBase;
rs_allocation g_imCur;

int32_t g_offsetX;
int32_t g_offsetY;

int32_t g_x_ind_max;
int32_t g_y_ind_max;

uint32_t g_rowIndexStart;
uint32_t g_rowIndexEnd;

uint32_t g_colIndexStart;
uint32_t g_colIndexEnd;

//float* g_errX;
//float* g_errY;

//this function substract the difference between the pixels in the specified row in the current
//image and the base image ,pixels in the current image is shifted by g_offsetX in column and
//g_offsetY in row respectively ;input parameter x corresponds to the index of row that is currently
//computed . during computation the index (x + g_ffsetX, g_colIndexStart ) and (x + g_ffsetX, g_colIndexEnd)
//are not checked ,invalid indexes causes crash. note index plus offset stepInfo in the estimated index in
// the base image

float __attribute__((kernel)) sum_substract_row(float in,uint32_t x){
    float diffSum = 0;
    uint32_t numPixs = 0;
    int32_t basePix;
    int32_t curPix;
    int32_t base_ind_x;
    int32_t base_ind_y;
    uint32_t curRowIndex = x + g_rowIndexStart;
    //rsDebug("x",x);

    for(int colIndex = g_colIndexStart;colIndex < g_colIndexEnd + 1;colIndex++)
    {

        base_ind_x = colIndex + g_offsetX;
        base_ind_y = curRowIndex + g_offsetY;


        numPixs++;
        basePix = rsGetElementAt_int(g_imBase,base_ind_x ,base_ind_y);
        curPix = rsGetElementAt_int(g_imCur,colIndex,curRowIndex);
        diffSum += abs(curPix - basePix);
    }

    //rsDebug("error:",diffSum/numPixs);

    return diffSum/numPixs;
}