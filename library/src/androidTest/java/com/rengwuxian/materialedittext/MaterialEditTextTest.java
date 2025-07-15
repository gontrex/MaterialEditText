package com.rengwuxian.materialedittext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MaterialEditTextTest {

    private MaterialEditText editTextUnderTest;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        editTextUnderTest = new MaterialEditText(context);
    }

    @Test
    public void testGetErrorReturnsNullIfNoErrorMessageWasSet() {
        assertNull(editTextUnderTest.getError());
    }

    @Test
    public void testGetErrorReturnsMessageSetEarlierViaSetError() {
        editTextUnderTest.layout(0, 0, 1000, 1000);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> editTextUnderTest.setError("Error!"));
        assertEquals("Error!", editTextUnderTest.getError().toString());
    }

    @Test
    public void testSetErrorWithZeroSizeDoesNotThrow() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> editTextUnderTest.setError("Error!"));
    }
}