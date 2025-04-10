/*
 * Copyright (c) 2004-2022, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors 
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hisp.dhis.scheduling;

import static java.util.stream.Collectors.toUnmodifiableSet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hisp.dhis.common.OpenApi;
import org.hisp.dhis.feedback.ErrorCode;
import org.hisp.dhis.tracker.imports.validation.ValidationCode;
import org.slf4j.helpers.MessageFormatter;

/**
 *
 *
 * <h3>Tracking</h3>
 *
 * The {@link JobProgress} API is mainly contains methods to track the progress of long-running jobs
 * on three levels:
 *
 * <ol>
 *   <li>Process: Outermost bracket around the entire work done by a job
 *   <li>Stage: A logical step within the entire process of the job. A process is a strict sequence
 *       of stages. Stages do not run in parallel.
 *   <li>(Work) Item: Performing an "non-interruptable" unit of work within a stage. Items can be
 *       processed in parallel or strictly sequential. Usually this is the function called in some
 *       form of a loop.
 * </ol>
 *
 * For each of the three levels a new node is announced up front by calling the corresponding {@code
 * startingProcess}, {@code startingStage} or {@code startingWorkItem} method.
 *
 * <p>The process will now expect a corresponding completion, for example {@code completedWorkItem}
 * in case of success or {@code failedWorkItem} in case of an error. The different {@link
 * #runStage(Stream, Function, Consumer)} or {@link #runStageInParallel(int, Collection, Function,
 * Consumer)} helpers can be used to do the error handling correctly and make sure the work items
 * are completed in both success and failure scenarios.
 *
 * <p>For stages that do not have work items {@link #runStage(Runnable)} and {@link
 * #runStage(Object, Callable)} can be used to make sure completion is handled correctly.
 *
 * <p>For stages with work items the number of items should be announced using {@link
 * #startingStage(String, int)}. This is a best-effort estimation of the actual items to allow
 * observers a better understanding how much progress has been made and how much work is left to do.
 * For stages where this is not known up-front the estimation is given as -1.
 *
 * <h3>Flow-Control</h3>
 *
 * The second part of the {@link JobProgress} is control flow. This is all based on a single method
 * {@link #isCancelled()}. The coordination is cooperative. This means cancellation of the running
 * process might be requested externally at any point or as a consequence of a failing work item.
 * This would flip the state returned by {@link #isCancelled()} which is/should be checked before
 * starting a new stage or work item.
 *
 * <p>A process should only continue starting new work as long as cancellation is not requested.
 * When cancellation is requested ongoing work items are finished and the process exists
 * cooperatively by not starting any further work.
 *
 * <p>When a stage is cancelled the run-methods usually return false to give caller a chance to
 * react if needed. The next call to {@code startingStage} will then throw a {@link
 * CancellationException} and thereby short-circuit the rest of the process.
 *
 * @author Jan Bernitt
 */
public interface JobProgress {

  /**
   * Main use case are tests and use as NULL object for the tracker progress delegate.
   *
   * @return A {@link JobProgress} that literally does nothing, this includes any form of abort or
   *     cancel logic
   */
  static JobProgress noop() {
    return NoopJobProgress.INSTANCE;
  }

  /*
   * Flow Control API:
   */

  /**
   * OBS! Should only be called after the task is complete and no further progress is tracked.
   *
   * @return true, if all processes in this tracking object were successful.
   */
  default boolean isSuccessful() {
    return true;
  }

  /**
   * @return true, if the job got cancelled and requests the processing thread to terminate, else
   *     false to continue processing the job
   */
  default boolean isCancelled() {
    return false;
  }

  default boolean isAborted() {
    return false;
  }

  /**
   * Note that this indication resets to false once another stage is started.
   *
   * @return true, if the currently running stage should be skipped. By default, this is only the
   *     case if cancellation was requested.
   */
  default boolean isSkipCurrentStage() {
    return isCancelled();
  }

  /*
  Error reporting API:
  */

  default void addError(
      @Nonnull ErrorCode code, @CheckForNull String uid, @Nonnull String type, String... args) {
    addError(code, uid, type, List.of(args));
  }

  default void addError(
      @Nonnull ValidationCode code,
      @CheckForNull String uid,
      @Nonnull String type,
      String... args) {
    addError(code, uid, type, List.of(args));
  }

  default void addError(
      @Nonnull ErrorCode code,
      @CheckForNull String uid,
      @Nonnull String type,
      @Nonnull List<String> args) {
    // is overridden by a tracker that collects errors
    // default is to not collect errors
  }

