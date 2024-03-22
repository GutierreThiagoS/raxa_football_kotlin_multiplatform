package framework.animation
/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import android.os.SystemProperties

/**
 * This custom, static handler handles the timing pulse that is shared by all active
 * ValueAnimators. This approach ensures that the setting of animation values will happen on the
 * same thread that animations start on, and that all animations will share the same times for
 * calculating their values, which makes synchronizing animations possible.
 *
 * The handler uses the Choreographer by default for doing periodic callbacks. A custom
 * AnimationFrameCallbackProvider can be set on the handler to provide timing pulse that
 * may be independent of UI frame update. This could be useful in testing.
 *
 * @hide
 */
class AnimationHandler {
    /**
     * Internal per-thread collections used to avoid set collisions as animations start and end
     * while being processed.
     */
    private val mDelayedCallbackStartTime = HashMap<AnimationFrameCallback, Long>()
    private val mAnimationCallbacks = ArrayList<AnimationFrameCallback>()
    private val mCommitCallbacks = ArrayList<AnimationFrameCallback>()
    private var mProvider: AnimationFrameCallbackProvider? = null

    /**
     * This paused list is used to store animators forcibly paused when the activity
     * went into the background (to avoid unnecessary background processing work).
     * These animators should be resume()'d when the activity returns to the foreground.
     */
    private val mPausedAnimators: ArrayList<android.animation.Animator> =
        ArrayList<android.animation.Animator>()

    /**
     * This structure is used to store the currently active objects (ViewRootImpls or
     * WallpaperService.Engines) in the process. Each of these objects sends a request to
     * AnimationHandler when it goes into the background (request to pause) or foreground
     * (request to resume). Because all animators are managed by AnimationHandler on the same
     * thread, it should only ever pause animators when *all* requestors are in the background.
     * This list tracks the background/foreground state of all requestors and only ever
     * pauses animators when all items are in the background (false). To simplify, we only ever
     * store visible (foreground) requestors; if the set size reaches zero, there are no
     * objects in the foreground and it is time to pause animators.
     */
    private val mAnimatorRequestors: ArrayList<WeakReference<Any>> =
        ArrayList<java.lang.ref.WeakReference<Any>>()
    private val mFrameCallback: FrameCallback = object : FrameCallback() {
        override fun doFrame(frameTimeNanos: Long) {
            doAnimationFrame(provider.frameTime)
            if (mAnimationCallbacks.size > 0) {
                provider.postFrameCallback(this)
            }
        }
    }
    private var mListDirty = false
    private fun requestAnimatorsEnabledImpl(enable: Boolean, requestor: Any) {
        val wasEmpty: Boolean = mAnimatorRequestors.isEmpty()
        setAnimatorPausingEnabled(isPauseBgAnimationsEnabledInSystemProperties)
        synchronized(mAnimatorRequestors) {
            // Only store WeakRef objects to avoid leaks
            if (enable) {
                // First, check whether such a reference is already on the list
                var weakRef: java.lang.ref.WeakReference<Any?>? = null
                for (i in mAnimatorRequestors.indices.reversed()) {
                    val ref: java.lang.ref.WeakReference<Any?> = mAnimatorRequestors.get(i)
                    val referent: Any = ref.get()
                    if (referent === requestor) {
                        weakRef = ref
                    } else if (referent == null) {
                        // Remove any reference that has been cleared
                        mAnimatorRequestors.removeAt(i)
                    }
                }
                if (weakRef == null) {
                    weakRef = java.lang.ref.WeakReference<Any>(requestor)
                    mAnimatorRequestors.add(weakRef)
                }
            } else {
                for (i in mAnimatorRequestors.indices.reversed()) {
                    val ref: java.lang.ref.WeakReference<Any> = mAnimatorRequestors.get(i)
                    val referent: Any = ref.get()
                    if (referent === requestor || referent == null) {
                        // remove requested item or item that has been cleared
                        mAnimatorRequestors.removeAt(i)
                    }
                }
                // If a reference to the requestor wasn't in the list, nothing to remove
            }
        }
        if (!sAnimatorPausingEnabled) {
            // Resume any animators that have been paused in the meantime, otherwise noop
            // Leave logic above so that if pausing gets re-enabled, the state of the requestors
            // list is valid
            resumeAnimators()
            return
        }
        val isEmpty: Boolean = mAnimatorRequestors.isEmpty()
        if (wasEmpty != isEmpty) {
            // only paused/resume animators if there was a visibility change
            if (!isEmpty) {
                // If any requestors are enabled, resume currently paused animators
                resumeAnimators()
            } else {
                // Wait before pausing to avoid thrashing animator state for temporary backgrounding
                Choreographer.getInstance().postFrameCallbackDelayed(
                    mPauser,
                    android.animation.Animator.getBackgroundPauseDelay()
                )
            }
        }
        if (LOCAL_LOGV) {
            android.util.Log.v(
                TAG, (if (enable) "enable" else "disable") + " animators for " + requestor
                        + " with pauseDelay of " + android.animation.Animator.getBackgroundPauseDelay()
            )
            for (i in mAnimatorRequestors.indices) {
                android.util.Log.v(
                    TAG, "animatorRequestors " + i + " = "
                            + mAnimatorRequestors.get(i) + " with referent "
                            + mAnimatorRequestors.get(i).get()
                )
            }
        }
    }

