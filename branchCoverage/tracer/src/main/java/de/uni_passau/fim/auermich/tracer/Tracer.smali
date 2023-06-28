.class public Lde/uni_passau/fim/auermich/tracer/Tracer;
.super Landroid/content/BroadcastReceiver;
.source "Tracer.java"


# static fields
.field private static final CACHE_SIZE:I = 0x1388

.field private static final INFO_FILE:Ljava/lang/String; = "info.txt"

.field private static final LOGGER:Ljava/util/logging/Logger;

.field private static final RUNNING_FILE:Ljava/lang/String; = "running.txt"

.field private static final TRACES_FILE:Ljava/lang/String; = "traces.txt"

.field private static numberOfTraces:I

.field private static final traces:Ljava/util/Set;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/Set",
            "<",
            "Ljava/lang/String;",
            ">;"
        }
    .end annotation
.end field

.field private static uncaughtExceptionHandler:Ljava/lang/Thread$UncaughtExceptionHandler;


# direct methods
.method static constructor <clinit>()V
    .registers 3

    .prologue
    .line 43
    const-class v2, Lde/uni_passau/fim/auermich/tracer/Tracer;

    invoke-virtual {v2}, Ljava/lang/Class;->getName()Ljava/lang/String;

    move-result-object v2

    invoke-static {v2}, Ljava/util/logging/Logger;->getLogger(Ljava/lang/String;)Ljava/util/logging/Logger;

    move-result-object v2

    sput-object v2, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    .line 59
    invoke-static {}, Ljava/lang/Thread;->getDefaultUncaughtExceptionHandler()Ljava/lang/Thread$UncaughtExceptionHandler;

    move-result-object v0

    .line 61
    .local v0, "defaultUncaughtExceptionHandler":Ljava/lang/Thread$UncaughtExceptionHandler;
    new-instance v1, Lde/uni_passau/fim/auermich/tracer/Tracer$1;

    invoke-direct {v1, v0}, Lde/uni_passau/fim/auermich/tracer/Tracer$1;-><init>(Ljava/lang/Thread$UncaughtExceptionHandler;)V

    .line 71
    .local v1, "uncaughtExceptionHandler":Ljava/lang/Thread$UncaughtExceptionHandler;
    invoke-static {v1}, Ljava/lang/Thread;->setDefaultUncaughtExceptionHandler(Ljava/lang/Thread$UncaughtExceptionHandler;)V

    .line 72
    sput-object v1, Lde/uni_passau/fim/auermich/tracer/Tracer;->uncaughtExceptionHandler:Ljava/lang/Thread$UncaughtExceptionHandler;

    .line 76
    new-instance v2, Ljava/util/LinkedHashSet;

    invoke-direct {v2}, Ljava/util/LinkedHashSet;-><init>()V

    sput-object v2, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    .line 88
    const/4 v2, 0x0

    sput v2, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    return-void
.end method

.method public constructor <init>()V
    .registers 1

    .prologue
    .line 29
    invoke-direct {p0}, Landroid/content/BroadcastReceiver;-><init>()V

    return-void
.end method

.method static synthetic access$000()Ljava/util/logging/Logger;
    .registers 1

    .prologue
    .line 29
    sget-object v0, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    return-object v0
.end method

.method static synthetic access$100()V
    .registers 0

    .prologue
    .line 29
    invoke-static {}, Lde/uni_passau/fim/auermich/tracer/Tracer;->writeRemainingTraces()V

    return-void
.end method