  default void addError(
      @Nonnull ValidationCode code,
      @CheckForNull String uid,
      @Nonnull String type,
      @Nonnull List<String> args) {
    // is overridden by a tracker that collects errors
    // default is to not collect errors
  }

  /*
   * Tracking API:
   */

  void startingProcess(@CheckForNull String description, Object... args);

  default void startingProcess() {
    startingProcess(null);
  }

  void completedProcess(@CheckForNull String summary, Object... args);

  void failedProcess(@CheckForNull String error, Object... args);

  default void failedProcess(@Nonnull Exception cause) {
    failedProcess("Process failed: {}", getMessage(cause));
  }

  default void endingProcess(boolean success) {
    if (success) {
      completedProcess(null);
    } else {
      failedProcess((String) null);
    }
  }

  /**
   * Announce start of a new stage.
   *
   * @param description describes the work done
   * @param workItems number of work items in the stage, -1 if unknown
   * @param onFailure what to do should the stage or one of its items fail
   * @throws CancellationException in case cancellation has been requested before this stage had
   *     started
   */
  void startingStage(@Nonnull String description, int workItems, @Nonnull FailurePolicy onFailure)
      throws CancellationException;

  /**
   * Should be called after a {@link #runStage(Callable)} or {@link #runStage(Object, Callable)} or
   * on of the other variants in case the returned value may be null but never should be null in
   * order to be able to continue the process.
   *
   * @param value a value returned by a {@code runStage} method that might be null
   * @return the same value but only if it is non-null
   * @param <T> type of the checked value
   * @throws CancellationException in case the value is null, this is similar to starting the
   *     cancellation based exception that would occur by starting the next stage except that it
   *     also has a message indicating that a failed post condition was the cause
   */
  @Nonnull
  default <T> T nonNullStagePostCondition(@CheckForNull T value) throws CancellationException {
    if (value == null) throw new CancellationException("Post-condition was null");
    return value;
  }

  default void startingStage(@Nonnull String description, int workItems) {
    startingStage(description, workItems, FailurePolicy.PARENT);
  }

  default void startingStage(@Nonnull String description, @Nonnull FailurePolicy onFailure) {
    startingStage(description, 0, onFailure);
  }

  default void startingStage(@Nonnull String description, Object... args) {
    startingStage(format(description, args), FailurePolicy.PARENT);
  }

  void completedStage(@CheckForNull String summary, Object... args);

  void failedStage(@Nonnull String error, Object... args);

  default void failedStage(@Nonnull Exception cause) {
    failedStage(getMessage(cause));
  }

  /**
   * @since 2.42
   * @param size number of work items to join into one entry
   */
  default void setWorkItemBucketing(int size) {
    // by default this is not supported and no bucketing will occur
  }

  default void startingWorkItem(@Nonnull String description, Object... args) {
    startingWorkItem(format(description, args), FailurePolicy.PARENT);
  }

  void startingWorkItem(@Nonnull String description, @Nonnull FailurePolicy onFailure);

  default void startingWorkItem(int i) {
    startingWorkItem("#" + (i + 1));
  }

  void completedWorkItem(@CheckForNull String summary, Object... args);

  void failedWorkItem(@Nonnull String error, Object... args);

  default void failedWorkItem(@Nonnull Exception cause) {
    failedWorkItem(getMessage(cause));
  }

  /*
   * Running work items within a stage
   */

  /**
   * Runs {@link Runnable} work items as sequence.
   *
   * @param items the work items to run in the sequence to run them
   * @see #runStage(Collection, Function, Consumer)
   */
  default void runStage(@Nonnull Collection<Runnable> items) {
    runStage(items, item -> null, Runnable::run);
  }

  /**
   * Runs {@link Runnable} work items as sequence.
   *
   * @param items the work items to run in the sequence to run them with the keys used as item
   *     description. Items are processed in map iteration order.
   * @see #runStage(Collection, Function, Consumer)
   */
  default void runStage(@Nonnull Map<String, Runnable> items) {
    runStage(items.entrySet(), Entry::getKey, entry -> entry.getValue().run());
  }

