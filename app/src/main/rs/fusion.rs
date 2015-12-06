#pragma version(1)
#pragma rs java_package_name(com.machao.camore.app.computation_core)
#pragma rs_fp_relaxed


rs_allocation low_res;
rs_allocation super_res;
float g_xl_subpix_offset;
float g_yl_subpix_offset;
int32_t g_xl_pix_offset;
int32_t g_yl_pix_offset;

int32_t g_num_im_processed;
int32_t g_xl_ind_max;
int32_t g_yl_ind_max;


int tmpnum = 0;

static float4 projection_on_sr(float* factor_base,uchar4 lrPix ,uint32_t xl,uint32_t yl,uint32_t xs,
uint32_t ys)
{
    float dis_x = fabs((float)xs - (float)(2*(xl + g_xl_pix_offset - g_xl_subpix_offset )+0.5));
    float dis_y = fabs((float)ys - (float)(2*(yl + g_yl_pix_offset - g_yl_subpix_offset )+0.5));
    float4 out;
    float factor = 0;

    if(dis_x + dis_y > 2)
    {
        return 0 ;
    }

    factor =  exp( -(pow(dis_x + dis_y,2)) );
    *factor_base  += factor;
    out = convert_float4(lrPix)*factor;
    //rsDebug("dis x +y",dis_x +dis_y);
    //rsDebug("pow 2", -pow(dis_x + dis_y,2));
    //rsDebug("factor",factor);
    //rsDebug("lrPix",lrPix);
    //rsDebug(" out value",out);
    return out;
}



uchar4 __attribute__((kernel)) fusion(uchar4 in,uint32_t x,uint32_t y)
{
    int32_t x_lr = x/2 - g_xl_pix_offset;
    int32_t y_lr = y/2 - g_yl_pix_offset;

    float4 sr_pix = convert_float4(rsGetElementAt_uchar4(super_res,x,y));
    float4 projection = 0;
    float4 contribution = 0;
    float factor_base = 0;
    uchar4 out;


    for(int32_t xl = x_lr -1 ;xl <= x_lr + 1; xl++)
    {
        for(int32_t yl = y_lr -1 ;yl <= y_lr +1 ;yl++)
        {

            //rsDebug("low fusioni x",xl);
            //rsDebug("low fusioni y",yl);
            if(xl <0 || xl > g_xl_ind_max
                || yl < 0 || yl >g_yl_ind_max)
            {
                 continue;
            }

            //rsDebug("projection first",projection);
            projection += projection_on_sr(&factor_base,rsGetElementAt_uchar4(low_res,xl,yl),xl,yl,
            x,y);

            //rsDebug("ratio ",ratio);
            //rsDebug("projection",projection);
        }
    }

    if(factor_base > 0.01)
    {
        projection = projection/factor_base;
        contribution = exp(-2*fabs((projection-sr_pix)/(sr_pix + 0.1)));
        contribution = fmax(fmax(contribution.r , contribution.g),  contribution.b);
        sr_pix = contribution*projection + (1-contribution)*sr_pix;

    }

    //rsDebug("fusioni return x",x);
    //rsDebug("fusioni return y",y);
    out = convert_uchar4(sr_pix);
    out.a = 255;
/*
    rsDebug("projection",projection);
    rsDebug("sr_pix ",sr_pix);
    rsDebug("out",out);
*/
    return out;
}