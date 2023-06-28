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
    .line 42
    const-class v2, Lde/uni_passau/fim/auermich/tracer/Tracer;

    invoke-virtual {v2}, Ljava/lang/Class;->getName()Ljava/lang/String;

    move-result-object v2

    invoke-static {v2}, Ljava/util/logging/Logger;->getLogger(Ljava/lang/String;)Ljava/util/logging/Logger;

    move-result-object v2

    sput-object v2, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    .line 58
    invoke-static {}, Ljava/lang/Thread;->getDefaultUncaughtExceptionHandler()Ljava/lang/Thread$UncaughtExceptionHandler;

    move-result-object v0

    .line 60
    .local v0, "defaultUncaughtExceptionHandler":Ljava/lang/Thread$UncaughtExceptionHandler;
    new-instance v1, Lde/uni_passau/fim/auermich/tracer/Tracer$1;

    invoke-direct {v1, v0}, Lde/uni_passau/fim/auermich/tracer/Tracer$1;-><init>(Ljava/lang/Thread$UncaughtExceptionHandler;)V

    .line 70
    .local v1, "uncaughtExceptionHandler":Ljava/lang/Thread$UncaughtExceptionHandler;
    invoke-static {v1}, Ljava/lang/Thread;->setDefaultUncaughtExceptionHandler(Ljava/lang/Thread$UncaughtExceptionHandler;)V

    .line 71
    sput-object v1, Lde/uni_passau/fim/auermich/tracer/Tracer;->uncaughtExceptionHandler:Ljava/lang/Thread$UncaughtExceptionHandler;

    .line 75
    new-instance v2, Ljava/util/LinkedHashSet;

    invoke-direct {v2}, Ljava/util/LinkedHashSet;-><init>()V

    sput-object v2, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    .line 87
    const/4 v2, 0x0

    sput v2, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    return-void
.end method

.method public constructor <init>()V
    .registers 1

    .prologue
    .line 28
    invoke-direct {p0}, Landroid/content/BroadcastReceiver;-><init>()V

    return-void
.end method

.method static synthetic access$000()Ljava/util/logging/Logger;
    .registers 1

    .prologue
    .line 28
    sget-object v0, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    return-object v0
.end method

.method static synthetic access$100()V
    .registers 0

    .prologue
    .line 28
    invoke-static {}, Lde/uni_passau/fim/auermich/tracer/Tracer;->writeRemainingTraces()V

    return-void
.end method