.method private static declared-synchronized createRunFile()V
    .registers 6

    .prologue
    .line 322
    const-class v4, Lde/uni_passau/fim/auermich/tracer/Tracer;

    monitor-enter v4

    :try_start_3
    invoke-static {}, Landroid/os/Environment;->getExternalStorageDirectory()Ljava/io/File;

    move-result-object v2

    .line 323
    .local v2, "sdCard":Ljava/io/File;
    new-instance v1, Ljava/io/File;

    const-string v3, "running.txt"

    invoke-direct {v1, v2, v3}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V
    :try_end_e
    .catchall {:try_start_3 .. :try_end_e} :catchall_25

    .line 326
    .local v1, "file":Ljava/io/File;
    :try_start_e
    invoke-virtual {v1}, Ljava/io/File;->createNewFile()Z
    :try_end_11
    .catch Ljava/io/IOException; {:try_start_e .. :try_end_11} :catch_13
    .catchall {:try_start_e .. :try_end_11} :catchall_25

    .line 331
    :goto_11
    monitor-exit v4

    return-void

    .line 327
    :catch_13
    move-exception v0

    .line 328
    .local v0, "e":Ljava/io/IOException;
    :try_start_14
    sget-object v3, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v5, "Failed to create file running.txt"

    invoke-virtual {v3, v5}, Ljava/util/logging/Logger;->warning(Ljava/lang/String;)V

    .line 329
    sget-object v3, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    invoke-virtual {v0}, Ljava/io/IOException;->getMessage()Ljava/lang/String;

    move-result-object v5

    invoke-virtual {v3, v5}, Ljava/util/logging/Logger;->warning(Ljava/lang/String;)V
    :try_end_24
    .catchall {:try_start_14 .. :try_end_24} :catchall_25

    goto :goto_11

    .line 322
    .end local v0    # "e":Ljava/io/IOException;
    .end local v1    # "file":Ljava/io/File;
    :catchall_25
    move-exception v3

    monitor-exit v4

    throw v3
.end method

.method private static declared-synchronized deleteRunFile()V
    .registers 5

    .prologue
    .line 337
    const-class v3, Lde/uni_passau/fim/auermich/tracer/Tracer;

    monitor-enter v3

    :try_start_3
    invoke-static {}, Landroid/os/Environment;->getExternalStorageDirectory()Ljava/io/File;

    move-result-object v1

    .line 338
    .local v1, "sdCard":Ljava/io/File;
    new-instance v2, Ljava/io/File;

    const-string v4, "running.txt"

    invoke-direct {v2, v1, v4}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V

    invoke-virtual {v2}, Ljava/io/File;->delete()Z
    :try_end_11
    .catchall {:try_start_3 .. :try_end_11} :catchall_14

    move-result v0

    .line 339
    .local v0, "ignored":Z
    monitor-exit v3

    return-void

    .line 337
    .end local v0    # "ignored":Z
    :catchall_14
    move-exception v2

    monitor-exit v3

    throw v2
.end method

