# Baseline Profile (Generator and Benchmark)

This module has been created following the recommendations
that can be seen [here](https://developer.android.com/topic/performance/baselineprofiles/overview)

The end goal is to optimize startup time and basic use cases of our application by creating 2
profiles `startup-prof.txt` and `baseline-prof.txt` that are located in
the following folder [Dashlane/src/release/generated](../Dashlane/src/release/generated).

These profiles are then bundled into our app and a device can use them optimize some
classes, making the whole app load faster than optimizing everything at runtime.

Finally, you currently have to generate and benchmark the profiles on your local machine.
Indeed, doing blind optimizations has little interest and the benchmarking has to be done only on
real devices to measure the effectiveness of the generated profile.

Updating the profiles once a year is enough, unless the first screens seen on startup have
changed (SplashScreen, Get Started, Login/Create Account, OnBoarding)

## Generating new profiles

Launch an emulator and then run the following gradlew command

```
./gradlew :Dashlane:generateReleaseBaselineProfile
```

More details can be found in
the [BaselineProfileGenerator](src/main/java/com/dashlane/baselineprofile/BaselineProfileGenerator.kt)
class

Some tests will be run multiple times, when it completes, new profile will be automatically
generated and put inside the appropriate folder.

Once you have done a benchmark
using [StartupBenchmarks](src/main/java/com/dashlane/baselineprofile/StartupBenchmarks.kt)
, commit the new `startup-prof. txt` and `baseline-prof.txt` files

## Benchmarking profiles

Directly inside Android Studio,
open [StartupBenchmarks](src/main/java/com/dashlane/baselineprofile/StartupBenchmarks.kt)
and run all the Instrumented Tests.

Each test will be run 10 times with and without profiles.

Warning: You have to run the tests on a real device

Once the tests are done, an output like this one will show up on the right panel of the Run window

```
StartupBenchmarks_startupCompilationBaselineProfiles
timeToInitialDisplayMs   min 555.0,   median 567.2,   max 602.2
Traces: Iteration 0 1 2 3 4 5 6 7 8 9


StartupBenchmarks_startupCompilationNone
timeToInitialDisplayMs   min 665.5,   median 689.1,   max 752.5
Traces: Iteration 0 1 2 3 4 5 6 7 8 9
```

You can then compare the startup time of the app without and with profiles

Clicking on one of the iteration will let you access some details inside the profiler.

## Updating the tests

If one of the test is failing, no profiles will be generated, you will then have to update
[BaselineProfileGenerator](src/main/java/com/dashlane/baselineprofile/BaselineProfileGenerator.kt)
to make it work again.

Please note the module is completely independent so you can't access app resources.

## Troubleshooting

Sometimes, it can be challenging to run the tests on a real device because we have a BackupAgent
that is able to identify the last logged in user and skip the onboarding.
In order to make sure you are in a clean state, uninstall the app and disable the BackupManager on
your device

```
adb shell bmgr enable true
```

The app will be automatically installed and uninstalled by the tests