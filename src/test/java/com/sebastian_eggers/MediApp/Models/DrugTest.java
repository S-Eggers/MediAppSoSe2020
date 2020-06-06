package com.sebastian_eggers.MediApp.Models;

import com.sebastian_eggers.MediApp.Enum.DrugForm;

import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class DrugTest {

    @Test
    public void setId() {
        Drug drug = new Drug("Test", new ArrayList<LocalTime>(), new ArrayList<DayOfWeek>(), 0, DrugForm.PILL);
        try {
            drug.setId(10);
        }
        catch (RuntimeException e) {
            fail(e.getMessage());
        }
        boolean failed = false;
        try {
            drug.setId(10);
        }
        catch (RuntimeException e) {
            failed = true;
        }
        assertTrue(failed);
    }

    @Test
    public void isNextIntakeToday() {
    }
}