# catapp
## Demostration for an malious android app

### Prerequisites for installation 
- Have a machine running the evilcats-api on an reachable IP addresse. This addresse is needed for a small edit in the catapp code.
- Change the IP addresse for the HTTP POST request in the source code. 
  - Path to file: CatApp\app\src\main\java\com\example\catapp\Fragments\CatsFragment.kt
    - Change the IP addresse and port in line 105
    ![screencap of CatsFragment.kt](https://github.com/jbt-improsec/catapp/tree/main/readme_resources/catapp_screencap_0.png?raw=true)

### Installation
- Download android studio https://developer.android.com/studio
- Enable developer options for demo android device https://developer.android.com/studio/debug/dev-options#enable
- Connect the device to android studio (USB is recommended) https://developer.android.com/studio/run/device
-