.method private static declared-synchronized createRunFile()V
    .registers 6

    .prologue
    .line 305
    const-class v4, Lde/uni_passau/fim/auermich/tracer/Tracer;

    monitor-enter v4

    :try_start_3
    invoke-static {}, Landroid/os/Environment;->getExternalStorageDirectory()Ljava/io/File;

    move-result-object v2

    .line 306
    .local v2, "sdCard":Ljava/io/File;
    new-instance v1, Ljava/io/File;

    const-string v3, "running.txt"

    invoke-direct {v1, v2, v3}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V
    :try_end_e
    .catchall {:try_start_3 .. :try_end_e} :catchall_25

    .line 309
    .local v1, "file":Ljava/io/File;
    :try_start_e
    invoke-virtual {v1}, Ljava/io/File;->createNewFile()Z
    :try_end_11
    .catch Ljava/io/IOException; {:try_start_e .. :try_end_11} :catch_13
    .catchall {:try_start_e .. :try_end_11} :catchall_25

    .line 314
    :goto_11
    monitor-exit v4

    return-void

    .line 310
    :catch_13
    move-exception v0

    .line 311
    .local v0, "e":Ljava/io/IOException;
    :try_start_14
    sget-object v3, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v5, "Failed to create file running.txt"

    invoke-virtual {v3, v5}, Ljava/util/logging/Logger;->warning(Ljava/lang/String;)V

    .line 312
    sget-object v3, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    invoke-virtual {v0}, Ljava/io/IOException;->getMessage()Ljava/lang/String;

    move-result-object v5

    invoke-virtual {v3, v5}, Ljava/util/logging/Logger;->warning(Ljava/lang/String;)V
    :try_end_24
    .catchall {:try_start_14 .. :try_end_24} :catchall_25

    goto :goto_11

    .line 305
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
    .line 320
    const-class v3, Lde/uni_passau/fim/auermich/tracer/Tracer;

    monitor-enter v3

    :try_start_3
    invoke-static {}, Landroid/os/Environment;->getExternalStorageDirectory()Ljava/io/File;

    move-result-object v1

    .line 321
    .local v1, "sdCard":Ljava/io/File;
    new-instance v2, Ljava/io/File;

    const-string v4, "running.txt"

    invoke-direct {v2, v1, v4}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V

    invoke-virtual {v2}, Ljava/io/File;->delete()Z
    :try_end_11
    .catchall {:try_start_3 .. :try_end_11} :catchall_14

    move-result v0

    .line 322
    .local v0, "ignored":Z
    monitor-exit v3

    return-void

    .line 320
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

    .line 142
    :try_start_1
    const-string v1, "android.app.ActivityThread"

    invoke-static {v1}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v1

    const-string v3, "currentApplication"

    const/4 v4, 0x0

    new-array v4, v4, [Ljava/lang/Class;

    .line 143
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

    .line 147
    .local v0, "e":Ljava/lang/Exception;
    :goto_1a
    return-object v1

    .line 144
    .end local v0    # "e":Ljava/lang/Exception;
    :catch_1b
    move-exception v0

    .line 145
    .restart local v0    # "e":Ljava/lang/Exception;
    sget-object v1, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v3, "Couldn\'t retrieve global context object!"

    invoke-virtual {v1, v3}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 146
    invoke-virtual {v0}, Ljava/lang/Exception;->printStackTrace()V

    move-object v1, v2

    .line 147
    goto :goto_1a
.end method

.method private static isPermissionGranted(Landroid/content/Context;Ljava/lang/String;)Z
    .registers 4
    .param p0, "context"    # Landroid/content/Context;
    .param p1, "permission"    # Ljava/lang/String;

    .prologue
    .line 161
    if-nez p0, :cond_6

    .line 162
    invoke-static {}, Lde/uni_passau/fim/auermich/tracer/Tracer;->getApplicationUsingReflection()Landroid/app/Application;

    move-result-object p0

    .line 165
    :cond_6
    if-nez p0, :cond_10

    .line 166
    new-instance v0, Ljava/lang/IllegalStateException;

    const-string v1, "Couldn\'t access context object!"

    invoke-direct {v0, v1}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V

    throw v0

    .line 169
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
    .line 130
    const-class v1, Lde/uni_passau/fim/auermich/tracer/Tracer;

    monitor-enter v1

    .line 131
    :try_start_3
    sget-object v0, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v0, p0}, Ljava/util/Set;->add(Ljava/lang/Object;)Z

    .line 133
    sget-object v0, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v0}, Ljava/util/Set;->size()I

    move-result v0

    const/16 v2, 0x1388

    if-ne v0, v2, :cond_15

    .line 134
    invoke-static {}, Lde/uni_passau/fim/auermich/tracer/Tracer;->writeTraces()V

    .line 136
    :cond_15
    monitor-exit v1

    .line 137
    return-void

    .line 136
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
    .line 235
    const-class v9, Lde/uni_passau/fim/auermich/tracer/Tracer;

    monitor-enter v9

    :try_start_3
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->uncaughtExceptionHandler:Ljava/lang/Thread$UncaughtExceptionHandler;

    invoke-static {}, Ljava/lang/Thread;->getDefaultUncaughtExceptionHandler()Ljava/lang/Thread$UncaughtExceptionHandler;

    move-result-object v10

    invoke-virtual {v8, v10}, Ljava/lang/Object;->equals(Ljava/lang/Object;)Z

    move-result v8

    if-nez v8, :cond_1b

    .line 236
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v10, "Default exception handler has been overridden!"

    invoke-virtual {v8, v10}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 237
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->uncaughtExceptionHandler:Ljava/lang/Thread$UncaughtExceptionHandler;

    invoke-static {v8}, Ljava/lang/Thread;->setDefaultUncaughtExceptionHandler(Ljava/lang/Thread$UncaughtExceptionHandler;)V

    .line 241
    :cond_1b
    invoke-static {}, Lde/uni_passau/fim/auermich/tracer/Tracer;->createRunFile()V

    .line 244
    invoke-static {}, Landroid/os/Environment;->getExternalStorageDirectory()Ljava/io/File;

    move-result-object v5

    .line 245
    .local v5, "sdCard":Ljava/io/File;
    new-instance v6, Ljava/io/File;

    const-string v8, "traces.txt"

    invoke-direct {v6, v5, v8}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V

    .line 247
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
    .catchall {:try_start_3 .. :try_end_47} :catchall_113

    .line 250
    :try_start_47
    new-instance v7, Ljava/io/FileWriter;

    const/4 v8, 0x1

    invoke-direct {v7, v6, v8}, Ljava/io/FileWriter;-><init>(Ljava/io/File;Z)V
    :try_end_4d
    .catch Ljava/lang/Exception; {:try_start_47 .. :try_end_4d} :catch_90
    .catchall {:try_start_47 .. :try_end_4d} :catchall_113

    .line 251
    .local v7, "writer":Ljava/io/FileWriter;
    :try_start_4d
    new-instance v0, Ljava/io/BufferedWriter;

    invoke-direct {v0, v7}, Ljava/io/BufferedWriter;-><init>(Ljava/io/Writer;)V
    :try_end_52
    .catch Ljava/lang/Throwable; {:try_start_4d .. :try_end_52} :catch_8b
    .catch Ljava/lang/Exception; {:try_start_4d .. :try_end_52} :catch_90
    .catchall {:try_start_4d .. :try_end_52} :catchall_113

    .line 253
    .local v0, "bufferedWriter":Ljava/io/BufferedWriter;
    :try_start_52
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v8}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v4

    .line 254
    .local v4, "iterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    const/4 v2, 0x0

    .line 256
    .local v2, "element":Ljava/lang/String;
    :goto_59
    invoke-interface {v4}, Ljava/util/Iterator;->hasNext()Z

    move-result v8

    if-eqz v8, :cond_ec

    .line 258
    if-nez v2, :cond_e5

    .line 259
    invoke-interface {v4}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v2

    .end local v2    # "element":Ljava/lang/String;
    check-cast v2, Ljava/lang/String;

    .line 260
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

    .line 265
    :goto_7f
    invoke-virtual {v0, v2}, Ljava/io/BufferedWriter;->write(Ljava/lang/String;)V

    .line 266
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->newLine()V
    :try_end_85
    .catch Ljava/lang/Throwable; {:try_start_52 .. :try_end_85} :catch_86
    .catch Ljava/lang/Exception; {:try_start_52 .. :try_end_85} :catch_90
    .catchall {:try_start_52 .. :try_end_85} :catchall_113

    goto :goto_59

    .line 250
    .end local v2    # "element":Ljava/lang/String;
    .end local v4    # "iterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    :catch_86
    move-exception v8

    :try_start_87
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->close()V
    :try_end_8a
    .catch Ljava/lang/Throwable; {:try_start_87 .. :try_end_8a} :catch_116
    .catch Ljava/lang/Exception; {:try_start_87 .. :try_end_8a} :catch_90
    .catchall {:try_start_87 .. :try_end_8a} :catchall_113

    :goto_8a
    :try_start_8a
    throw v8
    :try_end_8b
    .catch Ljava/lang/Throwable; {:try_start_8a .. :try_end_8b} :catch_8b
    .catch Ljava/lang/Exception; {:try_start_8a .. :try_end_8b} :catch_90
    .catchall {:try_start_8a .. :try_end_8b} :catchall_113

    .end local v0    # "bufferedWriter":Ljava/io/BufferedWriter;
    :catch_8b
    move-exception v8

    :try_start_8c
    invoke-virtual {v7}, Ljava/io/FileWriter;->close()V
    :try_end_8f
    .catch Ljava/lang/Throwable; {:try_start_8c .. :try_end_8f} :catch_11c
    .catch Ljava/lang/Exception; {:try_start_8c .. :try_end_8f} :catch_90
    .catchall {:try_start_8c .. :try_end_8f} :catchall_113

    :goto_8f
    :try_start_8f
    throw v8
    :try_end_90
    .catch Ljava/lang/Exception; {:try_start_8f .. :try_end_90} :catch_90
    .catchall {:try_start_8f .. :try_end_90} :catchall_113

    .line 273
    .end local v7    # "writer":Ljava/io/FileWriter;
    :catch_90
    move-exception v1

    .line 274
    .local v1, "e":Ljava/lang/Exception;
    :try_start_91
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v10, "Writing traces.txt to external storage failed."

    invoke-virtual {v8, v10}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 275
    invoke-virtual {v1}, Ljava/lang/Exception;->printStackTrace()V

    .line 279
    .end local v1    # "e":Ljava/lang/Exception;
    :goto_9b
    new-instance v3, Ljava/io/File;

    const-string v8, "info.txt"

    invoke-direct {v3, v5, v8}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V
    :try_end_a2
    .catchall {:try_start_91 .. :try_end_a2} :catchall_113

    .line 281
    .local v3, "infoFile":Ljava/io/File;
    :try_start_a2
    new-instance v7, Ljava/io/FileWriter;

    invoke-direct {v7, v3}, Ljava/io/FileWriter;-><init>(Ljava/io/File;)V
    :try_end_a7
    .catch Ljava/lang/Exception; {:try_start_a2 .. :try_end_a7} :catch_127
    .catchall {:try_start_a2 .. :try_end_a7} :catchall_113

    .line 283
    .restart local v7    # "writer":Ljava/io/FileWriter;
    :try_start_a7
    sget v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v10}, Ljava/util/Set;->size()I

    move-result v10

    add-int/2addr v8, v10

    sput v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    .line 284
    sget v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    invoke-static {v8}, Ljava/lang/String;->valueOf(I)Ljava/lang/String;

    move-result-object v8

    invoke-virtual {v7, v8}, Ljava/io/FileWriter;->append(Ljava/lang/CharSequence;)Ljava/io/Writer;

    .line 285
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
    :try_end_d5
    .catch Ljava/lang/Throwable; {:try_start_a7 .. :try_end_d5} :catch_122
    .catch Ljava/lang/Exception; {:try_start_a7 .. :try_end_d5} :catch_127
    .catchall {:try_start_a7 .. :try_end_d5} :catchall_113

    .line 287
    :try_start_d5
    invoke-virtual {v7}, Ljava/io/FileWriter;->close()V
    :try_end_d8
    .catch Ljava/lang/Exception; {:try_start_d5 .. :try_end_d8} :catch_127
    .catchall {:try_start_d5 .. :try_end_d8} :catchall_113

    .line 293
    .end local v7    # "writer":Ljava/io/FileWriter;
    :goto_d8
    const/4 v8, 0x0

    :try_start_d9
    sput v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    .line 296
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v8}, Ljava/util/Set;->clear()V

    .line 297
    invoke-static {}, Lde/uni_passau/fim/auermich/tracer/Tracer;->deleteRunFile()V
    :try_end_e3
    .catchall {:try_start_d9 .. :try_end_e3} :catchall_113

    .line 298
    monitor-exit v9

    return-void

    .line 262
    .end local v3    # "infoFile":Ljava/io/File;
    .restart local v0    # "bufferedWriter":Ljava/io/BufferedWriter;
    .restart local v2    # "element":Ljava/lang/String;
    .restart local v4    # "iterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    .restart local v7    # "writer":Ljava/io/FileWriter;
    :cond_e5
    :try_start_e5
    invoke-interface {v4}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v2

    .end local v2    # "element":Ljava/lang/String;
    check-cast v2, Ljava/lang/String;

    .restart local v2    # "element":Ljava/lang/String;
    goto :goto_7f

    .line 269
    :cond_ec
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v8}, Ljava/util/Set;->isEmpty()Z

    move-result v8

    if-nez v8, :cond_10c

    .line 270
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
    :try_end_10c
    .catch Ljava/lang/Throwable; {:try_start_e5 .. :try_end_10c} :catch_86
    .catch Ljava/lang/Exception; {:try_start_e5 .. :try_end_10c} :catch_90
    .catchall {:try_start_e5 .. :try_end_10c} :catchall_113

    .line 273
    :cond_10c
    :try_start_10c
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->close()V
    :try_end_10f
    .catch Ljava/lang/Throwable; {:try_start_10c .. :try_end_10f} :catch_8b
    .catch Ljava/lang/Exception; {:try_start_10c .. :try_end_10f} :catch_90
    .catchall {:try_start_10c .. :try_end_10f} :catchall_113

    :try_start_10f
    invoke-virtual {v7}, Ljava/io/FileWriter;->close()V
    :try_end_112
    .catch Ljava/lang/Exception; {:try_start_10f .. :try_end_112} :catch_90
    .catchall {:try_start_10f .. :try_end_112} :catchall_113

    goto :goto_9b

    .line 235
    .end local v0    # "bufferedWriter":Ljava/io/BufferedWriter;
    .end local v2    # "element":Ljava/lang/String;
    .end local v4    # "iterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    .end local v6    # "traceFile":Ljava/io/File;
    .end local v7    # "writer":Ljava/io/FileWriter;
    :catchall_113
    move-exception v8

    monitor-exit v9

    throw v8

    .line 250
    .restart local v0    # "bufferedWriter":Ljava/io/BufferedWriter;
    .restart local v6    # "traceFile":Ljava/io/File;
    .restart local v7    # "writer":Ljava/io/FileWriter;
    :catch_116
    move-exception v10

    :try_start_117
    invoke-virtual {v8, v10}, Ljava/lang/Throwable;->addSuppressed(Ljava/lang/Throwable;)V
    :try_end_11a
    .catch Ljava/lang/Throwable; {:try_start_117 .. :try_end_11a} :catch_8b
    .catch Ljava/lang/Exception; {:try_start_117 .. :try_end_11a} :catch_90
    .catchall {:try_start_117 .. :try_end_11a} :catchall_113

    goto/16 :goto_8a

    .end local v0    # "bufferedWriter":Ljava/io/BufferedWriter;
    :catch_11c
    move-exception v10

    :try_start_11d
    invoke-virtual {v8, v10}, Ljava/lang/Throwable;->addSuppressed(Ljava/lang/Throwable;)V
    :try_end_120
    .catch Ljava/lang/Exception; {:try_start_11d .. :try_end_120} :catch_90
    .catchall {:try_start_11d .. :try_end_120} :catchall_113

    goto/16 :goto_8f

    .line 281
    .restart local v3    # "infoFile":Ljava/io/File;
    :catch_122
    move-exception v8

    :try_start_123
    invoke-virtual {v7}, Ljava/io/FileWriter;->close()V
    :try_end_126
    .catch Ljava/lang/Throwable; {:try_start_123 .. :try_end_126} :catch_133
    .catch Ljava/lang/Exception; {:try_start_123 .. :try_end_126} :catch_127
    .catchall {:try_start_123 .. :try_end_126} :catchall_113

    :goto_126
    :try_start_126
    throw v8
    :try_end_127
    .catch Ljava/lang/Exception; {:try_start_126 .. :try_end_127} :catch_127
    .catchall {:try_start_126 .. :try_end_127} :catchall_113

    .line 287
    .end local v7    # "writer":Ljava/io/FileWriter;
    :catch_127
    move-exception v1

    .line 288
    .restart local v1    # "e":Ljava/lang/Exception;
    :try_start_128
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v10, "Writing info.txt to external storage failed."

    invoke-virtual {v8, v10}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 289
    invoke-virtual {v1}, Ljava/lang/Exception;->printStackTrace()V
    :try_end_132
    .catchall {:try_start_128 .. :try_end_132} :catchall_113

    goto :goto_d8

    .line 281
    .end local v1    # "e":Ljava/lang/Exception;
    .restart local v7    # "writer":Ljava/io/FileWriter;
    :catch_133
    move-exception v10

    :try_start_134
    invoke-virtual {v8, v10}, Ljava/lang/Throwable;->addSuppressed(Ljava/lang/Throwable;)V
    :try_end_137
    .catch Ljava/lang/Exception; {:try_start_134 .. :try_end_137} :catch_127
    .catchall {:try_start_134 .. :try_end_137} :catchall_113

    goto :goto_126
