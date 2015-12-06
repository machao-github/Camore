#pragma version(1)
#pragma rs java_package_name(com.machao.camore.app.computation_core)
#pragma rs_fp_relaxed

rs_allocation g_src;
int32_t g_x_start;
int32_t g_x_end;


float __attribute__((kernel)) sum_row_float(float in,uint32_t x)
{
    float sum_row = 0;

    for(int i = g_x_start;i<g_x_end;i++)
    {
        sum_row += rsGetElementAt_float(g_src,i,x);
    }

    return sum_row;

}

