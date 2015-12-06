#pragma version(1)
#pragma rs java_package_name(com.machao.camore.app.computation_core)
#pragma rs_fp_relaxed

rs_allocation g_im_gray;
uint32_t g_x_max;
uint32_t g_y_max;

int32_t __attribute__((kernel)) partial_x(int32_t in,uint32_t x,uint32_t y){
    int32_t pixRight;
    int32_t curPix;
    if(x == g_x_max)
    {
        return 0;
    }

    curPix = rsGetElementAt_int(g_im_gray,x,y);
    pixRight = rsGetElementAt_int(g_im_gray,x+1,y);
    return pixRight - curPix;

}