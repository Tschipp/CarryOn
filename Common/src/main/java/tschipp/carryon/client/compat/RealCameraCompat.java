package tschipp.carryon.client.compat;

import tschipp.carryon.platform.Services;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RealCameraCompat {
    private static Method RealCameraCore_isRendering;
    private static boolean isInit = false;
    private static boolean isRealCameraLoaded = false;

    public static boolean isRendering() throws InvocationTargetException, IllegalAccessException {
        if (!isInit) {
            if (Services.PLATFORM.isModLoaded("realcamera")) {
                isRealCameraLoaded = true;
                try {
                    Class<?> realCameraCore = Class.forName("com.xtracr.realcamera.RealCameraCore");
                    RealCameraCore_isRendering = realCameraCore.getDeclaredMethod("isRendering");
                } catch (Exception ignored) {
                }
            }
            isInit = true;
        }
        return isRealCameraLoaded && (boolean) RealCameraCore_isRendering.invoke(null);
    }
}