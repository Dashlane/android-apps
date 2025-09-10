# Android Database

The database used on Android to store the user vault data.

## API

Open an existing or new database

```
val context: Context = ... // Android Framework Context
val cryptography: Cryptography = ... // Cryptography object
val databaseProvider = DatabaseProvider(context.getFilesDir(), cryptography)
val database: Database = databaseProvider.open(userId, key)

```
Get an opened database
```
val database: Database? = databaseProvider.getDatabase(userId)
```
Update and delete items
```
val vaultObjectRepository = database.vaultObjectRepository
vaultObjectRepository.transaction {
    update(...)
    delete(...)
}
```
Read summary
```
val memorySummaryRepository = database.memorySummaryRepository
val databaseSummary = memorySummaryRepository.databaseSummary
val databaseSyncSummary = syncSummaryRepository.databaseSyncSummary
val databaseDataChangeHistorySummary = syncSummaryRepository.databaseDataChangeHistorySummary

```
Read VaultItem by id
```
val vaultObjectRepository = database.vaultObjectRepository
val vaultItem: VaultItem<*> = vaultObjectRepository[Id.of(...)]
```
## Development
### Update test data
If you edit test data in [TestData](database-jvm-common/src/test/java/com.dashlane.database/TestData.kt) and would like to populate to impacted test data in Json.

[Run the main function locally.](database/src/test/kotlin/com/dashlane/database/GenerateTestData.kt)

### Add a new field in VaultItem or SyncObject, but nothing new in SummaryObject
Just add the fields and `Update test data`

### Add a new field in the SummaryObject or SyncSummaryItem
If you add a new field in any [SummaryObject](database/src/main/kotlin/com/dashlane/vault/summary/SummaryObject.kt), you might also need to update the test data and make sure your new filed is included in the test. See `Update test data`

> **Mandantory: Increase DatabaseIndex.SUMMARY_VERSION which will perform migration of the summaries at open of database**

## Architecture

![Overview](/images/Android-Database-Project.png "Overview")

![Key class diagram](/images/Database-Architecture-class-diagram.png "Key class diagram")

## [Testing](TESTING.md)

To find information about tests. Where to find them and how to run them.
