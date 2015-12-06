#pragma version(1)
#pragma rs java_package_name(com.machao.camore.app.computation_core)
#pragma rs_fp_relaxed

rs_allocation g_im_gray;
uint32_t g_x_max;
uint32_t g_y_max;

int32_t __attribute__((kernel)) partial_y(int32_t in,uint32_t x,uint32_t y){
    int32_t pixDown ;
    int32_t curPix ;

    if(y == g_y_max)
    {
        return 0;
    }

    pixDown = rsGetElementAt_int(g_im_gray,x,y+1);
    curPix = rsGetElementAt_int(g_im_gray,x,y);
    return  pixDown - curPix;

}