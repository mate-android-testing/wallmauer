.class public Lde/uni_passau/fim/auermich/tracer/Tracer;
.super Landroid/content/BroadcastReceiver;
.source "Tracer.java"


# static fields
.field private static final CACHE_SIZE:I = 0x1388

.field private static final LOGGER:Ljava/util/logging/Logger;

.field private static final TRACES_FILE:Ljava/lang/String; = "traces.txt"

.field private static numberOfTraces:I

.field private static traces:Ljava/util/Set;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/Set",
            "<",
            "Ljava/lang/String;",
            ">;"
        }
    .end annotation
.end field


# direct methods
.method static constructor <clinit>()V
    .registers 1

    .prologue
    .line 42
    new-instance v0, Ljava/util/LinkedHashSet;

    invoke-direct {v0}, Ljava/util/LinkedHashSet;-><init>()V

    sput-object v0, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    .line 48
    const/4 v0, 0x0

    sput v0, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    .line 51
    const-class v0, Lde/uni_passau/fim/auermich/tracer/Tracer;

    invoke-virtual {v0}, Ljava/lang/Class;->getName()Ljava/lang/String;

    move-result-object v0

    invoke-static {v0}, Ljava/util/logging/Logger;->getLogger(Ljava/lang/String;)Ljava/util/logging/Logger;

    move-result-object v0

    sput-object v0, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    return-void
.end method

.method public constructor <init>()V
    .registers 1

    .prologue
    .line 28
    invoke-direct {p0}, Landroid/content/BroadcastReceiver;-><init>()V

    return-void
.end method

.method private static getApplicationUsingReflection()Landroid/app/Application;
    .registers 5

    .prologue
    const/4 v2, 0x0

    .line 107
    :try_start_1
    const-string v1, "android.app.ActivityThread"

    invoke-static {v1}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v1

    const-string v3, "currentApplication"

    const/4 v4, 0x0

    new-array v4, v4, [Ljava/lang/Class;

    .line 108
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

    .line 112
    .local v0, "e":Ljava/lang/Exception;
    :goto_1a
    return-object v1

    .line 109
    .end local v0    # "e":Ljava/lang/Exception;
    :catch_1b
    move-exception v0

    .line 110
    .restart local v0    # "e":Ljava/lang/Exception;
    sget-object v1, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v3, "Couldn\'t retrieve global context object!"

    invoke-virtual {v1, v3}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 111
    invoke-virtual {v0}, Ljava/lang/Exception;->printStackTrace()V

    move-object v1, v2

    .line 112
    goto :goto_1a
.end method

.method private static isPermissionGranted(Landroid/content/Context;Ljava/lang/String;)Z
    .registers 4
    .param p0, "context"    # Landroid/content/Context;
    .param p1, "permission"    # Ljava/lang/String;

    .prologue
    .line 126
    if-nez p0, :cond_6

    .line 127
    invoke-static {}, Lde/uni_passau/fim/auermich/tracer/Tracer;->getApplicationUsingReflection()Landroid/app/Application;

    move-result-object p0

    .line 130
    :cond_6
    if-nez p0, :cond_10

    .line 131
    new-instance v0, Ljava/lang/IllegalStateException;

    const-string v1, "Couldn\'t access context object!"

    invoke-direct {v0, v1}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V

    throw v0

    .line 133
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
    .line 94
    const-class v1, Lde/uni_passau/fim/auermich/tracer/Tracer;

    monitor-enter v1

    .line 95
    :try_start_3
    sget-object v0, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v0, p0}, Ljava/util/Set;->add(Ljava/lang/Object;)Z

    .line 97
    sget-object v0, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v0}, Ljava/util/Set;->size()I

    move-result v0

    const/16 v2, 0x1388

    if-ne v0, v2, :cond_1a

    .line 98
    invoke-static {}, Lde/uni_passau/fim/auermich/tracer/Tracer;->write()V

    .line 99
    sget-object v0, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v0}, Ljava/util/Set;->clear()V

    .line 101
    :cond_1a
    monitor-exit v1

    .line 102
    return-void

    .line 101
    :catchall_1c
    move-exception v0

    monitor-exit v1
    :try_end_1e
    .catchall {:try_start_3 .. :try_end_1e} :catchall_1c

    throw v0
.end method

