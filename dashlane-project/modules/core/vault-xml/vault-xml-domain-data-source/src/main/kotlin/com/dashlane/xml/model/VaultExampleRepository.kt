package com.dashlane.xml.model

import org.json.JSONObject
import org.w3c.dom.Document
import java.io.InputStream

class VaultExampleRepository {
    fun loadJsonExample(vaultModel: VaultModel): JSONObject =
        openJsonExampleStream(vaultModel).use(InputStream::toJson)

    fun openJsonExampleStream(vaultModel: VaultModel): InputStream =
        openResourceStream("examples/${vaultModel.name}.json")

    fun loadXmlExample(vaultModel: VaultModel): Document =
        openXmlExampleStream(vaultModel).toXml()

    fun openXmlExampleStream(vaultModel: VaultModel): InputStream =
        openResourceStream("examples/${vaultModel.name}.xml")
}