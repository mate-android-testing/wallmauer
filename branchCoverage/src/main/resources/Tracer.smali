.class public Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;
.super Ljava/lang/Object;
.source "Tracer.java"


# static fields
.field private static final LOGGER:Ljava/util/logging/Logger;

.field private static final TRACES_FILE:Ljava/lang/String; = "traces.txt"

.field private static executionPaths:Ljava/util/Map;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/Map",
            "<",
            "Ljava/lang/String;",
            "Ljava/lang/String;",
            ">;"
        }
    .end annotation
.end field

.field private static visitedBranches:Ljava/util/Set;
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
    .line 18
    new-instance v0, Ljava/util/LinkedHashMap;

    invoke-direct {v0}, Ljava/util/LinkedHashMap;-><init>()V

    sput-object v0, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;->executionPaths:Ljava/util/Map;

    .line 21
    new-instance v0, Ljava/util/HashSet;

    invoke-direct {v0}, Ljava/util/HashSet;-><init>()V

    sput-object v0, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;->visitedBranches:Ljava/util/Set;

    .line 27
    const-class v0, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;

    .line 28
    invoke-virtual {v0}, Ljava/lang/Class;->getName()Ljava/lang/String;

    move-result-object v0

    .line 27
    invoke-static {v0}, Ljava/util/logging/Logger;->getLogger(Ljava/lang/String;)Ljava/util/logging/Logger;

    move-result-object v0

    sput-object v0, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    return-void
.end method

.method public constructor <init>()V
    .registers 1

    .prologue
    .line 15
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method private static getCurrentTimeStamp()Ljava/lang/String;
    .registers 2

    .prologue
    .line 36
    invoke-static {}, Ljava/time/LocalDateTime;->now()Ljava/time/LocalDateTime;

    move-result-object v0

    const-string v1, "yyyy-MM-dd HH:mm:ss.SSS"

    .line 37
    invoke-static {v1}, Ljava/time/format/DateTimeFormatter;->ofPattern(Ljava/lang/String;)Ljava/time/format/DateTimeFormatter;

    move-result-object v1

    invoke-virtual {v0, v1}, Ljava/time/LocalDateTime;->format(Ljava/time/format/DateTimeFormatter;)Ljava/lang/String;

    move-result-object v0

    .line 36
    return-object v0
.end method

.method public static trace(Ljava/lang/String;)V
    .registers 3
    .param p0, "identifier"    # Ljava/lang/String;

    .prologue
    .line 48
    sget-object v0, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;->executionPaths:Ljava/util/Map;

    invoke-static {}, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;->getCurrentTimeStamp()Ljava/lang/String;

    move-result-object v1

    invoke-interface {v0, v1, p0}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    .line 49
    return-void
.end method

