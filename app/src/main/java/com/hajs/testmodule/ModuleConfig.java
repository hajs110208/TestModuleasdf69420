package com.hajs.testmodule;

/**
 * Edit this file to point the module at your own app and class.
 *
 * <p>For every name in {@link #REPLACED_CLASSES} there must be a smali file at
 * {@code app/src/main/smali/<package path>/<Class>.smali} (see
 * {@code com/example/targetapp/util/Greeter.smali} for the sample). The build assembles all
 * smali files into one dex ({@link #REPLACEMENT_DEX_ASSET}); that is what the target app
 * receives instead of its own class.
 */
public final class ModuleConfig {

    private ModuleConfig() {}

    /** Package name of the app whose class should be replaced. Also update res/values/arrays.xml. */
    public static final String TARGET_PACKAGE = "com.example.targetapp";

    /**
     * Fully qualified names (dotted form) of the classes to replace. Nested classes (Foo$Bar)
     * of a listed class are served from the module as well; give them their own smali files.
     */
    public static final String[] REPLACED_CLASSES = {
            "com.example.targetapp.util.Greeter",
    };

    /** Asset produced by the build's smali step. Must match app/build.gradle.kts. */
    public static final String REPLACEMENT_DEX_ASSET = "replacements.dex";
}
