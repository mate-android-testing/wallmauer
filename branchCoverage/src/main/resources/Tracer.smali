.class public Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;
.super Landroid/content/BroadcastReceiver;
.source "Tracer.java"


# static fields
.field private static final CACHE_SIZE:I = 0x1388

.field private static final LOGGER:Ljava/util/logging/Logger;

.field private static final TRACES_FILE:Ljava/lang/String; = "traces.txt"

.field private static executionPath:Ljava/util/List;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/List",
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
    .line 28
    new-instance v0, Ljava/util/ArrayList;

    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V

    invoke-static {v0}, Ljava/util/Collections;->synchronizedList(Ljava/util/List;)Ljava/util/List;

    move-result-object v0

    sput-object v0, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;->executionPath:Ljava/util/List;

    .line 34
    const-class v0, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;

    .line 35
    invoke-virtual {v0}, Ljava/lang/Class;->getName()Ljava/lang/String;

    move-result-object v0

    .line 34
    invoke-static {v0}, Ljava/util/logging/Logger;->getLogger(Ljava/lang/String;)Ljava/util/logging/Logger;

    move-result-object v0

    sput-object v0, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    return-void
.end method

.method public constructor <init>()V
    .registers 1

    .prologue
    .line 24
    invoke-direct {p0}, Landroid/content/BroadcastReceiver;-><init>()V

    return-void
.end method

.method public static trace(Ljava/lang/String;)V
    .registers 4
    .param p0, "identifier"    # Ljava/lang/String;

    .prologue
    .line 57
    const-class v1, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;

    monitor-enter v1

    .line 58
    :try_start_3
    sget-object v0, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v0, p0}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 60
    sget-object v0, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v0}, Ljava/util/List;->size()I

    move-result v0

    const/16 v2, 0x1388

    if-ne v0, v2, :cond_1a

    .line 61
    invoke-static {}, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;->write()V

    .line 62
    sget-object v0, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v0}, Ljava/util/List;->clear()V

    .line 64
    :cond_1a
    monitor-exit v1

    .line 65
    return-void

    .line 64
    :catchall_1c
    move-exception v0

    monitor-exit v1
    :try_end_1e
    .catchall {:try_start_3 .. :try_end_1e} :catchall_1c

    throw v0
.end method

