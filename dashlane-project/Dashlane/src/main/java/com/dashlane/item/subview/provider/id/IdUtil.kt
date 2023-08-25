package com.dashlane.item.subview.provider.id

import android.content.Context
import com.dashlane.R
import com.dashlane.item.ItemEditViewContract
import com.dashlane.item.subview.ItemSubView
import com.dashlane.item.subview.ItemSubViewImpl
import com.dashlane.item.subview.ItemSubViewWithActionWrapper
import com.dashlane.item.subview.ValueChangeManager
import com.dashlane.item.subview.action.CopyAction
import com.dashlane.item.subview.edit.ItemEditValueDateSubView
import com.dashlane.item.subview.edit.ItemEditValueListSubView
import com.dashlane.item.subview.edit.ItemEditValueTextSubView
import com.dashlane.item.subview.provider.SubViewFactory
import com.dashlane.item.subview.readonly.ItemReadValueDateSubView
import com.dashlane.item.subview.readonly.ItemReadValueListSubView
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.filter.genericFilter
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.ui.adapters.text.factory.toIdentityFormat
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.util.isSemanticallyNull
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.getLabelId
import com.dashlane.vault.model.hasBeenSaved
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import java.text.Collator
import java.time.LocalDate

fun <T : SyncObject> createIdentitySubviews(
    context: Context,
    subViewFactory: SubViewFactory,
    mainDataAccessor: MainDataAccessor,
    editMode: Boolean,
    listener: ItemEditViewContract.View.UiUpdateListener,
    item: VaultItem<T>,
    identityAdapter: IdentityAdapter<T>,
    fullNameOnly: Boolean = false
): List<ItemSubView<*>> = with(identityAdapter) {
    val list = mutableListOf<ItemSubView<*>?>()
    val isNew = !item.hasBeenSaved

    val selectedLinkedIdentity = if (isNew) {
        mainDataAccessor.getGenericDataQuery().queryFirst(
            genericFilter {
                specificDataType(SyncObjectType.IDENTITY)
            }
        )?.id
    } else {
        linkedIdentity(item)?.takeUnless { it.isSemanticallyNull() }
    }
    val isOther = selectedLinkedIdentity == null

    
    val fullNameView = createFieldFullName(context, subViewFactory, item, isOther)

    
    val genderView =
        if (fullNameOnly) null else createFieldGender(context, item, editMode, isOther, listener)

    
    val birthDateView =
        if (fullNameOnly) null else createFieldBirthDate(context, item, editMode, isOther, listener)

    
    createFieldLinkedIdentity(
        context,
        mainDataAccessor,
        selectedLinkedIdentity,
        editMode,
        fullNameView,
        listener,
        genderView,
        birthDateView
    )?.also {
        list.add(it)
    }

    if (isOther || editMode) {
        list.add(fullNameView)
        list.add(genderView)
        list.add(birthDateView)
    }

    return list.filterNotNull()
}

@Suppress("UNCHECKED_CAST")
private fun <T : SyncObject> IdentityAdapter<T>.createFieldFullName(
    context: Context,
    subViewFactory: SubViewFactory,
    item: VaultItem<T>,
    isOther: Boolean
): ItemSubView<String>? {
    return subViewFactory.createSubViewString(
        context.getString(R.string.abstact_ids_hint_full_name),
        fullName(item),
        false,
        { it, value ->
            val vaultItem = it as VaultItem<T>
            vaultItem.copyForUpdatedFullName(this, value)
        }
    )
        .apply { (this as? ItemEditValueTextSubView)?.invisible = !isOther }
}

