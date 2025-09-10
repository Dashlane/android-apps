package com.dashlane.autofill.formdetector.field

object CreditCardNumberLabel : LabelAcceptor(
    keywords = listOf(
        
        "numéro de carte",
        "numéro de la carte",
        "nº de carte",
        "carte perso",
        
        "card number",
        "credit card",
        
        "número de tarjeta",
        "tarjeta de crédito",
        
        "kartennummer",
        "kreditkarte",
        
        "numero della carta",
        "carta di credito",
        
        "「カード番号」",
        "「クレジットカード」",
        
        "카드 번호",
        "신용카드",
        
        "kaartnummer",
        
        "número do cartão",
        "cartão de crédito",
        
        "kortnummer",
        "kreditkort",
        
        "卡号",
        "信用卡",
    ),
)

object CreditCardSecurityCodeLabel : LabelAcceptor(
    keywords = listOf(
        
        "3 chiffres",
        "cryptogramme visuel",
        "cryptogramme",
        
        "cvv",
        "cvc",
    ),
)

object CreditCardDateLabel : LabelAcceptor(
    keywords = listOf(
        
        "mm/aa",
        "date d'expiration",
        "date de validité",
        "expiration date",
        
        "mm/yy",
        "expiry date",
        "validity date",
        
        "fecha de caducidad",
        "fecha de vencimiento",
        "fecha de validez",
        
        "mm/jj",
        "ablaufdatum",
        "gültigkeitsdatum",
        
        "data di scadenza",
        "data di validità",
        
        "有効期限",
        "有効日",
        
        "월/연도",
        "만료 날짜",
        "유효 날짜",
        
        "vervaldatum",
        "geldigheidsdatum",
        
        "data de expiração",
        "data de validade",
        
        "mm/åå",
        "utgångsdatum",
        "giltighetsdatum",
        
        "月/年",
        "到期日期",
        "有效日期",
    ),
)

object CreditCardNameHolderLabel : LabelAcceptor(
    keywords = listOf(
        
        "nom du titulaire",
        "titulaire de la carte",
        "prénom et nom",
        "nom sur la carte",
        
        "cardholder name",
        "cardholder's name",
        "name on card",
        
        "nombre del titular",
        "nombre y apellido",
        "titular de la tarjeta",
        "nombre del titular de la tarjeta",
        "nombre en la tarjeta",
        
        "karteninhaber",
        "name auf Karte",
        
        "nome del titolare della carta",
        "nome sulla carta",
        
        "カード名義人",
        "カードに記載されている名前",
        
        "카드 소유자 이름",
        "카드에 표시된 이름",
        
        "naam van kaarthouder",
        "naam op kaart",
        
        "nome do titular do cartão",
        "nome impresso no cartão",
        
        "kortinnehavarens namn",
        "namn på kortet",
        
        "持卡人姓名",
        "卡上的姓名",
    ),
)