# nextcloud-commons

[![Latest Release](https://img.shields.io/github/v/tag/stefan-niedermann/nextcloud-commons?label=latest+release&sort=semver)](https://github.com/stefan-niedermann/nextcloud-commons/releases)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/9f784826834042e8b512d531cab84711)](https://www.codacy.com/manual/info_147/nextcloud-commons?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=stefan-niedermann/nextcloud-commons&amp;utm_campaign=Badge_Grade)
[![GitHub issues](https://img.shields.io/github/issues/stefan-niedermann/nextcloud-commons.svg)](https://github.com/stefan-niedermann/nextcloud-commons/issues)
[![GitHub stars](https://img.shields.io/github/stars/stefan-niedermann/nextcloud-commons.svg)](https://github.com/stefan-niedermann/nextcloud-commons/stargazers)
[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

- [What is this](#what-is-this)
- [How to use](#how-to-use)
- [Modules](#modules)
  - [exception](#exception)
  - [sso-glide](#glide-sso)
- [License](#notebook-license)

## What is this

Many Android clients for Nextcloud apps need similar mechanisms. To reduce maintenance efforts and provide a similar look & feel, this library aims to provide tooling and support which can be useful for various Android clients.

## How to use

Add this dependency to your `build.gradle`-file to include *all* modules at once:

```groovy
implementation 'com.github.stefan-niedermann:nextcloud-commons:1.2.5'
```

## Modules

### exception

```groovy
implementation 'com.github.stefan-niedermann.nextcloud-commons:exception:1.2.5'
```

This is a util class which provides methods for generating a rich stacktrace from a throwable containing additional information like the used files app and OS versions.

#### Usage

```java
try {
  // ...
} catch (Exception exception) {
  String debug = ExceptionUtil.INSTANCE.getDebugInfos(context, exception);
}
```

#### Example

```
App Version: 2.17.1
App Version Code: 2017001
Server App Version: 3.2.0
App Flavor: dev

Files App Version Code: 30120090

---

OS Version: 4.14.112+(5775370)
OS API Level: 29
Device: generic_x86_64
Manufacturer: unknown
Model (and Product): Android SDK built for x86_64 (sdk_phone_x86_64)

---

java.lang.RuntimeException: Unable to start activity ComponentInfo{it.niedermann.owncloud.notes.dev/it.niedermann.owncloud.notes.main.MainActivity}: java.lang.NumberFormatException: For input string: "ASDF"
	at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:3270)
	at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:3409)
	at android.app.servertransaction.LaunchActivityItem.execute(LaunchActivityItem.java:83)
	at android.app.servertransaction.TransactionExecutor.executeCallbacks(TransactionExecutor.java:135)
	at android.app.servertransaction.TransactionExecutor.execute(TransactionExecutor.java:95)
	at android.app.ActivityThread$H.handleMessage(ActivityThread.java:2016)
	at android.os.Handler.dispatchMessage(Handler.java:107)
	at android.os.Looper.loop(Looper.java:214)
	at android.app.ActivityThread.main(ActivityThread.java:7356)
	at java.lang.reflect.Method.invoke(Native Method)
	at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:492)
	at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:930)
Caused by: java.lang.NumberFormatException: For input string: "ASDF"
	at java.lang.Integer.parseInt(Integer.java:615)
	at java.lang.Integer.parseInt(Integer.java:650)
	at it.niedermann.owncloud.notes.main.MainActivity.onCreate(MainActivity.java:180)
	at android.app.Activity.performCreate(Activity.java:7802)
	at android.app.Activity.performCreate(Activity.java:7791)
	at android.app.Instrumentation.callActivityOnCreate(Instrumentation.java:1299)
	at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:3245)
	... 11 more

```

### sso-glide

```groovy
implementation 'com.github.stefan-niedermann.nextcloud-commons:sso-glide:1.2.5'
```

This is a Glide-integration module. If you are using [Single Sign On](https://github.com/nextcloud/Android-SingleSignOn) you may want to also fetch avatars or other images via Glide but with the SSO network stack to avoid problems with self-signed certificates, 2fa and so on.

To make it work, you need also this dependencies in your `build.gradle`-file:

```groovy
implementation 'com.github.bumptech.glide:glide:4.11.0'
annotationProcessor 'com.github.bumptech.glide:compiler:4.11.0'
```

#### Usage

Then create a custom `AppGlideModule` at the place you want, like this:

```java
@GlideModule
public class CustomAppGlideModule extends AppGlideModule {
  @Override
  public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
    super.registerComponents(context, glide, registry);
  }
}
```

Glide will automatically recognize the custom `AppGlideModule` and process this library module.

```java
Glide.with(context)
     .load("https://nextcloud.example.com/index.php/avatar/username/32")
     .into(myImageView);
```

will make a request from the user which is stored as the current SingleSignOn account (see [here](https://github.com/nextcloud/Android-SingleSignOn#4-how-to-get-account-information)).

If you need to perform the request from another account (for example to display avatars in an account switcher), you can use a `SingleSignOnUrl` instance as `Url`:

```java
Glide.with(context)
     .load(new SingleSignOnUrl(ssoAccount, "https://nextcloud.example.com/index.php/avatar/username/32"))
     .into(myImageView);
```

## :notebook: License
This project is licensed under the [GNU GENERAL PUBLIC LICENSE](/LICENSE).
