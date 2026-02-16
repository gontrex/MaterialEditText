package com.rengwuxian.materialedittext;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatEditText;

import com.rengwuxian.materialedittext.validation.METLengthChecker;
import com.rengwuxian.materialedittext.validation.METValidator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * EditText in Material Design
 * <p/>
 * author:rengwuxian
 * <p/>
 */
@SuppressWarnings({"unused"})
public class MaterialEditText extends AppCompatEditText {

  @Retention(RetentionPolicy.SOURCE)
  @IntDef({FLOATING_LABEL_NONE, FLOATING_LABEL_NORMAL, FLOATING_LABEL_HIGHLIGHT})
  public @interface FloatingLabelType {
  }

  public static final int FLOATING_LABEL_NONE = 0;
  public static final int FLOATING_LABEL_NORMAL = 1;
  public static final int FLOATING_LABEL_HIGHLIGHT = 2;

  /**
   * Spacing between the main text and the inner top padding.
   */
  private int extraPaddingTop;

  /**
   * Spacing between the main text and the inner bottom padding.
   */
  private int extraPaddingBottom;

  /**
   * Extra spacing between the main text and the left, actually for the left icon.
   */
  private int extraPaddingLeft;

  /**
   * Extra spacing between the main text and the right, actually for the right icon.
   */
  private int extraPaddingRight;

  /**
   * Floating label's text size.
   */
  private int floatingLabelTextSize;

  /**
   * Floating label's text color.
   */
  private int floatingLabelTextColor;

  /**
   * Bottom text's size.
   */
  private int bottomTextSize;

  /**
   * Spacing between the main text and the floating label.
   */
  private int floatingLabelPadding;

  /**
   * Spacing between the main text and the bottom components (bottom ellipsis, helper/error text, characters counter).
   */
  private int bottomSpacing;

  /**
   * Whether the floating label should be shown. <p>
   * Default is false.
   */
  private boolean floatingLabelEnabled;

  /**
   * Whether to highlight the floating label's text color when focused (with the main color). <p>
   * Default is true.
   */
  private boolean highlightFloatingLabel;

  /**
   * Whether the floating label should always be opaque. <p>
   * Default is false.
   */
  private boolean floatingLabelAlwaysOpaque;

  /**
   * Base color of the line and the texts. <p>
   * Default is black.
   */
  private int baseColor;

  /**
   * Inner top padding
   * -- GETTER --
   *  Get inner top padding, not the real paddingTop.
   */
  private int innerPaddingTop;

  /**
   * Inner bottom padding
   * -- GETTER --
   *  Get inner bottom padding, not the real paddingBottom.
   */
  private int innerPaddingBottom;

  /**
   * Inner left padding
   * -- GETTER --
   *  Get inner left padding, not the real paddingLeft.
   */
  private int innerPaddingLeft;

  /**
   * Inner right padding
   * -- GETTER --
   *  Get inner right padding, not the real paddingRight.
   */
  private int innerPaddingRight;

  /**
   * Underline's highlight color, and the highlight color of the floating label if app:highlightFloatingLabel is set true in the xml. default is black(when app:darkTheme is false) or white(when app:darkTheme is true)
   */
  private int primaryColor;

  /**
   * Color for when something is wrong.(e.g. exceeding max characters).
   */
  private int errorColor;

  /**
   * Min characters count limit. 0 means no limit. <p>
   * Default is 0. <p>
   * NOTE: the character counter will increase the View's height.
   */
  private int minCharacters;

  /**
   * Max characters count limit. 0 means no limit. <p>
   * Default is 0. <p>
   * NOTE: The character counter will increase the View's height.
   */
  private int maxCharacters;

  /**
   * Whether to show the bottom ellipsis in singleLine mode. <p>
   * Default is false. <p>
   * NOTE: the bottom ellipsis will increase the View's height.
   */
  private boolean singleLineEllipsis;

  /**
   * Always show the floating label, instead of animating it in/out.<p>
   * Default is false.
   */
  private boolean floatingLabelAlwaysShown;

  /**
   * Always show the helper text, no matter if the edit text is focused.<p>
   * Default is false.
   */
  private boolean helperTextAlwaysShown;

  /**
   * Dynamic check whether to show the bottom ellipsis.
   */
  private boolean showBottomEllipsis;

  /**
   * Bottom ellipsis's height.
   */
  private int bottomEllipsisSize;

  /**
   * Min bottom lines count.
   */
  private int minBottomLines;

  /**
   * Reserved bottom text lines count, no matter if there is some helper/error text.
   */
  private int minBottomTextLines;

  /**
   * Real-time bottom lines count. Used for bottom extending/collapsing animation.
   */
  private float currentBottomLines;

  /**
   * Bottom lines count.
   */
  private float bottomLines;

  /**
   * Helper text at the bottom.
   */
  private String helperText;

  /**
   * Helper text color.
   */
  private int helperTextColor = -1;

  /**
   * Error text for manually invoked {@link #setError(CharSequence)}.
   */
  private String tempErrorText;

  /**
   * Animation fraction of the floating label (0 as totally hidden).
   */
  private float floatingLabelFraction;

  /**
   * Whether the floating label is being shown.
   */
  private boolean floatingLabelShown;

  /**
   * Floating label's focusFraction.
   */
  private float focusFraction;

  /**
   * The font used for the accent texts (floating label, error/helper text, character counter, etc.)
   */
  private Typeface accentTypeface;

  /**
   * Text for the floatLabel if different from the hint.
   */
  private CharSequence floatingLabelText;

  /**
   * Whether or not to show the underline.<p>
   * Shown by default.
   */
  private boolean hideUnderline;

  /**
   * Underline's color
   * -- GETTER --
   *  get the color of the underline for normal state.
   */
  private int underlineColor;

  /**
   * Whether to validate as soon as the text has changed.<p>
   * Default is false.
   */
  private boolean autoValidate;

