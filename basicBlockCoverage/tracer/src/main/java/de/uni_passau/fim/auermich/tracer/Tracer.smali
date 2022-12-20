.class public Lde/uni_passau/fim/auermich/tracer/Tracer;
.super Landroid/content/BroadcastReceiver;
.source "Tracer.java"


# static fields
.field private static final CACHE_SIZE:I = 0x1388

.field private static final INFO_FILE:Ljava/lang/String; = "info.txt"

.field private static final LOGGER:Ljava/util/logging/Logger;

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
    .line 41
    const-class v2, Lde/uni_passau/fim/auermich/tracer/Tracer;

    invoke-virtual {v2}, Ljava/lang/Class;->getName()Ljava/lang/String;

    move-result-object v2

    invoke-static {v2}, Ljava/util/logging/Logger;->getLogger(Ljava/lang/String;)Ljava/util/logging/Logger;

    move-result-object v2

    sput-object v2, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    .line 57
    invoke-static {}, Ljava/lang/Thread;->getDefaultUncaughtExceptionHandler()Ljava/lang/Thread$UncaughtExceptionHandler;

    move-result-object v0

    .line 59
    .local v0, "defaultUncaughtExceptionHandler":Ljava/lang/Thread$UncaughtExceptionHandler;
    new-instance v1, Lde/uni_passau/fim/auermich/tracer/Tracer$1;

    invoke-direct {v1, v0}, Lde/uni_passau/fim/auermich/tracer/Tracer$1;-><init>(Ljava/lang/Thread$UncaughtExceptionHandler;)V

    .line 69
    .local v1, "uncaughtExceptionHandler":Ljava/lang/Thread$UncaughtExceptionHandler;
    invoke-static {v1}, Ljava/lang/Thread;->setDefaultUncaughtExceptionHandler(Ljava/lang/Thread$UncaughtExceptionHandler;)V

    .line 70
    sput-object v1, Lde/uni_passau/fim/auermich/tracer/Tracer;->uncaughtExceptionHandler:Ljava/lang/Thread$UncaughtExceptionHandler;

    .line 74
    new-instance v2, Ljava/util/LinkedHashSet;

    invoke-direct {v2}, Ljava/util/LinkedHashSet;-><init>()V

    sput-object v2, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    .line 83
    const/4 v2, 0x0

    sput v2, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    return-void
.end method

.method public constructor <init>()V
    .registers 1

    .prologue
    .line 27
    invoke-direct {p0}, Landroid/content/BroadcastReceiver;-><init>()V

    return-void
.end method

.method static synthetic access$000()Ljava/util/logging/Logger;
    .registers 1

    .prologue
    .line 27
    sget-object v0, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    return-object v0
.end method

.method static synthetic access$100()V
    .registers 0

    .prologue
    .line 27
    invoke-static {}, Lde/uni_passau/fim/auermich/tracer/Tracer;->writeRemainingTraces()V

    return-void
.end method

