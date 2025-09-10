# Password Tools

This project contains various tools for manipulating passwords.

## Password Generator

Generate a random password matching some criteria.

```kotlin
val generator = PasswordGenerator()
val generatedPassword = generator.generate(
    length = 12,
    withDigits = true,
    withLetters = true,
    withSymbols = false,
    avoidAmbiguousChars = true
)
```

The `password-generator` module contains the generator API and implementation,
while the `password-generator-android-benchmark` module contains a performance
test of the generation. Running the benchmark test on a Samsung Galaxy S21
gives generation times of approximately 50 microseconds (0.05 milliseconds)
regardless of the password length. These results indicate that password 
generation is a quick enough operation that it doesn't need to be done on a 
background thread.

## Password Strength

The password strength is evaluated using the implementation of the
[zxcvbn](https://github.com/nulab/zxcvbn4j) algorithm in Java.

To evaluate the strength of a password, obtain an implementation of the
[`PasswordStrengthEvaluator`](password-strength/src/main/kotlin/com/dashlane/passwordstrength/PasswordStrengthEvaluator.kt)
interface from the `password-strength` module, using the
`PasswordStrengthEvaluator` factory function in the `password-strength-zxcvbn`.

Use the `getPasswordStrength` suspending function to evaluate the strength of
a password, which includes its score, some warnings if it's weak, and some
suggestions to make it stronger if relevant.

```kotlin
val evaluator = PasswordStrengthEvaluator()
val password = "Dashlane12"
launch {
    val passwordStrength = evaluator.getPasswordStrength(password)
    val passwordStrengthScore = strength.score
    // Use the strength score...
}
```

If only the score of the password is needed, and it's being evaluated for a
large number of passwords, then prefer the
`associatePasswordsWithPasswordStrengthScore` function which is does batch
evaluations for better performance.

```kotlin
val evaluator = PasswordStrengthEvaluator()
val passwords = listOf(
    "Qwerty12",
    "Dashlane12"
)
launch {
    val passwordStrengthScores =
        evaluator.associatePasswordsWithPasswordStrengthScore(passwords)
    val weakPasswordScores =
        passwordStrengthScores.filterValues { it < PasswordStrengthScore.SOMEWHAT_GUESSABLE }
    val weakPasswords = weakPasswordScores.keys
    // Handle weak passwords ...
}
```

If performing repeated evaluations similar sets of passwords, the
`PasswordStrengthEvaluator.cache` extension function is available, to create
an evaluator which caches its evaluation results. The caller is responsible
for handling the scope of the cache using the `cache` parameter. Repeated
calls to a cached evaluator to evaluate strength for the same passwords will
fetch the previous result from the cache

```kotlin
val evaluator = PasswordStrengthEvaluator()
val cache = PasswordStrengthEvaluatorCache()
val cachedEvaluator = evaluator.cache(cache) 
```

If performing evaluations on potentially large passwords where exact results
aren't strictly required, the `PasswordStrengthEvaluator.truncate` extension
function is available, to truncate passwords if necessary during their
evaluation, only considering their start, and therefore avoid unnecessarily
large computation. It's encouraged to set up password truncation on the
evaluator when using a cache, to avoid polluting the cache with very large
passwords.

```kotlin
val evaluator = PasswordStrengthEvaluator()
val cache = PasswordStrengthEvaluatorCache()
val cachedEvaluator = evaluator.cache(cache).truncated()
```

### Command-line Interface

The `password-strength-cli` module provides a command-line interface to
evaluate the strength of passwords.

To build the executable, run the following command.

```shell
./gradlew password-strength-cli:install`
```

The command can then be executed like in the following example.

```shell
password-strength-cli/build/install/password-strength-cli/bin/password-strength-cli Azerty12
```

Which produces the following output containing all the information obtained
from the evaluation of the password's strength.

```text
Strength: VERY_GUESSABLE
Warning: This is a very common password
Suggestion:
- Add another word or two. Uncommon words are better.
- Capitalization doesn't help very much
```

Alternatively, if several passwords are provided as command-line arguments,
then only their strength score is evaluated and displayed.

```shell
password-strength-cli/build/install/password-strength-cli/bin/password-strength-cli Azerty12 Dashlane12 OIhserg23452gwertasdr
```

Which produces the following output.

```
Azerty12: VERY_GUESSABLE
Dashlane12: SAFELY_UNGUESSABLE
OIhserg23452gwertasdr: VERY_UNGUESSABLE
```

### Module Overview

For this
reason, the implementation is highly platform-specific, depending on the
JavaScript engine available within the JVM environment.
This feature is therefore split into several modules.

| Module                                | Description                                                                                                                                                                                                                                                                                                             |
|---------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `password-strength`                   | Contains the main `PasswordStrengthEvaluator` API to evaluate the strength of a password, returning a `PasswordStrength` instance. Most libraries should stick to depending on this module, while the application which integrates them should depend on either `password-strength-android` or `password-strength-jdk`. |
| `password-strength-android-benchmark` | Benchmark module for Android implementation.                                                                                                                                                                                                                                                                            |
| `password-strength-cli`               | Command-line interface using Kotlin implementation, to evaluate strength of passwords.                                                                                                                                                                                                                                  |
| `password-strength-zxcvbn`            | Core Kotlin implementation                                                                                                                                                                                                                                                                                              |

### Benchmark Results

| Device                                       | Test                   | Median (ns) |  Mean (ns) |  Min (ns) |  Max (ns) | Stddev (ns) |
|----------------------------------------------|------------------------|------------:|-----------:|----------:|----------:|------------:|
| Google Pixel 2 Android API 30                | Individual Calculation |   772821640 |  752464151 | 576194485 |           |    88051678 |
| Google Pixel 2 Android API 30                | Batch Calculation      |   518154322 |  505112352 | 444888638 |           |    33374323 |
| Xiaomi Redmi 5A Android API 27               | Individual Calculation |   955266406 |  958543261 | 938549427 |           |    16669242 |
| Xiaomi Redmi 5A Android API 27               | Batch Calculation      |   789617265 |  818527040 | 781663854 |           |    75842752 |
| Samsung SM-G991B (Galaxy S21) Android API 31 | Individual Calculation |   203256154 |  204043096 | 180232693 |           |     8065158 |
| Samsung SM-G991B (Galaxy S21) Android API 31 | Batch Calculation      |   109914365 |  110648109 | 108447769 |           |     4826673 |
| Samsung SM-A515F (Galaxy A51) Android API 31 | Individual Calculation |  1280807403 | 1184773011 | 730439269 |           |   191652417 |
| Samsung SM-A515F (Galaxy A51) Android API 31 | Batch Calculation      |   316717981 |  316873344 | 314071731 |           |     1390099 |
| Google Pixel 6 Android API 31                | Individual Calculation |   278999817 |            | 234156291 | 323016887 |    21891331 |
| Google Pixel 6 Android API 31                | Batch Calculation      |   117100443 |            | 115572022 | 149729492 |     4864009 |

Batch calculation cuts the time by the following ratio, looking at median times.

| Device                                       | Gain |
|----------------------------------------------|------|
| Google Pixel 2 Android API 30                | 33%  |
| Xiaomi Redmi 5A Android API 27               | 17%  |
| Samsung SM-G991B (Galaxy S21) Android API 31 | 54%  |
| Samsung SM-A515F (Galaxy A51) Android API 31 | 75%  |
| Google Pixel 6 Android API 31                | 58%  |
