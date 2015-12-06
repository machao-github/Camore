#pragma version(1)
#pragma rs java_package_name(com.machao.camore.app.computation_core)
#pragma rs_fp_relaxed

uint32_t g_block_ind_left;
uint32_t g_block_ind_right;
uint32_t g_block_ind_top;
uint32_t g_block_ind_bot;

int32_t g_offset_x;
int32_t g_offset_y;
int32_t g_x_ind_max;
int32_t g_y_ind_max;

float g_vx;
float g_vy;

float g_estimate_threshold;
float g_regulation_threshold;

rs_allocation g_base_im;
rs_allocation g_cur_im;

rs_allocation g_baseim_partial_x;
rs_allocation g_baseim_partial_y;

rs_allocation g_baseim_sec_partial_xx;
rs_allocation g_baseim_sec_partial_yy;


static double fn_outlier_geman_mcclure(float err,float threshold)
{
    float err_square;
    err_square = pow(err,2);
    return err_square/(err_square + threshold);
}


static float fn_get_deltai(uint32_t x,uint32_t y)
{
    int32_t base_pix;
    int32_t cur_pix;

    int32_t ind_cur_x = 0;
    int32_t ind_cur_y = 0;
    float out = 0;
    ind_cur_x = x - g_offset_x;
    ind_cur_y = y - g_offset_y;
    if(ind_cur_x < 0 || ind_cur_y <0
        || ind_cur_x > g_x_ind_max
        || ind_cur_y > g_y_ind_max)
    {
        return 0;
    }else
    {
        base_pix = rsGetElementAt_int(g_base_im,x,y);
        cur_pix = rsGetElementAt_int(g_cur_im,ind_cur_x,ind_cur_y);
        out =  (float)(base_pix - cur_pix);
    }

    return out;

}


float __attribute__((kernel)) estimate_motion_err(float in,uint32_t x,uint32_t y){
    float error_estimate = 0;
    float error_regulation = 0;
    int32_t partial_x;
    int32_t partial_y;



    float sec_partial_ratio_xx;
    float sec_partial_ratio_yy;

    float error = 0;

    float deltai = 0;

    x += g_block_ind_left;
    y += g_block_ind_top;


    sec_partial_ratio_xx = rsGetElementAt_float(g_baseim_sec_partial_xx,x,y);
    sec_partial_ratio_yy = rsGetElementAt_float(g_baseim_sec_partial_yy,x,y);


    partial_x = rsGetElementAt_int(g_baseim_partial_x,x,y);
    partial_y = rsGetElementAt_int(g_baseim_partial_y,x,y);

    deltai = fn_get_deltai(x,y);

    error_estimate = g_vx*partial_x + g_vy*partial_y + deltai;



    //calculate error regulation

    error_regulation = pow(sec_partial_ratio_xx,2) + pow(sec_partial_ratio_yy,2);

    error = fn_outlier_geman_mcclure(error_regulation,g_regulation_threshold)
          + fn_outlier_geman_mcclure(error_estimate,g_estimate_threshold);

/*
if(error_estimate != error_estimate
||error_regulation != error_regulation
||error != error
||error_estimate > 1000
||error_regulation >1000
||error >1000)
{
    rsDebug("motion error ind x",x);
    rsDebug("motion error ind y",y);
    rsDebug("motion error sec partial x",sec_partial_ratio_xx);
    rsDebug("motion error sec partial y",sec_partial_ratio_yy);
    rsDebug("motion error estimate ",error_estimate);
    rsDebug("motion error regulation ",error_regulation);
    rsDebug("motion error ", error);

}

*/
    return error;
}