.method public static write(Ljava/lang/String;)V
    .registers 9
    .param p0, "packageName"    # Ljava/lang/String;

    .prologue
    .line 61
    new-instance v5, Ljava/lang/StringBuilder;

    invoke-direct {v5}, Ljava/lang/StringBuilder;-><init>()V

    const-string v6, "data/data/"

    invoke-virtual {v5, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v5

    invoke-virtual {v5, p0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v5

    invoke-virtual {v5}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    .line 63
    .local v3, "filePath":Ljava/lang/String;
    sget-object v5, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    invoke-virtual {v5, v3}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 65
    new-instance v2, Ljava/io/File;

    const-string v5, "traces.txt"

    invoke-direct {v2, v3, v5}, Ljava/io/File;-><init>(Ljava/lang/String;Ljava/lang/String;)V

    .line 69
    .local v2, "file":Ljava/io/File;
    :try_start_1f
    new-instance v4, Ljava/io/FileWriter;

    invoke-direct {v4, v2}, Ljava/io/FileWriter;-><init>(Ljava/io/File;)V

    .line 72
    .local v4, "writer":Ljava/io/FileWriter;
    new-instance v5, Ljava/lang/StringBuilder;

    invoke-direct {v5}, Ljava/lang/StringBuilder;-><init>()V

    invoke-static {}, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;->getCurrentTimeStamp()Ljava/lang/String;

    move-result-object v6

    invoke-virtual {v5, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v5

    const-string v6, ": NEW TRACE"

    invoke-virtual {v5, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v5

    invoke-virtual {v5}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v5

    invoke-virtual {v4, v5}, Ljava/io/FileWriter;->append(Ljava/lang/CharSequence;)Ljava/io/Writer;

    .line 73
    invoke-static {}, Ljava/lang/System;->lineSeparator()Ljava/lang/String;

    move-result-object v5

    invoke-virtual {v4, v5}, Ljava/io/FileWriter;->append(Ljava/lang/CharSequence;)Ljava/io/Writer;

    .line 75
    sget-object v5, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;->executionPaths:Ljava/util/Map;

    invoke-interface {v5}, Ljava/util/Map;->entrySet()Ljava/util/Set;

    move-result-object v5

    invoke-interface {v5}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v6

    :goto_4f
    invoke-interface {v6}, Ljava/util/Iterator;->hasNext()Z

    move-result v5

    if-eqz v5, :cond_95

    invoke-interface {v6}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v1

    check-cast v1, Ljava/util/Map$Entry;

    .line 76
    .local v1, "entry":Ljava/util/Map$Entry;, "Ljava/util/Map$Entry<Ljava/lang/String;Ljava/lang/String;>;"
    new-instance v7, Ljava/lang/StringBuilder;

    invoke-direct {v7}, Ljava/lang/StringBuilder;-><init>()V

    invoke-interface {v1}, Ljava/util/Map$Entry;->getKey()Ljava/lang/Object;

    move-result-object v5

    check-cast v5, Ljava/lang/String;

    invoke-virtual {v7, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v5

    const-string v7, ": "

    invoke-virtual {v5, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v7

    invoke-interface {v1}, Ljava/util/Map$Entry;->getValue()Ljava/lang/Object;

    move-result-object v5

    check-cast v5, Ljava/lang/String;

    invoke-virtual {v7, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v5

    invoke-virtual {v5}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v5

    invoke-virtual {v4, v5}, Ljava/io/FileWriter;->append(Ljava/lang/CharSequence;)Ljava/io/Writer;

    .line 77
    invoke-static {}, Ljava/lang/System;->lineSeparator()Ljava/lang/String;

    move-result-object v5

    invoke-virtual {v4, v5}, Ljava/io/FileWriter;->append(Ljava/lang/CharSequence;)Ljava/io/Writer;
    :try_end_88
    .catch Ljava/io/IOException; {:try_start_1f .. :try_end_88} :catch_89

    goto :goto_4f

    .line 94
    .end local v1    # "entry":Ljava/util/Map$Entry;, "Ljava/util/Map$Entry<Ljava/lang/String;Ljava/lang/String;>;"
    .end local v4    # "writer":Ljava/io/FileWriter;
    :catch_89
    move-exception v0

    .line 95
    .local v0, "e":Ljava/io/IOException;
    sget-object v5, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v6, "Writing to internal storage failed."

    invoke-virtual {v5, v6}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 96
    invoke-virtual {v0}, Ljava/io/IOException;->printStackTrace()V

    .line 98
    .end local v0    # "e":Ljava/io/IOException;
    :goto_94
    return-void

    .line 89
    .restart local v4    # "writer":Ljava/io/FileWriter;
    :cond_95
    :try_start_95
    sget-object v5, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;->executionPaths:Ljava/util/Map;

    invoke-interface {v5}, Ljava/util/Map;->clear()V

    .line 91
    invoke-virtual {v4}, Ljava/io/FileWriter;->flush()V

    .line 92
    invoke-virtual {v4}, Ljava/io/FileWriter;->close()V
    :try_end_a0
    .catch Ljava/io/IOException; {:try_start_95 .. :try_end_a0} :catch_89

    goto :goto_94
.end method
