package com.machao.camore;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.machao.camore.app.computation_core.im_data.PrimFieldToStr;
import com.machao.camore.app.computation_core.im_data.BlockSplitter;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
        testSplitter();
        testPrimFieldToStr();
    }

    public void testPrimFieldToStr(){
        PrimFieldToStr.main(null);
    }
    public void testSplitter(){
        BlockSplitter.main(null);
    }
}