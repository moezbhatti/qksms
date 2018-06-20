/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.moez.QKSMS.common.androidxcompat;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import com.uber.autodispose.LifecycleEndedException;
import com.uber.autodispose.LifecycleScopeProvider;
import io.reactivex.Observable;
import io.reactivex.functions.Function;

/**
 * A {@link LifecycleScopeProvider} that can provide scoping for Android {@link Lifecycle} and
 * {@link LifecycleOwner} classes.
 * <p>
 * <pre><code>
 *   AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(lifecycleOwner))
 * </code></pre>
 */
public final class AndroidLifecycleScopeProvider
    implements LifecycleScopeProvider<Lifecycle.Event> {

  private static final Function<Lifecycle.Event, Lifecycle.Event> DEFAULT_CORRESPONDING_EVENTS =
      new Function<Lifecycle.Event, Lifecycle.Event>() {
        @Override public Lifecycle.Event apply(Lifecycle.Event lastEvent) throws Exception {
          switch (lastEvent) {
            case ON_CREATE:
              return Lifecycle.Event.ON_DESTROY;
            case ON_START:
              return Lifecycle.Event.ON_STOP;
            case ON_RESUME:
              return Lifecycle.Event.ON_PAUSE;
            case ON_PAUSE:
              return Lifecycle.Event.ON_STOP;
            case ON_STOP:
            case ON_DESTROY:
            default:
              throw new LifecycleEndedException("Lifecycle has ended! Last event was " + lastEvent);
          }
        }
      };

  private final Function<Lifecycle.Event, Lifecycle.Event> boundaryResolver;

  /**
   * Creates a {@link AndroidLifecycleScopeProvider} for Android LifecycleOwners.
   *
   * @param owner the owner to scope for.
   * @return a {@link AndroidLifecycleScopeProvider} against this owner.
   */
  public static AndroidLifecycleScopeProvider from(LifecycleOwner owner) {
    return from(owner.getLifecycle());
  }

  /**
   * Creates a {@link AndroidLifecycleScopeProvider} for Android LifecycleOwners.
   *
   * @param owner the owner to scope for.
   * @param untilEvent the event until the scope is valid.
   * @return a {@link AndroidLifecycleScopeProvider} against this owner.
   */
  public static AndroidLifecycleScopeProvider from(
          LifecycleOwner owner,
          Lifecycle.Event untilEvent) {
    return from(owner.getLifecycle(), untilEvent);
  }

  /**
   * Creates a {@link AndroidLifecycleScopeProvider} for Android Lifecycles.
   *
   * @param lifecycle the lifecycle to scope for.
   * @return a {@link AndroidLifecycleScopeProvider} against this lifecycle.
   */
  public static AndroidLifecycleScopeProvider from(Lifecycle lifecycle) {
    return from(lifecycle, DEFAULT_CORRESPONDING_EVENTS);
  }

  /**
   * Creates a {@link AndroidLifecycleScopeProvider} for Android Lifecycles.
   *
   * @param lifecycle the lifecycle to scope for.
   * @param untilEvent the event until the scope is valid.
   * @return a {@link AndroidLifecycleScopeProvider} against this lifecycle.
   */
  public static AndroidLifecycleScopeProvider from(
          Lifecycle lifecycle,
          Lifecycle.Event untilEvent) {
    return from(lifecycle, new UntilEventFunction(untilEvent));
  }

  /**
   * Creates a {@link AndroidLifecycleScopeProvider} for Android Lifecycles.
   *
   * @param owner the owner to scope for.
   * @param boundaryResolver function that resolves the event boundary.
   * @return a {@link AndroidLifecycleScopeProvider} against this owner.
   */
  public static AndroidLifecycleScopeProvider from(
          LifecycleOwner owner,
          Function<Lifecycle.Event, Lifecycle.Event> boundaryResolver) {
    return from(owner.getLifecycle(), boundaryResolver);
  }

  /**
   * Creates a {@link AndroidLifecycleScopeProvider} for Android Lifecycles.
   *
   * @param lifecycle the lifecycle to scope for.
   * @param boundaryResolver function that resolves the event boundary.
   * @return a {@link AndroidLifecycleScopeProvider} against this lifecycle.
   */
  public static AndroidLifecycleScopeProvider from(
          Lifecycle lifecycle,
          Function<Lifecycle.Event, Lifecycle.Event> boundaryResolver) {
    return new AndroidLifecycleScopeProvider(lifecycle, boundaryResolver);
  }

  private final LifecycleEventsObservable lifecycleObservable;

  private AndroidLifecycleScopeProvider(Lifecycle lifecycle,
                                        Function<Lifecycle.Event, Lifecycle.Event> boundaryResolver) {
    this.lifecycleObservable = new LifecycleEventsObservable(lifecycle);
    this.boundaryResolver = boundaryResolver;
  }

  @Override public Observable<Lifecycle.Event> lifecycle() {
    return lifecycleObservable;
  }

  @Override public Function<Lifecycle.Event, Lifecycle.Event> correspondingEvents() {
    return boundaryResolver;
  }

  @Override public Lifecycle.Event peekLifecycle() {
    lifecycleObservable.backfillEvents();
    return lifecycleObservable.getValue();
  }

  private static class UntilEventFunction implements Function<Lifecycle.Event, Lifecycle.Event> {
    private final Lifecycle.Event untilEvent;

    UntilEventFunction(Lifecycle.Event untilEvent) {
      this.untilEvent = untilEvent;
    }

    @Override public Lifecycle.Event apply(Lifecycle.Event event) throws Exception {
      return untilEvent;
    }
  }
}