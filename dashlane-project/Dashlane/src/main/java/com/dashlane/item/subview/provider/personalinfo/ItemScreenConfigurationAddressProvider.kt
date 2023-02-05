package com.dashlane.item.subview.provider.personalinfo

import android.content.Context
import com.dashlane.R
import com.dashlane.core.domain.State
import com.dashlane.device.DeviceInfoRepository
import com.dashlane.item.ItemEditViewContract
import com.dashlane.item.ScreenConfiguration
import com.dashlane.item.header.ItemHeader
import com.dashlane.item.logger.AddressLogger
import com.dashlane.item.subview.ItemScreenConfigurationProvider
import com.dashlane.item.subview.ItemSubView
import com.dashlane.item.subview.ItemSubViewWithActionWrapper
import com.dashlane.item.subview.ValueChangeManager
import com.dashlane.item.subview.action.CopyAction
import com.dashlane.item.subview.edit.ItemEditValueListSubView
import com.dashlane.item.subview.edit.ItemEditValueTextSubView
import com.dashlane.item.subview.provider.DateTimeFieldFactory
import com.dashlane.item.subview.provider.SubViewFactory
import com.dashlane.item.subview.provider.getCountryIsoCode
import com.dashlane.item.subview.readonly.ItemReadValueListSubView
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.isSemanticallyNull
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.copySyncObject
import com.dashlane.vault.model.forLabelOrDefault
import com.dashlane.vault.model.getLabel
import com.dashlane.vault.model.isInGreatBritain
import com.dashlane.vault.model.labels
import com.dashlane.vault.summary.toSummary
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.utils.Country



