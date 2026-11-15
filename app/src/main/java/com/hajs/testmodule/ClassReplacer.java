package com.hajs.testmodule;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dalvik.system.InMemoryDexClassLoader;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * Makes the target app load selected classes from smali-built bytecode shipped in this module.
 *
 * <p>The module's smali sources (app/src/main/smali) are assembled at build time into
 * {@code assets/replacements.dex}. At runtime that dex is read out of the module APK and
 * loaded by a {@link ReplacementClassLoader}, which is "child-first" for the replaced class
 * names and delegates everything else to the app's class loader, so replacement code can
 * still reference the app's other classes. {@code ClassLoader.loadClass(String)} is hooked so
 * that when the app's loader is asked for a replaced name, the smali version is returned.
 *
 * <p>Limitation: a class the app already loaded before the hook was installed cannot be
 * swapped. Hooks installed from {@code handleLoadPackage} run before the app's Application
 * is created, which covers the normal case.
 */
final class ClassReplacer {

    private static final String TAG = "[TestModule] ";
    private static final String DEX_ENTRY = "assets/" + ModuleConfig.REPLACEMENT_DEX_ASSET;

    private final String modulePath;
    private final Set<String> replaced = new HashSet<>();

    ClassReplacer(String modulePath, String[] replacedClasses) {
        this.modulePath = modulePath;
        for (String name : replacedClasses) {
            replaced.add(name);
        }
    }

    void install(final ClassLoader appLoader) {
        final ReplacementClassLoader replacement;
        try {
            replacement = new ReplacementClassLoader(readDex(), appLoader, replaced);
        } catch (Throwable t) {
            XposedBridge.log(TAG + "cannot load " + DEX_ENTRY + " from module: " + t);
            return;
        }

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

    /** Reads the smali-built dex out of the module APK (no files written in the target app). */
    private ByteBuffer readDex() throws IOException {
        try (ZipFile apk = new ZipFile(modulePath)) {
            ZipEntry entry = apk.getEntry(DEX_ENTRY);
            if (entry == null) {
                throw new IOException(DEX_ENTRY + " not found in " + modulePath);
            }
            try (InputStream in = apk.getInputStream(entry)) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) != -1) {
                    out.write(buf, 0, n);
                }
                return ByteBuffer.wrap(out.toByteArray());
            }
        }
    }

    /** Loads replaced classes from the smali dex, everything else from the app. */
    static final class ReplacementClassLoader extends InMemoryDexClassLoader {

        private final Set<String> replaced;

        ReplacementClassLoader(ByteBuffer dex, ClassLoader appLoader, Set<String> replaced) {
            super(dex, appLoader);
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
                    c = findClass(name); // smali dex only, bypassing the parent
                }
                if (resolve) {
                    resolveClass(c);
                }
                return c;
            }
        }
    }
}
