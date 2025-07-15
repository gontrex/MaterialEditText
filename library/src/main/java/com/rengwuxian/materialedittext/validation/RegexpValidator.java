package com.rengwuxian.materialedittext.validation;

import androidx.annotation.NonNull;

import java.util.regex.Pattern;

/**
 * Custom validator for Regexes
 */
@SuppressWarnings({"unused"})
public class RegexpValidator extends METValidator {

  private final Pattern pattern;

  public RegexpValidator(@NonNull String errorMessage, @NonNull String regex) {
    super(errorMessage);
    pattern = Pattern.compile(regex);
  }

  public RegexpValidator(@NonNull String errorMessage, @NonNull Pattern pattern) {
    super(errorMessage);
    this.pattern = pattern;
  }

  @Override
  public boolean isValid(CharSequence text, boolean isEmpty) {
    return pattern.matcher(text).matches();
  }

}
