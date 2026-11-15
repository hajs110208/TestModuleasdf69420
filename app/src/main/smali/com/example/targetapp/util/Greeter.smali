.class public Lcom/example/targetapp/util/Greeter;
.super Ljava/lang/Object;
.source "Greeter.smali"

# SAMPLE replacement class, written directly in smali.
#
# Same fully qualified name as a (hypothetical) class inside the target app. The target app
# receives this version instead of its own, so it must keep every public member the app uses
# (names, descriptors, access flags), otherwise the app fails with NoSuchMethodError.
#
# Original Java-equivalent:
#   public String greet(String name) { return "Hello from the LSPosed module (smali), " + name + "!"; }

# direct methods
.method public constructor <init>()V
    .registers 1

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

# virtual methods
.method public greet(Ljava/lang/String;)Ljava/lang/String;
    .registers 4

    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v1, "Hello from the LSPosed module (smali), "

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    const-string v1, "!"

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    return-object v0
.end method