.end method

.method private static declared-synchronized writeTraces()V
    .registers 16

    .prologue
    .line 179
    const-class v11, Lde/uni_passau/fim/auermich/tracer/Tracer;

    monitor-enter v11

    :try_start_3
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->uncaughtExceptionHandler:Ljava/lang/Thread$UncaughtExceptionHandler;

    invoke-static {}, Ljava/lang/Thread;->getDefaultUncaughtExceptionHandler()Ljava/lang/Thread$UncaughtExceptionHandler;

    move-result-object v12

    invoke-virtual {v10, v12}, Ljava/lang/Object;->equals(Ljava/lang/Object;)Z

    move-result v10

    if-nez v10, :cond_1b

    .line 180
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v12, "Default exception handler has been overridden!"

    invoke-virtual {v10, v12}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 181
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->uncaughtExceptionHandler:Ljava/lang/Thread$UncaughtExceptionHandler;

    invoke-static {v10}, Ljava/lang/Thread;->setDefaultUncaughtExceptionHandler(Ljava/lang/Thread$UncaughtExceptionHandler;)V

    .line 185
    :cond_1b
    invoke-static {}, Lde/uni_passau/fim/auermich/tracer/Tracer;->createRunFile()V

    .line 187
    invoke-static {}, Landroid/os/Environment;->getExternalStorageDirectory()Ljava/io/File;

    move-result-object v2

    .line 188
    .local v2, "sdCard":Ljava/io/File;
    new-instance v8, Ljava/io/File;

    const-string v10, "traces.txt"

    invoke-direct {v8, v2, v10}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V

    .line 190
    .local v8, "traceFile":Ljava/io/File;
    sget v10, Landroid/os/Build$VERSION;->SDK_INT:I

    const/16 v12, 0x17

    if-lt v10, v12, :cond_3f

    .line 191
    const/4 v10, 0x0

    const-string v12, "android.permission.WRITE_EXTERNAL_STORAGE"

    invoke-static {v10, v12}, Lde/uni_passau/fim/auermich/tracer/Tracer;->isPermissionGranted(Landroid/content/Context;Ljava/lang/String;)Z

    move-result v10

    if-nez v10, :cond_3f

    .line 192
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v12, "Permissions got dropped unexpectedly!"

    invoke-virtual {v10, v12}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V
    :try_end_3f
    .catchall {:try_start_3 .. :try_end_3f} :catchall_10d

    .line 196
    :cond_3f
    :try_start_3f
    new-instance v9, Ljava/io/FileWriter;

    const/4 v10, 0x1

    invoke-direct {v9, v8, v10}, Ljava/io/FileWriter;-><init>(Ljava/io/File;Z)V
    :try_end_45
    .catch Ljava/lang/IndexOutOfBoundsException; {:try_start_3f .. :try_end_45} :catch_6d
    .catch Ljava/lang/Exception; {:try_start_3f .. :try_end_45} :catch_101
    .catchall {:try_start_3f .. :try_end_45} :catchall_10d

    .line 197
    .local v9, "writer":Ljava/io/FileWriter;
    :try_start_45
    new-instance v0, Ljava/io/BufferedWriter;

    invoke-direct {v0, v9}, Ljava/io/BufferedWriter;-><init>(Ljava/io/Writer;)V
    :try_end_4a
    .catch Ljava/lang/Throwable; {:try_start_45 .. :try_end_4a} :catch_68
    .catch Ljava/lang/IndexOutOfBoundsException; {:try_start_45 .. :try_end_4a} :catch_6d
    .catch Ljava/lang/Exception; {:try_start_45 .. :try_end_4a} :catch_101
    .catchall {:try_start_45 .. :try_end_4a} :catchall_10d

    .line 199
    .local v0, "bufferedWriter":Ljava/io/BufferedWriter;
    :try_start_4a
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v10}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v10

    :goto_50
    invoke-interface {v10}, Ljava/util/Iterator;->hasNext()Z

    move-result v12

    if-eqz v12, :cond_cb

    invoke-interface {v10}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v7

    check-cast v7, Ljava/lang/String;

    .line 200
    .local v7, "trace":Ljava/lang/String;
    invoke-virtual {v0, v7}, Ljava/io/BufferedWriter;->write(Ljava/lang/String;)V

    .line 201
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->newLine()V
    :try_end_62
    .catch Ljava/lang/Throwable; {:try_start_4a .. :try_end_62} :catch_63
    .catch Ljava/lang/IndexOutOfBoundsException; {:try_start_4a .. :try_end_62} :catch_6d
    .catch Ljava/lang/Exception; {:try_start_4a .. :try_end_62} :catch_101
    .catchall {:try_start_4a .. :try_end_62} :catchall_10d

    goto :goto_50

    .line 196
    .end local v7    # "trace":Ljava/lang/String;
    :catch_63
    move-exception v10

    :try_start_64
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->close()V
    :try_end_67
    .catch Ljava/lang/Throwable; {:try_start_64 .. :try_end_67} :catch_fb
    .catch Ljava/lang/IndexOutOfBoundsException; {:try_start_64 .. :try_end_67} :catch_6d
    .catch Ljava/lang/Exception; {:try_start_64 .. :try_end_67} :catch_101
    .catchall {:try_start_64 .. :try_end_67} :catchall_10d

    :goto_67
    :try_start_67
    throw v10
    :try_end_68
    .catch Ljava/lang/Throwable; {:try_start_67 .. :try_end_68} :catch_68
    .catch Ljava/lang/IndexOutOfBoundsException; {:try_start_67 .. :try_end_68} :catch_6d
    .catch Ljava/lang/Exception; {:try_start_67 .. :try_end_68} :catch_101
    .catchall {:try_start_67 .. :try_end_68} :catchall_10d

    .end local v0    # "bufferedWriter":Ljava/io/BufferedWriter;
    :catch_68
    move-exception v10

    :try_start_69
    invoke-virtual {v9}, Ljava/io/FileWriter;->close()V
    :try_end_6c
    .catch Ljava/lang/Throwable; {:try_start_69 .. :try_end_6c} :catch_110
    .catch Ljava/lang/IndexOutOfBoundsException; {:try_start_69 .. :try_end_6c} :catch_6d
    .catch Ljava/lang/Exception; {:try_start_69 .. :try_end_6c} :catch_101
    .catchall {:try_start_69 .. :try_end_6c} :catchall_10d

    :goto_6c
    :try_start_6c
    throw v10
    :try_end_6d
    .catch Ljava/lang/IndexOutOfBoundsException; {:try_start_6c .. :try_end_6d} :catch_6d
    .catch Ljava/lang/Exception; {:try_start_6c .. :try_end_6d} :catch_101
    .catchall {:try_start_6c .. :try_end_6d} :catchall_10d

    .line 208
    .end local v9    # "writer":Ljava/io/FileWriter;
    :catch_6d
    move-exception v1

    .line 209
    .local v1, "e":Ljava/lang/IndexOutOfBoundsException;
    :try_start_6e
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v12, "Synchronization issue!"

    invoke-virtual {v10, v12}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 210
    invoke-static {}, Ljava/lang/Thread;->getAllStackTraces()Ljava/util/Map;

    move-result-object v6

    .line 211
    .local v6, "threadStackTraces":Ljava/util/Map;, "Ljava/util/Map<Ljava/lang/Thread;[Ljava/lang/StackTraceElement;>;"
    invoke-interface {v6}, Ljava/util/Map;->keySet()Ljava/util/Set;

    move-result-object v10

    invoke-interface {v10}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v12

    :cond_81
    invoke-interface {v12}, Ljava/util/Iterator;->hasNext()Z

    move-result v10

    if-eqz v10, :cond_f1

    invoke-interface {v12}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v5

    check-cast v5, Ljava/lang/Thread;

    .line 212
    .local v5, "thread":Ljava/lang/Thread;
    invoke-interface {v6, v5}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v3

    check-cast v3, [Ljava/lang/StackTraceElement;

    .line 213
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

    .line 214
    array-length v13, v3

    const/4 v10, 0x0

    :goto_bb
    if-ge v10, v13, :cond_81

    aget-object v4, v3, v10

    .line 215
    .local v4, "stackTraceElement":Ljava/lang/StackTraceElement;
    sget-object v14, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    invoke-static {v4}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v15

    invoke-virtual {v14, v15}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V
    :try_end_c8
    .catchall {:try_start_6e .. :try_end_c8} :catchall_10d

    .line 214
    add-int/lit8 v10, v10, 0x1

    goto :goto_bb

    .line 205
    .end local v1    # "e":Ljava/lang/IndexOutOfBoundsException;
    .end local v3    # "stackTrace":[Ljava/lang/StackTraceElement;
    .end local v4    # "stackTraceElement":Ljava/lang/StackTraceElement;
    .end local v5    # "thread":Ljava/lang/Thread;
    .end local v6    # "threadStackTraces":Ljava/util/Map;, "Ljava/util/Map<Ljava/lang/Thread;[Ljava/lang/StackTraceElement;>;"
    .restart local v0    # "bufferedWriter":Ljava/io/BufferedWriter;
    .restart local v9    # "writer":Ljava/io/FileWriter;
    :cond_cb
    :try_start_cb
    sget v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    add-int/lit16 v10, v10, 0x1388

    sput v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    .line 206
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
    :try_end_eb
    .catch Ljava/lang/Throwable; {:try_start_cb .. :try_end_eb} :catch_63
    .catch Ljava/lang/IndexOutOfBoundsException; {:try_start_cb .. :try_end_eb} :catch_6d
    .catch Ljava/lang/Exception; {:try_start_cb .. :try_end_eb} :catch_101
    .catchall {:try_start_cb .. :try_end_eb} :catchall_10d

    .line 208
    :try_start_eb
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->close()V
    :try_end_ee
    .catch Ljava/lang/Throwable; {:try_start_eb .. :try_end_ee} :catch_68
    .catch Ljava/lang/IndexOutOfBoundsException; {:try_start_eb .. :try_end_ee} :catch_6d
    .catch Ljava/lang/Exception; {:try_start_eb .. :try_end_ee} :catch_101
    .catchall {:try_start_eb .. :try_end_ee} :catchall_10d

    :try_start_ee
    invoke-virtual {v9}, Ljava/io/FileWriter;->close()V
    :try_end_f1
    .catch Ljava/lang/IndexOutOfBoundsException; {:try_start_ee .. :try_end_f1} :catch_6d
    .catch Ljava/lang/Exception; {:try_start_ee .. :try_end_f1} :catch_101
    .catchall {:try_start_ee .. :try_end_f1} :catchall_10d

    .line 224
    .end local v0    # "bufferedWriter":Ljava/io/BufferedWriter;
    .end local v9    # "writer":Ljava/io/FileWriter;
    :cond_f1
    :goto_f1
    :try_start_f1
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v10}, Ljava/util/Set;->clear()V

    .line 225
    invoke-static {}, Lde/uni_passau/fim/auermich/tracer/Tracer;->deleteRunFile()V
    :try_end_f9
    .catchall {:try_start_f1 .. :try_end_f9} :catchall_10d

    .line 226
    monitor-exit v11

    return-void

    .line 196
    .restart local v0    # "bufferedWriter":Ljava/io/BufferedWriter;
    .restart local v9    # "writer":Ljava/io/FileWriter;
    :catch_fb
    move-exception v12

    :try_start_fc
    invoke-virtual {v10, v12}, Ljava/lang/Throwable;->addSuppressed(Ljava/lang/Throwable;)V
    :try_end_ff
    .catch Ljava/lang/Throwable; {:try_start_fc .. :try_end_ff} :catch_68
    .catch Ljava/lang/IndexOutOfBoundsException; {:try_start_fc .. :try_end_ff} :catch_6d
    .catch Ljava/lang/Exception; {:try_start_fc .. :try_end_ff} :catch_101
    .catchall {:try_start_fc .. :try_end_ff} :catchall_10d

    goto/16 :goto_67

    .line 218
    .end local v0    # "bufferedWriter":Ljava/io/BufferedWriter;
    .end local v9    # "writer":Ljava/io/FileWriter;
    :catch_101
    move-exception v1

    .line 219
    .local v1, "e":Ljava/lang/Exception;
    :try_start_102
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v12, "Writing traces.txt to external storage failed."

    invoke-virtual {v10, v12}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 220
    invoke-virtual {v1}, Ljava/lang/Exception;->printStackTrace()V
    :try_end_10c
    .catchall {:try_start_102 .. :try_end_10c} :catchall_10d

    goto :goto_f1

    .line 179
    .end local v1    # "e":Ljava/lang/Exception;
    .end local v8    # "traceFile":Ljava/io/File;
    :catchall_10d
    move-exception v10

    monitor-exit v11

    throw v10

    .line 196
    .restart local v8    # "traceFile":Ljava/io/File;
    .restart local v9    # "writer":Ljava/io/FileWriter;
    :catch_110
    move-exception v12

    :try_start_111
    invoke-virtual {v10, v12}, Ljava/lang/Throwable;->addSuppressed(Ljava/lang/Throwable;)V
    :try_end_114
    .catch Ljava/lang/IndexOutOfBoundsException; {:try_start_111 .. :try_end_114} :catch_6d
    .catch Ljava/lang/Exception; {:try_start_111 .. :try_end_114} :catch_101
    .catchall {:try_start_111 .. :try_end_114} :catchall_10d

    goto/16 :goto_6c