private fun <T : SyncObject> IdentityAdapter<T>.createFieldGender(
    context: Context,
    item: VaultItem<T>,
    editMode: Boolean,
    isOther: Boolean,
    listener: ItemEditViewContract.View.UiUpdateListener
): ItemSubViewImpl<String>? {
    val genderHeader = context.getString(R.string.gender)
    val possibleGenders = SyncObject.Gender.values().toSet()
    var hasGender = gender(item) in possibleGenders
    val selectLabel = context.getString(R.string.dropdown_select_incentive)
    val selectedGender = if (hasGender) context.getString(gender(item).getLabelId()) else selectLabel
    val genders = possibleGenders.map { context.getString(it.getLabelId()) }
        .sortedWith(Collator.getInstance())

    return when {
        editMode -> ItemEditValueListSubView(
            genderHeader,
            selectedGender,
            if (!hasGender) listOf(selectLabel) + genders else genders
        ) { it, value -> it.copyForUpdatedGender(this, context, possibleGenders, value) }
            .apply {
                invisible = !isOther

                addValueChangedListener(object : ValueChangeManager.Listener<String> {
                    override fun onValueChanged(origin: Any, newValue: String) {
                        
                        if (!hasGender && newValue in genders) {
                            val subView = origin as ItemEditValueListSubView
                            subView.values = genders
                            hasGender = true
                            listener.notifySubViewChanged(subView)
                        }
                    }
                })
            }
        
        !hasGender -> null
        else -> ItemReadValueListSubView(
            genderHeader,
            selectedGender,
            genders
        )
    }
}

private fun <T : SyncObject> IdentityAdapter<T>.createFieldBirthDate(
    context: Context,
    item: VaultItem<T>,
    editMode: Boolean,
    isOther: Boolean,
    listener: ItemEditViewContract.View.UiUpdateListener
): ItemSubViewImpl<LocalDate?>? {
    val birthDateHint = context.getString(R.string.date_of_birth)
    val birthDateLocalDate = birthDate(item)
    val birthDateFormatted = birthDateLocalDate?.toIdentityFormat()

    return when {
        editMode -> ItemEditValueDateSubView(
            birthDateHint,
            birthDateLocalDate,
            birthDateFormatted
        ) { it, value -> it.copyForUpdatedBirthDate(this, value) }
            .apply {
                invisible = !isOther
                addValueChangedListener(object : ValueChangeManager.Listener<LocalDate?> {
                    override fun onValueChanged(origin: Any, newValue: LocalDate?) {
                        val subView = origin as ItemEditValueDateSubView
                        subView.formattedDate = newValue?.toIdentityFormat()
                        subView.value = newValue
                        listener.notifySubViewChanged(subView)
                    }
                })
            }
        
        birthDateLocalDate == null -> null
        else -> ItemReadValueDateSubView(
            birthDateHint,
            birthDateLocalDate,
            birthDateFormatted
        )
    }
}

@Suppress("UNCHECKED_CAST")
private fun <T : SyncObject> IdentityAdapter<T>.createFieldLinkedIdentity(
    context: Context,
    mainDataAccessor: MainDataAccessor,
    selectedLinkedIdentity: String?,
    editMode: Boolean,
    fullNameView: ItemSubView<String>?,
    listener: ItemEditViewContract.View.UiUpdateListener,
    genderView: ItemSubViewImpl<String>?,
    birthDateView: ItemSubViewImpl<LocalDate?>?
): ItemSubViewImpl<String>? {
    val otherLabel = context.getString(R.string.other)
    val linkedIdentityHeader = context.getString(R.string.identity)
    val fullNameToIdentity: Map<String, VaultItem<SyncObject.Identity>> =
        mainDataAccessor.getVaultDataQuery().queryAll(
            vaultFilter {
                specificDataType(SyncObjectType.IDENTITY)
            }
        ).fold(mutableMapOf()) { acc, identity ->
            val id = identity as VaultItem<SyncObject.Identity>
            val fullName = id.syncObject.toSummary<SummaryObject.Identity>().fullName
            acc[fullName] = id
            acc
        }
    
    if (fullNameToIdentity.isEmpty()) return null

    val selectedFullName = fullNameToIdentity.entries.firstOrNull { (_, identity) ->
        selectedLinkedIdentity == identity.uid
    }?.key ?: otherLabel

    val fullNames = fullNameToIdentity.keys.sortedWith(Collator.getInstance()) + otherLabel

    return if (editMode) {
        ItemEditValueListSubView(
            linkedIdentityHeader,
            selectedFullName,
            fullNames
        ) { it, value -> it.copyForUpdatedLinkedIdentity(this, fullNameToIdentity, value) }
            .apply {
                addValueChangedListener(object : ValueChangeManager.Listener<String> {
                    override fun onValueChanged(origin: Any, newValue: String) {
                        val hasIdentity = newValue in fullNameToIdentity 

                        (fullNameView as? ItemEditValueTextSubView)?.run {
                            if (hasIdentity) {
                                value = ""
                            }
                            invisible = hasIdentity
                            listener.notifySubViewChanged(this)
                        }

                        (genderView as? ItemEditValueListSubView)?.run {
                            invisible = hasIdentity
                            listener.notifySubViewChanged(this)
                        }

                        (birthDateView as? ItemEditValueDateSubView)?.run {
                            if (hasIdentity) {
                                formattedDate = null
                                value = null
                            }

                            invisible = hasIdentity
                            listener.notifySubViewChanged(this)
                        }
                    }
                })
            }
    } else {
        ItemReadValueListSubView(
            linkedIdentityHeader,
            selectedFullName,
            fullNames
        )
    }
}

