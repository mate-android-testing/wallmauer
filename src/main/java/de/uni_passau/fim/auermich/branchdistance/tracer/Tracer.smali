.class public Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;
.super Landroid/content/BroadcastReceiver;
.source "Tracer.java"


# static fields
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
    .line 23
    new-instance v0, Ljava/util/LinkedList;

    invoke-direct {v0}, Ljava/util/LinkedList;-><init>()V

    sput-object v0, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    .line 29
    const-class v0, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;

    .line 30
    invoke-virtual {v0}, Ljava/lang/Class;->getName()Ljava/lang/String;

    move-result-object v0

    .line 29
    invoke-static {v0}, Ljava/util/logging/Logger;->getLogger(Ljava/lang/String;)Ljava/util/logging/Logger;

    move-result-object v0

    sput-object v0, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    return-void
.end method

.method public constructor <init>()V
    .registers 1

    .prologue
    .line 20
    invoke-direct {p0}, Landroid/content/BroadcastReceiver;-><init>()V

    return-void
.end method

.method private static getCurrentTimeStamp()Ljava/lang/String;
    .registers 2

    .prologue
    .line 39
    invoke-static {}, Ljava/time/LocalDateTime;->now()Ljava/time/LocalDateTime;

    move-result-object v0

    const-string v1, "yyyy-MM-dd HH:mm:ss.SSS"

    .line 40
    invoke-static {v1}, Ljava/time/format/DateTimeFormatter;->ofPattern(Ljava/lang/String;)Ljava/time/format/DateTimeFormatter;

    move-result-object v1

    invoke-virtual {v0, v1}, Ljava/time/LocalDateTime;->format(Ljava/time/format/DateTimeFormatter;)Ljava/lang/String;

    move-result-object v0

    .line 39
    return-object v0
.end method

.method public static trace(Ljava/lang/String;)V
    .registers 2
    .param p0, "identifier"    # Ljava/lang/String;

    .prologue
    .line 60
    sget-object v0, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v0, p0}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 61
    return-void
.end method

