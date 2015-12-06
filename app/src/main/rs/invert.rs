#pragma version(1)
#pragma rs java_package_name(com.machao.camore.app.computation_core)
#pragma rs_fp_relaxed

int32_t* g_image;
rs_allocation g_imB;
uint im_row;
uint im_col;

static uchar get_average(uint32_t x,uint32_t y)
{


    uint32_t valueSum = ((uchar4)rsGetElementAt_uint(g_imB,x-1,y)).b
    +((uchar4)rsGetElementAt_uint(g_imB,x+1,y)).b
    +((uchar4)rsGetElementAt_uint(g_imB,x,y-1)).b
    +((uchar4)rsGetElementAt_uint(g_imB,x,y+1)).b;
    return (uchar)(valueSum/4);
}


uchar4 __attribute__((kernel)) invert(uchar4 in,uint32_t x,uint32_t y)
{
   uchar4 outValue = in;

   uchar4 pixLeft = (uchar4)(g_image[im_col*y + x]);

   uchar4 curPix = (uchar4)rsGetElementAt_uint(g_imB,x,y);
   outValue.r = get_average(x,y);
   //outValue.r = outValue.b;
   //outValue.g = outValue.b;



   return outValue;
}