fun <T : SyncObject> createIdDateField(
    item: VaultItem<T>,
    editMode: Boolean,
    listener: ItemEditViewContract.View.UiUpdateListener,
    dateLocalDate: LocalDate?,
    dateHint: String,
    copyField: CopyField,
    valueUpdate: (VaultItem<*>, LocalDate?) -> VaultItem<*>?
): ItemSubView<*>? {
    val issueDateFormatted = dateLocalDate?.toIdentityFormat()
    return when {
        editMode -> ItemEditValueDateSubView(
            dateHint,
            dateLocalDate,
            issueDateFormatted,
            valueUpdate
        ).apply {
            addValueChangedListener(object : ValueChangeManager.Listener<LocalDate?> {
                override fun onValueChanged(origin: Any, newValue: LocalDate?) {
                    val subView = origin as ItemEditValueDateSubView
                    subView.formattedDate = newValue?.toIdentityFormat()
                    subView.value = newValue
                    listener.notifySubViewChanged(subView)
                }
            })
        }
        
        dateLocalDate == null -> null
        else -> ItemSubViewWithActionWrapper(
            ItemReadValueDateSubView(
                dateHint,
                dateLocalDate,
                issueDateFormatted
            ),
            CopyAction(item.toSummary(), copyField)
        )
    }
}

@Suppress("UNCHECKED_CAST")
private fun <T : SyncObject> VaultItem<*>.copyForUpdatedLinkedIdentity(
    adapter: IdentityAdapter<T>,
    fullNameToIdentity: Map<String, VaultItem<SyncObject.Identity>>,
    value: String
): VaultItem<T> {
    this as VaultItem<T>
    val newLinkedIdentity = fullNameToIdentity[value]?.uid
    return if (adapter.linkedIdentity(this) == newLinkedIdentity) {
        this
    } else {
        adapter.withLinkedIdentity(this, newLinkedIdentity)
    }
}

@Suppress("UNCHECKED_CAST")
private fun <T : SyncObject> VaultItem<*>.copyForUpdatedBirthDate(
    adapter: IdentityAdapter<T>,
    value: LocalDate?
): VaultItem<T> {
    this as VaultItem<T>
    return if (adapter.birthDate(this) == value) {
        this
    } else {
        adapter.withBirthDate(this, value)
    }
}

@Suppress("UNCHECKED_CAST")
private fun <T : SyncObject> VaultItem<*>.copyForUpdatedGender(
    adapter: IdentityAdapter<T>,
    context: Context,
    possibleGenders: Set<SyncObject.Gender>,
    value: String
): VaultItem<T> {
    this as VaultItem<T>
    val newGender = possibleGenders.firstOrNull { context.getString(it.getLabelId()) == value }
    return if (adapter.gender(this) == newGender) {
        this
    } else {
        adapter.withGender(this, newGender)
    }
}

private fun <T : SyncObject> VaultItem<T>.copyForUpdatedFullName(
    adapter: IdentityAdapter<T>,
    value: String
): VaultItem<T> {
    return if (adapter.fullName(this).orEmpty() == value) {
        this
    } else {
        adapter.withFullName(this, value)
    }
}