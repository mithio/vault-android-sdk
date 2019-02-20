VAULT SDK for Android
========================

This open-source library allows you to integrate VAULT into your app.
Learn more about about the provided samples, documentation, integrating the SDK into your app, and more at [deck slide](https://drive.google.com/file/d/1wjHUySvL6YMUFf3HkHrWVASJEipdooOo/view?usp=sharing)

FEATURE
--------
* [login](https://documenter.getpostman.com/view/4856913/RztrHRU9#3563f4ea-88bc-403d-8071-d3d3767bd01d)
* [mining](https://documenter.getpostman.com/view/4856913/RztrHRU9#0cbb0a41-2cfc-4d3a-b541-4cfbbf807843)
* [donate](https://documenter.getpostman.com/view/4856913/RztrHRU9#608ccdd4-6a95-41f0-b247-ffae9a976feb)

INSTALLATION
------------
- Add the JitPack repository to your `build.gradle` repositories:

```gradle
repositories {
    // ...
    maven { url "https://jitpack.io" }
}
```

- Add the core dependency:

```
dependencies {
    compile 'com.github.mithio:vault-oauth-android:{lastest-version}'
}
```

- Add the Vault SDK redirect scheme to gradle config

```
defaultConfig {
    // ...
    manifestPlaceholders = [
        'appAuthRedirectScheme': 'vault-{client-id}'
    ]
}
```

USAGE
------------
configure the sdk with:

```
VaultSDK.configure(
                context = context,
                clientId = {client-id},
                clientSecret = {client-secret},
                miningKey = {mining-key}
        )
```

and you are ready to call:

```
VaultSDK.getAccessToken(activity)
```
to get oauth access token

GIVE FEEDBACK
-------------
Please report bugs or issues to [hackathon@mith.io](hackathon@mith.io)
