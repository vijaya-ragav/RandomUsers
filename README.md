# RandomUsers

The purpose of this app is to demonstrate the good, modular and scalable Android app using Kotlin, MVVM Architecture using LiveData, ViewModel, coroutines, Retrofit, Hilt & Room Persistence Library.

## Features

Some of the features of the app include

**Effective Networking** - Using a combination of Retrofit and LiveData, we are able to handle networking in the most effective way.

**Modular** - The app is broken into modules of features and libraries which can be combined to build instant-app, complete app.

**MVVM architecture** - Using the lifecycle aware viewmodels, the view observes changes in the model / repository.

**Kotlin** - This app is completely written in Kotlin.

**Android Architecture Components** - Lifecycle awareness has been achieved using a combination of LiveData, ViewModels and Room.

**Offline first architecture** - All the data is tried to be loaded from the db when no network and then updated from the server when back online. This ensures that the app is usable even in an offline mode.

**Kotlin Coroutines** - a new way of managing background threads that can simplify code by reducing the need for callbacks. In this app coroutines are used to handel network calls and Room Persistence Library functionality.

## Libraries

- [Android Support Library](https://developer.android.com/jetpack/androidx)

- [Android Architecture Components](https://developer.android.com/topic/libraries/architecture)

- [Android Data Binding](https://developer.android.com/topic/libraries/data-binding)

- [Retrofit](https://square.github.io/retrofit/) for REST api communication

- [Glide](https://github.com/bumptech/glide) for image loading

- [Room Persistence Library](https://developer.android.com/topic/libraries/architecture/room) for offline mode