  /**
   * Run work items as sequence using a {@link Collection} of work item inputs and an execution work
   * {@link Consumer} function.
   *
   * @param items the work item inputs to run in the sequence to run them
   * @param description function to extract a description for a work item, may return {@code null}
   * @param work function to execute the work of a single work item input
   * @param <T> type of work item input
   * @see #runStage(Collection, Function, Consumer)
   */
  default <T> void runStage(
      @Nonnull Collection<T> items,
      @Nonnull Function<T, String> description,
      @Nonnull Consumer<T> work) {
    runStage(items.stream(), description, work);
  }

  /**
   * Run work items as sequence using a {@link Stream} of work item inputs and an execution work
   * {@link Consumer} function.
   *
   * @see #runStage(Stream, Function, Consumer,BiFunction)
   */
  default <T> void runStage(
      @Nonnull Stream<T> items,
      @Nonnull Function<T, String> description,
      @Nonnull Consumer<T> work) {
    runStage(
        items,
        description,
        work,
        (success, failed) -> format("{} successful and {} failed items", success, failed));
  }

  /**
   * Run work items as sequence using a {@link Stream} of work item inputs and an execution work
   * {@link Consumer} function.
   *
   * <p>
   *
   * @param items stream of inputs to execute a work item
   * @param description function to extract a description for a work item, may return {@code null}
   * @param work function to execute the work of a single work item input
   * @param summary accepts number of successful and failed items to compute a summary, may return
   *     {@code null}
   * @param <T> type of work item input
   */
  default <T> void runStage(
      @Nonnull Stream<T> items,
      @Nonnull Function<T, String> description,
      @Nonnull Consumer<T> work,
      @CheckForNull BiFunction<Integer, Integer, String> summary) {
    runStage(
        items,
        description,
        null,
        item -> {
          work.accept(item);
          return item;
        },
        summary);
  }

  /**
   * Run work items as sequence using a {@link Stream} of work item inputs and an execution work
   * {@link Consumer} function.
   *
   * <p>
   *
   * @param items stream of inputs to execute a work item
   * @param description function to extract a description for a work item, may return {@code null}
   * @param result function to extract a result summary for a successful work item, may return
   *     {@code null}
   * @param work function to execute the work of a single work item input
   * @param summary accepts number of successful and failed items to compute a summary, may return
   *     {@code null}
   * @param <T> type of work item input
   */
  default <T, R> void runStage(
      @Nonnull Stream<T> items,
      @Nonnull Function<T, String> description,
      @CheckForNull Function<R, String> result,
      @Nonnull Function<T, R> work,
      @CheckForNull BiFunction<Integer, Integer, String> summary) {
    int i = 0;
    int failed = 0;
    for (Iterator<T> it = items.iterator(); it.hasNext(); ) {
      T item = it.next();
      // check for async cancel
      if (autoSkipStage(summary, i - failed, failed)) {
        return; // ends the stage immediately
      }
      String desc = description.apply(item);
      if (desc == null) {
        startingWorkItem(i);
      } else {
        startingWorkItem(desc);
      }
      i++;
      try {
        R res = work.apply(item);
        completedWorkItem(result == null ? null : result.apply(res));
      } catch (RuntimeException ex) {
        failed++;
        failedWorkItem(ex);
        if (autoSkipStage(summary, i - failed, failed)) {
          return; // ends the stage immediately
        }
      }
    }
    completedStage(summary == null ? null : summary.apply(i - failed, failed));
  }

  /**
   * Automatically complete a stage as failed based on the {@link #isSkipCurrentStage()} state.
   *
   * <p>This completes the stage either with a {@link CancellationException} in case {@link
   * #isCancelled()} is true, or with just a summary text if it is false.
   *
   * @param summary optional callback to produce a summary
   * @param success number of successful items
   * @param failed number of failed items
   * @return true, if stage is/was skipped (complected as failed), false otherwise
   */
  default boolean autoSkipStage(
      @CheckForNull BiFunction<Integer, Integer, String> summary, int success, int failed) {
    if (isSkipCurrentStage()) {
      String text = summary == null ? "" : summary.apply(success, failed);
      if (isCancelled()) {
        failedStage(new CancellationException("skipped stage, failing item caused abort. " + text));
      } else {
        failedStage("skipped stage. " + text);
      }
      return true;
    }
    return false;
  }

  /**
   * @see #runStage(Function, Runnable)
   */
  default boolean runStage(@Nonnull Runnable work) {
    return runStage(null, work);
  }

