# acade.mic - Productivity-Enhancing Audio Recording App

This is a group project assignment from the course **CSC13009 – Mobile Application Development (21KTPM2)** at VNU-HCM, University of Science.

## Project contributors
1. Đặng Nhật Hòa ([hoadang0305](https://github.com/hoadang0305))
2. Nguyễn Quỳnh Hương ([qhuongng](https://github.com/qhuongng))
3. Hồ Hữu Tâm ([huutamisme](https://github.com/huutamisme))
4. Bùi Minh Đức ([MinhDuc2412](https://github.com/MinhDuc2412))
5. Vũ Anh Khoa ([VuKhoa23](https://github.com/VuKhoa23))

## General information
This is an Android audio recording application, with additional features and AI integration to improve the user experience and help boost productivity, such as:

- A home-screen widget to quickly start recording anytime, anywhere
- Prescheduled recordings
- Reminders to check out recordings
- Audio transcriptions that can be translated to multiple languages or summarized within the app

The features related to audio transcription are developed using Google Cloud's APIs.

### Points for improvement
- The app's recording behavior when there are incoming calls can be inconsistent.
- Optimize transcription performance by local execution instead of relying on cloud APIs.
- The UI of some activities can be inconsistent and difficult to understand.
- The app lacks cloud storage integration for backup and restoration of recordings.
- The app lacks advanced audio editing features: tempo and pitch modification, track-merging, automatic removal of silence, etc.

## Demo
The demo video for this application is available on [YouTube](https://www.youtube.com/watch?v=WfBfbDjdSws) (in Vietnamese).

## How to run
Since this is an Android Studio project, **[Android Studio](https://developer.android.com/studio?gad_source=1&gclid=Cj0KCQjwudexBhDKARIsAI-GWYVkfoOgTgBlEUi8YbHlayDhIk-Zw9kr72HzRuuthkrB-5S5NyO127saAlwPEALw_wcB&gclsrc=aw.ds)** is required to build and run the source code in this repository. A Google Cloud project with properly configured APIs and credentials is required to test the features related to audio transcription.

1. Create a [Google Cloud project](https://developers.google.com/workspace/guides/create-project).
   
2. In your project dashboard, select **APIs & Services** from the fly-out hamburger menu on the left and enable three APIs: **Cloud Speech-to-Text API**, **Cloud Translation API** and **Generative Language API**.
   
3. In the **APIs & Services** screen, select **Credentials** and generate an API key. Put this API key into the `cloud_api_key` item in the **strings.xml** file, which is located in this project's **res/values** folder.
   
4. Select **IAM & Admin > Service Accounts** from the fly-out hamburger menu on the left. Open the **Actions** menu of the service account linked to your Google Cloud project and choose **Manage keys**.
   
5. In the **Keys** screen, select **Add key > Create new key**. Select the JSON key option and click **Create**. Rename the downloaded file to **credential.json** and replace the original file in the **res/raw** folder with your file.
   
6. Build and run the project on an emulator or on your physical Android device.
