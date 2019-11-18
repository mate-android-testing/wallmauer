.class public Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;
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
    .line 24
    new-instance v0, Ljava/util/ArrayList;

    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V

    sput-object v0, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    .line 30
    const-class v0, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;

    .line 31
    invoke-virtual {v0}, Ljava/lang/Class;->getName()Ljava/lang/String;

    move-result-object v0

    .line 30
    invoke-static {v0}, Ljava/util/logging/Logger;->getLogger(Ljava/lang/String;)Ljava/util/logging/Logger;

    move-result-object v0

    sput-object v0, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    return-void
.end method

.method public constructor <init>()V
    .registers 1

    .prologue
    .line 21
    invoke-direct {p0}, Landroid/content/BroadcastReceiver;-><init>()V

    return-void
.end method

.method private static getCurrentTimeStamp()Ljava/lang/String;
    .registers 2

    .prologue
    .line 42
    invoke-static {}, Ljava/time/LocalDateTime;->now()Ljava/time/LocalDateTime;

    move-result-object v0

    const-string v1, "yyyy-MM-dd HH:mm:ss.SSS"

    .line 43
    invoke-static {v1}, Ljava/time/format/DateTimeFormatter;->ofPattern(Ljava/lang/String;)Ljava/time/format/DateTimeFormatter;

    move-result-object v1

    invoke-virtual {v0, v1}, Ljava/time/LocalDateTime;->format(Ljava/time/format/DateTimeFormatter;)Ljava/lang/String;

    move-result-object v0

    .line 42
    return-object v0
.end method

.method public static trace(Ljava/lang/String;)V
    .registers 3
    .param p0, "identifier"    # Ljava/lang/String;

    .prologue
    .line 64
    sget-object v0, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v0, p0}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 66
    sget-object v0, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v0}, Ljava/util/List;->size()I

    move-result v0

    const/16 v1, 0x1388

    if-ne v0, v1, :cond_17

    .line 67
    invoke-static {}, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->write()V

    .line 68
    sget-object v0, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v0}, Ljava/util/List;->clear()V

    .line 70
    :cond_17
    return-void
.end method

.method private static write()V
    .registers 9

    .prologue
    .line 74
    invoke-static {}, Landroid/os/Environment;->getExternalStorageDirectory()Ljava/io/File;

    move-result-object v4

    .line 75
    .local v4, "sdCard":Ljava/io/File;
    new-instance v5, Ljava/io/File;

    const-string v7, "traces.txt"

    invoke-direct {v5, v4, v7}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V

    .line 79
    .local v5, "traces":Ljava/io/File;
    :try_start_b
    new-instance v6, Ljava/io/FileWriter;

    const/4 v7, 0x1

    invoke-direct {v6, v5, v7}, Ljava/io/FileWriter;-><init>(Ljava/io/File;Z)V

    .line 80
    .local v6, "writer":Ljava/io/FileWriter;
    new-instance v0, Ljava/io/BufferedWriter;

    invoke-direct {v0, v6}, Ljava/io/BufferedWriter;-><init>(Ljava/io/Writer;)V

    .line 82
    .local v0, "br":Ljava/io/BufferedWriter;
    const/4 v2, 0x0

    .local v2, "i":I
    :goto_17
    const/16 v7, 0x1388

    if-ge v2, v7, :cond_2c

    .line 83
    sget-object v7, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v7, v2}, Ljava/util/List;->get(I)Ljava/lang/Object;

    move-result-object v3

    check-cast v3, Ljava/lang/String;

    .line 84
    .local v3, "pathNode":Ljava/lang/String;
    invoke-virtual {v0, v3}, Ljava/io/BufferedWriter;->write(Ljava/lang/String;)V

    .line 85
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->newLine()V

    .line 82
    add-int/lit8 v2, v2, 0x1

    goto :goto_17

    .line 88
    .end local v3    # "pathNode":Ljava/lang/String;
    :cond_2c
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->flush()V

    .line 89
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->close()V

    .line 90
    invoke-virtual {v6}, Ljava/io/FileWriter;->close()V
    :try_end_35
    .catch Ljava/io/IOException; {:try_start_b .. :try_end_35} :catch_36

    .line 96
    .end local v0    # "br":Ljava/io/BufferedWriter;
    .end local v2    # "i":I
    .end local v6    # "writer":Ljava/io/FileWriter;
    :goto_35
    return-void

    .line 92
    :catch_36
    move-exception v1

    .line 93
    .local v1, "e":Ljava/io/IOException;
    sget-object v7, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v8, "Writing to external storage failed."

    invoke-virtual {v7, v8}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 94
    invoke-virtual {v1}, Ljava/io/IOException;->printStackTrace()V

    goto :goto_35