  /**
   * Run a stage with no individual work items but a single work {@link Runnable} with proper
   * completion wrapping.
   *
   * <p>If the work task throws an {@link Exception} the stage is considered failed otherwise it is
   * considered complete when done.
   *
   * @param summary used when stage completes successfully (return value to summary)
   * @param work work for the entire stage
   * @return true, if completed successful, false if completed exceptionally
   */
  default boolean runStage(
      @CheckForNull Function<Boolean, String> summary, @Nonnull Runnable work) {
    return runStage(
        false,
        summary,
        () -> {
          work.run();
          return true;
        });
  }

  @CheckForNull
  default <T> T runStage(@Nonnull Callable<T> work) {
    return runStage(null, work);
  }

  /**
   * @see #runStage(Object, Function, Callable)
   */
  default <T> T runStage(@CheckForNull T errorValue, @Nonnull Callable<T> work) {
    return runStage(errorValue, null, work);
  }

  /**
   * Run a stage with no individual work items but a single work {@link Runnable} with proper
   * completion wrapping.
   *
   * <p>If the work task throws an {@link Exception} the stage is considered failed otherwise it is
   * considered complete when done.
   *
   * @param errorValue the value returned in case the work throws an {@link Exception}
   * @param summary used when stage completes successfully (return value to summary)
   * @param work work for the entire stage
   * @return the value returned by work task when successful or the errorValue in case the task
   *     threw an {@link Exception}
   */
  default <T> T runStage(
      @CheckForNull T errorValue,
      @CheckForNull Function<T, String> summary,
      @Nonnull Callable<T> work) {
    try {
      T res = work.call();
      completedStage(summary == null ? null : summary.apply(res));
      return res;
    } catch (Exception ex) {
      failedStage(ex);
      return errorValue;
    }
  }

  /**
   * Runs the work items of a stage with the given parallelism. At most a parallelism equal to the
   * number of available processor cores is used.
   *
   * <p>If the parallelism is smaller or equal to 1 the items are processed sequentially using
   * {@link #runStage(Collection, Function, Consumer)}.
   *
   * <p>While the items are processed in parallel this method is synchronous for the caller and will
   * first return when all work is done.
   *
   * <p>If cancellation is requested work items might be skipped entirely.
   *
   * @param parallelism number of items that at maximum should be processed in parallel
   * @param items work item inputs to be processed in parallel
   * @param description function to extract a description for a work item, may return {@code null}
   * @param work function to execute the work of a single work item input
   * @param <T> type of work item input
   */
  default <T> void runStageInParallel(
      int parallelism,
      @Nonnull Collection<T> items,
      @Nonnull Function<T, String> description,
      @Nonnull Consumer<T> work) {
    if (parallelism <= 1) {
      runStage(items, description, work);
      return;
    }
    AtomicInteger success = new AtomicInteger();
    AtomicInteger failed = new AtomicInteger();

    Callable<Boolean> task =
        () ->
            items.parallelStream()
                .map(
                    item -> {
                      if (isSkipCurrentStage()) {
                        return false;
                      }
                      startingWorkItem(description.apply(item));
                      try {
                        work.accept(item);
                        completedWorkItem(null);
                        success.incrementAndGet();
                        return true;
                      } catch (Exception ex) {
                        failedWorkItem(ex);
                        failed.incrementAndGet();
                        return false;
                      }
                    })
                .reduce(Boolean::logicalAnd)
                .orElse(false);

    ForkJoinPool pool = new ForkJoinPool(parallelism);
    try {
      // this might not be obvious but running a parallel stream
      // as task in a FJP makes the stream use the pool
      boolean allSuccessful = pool.submit(task).get();
      if (allSuccessful) {
        completedStage(null);
      } else {
        autoSkipStage(
            (s, f) ->
                format("parallel processing aborted after {} successful and {} failed items", s, f),
            success.get(),
            failed.get());
      }
    } catch (InterruptedException ex) {
      failedStage(ex);
      Thread.currentThread().interrupt();
    } catch (Exception ex) {
      failedStage(ex);
    } finally {
      pool.shutdown();
    }
  }

  /**
   * Returns a formatted string, or null if the given format is null.
   *
   * @param pattern the pattern string.
   * @param args the pattern arguments.
   * @return a formatted message string.
   */
  default String format(@CheckForNull String pattern, Object... args) {
    return pattern != null ? MessageFormatter.arrayFormat(pattern, args).getMessage() : null;
  }

  /*
   * Model (for representing progress as data)
   */

  @OpenApi.Shared(name = "JobProgressStatus")
  enum Status {
    RUNNING,
    SUCCESS,
    ERROR,
    CANCELLED
  }