.method private static getApplicationUsingReflection()Landroid/app/Application;
    .registers 5

    .prologue
    const/4 v2, 0x0

    .line 138
    :try_start_1
    const-string v1, "android.app.ActivityThread"

    invoke-static {v1}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v1

    const-string v3, "currentApplication"

    const/4 v4, 0x0

    new-array v4, v4, [Ljava/lang/Class;

    .line 139
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

    .line 143
    .local v0, "e":Ljava/lang/Exception;
    :goto_1a
    return-object v1

    .line 140
    .end local v0    # "e":Ljava/lang/Exception;
    :catch_1b
    move-exception v0

    .line 141
    .restart local v0    # "e":Ljava/lang/Exception;
    sget-object v1, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v3, "Couldn\'t retrieve global context object!"

    invoke-virtual {v1, v3}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 142
    invoke-virtual {v0}, Ljava/lang/Exception;->printStackTrace()V

    move-object v1, v2

    .line 143
    goto :goto_1a
.end method

.method private static isPermissionGranted(Landroid/content/Context;Ljava/lang/String;)Z
    .registers 4
    .param p0, "context"    # Landroid/content/Context;
    .param p1, "permission"    # Ljava/lang/String;

    .prologue
    .line 157
    if-nez p0, :cond_6

    .line 158
    invoke-static {}, Lde/uni_passau/fim/auermich/tracer/Tracer;->getApplicationUsingReflection()Landroid/app/Application;

    move-result-object p0

    .line 161
    :cond_6
    if-nez p0, :cond_10

    .line 162
    new-instance v0, Ljava/lang/IllegalStateException;

    const-string v1, "Couldn\'t access context object!"

    invoke-direct {v0, v1}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V

    throw v0

    .line 165
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
    .line 126
    const-class v1, Lde/uni_passau/fim/auermich/tracer/Tracer;

    monitor-enter v1

    .line 127
    :try_start_3
    sget-object v0, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v0, p0}, Ljava/util/Set;->add(Ljava/lang/Object;)Z

    .line 129
    sget-object v0, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v0}, Ljava/util/Set;->size()I

    move-result v0

    const/16 v2, 0x1388

    if-ne v0, v2, :cond_15

    .line 130
    invoke-static {}, Lde/uni_passau/fim/auermich/tracer/Tracer;->writeTraces()V

    .line 132
    :cond_15
    monitor-exit v1

    .line 133
    return-void

    .line 132
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
    .line 227
    const-class v9, Lde/uni_passau/fim/auermich/tracer/Tracer;

    monitor-enter v9

    :try_start_3
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->uncaughtExceptionHandler:Ljava/lang/Thread$UncaughtExceptionHandler;

    invoke-static {}, Ljava/lang/Thread;->getDefaultUncaughtExceptionHandler()Ljava/lang/Thread$UncaughtExceptionHandler;

    move-result-object v10

    invoke-virtual {v8, v10}, Ljava/lang/Object;->equals(Ljava/lang/Object;)Z

    move-result v8

    if-nez v8, :cond_1b

    .line 228
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v10, "Default exception handler has been overridden!"

    invoke-virtual {v8, v10}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 229
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->uncaughtExceptionHandler:Ljava/lang/Thread$UncaughtExceptionHandler;

    invoke-static {v8}, Ljava/lang/Thread;->setDefaultUncaughtExceptionHandler(Ljava/lang/Thread$UncaughtExceptionHandler;)V

    .line 233
    :cond_1b
    invoke-static {}, Landroid/os/Environment;->getExternalStorageDirectory()Ljava/io/File;

    move-result-object v5

    .line 234
    .local v5, "sdCard":Ljava/io/File;
    new-instance v6, Ljava/io/File;

    const-string v8, "traces.txt"

    invoke-direct {v6, v5, v8}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V

    .line 236
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
    :try_end_44
    .catchall {:try_start_3 .. :try_end_44} :catchall_10d

    .line 239
    :try_start_44
    new-instance v7, Ljava/io/FileWriter;

    const/4 v8, 0x1

    invoke-direct {v7, v6, v8}, Ljava/io/FileWriter;-><init>(Ljava/io/File;Z)V
    :try_end_4a
    .catch Ljava/lang/Exception; {:try_start_44 .. :try_end_4a} :catch_8d
    .catchall {:try_start_44 .. :try_end_4a} :catchall_10d

    .line 240
    .local v7, "writer":Ljava/io/FileWriter;
    :try_start_4a
    new-instance v0, Ljava/io/BufferedWriter;

    invoke-direct {v0, v7}, Ljava/io/BufferedWriter;-><init>(Ljava/io/Writer;)V
    :try_end_4f
    .catch Ljava/lang/Throwable; {:try_start_4a .. :try_end_4f} :catch_88
    .catch Ljava/lang/Exception; {:try_start_4a .. :try_end_4f} :catch_8d
    .catchall {:try_start_4a .. :try_end_4f} :catchall_10d

    .line 242
    .local v0, "bufferedWriter":Ljava/io/BufferedWriter;
    :try_start_4f
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v8}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v4

    .line 243
    .local v4, "iterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    const/4 v2, 0x0

    .line 245
    .local v2, "element":Ljava/lang/String;
    :goto_56
    invoke-interface {v4}, Ljava/util/Iterator;->hasNext()Z

    move-result v8

    if-eqz v8, :cond_e6

    .line 247
    if-nez v2, :cond_df

    .line 248
    invoke-interface {v4}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v2

    .end local v2    # "element":Ljava/lang/String;
    check-cast v2, Ljava/lang/String;

    .line 249
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

    .line 254
    :goto_7c
    invoke-virtual {v0, v2}, Ljava/io/BufferedWriter;->write(Ljava/lang/String;)V

    .line 255
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->newLine()V
    :try_end_82
    .catch Ljava/lang/Throwable; {:try_start_4f .. :try_end_82} :catch_83
    .catch Ljava/lang/Exception; {:try_start_4f .. :try_end_82} :catch_8d
    .catchall {:try_start_4f .. :try_end_82} :catchall_10d

    goto :goto_56

    .line 239
    .end local v2    # "element":Ljava/lang/String;
    .end local v4    # "iterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    :catch_83
    move-exception v8

    :try_start_84
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->close()V
    :try_end_87
    .catch Ljava/lang/Throwable; {:try_start_84 .. :try_end_87} :catch_110
    .catch Ljava/lang/Exception; {:try_start_84 .. :try_end_87} :catch_8d
    .catchall {:try_start_84 .. :try_end_87} :catchall_10d

    :goto_87
    :try_start_87
    throw v8
    :try_end_88
    .catch Ljava/lang/Throwable; {:try_start_87 .. :try_end_88} :catch_88
    .catch Ljava/lang/Exception; {:try_start_87 .. :try_end_88} :catch_8d
    .catchall {:try_start_87 .. :try_end_88} :catchall_10d

    .end local v0    # "bufferedWriter":Ljava/io/BufferedWriter;
    :catch_88
    move-exception v8

    :try_start_89
    invoke-virtual {v7}, Ljava/io/FileWriter;->close()V
    :try_end_8c
    .catch Ljava/lang/Throwable; {:try_start_89 .. :try_end_8c} :catch_116
    .catch Ljava/lang/Exception; {:try_start_89 .. :try_end_8c} :catch_8d
    .catchall {:try_start_89 .. :try_end_8c} :catchall_10d

    :goto_8c
    :try_start_8c
    throw v8
    :try_end_8d
    .catch Ljava/lang/Exception; {:try_start_8c .. :try_end_8d} :catch_8d
    .catchall {:try_start_8c .. :try_end_8d} :catchall_10d

    .line 262
    .end local v7    # "writer":Ljava/io/FileWriter;
    :catch_8d
    move-exception v1

    .line 263
    .local v1, "e":Ljava/lang/Exception;
    :try_start_8e
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v10, "Writing traces.txt to external storage failed."

    invoke-virtual {v8, v10}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 264
    invoke-virtual {v1}, Ljava/lang/Exception;->printStackTrace()V

    .line 268
    .end local v1    # "e":Ljava/lang/Exception;
    :goto_98
    new-instance v3, Ljava/io/File;

    const-string v8, "info.txt"

    invoke-direct {v3, v5, v8}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V
    :try_end_9f
    .catchall {:try_start_8e .. :try_end_9f} :catchall_10d

    .line 270
    .local v3, "infoFile":Ljava/io/File;
    :try_start_9f
    new-instance v7, Ljava/io/FileWriter;

    invoke-direct {v7, v3}, Ljava/io/FileWriter;-><init>(Ljava/io/File;)V
    :try_end_a4
    .catch Ljava/lang/Exception; {:try_start_9f .. :try_end_a4} :catch_121
    .catchall {:try_start_9f .. :try_end_a4} :catchall_10d

    .line 272
    .restart local v7    # "writer":Ljava/io/FileWriter;
    :try_start_a4
    sget v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v10}, Ljava/util/Set;->size()I

    move-result v10

    add-int/2addr v8, v10

    sput v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    .line 273
    sget v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    invoke-static {v8}, Ljava/lang/String;->valueOf(I)Ljava/lang/String;

    move-result-object v8

    invoke-virtual {v7, v8}, Ljava/io/FileWriter;->append(Ljava/lang/CharSequence;)Ljava/io/Writer;

    .line 274
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
    :try_end_d2
    .catch Ljava/lang/Throwable; {:try_start_a4 .. :try_end_d2} :catch_11c
    .catch Ljava/lang/Exception; {:try_start_a4 .. :try_end_d2} :catch_121
    .catchall {:try_start_a4 .. :try_end_d2} :catchall_10d

    .line 276
    :try_start_d2
    invoke-virtual {v7}, Ljava/io/FileWriter;->close()V
    :try_end_d5
    .catch Ljava/lang/Exception; {:try_start_d2 .. :try_end_d5} :catch_121
    .catchall {:try_start_d2 .. :try_end_d5} :catchall_10d

    .line 282
    .end local v7    # "writer":Ljava/io/FileWriter;
    :goto_d5
    const/4 v8, 0x0

    :try_start_d6
    sput v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    .line 285
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v8}, Ljava/util/Set;->clear()V
    :try_end_dd
    .catchall {:try_start_d6 .. :try_end_dd} :catchall_10d

    .line 286
    monitor-exit v9

    return-void

    .line 251
    .end local v3    # "infoFile":Ljava/io/File;
    .restart local v0    # "bufferedWriter":Ljava/io/BufferedWriter;
    .restart local v2    # "element":Ljava/lang/String;
    .restart local v4    # "iterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    .restart local v7    # "writer":Ljava/io/FileWriter;
    :cond_df
    :try_start_df
    invoke-interface {v4}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v2

    .end local v2    # "element":Ljava/lang/String;
    check-cast v2, Ljava/lang/String;

    .restart local v2    # "element":Ljava/lang/String;
    goto :goto_7c

    .line 258
    :cond_e6
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v8}, Ljava/util/Set;->isEmpty()Z

    move-result v8

    if-nez v8, :cond_106

    .line 259
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
    :try_end_106
    .catch Ljava/lang/Throwable; {:try_start_df .. :try_end_106} :catch_83
    .catch Ljava/lang/Exception; {:try_start_df .. :try_end_106} :catch_8d
    .catchall {:try_start_df .. :try_end_106} :catchall_10d

    .line 262
    :cond_106
    :try_start_106
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->close()V
    :try_end_109
    .catch Ljava/lang/Throwable; {:try_start_106 .. :try_end_109} :catch_88
    .catch Ljava/lang/Exception; {:try_start_106 .. :try_end_109} :catch_8d
    .catchall {:try_start_106 .. :try_end_109} :catchall_10d

    :try_start_109
    invoke-virtual {v7}, Ljava/io/FileWriter;->close()V
    :try_end_10c
    .catch Ljava/lang/Exception; {:try_start_109 .. :try_end_10c} :catch_8d
    .catchall {:try_start_109 .. :try_end_10c} :catchall_10d

    goto :goto_98

    .line 227
    .end local v0    # "bufferedWriter":Ljava/io/BufferedWriter;
    .end local v2    # "element":Ljava/lang/String;
    .end local v4    # "iterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    .end local v6    # "traceFile":Ljava/io/File;
    .end local v7    # "writer":Ljava/io/FileWriter;
    :catchall_10d
    move-exception v8

    monitor-exit v9

    throw v8

    .line 239
    .restart local v0    # "bufferedWriter":Ljava/io/BufferedWriter;
    .restart local v6    # "traceFile":Ljava/io/File;
    .restart local v7    # "writer":Ljava/io/FileWriter;
    :catch_110
    move-exception v10

    :try_start_111
    invoke-virtual {v8, v10}, Ljava/lang/Throwable;->addSuppressed(Ljava/lang/Throwable;)V
    :try_end_114
    .catch Ljava/lang/Throwable; {:try_start_111 .. :try_end_114} :catch_88
    .catch Ljava/lang/Exception; {:try_start_111 .. :try_end_114} :catch_8d
    .catchall {:try_start_111 .. :try_end_114} :catchall_10d

    goto/16 :goto_87

    .end local v0    # "bufferedWriter":Ljava/io/BufferedWriter;
    :catch_116
    move-exception v10

    :try_start_117
    invoke-virtual {v8, v10}, Ljava/lang/Throwable;->addSuppressed(Ljava/lang/Throwable;)V
    :try_end_11a
    .catch Ljava/lang/Exception; {:try_start_117 .. :try_end_11a} :catch_8d
    .catchall {:try_start_117 .. :try_end_11a} :catchall_10d

    goto/16 :goto_8c

    .line 270
    .restart local v3    # "infoFile":Ljava/io/File;
    :catch_11c
    move-exception v8

    :try_start_11d
    invoke-virtual {v7}, Ljava/io/FileWriter;->close()V
    :try_end_120
    .catch Ljava/lang/Throwable; {:try_start_11d .. :try_end_120} :catch_12d
    .catch Ljava/lang/Exception; {:try_start_11d .. :try_end_120} :catch_121
    .catchall {:try_start_11d .. :try_end_120} :catchall_10d

    :goto_120
    :try_start_120
    throw v8
    :try_end_121
    .catch Ljava/lang/Exception; {:try_start_120 .. :try_end_121} :catch_121
    .catchall {:try_start_120 .. :try_end_121} :catchall_10d

    .line 276
    .end local v7    # "writer":Ljava/io/FileWriter;
    :catch_121
    move-exception v1

    .line 277
    .restart local v1    # "e":Ljava/lang/Exception;
    :try_start_122
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v10, "Writing info.txt to external storage failed."

    invoke-virtual {v8, v10}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 278
    invoke-virtual {v1}, Ljava/lang/Exception;->printStackTrace()V
    :try_end_12c
    .catchall {:try_start_122 .. :try_end_12c} :catchall_10d

    goto :goto_d5

    .line 270
    .end local v1    # "e":Ljava/lang/Exception;
    .restart local v7    # "writer":Ljava/io/FileWriter;
    :catch_12d
    move-exception v10

    :try_start_12e
    invoke-virtual {v8, v10}, Ljava/lang/Throwable;->addSuppressed(Ljava/lang/Throwable;)V
    :try_end_131
    .catch Ljava/lang/Exception; {:try_start_12e .. :try_end_131} :catch_121
    .catchall {:try_start_12e .. :try_end_131} :catchall_10d

    goto :goto_120
