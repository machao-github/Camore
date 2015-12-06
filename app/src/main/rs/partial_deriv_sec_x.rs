#pragma version(1)
#pragma rs java_package_name(com.machao.camore.app.computation_core)
#pragma rs_fp_relaxed

rs_allocation g_partial_x;
uint32_t g_x_max;
uint32_t g_y_max;

float __attribute__((kernel)) sec_partial_x(float in,uint32_t x,uint32_t y){
    int32_t pix_rig ;
    int32_t pix_cur ;

    float bigger ;
    float change_ratio ;

    if( x == g_x_max)
    {
        return 0;
    }

    pix_rig = rsGetElementAt_int(g_partial_x,x+1,y);
    pix_cur = rsGetElementAt_int(g_partial_x,x,y);

    bigger = max(abs(pix_cur),abs(pix_rig));
    change_ratio = (bigger== 0) ?0:(float)(pix_rig - pix_cur)/bigger;

    return change_ratio;
}