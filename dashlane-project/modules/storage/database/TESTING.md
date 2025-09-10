# Testing

## Testing for Migration

### Migration Test strategy

The objective is to assert when introduce breaking changes in SummaryObject, tests will fail.

When developers introduce breaking changes in SummaryObject, we have 3 objectives:

 - Update all test data to assert the library works in latest version of SummaryObject.
 - Ensure opening old version databases should work correctly with migration. Basically all snapshots.
 - Migration is tested on its own.

Steps to achieve the objectives:

1. Run GenerateTestData.kt to update all test data. 
   This assert the library works in latest version of SummaryObject, but it doesn't assert open old version database.
   It will also generate a new snapshot database to capture the snapshot of the current version of database, which will be tested whenever database changes.

2. DatabaseSnapshotTest -> openSnapshotDatabase() will try to open all historical snapshots.

3. DatabaseTest contains migration tests.

4. Increase DatabaseIndex.SUMMARY_VERSION
   This will ensure a migration is triggered when opening old version database.

## Testing for implementation

Tests contains unit test for JVM

Run `./gradlew test` to run all tests.