.method private static write(Ljava/lang/String;)V
    .registers 16
    .param p0, "packageName"    # Ljava/lang/String;

    .prologue
    .line 76
    invoke-static {}, Landroid/os/Environment;->getExternalStorageDirectory()Ljava/io/File;

    move-result-object v7

    .line 79
    .local v7, "sdCard":Ljava/io/File;
    new-instance v0, Ljava/io/File;

    new-instance v10, Ljava/lang/StringBuilder;

    invoke-direct {v10}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {v10, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    move-result-object v10

    sget-object v11, Ljava/io/File;->separator:Ljava/lang/String;

    invoke-virtual {v10, v11}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v10

    invoke-virtual {v10, p0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v10

    invoke-virtual {v10}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v10

    invoke-direct {v0, v10}, Ljava/io/File;-><init>(Ljava/lang/String;)V

    .line 80
    .local v0, "dir":Ljava/io/File;
    invoke-virtual {v0}, Ljava/io/File;->mkdirs()Z

    .line 82
    const-class v11, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;

    monitor-enter v11

    .line 83
    :try_start_26
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

    .line 85
    sget-object v10, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    invoke-virtual {v0}, Ljava/io/File;->getAbsolutePath()Ljava/lang/String;

    move-result-object v12

    invoke-virtual {v10, v12}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 87
    sget-object v10, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v10}, Ljava/util/List;->isEmpty()Z

    move-result v10

    if-nez v10, :cond_9e

    .line 88
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

    .line 89
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

    .line 91
    :cond_9e
    monitor-exit v11
    :try_end_9f
    .catchall {:try_start_26 .. :try_end_9f} :catchall_d5

    .line 93
    new-instance v2, Ljava/io/File;

    const-string v10, "traces.txt"

    invoke-direct {v2, v0, v10}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V

    .line 97
    .local v2, "file":Ljava/io/File;
    :try_start_a6
    new-instance v9, Ljava/io/FileWriter;

    invoke-direct {v9, v2}, Ljava/io/FileWriter;-><init>(Ljava/io/File;)V

    .line 100
    .local v9, "writer":Ljava/io/FileWriter;
    const-string v10, "NEW TRACE"

    invoke-virtual {v9, v10}, Ljava/io/FileWriter;->append(Ljava/lang/CharSequence;)Ljava/io/Writer;

    .line 101
    invoke-static {}, Ljava/lang/System;->lineSeparator()Ljava/lang/String;

    move-result-object v10

    invoke-virtual {v9, v10}, Ljava/io/FileWriter;->append(Ljava/lang/CharSequence;)Ljava/io/Writer;

    .line 104
    const/4 v4, 0x0

    .local v4, "i":I
    :goto_b8
    sget-object v10, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v10}, Ljava/util/List;->size()I

    move-result v10

    if-ge v4, v10, :cond_d8

    .line 106
    sget-object v10, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v10, v4}, Ljava/util/List;->get(I)Ljava/lang/Object;

    move-result-object v6

    check-cast v6, Ljava/lang/String;

    .line 107
    .local v6, "pathNode":Ljava/lang/String;
    invoke-virtual {v9, v6}, Ljava/io/FileWriter;->append(Ljava/lang/CharSequence;)Ljava/io/Writer;

    .line 108
    invoke-static {}, Ljava/lang/System;->lineSeparator()Ljava/lang/String;

    move-result-object v10

    invoke-virtual {v9, v10}, Ljava/io/FileWriter;->append(Ljava/lang/CharSequence;)Ljava/io/Writer;
    :try_end_d2
    .catch Ljava/io/IOException; {:try_start_a6 .. :try_end_d2} :catch_177

    .line 104
    add-int/lit8 v4, v4, 0x1

    goto :goto_b8

    .line 91
    .end local v2    # "file":Ljava/io/File;
    .end local v4    # "i":I
    .end local v6    # "pathNode":Ljava/lang/String;
    .end local v9    # "writer":Ljava/io/FileWriter;
    :catchall_d5
    move-exception v10

    :try_start_d6
    monitor-exit v11
    :try_end_d7
    .catchall {:try_start_d6 .. :try_end_d7} :catchall_d5

    throw v10

    .line 115
    .restart local v2    # "file":Ljava/io/File;
    .restart local v4    # "i":I
    .restart local v9    # "writer":Ljava/io/FileWriter;
    :cond_d8
    :try_start_d8
    invoke-virtual {v9}, Ljava/io/FileWriter;->flush()V

    .line 116
    invoke-virtual {v9}, Ljava/io/FileWriter;->close()V
    :try_end_de
    .catch Ljava/io/IOException; {:try_start_d8 .. :try_end_de} :catch_177

    .line 123
    .end local v4    # "i":I
    .end local v9    # "writer":Ljava/io/FileWriter;
    :goto_de
    sget-object v10, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v10}, Ljava/util/List;->size()I

    move-result v8

    .line 124
    .local v8, "size":I
    sget-object v10, Ljava/lang/System;->out:Ljava/io/PrintStream;

    new-instance v11, Ljava/lang/StringBuilder;

    invoke-direct {v11}, Ljava/lang/StringBuilder;-><init>()V

    const-string v12, "Size: "

    invoke-virtual {v11, v12}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v11

    invoke-virtual {v11, v8}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v11

    invoke-virtual {v11}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v11

    invoke-virtual {v10, v11}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 125
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

    .line 126
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

    .line 127
    sget-object v10, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v10}, Ljava/util/List;->clear()V

    .line 131
    :try_start_14a
    new-instance v10, Ljava/lang/StringBuilder;

    invoke-direct {v10}, Ljava/lang/StringBuilder;-><init>()V

    const-string v11, "data/data/"

    invoke-virtual {v10, v11}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v10

    invoke-virtual {v10, p0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v10

    invoke-virtual {v10}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    .line 132
    .local v3, "filePath":Ljava/lang/String;
    new-instance v5, Ljava/io/File;

    const-string v10, "info.txt"

    invoke-direct {v5, v3, v10}, Ljava/io/File;-><init>(Ljava/lang/String;Ljava/lang/String;)V

    .line 133
    .local v5, "info":Ljava/io/File;
    new-instance v9, Ljava/io/FileWriter;

    invoke-direct {v9, v5}, Ljava/io/FileWriter;-><init>(Ljava/io/File;)V

    .line 135
    .restart local v9    # "writer":Ljava/io/FileWriter;
    invoke-static {v8}, Ljava/lang/String;->valueOf(I)Ljava/lang/String;

    move-result-object v10

    invoke-virtual {v9, v10}, Ljava/io/FileWriter;->append(Ljava/lang/CharSequence;)Ljava/io/Writer;

    .line 136
    invoke-virtual {v9}, Ljava/io/FileWriter;->flush()V

    .line 137
    invoke-virtual {v9}, Ljava/io/FileWriter;->close()V
    :try_end_176
    .catch Ljava/io/IOException; {:try_start_14a .. :try_end_176} :catch_184

    .line 143
    .end local v3    # "filePath":Ljava/lang/String;
    .end local v5    # "info":Ljava/io/File;
    .end local v9    # "writer":Ljava/io/FileWriter;
    :goto_176
    return-void

    .line 118
    .end local v8    # "size":I
    :catch_177
    move-exception v1

    .line 119
    .local v1, "e":Ljava/io/IOException;
    sget-object v10, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v11, "Writing to external storage failed."

    invoke-virtual {v10, v11}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 120
    invoke-virtual {v1}, Ljava/io/IOException;->printStackTrace()V

    goto/16 :goto_de

    .line 139
    .end local v1    # "e":Ljava/io/IOException;
    .restart local v8    # "size":I
    :catch_184
    move-exception v1

    .line 140
    .restart local v1    # "e":Ljava/io/IOException;
    sget-object v10, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v11, "Writing to internal storage failed."

    invoke-virtual {v10, v11}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 141
    invoke-virtual {v1}, Ljava/io/IOException;->printStackTrace()V

    goto :goto_176
.end method


# virtual methods
.method public onReceive(Landroid/content/Context;Landroid/content/Intent;)V
    .registers 6
    .param p1, "context"    # Landroid/content/Context;
    .param p2, "intent"    # Landroid/content/Intent;

    .prologue
    .line 45
    sget-object v1, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v2, "Received Broadcast"

    invoke-virtual {v1, v2}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 47
    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;

    move-result-object v1

    if-eqz v1, :cond_22

    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;

    move-result-object v1

    const-string v2, "STORE_TRACES"

    invoke-virtual {v1, v2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v1

    if-eqz v1, :cond_22

    .line 48
    const-string v1, "packageName"

    invoke-virtual {p2, v1}, Landroid/content/Intent;->getStringExtra(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    .line 49
    .local v0, "packageName":Ljava/lang/String;
    invoke-static {v0}, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->write(Ljava/lang/String;)V

    .line 51
    .end local v0    # "packageName":Ljava/lang/String;
    :cond_22
    return-void
.end method
