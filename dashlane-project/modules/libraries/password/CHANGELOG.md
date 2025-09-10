# Change Log

## Version 2.0.3
- Removed PasswordStrengthSuggestion.AVOID_RECENT_YEARS as it's not a case from zxcvbn

## Version 2.0.2
- Fix PasswordStrength parsing crash due to wrong value for PasswordStrengthSuggestion.AVOID_PERSONAL_YEARS

## Version 2.0.1
- Remove Moshi that is now unused

## Version 2.0.0
- Remove password-strength-android and password-strength-jdk
- Switch from JS to Java/Kotlin implementation
- Fix associatePasswordsWithPasswordStrengthScore creating silent issue in main app due to not mapping with "distinct" passwords

## Version 1.1.0

- Password generator module.

## Version 1.0.0

- Initial version containing password strength evaluation using ZXCVBN 
  implementation, evaluated in JavaScript.
