# nextcloud-commons

## How to use

Add this dependency to your `build.gradle`-file:

`implementation 'com.github.stefan-niedermann:nextcloud-commons:c491b84271'`

## Modules

### exception

This is a global `UncaughtExceptionHandler`. You can call it like this in your `onCreate`-callback of an activity:

`Thread.currentThread().setUncaughtExceptionHandler(new ExceptionHandler(this, YoutExceptionActivity.class));`

It will create a better stacktrace with rich informations like your app version, the files-app version and device & OS information.

### sso-glide

This is a Glide-integration module. If you are using [Single Sign On](https://github.com/nextcloud/Android-SingleSignOn) you may want to also fetch avatars or other images via Glide but with the SSO network stack to avoid problems with self-signed certificates, 2fa and so on.

To make it work, add this dependency to your `build.gradle`-file:

`implementation 'com.github.bumptech.glide:glide:4.10.0'`
`annotationProcessor 'com.github.bumptech.glide:compiler:4.10.0'`

Then create a custom AppGlideModule at the place you want, like this:

```java

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

@GlideModule
public class CustomAppGlideModule extends AppGlideModule {
  @Override
  public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
    super.registerComponents(context, glide, registry);
  }
}
```
