package com.hajs.testmodule;

import java.util.HashSet;
import java.util.Set;

import dalvik.system.PathClassLoader;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * Makes the target app load selected classes from this module's APK instead of its own.
 *
 * <p>How it works: a {@link ReplacementClassLoader} is created on top of the module APK. It is
 * "child-first" for the replaced class names (they come from the module) and delegates
 * everything else to the app's class loader, so the replacement can still reference the
 * app's other classes. {@code ClassLoader.loadClass(String)} is then hooked so that when the
 * app's loader is asked for a replaced name, the module's version is returned.
 *
 * <p>Limitation: a class that the app already loaded before the hook was installed cannot be
 * swapped. Hooks installed from {@code handleLoadPackage} run before the app's Application
 * is created, which covers the normal case.
 */
final class ClassReplacer {

    private static final String TAG = "[TestModule] ";

    private final String modulePath;
    private final Set<String> replaced = new HashSet<>();

    ClassReplacer(String modulePath, String[] replacedClasses) {
        this.modulePath = modulePath;
        for (String name : replacedClasses) {
            replaced.add(name);
        }
    }

    void install(final ClassLoader appLoader) {
        final ReplacementClassLoader replacement =
                new ReplacementClassLoader(modulePath, appLoader, replaced);

        XposedHelpers.findAndHookMethod(ClassLoader.class, "loadClass", String.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        // Only intercept lookups made through the app's own loader.
                        if (param.thisObject != appLoader) {
                            return;
                        }
                        String name = (String) param.args[0];
                        if (!ReplacementClassLoader.isOwned(replaced, name)) {
                            return;
                        }
                        try {
                            Class<?> clazz = replacement.loadClass(name);
                            param.setResult(clazz);
                            XposedBridge.log(TAG + "replaced " + name);
                        } catch (Throwable t) {
                            // Fall through to the app's original class.
                            XposedBridge.log(TAG + "could not load replacement for " + name
                                    + ": " + t);
                        }
                    }
                });
    }

    /** Loads replaced classes from the module APK, everything else from the app. */
    static final class ReplacementClassLoader extends PathClassLoader {

        private final Set<String> replaced;

        ReplacementClassLoader(String modulePath, ClassLoader appLoader, Set<String> replaced) {
            super(modulePath, appLoader);
            this.replaced = replaced;
        }

        static boolean isOwned(Set<String> replaced, String name) {
            if (replaced.contains(name)) {
                return true;
            }
            int dollar = name.indexOf('$');
            return dollar > 0 && replaced.contains(name.substring(0, dollar));
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve)
                throws ClassNotFoundException {
            if (!isOwned(replaced, name)) {
                return super.loadClass(name, resolve);
            }
            synchronized (getClassLoadingLock(name)) {
                Class<?> c = findLoadedClass(name);
                if (c == null) {
                    c = findClass(name); // module APK only, bypassing the parent
                }
                if (resolve) {
                    resolveClass(c);
                }
                return c;
            }
        }
    }
}
