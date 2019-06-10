.class public Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;
.super Ljava/lang/Object;
.source "Tracer.java"


# static fields
.field private static final TAG:Ljava/lang/String; = "TRACER"

.field private static final TRACES_FILE:Ljava/lang/String; = "traces.txt"

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
    .line 12
    new-instance v0, Ljava/util/HashSet;

    invoke-direct {v0}, Ljava/util/HashSet;-><init>()V

    sput-object v0, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;->visitedBranches:Ljava/util/Set;

    return-void
.end method

.method public constructor <init>()V
    .registers 1

    .prologue
    .line 9
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method public static trace(Ljava/lang/String;)V
    .registers 2
    .param p0, "identifier"    # Ljava/lang/String;

    .prologue
    .line 30
    sget-object v0, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;->visitedBranches:Ljava/util/Set;

    invoke-interface {v0, p0}, Ljava/util/Set;->add(Ljava/lang/Object;)Z

    .line 31
    return-void
.end method

.method public static write(Ljava/lang/String;)V
    .registers 8
    .param p0, "packageName"    # Ljava/lang/String;

    .prologue
    .line 36
    new-instance v5, Ljava/lang/StringBuilder;

    invoke-direct {v5}, Ljava/lang/StringBuilder;-><init>()V

    const-string v6, "data/data/"

    invoke-virtual {v5, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v5

    invoke-virtual {v5, p0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v5

    invoke-virtual {v5}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    .line 38
    .local v3, "filePath":Ljava/lang/String;
    sget-object v5, Ljava/lang/System;->out:Ljava/io/PrintStream;

    invoke-virtual {v5, v3}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 40
    new-instance v2, Ljava/io/File;

    const-string v5, "traces.txt"

    invoke-direct {v2, v3, v5}, Ljava/io/File;-><init>(Ljava/lang/String;Ljava/lang/String;)V

    .line 44
    .local v2, "file":Ljava/io/File;
    :try_start_1f
    new-instance v4, Ljava/io/FileWriter;

    invoke-direct {v4, v2}, Ljava/io/FileWriter;-><init>(Ljava/io/File;)V

    .line 46
    .local v4, "writer":Ljava/io/FileWriter;
    sget-object v5, Lde/uni_passau/fim/auermich/branchcoverage/tracer/Tracer;->visitedBranches:Ljava/util/Set;

    invoke-interface {v5}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v5

    :goto_2a
    invoke-interface {v5}, Ljava/util/Iterator;->hasNext()Z

    move-result v6

    if-eqz v6, :cond_4b

    invoke-interface {v5}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v0

    check-cast v0, Ljava/lang/String;

    .line 47
    .local v0, "branch":Ljava/lang/String;
    invoke-virtual {v4, v0}, Ljava/io/FileWriter;->append(Ljava/lang/CharSequence;)Ljava/io/Writer;

    .line 48
    const-string v6, "\n"

    invoke-virtual {v4, v6}, Ljava/io/FileWriter;->append(Ljava/lang/CharSequence;)Ljava/io/Writer;
    :try_end_3e
    .catch Ljava/io/IOException; {:try_start_1f .. :try_end_3e} :catch_3f

    goto :goto_2a

    .line 54
    .end local v0    # "branch":Ljava/lang/String;
    .end local v4    # "writer":Ljava/io/FileWriter;
    :catch_3f
    move-exception v1

    .line 55
    .local v1, "e":Ljava/io/IOException;
    sget-object v5, Ljava/lang/System;->out:Ljava/io/PrintStream;

    const-string v6, "Writing to internal storage failed."

    invoke-virtual {v5, v6}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 56
    invoke-virtual {v1}, Ljava/io/IOException;->printStackTrace()V

    .line 58
    .end local v1    # "e":Ljava/io/IOException;
    :goto_4a
    return-void

    .line 51
    .restart local v4    # "writer":Ljava/io/FileWriter;
    :cond_4b
    :try_start_4b
    invoke-virtual {v4}, Ljava/io/FileWriter;->flush()V

    .line 52
    invoke-virtual {v4}, Ljava/io/FileWriter;->close()V
    :try_end_51
    .catch Ljava/io/IOException; {:try_start_4b .. :try_end_51} :catch_3f

    goto :goto_4a
.end method