  /**
   * Whether the characters count is valid.
   */
  private boolean charactersCountValid;

  /**
   * Whether use animation to show/hide the floating label.
   */
  private boolean floatingLabelAnimating;

  /**
   * Whether check the characters count at the beginning it's shown.
   */
  private boolean checkCharactersCountAtBeginning;

  /**
   * Left Icon.
   */
  private Bitmap[] iconLeftBitmaps;

  /**
   * Right Icon.
   */
  private Bitmap[] iconRightBitmaps;

  /**
   * Clear Button.
   */
  private Bitmap[] clearButtonBitmaps;

  /**
   * Auto validate when focus lost.
   */
  private boolean validateOnFocusLost;

  private boolean showClearButton;
  private boolean firstShown;
  private int iconSize;
  private int iconOuterWidth;
  private int iconOuterHeight;
  private int iconPadding;
  private boolean clearButtonTouched;
  private boolean clearButtonClicking;
  private ColorStateList textColorStateList;
  private ColorStateList textColorHintStateList;
  private final ArgbEvaluator focusEvaluator = new ArgbEvaluator();
  Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
  TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
  StaticLayout textLayout;
  ObjectAnimator labelAnimator;
  ObjectAnimator labelFocusAnimator;
  ObjectAnimator bottomLinesAnimator;
  OnFocusChangeListener innerFocusChangeListener;
  OnFocusChangeListener outerFocusChangeListener;
  private List<METValidator> validators;
  private METLengthChecker lengthChecker;

  public MaterialEditText(Context context) {
    super(context);
    init(context, null);
  }