.end method

.method private static declared-synchronized writeTraces()V
    .registers 16

    .prologue
    .line 175
    const-class v11, Lde/uni_passau/fim/auermich/tracer/Tracer;

    monitor-enter v11

    :try_start_3
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->uncaughtExceptionHandler:Ljava/lang/Thread$UncaughtExceptionHandler;

    invoke-static {}, Ljava/lang/Thread;->getDefaultUncaughtExceptionHandler()Ljava/lang/Thread$UncaughtExceptionHandler;

    move-result-object v12

    invoke-virtual {v10, v12}, Ljava/lang/Object;->equals(Ljava/lang/Object;)Z

    move-result v10

    if-nez v10, :cond_1b

    .line 176
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v12, "Default exception handler has been overridden!"

    invoke-virtual {v10, v12}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 177
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->uncaughtExceptionHandler:Ljava/lang/Thread$UncaughtExceptionHandler;

    invoke-static {v10}, Ljava/lang/Thread;->setDefaultUncaughtExceptionHandler(Ljava/lang/Thread$UncaughtExceptionHandler;)V

    .line 180
    :cond_1b
    invoke-static {}, Landroid/os/Environment;->getExternalStorageDirectory()Ljava/io/File;

    move-result-object v2

    .line 181
    .local v2, "sdCard":Ljava/io/File;
    new-instance v8, Ljava/io/File;

    const-string v10, "traces.txt"

    invoke-direct {v8, v2, v10}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V

    .line 183
    .local v8, "traceFile":Ljava/io/File;
    sget v10, Landroid/os/Build$VERSION;->SDK_INT:I

    const/16 v12, 0x17

    if-lt v10, v12, :cond_3c

    .line 184
    const/4 v10, 0x0

    const-string v12, "android.permission.WRITE_EXTERNAL_STORAGE"

    invoke-static {v10, v12}, Lde/uni_passau/fim/auermich/tracer/Tracer;->isPermissionGranted(Landroid/content/Context;Ljava/lang/String;)Z

    move-result v10

    if-nez v10, :cond_3c

    .line 185
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v12, "Permissions got dropped unexpectedly!"

    invoke-virtual {v10, v12}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V
    :try_end_3c
    .catchall {:try_start_3 .. :try_end_3c} :catchall_107

    .line 189
    :cond_3c
    :try_start_3c
    new-instance v9, Ljava/io/FileWriter;

    const/4 v10, 0x1

    invoke-direct {v9, v8, v10}, Ljava/io/FileWriter;-><init>(Ljava/io/File;Z)V
    :try_end_42
    .catch Ljava/lang/IndexOutOfBoundsException; {:try_start_3c .. :try_end_42} :catch_6a
    .catch Ljava/lang/Exception; {:try_start_3c .. :try_end_42} :catch_fb
    .catchall {:try_start_3c .. :try_end_42} :catchall_107

    .line 190
    .local v9, "writer":Ljava/io/FileWriter;
    :try_start_42
    new-instance v0, Ljava/io/BufferedWriter;

    invoke-direct {v0, v9}, Ljava/io/BufferedWriter;-><init>(Ljava/io/Writer;)V
    :try_end_47
    .catch Ljava/lang/Throwable; {:try_start_42 .. :try_end_47} :catch_65
    .catch Ljava/lang/IndexOutOfBoundsException; {:try_start_42 .. :try_end_47} :catch_6a
    .catch Ljava/lang/Exception; {:try_start_42 .. :try_end_47} :catch_fb
    .catchall {:try_start_42 .. :try_end_47} :catchall_107

    .line 192
    .local v0, "bufferedWriter":Ljava/io/BufferedWriter;
    :try_start_47
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v10}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v10

    :goto_4d
    invoke-interface {v10}, Ljava/util/Iterator;->hasNext()Z

    move-result v12

    if-eqz v12, :cond_c8

    invoke-interface {v10}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v7

    check-cast v7, Ljava/lang/String;

    .line 193
    .local v7, "trace":Ljava/lang/String;
    invoke-virtual {v0, v7}, Ljava/io/BufferedWriter;->write(Ljava/lang/String;)V

    .line 194
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->newLine()V
    :try_end_5f
    .catch Ljava/lang/Throwable; {:try_start_47 .. :try_end_5f} :catch_60
    .catch Ljava/lang/IndexOutOfBoundsException; {:try_start_47 .. :try_end_5f} :catch_6a
    .catch Ljava/lang/Exception; {:try_start_47 .. :try_end_5f} :catch_fb
    .catchall {:try_start_47 .. :try_end_5f} :catchall_107

    goto :goto_4d

    .line 189
    .end local v7    # "trace":Ljava/lang/String;
    :catch_60
    move-exception v10

    :try_start_61
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->close()V
    :try_end_64
    .catch Ljava/lang/Throwable; {:try_start_61 .. :try_end_64} :catch_f5
    .catch Ljava/lang/IndexOutOfBoundsException; {:try_start_61 .. :try_end_64} :catch_6a
    .catch Ljava/lang/Exception; {:try_start_61 .. :try_end_64} :catch_fb
    .catchall {:try_start_61 .. :try_end_64} :catchall_107

    :goto_64
    :try_start_64
    throw v10
    :try_end_65
    .catch Ljava/lang/Throwable; {:try_start_64 .. :try_end_65} :catch_65
    .catch Ljava/lang/IndexOutOfBoundsException; {:try_start_64 .. :try_end_65} :catch_6a
    .catch Ljava/lang/Exception; {:try_start_64 .. :try_end_65} :catch_fb
    .catchall {:try_start_64 .. :try_end_65} :catchall_107

    .end local v0    # "bufferedWriter":Ljava/io/BufferedWriter;
    :catch_65
    move-exception v10

    :try_start_66
    invoke-virtual {v9}, Ljava/io/FileWriter;->close()V
    :try_end_69
    .catch Ljava/lang/Throwable; {:try_start_66 .. :try_end_69} :catch_10a
    .catch Ljava/lang/IndexOutOfBoundsException; {:try_start_66 .. :try_end_69} :catch_6a
    .catch Ljava/lang/Exception; {:try_start_66 .. :try_end_69} :catch_fb
    .catchall {:try_start_66 .. :try_end_69} :catchall_107

    :goto_69
    :try_start_69
    throw v10
    :try_end_6a
    .catch Ljava/lang/IndexOutOfBoundsException; {:try_start_69 .. :try_end_6a} :catch_6a
    .catch Ljava/lang/Exception; {:try_start_69 .. :try_end_6a} :catch_fb
    .catchall {:try_start_69 .. :try_end_6a} :catchall_107

    .line 201
    .end local v9    # "writer":Ljava/io/FileWriter;
    :catch_6a
    move-exception v1

    .line 202
    .local v1, "e":Ljava/lang/IndexOutOfBoundsException;
    :try_start_6b
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v12, "Synchronization issue!"

    invoke-virtual {v10, v12}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 203
    invoke-static {}, Ljava/lang/Thread;->getAllStackTraces()Ljava/util/Map;

    move-result-object v6

    .line 204
    .local v6, "threadStackTraces":Ljava/util/Map;, "Ljava/util/Map<Ljava/lang/Thread;[Ljava/lang/StackTraceElement;>;"
    invoke-interface {v6}, Ljava/util/Map;->keySet()Ljava/util/Set;

    move-result-object v10

    invoke-interface {v10}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v12

    :cond_7e
    invoke-interface {v12}, Ljava/util/Iterator;->hasNext()Z

    move-result v10

    if-eqz v10, :cond_ee

    invoke-interface {v12}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v5

    check-cast v5, Ljava/lang/Thread;

    .line 205
    .local v5, "thread":Ljava/lang/Thread;
    invoke-interface {v6, v5}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v3

    check-cast v3, [Ljava/lang/StackTraceElement;

    .line 206
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

    .line 207
    array-length v13, v3

    const/4 v10, 0x0

    :goto_b8
    if-ge v10, v13, :cond_7e

    aget-object v4, v3, v10

    .line 208
    .local v4, "stackTraceElement":Ljava/lang/StackTraceElement;
    sget-object v14, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    invoke-static {v4}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v15

    invoke-virtual {v14, v15}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V
    :try_end_c5
    .catchall {:try_start_6b .. :try_end_c5} :catchall_107

    .line 207
    add-int/lit8 v10, v10, 0x1

    goto :goto_b8

    .line 198
    .end local v1    # "e":Ljava/lang/IndexOutOfBoundsException;
    .end local v3    # "stackTrace":[Ljava/lang/StackTraceElement;
    .end local v4    # "stackTraceElement":Ljava/lang/StackTraceElement;
    .end local v5    # "thread":Ljava/lang/Thread;
    .end local v6    # "threadStackTraces":Ljava/util/Map;, "Ljava/util/Map<Ljava/lang/Thread;[Ljava/lang/StackTraceElement;>;"
    .restart local v0    # "bufferedWriter":Ljava/io/BufferedWriter;
    .restart local v9    # "writer":Ljava/io/FileWriter;
    :cond_c8
    :try_start_c8
    sget v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    add-int/lit16 v10, v10, 0x1388

    sput v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    .line 199
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
    :try_end_e8
    .catch Ljava/lang/Throwable; {:try_start_c8 .. :try_end_e8} :catch_60
    .catch Ljava/lang/IndexOutOfBoundsException; {:try_start_c8 .. :try_end_e8} :catch_6a
    .catch Ljava/lang/Exception; {:try_start_c8 .. :try_end_e8} :catch_fb
    .catchall {:try_start_c8 .. :try_end_e8} :catchall_107

    .line 201
    :try_start_e8
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->close()V
    :try_end_eb
    .catch Ljava/lang/Throwable; {:try_start_e8 .. :try_end_eb} :catch_65
    .catch Ljava/lang/IndexOutOfBoundsException; {:try_start_e8 .. :try_end_eb} :catch_6a
    .catch Ljava/lang/Exception; {:try_start_e8 .. :try_end_eb} :catch_fb
    .catchall {:try_start_e8 .. :try_end_eb} :catchall_107

    :try_start_eb
    invoke-virtual {v9}, Ljava/io/FileWriter;->close()V
    :try_end_ee
    .catch Ljava/lang/IndexOutOfBoundsException; {:try_start_eb .. :try_end_ee} :catch_6a
    .catch Ljava/lang/Exception; {:try_start_eb .. :try_end_ee} :catch_fb
    .catchall {:try_start_eb .. :try_end_ee} :catchall_107

    .line 217
    .end local v0    # "bufferedWriter":Ljava/io/BufferedWriter;
    .end local v9    # "writer":Ljava/io/FileWriter;
    :cond_ee
    :goto_ee
    :try_start_ee
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v10}, Ljava/util/Set;->clear()V
    :try_end_f3
    .catchall {:try_start_ee .. :try_end_f3} :catchall_107

    .line 218
    monitor-exit v11

    return-void

    .line 189
    .restart local v0    # "bufferedWriter":Ljava/io/BufferedWriter;
    .restart local v9    # "writer":Ljava/io/FileWriter;
    :catch_f5
    move-exception v12

    :try_start_f6
    invoke-virtual {v10, v12}, Ljava/lang/Throwable;->addSuppressed(Ljava/lang/Throwable;)V
    :try_end_f9
    .catch Ljava/lang/Throwable; {:try_start_f6 .. :try_end_f9} :catch_65
    .catch Ljava/lang/IndexOutOfBoundsException; {:try_start_f6 .. :try_end_f9} :catch_6a
    .catch Ljava/lang/Exception; {:try_start_f6 .. :try_end_f9} :catch_fb
    .catchall {:try_start_f6 .. :try_end_f9} :catchall_107

    goto/16 :goto_64

    .line 211
    .end local v0    # "bufferedWriter":Ljava/io/BufferedWriter;
    .end local v9    # "writer":Ljava/io/FileWriter;
    :catch_fb
    move-exception v1

    .line 212
    .local v1, "e":Ljava/lang/Exception;
    :try_start_fc
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v12, "Writing traces.txt to external storage failed."

    invoke-virtual {v10, v12}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 213
    invoke-virtual {v1}, Ljava/lang/Exception;->printStackTrace()V
    :try_end_106
    .catchall {:try_start_fc .. :try_end_106} :catchall_107

    goto :goto_ee

    .line 175
    .end local v1    # "e":Ljava/lang/Exception;
    .end local v8    # "traceFile":Ljava/io/File;
    :catchall_107
    move-exception v10

    monitor-exit v11

    throw v10

    .line 189
    .restart local v8    # "traceFile":Ljava/io/File;
    .restart local v9    # "writer":Ljava/io/FileWriter;
    :catch_10a
    move-exception v12

    :try_start_10b
    invoke-virtual {v10, v12}, Ljava/lang/Throwable;->addSuppressed(Ljava/lang/Throwable;)V
    :try_end_10e
    .catch Ljava/lang/IndexOutOfBoundsException; {:try_start_10b .. :try_end_10e} :catch_6a
    .catch Ljava/lang/Exception; {:try_start_10b .. :try_end_10e} :catch_fb
    .catchall {:try_start_10b .. :try_end_10e} :catchall_107

    goto/16 :goto_69
