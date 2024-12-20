package com.dashlane.item.v3.display

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.dashlane.item.v3.data.CreditCardFormData
import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.data.SecretFormData
import com.dashlane.item.v3.data.SecureNoteFormData
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.ui.thumbnail.ThumbnailDomainIcon
import com.dashlane.vault.model.getColorId
import com.dashlane.vault.model.getColorResource
import com.dashlane.xml.domain.SyncObject

@Composable
internal fun ItemHeader(data: Data<out FormData>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        when (data.formData) {
            is CredentialFormData -> {
                ThumbnailDomainIcon(
                    urlDomain = data.formData.url,
                    size = ThumbnailSize.XLarge,
                )
            }
            is CreditCardFormData -> {
                Thumbnail(
                    type = ThumbnailType.VaultItem.LegacyOtherIcon(
                        token = IconTokens.itemPaymentOutlined,
                        color = colorResource(id = data.formData.color.getColorResource()),
                    ),
                    size = ThumbnailSize.XLarge,
                )
            }
            is SecureNoteFormData -> {
                Thumbnail(
                    type = ThumbnailType.VaultItem.LegacyOtherIcon(
                        token = IconTokens.itemSecureNoteOutlined,
                        color = colorResource(id = data.formData.secureNoteType.getColorId()),
                    ),
                    size = ThumbnailSize.XLarge
                )
            }
            is SecretFormData ->
                Thumbnail(
                    type = ThumbnailType.VaultItem.OtherIcon(
                        token = IconTokens.itemSecretOutlined,
                        color = DecorativeColor.GREY,
                    ),
                    size = ThumbnailSize.XLarge
                )
        }
        Text(
            text = data.commonData.name,
            style = DashlaneTheme.typography.titleBlockMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}


@Preview
@Composable
private fun ItemHeaderSecureNotePreview() {
    DashlanePreview {
        ItemHeader(
            Data(
                commonData = CommonData(
                    name = "Secure Note"
                ),
                formData = SecureNoteFormData()
            )
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
                    color = SyncObject.PaymentCreditCard.Color.RED
                )
            )
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
                    name = "Secret"
                ),
                formData = SecretFormData()
            )
        )
    }
}