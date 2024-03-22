package framework.animation.model

import io.ktor.utils.io.errors.IOException
import kotlin.jvm.JvmOverloads

/*
* Copyright (C) 2006 The Android Open Source Project
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
 * The Typeface class specifies the typeface and intrinsic style of a font.
 * This is used in the paint, along with optionally Paint settings like
 * textSize, textSkewX, textScaleX to specify
 * how text appears when drawn (and measured).
 */
class Typeface private constructor(ni: Long, systemFontFamilyName: String?) {
    /**
     * @hide
     */
//    @UnsupportedAppUsage
    val native_instance: Long

    /**
     * Returns the system font family name if the typeface was created from a system font family,
     * otherwise returns null.
     */
    val systemFontFamilyName: String?
    private val mCleaner: java.lang.Runnable

    /** @hide
     */
    @IntDef(value = [NORMAL, BOLD, ITALIC, BOLD_ITALIC])
    @Retention(
        AnnotationRetention.SOURCE
    )
    annotation class Style

    /** Returns the typeface's intrinsic style attributes  */
//    @UnsupportedAppUsage
    @Style
    val style: Int

    /** Returns the typeface's weight value  */
    @get:IntRange(from = 0, to = 1000)
    @IntRange(from = 0, to = android.graphics.fonts.FontStyle.FONT_WEIGHT_MAX)
    val weight: Int

    @GuardedBy("this")
    private var mSupportedAxes: IntArray?
    val isBold: Boolean
        /** Returns true if getStyle() has the BOLD bit set.  */
        get() = style and BOLD != 0
    val isItalic: Boolean
        /** Returns true if getStyle() has the ITALIC bit set.  */
        get() = style and ITALIC != 0

    /**
     * A builder class for creating new Typeface instance.
     *
     *
     *
     * Examples,
     * 1) Create Typeface from ttf file.
     * <pre>
     * `
     * Typeface.Builder builder = new Typeface.Builder("your_font_file.ttf");
     * Typeface typeface = builder.build();
    ` *
    </pre> *
     *
     * 2) Create Typeface from ttc file in assets directory.
     * <pre>
     * `
     * Typeface.Builder builder = new Typeface.Builder(getAssets(), "your_font_file.ttc");
     * builder.setTtcIndex(2);  // Set index of font collection.
     * Typeface typeface = builder.build();
    ` *
    </pre> *
     *
     * 3) Create Typeface with variation settings.
     * <pre>
     * `
     * Typeface.Builder builder = new Typeface.Builder("your_font_file.ttf");
     * builder.setFontVariationSettings("'wght' 700, 'slnt' 20, 'ital' 1");
     * builder.setWeight(700);  // Tell the system that this is a bold font.
     * builder.setItalic(true);  // Tell the system that this is an italic style font.
     * Typeface typeface = builder.build();
    ` *
    </pre> *
     *
     */
    class Builder {
        // Kept for generating asset cache key.
        private val mAssetManager: AssetManager?
        private val mPath: String?
        private val mFontBuilder: android.graphics.fonts.Font.Builder?
        private var mFallbackFamilyName: String? = null
        private var mWeight: Int = RESOLVE_BY_FONT_TABLE
        private var mItalic: Int = RESOLVE_BY_FONT_TABLE

        /**
         * Constructs a builder with a file path.
         *
         * @param path The file object refers to the font file.
         */
        constructor(path: java.io.File) {
            mFontBuilder = android.graphics.fonts.Font.Builder(path)
            mAssetManager = null
            mPath = null
        }

        /**
         * Constructs a builder with a file descriptor.
         *
         * Caller is responsible for closing the passed file descriptor after [.build] is
         * called.
         *
         * @param fd The file descriptor. The passed fd must be mmap-able.
         */
        constructor(fd: java.io.FileDescriptor) {
            val builder: android.graphics.fonts.Font.Builder?
            builder = try {
                android.graphics.fonts.Font.Builder(ParcelFileDescriptor.dup(fd))
            } catch (e: IOException) {
                // We cannot tell the error to developer at this moment since we cannot change the
                // public API signature. Instead, silently fallbacks to system fallback in the build
                // method as the same as other error cases.
                null
            }
            mFontBuilder = builder
            mAssetManager = null
            mPath = null
        }

        /**
         * Constructs a builder with a file path.
         *
         * @param path The full path to the font file.
         */
        constructor(path: String) {
            mFontBuilder = android.graphics.fonts.Font.Builder(java.io.File(path))
            mAssetManager = null
            mPath = null
        }
        /**
         * Constructs a builder from an asset manager and a file path in an asset directory.
         *
         * @param assetManager The application's asset manager
         * @param path The file name of the font data in the asset directory
         * @param cookie a cookie for the asset
         * @hide
         */
        /**
         * Constructs a builder from an asset manager and a file path in an asset directory.
         *
         * @param assetManager The application's asset manager
         * @param path The file name of the font data in the asset directory
         */
        @JvmOverloads
        constructor(
            assetManager: AssetManager, path: String, isAsset: Boolean = true /* is asset */,
            cookie: Int = 0 /* cookie */
        ) {
            mFontBuilder = android.graphics.fonts.Font.Builder(assetManager, path, isAsset, cookie)
            mAssetManager = assetManager
            mPath = path
        }

        /**
         * Sets weight of the font.
         *
         * Tells the system the weight of the given font. If not provided, the system will resolve
         * the weight value by reading font tables.
         * @param weight a weight value.
         */
        fun setWeight(
            @IntRange(
                from = 1,
                to = 1000
            ) weight: Int
        ): Typeface.Builder {
            mWeight = weight
            mFontBuilder.setWeight(weight)
            return this
        }

        /**
         * Sets italic information of the font.
         *
         * Tells the system the style of the given font. If not provided, the system will resolve
         * the style by reading font tables.
         * @param italic `true` if the font is italic. Otherwise `false`.
         */
        fun setItalic(italic: Boolean): Typeface.Builder {
            mItalic =
                if (italic) android.graphics.fonts.FontStyle.FONT_SLANT_ITALIC else android.graphics.fonts.FontStyle.FONT_SLANT_UPRIGHT
            mFontBuilder.setSlant(mItalic)
            return this
        }

        /**
         * Sets an index of the font collection. See [android.R.attr.ttcIndex].
         *
         * Can not be used for Typeface source. build() method will return null for invalid index.
         * @param ttcIndex An index of the font collection. If the font source is not font
         * collection, do not call this method or specify 0.
         */
        fun setTtcIndex(@IntRange(from = 0) ttcIndex: Int): Typeface.Builder {
            mFontBuilder.setTtcIndex(ttcIndex)
            return this
        }

        /**
         * Sets a font variation settings.
         *
         * @param variationSettings See [android.widget.TextView.setFontVariationSettings].
         * @throws IllegalArgumentException If given string is not a valid font variation settings
         * format.
         */
        fun setFontVariationSettings(variationSettings: String?): Typeface.Builder {
            mFontBuilder.setFontVariationSettings(variationSettings)
            return this
        }

        /**
         * Sets a font variation settings.
         *
         * @param axes An array of font variation axis tag-value pairs.
         */
        fun setFontVariationSettings(axes: Array<android.graphics.fonts.FontVariationAxis?>?): Typeface.Builder {
            mFontBuilder.setFontVariationSettings(axes)
            return this
        }

