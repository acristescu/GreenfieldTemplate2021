# GreenfieldTemplate (2021 version: Compose)

This is a template that I use for new projects. This the bleeding edge version, if you're interested
in a more mature alternative, please see [this repo](https://github.com/acristescu/GreenfieldTemplate).
It already has some basic stuff setup, such as:

* Jetpack Compose (Alpha 9 as of this writing)
* Retrofit + OKHttp
* Coroutines
* Koin
* MVI-like architecture (leveraging ViewModels)
* JUnit tests (with Mockito)
* Compose UI tests (the current Android Studio Canary refuses to build these)
* Permission dispatcher library

The app itself connects to Flickr and displays a list of images. You can search for
particular tags or you can sort the images.

TODO:
* Migrate Gradle scripts to the Kotlin DSL
* CI Integration (AFAIK there is no easy way to run Compose UI tests on a device farm as with Espresso)
* Maybe extract some usecases