    private fun resumeAnimators() {
        Choreographer.getInstance().removeFrameCallback(mPauser)
        for (i in mPausedAnimators.indices.reversed()) {
            mPausedAnimators.get(i).resume()
        }
        mPausedAnimators.clear()
    }

    private val mPauser: FrameCallback = FrameCallback { frameTimeNanos: Long ->
        if (mAnimatorRequestors.size > 0) {
            // something enabled animators since this callback was scheduled - bail
            return@FrameCallback
        }
        for (i in mAnimationCallbacks.indices) {
            val callback: AnimationFrameCallback = mAnimationCallbacks.get(i)
            if (callback is android.animation.Animator) {
                val animator: android.animation.Animator = callback as android.animation.Animator
                if (animator.getTotalDuration() == android.animation.Animator.DURATION_INFINITE
                    && !animator.isPaused()
                ) {
                    mPausedAnimators.add(animator)
                    animator.pause()
                }
            }
        }
    }
    private var provider: AnimationFrameCallbackProvider?
        private get() {
            if (mProvider == null) {
                mProvider = MyFrameCallbackProvider()
            }
            return mProvider
        }
        /**
         * By default, the Choreographer is used to provide timing for frame callbacks. A custom
         * provider can be used here to provide different timing pulse.
         */
        set(provider) {
            mProvider = provider ?: MyFrameCallbackProvider()
        }

    /**
     * Register to get a callback on the next frame after the delay.
     */
    fun addAnimationFrameCallback(callback: AnimationFrameCallback?, delay: Long) {
        if (mAnimationCallbacks.size == 0) {
            provider!!.postFrameCallback(mFrameCallback)
        }
        if (!mAnimationCallbacks.contains(callback)) {
            mAnimationCallbacks.add(callback)
        }
        if (delay > 0) {
            mDelayedCallbackStartTime.put(callback, android.os.SystemClock.uptimeMillis() + delay)
        }
    }

    /**
     * Register to get a one shot callback for frame commit timing. Frame commit timing is the
     * time *after* traversals are done, as opposed to the animation frame timing, which is
     * before any traversals. This timing can be used to adjust the start time of an animation
     * when expensive traversals create big delta between the animation frame timing and the time
     * that animation is first shown on screen.
     *
     * Note this should only be called when the animation has already registered to receive
     * animation frame callbacks. This callback will be guaranteed to happen *after* the next
     * animation frame callback.
     */
    fun addOneShotCommitCallback(callback: AnimationFrameCallback?) {
        if (!mCommitCallbacks.contains(callback)) {
            mCommitCallbacks.add(callback)
        }
    }

    /**
     * Removes the given callback from the list, so it will no longer be called for frame related
     * timing.
     */
    fun removeCallback(callback: AnimationFrameCallback?) {
        mCommitCallbacks.remove(callback)
        mDelayedCallbackStartTime.remove(callback)
        val id: Int = mAnimationCallbacks.indexOf(callback)
        if (id >= 0) {
            mAnimationCallbacks.set(id, null)
            mListDirty = true
        }
    }

