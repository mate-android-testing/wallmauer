.class Lde/uni_passau/fim/auermich/tracer/Tracer$1;
.super Ljava/lang/Object;
.source "Tracer.java"

# interfaces
.implements Ljava/lang/Thread$UncaughtExceptionHandler;


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lde/uni_passau/fim/auermich/tracer/Tracer;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation


# instance fields
.field final synthetic val$defaultUncaughtExceptionHandler:Ljava/lang/Thread$UncaughtExceptionHandler;


# direct methods
.method constructor <init>(Ljava/lang/Thread$UncaughtExceptionHandler;)V
    .registers 2
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "()V"
        }
    .end annotation

    .prologue
    .line 61
    iput-object p1, p0, Lde/uni_passau/fim/auermich/tracer/Tracer$1;->val$defaultUncaughtExceptionHandler:Ljava/lang/Thread$UncaughtExceptionHandler;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public uncaughtException(Ljava/lang/Thread;Ljava/lang/Throwable;)V
    .registers 5
    .param p1, "t"    # Ljava/lang/Thread;
    .param p2, "e"    # Ljava/lang/Throwable;

    .prologue
    .line 65
    invoke-static {}, Lde/uni_passau/fim/auermich/tracer/Tracer;->access$000()Ljava/util/logging/Logger;

    move-result-object v0

    const-string v1, "Uncaught exception!"

    invoke-virtual {v0, v1}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 66
    invoke-static {}, Lde/uni_passau/fim/auermich/tracer/Tracer;->access$100()V

    .line 67
    iget-object v0, p0, Lde/uni_passau/fim/auermich/tracer/Tracer$1;->val$defaultUncaughtExceptionHandler:Ljava/lang/Thread$UncaughtExceptionHandler;

    invoke-interface {v0, p1, p2}, Ljava/lang/Thread$UncaughtExceptionHandler;->uncaughtException(Ljava/lang/Thread;Ljava/lang/Throwable;)V

    .line 68
    return-void
.end method
