This file is used to track progress on autofill for credit card.

## 02/08/22:

> 38 dumps available - 17 success - 44%

### Changes made

- Refactor and scoring system

## 04/07/22:

> 38 dumps available - 14 success - 36%

### Changes made

- Create a `CreditCardFormExtractor` to guess the credit card information
- Look in the "hint" field with extractor's

## 05/01/22:

> 38 dumps available - 8 success - 21%

### Changes made

- Filter `autofillHints` off and in without case-sensitive
- Add field `CREDIT_CARD_NAME_FULL` for name autofill

## 03/01/22:

> 38 dumps available - 6 success - 15%

## Why test are failing ?

- dump_com.ubercab_url_noUrlDetected_v108532_android30 -> No information on the edit text view but
  we can check the parent view to have more information.
- dump_com.pumpkin.pumpkin_url_noUrlDetected_v5210109_android30 -> No information on the edit text
  view but we can check the parent view to have more information.
- dump_com.revolut.revolut_url_noUrlDetected_v803402734_android30 -> Almost good, missing the card
  security code.
- dump_com.backmarket_url_noUrlDetected_v292_android31 -> There is username and password in the
  extract, so we found a credential rather than a credit card.
- dump_com.ubercab.eats_url_noUrlDetected_v108446_android30 -> No information on the edit text view
  but we can check the parent view to have more information.
- dump_fr.tm.fnac_url_mobileapp.fnacspectacles -> Credit card fields detected but a lot of other
  fields are also recognized
- dump_com.alibaba.aliexpresshd_url_noUrlDetected_v399_android31 -> No edit text found for
  expiration date.
- dump_com.holosfind.showroom_url_payment.payline.com_v627979_android31 -> htmlInfo info for
  expiration date is wrong ("
  CREDIT_CARD_EXP_DATE_4_DIGIT_YEAR" but we should autofill MM/YY).
- dump_com.joom_url_noUrlDetected_v4222992_android31 -> We detect way more fields
- dump_com.android.chrome_url_noUrlDetected_v466410433_android31 -> Possible to guess with hint "
  Numéro de la carte *"
  , "Date de validité * MM/YY", "Cryptogramme visuel *", "Nom sur la carte *"
- dump_go.voyage_url_noUrlDetected_v438300_android31 -> Can't find the CVV.
- dump_com.venteprivee_url_pay.veepee.com_v2023302617_android31 -> We detect two times each fields
  because htmlInfo and autofillHints are filled. Don't pass test but autill is working on device.
- dump_com.brave.browser_url_noUrlDetected_v413310624_android30 -> Too much character in the hint
  field and we have a condition to abort the extractor if number of char is > 30
- dump_com.android.chrome_url_noUrlDetected_v466410433_android31(3) -> Too much character in the
  hint field and we have a condition to abort the extractor if number of char is > 30
- dump_com.android.chrome_url_noUrlDetected_v466410433_android31(1) -> Too much character in the
  hint field and we have a condition to abort the extractor if number of char is > 30
- dump_com.airbnb.android_url_noUrlDetected_v24004805_android30(1) -> Looks impossible
- dump_com.decathlon.app_url_eu-prod.oppwa.com_v5001203_android31 -> Found "cc-number" but not "
  creditCardNumber" (
  should be mapped). Other htmlInfo attributes looks not common. We can also guess with hint "Numéro
  de la carte"", "
  CVV", "MM / AA", "Titulaire de la carte"
- dump_com.comuto_url_noUrlDetected_v340000377_android30 -> Possible to guess with hint "Nom du
  titulaire de la carte"
  , "Numéro de carte", "MM/AA", "CVV"
- dump_com.ocito.laredoute_url_www.laredoute.fr_v1238_android31 -> They use
  CREDIT_CARD_EXP_DATE_2_DIGIT_YEAR for credit card expiration so we autofill "
  creditCardExpirationYear" rather than "creditCardExpirationDate"
- dump_com.android.chrome_url_noUrlDetected_v466410433_android31(2) -> Looks impossible

