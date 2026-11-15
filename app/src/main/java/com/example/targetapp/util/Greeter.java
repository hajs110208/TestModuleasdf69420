package com.example.targetapp.util;

/**
 * SAMPLE replacement class.
 *
 * <p>This has the same fully qualified name as a (hypothetical) class inside the target app.
 * At runtime the target app receives this version instead of its own. It must keep the public
 * API the app calls (same method names, signatures and return types), otherwise the app will
 * crash with NoSuchMethodError / IncompatibleClassChangeError.
 *
 * <p>Replace it with the real class you want to override.
 */
public class Greeter {

    public String greet(String name) {
        return "Hello from the LSPosed module, " + name + "!";
    }
}
