# nextcloud-commons

[![Latest Release](https://img.shields.io/github/v/tag/stefan-niedermann/nextcloud-commons?label=latest+release&sort=semver)](https://github.com/stefan-niedermann/nextcloud-commons/releases)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/9f784826834042e8b512d531cab84711)](https://www.codacy.com/manual/info_147/nextcloud-commons?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=stefan-niedermann/nextcloud-commons&amp;utm_campaign=Badge_Grade)
[![GitHub issues](https://img.shields.io/github/issues/stefan-niedermann/nextcloud-commons.svg)](https://github.com/stefan-niedermann/nextcloud-commons/issues)
[![GitHub stars](https://img.shields.io/github/stars/stefan-niedermann/nextcloud-commons.svg)](https://github.com/stefan-niedermann/nextcloud-commons/stargazers)
[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

## How to use

Add this dependency to your `build.gradle`-file to include *all* modules at once:

```groovy
implementation 'com.github.stefan-niedermann:nextcloud-commons:0.0.4'
```

## Modules

### exception

```groovy
implementation 'com.github.stefan-niedermann.nextcloud-commons:exception:0.0.4'
```

This is a global `UncaughtExceptionHandler`. You can call it like this in your `onCreate`-callback of an activity:

```java
Thread.currentThread().setUncaughtExceptionHandler(new ExceptionHandler(this, YourExceptionActivity.class));
```

It will create a better stacktrace with rich informations like your app version, the files-app version and device & OS information.

### sso-glide

```groovy
implementation 'com.github.stefan-niedermann.nextcloud-commons:sso-glide:0.0.4'
```

This is a Glide-integration module. If you are using [Single Sign On](https://github.com/nextcloud/Android-SingleSignOn) you may want to also fetch avatars or other images via Glide but with the SSO network stack to avoid problems with self-signed certificates, 2fa and so on.

To make it work, you need also this dependencies in your `build.gradle`-file:

```groovy
implementation 'com.github.bumptech.glide:glide:4.10.0'
annotationProcessor 'com.github.bumptech.glide:compiler:4.10.0'
```

Then create a custom AppGlideModule at the place you want, like this:

```java
@GlideModule
public class CustomAppGlideModule extends AppGlideModule {
  @Override
  public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
    super.registerComponents(context, glide, registry);
  }
}
```

## :notebook: License
This project is licensed under the [GNU GENERAL PUBLIC LICENSE](/LICENSE).