  /**
   * How to behave when an item or stage fails. By default, a failure means the process is aborted.
   * Using a {@link FailurePolicy} allows to customize this behavior on a stage or item basis.
   *
   * <p>The implementation of {@link FailurePolicy} is done by affecting {@link
   * #isSkipCurrentStage()} and {@link #isCancelled()} accordingly after the failure occurred and
   * has been tracked using one of the {@code failedStage} or {@code failedWorkItem} methods.
   */
  enum FailurePolicy {
    /**
     * Default used to "inherit" the behavior from the node level above. If the root is not
     * specified the behavior is {@link #FAIL}.
     */
    PARENT,
    /** Fail and abort processing as soon as possible. This is the effective default. */
    FAIL,
    /**
     * When an item or stage fails the entire stage is skipped/ignored unconditionally. This means
     * no further items are processed in the stage.
     */
    SKIP_STAGE,
    /** When an item fails it is simply skipped/ignored unconditionally. */
    SKIP_ITEM,
    /**
     * Same as {@link #SKIP_ITEM} but only if there has been a successfully completed item before.
     * Otherwise, behaves like {@link #FAIL}.
     *
     * <p>This option is useful to only skip when it has been proven that the processing in general
     * works but some items just have issues.
     */
    SKIP_ITEM_OUTLIER
  }

  @Getter
  final class Progress {

    @Nonnull @JsonProperty final Deque<Process> sequence;

    @Nonnull
    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final Map<String, Map<String, Queue<Error>>> errors;

    public Progress() {
      this.sequence = new ConcurrentLinkedDeque<>();
      this.errors = new ConcurrentHashMap<>();
    }

    @JsonCreator
    public Progress(
        @Nonnull @JsonProperty("sequence") Deque<Process> sequence,
        @CheckForNull @JsonProperty("errors") Map<String, Map<String, Queue<Error>>> errors) {
      this.sequence = sequence;
      this.errors = errors == null ? Map.of() : errors;
    }

    public void addError(Error error) {
      Queue<Error> sameObjectAndCode =
          errors
              .computeIfAbsent(error.getId(), key -> new ConcurrentHashMap<>())
              .computeIfAbsent(error.getCode(), key2 -> new ConcurrentLinkedQueue<>());
      if (sameObjectAndCode.stream().noneMatch(e -> e.args.equals(error.args))) {
        sameObjectAndCode.add(error);
      }
    }

    public Progress withoutErrors() {
      return new Progress(sequence, Map.of());
    }

    public boolean hasErrors() {
      return !errors.isEmpty();
    }

    public Set<String> getErrorCodes() {
      return errors.values().stream()
          .flatMap(e -> e.keySet().stream())
          .collect(toUnmodifiableSet());
    }
  }

  @Getter
  @Accessors(chain = true)
  final class Error {

    @Nonnull @JsonProperty private final String code;

    /** The object that has the error */
    @Nonnull @JsonProperty private final String id;

    /** The type of the object identified by #id that has the error */
    @Nonnull @JsonProperty private final String type;

    /** The arguments used in the {@link #code}'s {@link ErrorCode#getMessage()} template */
    @Nonnull @JsonProperty private final List<String> args;

    /**
     * The message as created from {@link #code} and {@link #args}. This is only set in service
     * layer for the web API using the setter, it is not persisted.
     */
    @Setter
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String message;

    @JsonCreator
    public Error(
        @Nonnull @JsonProperty("code") String code,
        @Nonnull @JsonProperty("id") String id,
        @Nonnull @JsonProperty("type") String type,
        @Nonnull @JsonProperty("args") List<String> args) {
      this.code = code;
      this.id = id;
      this.type = type;
      this.args = args;
    }
  }

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor(access = AccessLevel.PROTECTED)
  abstract class Node implements Serializable {

    @JsonProperty private String error;
    @JsonProperty private String summary;
    private Exception cause;
    @JsonProperty protected Status status = Status.RUNNING;
    @JsonProperty private Date completedTime;

    @JsonProperty
    public abstract Date getStartedTime();

    @JsonProperty
    public abstract String getDescription();

    @JsonProperty
    public long getDuration() {
      return completedTime == null
          ? System.currentTimeMillis() - getStartedTime().getTime()
          : completedTime.getTime() - getStartedTime().getTime();
    }

    @JsonProperty
    public boolean isComplete() {
      return status != Status.RUNNING;
    }

