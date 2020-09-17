# Sample Preferences DataStore

This sample app was built following [Preferences DataStore codelab](https://codelabs.developers.google.com/codelabs/android-preferences-datastore/#0).
The app that displays a list of tasks that can be <b>filtered</b> by their completed status and can be <b>sorted</b> by priority and deadline, which are persisted to disk using a ```Preferences DataStore```. The app also demonstrates how to migrate from ```SharedPreferences```.

## CheatSheet

### What's DataStore?
+ DataStore is a new and improved data storage solution aimed at <b>replacing SharedPreferences.</b> 
+ Built on Kotlin coroutines and Flow, DataStore provides two different implementations: ```Proto DataStore```, that lets you store <b>typed objects</b>(backed by protocol buffers) and ```Preferences DataStore```, that stores <b>key-value pairs</b>. 
+ Data is stored <b>asynchronously, consistently, and transactionally</b>, overcoming some of the drawbacks of SharedPreferences.
+ DataStore is ideal for small or simple datasets and does not support partial updates or referential integrity.

