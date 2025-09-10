# Change Log

## Version 3.6.0
_2024-12_04_

- Add view id and ancestry list to AutofillSummaryHint.Entry

## Version 3.5.0
_2024-11-15_

- Remove support for accessibility-based autofill
- Update minSdk to API 29
- Update compileSdk to API 35

## Version 3.4.0
_2024-05-28_

- Migrate to version catalog and bumped dependencies where possible

## Version 3.3.0
_2023-12-06_

- Add hasHtmlAttributes to node view to prevent an autofill hack where a browser would autofill a
  native form with in addition of the webview

## Version 3.2.0
_2023-11-07_

- Timeout the autofill detection after 4 second

## Version 3.1.0
_2023-10-25_

- Look at brother view to obtain more information about the field
- Add more dumps where no suggestion should be provided

## Version 3.0.5
_2023-01-26_

- Higher hint accurate needed to add a candidate as password or username
- Don't add password candidate without a password type field

## Version 3.0.4
_2022-10-06_

- No more focused field require. To fix a Firefox issue.

## Version 3.0.3
_2022-10-06_

- 35% to 20% accuracy for username input type. We gave too much accuracy to this attribute because a developer can put edit text type as "email" but not necessarily expect an email.
- 20% to 30% for hint completer when a password field is found. The goal is to give more accuracy to a username when a password is found. Most of the time if we found a password field there is a username field.

## Version 3.0.2
_2022-08-30_

- Fix the check for multiple hints to be at least >40% of accuracy

## Version 3.0.1
_2022-08-26_

- Fix some issue found during library implementation

## Version 3.0.0
_2022-08-22_

- Refactor the detection with a scoring system. [See the documentation](https://confluence.dashlane.com/display/AND/Implementation+of+scoring+system+for+autofill)
- Better credit card detection with a credit card extractor
- Translation of keywords used to guess hint from fields attributes

## Version 2.5.3
_2022-08-08_

- Fix crash when accessing package name on Android 8

## Version 2.5.2
_2022-07-20_

- Fix Tesla login autofill by adding some new field in the guessing part

## Version 2.5.1
_2022-02-02_

- Add more autofill dumps from JIRA tickets:
  - ANDROID-7830
  - & ANDROID-7839

## Version 2.5.0
_2022-02-01_

- Add supports of even more Android Webview tags ([source]|(https://chromium.googlesource.com/chromium/src/+/refs/heads/main/components/android_autofill/browser/java/src/org/chromium/components/autofill/AutofillProvider.java))
    - "crowdsourcing-autofill-hints"
    - "computed-autofill-hints"
    - "crowdsourcing-predictions-autofill-hints"

- Add new autofill dumps for Credit cards scenarios + Update unit tests

## Version 2.4.0
_2021-10-25_

- Add support of [Chromium's Autofill field types]|(https://github.com/adobe/chromium/blob/master/chrome/browser/autofill/field_types.h),
  used in Android Webviews (and possibly in Custom Tabs).

## Version 2.3.0
_2021-06-02_

- Extract all password fields in LoginForm instead of only one

## Version 2.1.0

_2020-07-08_

- Add support of the inline autofill to have suggestions displayed in supporting keyboard apps.
It is Only compatible for Android 11 and above.

## Version 2.0.0

_2019-12-13_

- Changes in summary to identify the sources of auto-fill events
    - Refactor `AutoFillHintSummary` to be immutable
    - Move `formType` to be part of `AutoFillHintSummary`
    - On `AutoFillHintSummary` add `formSource` that replaces `packageName` and `webDomain`
    - `packageName` and `webDomain` still exists but as views over `formSource`

## Version 1.1.1

_2019-12-06_

- Improve OTP detection, to not throw it if we are within a CreditCard form.

## Version 1.1.0

_2019-11-25_

- Added into the `AutofillSummary` the fact that we have a "manual" request for autofill.

## Version 1.0.0

_2019-11-08_

- Extract the code from the main repository to this module, without any modification