    public void complete(String summary) {
      this.summary = summary;
      this.completedTime = new Date();
      if (status == Status.RUNNING) {
        this.status = Status.SUCCESS;
      }
    }

    public void completeExceptionally(String error, Exception cause) {
      this.error = error;
      this.cause = cause;
      this.completedTime = new Date();
      if (status == Status.RUNNING) {
        this.status = cause instanceof CancellationException ? Status.CANCELLED : Status.ERROR;
      }
    }
  }

  @Getter
  final class Process extends Node {
    public static Date startedTime(Collection<Process> job, Date defaultValue) {
      return job.isEmpty() ? defaultValue : job.iterator().next().getStartedTime();
    }

    private final Date startedTime;
    @JsonProperty private final String description;
    @JsonProperty private final Deque<Stage> stages;
    @Setter @JsonProperty private String jobId;
    @JsonProperty private Date cancelledTime;
    @Setter @JsonProperty private String userId;

    public Process(String description) {
      this.description = description;
      this.startedTime = new Date();
      this.stages = new ConcurrentLinkedDeque<>();
    }

    /** For recreation when de-serializing from a JSON string */
    @JsonCreator
    public Process(
        @JsonProperty("error") String error,
        @JsonProperty("summary") String summary,
        @JsonProperty("status") Status status,
        @JsonProperty("startedTime") Date startedTime,
        @JsonProperty("completedTime") Date completedTime,
        @JsonProperty("description") String description,
        @JsonProperty("stages") Deque<Stage> stages,
        @JsonProperty("jobId") String jobId,
        @JsonProperty("cancelledTime") Date cancelledTime,
        @JsonProperty("userId") String userId) {
      super(error, summary, null, status, completedTime);
      this.startedTime = startedTime;
      this.description = description;
      this.stages = stages;
      this.jobId = jobId;
      this.cancelledTime = cancelledTime;
      this.userId = userId;
    }

    public void cancel() {
      this.cancelledTime = new Date();
      this.status = Status.CANCELLED;
    }
  }

  @Getter
  final class Stage extends Node {
    private final Date startedTime;

    @JsonProperty private final String description;

    /**
     * This is the number of expected items, negative when unknown, zero when the stage has no items
     * granularity
     */
    @JsonProperty private final int totalItems;

    @JsonProperty private final FailurePolicy onFailure;
    @JsonProperty private final Deque<Item> items;

    public Stage(String description, int totalItems, FailurePolicy onFailure) {
      this.description = description;
      this.totalItems = totalItems;
      this.onFailure = onFailure;
      this.startedTime = new Date();
      this.items = new ConcurrentLinkedDeque<>();
    }

    @JsonCreator
    public Stage(
        @JsonProperty("error") String error,
        @JsonProperty("summary") String summary,
        @JsonProperty("status") Status status,
        @JsonProperty("startedTime") Date startedTime,
        @JsonProperty("completedTime") Date completedTime,
        @JsonProperty("description") String description,
        @JsonProperty("totalItems") int totalItems,
        @JsonProperty("onFailure") FailurePolicy onFailure,
        @JsonProperty("items") Deque<Item> items) {
      super(error, summary, null, status, completedTime);
      this.description = description;
      this.totalItems = totalItems;
      this.onFailure = onFailure;
      this.items = items;
      this.startedTime = startedTime;
    }
  }

  @Getter
  final class Item extends Node {
    private final Date startedTime;

    @JsonProperty private final String description;
    @JsonProperty private final FailurePolicy onFailure;

    public Item(String description, FailurePolicy onFailure) {
      this.description = description;
      this.onFailure = onFailure;
      this.startedTime = new Date();
    }

    @JsonCreator
    public Item(
        @JsonProperty("error") String error,
        @JsonProperty("summary") String summary,
        @JsonProperty("status") Status status,
        @JsonProperty("startedTime") Date startedTime,
        @JsonProperty("completedTime") Date completedTime,
        @JsonProperty("description") String description,
        @JsonProperty("onFailure") FailurePolicy onFailure) {
      super(error, summary, null, status, completedTime);
      this.startedTime = startedTime;
      this.description = description;
      this.onFailure = onFailure;
    }
  }

  @Nonnull
  static String getMessage(@Nonnull Exception cause) {
    String msg = cause.getMessage();
    return msg == null || msg.isBlank() ? cause.getClass().getName() : msg;
  }
}
