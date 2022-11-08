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

.method public static computeBranchDistance(Ljava/lang/String;I)V
    .registers 3
    .param p0, "operation"    # Ljava/lang/String;
    .param p1, "argument"    # I

    .prologue
    .line 367
    const/4 v0, 0x0

    invoke-static {p0, p1, v0}, Lde/uni_passau/fim/auermich/tracer/Tracer;->computeBranchDistance(Ljava/lang/String;II)V

    .line 368
    return-void
.end method

.method public static computeBranchDistance(Ljava/lang/String;II)V
    .registers 12
    .param p0, "operation"    # Ljava/lang/String;
    .param p1, "argument1"    # I
    .param p2, "argument2"    # I

    .prologue
    .line 373
    const/4 v1, 0x0

    .line 374
    .local v1, "distanceThenBranch":I
    const/4 v0, 0x0

    .line 376
    .local v0, "distanceElseBranch":I
    const-string v7, ":"

    invoke-virtual {p0, v7}, Ljava/lang/String;->split(Ljava/lang/String;)[Ljava/lang/String;

    move-result-object v4

    .line 377
    .local v4, "tokens":[Ljava/lang/String;
    const/4 v7, 0x0

    aget-object v7, v4, v7

    invoke-static {v7}, Ljava/lang/Integer;->parseInt(Ljava/lang/String;)I

    move-result v3

    .line 378
    .local v3, "opcode":I
    const/4 v7, 0x1

    aget-object v2, v4, v7

    .line 386
    .local v2, "identifier":Ljava/lang/String;
    packed-switch v3, :pswitch_data_be

    .line 442
    new-instance v7, Ljava/lang/UnsupportedOperationException;

    const-string v8, "Comparison operator not yet supported!"

    invoke-direct {v7, v8}, Ljava/lang/UnsupportedOperationException;-><init>(Ljava/lang/String;)V

    throw v7

    .line 388
    :pswitch_1d
    if-eq p1, p2, :cond_5b

    .line 389
    sub-int v7, p1, p2

    invoke-static {v7}, Ljava/lang/Math;->abs(I)I

    move-result v1

    .line 390
    const/4 v0, 0x0

    .line 445
    :goto_26
    new-instance v7, Ljava/lang/StringBuilder;

    invoke-direct {v7}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {v7, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v7

    const-string v8, ":"

    invoke-virtual {v7, v8}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v7

    invoke-virtual {v7, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v7

    invoke-virtual {v7}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v6

    .line 446
    .local v6, "traceThenBranch":Ljava/lang/String;
    new-instance v7, Ljava/lang/StringBuilder;

    invoke-direct {v7}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {v7, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v7

    const-string v8, ":"

    invoke-virtual {v7, v8}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v7

    invoke-virtual {v7, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v7

    invoke-virtual {v7}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v5

    .line 447
    .local v5, "traceElseBranch":Ljava/lang/String;
    invoke-static {v6}, Lde/uni_passau/fim/auermich/tracer/Tracer;->trace(Ljava/lang/String;)V

    .line 448
    invoke-static {v5}, Lde/uni_passau/fim/auermich/tracer/Tracer;->trace(Ljava/lang/String;)V

    .line 449
    return-void

    .line 392
    .end local v5    # "traceElseBranch":Ljava/lang/String;
    .end local v6    # "traceThenBranch":Ljava/lang/String;
    :cond_5b
    const/4 v1, 0x0

    .line 393
    const/4 v0, 0x1

    .line 395
    goto :goto_26

    .line 397
    :pswitch_5e
    if-eq p1, p2, :cond_68

    .line 398
    const/4 v1, 0x0

    .line 399
    sub-int v7, p1, p2

    invoke-static {v7}, Ljava/lang/Math;->abs(I)I

    move-result v0

    goto :goto_26

    .line 401
    :cond_68
    const/4 v1, 0x1

    .line 402
    const/4 v0, 0x0

    .line 404
    goto :goto_26

    .line 406
    :pswitch_6b
    if-gt p1, p2, :cond_77

    .line 407
    const/4 v1, 0x0

    .line 408
    sub-int v7, p2, p1

    invoke-static {v7}, Ljava/lang/Math;->abs(I)I

    move-result v7

    add-int/lit8 v0, v7, 0x1

    goto :goto_26

    .line 410
    :cond_77
    sub-int v7, p1, p2

    invoke-static {v7}, Ljava/lang/Math;->abs(I)I

    move-result v1

    .line 411
    const/4 v0, 0x0

    .line 413
    goto :goto_26

    .line 415
    :pswitch_7f
    if-ge p1, p2, :cond_89

    .line 416
    const/4 v1, 0x0

    .line 417
    sub-int v7, p2, p1

    invoke-static {v7}, Ljava/lang/Math;->abs(I)I

    move-result v0

    goto :goto_26

    .line 419
    :cond_89
    sub-int v7, p1, p2

    invoke-static {v7}, Ljava/lang/Math;->abs(I)I

    move-result v7

    add-int/lit8 v1, v7, 0x1

    .line 420
    const/4 v0, 0x0

    .line 422
    goto :goto_26

    .line 424
    :pswitch_93
    if-lt p1, p2, :cond_9f

    .line 425
    const/4 v1, 0x0

    .line 426
    sub-int v7, p1, p2

    invoke-static {v7}, Ljava/lang/Math;->abs(I)I

    move-result v7

    add-int/lit8 v0, v7, 0x1

    goto :goto_26

    .line 428
    :cond_9f
    sub-int v7, p2, p1

    invoke-static {v7}, Ljava/lang/Math;->abs(I)I

    move-result v1

    .line 429
    const/4 v0, 0x0

    .line 431
    goto :goto_26

    .line 433
    :pswitch_a7
    if-le p1, p2, :cond_b2

    .line 434
    const/4 v1, 0x0

    .line 435
    sub-int v7, p1, p2

    invoke-static {v7}, Ljava/lang/Math;->abs(I)I

    move-result v0

    goto/16 :goto_26

    .line 437
    :cond_b2
    sub-int v7, p2, p1

    invoke-static {v7}, Ljava/lang/Math;->abs(I)I

    move-result v7

    add-int/lit8 v1, v7, 0x1

    .line 438
    const/4 v0, 0x0

    .line 440
    goto/16 :goto_26

    .line 386
    nop

    :pswitch_data_be
    .packed-switch 0x0
        :pswitch_1d
        :pswitch_5e
        :pswitch_6b
        :pswitch_7f
        :pswitch_93
        :pswitch_a7
    .end packed-switch
.end method

.method public static computeBranchDistance(Ljava/lang/String;Ljava/lang/Object;)V
    .registers 3
    .param p0, "operation"    # Ljava/lang/String;
    .param p1, "argument"    # Ljava/lang/Object;

    .prologue
    .line 316
    const/4 v0, 0x0

    invoke-static {p0, p1, v0}, Lde/uni_passau/fim/auermich/tracer/Tracer;->computeBranchDistance(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V

    .line 317
    return-void
.end method

.method public static computeBranchDistance(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V
    .registers 13
    .param p0, "operation"    # Ljava/lang/String;
    .param p1, "argument1"    # Ljava/lang/Object;
    .param p2, "argument2"    # Ljava/lang/Object;

    .prologue
    .line 322
    const/4 v1, 0x0

    .line 323
    .local v1, "distanceThenBranch":I
    const/4 v0, 0x0

    .line 325
    .local v0, "distanceElseBranch":I
    const-string v7, ":"

    invoke-virtual {p0, v7}, Ljava/lang/String;->split(Ljava/lang/String;)[Ljava/lang/String;

    move-result-object v4

    .line 326
    .local v4, "tokens":[Ljava/lang/String;
    const/4 v7, 0x0

    aget-object v7, v4, v7

    invoke-static {v7}, Ljava/lang/Integer;->parseInt(Ljava/lang/String;)I

    move-result v3

    .line 327
    .local v3, "opcode":I
    const/4 v7, 0x1

    aget-object v2, v4, v7

    .line 335
    .local v2, "identifier":Ljava/lang/String;
    packed-switch v3, :pswitch_data_78

    .line 356
    new-instance v7, Ljava/lang/UnsupportedOperationException;

    new-instance v8, Ljava/lang/StringBuilder;

    invoke-direct {v8}, Ljava/lang/StringBuilder;-><init>()V

    const-string v9, "Comparison operator "

    invoke-virtual {v8, v9}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v8

    invoke-virtual {v8, p0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v8

    const-string v9, " not yet supported!"

    invoke-virtual {v8, v9}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v8

    invoke-virtual {v8}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v8

    invoke-direct {v7, v8}, Ljava/lang/UnsupportedOperationException;-><init>(Ljava/lang/String;)V

    throw v7

    .line 337
    :pswitch_34
    if-ne p1, p2, :cond_6d

    .line 338
    const/4 v1, 0x0

    .line 339
    const/4 v0, 0x1

    .line 359
    :goto_38
    new-instance v7, Ljava/lang/StringBuilder;

    invoke-direct {v7}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {v7, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v7

    const-string v8, ":"

    invoke-virtual {v7, v8}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v7

    invoke-virtual {v7, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v7

    invoke-virtual {v7}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v6

    .line 360
    .local v6, "traceThenBranch":Ljava/lang/String;
    new-instance v7, Ljava/lang/StringBuilder;

    invoke-direct {v7}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {v7, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v7

    const-string v8, ":"

    invoke-virtual {v7, v8}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v7

    invoke-virtual {v7, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v7

    invoke-virtual {v7}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v5

    .line 361
    .local v5, "traceElseBranch":Ljava/lang/String;
    invoke-static {v6}, Lde/uni_passau/fim/auermich/tracer/Tracer;->trace(Ljava/lang/String;)V

    .line 362
    invoke-static {v5}, Lde/uni_passau/fim/auermich/tracer/Tracer;->trace(Ljava/lang/String;)V

    .line 363
    return-void

    .line 341
    .end local v5    # "traceElseBranch":Ljava/lang/String;
    .end local v6    # "traceThenBranch":Ljava/lang/String;
    :cond_6d
    const/4 v1, 0x1

    .line 342
    const/4 v0, 0x0

    .line 344
    goto :goto_38

    .line 346
    :pswitch_70
    if-eq p1, p2, :cond_75

    .line 347
    const/4 v1, 0x0

    .line 348
    const/4 v0, 0x1

    goto :goto_38

    .line 350
    :cond_75
    const/4 v1, 0x1

    .line 351
    const/4 v0, 0x0

    .line 353
    goto :goto_38

    .line 335
    :pswitch_data_78
    .packed-switch 0x0
        :pswitch_34
        :pswitch_70
    .end packed-switch
.end method

.method private static declared-synchronized createRunFile()V
    .registers 6

    .prologue
    .line 452
    const-class v4, Lde/uni_passau/fim/auermich/tracer/Tracer;

    monitor-enter v4

    :try_start_3
    invoke-static {}, Landroid/os/Environment;->getExternalStorageDirectory()Ljava/io/File;

    move-result-object v2

    .line 453
    .local v2, "sdCard":Ljava/io/File;
    new-instance v1, Ljava/io/File;

    const-string v3, "running.txt"

    invoke-direct {v1, v2, v3}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V
    :try_end_e
    .catchall {:try_start_3 .. :try_end_e} :catchall_25

    .line 456
    .local v1, "file":Ljava/io/File;
    :try_start_e
    invoke-virtual {v1}, Ljava/io/File;->createNewFile()Z
    :try_end_11
    .catch Ljava/io/IOException; {:try_start_e .. :try_end_11} :catch_13
    .catchall {:try_start_e .. :try_end_11} :catchall_25

    .line 461
    :goto_11
    monitor-exit v4

    return-void

    .line 457
    :catch_13
    move-exception v0

    .line 458
    .local v0, "e":Ljava/io/IOException;
    :try_start_14
    sget-object v3, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v5, "Got IOException when creating file running.txt"

    invoke-virtual {v3, v5}, Ljava/util/logging/Logger;->warning(Ljava/lang/String;)V

    .line 459
    sget-object v3, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    invoke-virtual {v0}, Ljava/io/IOException;->getMessage()Ljava/lang/String;

    move-result-object v5

    invoke-virtual {v3, v5}, Ljava/util/logging/Logger;->warning(Ljava/lang/String;)V
    :try_end_24
    .catchall {:try_start_14 .. :try_end_24} :catchall_25

    goto :goto_11

    .line 452
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
    .line 464
    const-class v3, Lde/uni_passau/fim/auermich/tracer/Tracer;

    monitor-enter v3

    :try_start_3
    invoke-static {}, Landroid/os/Environment;->getExternalStorageDirectory()Ljava/io/File;

    move-result-object v1

    .line 465
    .local v1, "sdCard":Ljava/io/File;
    new-instance v2, Ljava/io/File;

    const-string v4, "running.txt"

    invoke-direct {v2, v1, v4}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V

    invoke-virtual {v2}, Ljava/io/File;->delete()Z
    :try_end_11
    .catchall {:try_start_3 .. :try_end_11} :catchall_14

    move-result v0

    .line 466
    .local v0, "ignored":Z
    monitor-exit v3

    return-void

    .line 464
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

    .line 125
    :try_start_1
    const-string v1, "android.app.ActivityThread"

    invoke-static {v1}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v1

    const-string v3, "currentApplication"

    const/4 v4, 0x0

    new-array v4, v4, [Ljava/lang/Class;

    .line 126
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

    .line 130
    .local v0, "e":Ljava/lang/Exception;
    :goto_1a
    return-object v1

    .line 127
    .end local v0    # "e":Ljava/lang/Exception;
    :catch_1b
    move-exception v0

    .line 128
    .restart local v0    # "e":Ljava/lang/Exception;
    sget-object v1, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v3, "Couldn\'t retrieve global context object!"

    invoke-virtual {v1, v3}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 129
    invoke-virtual {v0}, Ljava/lang/Exception;->printStackTrace()V

    move-object v1, v2

    .line 130
    goto :goto_1a
.end method

.method private static isPermissionGranted(Landroid/content/Context;Ljava/lang/String;)Z
    .registers 4
    .param p0, "context"    # Landroid/content/Context;
    .param p1, "permission"    # Ljava/lang/String;

    .prologue
    .line 144
    if-nez p0, :cond_6

    .line 145
    invoke-static {}, Lde/uni_passau/fim/auermich/tracer/Tracer;->getApplicationUsingReflection()Landroid/app/Application;

    move-result-object p0

    .line 148
    :cond_6
    if-nez p0, :cond_10

    .line 149
    new-instance v0, Ljava/lang/IllegalStateException;

    const-string v1, "Couldn\'t access context object!"

    invoke-direct {v0, v1}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V

    throw v0

    .line 152
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
    .line 164
    const-class v1, Lde/uni_passau/fim/auermich/tracer/Tracer;

    monitor-enter v1

    .line 165
    :try_start_3
    sget-object v0, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v0, p0}, Ljava/util/Set;->add(Ljava/lang/Object;)Z

    .line 167
    sget-object v0, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v0}, Ljava/util/Set;->size()I

    move-result v0

    const/16 v2, 0x1388

    if-ne v0, v2, :cond_15

    .line 168
    invoke-static {}, Lde/uni_passau/fim/auermich/tracer/Tracer;->writeTraces()V

    .line 170
    :cond_15
    monitor-exit v1

    .line 171
    return-void

    .line 170
    :catchall_17
    move-exception v0

    monitor-exit v1
    :try_end_19
    .catchall {:try_start_3 .. :try_end_19} :catchall_17

    throw v0
.end method

.method private static writeRemainingTraces()V
    .registers 11

    .prologue
    .line 240
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->uncaughtExceptionHandler:Ljava/lang/Thread$UncaughtExceptionHandler;

    invoke-static {}, Ljava/lang/Thread;->getDefaultUncaughtExceptionHandler()Ljava/lang/Thread$UncaughtExceptionHandler;

    move-result-object v9

    invoke-virtual {v8, v9}, Ljava/lang/Object;->equals(Ljava/lang/Object;)Z

    move-result v8

    if-nez v8, :cond_18

    .line 241
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v9, "Default exception handler has been overridden!"

    invoke-virtual {v8, v9}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 242
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->uncaughtExceptionHandler:Ljava/lang/Thread$UncaughtExceptionHandler;

    invoke-static {v8}, Ljava/lang/Thread;->setDefaultUncaughtExceptionHandler(Ljava/lang/Thread$UncaughtExceptionHandler;)V

    .line 245
    :cond_18
    invoke-static {}, Lde/uni_passau/fim/auermich/tracer/Tracer;->createRunFile()V

    .line 248
    invoke-static {}, Landroid/os/Environment;->getExternalStorageDirectory()Ljava/io/File;

    move-result-object v5

    .line 249
    .local v5, "sdCard":Ljava/io/File;
    new-instance v6, Ljava/io/File;

    const-string v8, "traces.txt"

    invoke-direct {v6, v5, v8}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V

    .line 251
    .local v6, "traceFile":Ljava/io/File;
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    new-instance v9, Ljava/lang/StringBuilder;

    invoke-direct {v9}, Ljava/lang/StringBuilder;-><init>()V

    const-string v10, "Remaining traces size: "

    invoke-virtual {v9, v10}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v9

    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v10}, Ljava/util/Set;->size()I

    move-result v10

    invoke-virtual {v9, v10}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v9

    invoke-virtual {v9}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v9

    invoke-virtual {v8, v9}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 256
    :try_start_44
    new-instance v7, Ljava/io/FileWriter;

    const/4 v8, 0x1

    invoke-direct {v7, v6, v8}, Ljava/io/FileWriter;-><init>(Ljava/io/File;Z)V

    .line 257
    .local v7, "writer":Ljava/io/FileWriter;
    new-instance v0, Ljava/io/BufferedWriter;

    invoke-direct {v0, v7}, Ljava/io/BufferedWriter;-><init>(Ljava/io/Writer;)V

    .line 259
    .local v0, "br":Ljava/io/BufferedWriter;
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v8}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v4

    .line 260
    .local v4, "iterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    const/4 v2, 0x0

    .line 262
    .local v2, "element":Ljava/lang/String;
    :goto_56
    invoke-interface {v4}, Ljava/util/Iterator;->hasNext()Z

    move-result v8

    if-eqz v8, :cond_e1

    .line 264
    if-nez v2, :cond_da

    .line 265
    invoke-interface {v4}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v2

    .end local v2    # "element":Ljava/lang/String;
    check-cast v2, Ljava/lang/String;

    .line 266
    .restart local v2    # "element":Ljava/lang/String;
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    new-instance v9, Ljava/lang/StringBuilder;

    invoke-direct {v9}, Ljava/lang/StringBuilder;-><init>()V

    const-string v10, "First entry: "

    invoke-virtual {v9, v10}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v9

    invoke-virtual {v9, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v9

    invoke-virtual {v9}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v9

    invoke-virtual {v8, v9}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 271
    :goto_7c
    invoke-virtual {v0, v2}, Ljava/io/BufferedWriter;->write(Ljava/lang/String;)V

    .line 272
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->newLine()V
    :try_end_82
    .catch Ljava/lang/Exception; {:try_start_44 .. :try_end_82} :catch_83

    goto :goto_56

    .line 283
    .end local v0    # "br":Ljava/io/BufferedWriter;
    .end local v2    # "element":Ljava/lang/String;
    .end local v4    # "iterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    .end local v7    # "writer":Ljava/io/FileWriter;
    :catch_83
    move-exception v1

    .line 284
    .local v1, "e":Ljava/lang/Exception;
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v9, "Writing traces.txt to external storage failed."

    invoke-virtual {v8, v9}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 285
    invoke-virtual {v1}, Ljava/lang/Exception;->printStackTrace()V

    .line 289
    .end local v1    # "e":Ljava/lang/Exception;
    :goto_8e
    new-instance v3, Ljava/io/File;

    const-string v8, "info.txt"

    invoke-direct {v3, v5, v8}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V

    .line 292
    .local v3, "infoFile":Ljava/io/File;
    :try_start_95
    new-instance v7, Ljava/io/FileWriter;

    invoke-direct {v7, v3}, Ljava/io/FileWriter;-><init>(Ljava/io/File;)V

    .line 294
    .restart local v7    # "writer":Ljava/io/FileWriter;
    sget v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    sget-object v9, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v9}, Ljava/util/Set;->size()I

    move-result v9

    add-int/2addr v8, v9

    sput v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    .line 295
    sget v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    invoke-static {v8}, Ljava/lang/String;->valueOf(I)Ljava/lang/String;

    move-result-object v8

    invoke-virtual {v7, v8}, Ljava/io/FileWriter;->append(Ljava/lang/CharSequence;)Ljava/io/Writer;

    .line 296
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    new-instance v9, Ljava/lang/StringBuilder;

    invoke-direct {v9}, Ljava/lang/StringBuilder;-><init>()V

    const-string v10, "Total number of traces in file: "

    invoke-virtual {v9, v10}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v9

    sget v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    invoke-virtual {v9, v10}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v9

    invoke-virtual {v9}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v9

    invoke-virtual {v8, v9}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 299
    const/4 v8, 0x0

    sput v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    .line 301
    invoke-virtual {v7}, Ljava/io/FileWriter;->flush()V

    .line 302
    invoke-virtual {v7}, Ljava/io/FileWriter;->close()V
    :try_end_d1
    .catch Ljava/lang/Exception; {:try_start_95 .. :try_end_d1} :catch_10b

    .line 310
    .end local v7    # "writer":Ljava/io/FileWriter;
    :goto_d1
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v8}, Ljava/util/Set;->clear()V

    .line 311
    invoke-static {}, Lde/uni_passau/fim/auermich/tracer/Tracer;->deleteRunFile()V

    .line 312
    return-void

    .line 268
    .end local v3    # "infoFile":Ljava/io/File;
    .restart local v0    # "br":Ljava/io/BufferedWriter;
    .restart local v2    # "element":Ljava/lang/String;
    .restart local v4    # "iterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    .restart local v7    # "writer":Ljava/io/FileWriter;
    :cond_da
    :try_start_da
    invoke-interface {v4}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v2

    .end local v2    # "element":Ljava/lang/String;
    check-cast v2, Ljava/lang/String;

    .restart local v2    # "element":Ljava/lang/String;
    goto :goto_7c

    .line 275
    :cond_e1
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v8}, Ljava/util/Set;->isEmpty()Z

    move-result v8

    if-nez v8, :cond_101

    .line 276
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    new-instance v9, Ljava/lang/StringBuilder;

    invoke-direct {v9}, Ljava/lang/StringBuilder;-><init>()V

    const-string v10, "Last entry: "

    invoke-virtual {v9, v10}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v9

    invoke-virtual {v9, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v9

    invoke-virtual {v9}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v9

    invoke-virtual {v8, v9}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 279
    :cond_101
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->flush()V

    .line 280
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->close()V

    .line 281
    invoke-virtual {v7}, Ljava/io/FileWriter;->close()V
    :try_end_10a
    .catch Ljava/lang/Exception; {:try_start_da .. :try_end_10a} :catch_83

    goto :goto_8e

    .line 304
    .end local v0    # "br":Ljava/io/BufferedWriter;
    .end local v2    # "element":Ljava/lang/String;
    .end local v4    # "iterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    .end local v7    # "writer":Ljava/io/FileWriter;
    .restart local v3    # "infoFile":Ljava/io/File;
    :catch_10b
    move-exception v1

    .line 305
    .restart local v1    # "e":Ljava/lang/Exception;
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v9, "Writing info.txt to external storage failed."

    invoke-virtual {v8, v9}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 306
    invoke-virtual {v1}, Ljava/lang/Exception;->printStackTrace()V

    goto :goto_d1
.end method

.method private static writeTraces()V
    .registers 16

    .prologue
    .line 179
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->uncaughtExceptionHandler:Ljava/lang/Thread$UncaughtExceptionHandler;

    invoke-static {}, Ljava/lang/Thread;->getDefaultUncaughtExceptionHandler()Ljava/lang/Thread$UncaughtExceptionHandler;

    move-result-object v11

    invoke-virtual {v10, v11}, Ljava/lang/Object;->equals(Ljava/lang/Object;)Z

    move-result v10

    if-nez v10, :cond_18

    .line 180
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v11, "Default exception handler has been overridden!"

    invoke-virtual {v10, v11}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 181
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->uncaughtExceptionHandler:Ljava/lang/Thread$UncaughtExceptionHandler;

    invoke-static {v10}, Ljava/lang/Thread;->setDefaultUncaughtExceptionHandler(Ljava/lang/Thread$UncaughtExceptionHandler;)V

    .line 184
    :cond_18
    invoke-static {}, Lde/uni_passau/fim/auermich/tracer/Tracer;->createRunFile()V

    .line 186
    invoke-static {}, Landroid/os/Environment;->getExternalStorageDirectory()Ljava/io/File;

    move-result-object v2

    .line 187
    .local v2, "sdCard":Ljava/io/File;
    new-instance v8, Ljava/io/File;

    const-string v10, "traces.txt"

    invoke-direct {v8, v2, v10}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V

    .line 189
    .local v8, "traceFile":Ljava/io/File;
    sget v10, Landroid/os/Build$VERSION;->SDK_INT:I

    const/16 v11, 0x17

    if-lt v10, v11, :cond_3c

    .line 190
    const/4 v10, 0x0

    const-string v11, "android.permission.WRITE_EXTERNAL_STORAGE"

    invoke-static {v10, v11}, Lde/uni_passau/fim/auermich/tracer/Tracer;->isPermissionGranted(Landroid/content/Context;Ljava/lang/String;)Z

    move-result v10

    if-nez v10, :cond_3c

    .line 191
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v11, "Permissions got dropped unexpectedly!"

    invoke-virtual {v10, v11}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 197
    :cond_3c
    :try_start_3c
    new-instance v9, Ljava/io/FileWriter;

    const/4 v10, 0x1

    invoke-direct {v9, v8, v10}, Ljava/io/FileWriter;-><init>(Ljava/io/File;Z)V

    .line 198
    .local v9, "writer":Ljava/io/FileWriter;
    new-instance v0, Ljava/io/BufferedWriter;

    invoke-direct {v0, v9}, Ljava/io/BufferedWriter;-><init>(Ljava/io/Writer;)V

    .line 200
    .local v0, "br":Ljava/io/BufferedWriter;
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v10}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v10

    :goto_4d
    invoke-interface {v10}, Ljava/util/Iterator;->hasNext()Z

    move-result v11

    if-eqz v11, :cond_be

    invoke-interface {v10}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v7

    check-cast v7, Ljava/lang/String;

    .line 201
    .local v7, "trace":Ljava/lang/String;
    invoke-virtual {v0, v7}, Ljava/io/BufferedWriter;->write(Ljava/lang/String;)V

    .line 202
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->newLine()V
    :try_end_5f
    .catch Ljava/lang/IndexOutOfBoundsException; {:try_start_3c .. :try_end_5f} :catch_60
    .catch Ljava/lang/Exception; {:try_start_3c .. :try_end_5f} :catch_f0

    goto :goto_4d

    .line 213
    .end local v0    # "br":Ljava/io/BufferedWriter;
    .end local v7    # "trace":Ljava/lang/String;
    .end local v9    # "writer":Ljava/io/FileWriter;
    :catch_60
    move-exception v1

    .line 214
    .local v1, "e":Ljava/lang/IndexOutOfBoundsException;
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v11, "Synchronization issue!"

    invoke-virtual {v10, v11}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 215
    invoke-static {}, Ljava/lang/Thread;->getAllStackTraces()Ljava/util/Map;

    move-result-object v6

    .line 216
    .local v6, "threadStackTraces":Ljava/util/Map;, "Ljava/util/Map<Ljava/lang/Thread;[Ljava/lang/StackTraceElement;>;"
    invoke-interface {v6}, Ljava/util/Map;->keySet()Ljava/util/Set;

    move-result-object v10

    invoke-interface {v10}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v11

    :cond_74
    invoke-interface {v11}, Ljava/util/Iterator;->hasNext()Z

    move-result v10

    if-eqz v10, :cond_e7

    invoke-interface {v11}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v5

    check-cast v5, Ljava/lang/Thread;

    .line 217
    .local v5, "thread":Ljava/lang/Thread;
    invoke-interface {v6, v5}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v3

    check-cast v3, [Ljava/lang/StackTraceElement;

    .line 218
    .local v3, "stackTrace":[Ljava/lang/StackTraceElement;
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    new-instance v12, Ljava/lang/StringBuilder;

    invoke-direct {v12}, Ljava/lang/StringBuilder;-><init>()V

    const-string v13, "Thread["

    invoke-virtual {v12, v13}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v12

    invoke-virtual {v5}, Ljava/lang/Thread;->getId()J

    move-result-wide v14

    invoke-virtual {v12, v14, v15}, Ljava/lang/StringBuilder;->append(J)Ljava/lang/StringBuilder;

    move-result-object v12

    const-string v13, "]: "

    invoke-virtual {v12, v13}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v12

    invoke-virtual {v12, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    move-result-object v12

    invoke-virtual {v12}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v12

    invoke-virtual {v10, v12}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 219
    array-length v12, v3

    const/4 v10, 0x0

    :goto_ae
    if-ge v10, v12, :cond_74

    aget-object v4, v3, v10

    .line 220
    .local v4, "stackTraceElement":Ljava/lang/StackTraceElement;
    sget-object v13, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    invoke-static {v4}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v14

    invoke-virtual {v13, v14}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 219
    add-int/lit8 v10, v10, 0x1

    goto :goto_ae

    .line 206
    .end local v1    # "e":Ljava/lang/IndexOutOfBoundsException;
    .end local v3    # "stackTrace":[Ljava/lang/StackTraceElement;
    .end local v4    # "stackTraceElement":Ljava/lang/StackTraceElement;
    .end local v5    # "thread":Ljava/lang/Thread;
    .end local v6    # "threadStackTraces":Ljava/util/Map;, "Ljava/util/Map<Ljava/lang/Thread;[Ljava/lang/StackTraceElement;>;"
    .restart local v0    # "br":Ljava/io/BufferedWriter;
    .restart local v9    # "writer":Ljava/io/FileWriter;
    :cond_be
    :try_start_be
    sget v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    add-int/lit16 v10, v10, 0x1388

    sput v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    .line 207
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    new-instance v11, Ljava/lang/StringBuilder;

    invoke-direct {v11}, Ljava/lang/StringBuilder;-><init>()V

    const-string v12, "Accumulated traces size: "

    invoke-virtual {v11, v12}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v11

    sget v12, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    invoke-virtual {v11, v12}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v11

    invoke-virtual {v11}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v11

    invoke-virtual {v10, v11}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 209
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->flush()V

    .line 210
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->close()V

    .line 211
    invoke-virtual {v9}, Ljava/io/FileWriter;->close()V
    :try_end_e7
    .catch Ljava/lang/IndexOutOfBoundsException; {:try_start_be .. :try_end_e7} :catch_60
    .catch Ljava/lang/Exception; {:try_start_be .. :try_end_e7} :catch_f0

    .line 229
    .end local v0    # "br":Ljava/io/BufferedWriter;
    .end local v9    # "writer":Ljava/io/FileWriter;
    :cond_e7
    :goto_e7
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v10}, Ljava/util/Set;->clear()V

    .line 230
    invoke-static {}, Lde/uni_passau/fim/auermich/tracer/Tracer;->deleteRunFile()V

    .line 231
    return-void

    .line 223
    :catch_f0
    move-exception v1

    .line 224
    .local v1, "e":Ljava/lang/Exception;
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v11, "Writing traces.txt to external storage failed."

    invoke-virtual {v10, v11}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 225
    invoke-virtual {v1}, Ljava/lang/Exception;->printStackTrace()V

    goto :goto_e7
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
