# Rick and Morty Hub — Lab 2 changes merged into Lab 3

This project is based on the supplied `3rick_and_morty_hub.zip`.
The registration/login behavior from `2rick_and_morty_hub.zip` was merged into it.
The Lab 3 Retrofit, pagination, local cache, offline behavior and backup/cache operations were retained.

## What changed

### Registration and login
- Added `AuthViewModel` shared by the `login` and `registration` destinations in `MainNavigation`.
- Registration now passes a complete `RegisteredUser` (first name, last name, login, email and password) to the ViewModel.
- After registration, navigation returns to the existing login screen and fills in the registered login.
- Login checks the entered credentials. An unknown account and incorrect credentials produce separate messages.
- Successful login opens the existing network-backed `ContentScreen` and removes the login destination from the back stack.
- Added a button to return from registration to login.
- Added the `lifecycle-viewmodel-compose` dependency.

Note: like the supplied Lab 2 implementation, the demo account is held in memory by `AuthViewModel`.
It is not a production authentication system and is reset when the app process is recreated.

### Character data layer split
The former `data/CharacterData.kt` contained the API contract, DTOs, cache, repository and mapping code.
It has been replaced by these focused files:

- `data/model/Character.kt` — domain model and cache snapshot.
- `data/remote/RickAndMortyApi.kt` — Retrofit API, response DTOs and API client.
- `data/local/CharacterCache.kt` — SharedPreferences JSON cache and stored entity.
- `data/CharacterRepository.kt` — coordinates remote loading, mapping and local cache operations.

The `Character` model is now in `com.example.rick_and_morty_hub.data.model`.
Imports were updated in `ContentActivity.kt` and `BackupManager.kt`.

## Applying as a patch

If applying these files to an existing Lab 3 project:
1. Copy the files from this archive over the matching paths.
2. Delete the old `app/src/main/java/com/example/rick_and_morty_hub/data/CharacterData.kt`.
3. Open Android Studio and run Gradle Sync.
4. Build and test registration -> login -> content.
5. Test loading more than one character page and verify cached characters are still available offline.

The archive is a complete project snapshot, so it can also be opened directly in Android Studio.