.end method


# virtual methods
.method public onReceive(Landroid/content/Context;Landroid/content/Intent;)V
    .registers 5
    .param p1, "context"    # Landroid/content/Context;
    .param p2, "intent"    # Landroid/content/Intent;

    .prologue
    .line 102
    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;

    move-result-object v0

    if-eqz v0, :cond_35

    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;

    move-result-object v0

    const-string v1, "STORE_TRACES"

    invoke-virtual {v0, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v0

    if-eqz v0, :cond_35

    .line 103
    sget-object v0, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v1, "Received Broadcast"

    invoke-virtual {v0, v1}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 105
    sget v0, Landroid/os/Build$VERSION;->SDK_INT:I

    const/16 v1, 0x17

    if-lt v0, v1, :cond_2e

    .line 106
    const-string v0, "android.permission.WRITE_EXTERNAL_STORAGE"

    invoke-static {p1, v0}, Lde/uni_passau/fim/auermich/tracer/Tracer;->isPermissionGranted(Landroid/content/Context;Ljava/lang/String;)Z

    move-result v0

    if-nez v0, :cond_2e

    .line 107
    sget-object v0, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v1, "Permissions got dropped unexpectedly!"

    invoke-virtual {v0, v1}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 116
    :cond_2e
    const-class v1, Lde/uni_passau/fim/auermich/tracer/Tracer;

    monitor-enter v1

    .line 117
    :try_start_31
    invoke-static {}, Lde/uni_passau/fim/auermich/tracer/Tracer;->writeRemainingTraces()V

    .line 118
    monitor-exit v1

    .line 120
    :cond_35
    return-void

    .line 118
    :catchall_36
    move-exception v0

    monitor-exit v1
    :try_end_38
    .catchall {:try_start_31 .. :try_end_38} :catchall_36

    throw v0
.end method