.end method

.method private static write(Ljava/lang/String;)V
    .registers 16
    .param p0, "packageName"    # Ljava/lang/String;

    .prologue
    .line 108
    invoke-static {}, Landroid/os/Environment;->getExternalStorageDirectory()Ljava/io/File;

    move-result-object v6

    .line 109
    .local v6, "sdCard":Ljava/io/File;
    new-instance v8, Ljava/io/File;

    const-string v10, "traces.txt"

    invoke-direct {v8, v6, v10}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V

    .line 111
    .local v8, "traces":Ljava/io/File;
    const-class v11, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;

    monitor-enter v11

    .line 112
    :try_start_e
    sget-object v10, Ljava/lang/System;->out:Ljava/io/PrintStream;

    new-instance v12, Ljava/lang/StringBuilder;

    invoke-direct {v12}, Ljava/lang/StringBuilder;-><init>()V

    const-string v13, "Size: "

    invoke-virtual {v12, v13}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v12

    sget-object v13, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v13}, Ljava/util/List;->size()I

    move-result v13

    invoke-virtual {v12, v13}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v12

    invoke-virtual {v12}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v12

    invoke-virtual {v10, v12}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 114
    sget-object v10, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v10}, Ljava/util/List;->isEmpty()Z

    move-result v10

    if-nez v10, :cond_7d

    .line 115
    sget-object v12, Ljava/lang/System;->out:Ljava/io/PrintStream;

    new-instance v10, Ljava/lang/StringBuilder;

    invoke-direct {v10}, Ljava/lang/StringBuilder;-><init>()V

    const-string v13, "First entry: "

    invoke-virtual {v10, v13}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v13

    sget-object v10, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    const/4 v14, 0x0

    invoke-interface {v10, v14}, Ljava/util/List;->get(I)Ljava/lang/Object;

    move-result-object v10

    check-cast v10, Ljava/lang/String;

    invoke-virtual {v13, v10}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v10

    invoke-virtual {v10}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v10

    invoke-virtual {v12, v10}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 116
    sget-object v12, Ljava/lang/System;->out:Ljava/io/PrintStream;

    new-instance v10, Ljava/lang/StringBuilder;

    invoke-direct {v10}, Ljava/lang/StringBuilder;-><init>()V

    const-string v13, "Last entry: "

    invoke-virtual {v10, v13}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v13

    sget-object v10, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    sget-object v14, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v14}, Ljava/util/List;->size()I

    move-result v14

    add-int/lit8 v14, v14, -0x1

    invoke-interface {v10, v14}, Ljava/util/List;->get(I)Ljava/lang/Object;

    move-result-object v10

    check-cast v10, Ljava/lang/String;

    invoke-virtual {v13, v10}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v10

    invoke-virtual {v10}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v10

    invoke-virtual {v12, v10}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 118
    :cond_7d
    monitor-exit v11
    :try_end_7e
    .catchall {:try_start_e .. :try_end_7e} :catchall_a3

    .line 123
    :try_start_7e
    new-instance v9, Ljava/io/FileWriter;

    const/4 v10, 0x1

    invoke-direct {v9, v8, v10}, Ljava/io/FileWriter;-><init>(Ljava/io/File;Z)V

    .line 124
    .local v9, "writer":Ljava/io/FileWriter;
    new-instance v0, Ljava/io/BufferedWriter;

    invoke-direct {v0, v9}, Ljava/io/BufferedWriter;-><init>(Ljava/io/Writer;)V

    .line 126
    .local v0, "br":Ljava/io/BufferedWriter;
    const/4 v3, 0x0

    .local v3, "i":I
    :goto_8a
    sget-object v10, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v10}, Ljava/util/List;->size()I

    move-result v10

    if-ge v3, v10, :cond_a6

    .line 127
    sget-object v10, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v10, v3}, Ljava/util/List;->get(I)Ljava/lang/Object;

    move-result-object v5

    check-cast v5, Ljava/lang/String;

    .line 128
    .local v5, "pathNode":Ljava/lang/String;
    invoke-virtual {v0, v5}, Ljava/io/BufferedWriter;->write(Ljava/lang/String;)V

    .line 129
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->newLine()V
    :try_end_a0
    .catch Ljava/io/IOException; {:try_start_7e .. :try_end_a0} :catch_143

    .line 126
    add-int/lit8 v3, v3, 0x1

    goto :goto_8a

    .line 118
    .end local v0    # "br":Ljava/io/BufferedWriter;
    .end local v3    # "i":I
    .end local v5    # "pathNode":Ljava/lang/String;
    .end local v9    # "writer":Ljava/io/FileWriter;
    :catchall_a3
    move-exception v10

    :try_start_a4
    monitor-exit v11
    :try_end_a5
    .catchall {:try_start_a4 .. :try_end_a5} :catchall_a3

    throw v10

    .line 132
    .restart local v0    # "br":Ljava/io/BufferedWriter;
    .restart local v3    # "i":I
    .restart local v9    # "writer":Ljava/io/FileWriter;
    :cond_a6
    :try_start_a6
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->flush()V

    .line 133
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->close()V

    .line 134
    invoke-virtual {v9}, Ljava/io/FileWriter;->close()V
    :try_end_af
    .catch Ljava/io/IOException; {:try_start_a6 .. :try_end_af} :catch_143

    .line 141
    .end local v0    # "br":Ljava/io/BufferedWriter;
    .end local v3    # "i":I
    .end local v9    # "writer":Ljava/io/FileWriter;
    :goto_af
    sget-object v10, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v10}, Ljava/util/List;->size()I

    move-result v7

    .line 142
    .local v7, "size":I
    sget-object v10, Ljava/lang/System;->out:Ljava/io/PrintStream;

    new-instance v11, Ljava/lang/StringBuilder;

    invoke-direct {v11}, Ljava/lang/StringBuilder;-><init>()V

    const-string v12, "Size: "

    invoke-virtual {v11, v12}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v11

    invoke-virtual {v11, v7}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v11

    invoke-virtual {v11}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v11

    invoke-virtual {v10, v11}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 143
    sget-object v11, Ljava/lang/System;->out:Ljava/io/PrintStream;

    new-instance v10, Ljava/lang/StringBuilder;

    invoke-direct {v10}, Ljava/lang/StringBuilder;-><init>()V

    const-string v12, "First entry afterwards: "

    invoke-virtual {v10, v12}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v12

    sget-object v10, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    const/4 v13, 0x0

    invoke-interface {v10, v13}, Ljava/util/List;->get(I)Ljava/lang/Object;

    move-result-object v10

    check-cast v10, Ljava/lang/String;

    invoke-virtual {v12, v10}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v10

    invoke-virtual {v10}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v10

    invoke-virtual {v11, v10}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 144
    sget-object v11, Ljava/lang/System;->out:Ljava/io/PrintStream;

    new-instance v10, Ljava/lang/StringBuilder;

    invoke-direct {v10}, Ljava/lang/StringBuilder;-><init>()V

    const-string v12, "Last entry afterwards: "

    invoke-virtual {v10, v12}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v12

    sget-object v10, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    sget-object v13, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v13}, Ljava/util/List;->size()I

    move-result v13

    add-int/lit8 v13, v13, -0x1

    invoke-interface {v10, v13}, Ljava/util/List;->get(I)Ljava/lang/Object;

    move-result-object v10

    check-cast v10, Ljava/lang/String;

    invoke-virtual {v12, v10}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v10

    invoke-virtual {v10}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v10

    invoke-virtual {v11, v10}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 148
    :try_start_116
    new-instance v10, Ljava/lang/StringBuilder;

    invoke-direct {v10}, Ljava/lang/StringBuilder;-><init>()V

    const-string v11, "data/data/"

    invoke-virtual {v10, v11}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v10

    invoke-virtual {v10, p0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v10

    invoke-virtual {v10}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    .line 149
    .local v2, "filePath":Ljava/lang/String;
    new-instance v4, Ljava/io/File;

    const-string v10, "info.txt"

    invoke-direct {v4, v2, v10}, Ljava/io/File;-><init>(Ljava/lang/String;Ljava/lang/String;)V

    .line 150
    .local v4, "info":Ljava/io/File;
    new-instance v9, Ljava/io/FileWriter;

    invoke-direct {v9, v4}, Ljava/io/FileWriter;-><init>(Ljava/io/File;)V

    .line 152
    .restart local v9    # "writer":Ljava/io/FileWriter;
    invoke-static {v7}, Ljava/lang/String;->valueOf(I)Ljava/lang/String;

    move-result-object v10

    invoke-virtual {v9, v10}, Ljava/io/FileWriter;->append(Ljava/lang/CharSequence;)Ljava/io/Writer;

    .line 153
    invoke-virtual {v9}, Ljava/io/FileWriter;->flush()V

    .line 154
    invoke-virtual {v9}, Ljava/io/FileWriter;->close()V
    :try_end_142
    .catch Ljava/io/IOException; {:try_start_116 .. :try_end_142} :catch_150

    .line 160
    .end local v2    # "filePath":Ljava/lang/String;
    .end local v4    # "info":Ljava/io/File;
    .end local v9    # "writer":Ljava/io/FileWriter;
    :goto_142
    return-void

    .line 136
    .end local v7    # "size":I
    :catch_143
    move-exception v1

    .line 137
    .local v1, "e":Ljava/io/IOException;
    sget-object v10, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v11, "Writing to external storage failed."

    invoke-virtual {v10, v11}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 138
    invoke-virtual {v1}, Ljava/io/IOException;->printStackTrace()V

    goto/16 :goto_af

    .line 156
    .end local v1    # "e":Ljava/io/IOException;
    .restart local v7    # "size":I
    :catch_150
    move-exception v1

    .line 157
    .restart local v1    # "e":Ljava/io/IOException;
    sget-object v10, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v11, "Writing to internal storage failed."

    invoke-virtual {v10, v11}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 158
    invoke-virtual {v1}, Ljava/io/IOException;->printStackTrace()V

    goto :goto_142
.end method


# virtual methods
.method public onReceive(Landroid/content/Context;Landroid/content/Intent;)V
    .registers 6
    .param p1, "context"    # Landroid/content/Context;
    .param p2, "intent"    # Landroid/content/Intent;

    .prologue
    .line 48
    sget-object v1, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v2, "Received Broadcast"

    invoke-virtual {v1, v2}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 50
    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;

    move-result-object v1

    if-eqz v1, :cond_27

    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;

    move-result-object v1

    const-string v2, "STORE_TRACES"

    invoke-virtual {v1, v2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v1

    if-eqz v1, :cond_27

    .line 51
    const-string v1, "packageName"

    invoke-virtual {p2, v1}, Landroid/content/Intent;->getStringExtra(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    .line 52
    .local v0, "packageName":Ljava/lang/String;
    invoke-static {v0}, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->write(Ljava/lang/String;)V

    .line 53
    sget-object v1, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v1}, Ljava/util/List;->clear()V

    .line 55
    .end local v0    # "packageName":Ljava/lang/String;
    :cond_27
    return-void
.end method
