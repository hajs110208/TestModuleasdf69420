package com.hajs.testmodule;

/**
 * Edit this file to point the module at your own app and class.
 *
 * <p>For every name in {@link #REPLACED_CLASSES} the module APK must contain a class with
 * exactly the same fully qualified name (see {@code com.example.targetapp.util.Greeter}
 * under {@code app/src/main/java} for the sample). That class is what the target app will
 * receive instead of its own.
 */
public final class ModuleConfig {

    private ModuleConfig() {}

    /** Package name of the app whose class should be replaced. Also update res/values/arrays.xml. */
    public static final String TARGET_PACKAGE = "com.example.targetapp";

    /**
     * Fully qualified names of the classes to replace. Nested classes (Foo$Bar) of a listed
     * class are served from the module as well.
     */
    public static final String[] REPLACED_CLASSES = {
            "com.example.targetapp.util.Greeter",
    };
}