.method private static write()V
    .registers 16

    .prologue
    .line 69
    invoke-static {}, Landroid/os/Environment;->getExternalStorageDirectory()Ljava/io/File;

    move-result-object v4

    .line 70
    .local v4, "sdCard":Ljava/io/File;
    new-instance v9, Ljava/io/File;

    const-string v11, "traces.txt"

    invoke-direct {v9, v4, v11}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V

    .line 74
    .local v9, "traces":Ljava/io/File;
    :try_start_b
    new-instance v10, Ljava/io/FileWriter;

    const/4 v11, 0x1

    invoke-direct {v10, v9, v11}, Ljava/io/FileWriter;-><init>(Ljava/io/File;Z)V

    .line 75
    .local v10, "writer":Ljava/io/FileWriter;
    new-instance v0, Ljava/io/BufferedWriter;

    invoke-direct {v0, v10}, Ljava/io/BufferedWriter;-><init>(Ljava/io/Writer;)V

    .line 77
    .local v0, "br":Ljava/io/BufferedWriter;
    const/4 v2, 0x0

    .local v2, "i":I
    :goto_17
    const/16 v11, 0x1388

    if-ge v2, v11, :cond_2c

    .line 78
    sget-object v11, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v11, v2}, Ljava/util/List;->get(I)Ljava/lang/Object;

    move-result-object v3

    check-cast v3, Ljava/lang/String;

    .line 79
    .local v3, "pathNode":Ljava/lang/String;
    invoke-virtual {v0, v3}, Ljava/io/BufferedWriter;->write(Ljava/lang/String;)V

    .line 80
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->newLine()V

    .line 77
    add-int/lit8 v2, v2, 0x1

    goto :goto_17

    .line 83
    .end local v3    # "pathNode":Ljava/lang/String;
    :cond_2c
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->flush()V

    .line 84
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->close()V

    .line 85
    invoke-virtual {v10}, Ljava/io/FileWriter;->close()V
    :try_end_35
    .catch Ljava/lang/IndexOutOfBoundsException; {:try_start_b .. :try_end_35} :catch_36
    .catch Ljava/io/IOException; {:try_start_b .. :try_end_35} :catch_8d

    .line 100
    .end local v0    # "br":Ljava/io/BufferedWriter;
    .end local v2    # "i":I
    .end local v10    # "writer":Ljava/io/FileWriter;
    :cond_35
    :goto_35
    return-void

    .line 87
    :catch_36
    move-exception v1

    .line 88
    .local v1, "e":Ljava/lang/IndexOutOfBoundsException;
    invoke-static {}, Ljava/lang/Thread;->getAllStackTraces()Ljava/util/Map;

    move-result-object v8

    .line 89
    .local v8, "threadStackTraces":Ljava/util/Map;, "Ljava/util/Map<Ljava/lang/Thread;[Ljava/lang/StackTraceElement;>;"
    invoke-interface {v8}, Ljava/util/Map;->keySet()Ljava/util/Set;

    move-result-object v11

    invoke-interface {v11}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v12

    :cond_43
    invoke-interface {v12}, Ljava/util/Iterator;->hasNext()Z

    move-result v11

    if-eqz v11, :cond_35

    invoke-interface {v12}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v7

    check-cast v7, Ljava/lang/Thread;

    .line 90
    .local v7, "thread":Ljava/lang/Thread;
    invoke-interface {v8, v7}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v5

    check-cast v5, [Ljava/lang/StackTraceElement;

    .line 91
    .local v5, "stackTrace":[Ljava/lang/StackTraceElement;
    sget-object v11, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    new-instance v13, Ljava/lang/StringBuilder;

    invoke-direct {v13}, Ljava/lang/StringBuilder;-><init>()V

    const-string v14, "Thread["

    invoke-virtual {v13, v14}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v13

    invoke-virtual {v7}, Ljava/lang/Thread;->getId()J

    move-result-wide v14

    invoke-virtual {v13, v14, v15}, Ljava/lang/StringBuilder;->append(J)Ljava/lang/StringBuilder;

    move-result-object v13

    const-string v14, "]: "

    invoke-virtual {v13, v14}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v13

    invoke-virtual {v13, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    move-result-object v13

    invoke-virtual {v13}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v13

    invoke-virtual {v11, v13}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 92
    array-length v13, v5

    const/4 v11, 0x0

    :goto_7d
    if-ge v11, v13, :cond_43

    aget-object v6, v5, v11

    .line 93
    .local v6, "stackTraceElement":Ljava/lang/StackTraceElement;
    sget-object v14, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    invoke-static {v6}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v15

    invoke-virtual {v14, v15}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 92
    add-int/lit8 v11, v11, 0x1

    goto :goto_7d

    .line 96
    .end local v1    # "e":Ljava/lang/IndexOutOfBoundsException;
    .end local v5    # "stackTrace":[Ljava/lang/StackTraceElement;
    .end local v6    # "stackTraceElement":Ljava/lang/StackTraceElement;
    .end local v7    # "thread":Ljava/lang/Thread;
    .end local v8    # "threadStackTraces":Ljava/util/Map;, "Ljava/util/Map<Ljava/lang/Thread;[Ljava/lang/StackTraceElement;>;"
    :catch_8d
    move-exception v1

    .line 97
    .local v1, "e":Ljava/io/IOException;
    sget-object v11, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v12, "Writing to external storage failed."

    invoke-virtual {v11, v12}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 98
    invoke-virtual {v1}, Ljava/io/IOException;->printStackTrace()V

    goto :goto_35
.end method

.method private static write(Ljava/lang/String;)V
    .registers 14
    .param p0, "packageName"    # Ljava/lang/String;

    .prologue
    .line 112
    invoke-static {}, Landroid/os/Environment;->getExternalStorageDirectory()Ljava/io/File;

    move-result-object v6

    .line 113
    .local v6, "sdCard":Ljava/io/File;
    new-instance v7, Ljava/io/File;

    const-string v9, "traces.txt"

    invoke-direct {v7, v6, v9}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V

    .line 115
    .local v7, "traces":Ljava/io/File;
    sget-object v9, Ljava/lang/System;->out:Ljava/io/PrintStream;

    new-instance v10, Ljava/lang/StringBuilder;

    invoke-direct {v10}, Ljava/lang/StringBuilder;-><init>()V

    const-string v11, "Size: "

    invoke-virtual {v10, v11}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v10

    sget-object v11, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v11}, Ljava/util/List;->size()I

    move-result v11

    invoke-virtual {v10, v11}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v10

    invoke-virtual {v10}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v10

    invoke-virtual {v9, v10}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 117
    sget-object v9, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v9}, Ljava/util/List;->isEmpty()Z

    move-result v9

    if-nez v9, :cond_7a

    .line 118
    sget-object v10, Ljava/lang/System;->out:Ljava/io/PrintStream;

    new-instance v9, Ljava/lang/StringBuilder;

    invoke-direct {v9}, Ljava/lang/StringBuilder;-><init>()V

    const-string v11, "First entry: "

    invoke-virtual {v9, v11}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v11

    sget-object v9, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;->executionPath:Ljava/util/List;

    const/4 v12, 0x0

    invoke-interface {v9, v12}, Ljava/util/List;->get(I)Ljava/lang/Object;

    move-result-object v9

    check-cast v9, Ljava/lang/String;

    invoke-virtual {v11, v9}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v9

    invoke-virtual {v9}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v9

    invoke-virtual {v10, v9}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 119
    sget-object v10, Ljava/lang/System;->out:Ljava/io/PrintStream;

    new-instance v9, Ljava/lang/StringBuilder;

    invoke-direct {v9}, Ljava/lang/StringBuilder;-><init>()V

    const-string v11, "Last entry: "

    invoke-virtual {v9, v11}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v11

    sget-object v9, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;->executionPath:Ljava/util/List;

    sget-object v12, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v12}, Ljava/util/List;->size()I

    move-result v12

    add-int/lit8 v12, v12, -0x1

    invoke-interface {v9, v12}, Ljava/util/List;->get(I)Ljava/lang/Object;

    move-result-object v9

    check-cast v9, Ljava/lang/String;

    invoke-virtual {v11, v9}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v9

    invoke-virtual {v9}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v9

    invoke-virtual {v10, v9}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 125
    :cond_7a
    :try_start_7a
    new-instance v8, Ljava/io/FileWriter;

    const/4 v9, 0x1

    invoke-direct {v8, v7, v9}, Ljava/io/FileWriter;-><init>(Ljava/io/File;Z)V

    .line 126
    .local v8, "writer":Ljava/io/FileWriter;
    new-instance v0, Ljava/io/BufferedWriter;

    invoke-direct {v0, v8}, Ljava/io/BufferedWriter;-><init>(Ljava/io/Writer;)V

    .line 128
    .local v0, "br":Ljava/io/BufferedWriter;
    const/4 v3, 0x0

    .local v3, "i":I
    :goto_86
    sget-object v9, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v9}, Ljava/util/List;->size()I

    move-result v9

    if-ge v3, v9, :cond_9f

    .line 129
    sget-object v9, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v9, v3}, Ljava/util/List;->get(I)Ljava/lang/Object;

    move-result-object v5

    check-cast v5, Ljava/lang/String;

    .line 130
    .local v5, "pathNode":Ljava/lang/String;
    invoke-virtual {v0, v5}, Ljava/io/BufferedWriter;->write(Ljava/lang/String;)V

    .line 131
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->newLine()V

    .line 128
    add-int/lit8 v3, v3, 0x1

    goto :goto_86

    .line 134
    .end local v5    # "pathNode":Ljava/lang/String;
    :cond_9f
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->flush()V

    .line 135
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->close()V

    .line 136
    invoke-virtual {v8}, Ljava/io/FileWriter;->close()V
    :try_end_a8
    .catch Ljava/io/IOException; {:try_start_7a .. :try_end_a8} :catch_db

    .line 145
    .end local v0    # "br":Ljava/io/BufferedWriter;
    .end local v3    # "i":I
    .end local v8    # "writer":Ljava/io/FileWriter;
    :goto_a8
    :try_start_a8
    new-instance v9, Ljava/lang/StringBuilder;

    invoke-direct {v9}, Ljava/lang/StringBuilder;-><init>()V

    const-string v10, "data/data/"

    invoke-virtual {v9, v10}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v9

    invoke-virtual {v9, p0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v9

    invoke-virtual {v9}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    .line 146
    .local v2, "filePath":Ljava/lang/String;
    new-instance v4, Ljava/io/File;

    const-string v9, "info.txt"

    invoke-direct {v4, v2, v9}, Ljava/io/File;-><init>(Ljava/lang/String;Ljava/lang/String;)V

    .line 147
    .local v4, "info":Ljava/io/File;
    new-instance v8, Ljava/io/FileWriter;

    invoke-direct {v8, v4}, Ljava/io/FileWriter;-><init>(Ljava/io/File;)V

    .line 149
    .restart local v8    # "writer":Ljava/io/FileWriter;
    sget-object v9, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v9}, Ljava/util/List;->size()I

    move-result v9

    invoke-static {v9}, Ljava/lang/String;->valueOf(I)Ljava/lang/String;

    move-result-object v9

    invoke-virtual {v8, v9}, Ljava/io/FileWriter;->append(Ljava/lang/CharSequence;)Ljava/io/Writer;

    .line 150
    invoke-virtual {v8}, Ljava/io/FileWriter;->flush()V

    .line 151
    invoke-virtual {v8}, Ljava/io/FileWriter;->close()V
    :try_end_da
    .catch Ljava/io/IOException; {:try_start_a8 .. :try_end_da} :catch_e7

    .line 157
    .end local v2    # "filePath":Ljava/lang/String;
    .end local v4    # "info":Ljava/io/File;
    .end local v8    # "writer":Ljava/io/FileWriter;
    :goto_da
    return-void

    .line 138
    :catch_db
    move-exception v1

    .line 139
    .local v1, "e":Ljava/io/IOException;
    sget-object v9, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v10, "Writing to external storage failed."

    invoke-virtual {v9, v10}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 140
    invoke-virtual {v1}, Ljava/io/IOException;->printStackTrace()V

    goto :goto_a8

    .line 153
    .end local v1    # "e":Ljava/io/IOException;
    :catch_e7
    move-exception v1

    .line 154
    .restart local v1    # "e":Ljava/io/IOException;
    sget-object v9, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v10, "Writing to internal storage failed."

    invoke-virtual {v9, v10}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 155
    invoke-virtual {v1}, Ljava/io/IOException;->printStackTrace()V

    goto :goto_da
.end method


# virtual methods
.method public onReceive(Landroid/content/Context;Landroid/content/Intent;)V
    .registers 6
    .param p1, "context"    # Landroid/content/Context;
    .param p2, "intent"    # Landroid/content/Intent;

    .prologue
    .line 41
    sget-object v1, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v2, "Received Broadcast"

    invoke-virtual {v1, v2}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 43
    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;

    move-result-object v1

    if-eqz v1, :cond_27

    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;

    move-result-object v1

    const-string v2, "STORE_TRACES"

    invoke-virtual {v1, v2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v1

    if-eqz v1, :cond_27

    .line 44
    const-string v1, "packageName"

    invoke-virtual {p2, v1}, Landroid/content/Intent;->getStringExtra(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    .line 45
    .local v0, "packageName":Ljava/lang/String;
    invoke-static {v0}, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;->write(Ljava/lang/String;)V

    .line 46
    sget-object v1, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v1}, Ljava/util/List;->clear()V

    .line 48
    .end local v0    # "packageName":Ljava/lang/String;
    :cond_27
    return-void
.end method
