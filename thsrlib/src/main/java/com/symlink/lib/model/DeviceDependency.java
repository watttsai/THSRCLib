package com.symlink.lib.model;

import android.os.Build;
import android.util.Log;

public class DeviceDependency {
    public static boolean shouldUseSecure() {
        return true;
//		if (Build.MANUFACTURER.equals("Xiaomi") &&
//				Build.MODEL.equals("2013022") && Build.VERSION.RELEASE.equals("4.2.1"))
//			return true;
//		if (Build.MODEL.equals("Lenovo A820"))
//			return true;
//		return false;
    }

    public static boolean isMTK() {
		return Build.MODEL.equals("Lenovo A820");
	}

    public static boolean shouldUseFixChannel() {
        if (Build.VERSION.RELEASE.startsWith("4.0.")) {
            if (Build.MANUFACTURER.equals("samsung"))
                return true;
            if (Build.MANUFACTURER.equals("HTC"))
                return true;
            if (Build.MANUFACTURER.equals("Sony"))
                return true;
        }
        if (Build.VERSION.RELEASE.startsWith("4.1.") &&
                Build.MANUFACTURER.equals("samsung"))
            return true;
		return Build.MANUFACTURER.equals("Xiaomi") &&
				Build.VERSION.RELEASE.equals("2.3.5");
	}

    public static void Print() {
        String ANDROID = Build.VERSION.RELEASE;
        String BOARD = Build.BOARD;
        String BOOTLOADER = Build.BOOTLOADER;
        String BRAND = Build.BRAND;
        String CPU_ABI = Build.CPU_ABI;
        String CPU_ABI2 = Build.CPU_ABI2;
        String DEVICE = Build.DEVICE;
        String DISPLAY = Build.DISPLAY;
        String FINGERPRINT = Build.FINGERPRINT;
        String HARDWARE = Build.HARDWARE;
        String HOST = Build.HOST;
        String ID = Build.ID;
        String MANUFACTURER = Build.MANUFACTURER;
        String MODEL = Build.MODEL;
        String PRODUCT = Build.PRODUCT;
        String TAGS = Build.TAGS;
        String TYPE = Build.TYPE;
        String USER = Build.USER;
        Log.i("Device Information", "ANDROID = " + ANDROID + " BOARD = " + BOARD + " BOOTLOADER = " + BOOTLOADER + " BRAND = " + BRAND + " CPU_ABI = " + CPU_ABI + " CPU_ABI2 = " + CPU_ABI2 + " DEVICE = " + DEVICE + " DISPLAY = " + DISPLAY + " FINGERPRINT = " + FINGERPRINT + " HARDWARE = " + HARDWARE + " HOST = " + HOST + " ID = " + ID + " MANUFACTURER = " + MANUFACTURER + " MODEL = " + MODEL + " PRODUCT = " + PRODUCT + " TAGS = " + TAGS + " TYPE = " + TYPE + " USER = " + USER);
    }
}
