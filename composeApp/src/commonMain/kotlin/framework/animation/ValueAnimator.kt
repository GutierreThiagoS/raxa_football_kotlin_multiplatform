package framework.animation

import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/*
* Copyright (C) 2010 The Android Open Source Project
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


/**
 * This class provides a simple timing engine for running animations
 * which calculate animated values and set them on target objects.
 *
 *
 * There is a single timing pulse that all animations use. It runs in a
 * custom handler to ensure that property changes happen on the UI thread.
 *
 *
 * By default, ValueAnimator uses non-linear time interpolation, via the
 * [AccelerateDecelerateInterpolator] class, which accelerates into and decelerates
 * out of an animation. This behavior can be changed by calling
 * [ValueAnimator.setInterpolator].
 *
 *
 * Animators can be created from either code or resource files. Here is an example
 * of a ValueAnimator resource file:
 *
 * {@sample development/samples/ApiDemos/res/anim/animator.xml ValueAnimatorResources}
 *
 *
 * Starting from API 23, it is also possible to use a combination of [PropertyValuesHolder]
 * and [Keyframe] resource tags to create a multi-step animation.
 * Note that you can specify explicit fractional values (from 0 to 1) for
 * each keyframe to determine when, in the overall duration, the animation should arrive at that
 * value. Alternatively, you can leave the fractions off and the keyframes will be equally
 * distributed within the total duration:
 *
 * {@sample development/samples/ApiDemos/res/anim/value_animator_pvh_kf.xml
 * * ValueAnimatorKeyframeResources}
 *
 * <div class="special reference">
 * <h3>Developer Guides</h3>
 *
 * For more information about animating with `ValueAnimator`, read the
 * [Property
 * Animation]({@docRoot}guide/topics/graphics/prop-animation.html#value-animator) developer guide.
</div> *
 */
