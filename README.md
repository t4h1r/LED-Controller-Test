# Magic CarLight AA Tester — one APK

One Android app with three protocol buttons: **E1 / 7E**, **E2 / 8E**, and **RS / 8E**.

It has two interfaces:
- **Phone tester:** scan/connect to the BLE controller and test each protocol with RED/GREEN/BLUE/WHITE, ON/OFF and brightness.
- **Android Auto:** an IoT car-app screen exposing the same protocol selector and basic controls.

## Important
This project is intentionally a protocol tester. The E1/E2/RS packet formats are candidates and should be treated as unvalidated until one works with the user's hardware. Fully close Magic CarLight before testing because the controller may allow only one BLE connection.

## Build without Android Studio
A GitHub Actions workflow is included in `.github/workflows/build.yml`. Upload this project to a GitHub repository and run the workflow; it produces a debug APK as an artifact. No Android Studio is required.

## Phone test
1. Install the generated APK.
2. Grant Bluetooth permissions.
3. Power the LED controller.
4. Close Magic CarLight.
5. Open **Magic CarLight AA Tester**.
6. Tap **SCAN / CONNECT**.
7. Try E1 first, then E2, then RS. Test RED/GREEN/BLUE.

## Android Auto
The same APK declares an Android Auto IoT service. Whether Android Auto will expose a sideloaded debug build depends on Android Auto's current trusted-source/testing restrictions; the phone tester is the first step to identify the working protocol.