.end method


# virtual methods
.method public onReceive(Landroid/content/Context;Landroid/content/Intent;)V
    .registers 5
    .param p1, "context"    # Landroid/content/Context;
    .param p2, "intent"    # Landroid/content/Intent;

    .prologue
    .line 98
    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;

    move-result-object v0

    if-eqz v0, :cond_35

    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;

    move-result-object v0

    const-string v1, "STORE_TRACES"

    invoke-virtual {v0, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v0

    if-eqz v0, :cond_35

    .line 99
    sget-object v0, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v1, "Received Broadcast"

    invoke-virtual {v0, v1}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 101
    sget v0, Landroid/os/Build$VERSION;->SDK_INT:I

    const/16 v1, 0x17

    if-lt v0, v1, :cond_2e

    .line 102
    const-string v0, "android.permission.WRITE_EXTERNAL_STORAGE"

    invoke-static {p1, v0}, Lde/uni_passau/fim/auermich/tracer/Tracer;->isPermissionGranted(Landroid/content/Context;Ljava/lang/String;)Z

    move-result v0

    if-nez v0, :cond_2e

    .line 103
    sget-object v0, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v1, "Permissions got dropped unexpectedly!"

    invoke-virtual {v0, v1}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 112
    :cond_2e
    const-class v1, Lde/uni_passau/fim/auermich/tracer/Tracer;

    monitor-enter v1

    .line 113
    :try_start_31
    invoke-static {}, Lde/uni_passau/fim/auermich/tracer/Tracer;->writeRemainingTraces()V

    .line 114
    monitor-exit v1

    .line 116
    :cond_35
    return-void

    .line 114
    :catchall_36
    move-exception v0

    monitor-exit v1
    :try_end_38
    .catchall {:try_start_31 .. :try_end_38} :catchall_36

    throw v0
.end method
