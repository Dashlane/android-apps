package com.dashlane.auditduplicates.grouping



data class GroupDetail(
    val groupNbCredentials: Int,
    val groupNbExactDuplicates: Int,
    val groupNbDifferentHosts: Int,
    val groupNbDifferentPasswords: Int
)