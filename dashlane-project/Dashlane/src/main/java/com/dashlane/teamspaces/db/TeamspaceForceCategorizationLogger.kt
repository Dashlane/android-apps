package com.dashlane.teamspaces.db

import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.useractivity.log.usage.UsageLogCode11



class TeamspaceForceCategorizationLogger(private val usageLogRepository: UsageLogRepository?) {

    

    fun newLog(): UsageLogCode11Wrapper = UsageLogCode11Wrapper(usageLogRepository)

    class UsageLogCode11Wrapper(private val usageLogRepository: UsageLogRepository?) {

        private var log = UsageLogCode11()

        

        fun setType(value: UsageLogCode11.Type?): UsageLogCode11Wrapper {
            log = log.copy(type = value)
            return this
        }

        

        fun setAction(value: UsageLogCode11.Action?): UsageLogCode11Wrapper {
            log = log.copy(action = value)
            return this
        }

        

        fun setWebsite(value: String?): UsageLogCode11Wrapper {
            log = log.copy(website = value)
            return this
        }

        

        fun setCounter(value: Int?): UsageLogCode11Wrapper {
            log = log.copy(counter = value)
            return this
        }

        

        fun setFrom(value: String?): UsageLogCode11Wrapper {
            log = log.copy(fromStr = value)
            return this
        }

        

        @Deprecated("")
        fun setCompletion(value: Int): UsageLogCode11Wrapper {
            log = log.copy(completion = value)
            return this
        }

        

        fun setColor(value: String?): UsageLogCode11Wrapper {
            log = log.copy(color = value)
            return this
        }

        

        fun setItemId(value: String?): UsageLogCode11Wrapper {
            log = log.copy(itemId = value)
            return this
        }

        

        fun setSpaceId(value: String?): UsageLogCode11Wrapper {
            log = log.copy(spaceId = value)
            return this
        }

        fun send() {
            usageLogRepository?.enqueue(log)
        }
    }
}