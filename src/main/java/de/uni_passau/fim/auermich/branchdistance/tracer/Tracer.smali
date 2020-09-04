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
    .line 25
    new-instance v0, Ljava/util/ArrayList;

    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V

    invoke-static {v0}, Ljava/util/Collections;->synchronizedList(Ljava/util/List;)Ljava/util/List;

    move-result-object v0

    sput-object v0, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    .line 31
    const-class v0, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;

    .line 32
    invoke-virtual {v0}, Ljava/lang/Class;->getName()Ljava/lang/String;

    move-result-object v0

    .line 31
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

.method public static computeBranchDistance(II)V
    .registers 3
    .param p0, "operation"    # I
    .param p1, "argument"    # I

    .prologue
    .line 96
    const/4 v0, 0x0

    invoke-static {p0, p1, v0}, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->computeBranchDistance(III)V

    .line 97
    return-void
.end method

.method public static computeBranchDistance(III)V
    .registers 8
    .param p0, "operation"    # I
    .param p1, "argument1"    # I
    .param p2, "argument2"    # I

    .prologue
    .line 102
    const/4 v0, 0x0

    .line 104
    .local v0, "distance":I
    packed-switch p0, :pswitch_data_48

    .line 118
    new-instance v2, Ljava/lang/UnsupportedOperationException;

    const-string v3, "Comparison operator not yet supported!"

    invoke-direct {v2, v3}, Ljava/lang/UnsupportedOperationException;-><init>(Ljava/lang/String;)V

    throw v2

    .line 107
    :pswitch_c
    sub-int v2, p1, p2

    invoke-static {v2}, Ljava/lang/Math;->abs(I)I

    move-result v0

    .line 122
    :goto_12
    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "BRANCH DISTANCE: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v2

    invoke-virtual {v2, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v2

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    .line 123
    .local v1, "identifier":Ljava/lang/String;
    sget-object v2, Ljava/lang/System;->out:Ljava/io/PrintStream;

    new-instance v3, Ljava/lang/StringBuilder;

    invoke-direct {v3}, Ljava/lang/StringBuilder;-><init>()V

    const-string v4, "BRANCH_DISTANCE: "

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v3

    invoke-virtual {v3, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v3

    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    invoke-virtual {v2, v3}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 124
    invoke-static {v1}, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->trace(Ljava/lang/String;)V

    .line 125
    return-void

    .line 111
    .end local v1    # "identifier":Ljava/lang/String;
    :pswitch_41
    sub-int v0, p1, p2

    .line 112
    goto :goto_12

    .line 115
    :pswitch_44
    sub-int v0, p2, p1

    .line 116
    goto :goto_12

    .line 104
    nop

    :pswitch_data_48
    .packed-switch 0x0
        :pswitch_c
        :pswitch_c
        :pswitch_41
        :pswitch_41
        :pswitch_44
        :pswitch_44
    .end packed-switch
.end method

.method public static computeBranchDistance(ILjava/lang/Object;)V
    .registers 3
    .param p0, "operation"    # I
    .param p1, "argument"    # Ljava/lang/Object;

    .prologue
    .line 60
    const/4 v0, 0x0

    invoke-static {p0, p1, v0}, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->computeBranchDistance(ILjava/lang/Object;Ljava/lang/Object;)V

    .line 61
    return-void
.end method

.method public static computeBranchDistance(ILjava/lang/Object;Ljava/lang/Object;)V
    .registers 8
    .param p0, "operation"    # I
    .param p1, "argument1"    # Ljava/lang/Object;
    .param p2, "argument2"    # Ljava/lang/Object;

    .prologue
    .line 66
    const/4 v0, 0x0

    .line 68
    .local v0, "distance":I
    packed-switch p0, :pswitch_data_5e

    .line 85
    new-instance v2, Ljava/lang/UnsupportedOperationException;

    new-instance v3, Ljava/lang/StringBuilder;

    invoke-direct {v3}, Ljava/lang/StringBuilder;-><init>()V

    const-string v4, "Comparison operator "

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v3

    invoke-virtual {v3, p0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v3

    const-string v4, " not yet supported!"

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v3

    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    invoke-direct {v2, v3}, Ljava/lang/UnsupportedOperationException;-><init>(Ljava/lang/String;)V

    throw v2

    .line 70
    :pswitch_23
    if-ne p1, p2, :cond_55

    .line 71
    const/4 v0, 0x0

    .line 89
    :goto_26
    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "BRANCH DISTANCE: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v2

    invoke-virtual {v2, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v2

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    .line 90
    .local v1, "identifier":Ljava/lang/String;
    sget-object v2, Ljava/lang/System;->out:Ljava/io/PrintStream;

    new-instance v3, Ljava/lang/StringBuilder;

    invoke-direct {v3}, Ljava/lang/StringBuilder;-><init>()V

    const-string v4, "BRANCH_DISTANCE: "

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v3

    invoke-virtual {v3, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v3

    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    invoke-virtual {v2, v3}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 91
    invoke-static {v1}, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->trace(Ljava/lang/String;)V

    .line 92
    return-void

    .line 73
    .end local v1    # "identifier":Ljava/lang/String;
    :cond_55
    const/4 v0, 0x1

    .line 75
    goto :goto_26

    .line 77
    :pswitch_57
    if-eq p1, p2, :cond_5b

    .line 78
    const/4 v0, 0x0

    goto :goto_26

    .line 80
    :cond_5b
    const/4 v0, 0x1

    .line 82
    goto :goto_26

    .line 68
    nop

    :pswitch_data_5e
    .packed-switch 0x0
        :pswitch_23
        :pswitch_57
    .end packed-switch
.end method

.method private static getCurrentTimeStamp()Ljava/lang/String;
    .registers 2

    .prologue
    .line 43
    invoke-static {}, Ljava/time/LocalDateTime;->now()Ljava/time/LocalDateTime;

    move-result-object v0

    const-string v1, "yyyy-MM-dd HH:mm:ss.SSS"

    .line 44
    invoke-static {v1}, Ljava/time/format/DateTimeFormatter;->ofPattern(Ljava/lang/String;)Ljava/time/format/DateTimeFormatter;

    move-result-object v1

    invoke-virtual {v0, v1}, Ljava/time/LocalDateTime;->format(Ljava/time/format/DateTimeFormatter;)Ljava/lang/String;

    move-result-object v0

    .line 43
    return-object v0
.end method

.method public static trace(Ljava/lang/String;)V
    .registers 3
    .param p0, "identifier"    # Ljava/lang/String;

    .prologue
    .line 134
    sget-object v0, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v0, p0}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 136
    sget-object v0, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v0}, Ljava/util/List;->size()I

    move-result v0

    const/16 v1, 0x1388

    if-ne v0, v1, :cond_17

    .line 137
    invoke-static {}, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->write()V

    .line 138
    sget-object v0, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v0}, Ljava/util/List;->clear()V

    .line 140
    :cond_17
    return-void
.end method

.method private static write()V
    .registers 9

    .prologue
    .line 144
    invoke-static {}, Landroid/os/Environment;->getExternalStorageDirectory()Ljava/io/File;

    move-result-object v4

    .line 145
    .local v4, "sdCard":Ljava/io/File;
    new-instance v5, Ljava/io/File;

    const-string v7, "traces.txt"

    invoke-direct {v5, v4, v7}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V

    .line 149
    .local v5, "traces":Ljava/io/File;
    :try_start_b
    new-instance v6, Ljava/io/FileWriter;

    const/4 v7, 0x1

    invoke-direct {v6, v5, v7}, Ljava/io/FileWriter;-><init>(Ljava/io/File;Z)V

    .line 150
    .local v6, "writer":Ljava/io/FileWriter;
    new-instance v0, Ljava/io/BufferedWriter;

    invoke-direct {v0, v6}, Ljava/io/BufferedWriter;-><init>(Ljava/io/Writer;)V

    .line 152
    .local v0, "br":Ljava/io/BufferedWriter;
    const/4 v2, 0x0

    .local v2, "i":I
    :goto_17
    const/16 v7, 0x1388

    if-ge v2, v7, :cond_2c

    .line 153
    sget-object v7, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v7, v2}, Ljava/util/List;->get(I)Ljava/lang/Object;

    move-result-object v3

    check-cast v3, Ljava/lang/String;

    .line 154
    .local v3, "pathNode":Ljava/lang/String;
    invoke-virtual {v0, v3}, Ljava/io/BufferedWriter;->write(Ljava/lang/String;)V

    .line 155
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->newLine()V

    .line 152
    add-int/lit8 v2, v2, 0x1

    goto :goto_17

    .line 158
    .end local v3    # "pathNode":Ljava/lang/String;
    :cond_2c
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->flush()V

    .line 159
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->close()V

    .line 160
    invoke-virtual {v6}, Ljava/io/FileWriter;->close()V
    :try_end_35
    .catch Ljava/io/IOException; {:try_start_b .. :try_end_35} :catch_36

    .line 166
    .end local v0    # "br":Ljava/io/BufferedWriter;
    .end local v2    # "i":I
    .end local v6    # "writer":Ljava/io/FileWriter;
    :goto_35
    return-void

    .line 162
    :catch_36
    move-exception v1

    .line 163
    .local v1, "e":Ljava/io/IOException;
    sget-object v7, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v8, "Writing to external storage failed."

    invoke-virtual {v7, v8}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 164
    invoke-virtual {v1}, Ljava/io/IOException;->printStackTrace()V

    goto :goto_35
.end method

.method private static write(Ljava/lang/String;)V
    .registers 16
    .param p0, "packageName"    # Ljava/lang/String;

    .prologue
    const/4 v14, 0x0

    .line 178
    invoke-static {}, Landroid/os/Environment;->getExternalStorageDirectory()Ljava/io/File;

    move-result-object v6

    .line 179
    .local v6, "sdCard":Ljava/io/File;
    new-instance v8, Ljava/io/File;

    const-string v10, "traces.txt"

    invoke-direct {v8, v6, v10}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V

    .line 181
    .local v8, "traces":Ljava/io/File;
    sget-object v10, Ljava/lang/System;->out:Ljava/io/PrintStream;

    new-instance v11, Ljava/lang/StringBuilder;

    invoke-direct {v11}, Ljava/lang/StringBuilder;-><init>()V

    const-string v12, "Size: "

    invoke-virtual {v11, v12}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v11

    sget-object v12, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v12}, Ljava/util/List;->size()I

    move-result v12

    invoke-virtual {v11, v12}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v11

    invoke-virtual {v11}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v11

    invoke-virtual {v10, v11}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 183
    sget-object v10, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v10}, Ljava/util/List;->isEmpty()Z

    move-result v10

    if-nez v10, :cond_7a

    .line 184
    sget-object v11, Ljava/lang/System;->out:Ljava/io/PrintStream;

    new-instance v10, Ljava/lang/StringBuilder;

    invoke-direct {v10}, Ljava/lang/StringBuilder;-><init>()V

    const-string v12, "First entry: "

    invoke-virtual {v10, v12}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v12

    sget-object v10, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v10, v14}, Ljava/util/List;->get(I)Ljava/lang/Object;

    move-result-object v10

    check-cast v10, Ljava/lang/String;

    invoke-virtual {v12, v10}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v10

    invoke-virtual {v10}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v10

    invoke-virtual {v11, v10}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 185
    sget-object v11, Ljava/lang/System;->out:Ljava/io/PrintStream;

    new-instance v10, Ljava/lang/StringBuilder;

    invoke-direct {v10}, Ljava/lang/StringBuilder;-><init>()V

    const-string v12, "Last entry: "

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

    .line 191
    :cond_7a
    :try_start_7a
    new-instance v9, Ljava/io/FileWriter;

    const/4 v10, 0x1

    invoke-direct {v9, v8, v10}, Ljava/io/FileWriter;-><init>(Ljava/io/File;Z)V

    .line 192
    .local v9, "writer":Ljava/io/FileWriter;
    new-instance v0, Ljava/io/BufferedWriter;

    invoke-direct {v0, v9}, Ljava/io/BufferedWriter;-><init>(Ljava/io/Writer;)V

    .line 194
    .local v0, "br":Ljava/io/BufferedWriter;
    const/4 v3, 0x0

    .local v3, "i":I
    :goto_86
    sget-object v10, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v10}, Ljava/util/List;->size()I

    move-result v10

    if-ge v3, v10, :cond_9f

    .line 195
    sget-object v10, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v10, v3}, Ljava/util/List;->get(I)Ljava/lang/Object;

    move-result-object v5

    check-cast v5, Ljava/lang/String;

    .line 196
    .local v5, "pathNode":Ljava/lang/String;
    invoke-virtual {v0, v5}, Ljava/io/BufferedWriter;->write(Ljava/lang/String;)V

    .line 197
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->newLine()V

    .line 194
    add-int/lit8 v3, v3, 0x1

    goto :goto_86

    .line 200
    .end local v5    # "pathNode":Ljava/lang/String;
    :cond_9f
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->flush()V

    .line 201
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->close()V

    .line 202
    invoke-virtual {v9}, Ljava/io/FileWriter;->close()V
    :try_end_a8
    .catch Ljava/io/IOException; {:try_start_7a .. :try_end_a8} :catch_13b

    .line 210
    .end local v0    # "br":Ljava/io/BufferedWriter;
    .end local v3    # "i":I
    .end local v9    # "writer":Ljava/io/FileWriter;
    :goto_a8
    sget-object v10, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v10}, Ljava/util/List;->size()I

    move-result v7

    .line 211
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

    .line 212
    sget-object v11, Ljava/lang/System;->out:Ljava/io/PrintStream;

    new-instance v10, Ljava/lang/StringBuilder;

    invoke-direct {v10}, Ljava/lang/StringBuilder;-><init>()V

    const-string v12, "First entry afterwards: "

    invoke-virtual {v10, v12}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v12

    sget-object v10, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v10, v14}, Ljava/util/List;->get(I)Ljava/lang/Object;

    move-result-object v10

    check-cast v10, Ljava/lang/String;

    invoke-virtual {v12, v10}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v10

    invoke-virtual {v10}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v10

    invoke-virtual {v11, v10}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 213
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

    .line 217
    :try_start_10e
    new-instance v10, Ljava/lang/StringBuilder;

    invoke-direct {v10}, Ljava/lang/StringBuilder;-><init>()V

    const-string v11, "data/data/"

    invoke-virtual {v10, v11}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v10

    invoke-virtual {v10, p0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v10

    invoke-virtual {v10}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    .line 218
    .local v2, "filePath":Ljava/lang/String;
    new-instance v4, Ljava/io/File;

    const-string v10, "info.txt"

    invoke-direct {v4, v2, v10}, Ljava/io/File;-><init>(Ljava/lang/String;Ljava/lang/String;)V

    .line 219
    .local v4, "info":Ljava/io/File;
    new-instance v9, Ljava/io/FileWriter;

    invoke-direct {v9, v4}, Ljava/io/FileWriter;-><init>(Ljava/io/File;)V

    .line 221
    .restart local v9    # "writer":Ljava/io/FileWriter;
    invoke-static {v7}, Ljava/lang/String;->valueOf(I)Ljava/lang/String;

    move-result-object v10

    invoke-virtual {v9, v10}, Ljava/io/FileWriter;->append(Ljava/lang/CharSequence;)Ljava/io/Writer;

    .line 222
    invoke-virtual {v9}, Ljava/io/FileWriter;->flush()V

    .line 223
    invoke-virtual {v9}, Ljava/io/FileWriter;->close()V
    :try_end_13a
    .catch Ljava/io/IOException; {:try_start_10e .. :try_end_13a} :catch_148

    .line 229
    .end local v2    # "filePath":Ljava/lang/String;
    .end local v4    # "info":Ljava/io/File;
    .end local v9    # "writer":Ljava/io/FileWriter;
    :goto_13a
    return-void

    .line 204
    .end local v7    # "size":I
    :catch_13b
    move-exception v1

    .line 205
    .local v1, "e":Ljava/io/IOException;
    sget-object v10, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v11, "Writing to external storage failed."

    invoke-virtual {v10, v11}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 206
    invoke-virtual {v1}, Ljava/io/IOException;->printStackTrace()V

    goto/16 :goto_a8

    .line 225
    .end local v1    # "e":Ljava/io/IOException;
    .restart local v7    # "size":I
    :catch_148
    move-exception v1

    .line 226
    .restart local v1    # "e":Ljava/io/IOException;
    sget-object v10, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v11, "Writing to internal storage failed."

    invoke-virtual {v10, v11}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 227
    invoke-virtual {v1}, Ljava/io/IOException;->printStackTrace()V

    goto :goto_13a
.end method


# virtual methods
.method public onReceive(Landroid/content/Context;Landroid/content/Intent;)V
    .registers 6
    .param p1, "context"    # Landroid/content/Context;
    .param p2, "intent"    # Landroid/content/Intent;

    .prologue
    .line 49
    sget-object v1, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v2, "Received Broadcast"

    invoke-virtual {v1, v2}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 51
    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;

    move-result-object v1

    if-eqz v1, :cond_27

    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;

    move-result-object v1

    const-string v2, "STORE_TRACES"

    invoke-virtual {v1, v2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v1

    if-eqz v1, :cond_27

    .line 52
    const-string v1, "packageName"

    invoke-virtual {p2, v1}, Landroid/content/Intent;->getStringExtra(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    .line 53
    .local v0, "packageName":Ljava/lang/String;
    invoke-static {v0}, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->write(Ljava/lang/String;)V

    .line 54
    sget-object v1, Lde/uni_passau/fim/auermich/branchdistance/tracer/Tracer;->executionPath:Ljava/util/List;

    invoke-interface {v1}, Ljava/util/List;->clear()V

    .line 56
    .end local v0    # "packageName":Ljava/lang/String;
    :cond_27
    return-void
.end method
