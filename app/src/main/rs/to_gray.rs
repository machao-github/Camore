#pragma version(1)
#pragma rs java_package_name(com.machao.camore.app.computation_core)
#pragma rs_fp_relaxed

rs_allocation g_im;

int32_t __attribute__((kernel)) convert(int32_t in,uint32_t x,uint32_t y)
{
    int32_t out = 0;
    uchar4 pix = rsGetElementAt_uchar4(g_im,x,y);
    out = (int32_t)((uint32_t)pix.r + (uint32_t)pix.g +(uint32_t)pix.b);
    return out;
}