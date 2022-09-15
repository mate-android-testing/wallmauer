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

.method public static computeBranchDistance(Ljava/lang/String;I)V
    .registers 3
    .param p0, "operation"    # Ljava/lang/String;
    .param p1, "argument"    # I

    .prologue
    .line 357
    const/4 v0, 0x0

    invoke-static {p0, p1, v0}, Lde/uni_passau/fim/auermich/tracer/Tracer;->computeBranchDistance(Ljava/lang/String;II)V

    .line 358
    return-void
.end method

.method public static computeBranchDistance(Ljava/lang/String;II)V
    .registers 12
    .param p0, "operation"    # Ljava/lang/String;
    .param p1, "argument1"    # I
    .param p2, "argument2"    # I

    .prologue
    .line 363
    const/4 v1, 0x0

    .line 364
    .local v1, "distanceThenBranch":I
    const/4 v0, 0x0

    .line 366
    .local v0, "distanceElseBranch":I
    const-string v7, ":"

    invoke-virtual {p0, v7}, Ljava/lang/String;->split(Ljava/lang/String;)[Ljava/lang/String;

    move-result-object v4

    .line 367
    .local v4, "tokens":[Ljava/lang/String;
    const/4 v7, 0x0

    aget-object v7, v4, v7

    invoke-static {v7}, Ljava/lang/Integer;->parseInt(Ljava/lang/String;)I

    move-result v3

    .line 368
    .local v3, "opcode":I
    const/4 v7, 0x1

    aget-object v2, v4, v7

    .line 376
    .local v2, "identifier":Ljava/lang/String;
    packed-switch v3, :pswitch_data_be

    .line 432
    new-instance v7, Ljava/lang/UnsupportedOperationException;

    const-string v8, "Comparison operator not yet supported!"

    invoke-direct {v7, v8}, Ljava/lang/UnsupportedOperationException;-><init>(Ljava/lang/String;)V

    throw v7

    .line 378
    :pswitch_1d
    if-eq p1, p2, :cond_5b

    .line 379
    sub-int v7, p1, p2

    invoke-static {v7}, Ljava/lang/Math;->abs(I)I

    move-result v1

    .line 380
    const/4 v0, 0x0

    .line 435
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

    .line 436
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

    .line 437
    .local v5, "traceElseBranch":Ljava/lang/String;
    invoke-static {v6}, Lde/uni_passau/fim/auermich/tracer/Tracer;->trace(Ljava/lang/String;)V

    .line 438
    invoke-static {v5}, Lde/uni_passau/fim/auermich/tracer/Tracer;->trace(Ljava/lang/String;)V

    .line 439
    return-void

    .line 382
    .end local v5    # "traceElseBranch":Ljava/lang/String;
    .end local v6    # "traceThenBranch":Ljava/lang/String;
    :cond_5b
    const/4 v1, 0x0

    .line 383
    const/4 v0, 0x1

    .line 385
    goto :goto_26

    .line 387
    :pswitch_5e
    if-eq p1, p2, :cond_68

    .line 388
    const/4 v1, 0x0

    .line 389
    sub-int v7, p1, p2

    invoke-static {v7}, Ljava/lang/Math;->abs(I)I

    move-result v0

    goto :goto_26

    .line 391
    :cond_68
    const/4 v1, 0x1

    .line 392
    const/4 v0, 0x0

    .line 394
    goto :goto_26

    .line 396
    :pswitch_6b
    if-gt p1, p2, :cond_77

    .line 397
    const/4 v1, 0x0

    .line 398
    sub-int v7, p2, p1

    invoke-static {v7}, Ljava/lang/Math;->abs(I)I

    move-result v7

    add-int/lit8 v0, v7, 0x1

    goto :goto_26

    .line 400
    :cond_77
    sub-int v7, p1, p2

    invoke-static {v7}, Ljava/lang/Math;->abs(I)I

    move-result v1

    .line 401
    const/4 v0, 0x0

    .line 403
    goto :goto_26

    .line 405
    :pswitch_7f
    if-ge p1, p2, :cond_89

    .line 406
    const/4 v1, 0x0

    .line 407
    sub-int v7, p2, p1

    invoke-static {v7}, Ljava/lang/Math;->abs(I)I

    move-result v0

    goto :goto_26

    .line 409
    :cond_89
    sub-int v7, p1, p2

    invoke-static {v7}, Ljava/lang/Math;->abs(I)I

    move-result v7

    add-int/lit8 v1, v7, 0x1

    .line 410
    const/4 v0, 0x0

    .line 412
    goto :goto_26

    .line 414
    :pswitch_93
    if-lt p1, p2, :cond_9f

    .line 415
    const/4 v1, 0x0

    .line 416
    sub-int v7, p1, p2

    invoke-static {v7}, Ljava/lang/Math;->abs(I)I

    move-result v7

    add-int/lit8 v0, v7, 0x1

    goto :goto_26

    .line 418
    :cond_9f
    sub-int v7, p2, p1

    invoke-static {v7}, Ljava/lang/Math;->abs(I)I

    move-result v1

    .line 419
    const/4 v0, 0x0

    .line 421
    goto :goto_26

    .line 423
    :pswitch_a7
    if-le p1, p2, :cond_b2

    .line 424
    const/4 v1, 0x0

    .line 425
    sub-int v7, p1, p2

    invoke-static {v7}, Ljava/lang/Math;->abs(I)I

    move-result v0

    goto/16 :goto_26

    .line 427
    :cond_b2
    sub-int v7, p2, p1

    invoke-static {v7}, Ljava/lang/Math;->abs(I)I

    move-result v7

    add-int/lit8 v1, v7, 0x1

    .line 428
    const/4 v0, 0x0

    .line 430
    goto/16 :goto_26

    .line 376
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
    .line 306
    const/4 v0, 0x0

    invoke-static {p0, p1, v0}, Lde/uni_passau/fim/auermich/tracer/Tracer;->computeBranchDistance(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V

    .line 307
    return-void
.end method

.method public static computeBranchDistance(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V
    .registers 13
    .param p0, "operation"    # Ljava/lang/String;
    .param p1, "argument1"    # Ljava/lang/Object;
    .param p2, "argument2"    # Ljava/lang/Object;

    .prologue
    .line 312
    const/4 v1, 0x0

    .line 313
    .local v1, "distanceThenBranch":I
    const/4 v0, 0x0

    .line 315
    .local v0, "distanceElseBranch":I
    const-string v7, ":"

    invoke-virtual {p0, v7}, Ljava/lang/String;->split(Ljava/lang/String;)[Ljava/lang/String;

    move-result-object v4

    .line 316
    .local v4, "tokens":[Ljava/lang/String;
    const/4 v7, 0x0

    aget-object v7, v4, v7

    invoke-static {v7}, Ljava/lang/Integer;->parseInt(Ljava/lang/String;)I

    move-result v3

    .line 317
    .local v3, "opcode":I
    const/4 v7, 0x1

    aget-object v2, v4, v7

    .line 325
    .local v2, "identifier":Ljava/lang/String;
    packed-switch v3, :pswitch_data_78

    .line 346
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

    .line 327
    :pswitch_34
    if-ne p1, p2, :cond_6d

    .line 328
    const/4 v1, 0x0

    .line 329
    const/4 v0, 0x1

    .line 349
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

    .line 350
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

    .line 351
    .local v5, "traceElseBranch":Ljava/lang/String;
    invoke-static {v6}, Lde/uni_passau/fim/auermich/tracer/Tracer;->trace(Ljava/lang/String;)V

    .line 352
    invoke-static {v5}, Lde/uni_passau/fim/auermich/tracer/Tracer;->trace(Ljava/lang/String;)V

    .line 353
    return-void

    .line 331
    .end local v5    # "traceElseBranch":Ljava/lang/String;
    .end local v6    # "traceThenBranch":Ljava/lang/String;
    :cond_6d
    const/4 v1, 0x1

    .line 332
    const/4 v0, 0x0

    .line 334
    goto :goto_38

    .line 336
    :pswitch_70
    if-eq p1, p2, :cond_75

    .line 337
    const/4 v1, 0x0

    .line 338
    const/4 v0, 0x1

    goto :goto_38

    .line 340
    :cond_75
    const/4 v1, 0x1

    .line 341
    const/4 v0, 0x0

    .line 343
    goto :goto_38

    .line 325
    :pswitch_data_78
    .packed-switch 0x0
        :pswitch_34
        :pswitch_70
    .end packed-switch
.end method

.method public static computeBranchDistanceSwitch(Ljava/lang/String;II)V
    .registers 8
    .param p0, "trace"    # Ljava/lang/String;
    .param p1, "switchValue"    # I
    .param p2, "caseValue"    # I

    .prologue
    .line 456
    sub-int v2, p1, p2

    invoke-static {v2}, Ljava/lang/Math;->abs(I)I

    move-result v0

    .line 457
    .local v0, "distance":I
    sget-object v2, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    new-instance v3, Ljava/lang/StringBuilder;

    invoke-direct {v3}, Ljava/lang/StringBuilder;-><init>()V

    const-string v4, "Branch distance of case value "

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v3

    invoke-virtual {v3, p2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v3

    const-string v4, " for "

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v3

    invoke-virtual {v3, p0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v3

    const-string v4, ": "

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v3

    invoke-virtual {v3, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v3

    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    invoke-virtual {v2, v3}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 458
    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {v2, p0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v2

    const-string v3, ":"

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v2

    invoke-virtual {v2, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v2

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    .line 459
    .local v1, "distanceTrace":Ljava/lang/String;
    invoke-static {v1}, Lde/uni_passau/fim/auermich/tracer/Tracer;->trace(Ljava/lang/String;)V

    .line 460
    return-void
.end method

.method private static getApplicationUsingReflection()Landroid/app/Application;
    .registers 5

    .prologue
    const/4 v2, 0x0

    .line 121
    :try_start_1
    const-string v1, "android.app.ActivityThread"

    invoke-static {v1}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v1

    const-string v3, "currentApplication"

    const/4 v4, 0x0

    new-array v4, v4, [Ljava/lang/Class;

    .line 122
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

    .line 126
    .local v0, "e":Ljava/lang/Exception;
    :goto_1a
    return-object v1

    .line 123
    .end local v0    # "e":Ljava/lang/Exception;
    :catch_1b
    move-exception v0

    .line 124
    .restart local v0    # "e":Ljava/lang/Exception;
    sget-object v1, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v3, "Couldn\'t retrieve global context object!"

    invoke-virtual {v1, v3}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 125
    invoke-virtual {v0}, Ljava/lang/Exception;->printStackTrace()V

    move-object v1, v2

    .line 126
    goto :goto_1a
.end method

.method private static isPermissionGranted(Landroid/content/Context;Ljava/lang/String;)Z
    .registers 4
    .param p0, "context"    # Landroid/content/Context;
    .param p1, "permission"    # Ljava/lang/String;

    .prologue
    .line 140
    if-nez p0, :cond_6

    .line 141
    invoke-static {}, Lde/uni_passau/fim/auermich/tracer/Tracer;->getApplicationUsingReflection()Landroid/app/Application;

    move-result-object p0

    .line 144
    :cond_6
    if-nez p0, :cond_10

    .line 145
    new-instance v0, Ljava/lang/IllegalStateException;

    const-string v1, "Couldn\'t access context object!"

    invoke-direct {v0, v1}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V

    throw v0

    .line 148
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
    .line 160
    const-class v1, Lde/uni_passau/fim/auermich/tracer/Tracer;

    monitor-enter v1

    .line 161
    :try_start_3
    sget-object v0, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v0, p0}, Ljava/util/Set;->add(Ljava/lang/Object;)Z

    .line 163
    sget-object v0, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v0}, Ljava/util/Set;->size()I

    move-result v0

    const/16 v2, 0x1388

    if-ne v0, v2, :cond_15

    .line 164
    invoke-static {}, Lde/uni_passau/fim/auermich/tracer/Tracer;->writeTraces()V

    .line 166
    :cond_15
    monitor-exit v1

    .line 167
    return-void

    .line 166
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
    .line 233
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->uncaughtExceptionHandler:Ljava/lang/Thread$UncaughtExceptionHandler;

    invoke-static {}, Ljava/lang/Thread;->getDefaultUncaughtExceptionHandler()Ljava/lang/Thread$UncaughtExceptionHandler;

    move-result-object v9

    invoke-virtual {v8, v9}, Ljava/lang/Object;->equals(Ljava/lang/Object;)Z

    move-result v8

    if-nez v8, :cond_18

    .line 234
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v9, "Default exception handler has been overridden!"

    invoke-virtual {v8, v9}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 235
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->uncaughtExceptionHandler:Ljava/lang/Thread$UncaughtExceptionHandler;

    invoke-static {v8}, Ljava/lang/Thread;->setDefaultUncaughtExceptionHandler(Ljava/lang/Thread$UncaughtExceptionHandler;)V

    .line 239
    :cond_18
    invoke-static {}, Landroid/os/Environment;->getExternalStorageDirectory()Ljava/io/File;

    move-result-object v5

    .line 240
    .local v5, "sdCard":Ljava/io/File;
    new-instance v6, Ljava/io/File;

    const-string v8, "traces.txt"

    invoke-direct {v6, v5, v8}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V

    .line 242
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

    .line 247
    :try_start_41
    new-instance v7, Ljava/io/FileWriter;

    const/4 v8, 0x1

    invoke-direct {v7, v6, v8}, Ljava/io/FileWriter;-><init>(Ljava/io/File;Z)V

    .line 248
    .local v7, "writer":Ljava/io/FileWriter;
    new-instance v0, Ljava/io/BufferedWriter;

    invoke-direct {v0, v7}, Ljava/io/BufferedWriter;-><init>(Ljava/io/Writer;)V

    .line 250
    .local v0, "br":Ljava/io/BufferedWriter;
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v8}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v4

    .line 251
    .local v4, "iterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    const/4 v2, 0x0

    .line 253
    .local v2, "element":Ljava/lang/String;
    :goto_53
    invoke-interface {v4}, Ljava/util/Iterator;->hasNext()Z

    move-result v8

    if-eqz v8, :cond_db

    .line 255
    if-nez v2, :cond_d4

    .line 256
    invoke-interface {v4}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v2

    .end local v2    # "element":Ljava/lang/String;
    check-cast v2, Ljava/lang/String;

    .line 257
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

    .line 262
    :goto_79
    invoke-virtual {v0, v2}, Ljava/io/BufferedWriter;->write(Ljava/lang/String;)V

    .line 263
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->newLine()V
    :try_end_7f
    .catch Ljava/lang/Exception; {:try_start_41 .. :try_end_7f} :catch_80

    goto :goto_53

    .line 274
    .end local v0    # "br":Ljava/io/BufferedWriter;
    .end local v2    # "element":Ljava/lang/String;
    .end local v4    # "iterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    .end local v7    # "writer":Ljava/io/FileWriter;
    :catch_80
    move-exception v1

    .line 275
    .local v1, "e":Ljava/lang/Exception;
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v9, "Writing traces.txt to external storage failed."

    invoke-virtual {v8, v9}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 276
    invoke-virtual {v1}, Ljava/lang/Exception;->printStackTrace()V

    .line 280
    .end local v1    # "e":Ljava/lang/Exception;
    :goto_8b
    new-instance v3, Ljava/io/File;

    const-string v8, "info.txt"

    invoke-direct {v3, v5, v8}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V

    .line 283
    .local v3, "infoFile":Ljava/io/File;
    :try_start_92
    new-instance v7, Ljava/io/FileWriter;

    invoke-direct {v7, v3}, Ljava/io/FileWriter;-><init>(Ljava/io/File;)V

    .line 285
    .restart local v7    # "writer":Ljava/io/FileWriter;
    sget v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    sget-object v9, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v9}, Ljava/util/Set;->size()I

    move-result v9

    add-int/2addr v8, v9

    sput v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    .line 286
    sget v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    invoke-static {v8}, Ljava/lang/String;->valueOf(I)Ljava/lang/String;

    move-result-object v8

    invoke-virtual {v7, v8}, Ljava/io/FileWriter;->append(Ljava/lang/CharSequence;)Ljava/io/Writer;

    .line 287
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

    .line 290
    const/4 v8, 0x0

    sput v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    .line 292
    invoke-virtual {v7}, Ljava/io/FileWriter;->flush()V

    .line 293
    invoke-virtual {v7}, Ljava/io/FileWriter;->close()V
    :try_end_ce
    .catch Ljava/lang/Exception; {:try_start_92 .. :try_end_ce} :catch_105

    .line 301
    .end local v7    # "writer":Ljava/io/FileWriter;
    :goto_ce
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v8}, Ljava/util/Set;->clear()V

    .line 302
    return-void

    .line 259
    .end local v3    # "infoFile":Ljava/io/File;
    .restart local v0    # "br":Ljava/io/BufferedWriter;
    .restart local v2    # "element":Ljava/lang/String;
    .restart local v4    # "iterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    .restart local v7    # "writer":Ljava/io/FileWriter;
    :cond_d4
    :try_start_d4
    invoke-interface {v4}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v2

    .end local v2    # "element":Ljava/lang/String;
    check-cast v2, Ljava/lang/String;

    .restart local v2    # "element":Ljava/lang/String;
    goto :goto_79

    .line 266
    :cond_db
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v8}, Ljava/util/Set;->isEmpty()Z

    move-result v8

    if-nez v8, :cond_fb

    .line 267
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

    .line 270
    :cond_fb
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->flush()V

    .line 271
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->close()V

    .line 272
    invoke-virtual {v7}, Ljava/io/FileWriter;->close()V
    :try_end_104
    .catch Ljava/lang/Exception; {:try_start_d4 .. :try_end_104} :catch_80

    goto :goto_8b

    .line 295
    .end local v0    # "br":Ljava/io/BufferedWriter;
    .end local v2    # "element":Ljava/lang/String;
    .end local v4    # "iterator":Ljava/util/Iterator;, "Ljava/util/Iterator<Ljava/lang/String;>;"
    .end local v7    # "writer":Ljava/io/FileWriter;
    .restart local v3    # "infoFile":Ljava/io/File;
    :catch_105
    move-exception v1

    .line 296
    .restart local v1    # "e":Ljava/lang/Exception;
    sget-object v8, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v9, "Writing info.txt to external storage failed."

    invoke-virtual {v8, v9}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 297
    invoke-virtual {v1}, Ljava/lang/Exception;->printStackTrace()V

    goto :goto_ce
.end method

.method private static writeTraces()V
    .registers 16

    .prologue
    .line 175
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->uncaughtExceptionHandler:Ljava/lang/Thread$UncaughtExceptionHandler;

    invoke-static {}, Ljava/lang/Thread;->getDefaultUncaughtExceptionHandler()Ljava/lang/Thread$UncaughtExceptionHandler;

    move-result-object v11

    invoke-virtual {v10, v11}, Ljava/lang/Object;->equals(Ljava/lang/Object;)Z

    move-result v10

    if-nez v10, :cond_18

    .line 176
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v11, "Default exception handler has been overridden!"

    invoke-virtual {v10, v11}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 177
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->uncaughtExceptionHandler:Ljava/lang/Thread$UncaughtExceptionHandler;

    invoke-static {v10}, Ljava/lang/Thread;->setDefaultUncaughtExceptionHandler(Ljava/lang/Thread$UncaughtExceptionHandler;)V

    .line 180
    :cond_18
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

    const/16 v11, 0x17

    if-lt v10, v11, :cond_39

    .line 184
    const/4 v10, 0x0

    const-string v11, "android.permission.WRITE_EXTERNAL_STORAGE"

    invoke-static {v10, v11}, Lde/uni_passau/fim/auermich/tracer/Tracer;->isPermissionGranted(Landroid/content/Context;Ljava/lang/String;)Z

    move-result v10

    if-nez v10, :cond_39

    .line 185
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v11, "Permissions got dropped unexpectedly!"

    invoke-virtual {v10, v11}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 191
    :cond_39
    :try_start_39
    new-instance v9, Ljava/io/FileWriter;

    const/4 v10, 0x1

    invoke-direct {v9, v8, v10}, Ljava/io/FileWriter;-><init>(Ljava/io/File;Z)V

    .line 192
    .local v9, "writer":Ljava/io/FileWriter;
    new-instance v0, Ljava/io/BufferedWriter;

    invoke-direct {v0, v9}, Ljava/io/BufferedWriter;-><init>(Ljava/io/Writer;)V

    .line 194
    .local v0, "br":Ljava/io/BufferedWriter;
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v10}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v10

    :goto_4a
    invoke-interface {v10}, Ljava/util/Iterator;->hasNext()Z

    move-result v11

    if-eqz v11, :cond_bb

    invoke-interface {v10}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v7

    check-cast v7, Ljava/lang/String;

    .line 195
    .local v7, "trace":Ljava/lang/String;
    invoke-virtual {v0, v7}, Ljava/io/BufferedWriter;->write(Ljava/lang/String;)V

    .line 196
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->newLine()V
    :try_end_5c
    .catch Ljava/lang/IndexOutOfBoundsException; {:try_start_39 .. :try_end_5c} :catch_5d
    .catch Ljava/lang/Exception; {:try_start_39 .. :try_end_5c} :catch_ea

    goto :goto_4a

    .line 207
    .end local v0    # "br":Ljava/io/BufferedWriter;
    .end local v7    # "trace":Ljava/lang/String;
    .end local v9    # "writer":Ljava/io/FileWriter;
    :catch_5d
    move-exception v1

    .line 208
    .local v1, "e":Ljava/lang/IndexOutOfBoundsException;
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v11, "Synchronization issue!"

    invoke-virtual {v10, v11}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 209
    invoke-static {}, Ljava/lang/Thread;->getAllStackTraces()Ljava/util/Map;

    move-result-object v6

    .line 210
    .local v6, "threadStackTraces":Ljava/util/Map;, "Ljava/util/Map<Ljava/lang/Thread;[Ljava/lang/StackTraceElement;>;"
    invoke-interface {v6}, Ljava/util/Map;->keySet()Ljava/util/Set;

    move-result-object v10

    invoke-interface {v10}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v11

    :cond_71
    invoke-interface {v11}, Ljava/util/Iterator;->hasNext()Z

    move-result v10

    if-eqz v10, :cond_e4

    invoke-interface {v11}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v5

    check-cast v5, Ljava/lang/Thread;

    .line 211
    .local v5, "thread":Ljava/lang/Thread;
    invoke-interface {v6, v5}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v3

    check-cast v3, [Ljava/lang/StackTraceElement;

    .line 212
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

    .line 213
    array-length v12, v3

    const/4 v10, 0x0

    :goto_ab
    if-ge v10, v12, :cond_71

    aget-object v4, v3, v10

    .line 214
    .local v4, "stackTraceElement":Ljava/lang/StackTraceElement;
    sget-object v13, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    invoke-static {v4}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v14

    invoke-virtual {v13, v14}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 213
    add-int/lit8 v10, v10, 0x1

    goto :goto_ab

    .line 200
    .end local v1    # "e":Ljava/lang/IndexOutOfBoundsException;
    .end local v3    # "stackTrace":[Ljava/lang/StackTraceElement;
    .end local v4    # "stackTraceElement":Ljava/lang/StackTraceElement;
    .end local v5    # "thread":Ljava/lang/Thread;
    .end local v6    # "threadStackTraces":Ljava/util/Map;, "Ljava/util/Map<Ljava/lang/Thread;[Ljava/lang/StackTraceElement;>;"
    .restart local v0    # "br":Ljava/io/BufferedWriter;
    .restart local v9    # "writer":Ljava/io/FileWriter;
    :cond_bb
    :try_start_bb
    sget v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    add-int/lit16 v10, v10, 0x1388

    sput v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->numberOfTraces:I

    .line 201
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

    .line 203
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->flush()V

    .line 204
    invoke-virtual {v0}, Ljava/io/BufferedWriter;->close()V

    .line 205
    invoke-virtual {v9}, Ljava/io/FileWriter;->close()V
    :try_end_e4
    .catch Ljava/lang/IndexOutOfBoundsException; {:try_start_bb .. :try_end_e4} :catch_5d
    .catch Ljava/lang/Exception; {:try_start_bb .. :try_end_e4} :catch_ea

    .line 223
    .end local v0    # "br":Ljava/io/BufferedWriter;
    .end local v9    # "writer":Ljava/io/FileWriter;
    :cond_e4
    :goto_e4
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->traces:Ljava/util/Set;

    invoke-interface {v10}, Ljava/util/Set;->clear()V

    .line 224
    return-void

    .line 217
    :catch_ea
    move-exception v1

    .line 218
    .local v1, "e":Ljava/lang/Exception;
    sget-object v10, Lde/uni_passau/fim/auermich/tracer/Tracer;->LOGGER:Ljava/util/logging/Logger;

    const-string v11, "Writing traces.txt to external storage failed."

    invoke-virtual {v10, v11}, Ljava/util/logging/Logger;->info(Ljava/lang/String;)V

    .line 219
    invoke-virtual {v1}, Ljava/lang/Exception;->printStackTrace()V

    goto :goto_e4
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
