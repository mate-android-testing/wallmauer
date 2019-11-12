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
    .registers 9
    .param p0, "packageName"    # Ljava/lang/String;

    .prologue
    .line 73
    sget-object v6, Landroid/os/Environment;->DIRECTORY_DOWNLOADS:Ljava/lang/String;

    invoke-static {v6}, Landroid/os/Environment;->getExternalStoragePublicDirectory(Ljava/lang/String;)Ljava/io/File;

    move-result-object v1

    .line 77
    .local v1, "downloadDir":Ljava/io/File;
    new-instance v0, Ljava/io/File;

    new-instance v6, Ljava/lang/StringBuilder;

    invoke-direct {v6}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {v6, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    move-result-object v6

    sget-object v7, Ljava/io/File;->separator:Ljava/lang/String;

    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v6

    invoke-virtual {v6, p0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v6

    invoke-virtual {v6}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v6

    invoke-direct {v0, v6}, Ljava/io/File;-><init>(Ljava/lang/String;)V

    .line 78
    .local v0, "dir":Ljava/io/File;
    invoke-virtual {v0}, Ljava/io/File;->mkdirs()Z

    .line 80
    sget-object v6, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    invoke-virtual {v0}, Ljava/io/File;->getAbsolutePath()Ljava/lang/String;

    move-result-object v7

    invoke-virtual {v6, v7}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 82
    new-instance v3, Ljava/io/File;

    const-string v6, "traces.txt"

    invoke-direct {v3, v0, v6}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V

    .line 86
    .local v3, "file":Ljava/io/File;
    :try_start_35
    new-instance v5, Ljava/io/FileWriter;

    invoke-direct {v5, v3}, Ljava/io/FileWriter;-><init>(Ljava/io/File;)V

    .line 89
    .local v5, "writer":Ljava/io/FileWriter;
    const-string v6, "NEW TRACE"

    invoke-virtual {v5, v6}, Ljava/io/FileWriter;->append(Ljava/lang/CharSequence;)Ljava/io/Writer;

    .line 90
    invoke-static {}, Ljava/lang/System;->lineSeparator()Ljava/lang/String;

    move-result-object v6

    invoke-virtual {v5, v6}, Ljava/io/FileWriter;->append(Ljava/lang/CharSequence;)Ljava/io/Writer;

    .line 92
    sget-object v6, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v6}, Ljava/util/List;->iterator()Ljava/util/Iterator;

    move-result-object v6

    :goto_4c
    invoke-interface {v6}, Ljava/util/Iterator;->hasNext()Z

    move-result v7

    if-eqz v7, :cond_6f

    invoke-interface {v6}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v4

    check-cast v4, Ljava/lang/String;

    .line 93
    .local v4, "pathNode":Ljava/lang/String;
    invoke-virtual {v5, v4}, Ljava/io/FileWriter;->append(Ljava/lang/CharSequence;)Ljava/io/Writer;

    .line 94
    invoke-static {}, Ljava/lang/System;->lineSeparator()Ljava/lang/String;

    move-result-object v7

    invoke-virtual {v5, v7}, Ljava/io/FileWriter;->append(Ljava/lang/CharSequence;)Ljava/io/Writer;
    :try_end_62
    .catch Ljava/io/IOException; {:try_start_35 .. :try_end_62} :catch_63

    goto :goto_4c

    .line 103
    .end local v4    # "pathNode":Ljava/lang/String;
    .end local v5    # "writer":Ljava/io/FileWriter;
    :catch_63
    move-exception v2

    .line 104
    .local v2, "e":Ljava/io/IOException;
    sget-object v6, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v7, "Writing to internal storage failed."

    invoke-virtual {v6, v7}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 105
    invoke-virtual {v2}, Ljava/io/IOException;->printStackTrace()V

    .line 107
    .end local v2    # "e":Ljava/io/IOException;
    :goto_6e
    return-void

    .line 98
    .restart local v5    # "writer":Ljava/io/FileWriter;
    :cond_6f
    :try_start_6f
    sget-object v6, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v6}, Ljava/util/List;->clear()V

    .line 100
    invoke-virtual {v5}, Ljava/io/FileWriter;->flush()V

    .line 101
    invoke-virtual {v5}, Ljava/io/FileWriter;->close()V
    :try_end_7a
    .catch Ljava/io/IOException; {:try_start_6f .. :try_end_7a} :catch_63

    goto :goto_6e
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