        /**
         * Sets a fallback family name.
         *
         * By specifying a fallback family name, a fallback Typeface will be returned if the
         * [.build] method fails to create a Typeface from the provided font. The fallback
         * family will be resolved with the provided weight and italic information specified by
         * [.setWeight] and [.setItalic].
         *
         * If [.setWeight] is not called, the fallback family keeps the default weight.
         * Similary, if [.setItalic] is not called, the fallback family keeps the default
         * italic information. For example, calling `builder.setFallback("sans-serif-light")`
         * is equivalent to calling `builder.setFallback("sans-serif").setWeight(300)` in
         * terms of fallback. The default weight and italic information are overridden by calling
         * [.setWeight] and [.setItalic]. For example, if a Typeface is constructed
         * using `builder.setFallback("sans-serif-light").setWeight(700)`, the fallback text
         * will render as sans serif bold.
         *
         * @param familyName A family name to be used for fallback if the provided font can not be
         * used. By passing `null`, build() returns `null`.
         * If [.setFallback] is not called on the builder, `null`
         * is assumed.
         */
        fun setFallback(familyName: String?): Typeface.Builder {
            mFallbackFamilyName = familyName
            return this
        }

        private fun resolveFallbackTypeface(): Typeface? {
            if (mFallbackFamilyName == null) {
                return null
            }
            val base: Typeface =
                getSystemDefaultTypeface(mFallbackFamilyName)
            if (mWeight == RESOLVE_BY_FONT_TABLE && mItalic == RESOLVE_BY_FONT_TABLE) {
                return base
            }
            val weight =
                if (mWeight == RESOLVE_BY_FONT_TABLE) base.mWeight else mWeight
            val italic =
                if (mItalic == RESOLVE_BY_FONT_TABLE) base.mStyle and ITALIC != 0 else mItalic == 1
            return createWeightStyle(base, weight, italic)
        }

        /**
         * Generates new Typeface from specified configuration.
         *
         * @return Newly created Typeface. May return null if some parameters are invalid.
         */
        fun build(): Typeface? {
            return if (mFontBuilder == null) {
                resolveFallbackTypeface()
            } else try {
                val font: android.graphics.fonts.Font = mFontBuilder.build()
                val key: String? =
                    if (mAssetManager == null) null else Typeface.Builder.Companion.createAssetUid(
                        mAssetManager, mPath, font.getTtcIndex(), font.getAxes(),
                        mWeight, mItalic,
                        if (mFallbackFamilyName == null) DEFAULT_FAMILY else mFallbackFamilyName
                    )
                if (key != null) {
                    // Dynamic cache lookup is only for assets.
                    synchronized(sDynamicCacheLock) {
                        val typeface: Typeface =
                            sDynamicTypefaceCache.get(key)
                        if (typeface != null) {
                            return typeface
                        }
                    }
                }
                val family: android.graphics.fonts.FontFamily =
                    android.graphics.fonts.FontFamily.Builder(font).build()
                val weight =
                    if (mWeight == RESOLVE_BY_FONT_TABLE) font.getStyle()
                        .getWeight() else mWeight
                val slant =
                    if (mItalic == RESOLVE_BY_FONT_TABLE) font.getStyle()
                        .getSlant() else mItalic
                val builder: Typeface.CustomFallbackBuilder =
                    Typeface.CustomFallbackBuilder(family)
                        .setStyle(android.graphics.fonts.FontStyle(weight, slant))
                if (mFallbackFamilyName != null) {
                    builder.setSystemFallback(mFallbackFamilyName)
                }
                val typeface: Typeface = builder.build()
                if (key != null) {
                    synchronized(sDynamicCacheLock) {
                        sDynamicTypefaceCache.put(
                            key,
                            typeface
                        )
                    }
                }
                typeface
            } catch (e: java.io.IOException) {
                resolveFallbackTypeface()
            } catch (e: java.lang.IllegalArgumentException) {
                resolveFallbackTypeface()
            }
        }

        companion object {
            /** @hide
             */
            const val NORMAL_WEIGHT = 400

            /** @hide
             */
            const val BOLD_WEIGHT = 700

            /**
             * Creates a unique id for a given AssetManager and asset path.
             *
             * @param mgr  AssetManager instance
             * @param path The path for the asset.
             * @param ttcIndex The TTC index for the font.
             * @param axes The font variation settings.
             * @return Unique id for a given AssetManager and asset path.
             */
            private fun createAssetUid(
                mgr: AssetManager,
                path: String,
                ttcIndex: Int,
                axes: Array<android.graphics.fonts.FontVariationAxis>?,
                weight: Int,
                italic: Int,
                fallback: String
            ): String {
                val pkgs: android.util.SparseArray<String> = mgr.getAssignedPackageIdentifiers()
                val builder: java.lang.StringBuilder = java.lang.StringBuilder()
                val size: Int = pkgs.size()
                for (i in 0 until size) {
                    builder.append(pkgs.valueAt(i))
                    builder.append("-")
                }
                builder.append(path)
                builder.append("-")
                builder.append(ttcIndex.toString())
                builder.append("-")
                builder.append(weight.toString())
                builder.append("-")
                builder.append(italic.toString())
                // Family name may contain hyphen. Use double hyphen for avoiding key conflicts before
                // and after appending falblack name.
                builder.append("--")
                builder.append(fallback)
                builder.append("--")
                if (axes != null) {
                    for (axis in axes) {
                        builder.append(axis.getTag())
                        builder.append("-")
                        builder.append(axis.getStyleValue().toString())
                    }
                }
                return builder.toString()
            }
        }
    }

    /**
     * A builder class for creating new Typeface instance.
     *
     * There are two font fallback mechanisms, custom font fallback and system font fallback.
     * The custom font fallback is a simple ordered list. The text renderer tries to see if it can
     * render a character with the first font and if that font does not support the character, try
     * next one and so on. It will keep trying until end of the custom fallback chain. The maximum
     * length of the custom fallback chain is 64.
     * The system font fallback is a system pre-defined fallback chain. The system fallback is
     * processed only when no matching font is found in the custom font fallback.
     *
     *
     *
     * Examples,
     * 1) Create Typeface from single ttf file.
     * <pre>
     * `
     * Font font = new Font.Builder("your_font_file.ttf").build();
     * FontFamily family = new FontFamily.Builder(font).build();
     * Typeface typeface = new Typeface.CustomFallbackBuilder(family).build();
    ` *
    </pre> *
     *
     * 2) Create Typeface from multiple font files and select bold style by default.
     * <pre>
     * `
     * Font regularFont = new Font.Builder("regular.ttf").build();
     * Font boldFont = new Font.Builder("bold.ttf").build();
     * FontFamily family = new FontFamily.Builder(regularFont)
     * .addFont(boldFont).build();
     * Typeface typeface = new Typeface.CustomFallbackBuilder(family)
     * .setWeight(Font.FONT_WEIGHT_BOLD)  // Set bold style as the default style.
     * // If the font family doesn't have bold style font,
     * // system will select the closest font.
     * .build();
    ` *
    </pre> *
     *
     * 3) Create Typeface from single ttf file and if that font does not have glyph for the
     * characters, use "serif" font family instead.
     * <pre>
     * `
     * Font font = new Font.Builder("your_font_file.ttf").build();
     * FontFamily family = new FontFamily.Builder(font).build();
     * Typeface typeface = new Typeface.CustomFallbackBuilder(family)
     * .setSystemFallback("serif")  // Set serif font family as the fallback.
     * .build();
    ` *
    </pre> *
     * 4) Create Typeface from single ttf file and set another ttf file for the fallback.
     * <pre>
     * `
     * Font font = new Font.Builder("English.ttf").build();
     * FontFamily family = new FontFamily.Builder(font).build();
     *
     * Font fallbackFont = new Font.Builder("Arabic.ttf").build();
     * FontFamily fallbackFamily = new FontFamily.Builder(fallbackFont).build();
     * Typeface typeface = new Typeface.CustomFallbackBuilder(family)
     * .addCustomFallback(fallbackFamily)  // Specify fallback family.
     * .setSystemFallback("serif")  // Set serif font family as the fallback.
     * .build();
    ` *
    </pre> *
     *
     */
    class CustomFallbackBuilder(family: android.graphics.fonts.FontFamily) {
        private val mFamilies: java.util.ArrayList<android.graphics.fonts.FontFamily> =
            java.util.ArrayList<android.graphics.fonts.FontFamily>()
        private var mFallbackName: String? = null
        private var mStyle: android.graphics.fonts.FontStyle? = null