open class ValueAnimator
/**
 * Creates a new ValueAnimator object. This default constructor is primarily for
 * use internally; the factory methods which take parameters are more generally
 * useful.
 */
    : android.animation.Animator(), AnimationHandler.AnimationFrameCallback {
    /**
     * Internal variables
     * NOTE: This object implements the clone() method, making a deep copy of any referenced
     * objects. As other non-trivial fields are added to this class, make sure to add logic
     * to clone() to make deep copies of them.
     */
    /**
     * The first time that the animation's animateFrame() method is called. This time is used to
     * determine elapsed time (and therefore the elapsed fraction) in subsequent calls
     * to animateFrame().
     *
     * Whenever mStartTime is set, you must also update mStartTimeCommitted.
     */
    var mStartTime: Long = -1

    /**
     * When true, the start time has been firmly committed as a chosen reference point in
     * time by which the progress of the animation will be evaluated.  When false, the
     * start time may be updated when the first animation frame is committed so as
     * to compensate for jank that may have occurred between when the start time was
     * initialized and when the frame was actually drawn.
     *
     * This flag is generally set to false during the first frame of the animation
     * when the animation playing state transitions from STOPPED to RUNNING or
     * resumes after having been paused.  This flag is set to true when the start time
     * is firmly committed and should not be further compensated for jank.
     */
    var mStartTimeCommitted = false

    /**
     * Set when setCurrentPlayTime() is called. If negative, animation is not currently seeked
     * to a value.
     */
    var mSeekFraction = -1f

    /**
     * Set on the next frame after pause() is called, used to calculate a new startTime
     * or delayStartTime which allows the animator to continue from the point at which
     * it was paused. If negative, has not yet been set.
     */
    private var mPauseTime: Long = 0

    /**
     * Set when an animator is resumed. This triggers logic in the next frame which
     * actually resumes the animator.
     */
    private var mResumed = false

    /**
     * Flag to indicate whether this animator is playing in reverse mode, specifically
     * by being started or interrupted by a call to reverse(). This flag is different than
     * mPlayingBackwards, which indicates merely whether the current iteration of the
     * animator is playing in reverse. It is used in corner cases to determine proper end
     * behavior.
     */
    private var mReversing = false

    /**
     * Tracks the overall fraction of the animation, ranging from 0 to mRepeatCount + 1
     */
    private var mOverallFraction = 0f
    /**
     * Returns the current animation fraction, which is the elapsed/interpolated fraction used in
     * the most recent frame update on the animation.
     *
     * @return Elapsed/interpolated fraction of the animation.
     */
    /**
     * Tracks current elapsed/eased fraction, for querying in getAnimatedFraction().
     * This is calculated by interpolating the fraction (range: [0, 1]) in the current iteration.
     */
    open var animatedFraction = 0f
        private set

    /**
     * Tracks the time (in milliseconds) when the last frame arrived.
     */
    private var mLastFrameTime: Long = -1

    /**
     * Tracks the time (in milliseconds) when the first frame arrived. Note the frame may arrive
     * during the start delay.
     */
    private var mFirstFrameTime: Long = -1

    /**
     * Additional playing state to indicate whether an animator has been start()'d. There is
     * some lag between a call to start() and the first animation frame. We should still note
     * that the animation has been started, even if it's first animation frame has not yet
     * happened, and reflect that state in isRunning().
     * Note that delayed animations are different: they are not started until their first
     * animation frame, which occurs after their delay elapses.
     */
    var isRunning = false
        private set

    /**
     * Additional playing state to indicate whether an animator has been start()'d, whether or
     * not there is a nonzero startDelay.
     */
    var isStarted = false
        private set

    /**
     * Flag that denotes whether the animation is set up and ready to go. Used to
     * set up animation that has not yet been started.
     */
    var isInitialized = false

    /**
     * Flag that tracks whether animation has been requested to end.
     */
    private var mAnimationEndRequested = false

    /**
     * Gets the length of the animation. The default duration is 300 milliseconds.
     *
     * @return The length of the animation, in milliseconds.
     */
    //
    // Backing variables
    //
    // How long the animation should last in ms
    @UnsupportedAppUsage
    var duration: Long = 300
        private set

    // The amount of time in ms to delay starting the animation after start() is called. Note
    // that this start delay is unscaled. When there is a duration scale set on the animator, the
    // scaling factor will be applied to this delay.
    private var mStartDelay: Long = 0
    /**
     * Defines how many times the animation should repeat. The default value
     * is 0.
     *
     * @return the number of times the animation should repeat, or [.INFINITE]
     */
    /**
     * Sets how many times the animation should be repeated. If the repeat
     * count is 0, the animation is never repeated. If the repeat count is
     * greater than 0 or [.INFINITE], the repeat mode will be taken
     * into account. The repeat count is 0 by default.
     *
     * @param value the number of times the animation should be repeated
     */
    // The number of times the animation will repeat. The default is 0, which means the animation
    // will play only once
    var repeatCount = 0
    /**
     * Defines what this animation should do when it reaches the end.
     *
     * @return either one of [.REVERSE] or [.RESTART]
     */
    /**
     * Defines what this animation should do when it reaches the end. This
     * setting is applied only when the repeat count is either greater than
     * 0 or [.INFINITE]. Defaults to [.RESTART].
     *
     * @param value [.RESTART] or [.REVERSE]
     */
    /**
     * The type of repetition that will occur when repeatMode is nonzero. RESTART means the
     * animation will start from the beginning on every new cycle. REVERSE means the animation
     * will reverse directions on each iteration.
     */
    @get:android.animation.ValueAnimator.RepeatMode
    open var repeatMode: Int = RESTART

    /**
     * Whether or not the animator should register for its own animation callback to receive
     * animation pulse.
     */
    private var mSelfPulse = true

    /**
     * Whether or not the animator has been requested to start without pulsing. This flag gets set
     * in startWithoutPulsing(), and reset in start().
     */
    private var mSuppressSelfPulseRequested = false

    /**
     * The time interpolator to be used. The elapsed fraction of the animation will be passed
     * through this interpolator to calculate the interpolated fraction, which is then used to
     * calculate the animated values.
     */
    private var mInterpolator: TimeInterpolator = sDefaultInterpolator

    /**
     * The set of listeners to be sent events through the life of an animation.
     */
    var mUpdateListeners: java.util.ArrayList<AnimatorUpdateListener>? = null

    /**
     * The property/value sets being animated.
     */
    var mValues: Array<PropertyValuesHolder>?

    /**
     * A hashmap of the PropertyValuesHolder objects. This map is used to lookup animated values
     * by property name during calls to getAnimatedValue(String).
     */
    var mValuesMap: java.util.HashMap<String, PropertyValuesHolder>? = null

    /**
     * If set to non-negative value, this will override [.sDurationScale].
     */
    private var mDurationScale = -1f

    /**
     * Animation handler used to schedule updates for this animation.
     */
    private var mAnimationHandler: AnimationHandler? = null
    /**
     * Public constants
     */
    /** @hide
     */
    @IntDef([RESTART, REVERSE])
    @Retention(
        AnnotationRetention.SOURCE
    )
    annotation class RepeatMode

    /**
     * Sets int values that will be animated between. A single
     * value implies that that value is the one being animated to. However, this is not typically
     * useful in a ValueAnimator object because there is no way for the object to determine the
     * starting value for the animation (unlike ObjectAnimator, which can derive that value
     * from the target object and property being animated). Therefore, there should typically
     * be two or more values.
     *
     *
     * If there are already multiple sets of values defined for this ValueAnimator via more
     * than one PropertyValuesHolder object, this method will set the values for the first
     * of those objects.
     *
     * @param values A set of values that the animation will animate between over time.
     */
    open fun setIntValues(vararg values: Int) {
        if (values == null || values.size == 0) {
            return
        }
        if (mValues == null || mValues!!.size == 0) {
            setValues(PropertyValuesHolder.ofInt("", *values))
        } else {
            val valuesHolder: PropertyValuesHolder = mValues!![0]
            valuesHolder.setIntValues(*values)
        }
        // New property/values/target should cause re-initialization prior to starting
        this.isInitialized = false
    }

    /**
     * Sets float values that will be animated between. A single
     * value implies that that value is the one being animated to. However, this is not typically
     * useful in a ValueAnimator object because there is no way for the object to determine the
     * starting value for the animation (unlike ObjectAnimator, which can derive that value
     * from the target object and property being animated). Therefore, there should typically
     * be two or more values.
     *
     *
     * If there are already multiple sets of values defined for this ValueAnimator via more
     * than one PropertyValuesHolder object, this method will set the values for the first
     * of those objects.
     *
     * @param values A set of values that the animation will animate between over time.
     */
    open fun setFloatValues(vararg values: Float) {
        if (values == null || values.size == 0) {
            return
        }
        if (mValues == null || mValues!!.size == 0) {
            setValues(PropertyValuesHolder.ofFloat("", *values))
        } else {
            val valuesHolder: PropertyValuesHolder = mValues!![0]
            valuesHolder.setFloatValues(*values)
        }
        // New property/values/target should cause re-initialization prior to starting
        this.isInitialized = false
    }

    /**
     * Sets the values to animate between for this animation. A single
     * value implies that that value is the one being animated to. However, this is not typically
     * useful in a ValueAnimator object because there is no way for the object to determine the
     * starting value for the animation (unlike ObjectAnimator, which can derive that value
     * from the target object and property being animated). Therefore, there should typically
     * be two or more values.
     *
     *
     * **Note:** The Object values are stored as references to the original
     * objects, which means that changes to those objects after this method is called will
     * affect the values on the animator. If the objects will be mutated externally after
     * this method is called, callers should pass a copy of those objects instead.
     *
     *
     * If there are already multiple sets of values defined for this ValueAnimator via more
     * than one PropertyValuesHolder object, this method will set the values for the first
     * of those objects.
     *
     *
     * There should be a TypeEvaluator set on the ValueAnimator that knows how to interpolate
     * between these value objects. ValueAnimator only knows how to interpolate between the
     * primitive types specified in the other setValues() methods.
     *
     * @param values The set of values to animate between.
     */
    open fun setObjectValues(vararg values: Any?) {
        if (values == null || values.size == 0) {
            return
        }
        if (mValues == null || mValues!!.size == 0) {
            setValues(PropertyValuesHolder.ofObject("", null, *values))
        } else {
            val valuesHolder: PropertyValuesHolder = mValues!![0]
            valuesHolder.setObjectValues(*values)
        }
        // New property/values/target should cause re-initialization prior to starting
        this.isInitialized = false
    }

    /**
     * Sets the values, per property, being animated between. This function is called internally
     * by the constructors of ValueAnimator that take a list of values. But a ValueAnimator can
     * be constructed without values and this method can be called to set the values manually
     * instead.
     *
     * @param values The set of values, per property, being animated between.
     */
    fun setValues(vararg values: PropertyValuesHolder) {
        val numValues = values.size
        mValues = values
        mValuesMap = java.util.HashMap<String, PropertyValuesHolder>(numValues)
        for (i in 0 until numValues) {
            val valuesHolder: PropertyValuesHolder = values[i]
            mValuesMap.put(valuesHolder.getPropertyName(), valuesHolder)
        }
        // New property/values/target should cause re-initialization prior to starting
        this.isInitialized = false
    }

    val values: Array<Any>?
        /**
         * Returns the values that this ValueAnimator animates between. These values are stored in
         * PropertyValuesHolder objects, even if the ValueAnimator was created with a simple list
         * of value objects instead.
         *
         * @return PropertyValuesHolder[] An array of PropertyValuesHolder objects which hold the
         * values, per property, that define the animation.
         */
        get() = mValues

    /**
     * This function is called immediately before processing the first animation
     * frame of an animation. If there is a nonzero `startDelay`, the
     * function is called after that delay ends.
     * It takes care of the final initialization steps for the
     * animation.
     *
     *
     * Overrides of this method should call the superclass method to ensure
     * that internal mechanisms for the animation are set up correctly.
     */
    @CallSuper
    fun initAnimation() {
        if (!this.isInitialized) {
            if (mValues != null) {
                val numValues = mValues!!.size
                for (i in 0 until numValues) {
                    mValues!![i].init()
                }
            }
            this.isInitialized = true
        }
    }

    /**
     * Sets the length of the animation. The default duration is 300 milliseconds.
     *
     * @param duration The length of the animation, in milliseconds. This value cannot
     * be negative.
     * @return ValueAnimator The object called with setDuration(). This return
     * value makes it easier to compose statements together that construct and then set the
     * duration, as in `ValueAnimator.ofInt(0, 10).setDuration(500).start()`.
     */
    override fun setDuration(duration: Long): ValueAnimator {
        if (duration < 0) {
            throw IllegalArgumentException(
                "Animators cannot have negative duration: " +
                        duration
            )
        }
        this.duration = duration
        return this
    }

    /**
     * Overrides the global duration scale by a custom value.
     *
     * @param durationScale The duration scale to set; or `-1f` to use the global duration
     * scale.
     * @hide
     */
    fun overrideDurationScale(durationScale: Float) {
        mDurationScale = durationScale
    }

    private fun resolveDurationScale(): Float {
        return if (mDurationScale >= 0f) mDurationScale else sDurationScale
    }

    private val scaledDuration: Long
        private get() = (duration * resolveDurationScale()).toLong()
    val totalDuration: Long
        get() = if (repeatCount == INFINITE) {
            android.animation.Animator.DURATION_INFINITE
        } else {
            mStartDelay + (duration * (repeatCount + 1))
        }

    /**
     * Sets the position of the animation to the specified fraction. This fraction should
     * be between 0 and the total fraction of the animation, including any repetition. That is,
     * a fraction of 0 will position the animation at the beginning, a value of 1 at the end,
     * and a value of 2 at the end of a reversing animator that repeats once. If
     * the animation has not yet been started, then it will not advance forward after it is
     * set to this fraction; it will simply set the fraction to this value and perform any
     * appropriate actions based on that fraction. If the animation is already running, then
     * setCurrentFraction() will set the current fraction to this value and continue
     * playing from that point. [Animator.AnimatorListener] events are not called
     * due to changing the fraction; those events are only processed while the animation
     * is running.
     *
     * @param fraction The fraction to which the animation is advanced or rewound. Values
     * outside the range of 0 to the maximum fraction for the animator will be clamped to
     * the correct range.
     */
    fun setCurrentFraction(fraction: Float) {
        var fraction = fraction
        initAnimation()
        fraction = clampFraction(fraction)
        mStartTimeCommitted = true // do not allow start time to be compensated for jank
        if (isPulsingInternal) {
            val seekTime = (scaledDuration * fraction).toLong()
            val currentTime: Long = AnimationUtils.currentAnimationTimeMillis()
            // Only modify the start time when the animation is running. Seek fraction will ensure
            // non-running animations skip to the correct start time.
            mStartTime = currentTime - seekTime
        } else {
            // If the animation loop hasn't started, or during start delay, the startTime will be
            // adjusted once the delay has passed based on seek fraction.
            mSeekFraction = fraction
        }
        mOverallFraction = fraction
        val currentIterationFraction = getCurrentIterationFraction(fraction, mReversing)
        animateValue(currentIterationFraction)
    }

    /**
     * Calculates current iteration based on the overall fraction. The overall fraction will be
     * in the range of [0, mRepeatCount + 1]. Both current iteration and fraction in the current
     * iteration can be derived from it.
     */
    private fun getCurrentIteration(fraction: Float): Int {
        var fraction = fraction
        fraction = clampFraction(fraction)
        // If the overall fraction is a positive integer, we consider the current iteration to be
        // complete. In other words, the fraction for the current iteration would be 1, and the
        // current iteration would be overall fraction - 1.
        var iteration = floor(fraction.toDouble())
        if (fraction.toDouble() == iteration && fraction > 0) {
            iteration--
        }
        return iteration.toInt()
    }

    /**
     * Calculates the fraction of the current iteration, taking into account whether the animation
     * should be played backwards. E.g. When the animation is played backwards in an iteration,
     * the fraction for that iteration will go from 1f to 0f.
     */
    private fun getCurrentIterationFraction(fraction: Float, inReverse: Boolean): Float {
        var fraction = fraction
        fraction = clampFraction(fraction)
        val iteration = getCurrentIteration(fraction)
        val currentFraction = fraction - iteration
        return if (shouldPlayBackward(
                iteration,
                inReverse
            )
        ) 1f - currentFraction else currentFraction
    }

    /**
     * Clamps fraction into the correct range: [0, mRepeatCount + 1]. If repeat count is infinite,
     * no upper bound will be set for the fraction.
     *
     * @param fraction fraction to be clamped
     * @return fraction clamped into the range of [0, mRepeatCount + 1]
     */
    private fun clampFraction(fraction: Float): Float {
        var fraction = fraction
        if (fraction < 0) {
            fraction = 0f
        } else if (repeatCount != INFINITE) {
            fraction = min(fraction.toDouble(), (repeatCount + 1).toDouble()).toFloat()
        }
        return fraction
    }

    /**
     * Calculates the direction of animation playing (i.e. forward or backward), based on 1)
     * whether the entire animation is being reversed, 2) repeat mode applied to the current
     * iteration.
     */
    private fun shouldPlayBackward(iteration: Int, inReverse: Boolean): Boolean {
        return if (iteration > 0 && repeatMode == REVERSE &&
            (iteration < repeatCount + 1 || repeatCount == INFINITE)
        ) {
            // if we were seeked to some other iteration in a reversing animator,
            // figure out the correct direction to start playing based on the iteration
            if (inReverse) {
                (iteration % 2) == 0
            } else {
                (iteration % 2) != 0
            }
        } else {
            inReverse
        }
    }

    open var currentPlayTime: Long
        /**
         * Gets the current position of the animation in time, which is equal to the current
         * time minus the time that the animation started. An animation that is not yet started will
         * return a value of zero, unless the animation has has its play time set via
         * [.setCurrentPlayTime] or [.setCurrentFraction], in which case
         * it will return the time that was set.
         *
         * @return The current position in time of the animation.
         */
        get() {
            if (!this.isInitialized || !isStarted && mSeekFraction < 0) {
                return 0
            }
            if (mSeekFraction >= 0) {
                return (duration * mSeekFraction).toLong()
            }
            var durationScale = resolveDurationScale()
            if (durationScale == 0f) {
                durationScale = 1f
            }
            return ((AnimationUtils.currentAnimationTimeMillis() - mStartTime) / durationScale).toLong()
        }
        /**
         * Sets the position of the animation to the specified point in time. This time should
         * be between 0 and the total duration of the animation, including any repetition. If
         * the animation has not yet been started, then it will not advance forward after it is
         * set to this time; it will simply set the time to this value and perform any appropriate
         * actions based on that time. If the animation is already running, then setCurrentPlayTime()
         * will set the current playing time to this value and continue playing from that point.
         *
         * @param playTime The time, in milliseconds, to which the animation is advanced or rewound.
         */
        set(playTime) {
            val fraction: Float = if (duration > 0) playTime.toFloat() / duration else 1
            setCurrentFraction(fraction)
        }
    var startDelay: Long
        /**
         * The amount of time, in milliseconds, to delay starting the animation after
         * [.start] is called.
         *
         * @return the number of milliseconds to delay running the animation
         */
        get() = mStartDelay
        /**
         * The amount of time, in milliseconds, to delay starting the animation after
         * [.start] is called. Note that the start delay should always be non-negative. Any
         * negative start delay will be clamped to 0 on N and above.
         *
         * @param startDelay The amount of the delay, in milliseconds
         */
        set(startDelay) {
            // Clamp start delay to non-negative range.
            var startDelay = startDelay
            if (startDelay < 0) {
                android.util.Log.w(
                    TAG,
                    "Start delay should always be non-negative"
                )
                startDelay = 0
            }
            mStartDelay = startDelay
        }
    open val animatedValue: Any?
        /**
         * The most recent value calculated by this `ValueAnimator` when there is just one
         * property being animated. This value is only sensible while the animation is running. The main
         * purpose for this read-only property is to retrieve the value from the `ValueAnimator`
         * during a call to [AnimatorUpdateListener.onAnimationUpdate], which
         * is called during each animation frame, immediately after the value is calculated.
         *
         * @return animatedValue The value most recently calculated by this `ValueAnimator` for
         * the single property being animated. If there are several properties being animated
         * (specified by several PropertyValuesHolder objects in the constructor), this function
         * returns the animated value for the first of those objects.
         */
        get() {
            return if (mValues != null && mValues!!.size > 0) {
                mValues!!.get(0).getAnimatedValue()
            } else null
            // Shouldn't get here; should always have values unless ValueAnimator was set up wrong
        }

    /**
     * The most recent value calculated by this `ValueAnimator` for `propertyName`.
     * The main purpose for this read-only property is to retrieve the value from the
     * `ValueAnimator` during a call to
     * [AnimatorUpdateListener.onAnimationUpdate], which
     * is called during each animation frame, immediately after the value is calculated.
     *
     * @return animatedValue The value most recently calculated for the named property
     * by this `ValueAnimator`.
     */
    fun getAnimatedValue(propertyName: String?): Any? {
        val valuesHolder: PropertyValuesHolder = mValuesMap.get(propertyName)
        return if (valuesHolder != null) {
            valuesHolder.getAnimatedValue()
        } else {
            // At least avoid crashing if called with bogus propertyName
            null
        }
    }

    /**
     * Adds a listener to the set of listeners that are sent update events through the life of
     * an animation. This method is called on all listeners for every frame of the animation,
     * after the values for the animation have been calculated.
     *
     * @param listener the listener to be added to the current set of listeners for this animation.
     */
    fun addUpdateListener(listener: AnimatorUpdateListener?) {
        if (mUpdateListeners == null) {
            mUpdateListeners = java.util.ArrayList<AnimatorUpdateListener>()
        }
        mUpdateListeners.add(listener)
    }

    /**
     * Removes all listeners from the set listening to frame updates for this animation.
     */
    open fun removeAllUpdateListeners() {
        if (mUpdateListeners == null) {
            return
        }
        mUpdateListeners.clear()
        mUpdateListeners = null
    }

    /**
     * Removes a listener from the set listening to frame updates for this animation.
     *
     * @param listener the listener to be removed from the current set of update listeners
     * for this animation.
     */
    fun removeUpdateListener(listener: AnimatorUpdateListener?) {
        if (mUpdateListeners == null) {
            return
        }
        mUpdateListeners.remove(listener)
        if (mUpdateListeners.size == 0) {
            mUpdateListeners = null
        }
    }

    var interpolator: TimeInterpolator
        /**
         * Returns the timing interpolator that this ValueAnimator uses.
         *
         * @return The timing interpolator for this ValueAnimator.
         */
        get() = mInterpolator
        /**
         * The time interpolator used in calculating the elapsed fraction of this animation. The
         * interpolator determines whether the animation runs with linear or non-linear motion,
         * such as acceleration and deceleration. The default value is
         * [android.view.animation.AccelerateDecelerateInterpolator]
         *
         * @param value the interpolator to be used by this animation. A value of `null`
         * will result in linear interpolation.
         */
        set(value) {
            if (value != null) {
                mInterpolator = value
            } else {
                mInterpolator = LinearInterpolator()
            }
        }

    /**
     * The type evaluator to be used when calculating the animated values of this animation.
     * The system will automatically assign a float or int evaluator based on the type
     * of `startValue` and `endValue` in the constructor. But if these values
     * are not one of these primitive types, or if different evaluation is desired (such as is
     * necessary with int values that represent colors), a custom evaluator needs to be assigned.
     * For example, when running an animation on color values, the [ArgbEvaluator]
     * should be used to get correct RGB color interpolation.
     *
     *
     * If this ValueAnimator has only one set of values being animated between, this evaluator
     * will be used for that set. If there are several sets of values being animated, which is
     * the case if PropertyValuesHolder objects were set on the ValueAnimator, then the evaluator
     * is assigned just to the first PropertyValuesHolder object.
     *
     * @param value the evaluator to be used this animation
     */
    fun setEvaluator(value: android.animation.TypeEvaluator?) {
        if (value != null && mValues != null && mValues!!.size > 0) {
            mValues!![0].setEvaluator(value)
        }
    }

    /**
     * Start the animation playing. This version of start() takes a boolean flag that indicates
     * whether the animation should play in reverse. The flag is usually false, but may be set
     * to true if called from the reverse() method.
     *
     *
     * The animation started by calling this method will be run on the thread that called
     * this method. This thread should have a Looper on it (a runtime exception will be thrown if
     * this is not the case). Also, if the animation will animate
     * properties of objects in the view hierarchy, then the calling thread should be the UI
     * thread for that view hierarchy.
     *
     * @param playBackwards Whether the ValueAnimator should start playing in reverse.
     */
    private fun start(playBackwards: Boolean) {
        if (Looper.myLooper() == null) {
            throw AndroidRuntimeException("Animators may only be run on Looper threads")
        }
        mReversing = playBackwards
        mSelfPulse = !mSuppressSelfPulseRequested
        // Special case: reversing from seek-to-0 should act as if not seeked at all.
        if (playBackwards && mSeekFraction != -1f && mSeekFraction != 0f) {
            mSeekFraction = if (repeatCount == INFINITE) {
                // Calculate the fraction of the current iteration.
                val fraction = (mSeekFraction - floor(mSeekFraction.toDouble())).toFloat()
                1 - fraction
            } else {
                1 + repeatCount - mSeekFraction
            }
        }
        isStarted = true
        mPaused = false
        isRunning = false
        mAnimationEndRequested = false
        // Resets mLastFrameTime when start() is called, so that if the animation was running,
        // calling start() would put the animation in the
        // started-but-not-yet-reached-the-first-frame phase.
        mLastFrameTime = -1
        mFirstFrameTime = -1
        mStartTime = -1
        addAnimationCallback(0)
        if (mStartDelay == 0L || mSeekFraction >= 0 || mReversing) {
            // If there's no start delay, init the animation and notify start listeners right away
            // to be consistent with the previous behavior. Otherwise, postpone this until the first
            // frame after the start delay.
            startAnimation()
            if (mSeekFraction == -1f) {
                // No seek, start at play time 0. Note that the reason we are not using fraction 0
                // is because for animations with 0 duration, we want to be consistent with pre-N
                // behavior: skip to the final value immediately.
                currentPlayTime = 0
            } else {
                setCurrentFraction(mSeekFraction)
            }
        }
    }

    fun startWithoutPulsing(inReverse: Boolean) {
        mSuppressSelfPulseRequested = true
        if (inReverse) {
            reverse()
        } else {
            start()
        }
        mSuppressSelfPulseRequested = false
    }

    override fun start() {
        start(false)
    }

    override fun cancel() {
        if (Looper.myLooper() == null) {
            throw AndroidRuntimeException("Animators may only be run on Looper threads")
        }

        // If end has already been requested, through a previous end() or cancel() call, no-op
        // until animation starts again.
        if (mAnimationEndRequested) {
            return
        }

        // Only cancel if the animation is actually running or has been started and is about
        // to run
        // Only notify listeners if the animator has actually started
        if ((isStarted || isRunning || mStartListenersCalled) && mListeners != null) {
            if (!isRunning) {
                // If it's not yet running, then start listeners weren't called. Call them now.
                notifyStartListeners(mReversing)
            }
            notifyListeners(AnimatorCaller.ON_CANCEL, false)
        }
        endAnimation()
    }

    override fun end() {
        if (Looper.myLooper() == null) {
            throw AndroidRuntimeException("Animators may only be run on Looper threads")
        }
        if (!isRunning) {
            // Special case if the animation has not yet started; get it ready for ending
            startAnimation()
            isStarted = true
        } else if (!this.isInitialized) {
            initAnimation()
        }
        animateValue(if (shouldPlayBackward(repeatCount, mReversing)) 0f else 1f)
        endAnimation()
    }

    override fun resume() {
        if (Looper.myLooper() == null) {
            throw AndroidRuntimeException(
                "Animators may only be resumed from the same " +
                        "thread that the animator was started on"
            )
        }
        if (mPaused && !mResumed) {
            mResumed = true
            if (mPauseTime > 0) {
                addAnimationCallback(0)
            }
        }
        super.resume()
    }

    override fun pause() {
        val previouslyPaused: Boolean = mPaused
        super.pause()
        if (!previouslyPaused && mPaused) {
            mPauseTime = -1
            mResumed = false
        }
    }

    /**
     * Plays the ValueAnimator in reverse. If the animation is already running,
     * it will stop itself and play backwards from the point reached when reverse was called.
     * If the animation is not currently running, then it will start from the end and
     * play backwards. This behavior is only set for the current animation; future playing
     * of the animation will use the default behavior of playing forward.
     */
    fun reverse() {
        if (isPulsingInternal) {
            val currentTime: Long = AnimationUtils.currentAnimationTimeMillis()
            val currentPlayTime = currentTime - mStartTime
            val timeLeft = scaledDuration - currentPlayTime
            mStartTime = currentTime - timeLeft
            mStartTimeCommitted = true // do not allow start time to be compensated for jank
            mReversing = !mReversing
        } else if (isStarted) {
            mReversing = !mReversing
            end()
        } else {
            start(true)
        }
    }

    /**
     * @hide
     */
    fun canReverse(): Boolean {
        return true
    }

    /**
     * Called internally to end an animation by removing it from the animations list. Must be
     * called on the UI thread.
     */
    private fun endAnimation() {
        if (mAnimationEndRequested) {
            return
        }
        removeAnimationCallback()
        mAnimationEndRequested = true
        mPaused = false
        val notify = (isStarted || isRunning) && mListeners != null
        if (notify && !isRunning) {
            // If it's not yet running, then start listeners weren't called. Call them now.
            notifyStartListeners(mReversing)
        }
        mLastFrameTime = -1
        mFirstFrameTime = -1
        mStartTime = -1
        isRunning = false
        isStarted = false
        notifyEndListeners(mReversing)
        // mReversing needs to be reset *after* notifying the listeners for the end callbacks.
        mReversing = false
        if (android.os.Trace.isTagEnabled(android.os.Trace.TRACE_TAG_VIEW)) {
            android.os.Trace.asyncTraceEnd(
                android.os.Trace.TRACE_TAG_VIEW, nameForTrace,
                java.lang.System.identityHashCode(this)
            )
        }
    }

    /**
     * Called internally to start an animation by adding it to the active animations list. Must be
     * called on the UI thread.
     */
    private fun startAnimation() {
        if (android.os.Trace.isTagEnabled(android.os.Trace.TRACE_TAG_VIEW)) {
            android.os.Trace.asyncTraceBegin(
                android.os.Trace.TRACE_TAG_VIEW, nameForTrace,
                java.lang.System.identityHashCode(this)
            )
        }
        mAnimationEndRequested = false
        initAnimation()
        isRunning = true
        mOverallFraction = if (mSeekFraction >= 0) {
            mSeekFraction
        } else {
            0f
        }
        notifyStartListeners(mReversing)
    }

    private val isPulsingInternal: Boolean
        /**
         * Internal only: This tracks whether the animation has gotten on the animation loop. Note
         * this is different than [.isRunning] in that the latter tracks the time after start()
         * is called (or after start delay if any), which may be before the animation loop starts.
         */
        private get() = mLastFrameTime >= 0
    val nameForTrace: String
        /**
         * Returns the name of this animator for debugging purposes.
         */
        get() = "animator"

    /**
     * Applies an adjustment to the animation to compensate for jank between when
     * the animation first ran and when the frame was drawn.
     * @hide
     */
    fun commitAnimationFrame(frameTime: Long) {
        if (!mStartTimeCommitted) {
            mStartTimeCommitted = true
            val adjustment = frameTime - mLastFrameTime
            if (adjustment > 0) {
                mStartTime += adjustment
                if (DEBUG) {
                    android.util.Log.d(
                        TAG,
                        "Adjusted start time by " + adjustment + " ms: " + toString()
                    )
                }
            }
        }
    }

    /**
     * This internal function processes a single animation frame for a given animation. The
     * currentTime parameter is the timing pulse sent by the handler, used to calculate the
     * elapsed duration, and therefore
     * the elapsed fraction, of the animation. The return value indicates whether the animation
     * should be ended (which happens when the elapsed time of the animation exceeds the
     * animation's duration, including the repeatCount).
     *
     * @param currentTime The current time, as tracked by the static timing handler
     * @return true if the animation's duration, including any repetitions due to
     * `repeatCount` has been exceeded and the animation should be ended.
     */
    fun animateBasedOnTime(currentTime: Long): Boolean {
        var done = false
        if (isRunning) {
            val scaledDuration = scaledDuration
            val fraction =
                if (scaledDuration > 0) (currentTime - mStartTime).toFloat() / scaledDuration else 1f
            val lastFraction = mOverallFraction
            val newIteration = fraction.toInt() > lastFraction.toInt()
            val lastIterationFinished =
                fraction >= repeatCount + 1 && repeatCount != INFINITE
            if (scaledDuration == 0L) {
                // 0 duration animator, ignore the repeat count and skip to the end
                done = true
            } else if (newIteration && !lastIterationFinished) {
                // Time to repeat
                notifyListeners(AnimatorCaller.ON_REPEAT, false)
            } else if (lastIterationFinished) {
                done = true
            }
            mOverallFraction = clampFraction(fraction)
            val currentIterationFraction = getCurrentIterationFraction(
                mOverallFraction, mReversing
            )
            animateValue(currentIterationFraction)
        }
        return done
    }

    /**
     * Internal use only.
     *
     * This method does not modify any fields of the animation. It should be called when seeking
     * in an AnimatorSet. When the last play time and current play time are of different repeat
     * iterations,
     * [android.view.animation.Animation.AnimationListener.onAnimationRepeat]
     * will be called.
     */
    fun animateValuesInRange(currentPlayTime: Long, lastPlayTime: Long, notify: Boolean) {
        var currentPlayTime = currentPlayTime
        var lastPlayTime = lastPlayTime
        if (currentPlayTime < 0 || lastPlayTime < -1) {
            throw java.lang.UnsupportedOperationException("Error: Play time should never be negative.")
        }
        initAnimation()
        val duration = totalDuration
        if (notify) {
            if (lastPlayTime < 0 || lastPlayTime == 0L && currentPlayTime > 0) {
                notifyStartListeners(false)
            } else if (lastPlayTime > duration || lastPlayTime == duration && currentPlayTime < duration) {
                notifyStartListeners(true)
            }
        }
        if (duration >= 0) {
            lastPlayTime = min(duration.toDouble(), lastPlayTime.toDouble()).toLong()
        }
        lastPlayTime -= mStartDelay
        currentPlayTime -= mStartDelay

        // Check whether repeat callback is needed only when repeat count is non-zero
        if (repeatCount > 0) {
            var iteration =
                max(0.0, (currentPlayTime / this.duration).toInt().toDouble()).toInt()
            var lastIteration =
                max(0.0, (lastPlayTime / this.duration).toInt().toDouble()).toInt()

            // Clamp iteration to [0, mRepeatCount]
            iteration = min(iteration.toDouble(), repeatCount.toDouble()).toInt()
            lastIteration = min(lastIteration.toDouble(), repeatCount.toDouble()).toInt()
            if (notify && iteration != lastIteration) {
                notifyListeners(AnimatorCaller.ON_REPEAT, false)
            }
        }
        if (repeatCount != INFINITE && currentPlayTime > (repeatCount + 1) * this.duration) {
            throw java.lang.IllegalStateException("Can't animate a value outside of the duration")
        } else {
            // Find the current fraction:
            var fraction = (max(0.0, currentPlayTime.toDouble()) / duration.toFloat()).toFloat()
            fraction = getCurrentIterationFraction(fraction, false)
            animateValue(fraction)
        }
    }

    fun animateSkipToEnds(currentPlayTime: Long, lastPlayTime: Long, notify: Boolean) {
        val inReverse = currentPlayTime < lastPlayTime
        val doSkip: Boolean
        doSkip = if (currentPlayTime <= 0 && lastPlayTime > 0) {
            true
        } else {
            val duration = totalDuration
            duration >= 0 && currentPlayTime >= duration && lastPlayTime < duration
        }
        if (doSkip) {
            if (notify) {
                notifyStartListeners(inReverse)
            }
            skipToEndValue(inReverse)
            if (notify) {
                notifyEndListeners(inReverse)
            }
        }
    }

    /**
     * Internal use only.
     * Skips the animation value to end/start, depending on whether the play direction is forward
     * or backward.
     *
     * @param inReverse whether the end value is based on a reverse direction. If yes, this is
     * equivalent to skip to start value in a forward playing direction.
     */
    fun skipToEndValue(inReverse: Boolean) {
        initAnimation()
        var endFraction = if (inReverse) 0f else 1f
        if (repeatCount % 2 == 1 && repeatMode == REVERSE) {
            // This would end on fraction = 0
            endFraction = 0f
        }
        animateValue(endFraction)
    }

    /**
     * Processes a frame of the animation, adjusting the start time if needed.
     *
     * @param frameTime The frame time.
     * @return true if the animation has ended.
     * @hide
     */
    fun doAnimationFrame(frameTime: Long): Boolean {
        if (mStartTime < 0) {
            // First frame. If there is start delay, start delay count down will happen *after* this
            // frame.
            mStartTime =
                if (mReversing) frameTime else frameTime + (mStartDelay * resolveDurationScale()).toLong()
        }

        // Handle pause/resume
        if (mPaused) {
            mPauseTime = frameTime
            removeAnimationCallback()
            return false
        } else if (mResumed) {
            mResumed = false
            if (mPauseTime > 0) {
                // Offset by the duration that the animation was paused
                mStartTime += frameTime - mPauseTime
            }
        }
        if (!isRunning) {
            // If not running, that means the animation is in the start delay phase of a forward
            // running animation. In the case of reversing, we want to run start delay in the end.
            if (mStartTime > frameTime && mSeekFraction == -1f) {
                // This is when no seek fraction is set during start delay. If developers change the
                // seek fraction during the delay, animation will start from the seeked position
                // right away.
                return false
            } else {
                // If mRunning is not set by now, that means non-zero start delay,
                // no seeking, not reversing. At this point, start delay has passed.
                isRunning = true
                startAnimation()
            }
        }
        if (mLastFrameTime < 0) {
            if (mSeekFraction >= 0) {
                val seekTime = (scaledDuration * mSeekFraction).toLong()
                mStartTime = frameTime - seekTime
                mSeekFraction = -1f
            }
            mStartTimeCommitted = false // allow start time to be compensated for jank
        }
        mLastFrameTime = frameTime
        // The frame time might be before the start time during the first frame of
        // an animation.  The "current time" must always be on or after the start
        // time to avoid animating frames at negative time intervals.  In practice, this
        // is very rare and only happens when seeking backwards.
        val currentTime =
            max(frameTime.toDouble(), mStartTime.toDouble()).toLong()
        val finished = animateBasedOnTime(currentTime)
        if (finished) {
            endAnimation()
        }
        return finished
    }

    fun pulseAnimationFrame(frameTime: Long): Boolean {
        return if (mSelfPulse) {
            // Pulse animation frame will *always* be after calling start(). If mSelfPulse isn't
            // set to false at this point, that means child animators did not call super's start().
            // This can happen when the Animator is just a non-animating wrapper around a real
            // functional animation. In this case, we can't really pulse a frame into the animation,
            // because the animation cannot necessarily be properly initialized (i.e. no start/end
            // values set).
            false
        } else doAnimationFrame(frameTime)
    }

    private fun addOneShotCommitCallback() {
        if (!mSelfPulse) {
            return
        }
        animationHandler.addOneShotCommitCallback(this)
    }

    private fun removeAnimationCallback() {
        if (!mSelfPulse) {
            return
        }
        animationHandler.removeCallback(this)
    }

    private fun addAnimationCallback(delay: Long) {
        if (!mSelfPulse) {
            return
        }
        animationHandler.addAnimationFrameCallback(this, delay)
    }

    /**
     * This method is called with the elapsed fraction of the animation during every
     * animation frame. This function turns the elapsed fraction into an interpolated fraction
     * and then into an animated value (from the evaluator. The function is called mostly during
     * animation updates, but it is also called when the `end()`
     * function is called, to set the final value on the property.
     *
     *
     * Overrides of this method must call the superclass to perform the calculation
     * of the animated value.
     *
     * @param fraction The elapsed fraction of the animation.
     */
    @CallSuper
    @UnsupportedAppUsage
    fun animateValue(fraction: Float) {
        var fraction = fraction
        if (TRACE_ANIMATION_FRACTION) {
            android.os.Trace.traceCounter(
                android.os.Trace.TRACE_TAG_VIEW,
                nameForTrace + hashCode(),
                (fraction * 1000).toInt()
            )
        }
        if (mValues == null) {
            return
        }
        fraction = mInterpolator.getInterpolation(fraction)
        animatedFraction = fraction
        val numValues = mValues!!.size
        for (i in 0 until numValues) {
            mValues!![i].calculateValue(fraction)
        }
        if (mSeekFraction >= 0 || mStartListenersCalled) {
            callOnList(mUpdateListeners, AnimatorCaller.ON_UPDATE, this, false)
        }
    }

    override fun clone(): android.animation.ValueAnimator {
        val anim: android.animation.ValueAnimator = super.clone() as android.animation.ValueAnimator
        if (mUpdateListeners != null) {
            anim.mUpdateListeners = java.util.ArrayList<AnimatorUpdateListener>(mUpdateListeners)
        }
        anim.mSeekFraction = -1f
        anim.mReversing = false
        anim.mInitialized = false
        anim.mStarted = false
        anim.mRunning = false
        anim.mPaused = false
        anim.mResumed = false
        anim.mStartTime = -1
        anim.mStartTimeCommitted = false
        anim.mAnimationEndRequested = false
        anim.mPauseTime = -1
        anim.mLastFrameTime = -1
        anim.mFirstFrameTime = -1
        anim.mOverallFraction = 0f
        anim.mCurrentFraction = 0f
        anim.mSelfPulse = true
        anim.mSuppressSelfPulseRequested = false
        val oldValues: Array<PropertyValuesHolder>? = mValues
        if (oldValues != null) {
            val numValues = oldValues.size
            anim.mValues = arrayOfNulls<PropertyValuesHolder>(numValues)
            anim.mValuesMap = java.util.HashMap<String, PropertyValuesHolder>(numValues)
            for (i in 0 until numValues) {
                val newValuesHolder: PropertyValuesHolder = oldValues[i].clone()
                anim.mValues.get(i) = newValuesHolder
                anim.mValuesMap.put(newValuesHolder.getPropertyName(), newValuesHolder)
            }
        }
        return anim
    }

    /**
     * Implementors of this interface can add themselves as update listeners
     * to an `ValueAnimator` instance to receive callbacks on every animation
     * frame, after the current frame's values have been calculated for that
     * `ValueAnimator`.
     */
    interface AnimatorUpdateListener {
        /**
         *
         * Notifies the occurrence of another frame of the animation.
         *
         * @param animation The animation which was repeated.
         */
        fun onAnimationUpdate(animation: android.animation.ValueAnimator)
    }

    override fun toString(): String {
        var returnVal = "ValueAnimator@" + java.lang.Integer.toHexString(hashCode())
        if (mValues != null) {
            for (i in mValues.indices) {
                returnVal += """
    ${mValues!![i].toString()}"""
            }
        }
        return returnVal
    }

    /**
     *
     * Whether or not the ValueAnimator is allowed to run asynchronously off of
     * the UI thread. This is a hint that informs the ValueAnimator that it is
     * OK to run the animation off-thread, however ValueAnimator may decide
     * that it must run the animation on the UI thread anyway. For example if there
     * is an [AnimatorUpdateListener] the animation will run on the UI thread,
     * regardless of the value of this hint.
     *
     *
     * Regardless of whether or not the animation runs asynchronously, all
     * listener callbacks will be called on the UI thread.
     *
     *
     * To be able to use this hint the following must be true:
     *
     *  1. [.getAnimatedFraction] is not needed (it will return undefined values).
     *  1. The animator is immutable while [.isStarted] is true. Requests
     * to change values, duration, delay, etc... may be ignored.
     *  1. Lifecycle callback events may be asynchronous. Events such as
     * [Animator.AnimatorListener.onAnimationEnd] or
     * [Animator.AnimatorListener.onAnimationRepeat] may end up delayed
     * as they must be posted back to the UI thread, and any actions performed
     * by those callbacks (such as starting new animations) will not happen
     * in the same frame.
     *  1. State change requests ([.cancel], [.end], [.reverse], etc...)
     * may be asynchronous. It is guaranteed that all state changes that are
     * performed on the UI thread in the same frame will be applied as a single
     * atomic update, however that frame may be the current frame,
     * the next frame, or some future frame. This will also impact the observed
     * state of the Animator. For example, [.isStarted] may still return true
     * after a call to [.end]. Using the lifecycle callbacks is preferred over
     * queries to [.isStarted], [.isRunning], and [.isPaused]
     * for this reason.
     *
     * @hide
     */
    fun setAllowRunningAsynchronously(mayRunAsync: Boolean) {
        // It is up to subclasses to support this, if they can.
    }

    var animationHandler: AnimationHandler
        /**
         * @return The [AnimationHandler] that will be used to schedule updates for this animator.
         * @hide
         */
        get() = if (mAnimationHandler != null) mAnimationHandler else AnimationHandler.getInstance()
        /**
         * Sets the animation handler used to schedule updates for this animator or `null` to use
         * the default handler.
         * @hide
         */
        set(animationHandler) {
            mAnimationHandler = animationHandler
        }

    /**
     * Listener interface for the system-wide scaling factor for Animator-based animations.
     *
     * @see .registerDurationScaleChangeListener
     * @see .unregisterDurationScaleChangeListener
     */
    interface DurationScaleChangeListener {
        /**
         * Called when the duration scale changes.
         * @param scale the duration scale
         */
        fun onChanged(@FloatRange(from = 0) scale: Float)
    }

    companion object {
        private const val TAG = "ValueAnimator"
        private const val DEBUG = false
        private val TRACE_ANIMATION_FRACTION: Boolean = SystemProperties.getBoolean(
            "persist.debug.animator.trace_fraction", false
        )
        /**
         * Internal constants
         */
        /**
         * System-wide animation scale.
         *
         *
         * To check whether animations are enabled system-wise use [.areAnimatorsEnabled].
         */
        @UnsupportedAppUsage(maxTargetSdk = android.os.Build.VERSION_CODES.P)
        private var sDurationScale = 1.0f
        private val sDurationScaleChangeListeners: ArrayList<java.lang.ref.WeakReference<DurationScaleChangeListener>> =
            java.util.ArrayList<java.lang.ref.WeakReference<DurationScaleChangeListener>>()

        // The time interpolator to be used if none is set on the animation
        private val sDefaultInterpolator: TimeInterpolator = AccelerateDecelerateInterpolator()

        /**
         * When the animation reaches the end and `repeatCount` is INFINITE
         * or a positive value, the animation restarts from the beginning.
         */
        const val RESTART = 1

        /**
         * When the animation reaches the end and `repeatCount` is INFINITE
         * or a positive value, the animation reverses direction on every iteration.
         */
        const val REVERSE = 2

        /**
         * This value used used with the [.setRepeatCount] property to repeat
         * the animation indefinitely.
         */
        const val INFINITE = -1

        @get:FloatRange(from = 0)
        @set:MainThread
        @set:TestApi
        @set:UnsupportedAppUsage
        var durationScale: Float
            /**
             * Returns the system-wide scaling factor for Animator-based animations.
             *
             * This affects both the start delay and duration of all such animations. Setting to 0 will
             * cause animations to end immediately. The default value is 1.0f.
             *
             * @return the duration scale.
             */
            get() = sDurationScale
            /**
             * @hide
             */
            set(durationScale) {
                sDurationScale = durationScale
                var listenerCopy: List<java.lang.ref.WeakReference<DurationScaleChangeListener?>>
                synchronized(sDurationScaleChangeListeners) {
                    listenerCopy =
                        java.util.ArrayList<java.lang.ref.WeakReference<DurationScaleChangeListener>>(
                            sDurationScaleChangeListeners
                        )
                }
                val listenersSize = listenerCopy.size
                for (i in 0 until listenersSize) {
                    val listener: DurationScaleChangeListener = listenerCopy[i].get()
                    if (listener != null) {
                        listener.onChanged(durationScale)
                    }
                }
            }

        /**
         * Registers a [DurationScaleChangeListener]
         *
         * This listens for changes to the system-wide scaling factor for Animator-based animations.
         * Listeners will be called on the main thread.
         *
         * @param listener the listener to register.
         * @return true if the listener was registered.
         */
        fun registerDurationScaleChangeListener(
            listener: DurationScaleChangeListener
        ): Boolean {
            var posToReplace = -1
            synchronized(sDurationScaleChangeListeners) {
                for (i in sDurationScaleChangeListeners.indices) {
                    val ref: java.lang.ref.WeakReference<DurationScaleChangeListener> =
                        sDurationScaleChangeListeners.get(
                            i
                        )
                    if (ref.get() == null) {
                        if (posToReplace == -1) {
                            posToReplace = i
                        }
                    } else if (ref.get() === listener) {
                        return false
                    }
                }
                if (posToReplace != -1) {
                    sDurationScaleChangeListeners.set(
                        posToReplace,
                        java.lang.ref.WeakReference<DurationScaleChangeListener>(listener)
                    )
                    return true
                } else {
                    return sDurationScaleChangeListeners.add(
                        java.lang.ref.WeakReference<DurationScaleChangeListener>(listener)
                    )
                }
            }
        }

        /**
         * Unregisters a DurationScaleChangeListener.
         *
         * @see .registerDurationScaleChangeListener
         * @param listener the listener to unregister.
         * @return true if the listener was unregistered.
         */
        fun unregisterDurationScaleChangeListener(
            listener: DurationScaleChangeListener
        ): Boolean {
            synchronized(sDurationScaleChangeListeners) {
                var listenerRefToRemove: java.lang.ref.WeakReference<DurationScaleChangeListener?>? =
                    null
                for (listenerRef: java.lang.ref.WeakReference<DurationScaleChangeListener?> in sDurationScaleChangeListeners) {
                    if (listenerRef.get() === listener) {
                        listenerRefToRemove = listenerRef
                        break
                    }
                }
                return sDurationScaleChangeListeners.remove(
                    listenerRefToRemove
                )
            }
        }

        /**
         * Returns whether animators are currently enabled, system-wide. By default, all
         * animators are enabled. This can change if either the user sets a Developer Option
         * to set the animator duration scale to 0 or by Battery Savery mode being enabled
         * (which disables all animations).
         *
         *
         * Developers should not typically need to call this method, but should an app wish
         * to show a different experience when animators are disabled, this return value
         * can be used as a decider of which experience to offer.
         *
         * @return boolean Whether animators are currently enabled. The default value is
         * `true`.
         */
        fun areAnimatorsEnabled(): Boolean {
            return sDurationScale != 0f
        }

        /**
         * Constructs and returns a ValueAnimator that animates between int values. A single
         * value implies that that value is the one being animated to. However, this is not typically
         * useful in a ValueAnimator object because there is no way for the object to determine the
         * starting value for the animation (unlike ObjectAnimator, which can derive that value
         * from the target object and property being animated). Therefore, there should typically
         * be two or more values.
         *
         * @param values A set of values that the animation will animate between over time.
         * @return A ValueAnimator object that is set up to animate between the given values.
         */
        fun ofInt(vararg values: Int): android.animation.ValueAnimator {
            val anim: android.animation.ValueAnimator = android.animation.ValueAnimator()
            anim.setIntValues(*values)
            return anim
        }

        /**
         * Constructs and returns a ValueAnimator that animates between color values. A single
         * value implies that that value is the one being animated to. However, this is not typically
         * useful in a ValueAnimator object because there is no way for the object to determine the
         * starting value for the animation (unlike ObjectAnimator, which can derive that value
         * from the target object and property being animated). Therefore, there should typically
         * be two or more values.
         *
         * @param values A set of values that the animation will animate between over time.
         * @return A ValueAnimator object that is set up to animate between the given values.
         */
        fun ofArgb(vararg values: Int): android.animation.ValueAnimator {
            val anim: android.animation.ValueAnimator = android.animation.ValueAnimator()
            anim.setIntValues(*values)
            anim.setEvaluator(ArgbEvaluator.getInstance())
            return anim
        }

        /**
         * Constructs and returns a ValueAnimator that animates between float values. A single
         * value implies that that value is the one being animated to. However, this is not typically
         * useful in a ValueAnimator object because there is no way for the object to determine the
         * starting value for the animation (unlike ObjectAnimator, which can derive that value
         * from the target object and property being animated). Therefore, there should typically
         * be two or more values.
         *
         * @param values A set of values that the animation will animate between over time.
         * @return A ValueAnimator object that is set up to animate between the given values.
         */
        fun ofFloat(vararg values: Float): android.animation.ValueAnimator {
            val anim: android.animation.ValueAnimator = android.animation.ValueAnimator()
            anim.setFloatValues(*values)
            return anim
        }

        /**
         * Constructs and returns a ValueAnimator that animates between the values
         * specified in the PropertyValuesHolder objects.
         *
         * @param values A set of PropertyValuesHolder objects whose values will be animated
         * between over time.
         * @return A ValueAnimator object that is set up to animate between the given values.
         */
        fun ofPropertyValuesHolder(vararg values: PropertyValuesHolder?): android.animation.ValueAnimator {
            val anim: android.animation.ValueAnimator = android.animation.ValueAnimator()
            anim.setValues(*values)
            return anim
        }

        /**
         * Constructs and returns a ValueAnimator that animates between Object values. A single
         * value implies that that value is the one being animated to. However, this is not typically
         * useful in a ValueAnimator object because there is no way for the object to determine the
         * starting value for the animation (unlike ObjectAnimator, which can derive that value
         * from the target object and property being animated). Therefore, there should typically
         * be two or more values.
         *
         *
         * **Note:** The Object values are stored as references to the original
         * objects, which means that changes to those objects after this method is called will
         * affect the values on the animator. If the objects will be mutated externally after
         * this method is called, callers should pass a copy of those objects instead.
         *
         *
         * Since ValueAnimator does not know how to animate between arbitrary Objects, this
         * factory method also takes a TypeEvaluator object that the ValueAnimator will use
         * to perform that interpolation.
         *
         * @param evaluator A TypeEvaluator that will be called on each animation frame to
         * provide the ncessry interpolation between the Object values to derive the animated
         * value.
         * @param values A set of values that the animation will animate between over time.
         * @return A ValueAnimator object that is set up to animate between the given values.
         */
        fun ofObject(
            evaluator: android.animation.TypeEvaluator?,
            vararg values: Any?
        ): android.animation.ValueAnimator {
            val anim: android.animation.ValueAnimator = android.animation.ValueAnimator()
            anim.setObjectValues(*values)
            anim.setEvaluator(evaluator)
            return anim
        }

        var frameDelay: Long
            /**
             * The amount of time, in milliseconds, between each frame of the animation. This is a
             * requested time that the animation will attempt to honor, but the actual delay between
             * frames may be different, depending on system load and capabilities. This is a static
             * function because the same delay will be applied to all animations, since they are all
             * run off of a single timing loop.
             *
             * The frame delay may be ignored when the animation system uses an external timing
             * source, such as the display refresh rate (vsync), to govern animations.
             *
             * Note that this method should be called from the same thread that [.start] is
             * called in order to check the frame delay for that animation. A runtime exception will be
             * thrown if the calling thread does not have a Looper.
             *
             * @return the requested time between frames, in milliseconds
             */
            get() = AnimationHandler.getInstance().getFrameDelay()
            /**
             * The amount of time, in milliseconds, between each frame of the animation. This is a
             * requested time that the animation will attempt to honor, but the actual delay between
             * frames may be different, depending on system load and capabilities. This is a static
             * function because the same delay will be applied to all animations, since they are all
             * run off of a single timing loop.
             *
             * The frame delay may be ignored when the animation system uses an external timing
             * source, such as the display refresh rate (vsync), to govern animations.
             *
             * Note that this method should be called from the same thread that [.start] is
             * called in order to have the new frame delay take effect on that animation. A runtime
             * exception will be thrown if the calling thread does not have a Looper.
             *
             * @param frameDelay the requested time between frames, in milliseconds
             */
            set(frameDelay) {
                AnimationHandler.getInstance().setFrameDelay(frameDelay)
            }
        val currentAnimationsCount: Int
            /**
             * Return the number of animations currently running.
             *
             * Used by StrictMode internally to annotate violations.
             * May be called on arbitrary threads!
             *
             * @hide
             */
            get() = AnimationHandler.getAnimationCount()
    }
}