class ItemScreenConfigurationAddressProvider(
    private val teamspaceAccessor: TeamspaceAccessor,
    private val mainDataAccessor: MainDataAccessor,
    deviceInfoRepository: DeviceInfoRepository,
    sessionManager: SessionManager,
    bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val dateTimeFieldFactory: DateTimeFieldFactory
) : ItemScreenConfigurationProvider(
    teamspaceAccessor, mainDataAccessor.getDataCounter(),
    sessionManager, bySessionUsageLogRepository
) {

    override val logger = AddressLogger(
        teamspaceAccessor, deviceInfoRepository, mainDataAccessor.getDataCounter(),
        sessionManager, bySessionUsageLogRepository
    )

    @Suppress("UNCHECKED_CAST")
    override fun createScreenConfiguration(
        context: Context,
        item: VaultItem<*>,
        subViewFactory: SubViewFactory,
        editMode: Boolean,
        canDelete: Boolean,
        listener: ItemEditViewContract.View.UiUpdateListener
    ): ScreenConfiguration {
        item as VaultItem<SyncObject.Address>
        return ScreenConfiguration(
            createSubViews(context, item, subViewFactory, editMode, canDelete, listener),
            createHeader(context, item)
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun hasEnoughDataToSave(itemToSave: VaultItem<*>): Boolean {
        itemToSave as VaultItem<SyncObject.Address>
        return itemToSave.syncObject.addressFull?.trim().isNotSemanticallyNull() ||
                itemToSave.syncObject.city?.trim().isNotSemanticallyNull() ||
                itemToSave.syncObject.zipCode?.trim().isNotSemanticallyNull() ||
                itemToSave.syncObject.receiver?.trim().isNotSemanticallyNull() ||
                itemToSave.syncObject.building?.trim().isNotSemanticallyNull() ||
                itemToSave.syncObject.door?.trim().isNotSemanticallyNull() ||
                itemToSave.syncObject.digitCode?.trim().isNotSemanticallyNull() ||
                itemToSave.syncObject.floor?.trim().isNotSemanticallyNull() ||
                itemToSave.syncObject.streetNumber?.trim().isNotSemanticallyNull()
    }

    private fun createHeader(
        context: Context,
        item: VaultItem<*>
    ): ItemHeader {
        val iconDrawable = createDefaultHeaderIcon(context, item.syncObject)
        val addressTitle = context.getString(R.string.address)
        return ItemHeader(createMenus(), addressTitle, iconDrawable)
    }

    private fun createSubViews(
        context: Context,
        item: VaultItem<SyncObject.Address>,
        subViewFactory: SubViewFactory,
        editMode: Boolean,
        canDelete: Boolean,
        listener: ItemEditViewContract.View.UiUpdateListener
    ): List<ItemSubView<*>> {
        val stateView = createStateSpinnerField(context, item, editMode)
        val houseNumberView = createHouseNumberField(subViewFactory, context, item, editMode)
        return listOfNotNull(
            
            createNameField(subViewFactory, context, item),
            
            createAddressField(subViewFactory, context, item, editMode),
            
            createCityField(subViewFactory, context, item, editMode),
            
            createZipCodeField(subViewFactory, context, item, editMode),
            
            createCountryField(context, item, editMode, stateView, houseNumberView, listener),
            
            stateView,
            
            houseNumberView,
            
            createRecipientField(subViewFactory, context, item),
            
            createBuildingField(subViewFactory, context, item),
            
            createFloorField(subViewFactory, context, item),
            
            createApartmentNumberField(subViewFactory, context, item),
            
            createDoorCodeField(subViewFactory, context, item),
            
            createPhoneNumberField(context, item, editMode),
            
            createTeamspaceField(subViewFactory, item),
            
            subViewFactory.createSubviewAttachmentDetails(context, item),
            
            dateTimeFieldFactory.createCreationDateField(editMode = editMode, context = context, item = item),
            
            dateTimeFieldFactory.createLatestUpdateDateField(editMode = editMode, context = context, item = item),
            
            subViewFactory.createSubviewDelete(context, listener, canDelete)
        )
    }

    private fun createTeamspaceField(
        subViewFactory: SubViewFactory,
        item: VaultItem<SyncObject.Address>
    ): ItemSubView<*>? {
        return if (teamspaceAccessor.canChangeTeamspace()) {
            subViewFactory.createSpaceSelector(
                item.syncObject.spaceId, teamspaceAccessor, null,
                VaultItem<*>::copyForUpdatedTeamspace
            )
        } else {
            null
        }
    }

    private fun createPhoneNumberField(
        context: Context,
        item: VaultItem<SyncObject.Address>,
        editMode: Boolean
    ): ItemSubView<*>? {
        val noneLabel = context.getString(R.string.none)
        val phoneList = mainDataAccessor.getPhoneList(context, noneLabel)

        return if (phoneList.isNotEmpty()) {
            val selectedNumber = phoneList.firstOrNull { it.second == item.syncObject.linkedPhone }?.first
                ?: noneLabel
            val phoneHeader = context.getString(R.string.phone)
            when {
                editMode -> ItemEditValueListSubView(
                    phoneHeader,
                    selectedNumber,
                    phoneList.map { it.first }) { it, value -> it.copyForUpdatedPhone(phoneList, value) }
                else -> ItemReadValueListSubView(phoneHeader, selectedNumber, phoneList.map { it.first })
            }
        } else {
            null
        }
    }

    private fun createDoorCodeField(
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.Address>
    ): ItemSubView<*>? {
        return subViewFactory.createSubViewString(
            context.getString(R.string.address_hint_doorcode),
            item.syncObject.digitCode,
            false,
            VaultItem<*>::copyForUpdatedDigitCode
        )
    }

    private fun createApartmentNumberField(
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.Address>
    ): ItemSubView<*>? {
        return subViewFactory.createSubViewString(
            context.getString(R.string.address_hint_apartment),
            item.syncObject.door,
            false,
            VaultItem<*>::copyForUpdatedDoor
        )
    }

    private fun createFloorField(
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.Address>
    ): ItemSubView<*>? {
        return subViewFactory.createSubViewString(
            context.getString(R.string.address_hint_floor),
            item.syncObject.floor,
            false,
            VaultItem<*>::copyForUpdatedFloor
        )
    }

    private fun createBuildingField(
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.Address>
    ): ItemSubView<*>? {
        return subViewFactory.createSubViewString(
            context.getString(R.string.address_hint_building),
            item.syncObject.building,
            false,
            VaultItem<*>::copyForUpdatedBilling
        )
    }

    private fun createHouseNumberField(
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.Address>,
        editMode: Boolean
    ): ItemSubView<String>? {
        return subViewFactory.createSubViewString(
            context.getString(R.string.address_hint_house_number),
            item.syncObject.streetNumber,
            false,
            VaultItem<*>::copyForUpdatedStreetNumber
        ).apply {
            if (editMode) {
                val view = this as ItemEditValueTextSubView
                val isCountryInGb = item.syncObject.localeFormat.isInGreatBritain
                
                view.invisible = (value as? String).isSemanticallyNull() && !isCountryInGb
            }
        }
    }

    private fun createRecipientField(
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.Address>
    ): ItemSubView<*>? {
        return subViewFactory.createSubViewString(
            context.getString(R.string.address_hint_receipent),
            item.syncObject.receiver,
            false,
            VaultItem<*>::copyForUpdatedReceiver
        )
    }

    private fun createCountryField(
        context: Context,
        item: VaultItem<SyncObject.Address>,
        editMode: Boolean,
        stateView: ItemSubView<*>?,
        houseNumber: ItemSubView<String>?,
        listener: ItemEditViewContract.View.UiUpdateListener
    ): ItemSubView<*> {
        val countryHeader = context.getString(R.string.country)
        val allCountryList = Country.labels(context)
        val selectedCountry = item.syncObject.localeFormat?.getLabel(context) ?: ""
        return when {
            editMode -> ItemEditValueListSubView(
                countryHeader,
                selectedCountry,
                allCountryList
            ) { it, value -> it.copyForUpdatedCountry(context, value) }
                .apply {
                    addValueChangedListener(object : ValueChangeManager.Listener<String> {
                        override fun onValueChanged(origin: Any, newValue: String) {
                            val newCountry = Country.forLabelOrDefault(context, newValue)
                            val isCountryInGb = newCountry.isInGreatBritain
                            stateView?.apply {
                                val view = this as ItemEditValueListSubView
                                view.title = if (isCountryInGb) {
                                    context.getString(R.string.county)
                                } else {
                                    context.getString(R.string.state)
                                }
                                val stateList =
                                    State.getStatesForCountry(newCountry).map { it.name }
                                view.invisible = stateList.isEmpty()
                                view.values = stateList
                                view.value = context.getString(R.string.dropdown_select_incentive)
                                listener.notifySubViewChanged(this)
                            }

                            houseNumber?.apply {
                                val view = this as ItemEditValueTextSubView
                                
                                view.invisible = value.isSemanticallyNull() && !isCountryInGb
                                listener.notifySubViewChanged(this)
                            }
                        }
                    })
                }
            else -> ItemReadValueListSubView(countryHeader, selectedCountry, allCountryList)
        }
    }

    private fun createStateSpinnerField(
        context: Context,
        item: VaultItem<SyncObject.Address>,
        editMode: Boolean
    ): ItemSubView<*>? {
        val currentCountry = item.syncObject.localeFormat
        val stateHeader = if (currentCountry.isInGreatBritain) {
            context.getString(R.string.county)
        } else {
            context.getString(R.string.state)
        }
        val stateList = item.syncObject.getStatesForCurrentCountry()
        val selectedState = stateList.find { it.stateDescriptor == item.syncObject.state }?.name
            ?: context.getString(R.string.dropdown_select_incentive)
        return when {
            editMode -> ItemEditValueListSubView(
                stateHeader,
                selectedState,
                stateList.map { it.name },
                VaultItem<*>::copyForUpdatedState
            ).apply {
                invisible = stateList.isEmpty()
            }
            stateList.isEmpty() -> null
            else -> ItemReadValueListSubView(
                stateHeader,
                selectedState,
                stateList.map { it.name })
        }
    }

    private fun createZipCodeField(
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.Address>,
        editMode: Boolean
    ): ItemSubView<*>? {
        val createSubViewString = subViewFactory.createSubViewString(
            context.getString(R.string.address_hint_zipcode),
            item.syncObject.zipCode,
            false,
            VaultItem<*>::copyForUpdatedZipCode
        )
        return if (createSubViewString == null || editMode) {
            createSubViewString
        } else {
            ItemSubViewWithActionWrapper(
                createSubViewString,
                CopyAction(item.toSummary(), CopyField.ZipCode)
            )
        }
    }

    private fun createCityField(
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.Address>,
        editMode: Boolean
    ): ItemSubView<*>? {
        val createSubViewString = subViewFactory.createSubViewString(
            context.getString(R.string.address_hint_city),
            item.syncObject.city,
            false,
            VaultItem<*>::copyForUpdatedCity
        )
        return if (createSubViewString == null || editMode) {
            createSubViewString
        } else {
            ItemSubViewWithActionWrapper(
                createSubViewString,
                CopyAction(item.toSummary(), CopyField.City)
            )
        }
    }

    private fun createAddressField(
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.Address>,
        editMode: Boolean
    ): ItemSubView<*>? {
        val createSubViewString = subViewFactory.createSubViewString(
            context.getString(R.string.address_hint_address),
            item.syncObject.addressFull,
            false,
            VaultItem<*>::copyForUpdatedFull
        )
        return if (createSubViewString == null || editMode) {
            createSubViewString
        } else {
            ItemSubViewWithActionWrapper(
                createSubViewString,
                CopyAction(item.toSummary(), CopyField.Address)
            )
        }
    }

    private fun createNameField(
        subViewFactory: SubViewFactory,
        context: Context,
        item: VaultItem<SyncObject.Address>
    ): ItemSubView<*>? {
        return subViewFactory.createSubViewString(
            context.getString(R.string.address_hint_name),
            item.syncObject.addressName,
            false,
            VaultItem<*>::copyForUpdatedName
        )
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedFull(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.Address>
    val address = this.syncObject
    return if (value == address.addressFull.orEmpty()) {
        this
    } else {
        this.copySyncObject { addressFull = value }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedName(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.Address>
    val address = this.syncObject
    return if (value == address.addressName.orEmpty()) {
        this
    } else {
        this.copySyncObject { addressName = value }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedTeamspace(value: Teamspace): VaultItem<*> {
    this as VaultItem<SyncObject.Address>
    val address = this.syncObject
    return if (value.teamId == address.spaceId) {
        this
    } else {
        this.copyWithAttrs { teamSpaceId = value.teamId }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedPhone(
    phoneList: List<Pair<String, String?>>,
    value: String
): VaultItem<*> {
    this as VaultItem<SyncObject.Address>
    val address = this.syncObject
    return if (value == address.linkedPhone) {
        this
    } else {
        val phoneUid = phoneList.firstOrNull { it.first == value }?.second
        this.copySyncObject { linkedPhone = phoneUid }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedDigitCode(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.Address>
    val address = this.syncObject
    return if (value == address.digitCode.orEmpty()) {
        this
    } else {
        this.copySyncObject { digitCode = value }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedDoor(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.Address>
    val address = this.syncObject
    return if (value == address.door.orEmpty()) {
        this
    } else {
        this.copySyncObject { door = value }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedFloor(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.Address>
    val address = this.syncObject
    return if (value == address.floor.orEmpty()) {
        this
    } else {
        this.copySyncObject { floor = value }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedBilling(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.Address>
    val address = this.syncObject
    return if (value == address.building.orEmpty()) {
        this
    } else {
        this.copySyncObject { building = value }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedStreetNumber(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.Address>
    val address = this.syncObject
    return if (value == address.streetNumber.orEmpty()) {
        this
    } else {
        this.copySyncObject { streetNumber = value }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedReceiver(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.Address>
    val address = this.syncObject
    return if (value == address.receiver.orEmpty()) {
        this
    } else {
        this.copySyncObject { receiver = value }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedCountry(context: Context, value: String): VaultItem<*> {
    this as VaultItem<SyncObject.Address>
    val address = this.syncObject
    val countryIsoCode = getCountryIsoCode(context, value)
    return if (countryIsoCode == address.localeFormat && countryIsoCode == address.country) {
        this
    } else {
        copy(
            syncObject = syncObject.copy {
                localeFormat = countryIsoCode
                country = countryIsoCode
            }
        )
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedState(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.Address>
    val address = this.syncObject
    return if (value == address.state) {
        this
    } else {
        val newStateDescriptor =
            address.getStatesForCurrentCountry().find { it.name == value }
                ?.stateDescriptor
        this.copySyncObject { state = newStateDescriptor }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedZipCode(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.Address>
    val address = this.syncObject
    return if (value == address.zipCode.orEmpty()) {
        this
    } else {
        this.copySyncObject { zipCode = value }
    }
}

@Suppress("UNCHECKED_CAST")
private fun VaultItem<*>.copyForUpdatedCity(value: String): VaultItem<*> {
    this as VaultItem<SyncObject.Address>
    val address = this.syncObject
    return if (value == address.city) {
        this
    } else {
        this.copySyncObject { city = value }
    }
}

private fun SyncObject.Address.getStatesForCurrentCountry(): List<State> {
    val country = localeFormat ?: Country.UnitedStates
    return State.getStatesForCountry(country)
}