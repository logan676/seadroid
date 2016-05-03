package com.seafile.seadroid2.util;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class UtilsTest {
    @Test
    public void testJoinPaths() {
        String result = Utils.pathJoin("a", "b");
        Assert.assertEquals("a/b", result);

        result = Utils.pathJoin("a", "b/c");
        Assert.assertEquals("a/b/c", result);

        result = Utils.pathJoin("/a", "b/c");
        Assert.assertEquals("/a/b/c", result);

        result = Utils.pathJoin("/a", "/b/c");
        Assert.assertEquals("/a/b/c", result);

        result = Utils.pathJoin("/a/", "/b/c");
        Assert.assertEquals("/a/b/c", result);

        result = Utils.pathJoin("/a/", "/b/c/");
        Assert.assertEquals("/a/b/c/", result);

        result = Utils.pathJoin("/a", "/b/c", "d/e");
        Assert.assertEquals("/a/b/c/d/e", result);

        result = Utils.pathJoin("/a/", "/b/c/", "/d/e");
        Assert.assertEquals("/a/b/c/d/e", result);
    }

    @Test
    public void testEmailValid() {
        boolean valid = Utils.isValidEmail("test@test123.com");
        Assert.assertEquals(valid, true);
        valid = Utils.isValidEmail("test123@test123.com");
        Assert.assertEquals(valid, true);
        valid = Utils.isValidEmail("test123_abc@test123.com");
        Assert.assertEquals(valid, true);
        valid = Utils.isValidEmail("test.tset@test123.com");
        Assert.assertEquals(valid, true);
        valid = Utils.isValidEmail("test@test123.com.cn");
        Assert.assertEquals(valid, true);
        valid = Utils.isValidEmail("test@test123_abc.com");
        Assert.assertEquals(valid, false);
        valid = Utils.isValidEmail("test_test123.com.cn");
        Assert.assertEquals(valid, false);
        valid = Utils.isValidEmail("test@test123");
        Assert.assertEquals(valid, false);
    }
}