    private fun doAnimationFrame(frameTime: Long) {
        val currentTime: Long = android.os.SystemClock.uptimeMillis()
        val size: Int = mAnimationCallbacks.size
        for (i in 0 until size) {
            val callback: AnimationFrameCallback = mAnimationCallbacks.get(i) ?: continue
            if (isCallbackDue(callback, currentTime)) {
                callback.doAnimationFrame(frameTime)
                if (mCommitCallbacks.contains(callback)) {
                    provider!!.postCommitCallback(object : java.lang.Runnable() {
                        override fun run() {
                            commitAnimationFrame(callback, provider.frameTime)
                        }
                    })
                }
            }
        }
        cleanUpList()
    }

    private fun commitAnimationFrame(callback: AnimationFrameCallback, frameTime: Long) {
        if (!mDelayedCallbackStartTime.containsKey(callback) &&
            mCommitCallbacks.contains(callback)
        ) {
            callback.commitAnimationFrame(frameTime)
            mCommitCallbacks.remove(callback)
        }
    }

    /**
     * Remove the callbacks from mDelayedCallbackStartTime once they have passed the initial delay
     * so that they can start getting frame callbacks.
     *
     * @return true if they have passed the initial delay or have no delay, false otherwise.
     */
    private fun isCallbackDue(callback: AnimationFrameCallback, currentTime: Long): Boolean {
        val startTime: Long = mDelayedCallbackStartTime.get(callback) ?: return true
        if (startTime < currentTime) {
            mDelayedCallbackStartTime.remove(callback)
            return true
        }
        return false
    }

    fun autoCancelBasedOn(objectAnimator: ObjectAnimator) {
        for (i in mAnimationCallbacks.indices.reversed()) {
            val cb: AnimationFrameCallback = mAnimationCallbacks.get(i) ?: continue
            if (objectAnimator.shouldAutoCancel(cb)) {
                (mAnimationCallbacks.get(i) as android.animation.Animator).cancel()
            }
        }
    }

    private fun cleanUpList() {
        if (mListDirty) {
            for (i in mAnimationCallbacks.indices.reversed()) {
                if (mAnimationCallbacks.get(i) == null) {
                    mAnimationCallbacks.removeAt(i)
                }
            }
            mListDirty = false
        }
    }

    private val callbackSize: Int
        private get() {
            var count = 0
            val size: Int = mAnimationCallbacks.size
            for (i in size - 1 downTo 0) {
                if (mAnimationCallbacks.get(i) != null) {
                    count++
                }
            }
            return count
        }

    /**
     * Default provider of timing pulse that uses Choreographer for frame callbacks.
     */
    private inner class MyFrameCallbackProvider : AnimationFrameCallbackProvider {
        val mChoreographer: Choreographer = Choreographer.getInstance()
        override fun postFrameCallback(callback: FrameCallback?) {
            mChoreographer.postFrameCallback(callback)
        }

        override fun postCommitCallback(runnable: java.lang.Runnable?) {
            mChoreographer.postCallback(Choreographer.CALLBACK_COMMIT, runnable, null)
        }

        override val frameTime: Long
            get() = mChoreographer.getFrameTime()
        override var frameDelay: Long
            get() = Choreographer.getFrameDelay()
            set(delay) {
                Choreographer.setFrameDelay(delay)
            }
    }

    /**
     * Callbacks that receives notifications for animation timing and frame commit timing.
     * @hide
     */
    interface AnimationFrameCallback {
        /**
         * Run animation based on the frame time.
         * @param frameTime The frame start time, in the [SystemClock.uptimeMillis] time
         * base.
         * @return if the animation has finished.
         */
        fun doAnimationFrame(frameTime: Long): Boolean

        /**
         * This notifies the callback of frame commit time. Frame commit time is the time after
         * traversals happen, as opposed to the normal animation frame time that is before
         * traversals. This is used to compensate expensive traversals that happen as the
         * animation starts. When traversals take a long time to complete, the rendering of the
         * initial frame will be delayed (by a long time). But since the startTime of the
         * animation is set before the traversal, by the time of next frame, a lot of time would
         * have passed since startTime was set, the animation will consequently skip a few frames
         * to respect the new frameTime. By having the commit time, we can adjust the start time to
         * when the first frame was drawn (after any expensive traversals) so that no frames
         * will be skipped.
         *
         * @param frameTime The frame time after traversals happen, if any, in the
         * [SystemClock.uptimeMillis] time base.
         */
        fun commitAnimationFrame(frameTime: Long)
    }

