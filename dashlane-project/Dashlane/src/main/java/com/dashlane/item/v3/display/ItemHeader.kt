package com.dashlane.item.v3.display

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dashlane.design.component.Text
import com.dashlane.design.component.Thumbnail
import com.dashlane.design.component.ThumbnailSize
import com.dashlane.design.component.ThumbnailType
import com.dashlane.design.iconography.IconTokens
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.design.theme.color.DecorativeColor
import com.dashlane.design.theme.tooling.DashlanePreview
import com.dashlane.item.v3.data.CommonData
import com.dashlane.item.v3.data.CredentialFormData
import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.data.SecretFormData
import com.dashlane.item.v3.data.SecureNoteFormData
import com.dashlane.item.v3.itemtype.creditcard.CreditCardFormData
import com.dashlane.item.v3.itemtype.wifi.WiFiFormData
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.ui.thumbnail.ThumbnailDomainIcon
import com.dashlane.vault.model.R
import com.dashlane.vault.model.getColorId
import com.dashlane.xml.domain.SyncObject

@Composable
internal fun ItemHeader(data: Data<out FormData>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        ItemHeaderThumbnail(
            data = data.formData,
        )
        val context = LocalContext.current
        val defaultTitle = remember {
            when (data.formData) {
                is CredentialFormData -> context.getString(com.dashlane.R.string.creditcard_new_label)
                else -> ""
            }
        }
        Text(
            text = data.commonData.name ?: defaultTitle,
            style = DashlaneTheme.typography.titleBlockMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 12.dp, start = 16.dp, end = 16.dp),
        )
    }
}

@Suppress("DEPRECATION")
@Composable
private fun ItemHeaderThumbnail(
    modifier: Modifier = Modifier,
    data: FormData,
) {
    val type = when (data) {
        is CredentialFormData -> {
            ThumbnailDomainIcon(
                modifier = modifier,
                urlDomain = data.url,
                size = ThumbnailSize.XLarge,
            )
            return
        }
        is CreditCardFormData -> {
            ThumbnailType.VaultItem.LegacyOtherIcon(
                token = IconTokens.itemPaymentOutlined,
                color = colorResource(data.color),
            )
        }
        is SecureNoteFormData -> {
            ThumbnailType.VaultItem.LegacyOtherIcon(
                token = IconTokens.itemSecureNoteOutlined,
                color = colorResource(id = data.secureNoteType.getColorId()),
            )
        }
        is SecretFormData ->
            ThumbnailType.VaultItem.OtherIcon(
                token = IconTokens.itemSecretOutlined,
                color = DecorativeColor.GREY,
            )
        is WiFiFormData ->
            ThumbnailType.VaultItem.OtherIcon(
                token = IconTokens.itemWifiOutlined,
                color = DecorativeColor.GREY,
            )
        else -> return
    }

    Thumbnail(
        modifier = modifier,
        type = type,
        size = ThumbnailSize.XLarge,
    )
}


@Preview
@Composable
private fun ItemHeaderSecureNotePreview() {
    DashlanePreview {
        ItemHeader(
            Data(
                commonData = CommonData(
                    name = "Secure Note",
                ),
                formData = SecureNoteFormData(),
            ),
        )
    }
}

@Preview
@Composable
private fun ItemHeaderCreditCardPreview() {
    DashlanePreview {
        ItemHeader(
            Data(
                commonData = CommonData(name = "google.com"),
                formData = CreditCardFormData(
                    colorId = SyncObject.PaymentCreditCard.Color.BLUE_1.value,
                    color = R.color.credit_card_blue1,
                ),
            ),
        )
    }
}

@Preview
@Composable
private fun ItemHeaderSecretPreview() {
    DashlanePreview {
        ItemHeader(
            Data(
                commonData = CommonData(
                    name = "Secret",
                ),
                formData = SecretFormData(),
            ),
        )
    }
}