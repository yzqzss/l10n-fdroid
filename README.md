# l10n-fdroid

## Usage

```shell
export MONGODB_URI=mongodb://localhost:27017 # set MongoDB URI
java -cp l10n-fdroid.jar l10n.fdroid.worker.PickerWorker # to init all apps to MongoDB
java -cp l10n-fdroid.jar l10n.fdroid.worker.ValuesWorker # to download apks and parse all the strings.xml to MongoDB (It is parallelizable, simply run multiple programs in your terminal to speed up the process)
```

NOTE: Create a 'stop' file in the ValuesWorker's pwd to stop it safely.