.method private static getApplicationUsingReflection()Landroid/app/Application;
    .registers 5

    .prologue
    const/4 v2, 0x0

    .line 143
    :try_start_1
    const-string v1, "android.app.ActivityThread"

    invoke-static {v1}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v1

    const-string v3, "currentApplication"

    const/4 v4, 0x0

    new-array v4, v4, [Ljava/lang/Class;

    .line 144
    invoke-virtual {v1, v3, v4}, Ljava/lang/Class;->getMethod(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;

    move-result-object v3

    const/4 v4, 0x0

    const/4 v1, 0x0

    check-cast v1, [Ljava/lang/Object;

    invoke-virtual {v3, v4, v1}, Ljava/lang/reflect/Method;->invoke(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v1

    check-cast v1, Landroid/app/Application;
    :try_end_1a
    .catch Ljava/lang/Exception; {:try_start_1 .. :try_end_1a} :catch_1b

    .line 148
    .local v0, "e":Ljava/lang/Exception;
    :goto_1a
    return-object v1

    .line 145
    .end local v0    # "e":Ljava/lang/Exception;
    :catch_1b
    move-exception v0

    .line 146
    .restart local v0    # "e":Ljava/lang/Exception;
    sget-object v1, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v3, "Couldn\'t retrieve global context object!"

    invoke-virtual {v1, v3}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 147
    invoke-virtual {v0}, Ljava/lang/Exception;->printStackTrace()V

    move-object v1, v2

    .line 148
    goto :goto_1a
.end method

.method private static isPermissionGranted(Landroid/content/Context;Ljava/lang/String;)Z
    .registers 4
    .param p0, "context"    # Landroid/content/Context;
    .param p1, "permission"    # Ljava/lang/String;

    .prologue
    .line 162
    if-nez p0, :cond_6

    .line 163
    invoke-static {}, Lde/uni_passau/fim/auermich/tracer/Tracer;->getApplicationUsingReflection()Landroid/app/Application;

    move-result-object p0

    .line 166
    :cond_6
    if-nez p0, :cond_10

    .line 167
    new-instance v0, Ljava/lang/IllegalStateException;

    const-string v1, "Couldn\'t access context object!"

    invoke-direct {v0, v1}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V

    throw v0

    .line 170
    :cond_10
    invoke-virtual {p0, p1}, Landroid/content/Context;->checkSelfPermission(Ljava/lang/String;)I

    move-result v0

    if-nez v0, :cond_18

    const/4 v0, 0x1

    :goto_17
    return v0

    :cond_18
    const/4 v0, 0x0

    goto :goto_17
.end method

.method public static trace(Ljava/lang/String;)V
    .registers 4
    .param p0, "identifier"    # Ljava/lang/String;

    .prologue
    .line 131
    const-class v1, Lde/uni_passau/fim/auermich/tracer/Tracer;

    monitor-enter v1

    .line 132
    :try_start_3
    sget-object v0, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v0, p0}, Ljava/util/Set;->add(Ljava/lang/Object;)Z

    .line 134
    sget-object v0, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v0}, Ljava/util/Set;->size()I

    move-result v0

    const/16 v2, 0x1388

    if-ne v0, v2, :cond_15

    .line 135
    invoke-static {}, Lde/uni_passau/fim/auermich/tracer/Tracer;->writeTraces()V

    .line 137
    :cond_15
    monitor-exit v1

    .line 138
    return-void

    .line 137
    :catchall_17
    move-exception v0

    monitor-exit v1
    :try_end_19
    .catchall {:try_start_3 .. :try_end_19} :catchall_17

    throw v0
.end method

.method private static declared-synchronized writeRemainingTraces()V
    .registers 12

    .prologue
    .line 242
    const-class v9, Lde/uni_passau/fim/auermich/tracer/Tracer;

    monitor-enter v9

    :try_start_3
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->uncaughtExceptionHandler:Ljava/lang/Thread$UncaughtExceptionHandler;

    invoke-static {}, Ljava/lang/Thread;->getDefaultUncaughtExceptionHandler()Ljava/lang/Thread$UncaughtExceptionHandler;

    move-result-object v10

    invoke-virtual {v8, v10}, Ljava/lang/Object;->equals(Ljava/lang/Object;)Z

    move-result v8

    if-nez v8, :cond_1b

    .line 243
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v10, "Default exception handler has been overridden!"

    invoke-virtual {v8, v10}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 244
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->uncaughtExceptionHandler:Ljava/lang/Thread$UncaughtExceptionHandler;

    invoke-static {v8}, Ljava/lang/Thread;->setDefaultUncaughtExceptionHandler(Ljava/lang/Thread$UncaughtExceptionHandler;)V

    .line 248
    :cond_1b
    invoke-static {}, Lde/uni_passau/fim/auermich/tracer/Tracer;->createRunFile()V

    .line 251
    invoke-static {}, Landroid/os/Environment;->getExternalStorageDirectory()Ljava/io/File;

    move-result-object v5

    .line 252
    .local v5, "sdCard":Ljava/io/File;
    new-instance v6, Ljava/io/File;

    const-string v8, "traces.txt"

    invoke-direct {v6, v5, v8}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V

    .line 254
    .local v6, "traceFile":Ljava/io/File;
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    new-instance v10, Ljava/lang/StringBuilder;

    invoke-direct {v10}, Ljava/lang/StringBuilder;-><init>()V

    const-string v11, "Remaining traces size: "

    invoke-virtual {v10, v11}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v10

    sget-object v11, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v11}, Ljava/util/Set;->size()I

    move-result v11

    invoke-virtual {v10, v11}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v10

    invoke-virtual {v10}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v10

    invoke-virtual {v8, v10}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V
    :try_end_47
    .catchall {:try_start_3 .. :try_end_47} :catchall_10f

    .line 259
    :try_start_47
    new-instance v7, Ljava/io/FileWriter;

    const/4 v8, 0x1

    invoke-direct {v7, v6, v8}, Ljava/io/FileWriter;-><init>(Ljava/io/File;Z)V

    .line 260
    .local v7, "writer":Ljava/io/FileWriter;
    new-instance v0, Ljava/io/BufferedWriter;

    invoke-direct {v0, v7}, Ljava/io/BufferedWriter;-><init>(Ljava/io/Writer;)V

    .line 262
    .local v0, "br":Ljava/io/BufferedWriter;
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v8}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v4

    .line 263
    .local v4, "iterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    const/4 v2, 0x0

    .line 265
    .local v2, "element":Ljava/lang/String;
    :goto_59
    invoke-interface {v4}, Ljava/util/Iterator;->hasNext()Z

    move-result v8

    if-eqz v8, :cond_e5

    .line 267
    if-nez v2, :cond_de

    .line 268
    invoke-interface {v4}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v2

    .end local v2    # "element":Ljava/lang/String;
    check-cast v2, Ljava/lang/String;

    .line 269
    .restart local v2    # "element":Ljava/lang/String;
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    new-instance v10, Ljava/lang/StringBuilder;

    invoke-direct {v10}, Ljava/lang/StringBuilder;-><init>()V

    const-string v11, "First entry: "

    invoke-virtual {v10, v11}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v10

    invoke-virtual {v10, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v10

    invoke-virtual {v10}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v10

    invoke-virtual {v8, v10}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 274
    :goto_7f
    invoke-virtual {v0, v2}, Ljava/io/BufferedWriter;->write(Ljava/lang/String;)V

    .line 275
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->newLine()V
    :try_end_85
    .catch Ljava/lang/Exception; {:try_start_47 .. :try_end_85} :catch_86
    .catchall {:try_start_47 .. :try_end_85} :catchall_10f

    goto :goto_59

    .line 286
    .end local v0    # "br":Ljava/io/BufferedWriter;
    .end local v2    # "element":Ljava/lang/String;
    .end local v4    # "iterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    .end local v7    # "writer":Ljava/io/FileWriter;
    :catch_86
    move-exception v1

    .line 287
    .local v1, "e":Ljava/lang/Exception;
    :try_start_87
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v10, "Writing traces.txt to external storage failed."

    invoke-virtual {v8, v10}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 288
    invoke-virtual {v1}, Ljava/lang/Exception;->printStackTrace()V

    .line 292
    .end local v1    # "e":Ljava/lang/Exception;
    :goto_91
    new-instance v3, Ljava/io/File;

    const-string v8, "info.txt"

    invoke-direct {v3, v5, v8}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V
    :try_end_98
    .catchall {:try_start_87 .. :try_end_98} :catchall_10f

    .line 295
    .local v3, "infoFile":Ljava/io/File;
    :try_start_98
    new-instance v7, Ljava/io/FileWriter;

    invoke-direct {v7, v3}, Ljava/io/FileWriter;-><init>(Ljava/io/File;)V

    .line 297
    .restart local v7    # "writer":Ljava/io/FileWriter;
    sget v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v10}, Ljava/util/Set;->size()I

    move-result v10

    add-int/2addr v8, v10

    sput v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    .line 298
    sget v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    invoke-static {v8}, Ljava/lang/String;->valueOf(I)Ljava/lang/String;

    move-result-object v8

    invoke-virtual {v7, v8}, Ljava/io/FileWriter;->append(Ljava/lang/CharSequence;)Ljava/io/Writer;

    .line 299
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    new-instance v10, Ljava/lang/StringBuilder;

    invoke-direct {v10}, Ljava/lang/StringBuilder;-><init>()V

    const-string v11, "Total number of traces in file: "

    invoke-virtual {v10, v11}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v10

    sget v11, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    invoke-virtual {v10, v11}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v10

    invoke-virtual {v10}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v10

    invoke-virtual {v8, v10}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 302
    const/4 v8, 0x0

    sput v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    .line 304
    invoke-virtual {v7}, Ljava/io/FileWriter;->flush()V

    .line 305
    invoke-virtual {v7}, Ljava/io/FileWriter;->close()V
    :try_end_d4
    .catch Ljava/lang/Exception; {:try_start_98 .. :try_end_d4} :catch_112
    .catchall {:try_start_98 .. :try_end_d4} :catchall_10f

    .line 313
    .end local v7    # "writer":Ljava/io/FileWriter;
    :goto_d4
    :try_start_d4
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v8}, Ljava/util/Set;->clear()V

    .line 314
    invoke-static {}, Lde/uni_passau/fim/auermich/tracer/Tracer;->deleteRunFile()V
    :try_end_dc
    .catchall {:try_start_d4 .. :try_end_dc} :catchall_10f

    .line 315
    monitor-exit v9

    return-void

    .line 271
    .end local v3    # "infoFile":Ljava/io/File;
    .restart local v0    # "br":Ljava/io/BufferedWriter;
    .restart local v2    # "element":Ljava/lang/String;
    .restart local v4    # "iterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    .restart local v7    # "writer":Ljava/io/FileWriter;
    :cond_de
    :try_start_de
    invoke-interface {v4}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v2

    .end local v2    # "element":Ljava/lang/String;
    check-cast v2, Ljava/lang/String;

    .restart local v2    # "element":Ljava/lang/String;
    goto :goto_7f

    .line 278
    :cond_e5
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v8}, Ljava/util/Set;->isEmpty()Z

    move-result v8

    if-nez v8, :cond_105

    .line 279
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    new-instance v10, Ljava/lang/StringBuilder;

    invoke-direct {v10}, Ljava/lang/StringBuilder;-><init>()V

    const-string v11, "Last entry: "

    invoke-virtual {v10, v11}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v10

    invoke-virtual {v10, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v10

    invoke-virtual {v10}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v10

    invoke-virtual {v8, v10}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 282
    :cond_105
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->flush()V

    .line 283
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->close()V

    .line 284
    invoke-virtual {v7}, Ljava/io/FileWriter;->close()V
    :try_end_10e
    .catch Ljava/lang/Exception; {:try_start_de .. :try_end_10e} :catch_86
    .catchall {:try_start_de .. :try_end_10e} :catchall_10f

    goto :goto_91

    .line 242
    .end local v0    # "br":Ljava/io/BufferedWriter;
    .end local v2    # "element":Ljava/lang/String;
    .end local v4    # "iterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    .end local v6    # "traceFile":Ljava/io/File;
    .end local v7    # "writer":Ljava/io/FileWriter;
    :catchall_10f
    move-exception v8

    monitor-exit v9

    throw v8

    .line 307
    .restart local v3    # "infoFile":Ljava/io/File;
    .restart local v6    # "traceFile":Ljava/io/File;
    :catch_112
    move-exception v1

    .line 308
    .restart local v1    # "e":Ljava/lang/Exception;
    :try_start_113
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v10, "Writing info.txt to external storage failed."

    invoke-virtual {v8, v10}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 309
    invoke-virtual {v1}, Ljava/lang/Exception;->printStackTrace()V
    :try_end_11d
    .catchall {:try_start_113 .. :try_end_11d} :catchall_10f

    goto :goto_d4
.end method

.method private static declared-synchronized writeTraces()V
    .registers 16

    .prologue
    .line 180
    const-class v11, Lde/uni_passau/fim/auermich/tracer/Tracer;

    monitor-enter v11

    :try_start_3
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->uncaughtExceptionHandler:Ljava/lang/Thread$UncaughtExceptionHandler;

    invoke-static {}, Ljava/lang/Thread;->getDefaultUncaughtExceptionHandler()Ljava/lang/Thread$UncaughtExceptionHandler;

    move-result-object v12

    invoke-virtual {v10, v12}, Ljava/lang/Object;->equals(Ljava/lang/Object;)Z

    move-result v10

    if-nez v10, :cond_1b

    .line 181
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v12, "Default exception handler has been overridden!"

    invoke-virtual {v10, v12}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 182
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->uncaughtExceptionHandler:Ljava/lang/Thread$UncaughtExceptionHandler;

    invoke-static {v10}, Ljava/lang/Thread;->setDefaultUncaughtExceptionHandler(Ljava/lang/Thread$UncaughtExceptionHandler;)V

    .line 186
    :cond_1b
    invoke-static {}, Lde/uni_passau/fim/auermich/tracer/Tracer;->createRunFile()V

    .line 188
    invoke-static {}, Landroid/os/Environment;->getExternalStorageDirectory()Ljava/io/File;

    move-result-object v2

    .line 189
    .local v2, "sdCard":Ljava/io/File;
    new-instance v8, Ljava/io/File;

    const-string v10, "traces.txt"

    invoke-direct {v8, v2, v10}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V

    .line 191
    .local v8, "traceFile":Ljava/io/File;
    sget v10, Landroid/os/Build$VERSION;->SDK_INT:I

    const/16 v12, 0x17

    if-lt v10, v12, :cond_3f

    .line 192
    const/4 v10, 0x0

    const-string v12, "android.permission.WRITE_EXTERNAL_STORAGE"

    invoke-static {v10, v12}, Lde/uni_passau/fim/auermich/tracer/Tracer;->isPermissionGranted(Landroid/content/Context;Ljava/lang/String;)Z

    move-result v10

    if-nez v10, :cond_3f

    .line 193
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v12, "Permissions got dropped unexpectedly!"

    invoke-virtual {v10, v12}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V
    :try_end_3f
    .catchall {:try_start_3 .. :try_end_3f} :catchall_100

    .line 199
    :cond_3f
    :try_start_3f
    new-instance v9, Ljava/io/FileWriter;

    const/4 v10, 0x1

    invoke-direct {v9, v8, v10}, Ljava/io/FileWriter;-><init>(Ljava/io/File;Z)V

    .line 200
    .local v9, "writer":Ljava/io/FileWriter;
    new-instance v0, Ljava/io/BufferedWriter;

    invoke-direct {v0, v9}, Ljava/io/BufferedWriter;-><init>(Ljava/io/Writer;)V

    .line 202
    .local v0, "br":Ljava/io/BufferedWriter;
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v10}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v10

    :goto_50
    invoke-interface {v10}, Ljava/util/Iterator;->hasNext()Z

    move-result v12

    if-eqz v12, :cond_c1

    invoke-interface {v10}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v7

    check-cast v7, Ljava/lang/String;

    .line 203
    .local v7, "trace":Ljava/lang/String;
    invoke-virtual {v0, v7}, Ljava/io/BufferedWriter;->write(Ljava/lang/String;)V

    .line 204
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->newLine()V
    :try_end_62
    .catch Ljava/lang/IndexOutOfBoundsException; {:try_start_3f .. :try_end_62} :catch_63
    .catch Ljava/lang/Exception; {:try_start_3f .. :try_end_62} :catch_f4
    .catchall {:try_start_3f .. :try_end_62} :catchall_100

    goto :goto_50

    .line 215
    .end local v0    # "br":Ljava/io/BufferedWriter;
    .end local v7    # "trace":Ljava/lang/String;
    .end local v9    # "writer":Ljava/io/FileWriter;
    :catch_63
    move-exception v1

    .line 216
    .local v1, "e":Ljava/lang/IndexOutOfBoundsException;
    :try_start_64
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v12, "Synchronization issue!"

    invoke-virtual {v10, v12}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 217
    invoke-static {}, Ljava/lang/Thread;->getAllStackTraces()Ljava/util/Map;

    move-result-object v6

    .line 218
    .local v6, "threadStackTraces":Ljava/util/Map;, "Ljava/util/Map<Ljava/lang/Thread;[Ljava/lang/StackTraceElement;>;"
    invoke-interface {v6}, Ljava/util/Map;->keySet()Ljava/util/Set;

    move-result-object v10

    invoke-interface {v10}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v12

    :cond_77
    invoke-interface {v12}, Ljava/util/Iterator;->hasNext()Z

    move-result v10

    if-eqz v10, :cond_ea

    invoke-interface {v12}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v5

    check-cast v5, Ljava/lang/Thread;

    .line 219
    .local v5, "thread":Ljava/lang/Thread;
    invoke-interface {v6, v5}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v3

    check-cast v3, [Ljava/lang/StackTraceElement;

    .line 220
    .local v3, "stackTrace":[Ljava/lang/StackTraceElement;
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    new-instance v13, Ljava/lang/StringBuilder;

    invoke-direct {v13}, Ljava/lang/StringBuilder;-><init>()V

    const-string v14, "Thread["

    invoke-virtual {v13, v14}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v13

    invoke-virtual {v5}, Ljava/lang/Thread;->getId()J

    move-result-wide v14

    invoke-virtual {v13, v14, v15}, Ljava/lang/StringBuilder;->append(J)Ljava/lang/StringBuilder;

    move-result-object v13

    const-string v14, "]: "

    invoke-virtual {v13, v14}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v13

    invoke-virtual {v13, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    move-result-object v13

    invoke-virtual {v13}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v13

    invoke-virtual {v10, v13}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 221
    array-length v13, v3

    const/4 v10, 0x0

    :goto_b1
    if-ge v10, v13, :cond_77

    aget-object v4, v3, v10

    .line 222
    .local v4, "stackTraceElement":Ljava/lang/StackTraceElement;
    sget-object v14, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    invoke-static {v4}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v15

    invoke-virtual {v14, v15}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V
    :try_end_be
    .catchall {:try_start_64 .. :try_end_be} :catchall_100

    .line 221
    add-int/lit8 v10, v10, 0x1

    goto :goto_b1

    .line 208
    .end local v1    # "e":Ljava/lang/IndexOutOfBoundsException;
    .end local v3    # "stackTrace":[Ljava/lang/StackTraceElement;
    .end local v4    # "stackTraceElement":Ljava/lang/StackTraceElement;
    .end local v5    # "thread":Ljava/lang/Thread;
    .end local v6    # "threadStackTraces":Ljava/util/Map;, "Ljava/util/Map<Ljava/lang/Thread;[Ljava/lang/StackTraceElement;>;"
    .restart local v0    # "br":Ljava/io/BufferedWriter;
    .restart local v9    # "writer":Ljava/io/FileWriter;
    :cond_c1
    :try_start_c1
    sget v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    add-int/lit16 v10, v10, 0x1388

    sput v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    .line 209
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    new-instance v12, Ljava/lang/StringBuilder;

    invoke-direct {v12}, Ljava/lang/StringBuilder;-><init>()V

    const-string v13, "Accumulated traces size: "

    invoke-virtual {v12, v13}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v12

    sget v13, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    invoke-virtual {v12, v13}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v12

    invoke-virtual {v12}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v12

    invoke-virtual {v10, v12}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 211
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->flush()V

    .line 212
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->close()V

    .line 213
    invoke-virtual {v9}, Ljava/io/FileWriter;->close()V
    :try_end_ea
    .catch Ljava/lang/IndexOutOfBoundsException; {:try_start_c1 .. :try_end_ea} :catch_63
    .catch Ljava/lang/Exception; {:try_start_c1 .. :try_end_ea} :catch_f4
    .catchall {:try_start_c1 .. :try_end_ea} :catchall_100

    .line 231
    .end local v0    # "br":Ljava/io/BufferedWriter;
    .end local v9    # "writer":Ljava/io/FileWriter;
    :cond_ea
    :goto_ea
    :try_start_ea
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v10}, Ljava/util/Set;->clear()V

    .line 232
    invoke-static {}, Lde/uni_passau/fim/auermich/tracer/Tracer;->deleteRunFile()V
    :try_end_f2
    .catchall {:try_start_ea .. :try_end_f2} :catchall_100

    .line 233
    monitor-exit v11

    return-void

    .line 225
    :catch_f4
    move-exception v1

    .line 226
    .local v1, "e":Ljava/lang/Exception;
    :try_start_f5
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v12, "Writing traces.txt to external storage failed."

    invoke-virtual {v10, v12}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 227
    invoke-virtual {v1}, Ljava/lang/Exception;->printStackTrace()V
    :try_end_ff
    .catchall {:try_start_f5 .. :try_end_ff} :catchall_100

    goto :goto_ea

    .line 180
    .end local v1    # "e":Ljava/lang/Exception;
    .end local v8    # "traceFile":Ljava/io/File;
    :catchall_100
    move-exception v10

    monitor-exit v11

    throw v10
.end method


# virtual methods
.method public onReceive(Landroid/content/Context;Landroid/content/Intent;)V
    .registers 5
    .param p1, "context"    # Landroid/content/Context;
    .param p2, "intent"    # Landroid/content/Intent;

    .prologue
    .line 103
    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;

    move-result-object v0

    if-eqz v0, :cond_35

    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;

    move-result-object v0

    const-string v1, "STORE_TRACES"

    invoke-virtual {v0, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v0

    if-eqz v0, :cond_35

    .line 104
    sget-object v0, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v1, "Received Broadcast"

    invoke-virtual {v0, v1}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 106
    sget v0, Landroid/os/Build$VERSION;->SDK_INT:I

    const/16 v1, 0x17

    if-lt v0, v1, :cond_2e

    .line 107
    const-string v0, "android.permission.WRITE_EXTERNAL_STORAGE"

    invoke-static {p1, v0}, Lde/uni_passau/fim/auermich/tracer/Tracer;->isPermissionGranted(Landroid/content/Context;Ljava/lang/String;)Z

    move-result v0

    if-nez v0, :cond_2e

    .line 108
    sget-object v0, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v1, "Permissions got dropped unexpectedly!"

    invoke-virtual {v0, v1}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 117
    :cond_2e
    const-class v1, Lde/uni_passau/fim/auermich/tracer/Tracer;

    monitor-enter v1

    .line 118
    :try_start_31
    invoke-static {}, Lde/uni_passau/fim/auermich/tracer/Tracer;->writeRemainingTraces()V

    .line 119
    monitor-exit v1

    .line 121
    :cond_35
    return-void

    .line 119
    :catchall_36
    move-exception v0

    monitor-exit v1
    :try_end_38
    .catchall {:try_start_31 .. :try_end_38} :catchall_36

    throw v0
.end method