.method private static declared-synchronized write()V
    .registers 16

    .prologue
    .line 143
    const-class v11, Lde/uni_passau/fim/auermich/tracer/Tracer;

    monitor-enter v11

    :try_start_3
    invoke-static {}, Landroid/os/Environment;->getExternalStorageDirectory()Ljava/io/File;

    move-result-object v2

    .line 144
    .local v2, "sdCard":Ljava/io/File;
    new-instance v8, Ljava/io/File;

    const-string v10, "traces.txt"

    invoke-direct {v8, v2, v10}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V

    .line 146
    .local v8, "traceFile":Ljava/io/File;
    const/4 v10, 0x0

    const-string v12, "android.permission.WRITE_EXTERNAL_STORAGE"

    invoke-static {v10, v12}, Lde/uni_passau/fim/auermich/tracer/Tracer;->isPermissionGranted(Landroid/content/Context;Ljava/lang/String;)Z

    move-result v10

    if-nez v10, :cond_1e

    .line 147
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v12, "Permissions got dropped unexpectedly!"

    invoke-virtual {v10, v12}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V
    :try_end_1e
    .catchall {:try_start_3 .. :try_end_1e} :catchall_d7

    .line 152
    :cond_1e
    :try_start_1e
    new-instance v9, Ljava/io/FileWriter;

    const/4 v10, 0x1

    invoke-direct {v9, v8, v10}, Ljava/io/FileWriter;-><init>(Ljava/io/File;Z)V

    .line 153
    .local v9, "writer":Ljava/io/FileWriter;
    new-instance v0, Ljava/io/BufferedWriter;

    invoke-direct {v0, v9}, Ljava/io/BufferedWriter;-><init>(Ljava/io/Writer;)V

    .line 155
    .local v0, "br":Ljava/io/BufferedWriter;
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v10}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v10

    :goto_2f
    invoke-interface {v10}, Ljava/util/Iterator;->hasNext()Z

    move-result v12

    if-eqz v12, :cond_a0

    invoke-interface {v10}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v7

    check-cast v7, Ljava/lang/String;

    .line 156
    .local v7, "trace":Ljava/lang/String;
    invoke-virtual {v0, v7}, Ljava/io/BufferedWriter;->write(Ljava/lang/String;)V

    .line 157
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->newLine()V
    :try_end_41
    .catch Ljava/lang/IndexOutOfBoundsException; {:try_start_1e .. :try_end_41} :catch_42
    .catch Ljava/io/IOException; {:try_start_1e .. :try_end_41} :catch_cb
    .catchall {:try_start_1e .. :try_end_41} :catchall_d7

    goto :goto_2f

    .line 168
    .end local v0    # "br":Ljava/io/BufferedWriter;
    .end local v7    # "trace":Ljava/lang/String;
    .end local v9    # "writer":Ljava/io/FileWriter;
    :catch_42
    move-exception v1

    .line 169
    .local v1, "e":Ljava/lang/IndexOutOfBoundsException;
    :try_start_43
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v12, "Synchronization issue!"

    invoke-virtual {v10, v12}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 170
    invoke-static {}, Ljava/lang/Thread;->getAllStackTraces()Ljava/util/Map;

    move-result-object v6

    .line 171
    .local v6, "threadStackTraces":Ljava/util/Map;, "Ljava/util/Map<Ljava/lang/Thread;[Ljava/lang/StackTraceElement;>;"
    invoke-interface {v6}, Ljava/util/Map;->keySet()Ljava/util/Set;

    move-result-object v10

    invoke-interface {v10}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v12

    :cond_56
    invoke-interface {v12}, Ljava/util/Iterator;->hasNext()Z

    move-result v10

    if-eqz v10, :cond_c9

    invoke-interface {v12}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v5

    check-cast v5, Ljava/lang/Thread;

    .line 172
    .local v5, "thread":Ljava/lang/Thread;
    invoke-interface {v6, v5}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v3

    check-cast v3, [Ljava/lang/StackTraceElement;

    .line 173
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

    .line 174
    array-length v13, v3

    const/4 v10, 0x0

    :goto_90
    if-ge v10, v13, :cond_56

    aget-object v4, v3, v10

    .line 175
    .local v4, "stackTraceElement":Ljava/lang/StackTraceElement;
    sget-object v14, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    invoke-static {v4}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v15

    invoke-virtual {v14, v15}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V
    :try_end_9d
    .catchall {:try_start_43 .. :try_end_9d} :catchall_d7

    .line 174
    add-int/lit8 v10, v10, 0x1

    goto :goto_90

    .line 161
    .end local v1    # "e":Ljava/lang/IndexOutOfBoundsException;
    .end local v3    # "stackTrace":[Ljava/lang/StackTraceElement;
    .end local v4    # "stackTraceElement":Ljava/lang/StackTraceElement;
    .end local v5    # "thread":Ljava/lang/Thread;
    .end local v6    # "threadStackTraces":Ljava/util/Map;, "Ljava/util/Map<Ljava/lang/Thread;[Ljava/lang/StackTraceElement;>;"
    .restart local v0    # "br":Ljava/io/BufferedWriter;
    .restart local v9    # "writer":Ljava/io/FileWriter;
    :cond_a0
    :try_start_a0
    sget v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    add-int/lit16 v10, v10, 0x1388

    sput v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    .line 162
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

    .line 164
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->flush()V

    .line 165
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->close()V

    .line 166
    invoke-virtual {v9}, Ljava/io/FileWriter;->close()V
    :try_end_c9
    .catch Ljava/lang/IndexOutOfBoundsException; {:try_start_a0 .. :try_end_c9} :catch_42
    .catch Ljava/io/IOException; {:try_start_a0 .. :try_end_c9} :catch_cb
    .catchall {:try_start_a0 .. :try_end_c9} :catchall_d7

    .line 182
    .end local v0    # "br":Ljava/io/BufferedWriter;
    .end local v9    # "writer":Ljava/io/FileWriter;
    :cond_c9
    :goto_c9
    monitor-exit v11

    return-void

    .line 178
    :catch_cb
    move-exception v1

    .line 179
    .local v1, "e":Ljava/io/IOException;
    :try_start_cc
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v12, "Writing to external storage failed."

    invoke-virtual {v10, v12}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 180
    invoke-virtual {v1}, Ljava/io/IOException;->printStackTrace()V
    :try_end_d6
    .catchall {:try_start_cc .. :try_end_d6} :catchall_d7

    goto :goto_c9

    .line 143
    .end local v1    # "e":Ljava/io/IOException;
    .end local v8    # "traceFile":Ljava/io/File;
    :catchall_d7
    move-exception v10

    monitor-exit v11

    throw v10
.end method

.method private static declared-synchronized write(Ljava/lang/String;)V
    .registers 14
    .param p0, "packageName"    # Ljava/lang/String;

    .prologue
    .line 194
    const-class v10, Lde/uni_passau/fim/auermich/tracer/Tracer;

    monitor-enter v10

    :try_start_3
    invoke-static {}, Landroid/os/Environment;->getExternalStorageDirectory()Ljava/io/File;

    move-result-object v6

    .line 195
    .local v6, "sdCard":Ljava/io/File;
    new-instance v7, Ljava/io/File;

    const-string v9, "traces.txt"

    invoke-direct {v7, v6, v9}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V

    .line 197
    .local v7, "traceFile":Ljava/io/File;
    sget-object v9, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    new-instance v11, Ljava/lang/StringBuilder;

    invoke-direct {v11}, Ljava/lang/StringBuilder;-><init>()V

    const-string v12, "Remaining traces size: "

    invoke-virtual {v11, v12}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v11

    sget-object v12, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v12}, Ljava/util/Set;->size()I

    move-result v12

    invoke-virtual {v11, v12}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v11

    invoke-virtual {v11}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v11

    invoke-virtual {v9, v11}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V
    :try_end_2c
    .catchall {:try_start_3 .. :try_end_2c} :catchall_100

    .line 202
    :try_start_2c
    new-instance v8, Ljava/io/FileWriter;

    const/4 v9, 0x1

    invoke-direct {v8, v7, v9}, Ljava/io/FileWriter;-><init>(Ljava/io/File;Z)V

    .line 203
    .local v8, "writer":Ljava/io/FileWriter;
    new-instance v0, Ljava/io/BufferedWriter;

    invoke-direct {v0, v8}, Ljava/io/BufferedWriter;-><init>(Ljava/io/Writer;)V

    .line 205
    .local v0, "br":Ljava/io/BufferedWriter;
    sget-object v9, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v9}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v5

    .line 206
    .local v5, "iterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    const/4 v2, 0x0

    .line 208
    .local v2, "element":Ljava/lang/String;
    :goto_3e
    invoke-interface {v5}, Ljava/util/Iterator;->hasNext()Z

    move-result v9

    if-eqz v9, :cond_d5

    .line 210
    if-nez v2, :cond_ce

    .line 211
    invoke-interface {v5}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v2

    .end local v2    # "element":Ljava/lang/String;
    check-cast v2, Ljava/lang/String;

    .line 212
    .restart local v2    # "element":Ljava/lang/String;
    sget-object v9, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    new-instance v11, Ljava/lang/StringBuilder;

    invoke-direct {v11}, Ljava/lang/StringBuilder;-><init>()V

    const-string v12, "First entry: "

    invoke-virtual {v11, v12}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v11

    invoke-virtual {v11, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v11

    invoke-virtual {v11}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v11

    invoke-virtual {v9, v11}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 217
    :goto_64
    invoke-virtual {v0, v2}, Ljava/io/BufferedWriter;->write(Ljava/lang/String;)V

    .line 218
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->newLine()V
    :try_end_6a
    .catch Ljava/io/IOException; {:try_start_2c .. :try_end_6a} :catch_6b
    .catchall {:try_start_2c .. :try_end_6a} :catchall_100

    goto :goto_3e

    .line 229
    .end local v0    # "br":Ljava/io/BufferedWriter;
    .end local v2    # "element":Ljava/lang/String;
    .end local v5    # "iterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    .end local v8    # "writer":Ljava/io/FileWriter;
    :catch_6b
    move-exception v1

    .line 230
    .local v1, "e":Ljava/io/IOException;
    :try_start_6c
    sget-object v9, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v11, "Writing to external storage failed."

    invoke-virtual {v9, v11}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 231
    invoke-virtual {v1}, Ljava/io/IOException;->printStackTrace()V
    :try_end_76
    .catchall {:try_start_6c .. :try_end_76} :catchall_100

    .line 236
    .end local v1    # "e":Ljava/io/IOException;
    :goto_76
    :try_start_76
    new-instance v9, Ljava/lang/StringBuilder;

    invoke-direct {v9}, Ljava/lang/StringBuilder;-><init>()V

    const-string v11, "data/data/"

    invoke-virtual {v9, v11}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v9

    invoke-virtual {v9, p0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v9

    invoke-virtual {v9}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    .line 237
    .local v3, "filePath":Ljava/lang/String;
    new-instance v4, Ljava/io/File;

    const-string v9, "info.txt"

    invoke-direct {v4, v3, v9}, Ljava/io/File;-><init>(Ljava/lang/String;Ljava/lang/String;)V

    .line 238
    .local v4, "info":Ljava/io/File;
    new-instance v8, Ljava/io/FileWriter;

    invoke-direct {v8, v4}, Ljava/io/FileWriter;-><init>(Ljava/io/File;)V

    .line 240
    .restart local v8    # "writer":Ljava/io/FileWriter;
    sget v9, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    sget-object v11, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v11}, Ljava/util/Set;->size()I

    move-result v11

    add-int/2addr v9, v11

    sput v9, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    .line 241
    sget v9, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    invoke-static {v9}, Ljava/lang/String;->valueOf(I)Ljava/lang/String;

    move-result-object v9

    invoke-virtual {v8, v9}, Ljava/io/FileWriter;->append(Ljava/lang/CharSequence;)Ljava/io/Writer;

    .line 242
    sget-object v9, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    new-instance v11, Ljava/lang/StringBuilder;

    invoke-direct {v11}, Ljava/lang/StringBuilder;-><init>()V

    const-string v12, "Total number of traces in file: "

    invoke-virtual {v11, v12}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v11

    sget v12, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    invoke-virtual {v11, v12}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v11

    invoke-virtual {v11}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v11

    invoke-virtual {v9, v11}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 245
    const/4 v9, 0x0

    sput v9, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    .line 247
    invoke-virtual {v8}, Ljava/io/FileWriter;->flush()V

    .line 248
    invoke-virtual {v8}, Ljava/io/FileWriter;->close()V
    :try_end_cc
    .catch Ljava/io/IOException; {:try_start_76 .. :try_end_cc} :catch_103
    .catchall {:try_start_76 .. :try_end_cc} :catchall_100

    .line 254
    .end local v3    # "filePath":Ljava/lang/String;
    .end local v4    # "info":Ljava/io/File;
    .end local v8    # "writer":Ljava/io/FileWriter;
    :goto_cc
    monitor-exit v10

    return-void

    .line 214
    .restart local v0    # "br":Ljava/io/BufferedWriter;
    .restart local v2    # "element":Ljava/lang/String;
    .restart local v5    # "iterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    .restart local v8    # "writer":Ljava/io/FileWriter;
    :cond_ce
    :try_start_ce
    invoke-interface {v5}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v2

    .end local v2    # "element":Ljava/lang/String;
    check-cast v2, Ljava/lang/String;

    .restart local v2    # "element":Ljava/lang/String;
    goto :goto_64

    .line 221
    :cond_d5
    sget-object v9, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v9}, Ljava/util/Set;->isEmpty()Z

    move-result v9

    if-nez v9, :cond_f5

    .line 222
    sget-object v9, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    new-instance v11, Ljava/lang/StringBuilder;

    invoke-direct {v11}, Ljava/lang/StringBuilder;-><init>()V

    const-string v12, "Last entry: "

    invoke-virtual {v11, v12}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v11

    invoke-virtual {v11, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v11

    invoke-virtual {v11}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v11

    invoke-virtual {v9, v11}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 225
    :cond_f5
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->flush()V

    .line 226
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->close()V

    .line 227
    invoke-virtual {v8}, Ljava/io/FileWriter;->close()V
    :try_end_fe
    .catch Ljava/io/IOException; {:try_start_ce .. :try_end_fe} :catch_6b
    .catchall {:try_start_ce .. :try_end_fe} :catchall_100

    goto/16 :goto_76

    .line 194
    .end local v0    # "br":Ljava/io/BufferedWriter;
    .end local v2    # "element":Ljava/lang/String;
    .end local v5    # "iterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    .end local v6    # "sdCard":Ljava/io/File;
    .end local v7    # "traceFile":Ljava/io/File;
    .end local v8    # "writer":Ljava/io/FileWriter;
    :catchall_100
    move-exception v9

    monitor-exit v10

    throw v9

    .line 250
    .restart local v6    # "sdCard":Ljava/io/File;
    .restart local v7    # "traceFile":Ljava/io/File;
    :catch_103
    move-exception v1

    .line 251
    .restart local v1    # "e":Ljava/io/IOException;
    :try_start_104
    sget-object v9, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v11, "Writing to internal storage failed."

    invoke-virtual {v9, v11}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 252
    invoke-virtual {v1}, Ljava/io/IOException;->printStackTrace()V
    :try_end_10e
    .catchall {:try_start_104 .. :try_end_10e} :catchall_100

    goto :goto_cc
.end method


# virtual methods
.method public onReceive(Landroid/content/Context;Landroid/content/Intent;)V
    .registers 6
    .param p1, "context"    # Landroid/content/Context;
    .param p2, "intent"    # Landroid/content/Intent;

    .prologue
    .line 66
    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;

    move-result-object v1

    if-eqz v1, :cond_3a

    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;

    move-result-object v1

    const-string v2, "STORE_TRACES"

    invoke-virtual {v1, v2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v1

    if-eqz v1, :cond_3a

    .line 67
    const-string v1, "packageName"

    invoke-virtual {p2, v1}, Landroid/content/Intent;->getStringExtra(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    .line 68
    .local v0, "packageName":Ljava/lang/String;
    sget-object v1, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v2, "Received Broadcast"

    invoke-virtual {v1, v2}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 70
    const-string v1, "android.permission.WRITE_EXTERNAL_STORAGE"

    invoke-static {p1, v1}, Lde/uni_passau/fim/auermich/tracer/Tracer;->isPermissionGranted(Landroid/content/Context;Ljava/lang/String;)Z

    move-result v1

    if-nez v1, :cond_2e

    .line 71
    sget-object v1, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v2, "Permissions got dropped unexpectedly!"

    invoke-virtual {v1, v2}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 79
    :cond_2e
    const-class v2, Lde/uni_passau/fim/auermich/tracer/Tracer;

    monitor-enter v2

    .line 80
    :try_start_31
    invoke-static {v0}, Lde/uni_passau/fim/auermich/tracer/Tracer;->write(Ljava/lang/String;)V

    .line 81
    sget-object v1, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v1}, Ljava/util/Set;->clear()V

    .line 82
    monitor-exit v2

    .line 84
    .end local v0    # "packageName":Ljava/lang/String;
    :cond_3a
    return-void

    .line 82
    .restart local v0    # "packageName":Ljava/lang/String;
    :catchall_3b
    move-exception v1

    monitor-exit v2
    :try_end_3d
    .catchall {:try_start_31 .. :try_end_3d} :catchall_3b

    throw v1
.end method