    /**
     * The intention for having this interface is to increase the testability of ValueAnimator.
     * Specifically, we can have a custom implementation of the interface below and provide
     * timing pulse without using Choreographer. That way we could use any arbitrary interval for
     * our timing pulse in the tests.
     *
     * @hide
     */
    interface AnimationFrameCallbackProvider {
        fun postFrameCallback(callback: FrameCallback?)
        fun postCommitCallback(runnable: java.lang.Runnable?)
        val frameTime: Long
        var frameDelay: Long
    }

    companion object {
        private const val TAG = "AnimationHandler"
        private const val LOCAL_LOGV = false

        // Static flag which allows the pausing behavior to be globally disabled/enabled.
        private var sAnimatorPausingEnabled =
            isPauseBgAnimationsEnabledInSystemProperties

        // Static flag which prevents the system property from overriding sAnimatorPausingEnabled field.
        private var sOverrideAnimatorPausingSystemProperty = false
        val sAnimatorHandler: java.lang.ThreadLocal<AnimationHandler> =
            java.lang.ThreadLocal<AnimationHandler>()
        val instance: AnimationHandler
            get() {
                if (sAnimatorHandler.get() == null) {
                    sAnimatorHandler.set(AnimationHandler())
                }
                return sAnimatorHandler.get()
            }
        private val isPauseBgAnimationsEnabledInSystemProperties: Boolean
            /**
             * System property that controls the behavior of pausing infinite animators when an app
             * is moved to the background.
             *
             * @return the value of 'framework.pause_bg_animations.enabled' system property
             */
            private get() = if (sOverrideAnimatorPausingSystemProperty) sAnimatorPausingEnabled else SystemProperties
                .getBoolean("framework.pause_bg_animations.enabled", true)

        /**
         * Disable the default behavior of pausing infinite animators when
         * apps go into the background.
         *
         * @param enable Enable (default behavior) or disable background pausing behavior.
         */
        fun setAnimatorPausingEnabled(enable: Boolean) {
            sAnimatorPausingEnabled = enable
        }

        /**
         * Prevents the setAnimatorPausingEnabled behavior from being overridden
         * by the 'framework.pause_bg_animations.enabled' system property value.
         *
         * This is for testing purposes only.
         *
         * @param enable Enable or disable (default behavior) overriding the system
         * property.
         */
        fun setOverrideAnimatorPausingSystemProperty(enable: Boolean) {
            sOverrideAnimatorPausingSystemProperty = enable
        }

        /**
         * This is called when a window goes away. We should remove
         * it from the requestors list to ensure that we are counting requests correctly and not
         * tracking obsolete+enabled requestors.
         */
        fun removeRequestor(requestor: Any) {
            instance.requestAnimatorsEnabledImpl(false, requestor)
            if (LOCAL_LOGV) {
                android.util.Log.v(TAG, "removeRequestor for $requestor")
            }
        }

        /**
         * This method is called from ViewRootImpl or WallpaperService when either a window is no
         * longer visible (enable == false) or when a window becomes visible (enable == true).
         * If animators are not properly disabled when activities are backgrounded, it can lead to
         * unnecessary processing, particularly for infinite animators, as the system will continue
         * to pulse timing events even though the results are not visible. As a workaround, we
         * pause all un-paused infinite animators, and resume them when any window in the process
         * becomes visible.
         */
        fun requestAnimatorsEnabled(enable: Boolean, requestor: Any) {
            instance.requestAnimatorsEnabledImpl(enable, requestor)
        }

        val animationCount: Int
            /**
             * Return the number of callbacks that have registered for frame callbacks.
             */
            get() {
                val handler: AnimationHandler =
                    sAnimatorHandler.get() ?: return 0
                return handler.callbackSize
            }
        var frameDelay: Long
            get() = instance.provider!!.frameDelay
            set(delay) {
                instance.provider!!.frameDelay = delay
            }
    }
}