        /**
         * Constructs a builder with a font family.
         *
         * @param family a family object
         */
        init {
            Preconditions.checkNotNull(family)
            mFamilies.add(family)
        }

        /**
         * Sets a system fallback by name.
         *
         * You can specify generic font familiy names or OEM specific family names. If the system
         * don't have a specified fallback, the default fallback is used instead.
         * For more information about generic font families, see [CSS specification](https://www.w3.org/TR/css-fonts-4/#generic-font-families)
         *
         * For more information about fallback, see class description.
         *
         * @param familyName a family name to be used for fallback if the provided fonts can not be
         * used
         */
        fun setSystemFallback(familyName: String): Typeface.CustomFallbackBuilder {
            Preconditions.checkNotNull(familyName)
            mFallbackName = familyName
            return this
        }

        /**
         * Sets a font style of the Typeface.
         *
         * If the font family doesn't have a font of given style, system will select the closest
         * font from font family. For example, if a font family has fonts of 300 weight and 700
         * weight then setWeight(400) is called, system will select the font of 300 weight.
         *
         * @param style a font style
         */
        fun setStyle(style: android.graphics.fonts.FontStyle): Typeface.CustomFallbackBuilder {
            mStyle = style
            return this
        }

        /**
         * Append a font family to the end of the custom font fallback.
         *
         * You can set up to 64 custom fallback families including the first font family you passed
         * to the constructor.
         * For more information about fallback, see class description.
         *
         * @param family a fallback family
         * @throws IllegalArgumentException if you give more than 64 custom fallback families
         */
        fun addCustomFallback(family: android.graphics.fonts.FontFamily): Typeface.CustomFallbackBuilder {
            Preconditions.checkNotNull(family)
            Preconditions.checkArgument(
                mFamilies.size < Typeface.CustomFallbackBuilder.Companion.getMaxCustomFallbackCount(),
                "Custom fallback limit exceeded(%d)",
                Typeface.CustomFallbackBuilder.Companion.getMaxCustomFallbackCount()
            )
            mFamilies.add(family)
            return this
        }

        /**
         * Create the Typeface based on the configured values.
         *
         * @return the Typeface object
         */
        fun build(): Typeface {
            val userFallbackSize: Int = mFamilies.size
            val fallbackTypeface: Typeface =
                getSystemDefaultTypeface(mFallbackName)
            val ptrArray = LongArray(userFallbackSize)
            for (i in 0 until userFallbackSize) {
                ptrArray[i] = mFamilies.get(i).getNativePtr()
            }
            val weight = if (mStyle == null) 400 else mStyle.getWeight()
            val italic =
                if (mStyle == null || mStyle.getSlant() == android.graphics.fonts.FontStyle.FONT_SLANT_UPRIGHT) 0 else 1
            return Typeface(
                nativeCreateFromArray(
                    ptrArray, fallbackTypeface.native_instance, weight, italic
                ), null
            )
        }

        companion object {
            private const val MAX_CUSTOM_FALLBACK = 64

            @get:IntRange(from = 64)
            val maxCustomFallbackCount: Int
                /**
                 * Returns the maximum capacity of custom fallback families.
                 *
                 * This includes the the first font family passed to the constructor.
                 * It is guaranteed that the value will be greater than or equal to 64.
                 *
                 * @return the maximum number of font families for the custom fallback
                 */
                get() = Typeface.CustomFallbackBuilder.Companion.MAX_CUSTOM_FALLBACK
        }
    }

    // don't allow clients to call this directly
    @UnsupportedAppUsage(maxTargetSdk = android.os.Build.VERSION_CODES.P, trackingBug = 115609023)
    private constructor(ni: Long) : this(ni, null)

    /**
     * Releases the underlying native object.
     *
     *
     * For testing only. Do not use the instance after this method is called.
     * It is safe to call this method twice or more on the same instance.
     * @hide
     */
    @TestApi
    fun releaseNativeObjectForTest() {
        mCleaner.run()
    }

    // don't allow clients to call this directly
    init {
        if (ni == 0L) {
            throw java.lang.RuntimeException("native typeface cannot be made")
        }
        native_instance = ni
        mCleaner = sRegistry.registerNativeAllocation(
            this,
            native_instance
        )
        style = nativeGetStyle(ni)
        weight = nativeGetWeight(ni)
        this.systemFontFamilyName = systemFontFamilyName
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val typeface: Typeface = o as Typeface
        return style == typeface.mStyle && native_instance == typeface.native_instance
    }

    override fun hashCode(): Int {
        /*
         * Modified method for hashCode with long native_instance derived from
         * http://developer.android.com/reference/java/lang/Object.html
         */
        var result = 17
        result = 31 * result + (native_instance xor (native_instance ushr 32)).toInt()
        result = 31 * result + style
        return result
    }

    /** @hide
     */
    fun isSupportedAxes(axis: Int): Boolean {
        synchronized(this) {
            if (mSupportedAxes == null) {
                mSupportedAxes =
                    nativeGetSupportedAxes(native_instance)
                if (mSupportedAxes == null) {
                    mSupportedAxes = EMPTY_AXES
                }
            }
        }
        return java.util.Arrays.binarySearch(mSupportedAxes, axis) >= 0
    }

