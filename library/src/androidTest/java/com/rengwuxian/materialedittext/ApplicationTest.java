package com.rengwuxian.materialedittext;

import static org.junit.Assert.assertNotNull;

import android.app.Application;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ApplicationTest {

    @Test
    public void testApplicationContext() {
        Application app = ApplicationProvider.getApplicationContext();
        assertNotNull(app);
    }
}