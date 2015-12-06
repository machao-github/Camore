#pragma version(1)
#pragma rs java_package_name(com.machao.camore.app.computation_core)
#pragma rs_fp_relaxed

rs_allocation g_partial_y;
uint32_t g_x_max;
uint32_t g_y_max;


float __attribute__((kernel)) sec_partial_y(float in,uint32_t x,uint32_t y){
    int32_t pix_down;
    int32_t pix_cur;
    float bigger;
    float changeRatio;

    if(y == g_y_max)
    {
        return 0;
    }

    pix_down = rsGetElementAt_int(g_partial_y,x,y+1);
    pix_cur = rsGetElementAt_int(g_partial_y,x,y);
    bigger = max(abs(pix_cur),abs(pix_down));
    changeRatio = (bigger==0 )? 0:(float)(pix_down - pix_cur)/bigger;

    return changeRatio;
}