    companion object {
        private const val TAG = "Typeface"

        /** @hide
         */
        const val ENABLE_LAZY_TYPEFACE_INITIALIZATION = true
        private val sRegistry: NativeAllocationRegistry = NativeAllocationRegistry.createMalloced(
            Typeface::class.java.getClassLoader(),
            nativeGetReleaseFunc()
        )

        /** The default NORMAL typeface object  */
        val DEFAULT: Typeface? = null

        /**
         * The default BOLD typeface object. Note: this may be not actually be
         * bold, depending on what fonts are installed. Call getStyle() to know
         * for sure.
         */
        val DEFAULT_BOLD: Typeface? = null

        /** The NORMAL style of the default sans serif typeface.  */
        val SANS_SERIF: Typeface? = null

        /** The NORMAL style of the default serif typeface.  */
        val SERIF: Typeface? = null

        /** The NORMAL style of the default monospace typeface.  */
        val MONOSPACE: Typeface? = null

        /**
         * The default [Typeface]s for different text styles.
         * Call [.defaultFromStyle] to get the default typeface for the given text style.
         * It shouldn't be changed for app wide typeface settings. Please use theme and font XML for
         * the same purpose.
         */
        @GuardedBy("SYSTEM_FONT_MAP_LOCK")
        @UnsupportedAppUsage(trackingBug = 123769446)
        var sDefaults: Array<Typeface>

        /**
         * Cache for Typeface objects for style variant. Currently max size is 3.
         */
        @GuardedBy("sStyledCacheLock")
        private val sStyledTypefaceCache: android.util.LongSparseArray<android.util.SparseArray<Typeface>> =
            android.util.LongSparseArray<android.util.SparseArray<Typeface>>(3)
        private val sStyledCacheLock = Any()

        /**
         * Cache for Typeface objects for weight variant. Currently max size is 3.
         */
        @GuardedBy("sWeightCacheLock")
        private val sWeightTypefaceCache: android.util.LongSparseArray<android.util.SparseArray<Typeface>> =
            android.util.LongSparseArray<android.util.SparseArray<Typeface>>(3)
        private val sWeightCacheLock = Any()

        /**
         * Cache for Typeface objects dynamically loaded from assets. Currently max size is 16.
         */
        @GuardedBy("sDynamicCacheLock")
        private val sDynamicTypefaceCache: android.util.LruCache<String, Typeface> =
            android.util.LruCache<String, Typeface>(16)
        private val sDynamicCacheLock = Any()

        @GuardedBy("SYSTEM_FONT_MAP_LOCK")
        var sDefaultTypeface: Typeface? = null

        /**
         * sSystemFontMap is read only and unmodifiable.
         * Use public API [.create] to get the typeface for given familyName.
         */
        @GuardedBy("SYSTEM_FONT_MAP_LOCK")
        @UnsupportedAppUsage(trackingBug = 123769347)
        val sSystemFontMap: Map<String, Typeface> =
            android.util.ArrayMap<String, Typeface>()

        // DirectByteBuffer object to hold sSystemFontMap's backing memory mapping.
        var sSystemFontMapBuffer: java.nio.ByteBuffer? = null
        var sSystemFontMapSharedMemory: android.os.SharedMemory? = null

        // Lock to guard sSystemFontMap and derived default or public typefaces.
        // sStyledCacheLock may be held while this lock is held. Holding them in the reverse order may
        // introduce deadlock.
        private val SYSTEM_FONT_MAP_LOCK = Any()
        // This field is used but left for hiddenapi private list
        // We cannot support sSystemFallbackMap since we will migrate to public FontFamily API.

        @UnsupportedAppUsage(trackingBug = 123768928)
        @Deprecated("Use {@link android.graphics.fonts.FontFamily} instead.")
        val sSystemFallbackMap: Map<String, Array<android.graphics.FontFamily>> =
            emptyMap<String, Array<android.graphics.FontFamily>>()

        @get:TestApi
        val systemFontMapSharedMemory: android.os.SharedMemory?
            /**
             * Returns the shared memory that used for creating Typefaces.
             *
             * @return A SharedMemory used for creating Typeface. Maybe null if the lazy initialization is
             * disabled or inside SystemServer or Zygote.
             * @hide
             */
            get() {
                if (ENABLE_LAZY_TYPEFACE_INITIALIZATION) {
                    java.util.Objects.requireNonNull<android.os.SharedMemory>(sSystemFontMapSharedMemory)
                }
                return sSystemFontMapSharedMemory
            }

        // Style
        const val NORMAL = 0
        const val BOLD = 1
        const val ITALIC = 2
        const val BOLD_ITALIC = 3

        /** @hide
         */
        const val STYLE_MASK = 0x03
        // Value for weight and italic. Indicates the value is resolved by font metadata.
        // Must be the same as the C++ constant in core/jni/android/graphics/FontFamily.cpp
        /** @hide
         */
        const val RESOLVE_BY_FONT_TABLE = -1

        /**
         * The key of the default font family.
         * @hide
         */
        const val DEFAULT_FAMILY = "sans-serif"

        // Style value for building typeface.
        private const val STYLE_NORMAL = 0
        private const val STYLE_ITALIC = 1
        private val EMPTY_AXES = intArrayOf()

        @set:UnsupportedAppUsage(maxTargetSdk = android.os.Build.VERSION_CODES.P)
        private var default: Typeface
            private get() {
                synchronized(SYSTEM_FONT_MAP_LOCK) { return sDefaultTypeface }
            }
            /**
             * Please use font in xml and also your application global theme to change the default Typeface.
             * android:textViewStyle and its attribute android:textAppearance can be used in order to change
             * typeface and other text related properties.
             */
            private set(t) {
                synchronized(SYSTEM_FONT_MAP_LOCK) {
                    sDefaultTypeface = t
                    nativeSetDefault(t.native_instance)
                }
            }

        /**
         * Returns true if the system has the font family with the name [familyName]. For example
         * querying with "sans-serif" would check if the "sans-serif" family is defined in the system
         * and return true if does.
         *
         * @param familyName The name of the font family, cannot be null. If null, exception will be
         * thrown.
         */
        private fun hasFontFamily(familyName: String): Boolean {
            java.util.Objects.requireNonNull<String>(familyName, "familyName cannot be null")
            synchronized(SYSTEM_FONT_MAP_LOCK) {
                return sSystemFontMap.containsKey(
                    familyName
                )
            }
        }

        /**
         * @hide
         * Used by Resources to load a font resource of type xml.
         */
        fun createFromResources(
            entry: FamilyResourceEntry, mgr: AssetManager?, path: String?
        ): Typeface? {
            if (entry is ProviderResourceEntry) {
                val providerEntry: ProviderResourceEntry = entry as ProviderResourceEntry
                val systemFontFamilyName: String = providerEntry.getSystemFontFamilyName()
                if (systemFontFamilyName != null && hasFontFamily(
                        systemFontFamilyName
                    )
                ) {
                    return create(
                        systemFontFamilyName,
                        NORMAL
                    )
                }
                // Downloadable font
                val givenCerts: List<List<String>> = providerEntry.getCerts()
                val certs: MutableList<List<ByteArray>> = java.util.ArrayList<List<ByteArray>>()
                if (givenCerts != null) {
                    for (i in givenCerts.indices) {
                        val certSet = givenCerts[i]
                        val byteArraySet: MutableList<ByteArray> = java.util.ArrayList<ByteArray>()
                        for (j in certSet.indices) {
                            byteArraySet.add(
                                android.util.Base64.decode(
                                    certSet[j],
                                    android.util.Base64.DEFAULT
                                )
                            )
                        }
                        certs.add(byteArraySet)
                    }
                }
                // Downloaded font and it wasn't cached, request it again and return a
                // default font instead (nothing we can do now).
                val request: android.provider.FontRequest = android.provider.FontRequest(
                    providerEntry.getAuthority(),
                    providerEntry.getPackage(), providerEntry.getQuery(), certs
                )
                val typeface: Typeface = FontsContract.getFontSync(request)
                return if (typeface == null) DEFAULT else typeface
            }
            var typeface: Typeface =
                findFromCache(mgr, path)
            if (typeface != null) return typeface

            // family is FontFamilyFilesResourceEntry
            val filesEntry: FontFamilyFilesResourceEntry = entry as FontFamilyFilesResourceEntry
            try {
                var familyBuilder: android.graphics.fonts.FontFamily.Builder? = null
                for (fontFile in filesEntry.getEntries()) {
                    val fontBuilder: android.graphics.fonts.Font.Builder =
                        android.graphics.fonts.Font.Builder(
                            mgr, fontFile.getFileName(),
                            false /* isAsset */, AssetManager.COOKIE_UNKNOWN
                        )
                            .setTtcIndex(fontFile.getTtcIndex())
                            .setFontVariationSettings(fontFile.getVariationSettings())
                    if (fontFile.getWeight() !== RESOLVE_BY_FONT_TABLE) {
                        fontBuilder.setWeight(fontFile.getWeight())
                    }
                    if (fontFile.getItalic() !== RESOLVE_BY_FONT_TABLE) {
                        fontBuilder.setSlant(if (fontFile.getItalic() === FontFileResourceEntry.ITALIC) android.graphics.fonts.FontStyle.FONT_SLANT_ITALIC else android.graphics.fonts.FontStyle.FONT_SLANT_UPRIGHT)
                    }
                    if (familyBuilder == null) {
                        familyBuilder =
                            android.graphics.fonts.FontFamily.Builder(fontBuilder.build())
                    } else {
                        familyBuilder.addFont(fontBuilder.build())
                    }
                }
                if (familyBuilder == null) {
                    return DEFAULT
                }
                val family: android.graphics.fonts.FontFamily = familyBuilder.build()
                val normal: android.graphics.fonts.FontStyle = android.graphics.fonts.FontStyle(
                    android.graphics.fonts.FontStyle.FONT_WEIGHT_NORMAL,
                    android.graphics.fonts.FontStyle.FONT_SLANT_UPRIGHT
                )
                var bestFont: android.graphics.fonts.Font = family.getFont(0)
                var bestScore: Int = normal.getMatchScore(bestFont.getStyle())
                for (i in 1 until family.getSize()) {
                    val candidate: android.graphics.fonts.Font = family.getFont(i)
                    val score: Int = normal.getMatchScore(candidate.getStyle())
                    if (score < bestScore) {
                        bestFont = candidate
                        bestScore = score
                    }
                }
                typeface = Typeface.CustomFallbackBuilder(family)
                    .setStyle(bestFont.getStyle())
                    .build()
            } catch (e: java.lang.IllegalArgumentException) {
                // To be a compatible behavior with API28 or before, catch IllegalArgumentExcetpion
                // thrown by native code and returns null.
                return null
            } catch (e: java.io.IOException) {
                typeface = DEFAULT
            }
            synchronized(sDynamicCacheLock) {
                val key: String = Typeface.Builder.Companion.createAssetUid(
                    mgr,
                    path,
                    0 /* ttcIndex */,
                    null /* axes */,
                    RESOLVE_BY_FONT_TABLE /* weight */,
                    RESOLVE_BY_FONT_TABLE /* italic */,
                    DEFAULT_FAMILY
                )
                sDynamicTypefaceCache.put(key, typeface)
            }
            return typeface
        }

        /**
         * Used by resources for cached loading if the font is available.
         * @hide
         */
        fun findFromCache(mgr: AssetManager?, path: String?): Typeface? {
            synchronized(sDynamicCacheLock) {
                val key: String = Typeface.Builder.Companion.createAssetUid(
                    mgr,
                    path,
                    0 /* ttcIndex */,
                    null /* axes */,
                    RESOLVE_BY_FONT_TABLE /* weight */,
                    RESOLVE_BY_FONT_TABLE /* italic */,
                    DEFAULT_FAMILY
                )
                val typeface: Typeface =
                    sDynamicTypefaceCache.get(key)
                if (typeface != null) {
                    return typeface
                }
            }
            return null
        }

        /**
         * Create a typeface object given a family name, and option style information.
         * If null is passed for the name, then the "default" font will be chosen.
         * The resulting typeface object can be queried (getStyle()) to discover what
         * its "real" style characteristics are.
         *
         * @param familyName May be null. The name of the font family.
         * @param style  The style (normal, bold, italic) of the typeface.
         * e.g. NORMAL, BOLD, ITALIC, BOLD_ITALIC
         * @return The best matching typeface.
         */
        fun create(
            familyName: String?,
            @Typeface.Style style: Int
        ): Typeface {
            return create(
                getSystemDefaultTypeface(
                    familyName
                ), style
            )
        }

        /**
         * Create a typeface object that best matches the specified existing
         * typeface and the specified Style. Use this call if you want to pick a new
         * style from the same family of an existing typeface object. If family is
         * null, this selects from the default font's family.
         *
         *
         *
         * This method is not thread safe on API 27 or before.
         * This method is thread safe on API 28 or after.
         *
         *
         * @param family An existing [Typeface] object. In case of `null`, the default
         * typeface is used instead.
         * @param style  The style (normal, bold, italic) of the typeface.
         * e.g. NORMAL, BOLD, ITALIC, BOLD_ITALIC
         * @return The best matching typeface.
         */
        fun create(
            family: Typeface?,
            @Typeface.Style style: Int
        ): Typeface? {
            var family: Typeface? = family
            var style = style
            if (style and STYLE_MASK.inv() != 0) {
                style = NORMAL
            }
            if (family == null) {
                family = getDefault()
            }

            // Return early if we're asked for the same face/style
            if (family.mStyle == style) {
                return family
            }
            val ni: Long = family.native_instance
            var typeface: Typeface
            synchronized(sStyledCacheLock) {
                var styles: android.util.SparseArray<Typeface?> =
                    sStyledTypefaceCache.get(ni)
                if (styles == null) {
                    styles = android.util.SparseArray<Typeface>(4)
                    sStyledTypefaceCache.put(ni, styles)
                } else {
                    typeface = styles.get(style)
                    if (typeface != null) {
                        return typeface
                    }
                }
                typeface = Typeface(
                    nativeCreateFromTypeface(ni, style),
                    family.getSystemFontFamilyName()
                )
                styles.put(style, typeface)
            }
            return typeface
        }

        /**
         * Creates a typeface object that best matches the specified existing typeface and the specified
         * weight and italic style
         *
         * Below are numerical values and corresponding common weight names.
         * <table>
         * <thead>
         * <tr><th>Value</th><th>Common weight name</th></tr>
        </thead> *
         * <tbody>
         * <tr><td>100</td><td>Thin</td></tr>
         * <tr><td>200</td><td>Extra Light</td></tr>
         * <tr><td>300</td><td>Light</td></tr>
         * <tr><td>400</td><td>Normal</td></tr>
         * <tr><td>500</td><td>Medium</td></tr>
         * <tr><td>600</td><td>Semi Bold</td></tr>
         * <tr><td>700</td><td>Bold</td></tr>
         * <tr><td>800</td><td>Extra Bold</td></tr>
         * <tr><td>900</td><td>Black</td></tr>
        </tbody> *
        </table> *
         *
         *
         *
         * This method is thread safe.
         *
         *
         * @param family An existing [Typeface] object. In case of `null`, the default
         * typeface is used instead.
         * @param weight The desired weight to be drawn.
         * @param italic `true` if italic style is desired to be drawn. Otherwise, `false`
         * @return A [Typeface] object for drawing specified weight and italic style. Never
         * returns `null`
         *
         * @see .getWeight
         * @see .isItalic
         */
        fun create(
            family: Typeface?,
            @IntRange(from = 1, to = 1000) weight: Int, italic: Boolean
        ): Typeface {
            var family: Typeface? = family
            Preconditions.checkArgumentInRange(weight, 0, 1000, "weight")
            if (family == null) {
                family = getDefault()
            }
            return createWeightStyle(family, weight, italic)
        }

        private fun createWeightStyle(
            base: Typeface,
            @IntRange(from = 1, to = 1000) weight: Int, italic: Boolean
        ): Typeface {
            val key = weight shl 1 or if (italic) 1 else 0
            var typeface: Typeface
            synchronized(sWeightCacheLock) {
                var innerCache: android.util.SparseArray<Typeface?> =
                    sWeightTypefaceCache.get(base.native_instance)
                if (innerCache == null) {
                    innerCache = android.util.SparseArray<Typeface>(4)
                    sWeightTypefaceCache.put(
                        base.native_instance,
                        innerCache
                    )
                } else {
                    typeface = innerCache.get(key)
                    if (typeface != null) {
                        return typeface
                    }
                }
                typeface = Typeface(
                    nativeCreateFromTypefaceWithExactStyle(
                        base.native_instance,
                        weight,
                        italic
                    ),
                    base.getSystemFontFamilyName()
                )
                innerCache.put(key, typeface)
            }
            return typeface
        }

        /** @hide
         */
        fun createFromTypefaceWithVariation(
            family: Typeface?,
            axes: List<android.graphics.fonts.FontVariationAxis?>
        ): Typeface {
            val base: Typeface =
                if (family == null) DEFAULT else family
            return Typeface(
                nativeCreateFromTypefaceWithVariation(
                    base.native_instance,
                    axes
                ),
                base.getSystemFontFamilyName()
            )
        }

        /**
         * Returns one of the default typeface objects, based on the specified style
         *
         * @return the default typeface that corresponds to the style
         */
        fun defaultFromStyle(@Typeface.Style style: Int): Typeface {
            synchronized(SYSTEM_FONT_MAP_LOCK) {
                return sDefaults.get(
                    style
                )
            }
        }

        /**
         * Create a new typeface from the specified font data.
         *
         * @param mgr  The application's asset manager
         * @param path The file name of the font data in the assets directory
         * @return The new typeface.
         */
        fun createFromAsset(mgr: AssetManager, path: String): Typeface {
            Preconditions.checkNotNull(path) // for backward compatibility
            Preconditions.checkNotNull(mgr)
            val typeface: Typeface =
                Typeface.Builder(mgr, path).build()
            if (typeface != null) return typeface
            // check if the file exists, and throw an exception for backward compatibility
            try {
                mgr.open(path).use { inputStream -> }
            } catch (e: java.io.IOException) {
                throw java.lang.RuntimeException("Font asset not found $path")
            }
            return DEFAULT
        }

        /**
         * Creates a unique id for a given font provider and query.
         */
        private fun createProviderUid(authority: String, query: String): String {
            val builder: java.lang.StringBuilder = java.lang.StringBuilder()
            builder.append("provider:")
            builder.append(authority)
            builder.append("-")
            builder.append(query)
            return builder.toString()
        }

        /**
         * Create a new typeface from the specified font file.
         *
         * @param file The path to the font data.
         * @return The new typeface.
         */
        fun createFromFile(file: java.io.File?): Typeface {
            // For the compatibility reasons, leaving possible NPE here.
            // See android.graphics.cts.TypefaceTest#testCreateFromFileByFileReferenceNull
            val typeface: Typeface =
                Typeface.Builder(file).build()
            if (typeface != null) return typeface

            // check if the file exists, and throw an exception for backward compatibility
            if (!file.exists()) {
                throw java.lang.RuntimeException("Font asset not found " + file.getAbsolutePath())
            }
            return DEFAULT
        }

        /**
         * Create a new typeface from the specified font file.
         *
         * @param path The full path to the font data.
         * @return The new typeface.
         */
        fun createFromFile(path: String?): Typeface {
            Preconditions.checkNotNull(path) // for backward compatibility
            return createFromFile(java.io.File(path))
        }

        /**
         * Create a new typeface from an array of font families.
         *
         * @param families array of font families
         */
        @UnsupportedAppUsage(trackingBug = 123768928)
        @Deprecated("")
        private fun createFromFamilies(families: Array<android.graphics.FontFamily>): Typeface {
            val ptrArray = LongArray(families.size)
            for (i in families.indices) {
                ptrArray[i] = families[i].mNativePtr
            }
            return Typeface(
                nativeCreateFromArray(
                    ptrArray, 0, RESOLVE_BY_FONT_TABLE,
                    RESOLVE_BY_FONT_TABLE
                ), null
            )
        }

        /**
         * Create a new typeface from an array of android.graphics.fonts.FontFamily.
         *
         * @param families array of font families
         */
        private fun createFromFamilies(
            familyName: String,
            families: Array<android.graphics.fonts.FontFamily>?
        ): Typeface {
            val ptrArray = LongArray(families!!.size)
            for (i in families.indices) {
                ptrArray[i] = families[i].getNativePtr()
            }
            return Typeface(
                nativeCreateFromArray(
                    ptrArray,
                    0,
                    RESOLVE_BY_FONT_TABLE,
                    RESOLVE_BY_FONT_TABLE
                ), familyName
            )
        }

        /**
         * This method is used by supportlib-v27.
         *
         */
        @UnsupportedAppUsage(trackingBug = 123768395)
        @Deprecated("Use {@link android.graphics.fonts.FontFamily} instead.")
        private fun createFromFamiliesWithDefault(
            families: Array<android.graphics.FontFamily>, weight: Int, italic: Int
        ): Typeface {
            return createFromFamiliesWithDefault(
                families,
                DEFAULT_FAMILY,
                weight,
                italic
            )
        }

        /**
         * Create a new typeface from an array of font families, including
         * also the font families in the fallback list.
         * @param fallbackName the family name. If given families don't support characters, the
         * characters will be rendered with this family.
         * @param weight the weight for this family. In that case, the table information in the first
         * family's font is used. If the first family has multiple fonts, the closest to
         * the regular weight and upright font is used.
         * @param italic the italic information for this family. In that case, the table information in
         * the first family's font is used. If the first family has multiple fonts, the
         * closest to the regular weight and upright font is used.
         * @param families array of font families
         *
         */
        @UnsupportedAppUsage(trackingBug = 123768928)
        @Deprecated("Use {@link android.graphics.fonts.FontFamily} instead.")
        private fun createFromFamiliesWithDefault(
            families: Array<android.graphics.FontFamily>,
            fallbackName: String,
            weight: Int,
            italic: Int
        ): Typeface {
            val fallbackTypeface: Typeface =
                getSystemDefaultTypeface(fallbackName)
            val ptrArray = LongArray(families.size)
            for (i in families.indices) {
                ptrArray[i] = families[i].mNativePtr
            }
            return Typeface(
                nativeCreateFromArray(
                    ptrArray, fallbackTypeface.native_instance, weight, italic
                ), null
            )
        }

        private fun getSystemDefaultTypeface(familyName: String): Typeface {
            val tf: Typeface =
                sSystemFontMap.get(familyName)
            return if (tf == null) DEFAULT else tf
        }

        /** @hide
         */
        @VisibleForTesting
        fun initSystemDefaultTypefaces(
            fallbacks: Map<String?, Array<android.graphics.fonts.FontFamily?>?>,
            aliases: List<FontConfig.Alias>,
            outSystemFontMap: MutableMap<String?, Typeface?>
        ) {
            for ((key, value) in fallbacks) {
                outSystemFontMap[key] =
                    createFromFamilies(key, value)
            }
            for (i in aliases.indices) {
                val alias: FontConfig.Alias = aliases[i]
                if (outSystemFontMap.containsKey(alias.getName())) {
                    continue  // If alias and named family are conflict, use named family.
                }
                val base: Typeface = outSystemFontMap[alias.getOriginal()]
                    ?: // The missing target is a valid thing, some configuration don't have font files,
                    // e.g. wear devices. Just skip this alias.
                    continue
                val weight: Int = alias.getWeight()
                val newFace: Typeface =
                    if (weight == 400) base else Typeface(
                        nativeCreateWeightAlias(
                            base.native_instance,
                            weight
                        ), alias.getName()
                    )
                outSystemFontMap[alias.getName()] = newFace
            }
        }

        private fun registerGenericFamilyNative(
            familyName: String,
            typeface: Typeface?
        ) {
            if (typeface != null) {
                nativeRegisterGenericFamily(
                    familyName,
                    typeface.native_instance
                )
            }
        }

        /**
         * Create a serialized system font mappings.
         *
         * @hide
         */
        @TestApi
        @Throws(java.io.IOException::class, ErrnoException::class)
        fun serializeFontMap(fontMap: Map<String?, Typeface?>): android.os.SharedMemory {
            val nativePtrs = LongArray(fontMap.size)
            // The name table will not be large, so let's create a byte array in memory.
            val namesBytes: java.io.ByteArrayOutputStream = java.io.ByteArrayOutputStream()
            var i = 0
            for ((key, value) in fontMap) {
                nativePtrs[i++] = value.native_instance
                writeString(namesBytes, key)
            }
            // int (typefacesBytesCount), typefaces, namesBytes
            val typefaceBytesCountSize: Int = java.lang.Integer.BYTES
            val typefacesBytesCount: Int = nativeWriteTypefaces(
                null,
                typefaceBytesCountSize,
                nativePtrs
            )
            val sharedMemory: android.os.SharedMemory = android.os.SharedMemory.create(
                "fontMap", typefaceBytesCountSize + typefacesBytesCount + namesBytes.size()
            )
            val writableBuffer: java.nio.ByteBuffer =
                sharedMemory.mapReadWrite().order(java.nio.ByteOrder.BIG_ENDIAN)
            try {
                writableBuffer.putInt(typefacesBytesCount)
                val writtenBytesCount: Int =
                    nativeWriteTypefaces(
                        writableBuffer,
                        writableBuffer.position(),
                        nativePtrs
                    )
                if (writtenBytesCount != typefacesBytesCount) {
                    throw java.io.IOException(
                        String.format(
                            "Unexpected bytes written: %d, expected: %d",
                            writtenBytesCount, typefacesBytesCount
                        )
                    )
                }
                writableBuffer.position(writableBuffer.position() + writtenBytesCount)
                writableBuffer.put(namesBytes.toByteArray())
            } finally {
                android.os.SharedMemory.unmap(writableBuffer)
            }
            sharedMemory.setProtect(OsConstants.PROT_READ)
            return sharedMemory
        }
        // buffer's byte order should be BIG_ENDIAN.
        /**
         * Deserialize the font mapping from the serialized byte buffer.
         *
         *
         * Warning: the given `buffer` must outlive generated Typeface
         * objects in `out`. In production code, this is guaranteed by
         * storing the buffer in [.sSystemFontMapBuffer].
         * If you call this method in a test, please make sure to destroy the
         * generated Typeface objects by calling
         * [.releaseNativeObjectForTest].
         *
         * @hide
         */
        @TestApi
        @Throws(java.io.IOException::class)
        fun deserializeFontMap(
            buffer: java.nio.ByteBuffer, out: MutableMap<String?, Typeface?>
        ): LongArray {
            val typefacesBytesCount: Int = buffer.getInt()
            // Note: Do not call buffer.slice(), as nativeReadTypefaces() expects
            // that buffer.address() is page-aligned.
            val nativePtrs: LongArray =
                nativeReadTypefaces(buffer, buffer.position())
                    ?: throw java.io.IOException("Could not read typefaces")
            out.clear()
            buffer.position(buffer.position() + typefacesBytesCount)
            for (nativePtr in nativePtrs) {
                val name: String = readString(buffer)
                out[name] = Typeface(nativePtr, name)
            }
            return nativePtrs
        }

        private fun readString(buffer: java.nio.ByteBuffer): String {
            val length: Int = buffer.getInt()
            val bytes = ByteArray(length)
            buffer.get(bytes)
            return String(bytes)
        }

        @Throws(java.io.IOException::class)
        private fun writeString(bos: java.io.ByteArrayOutputStream, value: String) {
            val bytes: ByteArray = value.toByteArray()
            writeInt(bos, bytes.size)
            bos.write(bytes)
        }

        private fun writeInt(bos: java.io.ByteArrayOutputStream, value: Int) {
            // Write in the big endian order.
            bos.write(value shr 24 and 0xFF)
            bos.write(value shr 16 and 0xFF)
            bos.write(value shr 8 and 0xFF)
            bos.write(value and 0xFF)
        }

        @set:Throws(
            java.io.IOException::class,
            ErrnoException::class
        )
        @set:UiThread
        var systemFontMap: Map<String, Typeface>
            /** @hide
             */
            get() {
                synchronized(SYSTEM_FONT_MAP_LOCK) { return sSystemFontMap }
            }
            /** @hide
             */
            set(sharedMemory) {
                if (sSystemFontMapBuffer != null) {
                    // Apps can re-send BIND_APPLICATION message from their code. This is a work around to
                    // detect it and avoid crashing.
                    if (sharedMemory == null || sharedMemory == sSystemFontMapSharedMemory) {
                        return
                    }
                    throw java.lang.UnsupportedOperationException(
                        "Once set, buffer-based system font map cannot be updated"
                    )
                }
                sSystemFontMapSharedMemory = sharedMemory
                android.os.Trace.traceBegin(android.os.Trace.TRACE_TAG_GRAPHICS, "setSystemFontMap")
                try {
                    if (sharedMemory == null) {
                        // FontManagerService is not started. This may happen in FACTORY_TEST_LOW_LEVEL
                        // mode for example.
                        loadPreinstalledSystemFontMap()
                        return
                    }
                    sSystemFontMapBuffer =
                        sharedMemory.mapReadOnly().order(java.nio.ByteOrder.BIG_ENDIAN)
                    val systemFontMap: Map<String, Typeface> =
                        android.util.ArrayMap<String, Typeface>()
                    val nativePtrs: LongArray =
                        deserializeFontMap(
                            sSystemFontMapBuffer,
                            systemFontMap
                        )

                    // Initialize native font APIs. The native font API will read fonts.xml by itself if
                    // Typeface is initialized with loadPreinstalledSystemFontMap.
                    for (ptr in nativePtrs) {
                        nativeAddFontCollections(ptr)
                    }
                    setSystemFontMap(systemFontMap)
                } finally {
                    android.os.Trace.traceEnd(android.os.Trace.TRACE_TAG_GRAPHICS)
                }
            }
        /**
         * Deserialize font map and set it as system font map. This method should be called at most once
         * per process.
         */
        /** @hide
         */
        @VisibleForTesting
        fun setSystemFontMap(systemFontMap: Map<String?, Typeface?>) {
            synchronized(SYSTEM_FONT_MAP_LOCK) {
                sSystemFontMap.clear()
                sSystemFontMap.putAll(systemFontMap)

                // We can't assume DEFAULT_FAMILY available on Roboletric.
                if (sSystemFontMap.containsKey(DEFAULT_FAMILY)) {
                    setDefault(
                        sSystemFontMap.get(
                            DEFAULT_FAMILY
                        )
                    )
                }

                // Set up defaults and typefaces exposed in public API
                // Use sDefaultTypeface here, because create(String, int) uses DEFAULT as fallback.
                nativeForceSetStaticFinalField(
                    "DEFAULT",
                    create(
                        sDefaultTypeface,
                        0
                    )
                )
                nativeForceSetStaticFinalField(
                    "DEFAULT_BOLD",
                    create(
                        sDefaultTypeface,
                        BOLD
                    )
                )
                nativeForceSetStaticFinalField(
                    "SANS_SERIF",
                    create("sans-serif", 0)
                )
                nativeForceSetStaticFinalField(
                    "SERIF",
                    create("serif", 0)
                )
                nativeForceSetStaticFinalField(
                    "MONOSPACE",
                    create("monospace", 0)
                )
                sDefaults =
                    arrayOf<Typeface>(
                        DEFAULT,
                        DEFAULT_BOLD,
                        create(
                            null as String?,
                            ITALIC
                        ),
                        create(
                            null as String?,
                            BOLD_ITALIC
                        )
                    )

                // A list of generic families to be registered in native.
                // https://www.w3.org/TR/css-fonts-4/#generic-font-families
                val genericFamilies = arrayOf(
                    "serif", "sans-serif", "cursive", "fantasy", "monospace", "system-ui"
                )
                for (genericFamily in genericFamilies) {
                    registerGenericFamilyNative(
                        genericFamily,
                        systemFontMap[genericFamily]
                    )
                }
            }
        }

        /**
         * Change default typefaces for testing purpose.
         *
         * Note: The existing TextView or Paint instance still holds the old Typeface.
         *
         * @param defaults array of [default, default_bold, default_italic, default_bolditalic].
         * @param genericFamilies array of [sans-serif, serif, monospace]
         * @return return the old defaults and genericFamilies
         * @hide
         */
        @TestApi
        fun changeDefaultFontForTest(
            defaults: List<Typeface?>,
            genericFamilies: List<Typeface?>
        ): android.util.Pair<List<Typeface>, List<Typeface>> {
            synchronized(SYSTEM_FONT_MAP_LOCK) {
                val oldDefaults: List<Typeface> =
                    java.util.Arrays.asList(*sDefaults)
                sDefaults = defaults.toTypedArray()
                setDefault(defaults[0])
                val oldGenerics: java.util.ArrayList<Typeface> =
                    java.util.ArrayList<Typeface>()
                oldGenerics.add(sSystemFontMap.get("sans-serif"))
                sSystemFontMap.put(
                    "sans-serif",
                    genericFamilies[0]
                )
                oldGenerics.add(sSystemFontMap.get("serif"))
                sSystemFontMap.put(
                    "serif",
                    genericFamilies[1]
                )
                oldGenerics.add(sSystemFontMap.get("monospace"))
                sSystemFontMap.put(
                    "monospace",
                    genericFamilies[2]
                )
                return android.util.Pair<List<Typeface>, List<Typeface>>(
                    oldDefaults,
                    oldGenerics
                )
            }
        }

        init {
            // Preload Roboto-Regular.ttf in Zygote for improving app launch performance.
            preloadFontFile("/system/fonts/Roboto-Regular.ttf")
            preloadFontFile("/system/fonts/RobotoStatic-Regular.ttf")
            val locale: String = SystemProperties.get("persist.sys.locale", "en-US")
            val script: String =
                ULocale.addLikelySubtags(ULocale.forLanguageTag(locale)).getScript()
            val config: FontConfig = SystemFonts.getSystemPreinstalledFontConfig()
            for (i in 0 until config.getFontFamilies().size()) {
                val family: FontConfig.FontFamily = config.getFontFamilies().get(i)
                if (!family.getLocaleList().isEmpty()) {
                    nativeRegisterLocaleList(
                        family.getLocaleList().toLanguageTags()
                    )
                }
                var loadFamily = false
                for (j in 0 until family.getLocaleList().size()) {
                    val fontScript: String = ULocale.addLikelySubtags(
                        ULocale.forLocale(family.getLocaleList().get(j))
                    ).getScript()
                    loadFamily = fontScript == script
                    if (loadFamily) {
                        break
                    }
                }
                if (loadFamily) {
                    for (j in 0 until family.getFontList().size()) {
                        preloadFontFile(
                            family.getFontList().get(j).getFile().getAbsolutePath()
                        )
                    }
                }
            }
        }

        private fun preloadFontFile(filePath: String) {
            val file: java.io.File = java.io.File(filePath)
            if (file.exists()) {
                android.util.Log.i(
                    TAG,
                    "Preloading " + file.getAbsolutePath()
                )
                nativeWarmUpCache(filePath)
            }
        }

        /** @hide
         */
        @VisibleForTesting
        fun destroySystemFontMap() {
            synchronized(SYSTEM_FONT_MAP_LOCK) {
                for (typeface in sSystemFontMap.values) {
                    typeface.releaseNativeObjectForTest()
                }
                sSystemFontMap.clear()
                if (sSystemFontMapBuffer != null) {
                    android.os.SharedMemory.unmap(sSystemFontMapBuffer)
                }
                sSystemFontMapBuffer = null
                sSystemFontMapSharedMemory = null
                synchronized(sStyledCacheLock) {
                    destroyTypefaceCacheLocked(
                        sStyledTypefaceCache
                    )
                }
                synchronized(sWeightCacheLock) {
                    destroyTypefaceCacheLocked(
                        sWeightTypefaceCache
                    )
                }
            }
        }

        private fun destroyTypefaceCacheLocked(cache: android.util.LongSparseArray<android.util.SparseArray<Typeface>>) {
            for (i in 0 until cache.size()) {
                val array: android.util.SparseArray<Typeface> = cache.valueAt(i)
                for (j in 0 until array.size()) {
                    array.valueAt(j).releaseNativeObjectForTest()
                }
            }
            cache.clear()
        }

        /** @hide
         */
        fun loadPreinstalledSystemFontMap() {
            val fontConfig: FontConfig = SystemFonts.getSystemPreinstalledFontConfig()
            val fallback: Map<String, Array<android.graphics.fonts.FontFamily>> =
                SystemFonts.buildSystemFallback(fontConfig)
            val typefaceMap: Map<String, Typeface> =
                SystemFonts.buildSystemTypefaces(fontConfig, fallback)
            setSystemFontMap(typefaceMap)
        }

        init {
            if (!ENABLE_LAZY_TYPEFACE_INITIALIZATION) {
                loadPreinstalledSystemFontMap()
            }
        }

        private external fun nativeCreateFromTypeface(native_instance: Long, style: Int): Long
        private external fun nativeCreateFromTypefaceWithExactStyle(
            native_instance: Long, weight: Int, italic: Boolean
        ): Long

        // TODO: clean up: change List<FontVariationAxis> to FontVariationAxis[]
        private external fun nativeCreateFromTypefaceWithVariation(
            native_instance: Long, axes: List<android.graphics.fonts.FontVariationAxis>
        ): Long

        @UnsupportedAppUsage
        private external fun nativeCreateWeightAlias(native_instance: Long, weight: Int): Long
        @UnsupportedAppUsage(
            maxTargetSdk = android.os.Build.VERSION_CODES.R,
            trackingBug = 170729553
        )
        private external fun nativeCreateFromArray(
            familyArray: LongArray, fallbackTypeface: Long, weight: Int, italic: Int
        ): Long

        private external fun nativeGetSupportedAxes(native_instance: Long): IntArray?
        @CriticalNative
        private external fun nativeSetDefault(nativePtr: Long)
        @CriticalNative
        private external fun nativeGetStyle(nativePtr: Long): Int
        @CriticalNative
        private external fun nativeGetWeight(nativePtr: Long): Int
        @CriticalNative
        private external fun nativeGetReleaseFunc(): Long
        private external fun nativeRegisterGenericFamily(str: String, nativePtr: Long)
        private external fun nativeWriteTypefaces(
            buffer: java.nio.ByteBuffer?, position: Int, nativePtrs: LongArray
        ): Int

        private external fun nativeReadTypefaces(
            buffer: java.nio.ByteBuffer,
            position: Int
        ): LongArray?

        private external fun nativeForceSetStaticFinalField(
            fieldName: String,
            typeface: Typeface
        )

        @CriticalNative
        private external fun nativeAddFontCollections(nativePtr: Long)
        private external fun nativeWarmUpCache(fileName: String)
        @FastNative
        private external fun nativeRegisterLocaleList(locales: String)
    }
}