  public MaterialEditText(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  // Compatibility
  @SuppressLint("ObsoleteSdkInt")
  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  public MaterialEditText(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {
    if (isInEditMode()) return;

    iconSize = getPixel(32);
    iconOuterWidth = getPixel(40);  // Default 48
    iconOuterHeight = getPixel(34); // Default 32

    bottomSpacing = getResources().getDimensionPixelSize(R.dimen.inner_components_spacing);
    bottomEllipsisSize = getResources().getDimensionPixelSize(R.dimen.bottom_ellipsis_height);

    int defaultBaseColor = Color.BLACK;

    try (TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MaterialEditText)) {
      textColorStateList = typedArray.getColorStateList(R.styleable.MaterialEditText_met_textColor);
      textColorHintStateList = typedArray.getColorStateList(R.styleable.MaterialEditText_met_textColorHint);
      baseColor = typedArray.getColor(R.styleable.MaterialEditText_met_baseColor, defaultBaseColor);

      int defaultPrimaryColor;
      TypedValue typedValue = new TypedValue();
      if (context.getTheme().resolveAttribute(android.R.attr.colorPrimary, typedValue, true)) {
        defaultPrimaryColor = typedValue.data;
      } else {
        defaultPrimaryColor = baseColor;
      }

      primaryColor = typedArray.getColor(R.styleable.MaterialEditText_met_primaryColor, defaultPrimaryColor);
      setFloatingLabelInternal(typedArray.getInt(R.styleable.MaterialEditText_met_floatingLabel, 0));
      errorColor = typedArray.getColor(R.styleable.MaterialEditText_met_errorColor, Color.parseColor("#e7492E"));
      minCharacters = typedArray.getInt(R.styleable.MaterialEditText_met_minCharacters, 0);
      maxCharacters = typedArray.getInt(R.styleable.MaterialEditText_met_maxCharacters, 0);
      singleLineEllipsis = typedArray.getBoolean(R.styleable.MaterialEditText_met_singleLineEllipsis, false);
      helperText = typedArray.getString(R.styleable.MaterialEditText_met_helperText);
      helperTextColor = typedArray.getColor(R.styleable.MaterialEditText_met_helperTextColor, -1);
      minBottomTextLines = typedArray.getInt(R.styleable.MaterialEditText_met_minBottomTextLines, 0);
      String fontPathForAccent = typedArray.getString(R.styleable.MaterialEditText_met_accentTypeface);
      if (fontPathForAccent != null && !isInEditMode()) {
        accentTypeface = getCustomTypeface(fontPathForAccent);
        textPaint.setTypeface(accentTypeface);
      }
      String fontPathForView = typedArray.getString(R.styleable.MaterialEditText_met_typeface);
      if (fontPathForView != null && !isInEditMode()) {
        Typeface typeface = getCustomTypeface(fontPathForView);
        setTypeface(typeface);
      }
      floatingLabelText = typedArray.getString(R.styleable.MaterialEditText_met_floatingLabelText);
      if (floatingLabelText == null) floatingLabelText = getHint();
      floatingLabelPadding = typedArray.getDimensionPixelSize(R.styleable.MaterialEditText_met_floatingLabelPadding, bottomSpacing);
      floatingLabelTextSize = typedArray.getDimensionPixelSize(R.styleable.MaterialEditText_met_floatingLabelTextSize, getResources().getDimensionPixelSize(R.dimen.floating_label_text_size));
      floatingLabelTextColor = typedArray.getColor(R.styleable.MaterialEditText_met_floatingLabelTextColor, -1);
      floatingLabelAnimating = typedArray.getBoolean(R.styleable.MaterialEditText_met_floatingLabelAnimating, true);
      bottomTextSize = typedArray.getDimensionPixelSize(R.styleable.MaterialEditText_met_bottomTextSize, getResources().getDimensionPixelSize(R.dimen.bottom_text_size));
      hideUnderline = typedArray.getBoolean(R.styleable.MaterialEditText_met_hideUnderline, false);
      underlineColor = typedArray.getColor(R.styleable.MaterialEditText_met_underlineColor, -1);
      autoValidate = typedArray.getBoolean(R.styleable.MaterialEditText_met_autoValidate, false);
      iconLeftBitmaps = generateIconBitmaps(typedArray.getResourceId(R.styleable.MaterialEditText_met_iconLeft, -1));
      iconRightBitmaps = generateIconBitmaps(typedArray.getResourceId(R.styleable.MaterialEditText_met_iconRight, -1));
      showClearButton = typedArray.getBoolean(R.styleable.MaterialEditText_met_clearButton, false);
      clearButtonBitmaps = generateIconBitmaps(R.drawable.met_ic_clear);
      iconPadding = typedArray.getDimensionPixelSize(R.styleable.MaterialEditText_met_iconPadding, getPixel(16));
      floatingLabelAlwaysShown = typedArray.getBoolean(R.styleable.MaterialEditText_met_floatingLabelAlwaysShown, false);
      helperTextAlwaysShown = typedArray.getBoolean(R.styleable.MaterialEditText_met_helperTextAlwaysShown, false);
      validateOnFocusLost = typedArray.getBoolean(R.styleable.MaterialEditText_met_validateOnFocusLost, false);
      checkCharactersCountAtBeginning = typedArray.getBoolean(R.styleable.MaterialEditText_met_checkCharactersCountAtBeginning, true);
    }

    int[] paddings = new int[]{
            android.R.attr.padding,       // 0
            android.R.attr.paddingLeft,   // 1
            android.R.attr.paddingTop,    // 2
            android.R.attr.paddingRight,  // 3
            android.R.attr.paddingBottom  // 4
    };

    try (TypedArray paddingsTypedArray = context.obtainStyledAttributes(attrs, paddings)) {
      int padding = paddingsTypedArray.getDimensionPixelSize(0, 0);
      innerPaddingLeft = paddingsTypedArray.getDimensionPixelSize(1, padding);
      innerPaddingTop = paddingsTypedArray.getDimensionPixelSize(2, padding);
      innerPaddingRight = paddingsTypedArray.getDimensionPixelSize(3, padding);
      innerPaddingBottom = paddingsTypedArray.getDimensionPixelSize(4, padding);
    }

    setBackground(null);
    if (singleLineEllipsis) {
      TransformationMethod transformationMethod = getTransformationMethod();
      setSingleLine();
      setTransformationMethod(transformationMethod);
    }

    initMinBottomLines();
    initPadding();
    initText();
    initFloatingLabel();
    initTextWatcher();
    checkCharactersCount();
  }

  private void initPadding() {
    extraPaddingTop = floatingLabelEnabled ? floatingLabelTextSize + floatingLabelPadding : floatingLabelPadding;
    textPaint.setTextSize(bottomTextSize);
    Paint.FontMetrics textMetrics = textPaint.getFontMetrics();
    extraPaddingBottom = (int) ((textMetrics.descent - textMetrics.ascent) * currentBottomLines) + (hideUnderline ? bottomSpacing : bottomSpacing * 2);
    extraPaddingLeft = iconLeftBitmaps == null ? 0 : (iconOuterWidth + iconPadding);
    extraPaddingRight = iconRightBitmaps == null ? 0 : (iconOuterWidth + iconPadding);
    correctPaddings();
  }

  /**
   * Calculate {@link #minBottomLines}.
   */
  private void initMinBottomLines() {
    boolean extendBottom = minCharacters > 0 || maxCharacters > 0 || singleLineEllipsis || tempErrorText != null || helperText != null;
    currentBottomLines = minBottomLines = minBottomTextLines > 0 ? minBottomTextLines : extendBottom ? 1 : 0;
  }

  private void initFloatingLabel() {
    // Observe the text changing
    addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }

      @Override
      public void afterTextChanged(Editable s) {
        if (floatingLabelEnabled) {
          if (TextUtils.isEmpty(s)) {
            if (floatingLabelShown) {
              floatingLabelShown = false;
              getLabelAnimator().reverse();
            }
          } else if (!floatingLabelShown) {
            floatingLabelShown = true;
            getLabelAnimator().start();
          }
        }
      }
    });
    // Observe the focus state to animate the floating label's text color appropriately
    innerFocusChangeListener = (v, hasFocus) -> {
      if (floatingLabelEnabled && highlightFloatingLabel) {
        if (hasFocus) {
          getLabelFocusAnimator().start();
        } else {
          getLabelFocusAnimator().reverse();
        }
      }
      correctPaddings();
      if (validateOnFocusLost && !hasFocus) validate();
      if (outerFocusChangeListener != null) outerFocusChangeListener.onFocusChange(v, hasFocus);
    };
    super.setOnFocusChangeListener(innerFocusChangeListener);
  }

  private void initText() {
    if (!TextUtils.isEmpty(getText())) {
      CharSequence text = getText();
      setText(null);
      resetHintTextColor();
      setText(text);
      setSelection(text.length());
      floatingLabelFraction = 1;
      floatingLabelShown = true;
    } else {
      resetHintTextColor();
    }
    resetTextColor();
  }

  private void initTextWatcher() {
    addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // Not used
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        // Not used
      }

      @Override
      public void afterTextChanged(Editable s) {
        checkCharactersCount();
        if (autoValidate) {
          validate();
        } else {
          setError(null);
        }
        correctPaddings();
        postInvalidate();
      }
    });
  }

  /* ######## PUBLIC ######## */
  public String getString() {
    if (getText() == null) return "";
    return getText().toString().trim();
  }

  public void setIconLeft(@DrawableRes int res) {
    iconLeftBitmaps = generateIconBitmaps(res);
    initPadding();
  }

  public void setIconLeft(Drawable drawable) {
    iconLeftBitmaps = generateIconBitmaps(drawable);
    initPadding();
  }

  public void setIconLeft(Bitmap bitmap) {
    iconLeftBitmaps = generateIconBitmaps(bitmap);
    initPadding();
  }

  public void setIconRight(@DrawableRes int res) {
    iconRightBitmaps = generateIconBitmaps(res);
    initPadding();
  }

  public void setIconRight(Drawable drawable) {
    iconRightBitmaps = generateIconBitmaps(drawable);
    initPadding();
  }

  public void setIconRight(Bitmap bitmap) {
    iconRightBitmaps = generateIconBitmaps(bitmap);
    initPadding();
  }

  public void setShowClearButton(boolean show) {
    showClearButton = show;
    correctPaddings();
  }

  public void setFloatingLabelFraction(float floatingLabelFraction) {
    this.floatingLabelFraction = floatingLabelFraction;
    invalidate();
  }

  public void setFocusFraction(float focusFraction) {
    this.focusFraction = focusFraction;
    invalidate();
  }

  public void setCurrentBottomLines(float currentBottomLines) {
    this.currentBottomLines = currentBottomLines;
    initPadding();
  }

  public void setFloatingLabelAlwaysShown(boolean floatingLabelAlwaysShown) {
    this.floatingLabelAlwaysShown = floatingLabelAlwaysShown;
    invalidate();
  }

  public void setHelperTextAlwaysShown(boolean helperTextAlwaysShown) {
    this.helperTextAlwaysShown = helperTextAlwaysShown;
    invalidate();
  }

  @Nullable
  public Typeface getAccentTypeface() {
    return accentTypeface;
  }

  /**
   * Set typeface used for the accent texts (floating label, error/helper text, character counter, etc.)
   */
  public void setAccentTypeface(Typeface accentTypeface) {
    this.accentTypeface = accentTypeface;
    this.textPaint.setTypeface(accentTypeface);
    postInvalidate();
  }

  /**
   * Set whether or not to hide the underline (shown by default).
   * <p/>
   * The positions of text below will be adjusted accordingly (error/helper text, character counter, ellipses, etc.)
   * <p/>
   * NOTE: You probably don't want to hide this if you have any subtext features of this enabled, as it can look weird to not have a dividing line between them.
   */
  public void setHideUnderline(boolean hideUnderline) {
    this.hideUnderline = hideUnderline;
    initPadding();
    postInvalidate();
  }

  /**
   * Set the color of the underline for normal state.
   */
  public void setUnderlineColor(int color) {
    this.underlineColor = color;
    postInvalidate();
  }

  /**
   * Set the floating label text.
   * <p/>
   * Pass null to force fallback to use hint's value.
   */
  public void setFloatingLabelText(@Nullable CharSequence floatingLabelText) {
    this.floatingLabelText = floatingLabelText == null ? getHint() : floatingLabelText;
    postInvalidate();
  }

  public void setFloatingLabelTextSize(int size) {
    floatingLabelTextSize = size;
    initPadding();
  }

  public void setFloatingLabelTextColor(int color) {
    this.floatingLabelTextColor = color;
    postInvalidate();
  }

  public void setBottomTextSize(int size) {
    bottomTextSize = size;
    initPadding();
  }

  /**
   * Use {@link #setPaddings(int, int, int, int)} instead, or the paddingTop and the paddingBottom may be set incorrectly.
   */
  @Deprecated
  @Override
  public final void setPadding(int left, int top, int right, int bottom) {
    super.setPadding(left, top, right, bottom);
  }

  /**
   * Use this method instead of {@link #setPadding(int, int, int, int)} to automatically set the paddingTop and the paddingBottom correctly.
   */
  public void setPaddings(int left, int top, int right, int bottom) {
    innerPaddingTop = top;
    innerPaddingBottom = bottom;
    innerPaddingLeft = left;
    innerPaddingRight = right;
    correctPaddings();
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (!firstShown) firstShown = true;
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    if (changed) adjustBottomLines();
  }

  public void setBaseColor(int color) {
    if (baseColor != color) baseColor = color;
    initText();
    postInvalidate();
  }

  public void setPrimaryColor(int color) {
    primaryColor = color;
    postInvalidate();
  }

  /**
   * Same function as {@link #setTextColor(int)}. (Directly overriding the built-in one could cause some error, so use this method instead.)
   */
  public void setMetTextColor(int color) {
    textColorStateList = ColorStateList.valueOf(color);
    resetTextColor();
  }

  /**
   * Same function as {@link #setTextColor(ColorStateList)}. (Directly overriding the built-in one could cause some error, so use this method instead.)
   */
  public void setMetTextColor(ColorStateList colors) {
    textColorStateList = colors;
    resetTextColor();
  }

  /**
   * Same function as {@link #setHintTextColor(int)}. (The built-in one is a final method that can't be overridden, so use this method instead.)
   */
  public void setMetHintTextColor(int color) {
    textColorHintStateList = ColorStateList.valueOf(color);
    resetHintTextColor();
  }

  /**
   * Same function as {@link #setHintTextColor(ColorStateList)}. (The built-in one is a final method that can't be overridden, so use this method instead.)
   */
  public void setMetHintTextColor(ColorStateList colors) {
    textColorHintStateList = colors;
    resetHintTextColor();
  }

  public void setFloatingLabel(@FloatingLabelType int mode) {
    setFloatingLabelInternal(mode);
    initPadding();
  }

  public void setFloatingLabelPadding(int padding) {
    floatingLabelPadding = padding;
    postInvalidate();
  }

  public void setSingleLineEllipsis() {
    setSingleLineEllipsis(true);
  }

  public void setSingleLineEllipsis(boolean enabled) {
    singleLineEllipsis = enabled;
    initMinBottomLines();
    initPadding();
    postInvalidate();
  }

  public void setMaxCharacters(int max) {
    maxCharacters = max;
    initMinBottomLines();
    initPadding();
    postInvalidate();
  }

  public void setMinCharacters(int min) {
    minCharacters = min;
    initMinBottomLines();
    initPadding();
    postInvalidate();
  }

  public void setMinBottomTextLines(int lines) {
    minBottomTextLines = lines;
    initMinBottomLines();
    initPadding();
    postInvalidate();
  }

  public void setAutoValidate(boolean autoValidate) {
    this.autoValidate = autoValidate;
    if (autoValidate) validate();
  }

  public void setErrorColor(int color) {
    errorColor = color;
    postInvalidate();
  }

  public void setHelperText(CharSequence helperText) {
    this.helperText = helperText == null ? null : helperText.toString();
    if (adjustBottomLines()) postInvalidate();
  }

  public void setHelperTextColor(int color) {
    helperTextColor = color;
    postInvalidate();
  }

  @Override
  public void setError(CharSequence errorText) {
    tempErrorText = errorText == null ? null : errorText.toString();
    if (adjustBottomLines()) postInvalidate();
  }

  @Override
  public CharSequence getError() {
    return tempErrorText;
  }

  /**
   * If the main text matches the regex.
   *
   * @deprecated use the new validator interface to add your own custom validator.
   */
  @Deprecated
  public boolean isValid(String regex) {
    if (regex == null || TextUtils.isEmpty(getText())) return false;
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(getText());
    return matcher.matches();
  }

  /**
   * Check if the main text matches the regex, and set the error text if not.
   *
   * @return true if it matches the regex, false if not.
   * @deprecated use the new validator interface to add your own custom validator.
   */
  @Deprecated
  public boolean validate(String regex, CharSequence errorText) {
    boolean isValid = isValid(regex);
    if (!isValid) setError(errorText);
    postInvalidate();
    return isValid;
  }

  /**
   * Run validation on a single validator instance.
   *
   * @param validator Validator to check
   * @return True if valid, false if not
   */
  public boolean validateWith(@NonNull METValidator validator) {
    CharSequence text = getText();
    boolean isValid = validator.isValid(text, TextUtils.isEmpty(text));
    if (!isValid) setError(validator.getErrorMessage());
    postInvalidate();
    return isValid;
  }

  /**
   * Check all validators, sets the error text if not.
   * <p/>
   * NOTE: this stops at the first validator to report invalid.
   *
   * @return True if all validators pass, false if not.
   */
  public boolean validate() {
    if (!hasValidators()) return true;

    CharSequence text = getText();
    boolean isEmpty = TextUtils.isEmpty(text);
    boolean isValid = true;

    for (METValidator validator : validators) {
      //noinspection ConstantConditions
      isValid = isValid && validator.isValid(text, isEmpty);
      if (!isValid) {
        setError(validator.getErrorMessage());
        break;
      }
    }
    if (isValid) setError(null);
    postInvalidate();
    return isValid;
  }

  public boolean hasValidators() {
    return validators != null && !validators.isEmpty();
  }

  /**
   * Adds a new validator to the View's list of validators.
   * <p/>
   * This will be checked with the others in {@link #validate()}.
   *
   * @param validator Validator to add.
   * @return This instance, for easy chaining.
   */
  public MaterialEditText addValidator(METValidator validator) {
    if (validators == null) this.validators = new ArrayList<>();
    this.validators.add(validator);
    return this;
  }

  @Nullable
  public List<METValidator> getValidators() {
    return this.validators;
  }

  public void clearValidators() {
    if (this.validators == null) return;
    this.validators.clear();
  }

  @Override
  public void setOnFocusChangeListener(OnFocusChangeListener listener) {
    if (innerFocusChangeListener == null) {
      super.setOnFocusChangeListener(listener);
    } else {
      outerFocusChangeListener = listener;
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (singleLineEllipsis && getScrollX() > 0 && event.getAction() == MotionEvent.ACTION_DOWN && event.getX() < getPixel(4 * 5)
            && event.getY() > getHeight() - extraPaddingBottom - innerPaddingBottom && event.getY() < getHeight() - innerPaddingBottom) {
      setSelection(0);
      return false;
    }
    if (hasFocus() && showClearButton) {
      switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
          if (insideClearButton(event)) {
            clearButtonTouched = true;
            clearButtonClicking = true;
            return true;
          }
          break;
        case MotionEvent.ACTION_MOVE:
          if (clearButtonClicking && !insideClearButton(event)) {
            clearButtonClicking = false;
          }
          if (clearButtonTouched) {
            return true;
          }
          break;
        case MotionEvent.ACTION_UP:
          if (clearButtonClicking) {
            if (!TextUtils.isEmpty(getText())) {
              setText(null);
            }
            clearButtonClicking = false;
          }
          if (clearButtonTouched) {
            clearButtonTouched = false;
            return true;
          }
          break;
        case MotionEvent.ACTION_CANCEL:
          clearButtonTouched = false;
          clearButtonClicking = false;
          break;
      }
    }
    return super.onTouchEvent(event);
  }

  @Override
  protected void onDraw(@NonNull Canvas canvas) {
    int startX = getScrollX() + (iconLeftBitmaps == null ? 0 : (iconOuterWidth + iconPadding));
    int endX = getScrollX() + (iconRightBitmaps == null ? getWidth() : getWidth() - iconOuterWidth - iconPadding);
    int lineStartY = getScrollY() + getHeight() - getPaddingBottom();

    showBottomEllipsis = hasFocus() && singleLineEllipsis && getScrollX() != 0;

    // Draw the icon(s)
    paint.setAlpha(255);
    if (iconLeftBitmaps != null) {
      Bitmap icon = iconLeftBitmaps[!isInternalValid() ? 3 : !isEnabled() ? 2 : hasFocus() ? 1 : 0];
      int iconLeft = startX - iconPadding - iconOuterWidth + (iconOuterWidth - icon.getWidth()) / 2;
      int iconTop = lineStartY + bottomSpacing - iconOuterHeight + (iconOuterHeight - icon.getHeight()) / 2;
      canvas.drawBitmap(icon, iconLeft, iconTop, paint);
    }
    if (iconRightBitmaps != null) {
      Bitmap icon = iconRightBitmaps[!isInternalValid() ? 3 : !isEnabled() ? 2 : hasFocus() ? 1 : 0];
      int iconRight = endX + iconPadding + (iconOuterWidth - icon.getWidth()) / 2;
      int iconTop = lineStartY + bottomSpacing - iconOuterHeight + (iconOuterHeight - icon.getHeight()) / 2;
      canvas.drawBitmap(icon, iconRight, iconTop, paint);
    }

    // Draw the clear button
    if (hasFocus() && showClearButton && !TextUtils.isEmpty(getText())) {
      paint.setAlpha(255);

      int buttonLeft = isRTL() ? startX : endX - iconOuterWidth;
      Bitmap clearButtonBitmap = clearButtonBitmaps[0];
      buttonLeft += (iconOuterWidth - clearButtonBitmap.getWidth()) / 2;

      Paint.FontMetrics fontMetrics = getPaint().getFontMetrics();
      float textCenterY = getBaseline() + (fontMetrics.ascent + fontMetrics.descent) / 2;
      int iconTop = (int) (textCenterY - (clearButtonBitmap.getHeight() / 2f));

      canvas.drawBitmap(clearButtonBitmap, buttonLeft, iconTop, paint);
    }

    // Draw the underline
    if (!hideUnderline) {
      lineStartY += bottomSpacing;
      if (!isInternalValid()) { // not valid
        paint.setColor(errorColor);
        canvas.drawRect(startX, lineStartY, endX, lineStartY + getPixel(2), paint);
      } else if (!isEnabled()) { // disabled
        paint.setColor(underlineColor != -1 ? underlineColor : baseColor & 0x00ffffff | 0x44000000);
        float interval = getPixel(1);
        for (float xOffset = 0; xOffset < getWidth(); xOffset += interval * 3) {
          canvas.drawRect(startX + xOffset, lineStartY, startX + xOffset + interval, lineStartY + getPixel(1), paint);
        }
      } else if (hasFocus()) { // focused
        paint.setColor(primaryColor);
        canvas.drawRect(startX, lineStartY, endX, lineStartY + getPixel(2), paint);
      } else { // normal
        paint.setColor(underlineColor != -1 ? underlineColor : baseColor & 0x00ffffff | 0x1E000000);
        canvas.drawRect(startX, lineStartY, endX, lineStartY + getPixel(1), paint);
      }
    }

    textPaint.setTextSize(bottomTextSize);
    Paint.FontMetrics textMetrics = textPaint.getFontMetrics();
    float relativeHeight = -textMetrics.ascent - textMetrics.descent;
    float bottomTextPadding = bottomTextSize + textMetrics.ascent + textMetrics.descent;

    // Draw the characters counter
    if ((hasFocus() && hasCharactersCounter()) || !charactersCountValid) {
      textPaint.setColor(charactersCountValid ? (baseColor & 0x00ffffff | 0x44000000) : errorColor);
      String charactersCounterText = getCharactersCounterText();
      canvas.drawText(charactersCounterText, isRTL() ? startX : endX - textPaint.measureText(charactersCounterText), lineStartY + bottomSpacing + relativeHeight, textPaint);
    }

    // Draw the bottom text
    int destBottomLines = getBottomLines();
    bottomLines = getBottomLines();

    if (textLayout != null) {
      if (tempErrorText != null || ((helperTextAlwaysShown || hasFocus()) && !TextUtils.isEmpty(helperText))) { // error text or helper text
        textPaint.setColor(tempErrorText != null ? errorColor : helperTextColor != -1 ? helperTextColor : (baseColor & 0x00ffffff | 0x44000000));
        canvas.save();
        if (isRTL()) {
          canvas.translate(endX - textLayout.getWidth(), lineStartY + bottomSpacing - bottomTextPadding);
        } else {
          float bottomOffset = showBottomEllipsis ? getBottomTextLeftOffset() : getPixel(4);
          canvas.translate(startX + bottomOffset, lineStartY + bottomSpacing - bottomTextPadding);
        }
        textLayout.draw(canvas);
        canvas.restore();
      }
    }

    // Draw the floating label
    if (floatingLabelEnabled && !TextUtils.isEmpty(floatingLabelText)) {
      textPaint.setTextSize(floatingLabelTextSize);
      // Calculate the text color
      textPaint.setColor((Integer) focusEvaluator.evaluate(focusFraction, floatingLabelTextColor != -1 ? floatingLabelTextColor : (baseColor & 0x00ffffff | 0x44000000), primaryColor));

      // Calculate the horizontal position
      float floatingLabelWidth = textPaint.measureText(floatingLabelText.toString());
      int floatingLabelStartX;
      if ((getGravity() & Gravity.END) == Gravity.END || isRTL()) {
        floatingLabelStartX = (int) (endX - floatingLabelWidth);
      } else if ((getGravity() & Gravity.START) == Gravity.START) {
        floatingLabelStartX = startX;
      } else {
        floatingLabelStartX = startX + (int) (innerPaddingLeft + (getWidth() - innerPaddingLeft - innerPaddingRight - floatingLabelWidth) / 2);
      }

      // Calculate the vertical position
      int distance = floatingLabelPadding;
      int floatingLabelStartY = (int) (innerPaddingTop + floatingLabelTextSize + floatingLabelPadding - distance * (floatingLabelAlwaysShown ? 1 : floatingLabelFraction) + getScrollY());

      // Calculate the alpha
      int alpha = ((int) ((floatingLabelAlwaysShown ? 1 : floatingLabelFraction) * 0xff * (0.74f * focusFraction + 0.26f) * (floatingLabelTextColor != -1 ? 1 : Color.alpha(floatingLabelTextColor) / 256f)));
      textPaint.setAlpha(alpha);

      // Draw the floating label
      canvas.drawText(floatingLabelText.toString(), floatingLabelStartX, floatingLabelStartY, textPaint);
    }

    // Draw the bottom ellipsis
    if (showBottomEllipsis) {
      paint.setColor(isInternalValid() ? primaryColor : errorColor);
      float startY = lineStartY + bottomSpacing;
      int ellipsisStartX;
      if (isRTL()) {
        ellipsisStartX = endX;
      } else {
        ellipsisStartX = startX;
      }
      int signum = isRTL() ? -1 : 1;
      canvas.drawCircle(ellipsisStartX + (float) (signum * bottomEllipsisSize) / 2, startY + (float) bottomEllipsisSize / 2, (float) bottomEllipsisSize / 2, paint);
      canvas.drawCircle(ellipsisStartX + (float) (signum * bottomEllipsisSize * 5) / 2, startY + (float) bottomEllipsisSize / 2, (float) bottomEllipsisSize / 2, paint);
      canvas.drawCircle(ellipsisStartX + (float) (signum * bottomEllipsisSize * 9) / 2, startY + (float) bottomEllipsisSize / 2, (float) bottomEllipsisSize / 2, paint);
    }

    // Draw the original things
    super.onDraw(canvas);
  }

  /* ######## PRIVATE ######## */
  /**
   * Only used to draw the bottom line.
   */
  private boolean isInternalValid() {
    return tempErrorText == null && charactersCountValid;
  }

  private boolean isRTL() {
    Configuration config = getResources().getConfiguration();
    return config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
  }

  private boolean hasCharactersCounter() {
    return minCharacters > 0 || maxCharacters > 0;
  }

  private int getPixel(int dp) {
    return Density.dp2px(getContext(), dp);
  }

  private Typeface getCustomTypeface(@NonNull String fontPath) {
    return Typeface.createFromAsset(getContext().getAssets(), fontPath);
  }

  private int getButtonsCount() {
    return showClearButton ? 1 : 0;
  }

  private ObjectAnimator getLabelAnimator() {
    if (labelAnimator == null) {
      labelAnimator = ObjectAnimator.ofFloat(this, "floatingLabelFraction", 0f, 1f);
    }
    labelAnimator.setDuration(floatingLabelAnimating ? 300 : 0);
    return labelAnimator;
  }

  private ObjectAnimator getLabelFocusAnimator() {
    if (labelFocusAnimator == null) {
      labelFocusAnimator = ObjectAnimator.ofFloat(this, "focusFraction", 0f, 1f);
    }
    return labelFocusAnimator;
  }

  private ObjectAnimator getBottomLinesAnimator(float destBottomLines) {
    if (bottomLinesAnimator == null) {
      bottomLinesAnimator = ObjectAnimator.ofFloat(this, "currentBottomLines", destBottomLines);
    } else {
      bottomLinesAnimator.cancel();
      bottomLinesAnimator.setFloatValues(destBottomLines);
    }
    return bottomLinesAnimator;
  }

  private int getBottomTextLeftOffset() {
    return isRTL() ? getCharactersCounterWidth() : getBottomEllipsisWidth();
  }

  private int getBottomTextRightOffset() {
    return isRTL() ? getBottomEllipsisWidth() : getCharactersCounterWidth();
  }

  private int getCharactersCounterWidth() {
    return hasCharactersCounter() ? (int) textPaint.measureText(getCharactersCounterText()) : 0;
  }

  private int getBottomEllipsisWidth() {
    return singleLineEllipsis ? (bottomEllipsisSize * 5 + getPixel(4)) : 0;
  }

  private String getCharactersCounterText() {
    String text;
    if (minCharacters <= 0) {
      text = isRTL() ? maxCharacters + " / " + checkLength(getText()) : checkLength(getText()) + " / " + maxCharacters;
    } else if (maxCharacters <= 0) {
      text = isRTL() ? "+" + minCharacters + " / " + checkLength(getText()) : checkLength(getText()) + " / " + minCharacters + "+";
    } else {
      text = isRTL() ? maxCharacters + "-" + minCharacters + " / " + checkLength(getText()) : checkLength(getText()) + " / " + minCharacters + "-" + maxCharacters;
    }
    return text;
  }

  private void checkCharactersCount() {
    if ((!firstShown && !checkCharactersCountAtBeginning) || !hasCharactersCounter()) {
      charactersCountValid = true;
    } else {
      CharSequence text = getText();
      int count = text == null ? 0 : checkLength(text);
      charactersCountValid = (count >= minCharacters && (maxCharacters <= 0 || count <= maxCharacters));
    }
  }

  private int checkLength(CharSequence text) {
    if (lengthChecker == null) return text.length();
    return lengthChecker.getLength(text);
  }

  private void setFloatingLabelInternal(int mode) {
    switch (mode) {
      case FLOATING_LABEL_NORMAL:
        floatingLabelEnabled = true;
        highlightFloatingLabel = false;
        break;
      case FLOATING_LABEL_HIGHLIGHT:
        floatingLabelEnabled = true;
        highlightFloatingLabel = true;
        break;
      default:
        floatingLabelEnabled = false;
        highlightFloatingLabel = false;
        break;
    }
  }

  /**
   * Set paddings to the correct values
   */
  private void correctPaddings() {
    int buttonsWidthLeft = 0, buttonsWidthRight = 0;

    boolean clearButtonVisible = hasFocus() && showClearButton && !TextUtils.isEmpty(getText());
    int buttonsWidth = clearButtonVisible ? iconOuterWidth * getButtonsCount() : 0;

    if (isRTL()) {
      buttonsWidthLeft = buttonsWidth;
    } else {
      buttonsWidthRight = buttonsWidth;
    }
    super.setPadding(innerPaddingLeft + extraPaddingLeft + buttonsWidthLeft, innerPaddingTop + extraPaddingTop, innerPaddingRight + extraPaddingRight + buttonsWidthRight, innerPaddingBottom + extraPaddingBottom);
  }

  private Bitmap[] generateIconBitmaps(@DrawableRes int origin) {
    if (origin == -1) return null;

    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeResource(getResources(), origin, options);
    int size = Math.max(options.outWidth, options.outHeight);
    options.inSampleSize = size > iconSize ? size / iconSize : 1;
    options.inJustDecodeBounds = false;
    return generateIconBitmaps(BitmapFactory.decodeResource(getResources(), origin, options));
  }

  private Bitmap[] generateIconBitmaps(Drawable drawable) {
    if (drawable == null) return null;

    Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    drawable.draw(canvas);
    return generateIconBitmaps(Bitmap.createScaledBitmap(bitmap, iconSize, iconSize, false));
  }

  private Bitmap[] generateIconBitmaps(Bitmap origin) {
    if (origin == null) return null;

    Bitmap[] iconBitmaps = new Bitmap[4];
    origin = scaleIcon(origin);
    iconBitmaps[0] = origin.copy(Bitmap.Config.ARGB_8888, true);
    Canvas canvas = new Canvas(iconBitmaps[0]);
    canvas.drawColor(baseColor & 0x00ffffff | (Colors.isLight(baseColor) ? 0xff000000 : 0x8a000000), PorterDuff.Mode.SRC_IN);
    iconBitmaps[1] = origin.copy(Bitmap.Config.ARGB_8888, true);
    canvas = new Canvas(iconBitmaps[1]);
    canvas.drawColor(primaryColor, PorterDuff.Mode.SRC_IN);
    iconBitmaps[2] = origin.copy(Bitmap.Config.ARGB_8888, true);
    canvas = new Canvas(iconBitmaps[2]);
    canvas.drawColor(baseColor & 0x00ffffff | (Colors.isLight(baseColor) ? 0x4c000000 : 0x42000000), PorterDuff.Mode.SRC_IN);
    iconBitmaps[3] = origin.copy(Bitmap.Config.ARGB_8888, true);
    canvas = new Canvas(iconBitmaps[3]);
    canvas.drawColor(errorColor, PorterDuff.Mode.SRC_IN);
    return iconBitmaps;
  }

  private Bitmap scaleIcon(Bitmap origin) {
    int width = origin.getWidth();
    int height = origin.getHeight();
    int size = Math.max(width, height);
    if (size == iconSize) {
      return origin;
    } else if (size > iconSize) {
      int scaledWidth;
      int scaledHeight;
      if (width > iconSize) {
        scaledWidth = iconSize;
        scaledHeight = (int) (iconSize * ((float) height / width));
      } else {
        scaledHeight = iconSize;
        scaledWidth = (int) (iconSize * ((float) width / height));
      }
      return Bitmap.createScaledBitmap(origin, scaledWidth, scaledHeight, false);
    } else {
      return origin;
    }
  }

  private int getBottomLines() {
    int destBottomLines;
    if (getWidth() != 0 && (tempErrorText != null || helperText != null)) {
      Layout.Alignment alignment = (getGravity() & Gravity.END) == Gravity.END || isRTL() ?
              Layout.Alignment.ALIGN_OPPOSITE : (getGravity() & Gravity.START) == Gravity.START ?
              Layout.Alignment.ALIGN_NORMAL : Layout.Alignment.ALIGN_CENTER;
      int bottomLeftOffset = showBottomEllipsis ? getBottomTextLeftOffset() : getPixel(8);
      textLayout = new StaticLayout(tempErrorText != null ? tempErrorText : helperText, textPaint,
//              getWidth() - getBottomTextLeftOffset() - getBottomTextRightOffset() - getPaddingLeft() - getPaddingRight(),
              getWidth() - bottomLeftOffset - getBottomTextRightOffset() - getPaddingLeft(),
              alignment, 1.0f, 0.0f, true);
      destBottomLines = Math.max(textLayout.getLineCount(), minBottomTextLines);
    } else {
      destBottomLines = minBottomLines;
    }
    return destBottomLines;
  }

  /**
   * @return True, if adjustments were made that require the view to be invalidated.
   */
  private boolean adjustBottomLines() {
    // Bail out if we have a zero width; lines will be adjusted during next layout.
    if (getWidth() == 0) return false;

    textPaint.setTextSize(bottomTextSize);
    int destBottomLines = getBottomLines();
    if (bottomLines != destBottomLines) getBottomLinesAnimator(destBottomLines).start();
    bottomLines = destBottomLines;
    return true;
  }

  private void resetTextColor() {
    if (textColorStateList == null) {
      textColorStateList = new ColorStateList(new int[][]{new int[]{android.R.attr.state_enabled}, EMPTY_STATE_SET},
              new int[]{baseColor & 0x00ffffff | 0xdf000000, baseColor & 0x00ffffff | 0x44000000});
      setTextColor(textColorStateList);
    } else {
      setTextColor(textColorStateList);
    }
  }

  private void resetHintTextColor() {
    if (textColorHintStateList == null) {
      setHintTextColor(baseColor & 0x00ffffff | 0x44000000);
    } else {
      setHintTextColor(textColorHintStateList);
    }
  }

  private boolean insideClearButton(MotionEvent event) {
    float x = event.getX();
    float y = event.getY();
    int startX = iconLeftBitmaps == null ? 0 : (iconOuterWidth + iconPadding);
    int endX = iconRightBitmaps == null ? getWidth() : getWidth() - iconOuterWidth - iconPadding;
    int buttonLeft = isRTL() ? startX : endX - iconOuterWidth;
    int buttonTop = getScrollY() + getHeight() - getPaddingBottom() + (bottomSpacing / 2) - iconOuterHeight;
    return x >= buttonLeft && x < buttonLeft + iconOuterWidth && y >= buttonTop && y < buttonTop + iconOuterHeight;
  }

}